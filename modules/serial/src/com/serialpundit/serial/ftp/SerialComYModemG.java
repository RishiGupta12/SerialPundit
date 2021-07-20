/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2021, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.serialpundit.serial.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.serialpundit.core.util.RingArrayBlockingQueue;
import com.serialpundit.core.util.SerialComCRCUtil;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComTimeOutException;
import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComManager;

// Note some of the variables are shared between sending and receiving methods of this class. 
// If anything is changed it has to be carefully done.

/**
 * <p>Implements YMODEM-G file transfer protocol state machine in Java.</p>
 * 
 * <p>This protocol does not provide software error correction or recovery, but expects the modem 
 * to provide the service. It is a streaming protocol that sends and receives 1K packets in a continuous 
 * stream until instructed to stop. It does not wait for positive acknowledgement after each block is 
 * sent, but rather sends blocks in rapid succession. If any block is unsuccessfully transferred, the 
 * entire transfer is canceled.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComYModemG {

    private final byte SOH   = 0x01;  // Start of header character
    private final byte STX   = 0x02;  // Start of text character
    private final byte EOT   = 0x04;  // End-of-transmission character
    private final byte ACK   = 0x06;  // Acknowledge byte character
    private final byte CAN   = 0x18;  // Cancel
    private final byte SUB   = 0x1A;  // Substitute/CTRL+Z
    private final byte G     = 0x47;  // ASCII capital G character
    private final byte CR    = 0x0D;  // Carriage return
    private final byte LF    = 0x0A;  // Line feed
    private final byte BS    = 0X08;  // Back space
    private final byte SPACE = 0x20;  // Space

    private final SerialComManager scm;
    private final long handle;
    private File[] filesToSend;
    private String currentlySendingFileName;
    private long lengthOfFileToSend;
    private File filesToReceive;
    private final boolean textMode;
    private final ISerialComYmodemProgress progressListener;
    private final SerialComFTPCMDAbort transferState;
    private final int osType;

    private int blockNumber;
    private byte[] block = new byte[1029];   // 1029 bytes ymodem info or data block/packet
    private byte[] block0 = null;
    private BufferedInputStream inStream;    // sent file from local to remote system
    private BufferedOutputStream outStream;  // received file from remote to local system
    private boolean noMoreData;
    private boolean isFirstDataBytePending = false;
    private boolean isSecondDataBytePending = false;
    private boolean isThirdDataBytePending = false;
    private boolean isLineFeedPending = false;
    private boolean isCarriageReturnPending = false;
    private byte firstPendingDataByte;
    private byte secondPendingDataByte;
    private byte thirdPendingDataByte;
    private boolean alreadySentEOFchar = false;
    private byte[] tmpSendBuffer = new byte[2048];
    private byte[] tmpReceiveBuffer = new byte[2526];
    private int mark = -1;
    private int limit = -1;
    private boolean lastCharacterReceivedWasLF = false;
    private boolean lastCharacterReceivedWasCR = false;
    private boolean unprocessedByteInReceivedDataExist = false;
    private byte unprocessedByteInLastReceivedBlock;
    private byte data0 = 0;
    private byte data1 = 0;
    private long numberOfBlocksSent = 0;     // track how many blocks have been sent till now.
    private long numberOfBlocksReceived = 0; // track how many blocks have been received till now.
    private int currentlyProcessingFilenumber = 0;
    private final byte ABORT_CMD[] = new byte[] { CAN, CAN, CAN, CAN, CAN, BS, BS, BS, BS, BS };
    private final SerialComCRCUtil crcCalculator = new SerialComCRCUtil();

    /* This thread inner class read data from serial port and insert in queue. This should read as soon 
     * as data comes to make sure that serial port buffers does not get full. The data collector and data 
     * processor threads can make each other terminate. */
    private class DataCollector implements Callable<Object> {

        private DataProcessor dataProcessor;
        private final RingArrayBlockingQueue<byte[]> dataQueue;
        private volatile boolean exitThread = false;

        public DataCollector(RingArrayBlockingQueue<byte[]> dataQueue) {
            this.dataQueue = dataQueue;
        }

        @Override
        public Object call() throws Exception {

            byte[] data = null;

            while(exitThread == false) {
                try {
                    data = scm.readBytes(handle, 1029);
                } catch (Exception e) {
                    dataProcessor.triggerExit();
                    throw e;
                }
                if((data != null) && (data.length > 0)) {
                    try {
                        dataQueue.offer(data);
                    } catch (Exception e) {
                        dataProcessor.triggerExit();
                        if(exitThread == true) {
                            return null;
                        }
                        throw e;
                    }
                }
                try {
                    if(osType == SerialComPlatform.OS_LINUX || osType == SerialComPlatform.OS_MAC_OS_X) {
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                    if(exitThread == true) {
                        dataProcessor.triggerExit();
                        return null;
                    }
                }              
            }
            return null;
        }

        public void triggerExit() {
            exitThread = true;
            Thread.currentThread().interrupt();
        }

        public void setDataProcessorRef(DataProcessor dataProcessor) {
            this.dataProcessor = dataProcessor;
        }
    }

    /* This thread inner class consumes data from queue, process it and writes data to file. */
    private class DataProcessor implements Callable<Object> {

        private DataCollector dataCollector;
        private final RingArrayBlockingQueue<byte[]> dataQueue;
        private final String receiverDirAbsolutePath = filesToReceive.getAbsolutePath();
        private volatile boolean exitThread = false;

        public DataProcessor(RingArrayBlockingQueue<byte[]> dataQueue) {
            this.dataQueue = dataQueue;
        }

        @Override
        public Object call() throws Exception {

            // Finite state machine's states.
            final int CONNECT       = 0x01;
            final int BLOCK0RCV     = 0x02;
            final int BEGINRCV      = 0x03;
            final int DATABLOCKRCV  = 0x04;
            final int VERIFY        = 0x05;
            final int DUMPDATA      = 0x06;
            final int ABORT         = 0x07;

            int i = 0;
            int x = 0;
            int crcl = 0;
            int state = 0;
            int block0index = 0;
            int dataBlockIndex = 0;
            int blockCRCval = 0;
            boolean receivingBlock0 = true;
            boolean receivingDataBlock = false;
            int spaceLeftInBlock0 = 0;
            int spaceLeftInDataBlock = 0;
            byte[] localData = null;
            byte[] datablock = null;
            byte[] block133 = new byte[133];
            String nameOfFileBeingReceived = null;
            boolean isFileOpen = false;
            int percentOfBlocksReceived = 0;
            long currentlyProcessingFileLength = 0;
            long currentlyProcessingFileModifyInfo = 0;
            long currentlyProcessingFileMode = 0;
            long totalNumberOfDataBytesReadTillNow = 0;
            Exception expt = null;
            int totalBytesRcvWhileRcvBlock0 = 0;
            int extraDataIdx = 0;
            byte[] extraData = null;

            state = CONNECT;
            while(exitThread == false) {
                switch(state) {

                case CONNECT:

                    try {
                        scm.writeSingleByte(handle, G);
                    } catch (Exception e) {
                        expt = e;
                        state = ABORT;
                        break;
                    }   
                    while(true) {
                        try {
                            localData = dataQueue.poll(1, TimeUnit.MINUTES);
                            if(localData == null) {
                                expt = new SerialComTimeOutException("Timedout while trying to connect to file sender !");
                                state = ABORT;
                                break;
                            }else {
                                state = BLOCK0RCV;
                                break;
                            }
                        } catch (InterruptedException e) {
                            if(exitThread == true) {
                                return null;
                            }
                        } catch (Exception e) {
                            expt = e;
                            state = ABORT;
                            break;
                        }   
                    }
                    break;

                case BLOCK0RCV:

                    totalBytesRcvWhileRcvBlock0 = totalBytesRcvWhileRcvBlock0 + localData.length;
                    if(localData[0] == SOH) {
                        block0 = block133;
                        spaceLeftInBlock0 = 133;
                        crcl = 131;
                    }else if(localData[0] == STX) {
                        block0 = block;
                        spaceLeftInBlock0 = 1029;
                        crcl = 1027;
                    }else {
                        expt = new SerialComException("Invalid character received !");
                        state = ABORT;
                        break;
                    }
                    while(true) {
                        if(localData.length < spaceLeftInBlock0) {
                            for(x=0; x < localData.length; x++) {
                                block0[block0index] = localData[x];
                                block0index++;
                                spaceLeftInBlock0--;
                            }
                        }else if(localData.length == spaceLeftInBlock0) {
                            for(x=0; x < localData.length; x++) {
                                block0[block0index] = localData[x];
                                block0index++;
                                spaceLeftInBlock0--;
                            }
                        }else {
                            i = spaceLeftInBlock0;
                            for(x=0; x < i; x++) {
                                block0[block0index] = localData[x];
                                block0index++;
                                spaceLeftInBlock0--;
                            }
                        }
                        if(spaceLeftInBlock0 <= 0) {
                            break;
                        }
                        try {
                            localData = dataQueue.poll(2, TimeUnit.SECONDS);
                            if(localData == null) {
                                expt = new SerialComTimeOutException("Timedout while trying to receive block 0 from file sender !");
                                state = ABORT;
                                break;   
                            }
                        } catch (InterruptedException e) {
                            if(exitThread == true) {
                                state = ABORT;
                                break;
                            }
                        } catch (Exception e) {
                            expt = e;
                            state = ABORT;
                            break;
                        }
                        totalBytesRcvWhileRcvBlock0 = totalBytesRcvWhileRcvBlock0 + localData.length;
                    }

                    receivingBlock0 = true;
                    receivingDataBlock = false;
                    block0index = 0;                       // reset
                    totalNumberOfDataBytesReadTillNow = 0; // reset
                    state = VERIFY;
                    break;

                case BEGINRCV:

                    try {
                        scm.writeSingleByte(handle, G);
                        receivingBlock0 = false;
                        receivingDataBlock = true;                          
                        datablock = null;
                        dataBlockIndex = 0;
                        state = DATABLOCKRCV;
                    } catch (Exception e) {
                        expt = e;
                        state = ABORT;
                    }
                    break;

                case DATABLOCKRCV:

                    if(extraData != null) {
                        // This is entered only when there was extra data from previous data block assembly.
                        if(extraData[extraDataIdx] == SOH) {
                            datablock = block133;
                            spaceLeftInDataBlock = 133;
                            crcl = 131;
                        }else if(extraData[extraDataIdx] == STX) {
                            datablock = block;
                            spaceLeftInDataBlock = 1029;
                            crcl = 1027;
                        }else {
                            expt = new SerialComException("Invalid character received !");
                            state = ABORT;
                            break;
                        }
                        if(spaceLeftInDataBlock > (extraData.length - extraDataIdx)) {
                            dataBlockIndex = 0;
                            for(x=0; x < (extraData.length - extraDataIdx); x++) {
                                datablock[dataBlockIndex] = extraData[extraDataIdx];
                                dataBlockIndex++;
                                extraDataIdx++;
                                spaceLeftInDataBlock--;
                            }
                            extraData = null;
                            extraDataIdx = 0;
                        }else if(spaceLeftInDataBlock < (extraData.length - extraDataIdx)) {
                            for(x=0; x < spaceLeftInDataBlock; x++) {
                                datablock[x] = extraData[extraDataIdx];
                                extraDataIdx++;
                            }
                            receivingDataBlock = true;
                            state = VERIFY;
                        }else {
                            for(x=0; x < (extraData.length - extraDataIdx); x++) {
                                datablock[x] = extraData[extraDataIdx];
                                extraDataIdx++;
                                spaceLeftInDataBlock--;
                            }
                            extraData = null;
                            extraDataIdx = 0;
                            receivingDataBlock = true;
                            state = VERIFY;
                        }
                    }
                    else {
                        // This is entered if there was no extra data or less extra data than the size of current data block.
                        // This means that reading data at-least once is required.
                        try {
                            // blocks if there is no data in queue
                            localData = dataQueue.take();
                        } catch (InterruptedException e) {
                            if(exitThread == true) {
                                state = ABORT;
                                break;
                            }
                        } catch (Exception e) {
                            expt = e;
                            state = ABORT;
                            break;
                        }
                        if(datablock == null) {
                            if(localData[0] == SOH) {
                                datablock = block133;
                                spaceLeftInDataBlock = 133;
                                crcl = 131;
                            }else if(localData[0] == STX) {
                                datablock = block;
                                spaceLeftInDataBlock = 1029;
                                crcl = 1027;
                            }else if(localData[0] == EOT) {
                                try {
                                    scm.writeSingleByte(handle, ACK);
                                    receivingBlock0 = true;
                                    receivingDataBlock = false;                          
                                    datablock = null;
                                    dataBlockIndex = 0;
                                    numberOfBlocksReceived = 0;            // reset
                                    totalNumberOfDataBytesReadTillNow = 0; // reset
                                    state = CONNECT;
                                    break;
                                } catch (Exception e) {
                                    expt = e;
                                    state = ABORT;
                                    break;
                                }
                            }else {
                                expt = new SerialComException("Invalid character received !");
                                state = ABORT;
                                break;
                            }
                            dataBlockIndex = 0;
                        }

                        if(state == DATABLOCKRCV) {
                            while(true) {
                                if(spaceLeftInDataBlock > localData.length) {
                                    for(x=0; x < localData.length; x++) {
                                        datablock[dataBlockIndex] = localData[x];
                                        dataBlockIndex++;
                                        spaceLeftInDataBlock--;
                                    }
                                }else if(spaceLeftInDataBlock < localData.length) {
                                    // extra data is there which will be consumed when assembling next block
                                    for(x=0; x < spaceLeftInDataBlock; x++) {
                                        datablock[dataBlockIndex] = localData[x];
                                        dataBlockIndex++;
                                    }
                                    extraData = localData;
                                    extraDataIdx = x;
                                    state = VERIFY;
                                    break;
                                }else {
                                    for(x=0; x < localData.length; x++) {
                                        datablock[dataBlockIndex] = localData[x];
                                        dataBlockIndex++;
                                    }
                                    state = VERIFY;
                                    break;
                                }
                                try {
                                    localData = dataQueue.take();
                                } catch (InterruptedException e) {
                                    if(exitThread == true) {
                                        state = ABORT;
                                        break;
                                    }
                                } catch (Exception e) {
                                    expt = e;
                                    state = ABORT;
                                    break;
                                }
                            }
                        }
                    }
                    break;

                case VERIFY:

                    if(receivingDataBlock) {
                        // check duplicate block.
                        if(datablock[1] == 0) {
                            expt = new SerialComException("Invalid block number received (block[1]=0) !");
                            state = ABORT;
                            break;
                        }else if(datablock[1] == (byte)(((blockNumber - 1) & 0xFF))) {
                            // ignore duplicate block
                            state = DATABLOCKRCV;
                            break;
                        }else {
                        }
                        // verify block number sequence and block number itself
                        if((datablock[1] != (byte) blockNumber) || (datablock[2] != (byte) ~blockNumber)) {
                            expt = new SerialComException("Invalid block number sequence received !");
                            state = ABORT;
                            break;
                        }
                        // verify CRC value
                        blockCRCval = crcCalculator.getCRC16CCITTValue(datablock, 3, (crcl - 1));
                        if((datablock[crcl] != (byte)(blockCRCval >>> 8)) || (datablock[crcl + 1] != (byte)blockCRCval)) {
                            expt = new SerialComException("Invalid CRC (corrupted data block received) !");
                            state = ABORT;
                            break;
                        }
                        state = DUMPDATA;
                    }
                    else if(receivingBlock0) {
                        if((block0[1] != (byte)0x00) || (block0[2] != (byte)0xFF)) {
                            expt = new SerialComException("Invalid block number received !");
                            state = ABORT;
                            break;
                        }

                        blockCRCval = crcCalculator.getCRC16CCITTValue(block0, 3, (crcl - 1));
                        if((block0[crcl] != (byte)(blockCRCval >>> 8)) || (block0[crcl + 1] != (byte)blockCRCval)) {
                            expt = new SerialComException("Invalid CRC (corrupted block 0 or final block received) !");
                            state = ABORT;
                            break;
                        }

                        // While we were expecting block 0 file information block, sender might indicate end of session 
                        // as there are no more files to be sent, so check it. This thread will instruct data collector 
                        // thread to terminate as well.
                        if((block0[crcl] == (byte)0x00) && (block0[crcl + 1] == (byte)0x00)) {
                            for(i=3; i < block0.length; i++) {
                                if(block0[i] != (byte)0x00) {
                                    break;
                                }
                            }
                            if(i >= block0.length) {
                                dataCollector.triggerExit();
                                return null;
                            }
                        }

                        blockNumber = 1;  // init data block number 
                        state = DUMPDATA;
                    }
                    else {
                    }
                    break;

                case DUMPDATA:

                    if(receivingDataBlock == true) {
                        totalNumberOfDataBytesReadTillNow = totalNumberOfDataBytesReadTillNow + (crcl - 3);
                        try {
                            if(textMode == true) {
                                // for ASCII mode, parse and then flush.
                                processAndWrite(datablock, (crcl - 3));
                            }else {
                                // for binary mode, just flush data as is to file physically.
                                if(currentlyProcessingFileLength != 0) {
                                    if(totalNumberOfDataBytesReadTillNow <= currentlyProcessingFileLength) {
                                        outStream.write(datablock, 3, (crcl - 3));
                                    }else {
                                        outStream.write(datablock, 3, (int)((crcl - 3) - (totalNumberOfDataBytesReadTillNow - currentlyProcessingFileLength)));
                                    }
                                }else {
                                    outStream.write(datablock, 3, (crcl - 3));
                                }
                            }
                        }catch (Exception e) {
                            expt = e;
                            state = ABORT;
                            break;
                        }

                        // update GUI that a block has been received if application has provided 
                        // a listener for this purpose.
                        if(progressListener != null) {
                            numberOfBlocksReceived++;
                            if(currentlyProcessingFileLength > 0) {
                                percentOfBlocksReceived = (int) (((crcl - 3) * numberOfBlocksReceived * 100) / currentlyProcessingFileLength);
                            }else {
                                percentOfBlocksReceived = 100;
                            }
                            if(percentOfBlocksReceived >= 100) {
                                percentOfBlocksReceived = 100;
                            }
                            progressListener.onYmodemReceiveProgressUpdate(nameOfFileBeingReceived, numberOfBlocksReceived, percentOfBlocksReceived);
                        }

                        // update block number to get next data block
                        blockNumber++;
                        if(blockNumber > 0xFF) {
                            blockNumber = 0x00;
                        }
                        datablock = null;     // reset
                        state = DATABLOCKRCV;
                    }
                    else if(receivingBlock0) {
                        // file name:
                        // The data bytes get flushed automatically to file system physically whenever 
                        // BufferedOutputStream's internal buffer gets full and request to write more 
                        // bytes have arrived.
                        for(x=3; x < block0.length; x++) {
                            if(block0[x] == '\0') {
                                break;
                            }
                        }
                        nameOfFileBeingReceived = new String(block0, 3, x-3);
                        if((nameOfFileBeingReceived == null) || (nameOfFileBeingReceived.length() == 0)) {
                            expt = new SerialComException("Sender did not sent file name !");
                            state = ABORT;
                            break;
                        }
                        File namefile = new File(receiverDirAbsolutePath, nameOfFileBeingReceived);
                        if(!namefile.exists()) {
                            try {
                                namefile.createNewFile();
                            } catch (Exception e) {
                                expt = e;
                                state = ABORT;
                                break;
                            }
                        }
                        try {
                            outStream = new BufferedOutputStream(new FileOutputStream(namefile));
                        } catch (FileNotFoundException e) {
                            expt = e;
                            state = ABORT;
                            break;
                        }
                        isFileOpen = true;

                        // file length (number of data bytes):
                        x++;
                        for(i=x; i < block0.length; i++) {
                            if(block0[i] == SPACE) {
                                break;
                            }
                        }
                        currentlyProcessingFileLength = Long.valueOf(new String(block0, x, i - x)).longValue();

                        // file modification info:
                        i++;
                        for(x=i; x < block0.length; x++) {
                            if(block0[x] == SPACE) {
                                break;
                            }
                        }
                        currentlyProcessingFileModifyInfo = Long.valueOf(new String(block0, i, x - i), 8);
                        if(currentlyProcessingFileModifyInfo != 0) {
                            namefile.setLastModified(currentlyProcessingFileModifyInfo);
                        }

                        // file mode:
                        x++;
                        for(i=x; i < block0.length; i++) {
                            if(block0[i] == SPACE) {
                                break;
                            }
                        }
                        currentlyProcessingFileMode = Long.valueOf(new String(block0, x, i - x), 8);
                        if(currentlyProcessingFileMode != 0) {
                            // our translation decision is based on text or binary mode.
                        }

                        try {
                            scm.writeSingleByte(handle, G);
                            receivingBlock0 = false;
                            receivingDataBlock = true;                          
                            datablock = null;
                            dataBlockIndex = 0;
                            state = DATABLOCKRCV;
                        } catch (Exception e) {
                            expt = e;
                            state = ABORT;
                        }
                    }
                    else {
                    }
                    break;

                case ABORT:

                    dataCollector.triggerExit();
                    if(isFileOpen == true) {
                        outStream.flush();
                        outStream.close();
                        isFileOpen = false;
                    }
                    if(expt != null) {
                        throw expt;
                    }else {
                        return null;
                    }
                }
            }

            return null;
        }

        public void triggerExit() {
            exitThread = true;
            Thread.currentThread().interrupt();
        }

        public void setDataCollectorRef(DataCollector dataCollector) {
            this.dataCollector = dataCollector;
        }
    }

    /**
     * <p>Allocates a new SerialComYModemG object with given details and associate it with the given 
     * instance of SerialComManager class. This is used for sending files.</p>
     * 
     * @param scm SerialComManager instance associated with this handle.
     * @param handle of the port on which file is to be communicated.
     * @param filesToSend all the files to be sent to the receiver end.
     * @param textMode if true file will be sent as text file (ASCII mode), if false file will be sent 
     *        as binary file.
     * @param progressListener object of class which implements ISerialComYmodemProgress interface and is 
     *        interested in knowing how many blocks have been sent/received till now.
     * @param transferState if application wish to abort sending/receiving file at instant of time due to 
     *        any reason, it can call abortTransfer method on this object. It can be null if application  
     *        does not wish to abort sending/receiving file explicitly.
     * @param osType operating system on which this application is running.
     */
    public SerialComYModemG(SerialComManager scm, long handle, File[] filesToSend, boolean textMode,
            ISerialComYmodemProgress progressListener, SerialComFTPCMDAbort transferState, int osType) {
        this.scm = scm;
        this.handle = handle;
        this.filesToSend = filesToSend;
        this.textMode = textMode;
        this.progressListener = progressListener;
        this.transferState = transferState;
        this.osType = osType;
    }

    /**
     * <p>Allocates a new SerialComYModemG object with given details and associate it with the given 
     * instance of SerialComManager class. This is used for receiving files.</p>
     * 
     * @param scm SerialComManager instance associated with this handle.
     * @param handle of the port on which file is to be communicated.
     * @param filesToReceive folder in which all files received will be placed.
     * @param textMode if true file will be sent as text file (ASCII mode), if false file will be sent 
     *        as binary file.
     * @param progressListener object of class which implements ISerialComYmodemProgress interface and is 
     *        interested in knowing how many blocks have been sent/received till now.
     * @param transferState if application wish to abort sending/receiving file at instant of time due to 
     *        any reason, it can call abortTransfer method on this object. It can be null if application 
     *        does not wish to abort sending/receiving file explicitly.
     * @param osType operating system on which this application is running.
     */
    public SerialComYModemG(SerialComManager scm, long handle, File filesToReceive, boolean textMode,
            ISerialComYmodemProgress progressListener, SerialComFTPCMDAbort transferState, int osType) {
        this.scm = scm;
        this.handle = handle;
        this.filesToReceive = filesToReceive;
        this.textMode = textMode;
        this.progressListener = progressListener;
        this.transferState = transferState;
        this.osType = osType;
    }

    /**
     * <p>Represents actions to execute in state machine to implement ymodem-g protocol for 
     * receiving files.</p>
     * 
     * @return true on success, false if application instructed to abort.
     * @throws IOException if any I/O error occurs.
     * @throws SerialComException if any I/0 error occurs or operation is aborted.
     */
    public boolean receiveFileY() throws IOException {

        final RingArrayBlockingQueue<byte[]> dataQueue = new RingArrayBlockingQueue<byte[]>(1024);
        final ExecutorService threadpool = Executors.newFixedThreadPool(2);

        final DataCollector taskDataCollection = new DataCollector(dataQueue);
        final DataProcessor taskDataProcessing = new DataProcessor(dataQueue);

        taskDataCollection.setDataProcessorRef(taskDataProcessing);
        taskDataProcessing.setDataCollectorRef(taskDataCollection);

        Future<?> DataProcessorFutureResult = threadpool.submit(taskDataProcessing);
        Future<?> DataCollectionFutureResult = threadpool.submit(taskDataCollection);

        // wait till both the tasks are complete
        while(true) {
            try {
                DataProcessorFutureResult.get();
                DataCollectionFutureResult.get();
            } catch (InterruptedException e) {
                continue; // ignore spurious interrupt
            } catch (Exception e) {
                threadpool.shutdown();
                throw (SerialComException) new SerialComException(e.getMessage()).initCause(e);
            }
            break;
        }

        threadpool.shutdown();
        return true;
    }

    /**
     * <p>Represents actions to execute in state machine to implement ymodem-g protocol
     * for sending files.</p>
     * 
     * @return true on success, false if application instructed to abort.
     * @throws SecurityException if unable to read from file to be sent.
     * @throws IOException if any I/O error occurs.
     * @throws SerialComException if any I/0 error on serial port communication occurs.
     */
    public boolean sendFileY() throws IOException {

        // Finite state machine's states.
        final int CONNECT    = 0x01;
        final int BLOCK0SEND = 0x02;
        final int BEGINSEND  = 0x03;
        final int WAITACK    = 0x04;
        final int ENDTX      = 0x05;
        final int ABORT      = 0x06;
        final int FINISHTX   = 0x07;

        boolean gReceived = false;
        boolean needToSendBlock0 = true;
        String errMsg = null;
        int state = -1;
        byte[] data = null;
        long responseWaitTimeOut = 0;
        long eotWaitTimeOut = 0;
        int percentOfBlocksSent = 0;

        currentlySendingFileName = filesToSend[currentlyProcessingFilenumber].getName();
        lengthOfFileToSend = filesToSend[currentlyProcessingFilenumber].length();
        inStream = new BufferedInputStream(new FileInputStream(filesToSend[currentlyProcessingFilenumber]));

        state = CONNECT;
        while(true) {
            switch(state) {

            case CONNECT:

                gReceived = false;
                responseWaitTimeOut = System.currentTimeMillis() + 60000;  // 1 minute
                while(gReceived != true) {
                    try {
                        data = scm.readBytes(handle, 1024);
                    } catch (SerialComException exp) {
                        inStream.close();
                        throw exp;
                    }
                    if((data != null) && (data.length > 0)) {
                        /* Instead of purging receive buffer and then waiting for G, receive all data because
                         * this approach might be faster. The other side might have opened first time and may 
                         * have flushed garbage data. So receive buffer may contain garbage + G character. */
                        for(int x=0; x < data.length; x++) {
                            if(data[x] == G) {
                                gReceived = true;
                                if(needToSendBlock0 == true) {
                                    state = BLOCK0SEND;
                                }else {
                                    state = BEGINSEND;
                                }
                                break;
                            }
                        }
                    }else {
                        try {
                            Thread.sleep(50); // delay before next attempt to check G character reception.
                        } catch (InterruptedException e) {
                        }
                        // abort if timed-out while waiting for C character.
                        if((gReceived != true) && (System.currentTimeMillis() >= responseWaitTimeOut)) {
                            errMsg = "Timedout while waiting for file receiver to initiate connection setup !";
                            state = ABORT;
                            break;
                        }
                    }

                    // check if application (file sender) wish to cancel sending file.
                    if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
                        inStream.close();
                        scm.writeBytes(handle, ABORT_CMD, 0);
                        return false;
                    }
                }
                break;

            case BLOCK0SEND:

                if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
                    scm.writeBytes(handle, ABORT_CMD, 0);
                    return false;
                }

                assembleBlock0();
                try {
                    scm.writeBytes(handle, block0, 0);
                } catch (SerialComException exp) {
                    inStream.close();
                    throw exp;
                }
                needToSendBlock0 = false;
                block0 = null;   // free memory
                blockNumber = 1; // Block numbering starts from 1 for the first data block sent, reset.
                state = CONNECT;
                break;

            case BEGINSEND:

                // check if sender wish to abort
                if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
                    scm.writeBytes(handle, ABORT_CMD, 0);
                    return false;
                }

                // check if receiver wish to abort
                try {
                    data = scm.readBytes(handle, 2);
                    if((data != null) && (data.length > 0) && (data[0] == CAN) && (data.length > 1) && (data[1] == CAN)) {
                        errMsg = "Received abort command from file receiving end !";
                        state = ABORT;
                        break;
                    }
                } catch (IOException exp) {
                    inStream.close();
                    throw exp;
                }

                assembleBlock();
                if(noMoreData == true) {
                    state = ENDTX; // if the file is empty or all data has been sent goto ENDTX state.
                    break;
                }

                try {
                    scm.writeBytes(handle, block, 0);
                } catch (SerialComException exp) {
                    inStream.close();
                    throw exp;
                }

                if(progressListener != null) {
                    numberOfBlocksSent++;
                    if(lengthOfFileToSend != 0) {
                        percentOfBlocksSent = (int) ((102400 * numberOfBlocksSent) / lengthOfFileToSend);
                    }else {
                        percentOfBlocksSent = 100;
                    }
                    if(percentOfBlocksSent >= 100) {
                        percentOfBlocksSent = 100;
                    }
                    progressListener.onYmodemSentProgressUpdate(currentlySendingFileName, numberOfBlocksSent, percentOfBlocksSent);
                }

                try {
                    if(osType == SerialComPlatform.OS_WINDOWS) {
                        Thread.sleep(250);
                    }else {
                        Thread.sleep(200);
                    }
                }catch (Exception e) {
                }

                blockNumber++;
                break;

            case WAITACK:

                eotWaitTimeOut = System.currentTimeMillis() + 1500; // 1.5 sec

                while(true) {
                    if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
                        scm.writeBytes(handle, ABORT_CMD, 0);
                        return false;
                    }
                    try {
                        data = scm.readBytes(handle, 1);
                        if((data != null) && (data.length > 0) && (data[0] == CAN)) {
                            errMsg = "Received abort command from file receiving end !";
                            state = ABORT;
                            break;
                        }
                    } catch (IOException exp) {
                        inStream.close();
                        throw exp;
                    }

                    if((data != null) && (data.length > 0)) {
                        if(data[0] == ACK) {
                            currentlyProcessingFilenumber++;

                            if(currentlyProcessingFilenumber >= filesToSend.length) {
                                state = FINISHTX;
                                break;
                            }

                            // send next file, reset all stuff
                            lengthOfFileToSend = filesToSend[currentlyProcessingFilenumber].length();
                            inStream = new BufferedInputStream(new FileInputStream(filesToSend[currentlyProcessingFilenumber]));
                            gReceived = false;
                            responseWaitTimeOut = 0;
                            percentOfBlocksSent = 0;
                            needToSendBlock0 = true;
                            noMoreData = false;
                            alreadySentEOFchar = false;
                            isFirstDataBytePending = false;
                            isSecondDataBytePending = false;
                            isThirdDataBytePending = false;
                            isLineFeedPending = false;
                            isCarriageReturnPending = false;
                            mark = -1;
                            limit = -1;
                            lastCharacterReceivedWasLF = false;
                            lastCharacterReceivedWasCR = false;
                            unprocessedByteInReceivedDataExist = false;
                            data0 = 0;
                            data1 = 0;
                            try {
                                Thread.sleep(200); // give some time to receiver to breath
                            }catch (Exception e) {
                            }
                            state = CONNECT;
                            break;
                        }else if(data[0] == CAN) {
                            // receiver might have sent us abort command while we were sending EOT to it.
                            errMsg = "Received abort command from file receiving end !";
                            state = ABORT;
                            break;
                        }else {
                            if(System.currentTimeMillis() >= eotWaitTimeOut) {
                                errMsg = "Timedout while waiting for EOT reception acknowledgement from file receiver !";
                                state = ABORT;
                                break;
                            }
                        }
                        break;
                    }else {
                        if(System.currentTimeMillis() >= eotWaitTimeOut) {
                            errMsg = "Timedout while waiting for EOT reception acknowledgement from file receiver !";
                            state = ABORT;
                            break;
                        }
                    }
                }
                break;

            case ENDTX:

                try {
                    scm.writeSingleByte(handle, EOT);
                } catch (SerialComException exp) {
                    inStream.close();
                    throw exp;
                }
                try {
                    inStream.close();
                } catch (IOException exp) {
                    throw exp;
                }
                numberOfBlocksSent = 0; // reset
                state = WAITACK;
                break;

            case FINISHTX:

                if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
                    scm.writeBytes(handle, ABORT_CMD, 0);
                    return false;
                }

                assembleFinalBlock();
                try {
                    scm.writeBytes(handle, block, 0);
                } catch (SerialComException exp) {
                    inStream.close();
                    throw exp;
                }

                // successfully sent all files, let's go back home happily.
                return true;

            case ABORT:
                /* if any exception occurs, control will not reach here instead exception would 
                 * have been already thrown. This state is entered explicitly to abort executing 
                 * actions in state machine. */
                inStream.close();
                throw new SerialComTimeOutException(errMsg);

            default:
                break;
            }
        }
    }

    /*
     * Prepares ymodem/crc block 0 of 133/1029 bytes in total using CRC-16-CCITT as given below :
     * [SOH/STX][0x00][0xFF][file name\0][file length][space][file modification info][space][file mode][space][padding][2 byte CRC]
     */
    private void assembleBlock0() throws IOException {
        int g = 0;
        int k = 0;
        int blockCRCval = 0;
        byte[] lenfm = null;

        // file name (null terminated)
        currentlySendingFileName = filesToSend[currentlyProcessingFilenumber].getName();
        byte[] nameb = currentlySendingFileName.getBytes();

        // file length in bytes
        long fileLength = filesToSend[currentlyProcessingFilenumber].length();
        String lenstr = String.valueOf(fileLength);
        byte[] lenb = lenstr.getBytes();

        // file modification date information
        long filemoddate = filesToSend[currentlyProcessingFilenumber].lastModified();
        String fmdstr = Long.toOctalString(filemoddate);
        byte[] lenfmd = fmdstr.getBytes();

        // file mode if os is unix-like
        if(osType != SerialComPlatform.OS_WINDOWS) {
            String fmstr = Long.toOctalString(0x8000);
            lenfm = fmstr.getBytes();
        }

        if(lenfm == null) {
            g = 3 + nameb.length + 1 + lenb.length + 1 + lenfmd.length + 2;
        }else {
            g = 3 + nameb.length + 1 + lenb.length + 1 + lenfmd.length + 1 + lenfm.length + 2;
        }

        // populate information gathered about file
        if(g <= 133) {
            block0 = new byte[133];
            block0[0] = SOH;
        }else {
            block0 = new byte[1029];
            block0[0] = STX;
        }
        block0[1] = (byte) 0x00;
        block0[2] = (byte) 0xFF;

        g = 3;
        for(k=0; k < nameb.length; k++) {
            block0[g] = nameb[k];
            g++;
        }
        block0[g] = '\0';
        g++;

        for(k=0; k < lenb.length; k++) {
            block0[g] = lenb[k];
            g++;
        }
        block0[g] = SPACE;
        g++;

        for(k=0; k < lenfmd.length; k++) {
            block0[g] = lenfmd[k];
            g++;
        }
        block0[g] = SPACE;
        g++;

        if(lenfm != null) {
            for(k=0; k < lenfm.length; k++) {
                block0[g] = lenfm[k];
                g++;
            }
        }else {
            block0[g] = (byte) 0x00;
            g++;
        }
        block0[g] = SPACE;
        g++;

        // padding
        for(k=g; k <= (block0.length - 3); k++) {
            block0[k] = (byte) 0x00;
        }

        // append 2 byte CRC value.
        blockCRCval = crcCalculator.getCRC16CCITTValue(block0, 3, (block0.length - 3));
        block0[block0.length - 2] = (byte) (blockCRCval >>> 8); // CRC high byte
        block0[block0.length - 1] = (byte) blockCRCval;         // CRC low byte
    }

    /*
     * Prepares ymodem/crc block indicating that the sender has sent all the files.
     * [SOH][0x00][0xFF][128 times 0x00][2 byte CRC]
     */
    private void assembleFinalBlock() throws IOException {
        int blockCRCval = 0;
        block[0] = SOH;
        block[1] = (byte) 0x00;
        block[2] = (byte) 0xFF;
        for(int d=0; d<128; d++) {
            block[d+3] = (byte) 0x00;
        }
        blockCRCval = crcCalculator.getCRC16CCITTValue(block, 3, 130);
        block[131] = (byte) (blockCRCval >>> 8);
        block[132] = (byte) blockCRCval;
    }

    /* 
     * Prepares ymodem-g block of 1029 bytes in total using CRC-16-CCITT as given below :
     * [SOH][blk #][255-blk #][1024 data bytes][2 byte CRC]
     * 
     * For text mode transfer, lines are terminated by CR+LF, EOF will be indicate
     * by one or more ^Z. If the data ends exactly on a 1024-byte boundary, i.e. 
     * CR in 1023, and LF in 1024, a subsequent sector containing the ^Z EOF character(s)
     * will be sent. This method handles text/ascii mode in operating system independent
     * way. 
     * 
     * This algorithm for processing assumes that a text file may contain following combinations
     * of character sequence with the corresponding data bytes sent to receiving end. The X
     * is a data byte other than CR and LF.
     * 
     * CR LF (send CR LF)
     * CR CR (send CR LF CR LF)
     * CR X  (send CR LF X)
     * LF CR (send CR LF)
     * LF LF (send CR LF CR LF)
     * LF X  (send CR LF X)
     * X  X  (send X X)
     * X  LF (send X CR LF)
     * X  CR (send X CR LF)
     * 
     * This algorithm algorithm takes 2 bytes at a time into consideration and check it against the 
     * above combination. Based on cases above, it will add/remove CR and LF etc characters if 
     * required.
     * 
     * For text mode data is first read into tmpSendBuffer and then parsed. 
     * mark  - points to current byte which needs to be sent to other end
     * limit - refers to number of bytes currently available in tmpSendBuffer
     * 
     * If we need to add extra LF or CR characters, it may be added in current block if there
     * is space or it will be added in next block if current block is full.
     * 
     * @throws IOException if any I/O error occurs.
     */
    private void assembleBlock() throws IOException {
        int x = 0;
        int numBytesRead = 0;
        int blockCRCval = 0;

        // starts at 01 increments by 1, and wraps 0FFH to 00H (not to 01).
        if(blockNumber > 0xFF) {
            blockNumber = 0x00;
        }

        block[0] = STX;
        block[1] = (byte) blockNumber;
        block[2] = (byte) ~blockNumber;

        if(textMode == true) {
            /* file is to be send as a text file. */

            // set index at which first data byte will be saved to send.
            x = 3;

            if(isFirstDataBytePending == true) {
                block[x] = firstPendingDataByte;
                x++;
                isFirstDataBytePending = false; // reset
                if(isSecondDataBytePending == true) {
                    block[x] = secondPendingDataByte;
                    x++;
                    isSecondDataBytePending = false; // reset
                    if(isThirdDataBytePending == true) {
                        block[x] = thirdPendingDataByte;
                        x++;
                        isThirdDataBytePending = false; // reset
                    }
                }
            }

            while(x < 1027) {
                // entering into this loop means that at-least one byte 
                // of space exist in block[] array.

                if((mark == limit) || (mark == -1)) {
                    // indicates we need to read more data from file as all data bytes in
                    // tmpSendBuffer has been sent to file receiver end.
                    limit = inStream.read(tmpSendBuffer, 0, 2048);
                    mark = 0; // reset mark.
                    if(limit < 0) {
                        // 0 bytes; EOF reached.
                        if(isLineFeedPending == true) {
                            data0 = LF;
                            isLineFeedPending = false; // reset
                            data1 = -1;
                        }else if(isCarriageReturnPending == true) {
                            data0 = CR;
                            isCarriageReturnPending = false; // reset
                            data1 = -1;
                        }else {
                            data0 = -1;
                            data1 = -1;
                        }
                    }else if(limit == 1) {
                        // 1 byte (last byte) of data in file.
                        if(isLineFeedPending == true) {
                            data0 = LF;
                            isLineFeedPending = false; // reset
                            data1 = tmpSendBuffer[mark];
                            mark++;
                        }else if(isCarriageReturnPending == true) {
                            data0 = CR;
                            isCarriageReturnPending = false; // reset
                            data1 = tmpSendBuffer[mark];
                            mark++;
                        }else {
                            data0 = tmpSendBuffer[mark];
                            mark++;
                            data1 = -1;
                        }
                    }else {
                        // 2 or more data bytes are there in file.
                        if(isLineFeedPending == true) {
                            data0 = LF;
                            isLineFeedPending = false; // reset
                            data1 = tmpSendBuffer[mark];
                            mark++;
                        }else if(isCarriageReturnPending == true) {
                            data0 = CR;
                            isCarriageReturnPending = false; // reset
                            data1 = tmpSendBuffer[mark];
                            mark++;
                        }else {
                            data0 = tmpSendBuffer[mark];
                            mark++;
                            data1 = tmpSendBuffer[mark];
                            mark++;
                        }
                    }
                }else if(mark == (limit - 1)) {
                    // indicates mark is at last byte of tmpSendBuffer and therefore
                    // 1 more data byte is needed (to be placed in data1 variable).
                    if(isLineFeedPending == true) {
                        data0 = LF;
                        isLineFeedPending = false; // reset
                        data1 = tmpSendBuffer[mark];
                        mark++;
                    }else if(isCarriageReturnPending == true) {
                        data0 = CR;
                        isCarriageReturnPending = false; // reset
                        data1 = tmpSendBuffer[mark];
                        mark++;
                    }else {
                        data0 = tmpSendBuffer[mark];
                        mark++;
                        limit = inStream.read(tmpSendBuffer, 0, 1024);
                        mark = 0; // reset mark.
                        if(limit < 0) {
                            data1 = -1;
                        }else {
                            data1 = tmpSendBuffer[mark];
                            mark++;
                        }
                    }
                }else if((mark == 0) && (limit == -1)) {
                    // indicates there is no more data to be sent.
                    if(isLineFeedPending == true) {
                        data0 = LF;
                        isLineFeedPending = false; // reset
                        data1 = -1;
                    }else if(isCarriageReturnPending == true) {
                        data0 = CR;
                        isCarriageReturnPending = false; // reset
                        data1 = -1;
                    }else {
                        data0 = -1;
                        data1 = -1;
                    }
                }else {
                    // indicates mark is at position from where 2 bytes in tmpSendBuffer
                    // are available to be analyzed.
                    if(isLineFeedPending == true) {
                        data0 = LF;
                        isLineFeedPending = false; // reset
                        data1 = tmpSendBuffer[mark];
                        mark++;
                    }else if(isCarriageReturnPending == true) {
                        data0 = CR;
                        isCarriageReturnPending = false; // reset
                        data1 = tmpSendBuffer[mark];
                        mark++;
                    }else {
                        data0 = tmpSendBuffer[mark];
                        mark++;
                        data1 = tmpSendBuffer[mark];
                        mark++;
                    }
                }

                // When control reached here, both data0 and data1 will have valid values.
                // so algorithm will work with 2 given bytes. The data0/data1 may contain
                // a printable character which can have negative value therefore allow data0 
                // and data1 to contain anything other than -1.
                if(data0 == -1) {
                    // indicates EOF reached.
                    if(alreadySentEOFchar == true) {
                        // EOF have been sent already in last block.
                        noMoreData = true;
                        return;
                    }else {
                        // assemble last block with ^Z padding. if x == 3,
                        // whole block will contain ^Z only as data bytes.
                        for(x = x + 0; x < 1027; x++) {
                            block[x] = SUB;
                        }
                        alreadySentEOFchar = true;
                    }
                }else if((data0 != -1) && (data1 == -1)) {
                    // indicates last byte of data in file.
                    if((data0 == LF) || (data0 == CR)) {
                        block[x] = CR;
                        x++;
                        if(x < 1027) {
                            block[x] = LF;
                            x++;
                        }else {
                            // now LF character will be sent in next block
                            isFirstDataBytePending = true;
                            firstPendingDataByte = LF;
                        }
                    }else {
                        block[x] = data0;
                        x++;
                    }
                }else {
                    // indicates 2 bytes of data are there and need to be processed.
                    if((data0 != LF) && (data0 != CR)) {
                        if((data1 != LF) && (data1 != CR)) {
                            // indicates XX case.
                            block[x] = data0;
                            x++;
                            if(x < 1027) {
                                block[x] = data1;
                                x++;
                            }else {
                                isFirstDataBytePending = true;
                                firstPendingDataByte = data1;
                            }
                        }else if(data1 == LF) {
                            // indicates XLF case.
                            block[x] = data0;
                            x++;
                            isLineFeedPending = true;
                        }else {
                            // indicates XCR case.
                            block[x] = data0;
                            x++;
                            isCarriageReturnPending = true;
                        }
                    }else if(((data0 == CR) && (data1 == LF)) || ((data0 == LF) && (data1 == CR))) {
                        // indicates LFCR or CRLF case.
                        block[x] = CR;
                        x++;
                        if(x < 1027) {
                            block[x] = LF;
                            x++;
                        }else {
                            isFirstDataBytePending = true;
                            firstPendingDataByte = LF;
                        }
                    }else if((data0 == LF) && (data1 != CR) && (data1 != LF)) {
                        // indicates LFX case.
                        block[x] = CR;
                        x++;
                        if(x < 1027) {
                            block[x] = LF;
                            x++;
                            if(x < 1027) {
                                block[x] = data1;
                                x++;
                            }else {
                                isFirstDataBytePending = true;
                                firstPendingDataByte = data1;
                            }
                        }else {
                            isFirstDataBytePending = true;
                            firstPendingDataByte = LF;
                            isSecondDataBytePending = true;
                            secondPendingDataByte = data1;
                        }
                    }else if((data0 == CR) && (data1 != CR) && (data1 != LF)) {
                        // indicates CRX case.
                        block[x] = CR;
                        x++;
                        if(x < 1027) {
                            block[x] = LF;
                            x++;
                            if(x < 1027) {
                                block[x] = data1;
                                x++;
                            }else {
                                isFirstDataBytePending = true;
                                firstPendingDataByte = data1;
                            }
                        }else {
                            isFirstDataBytePending = true;
                            firstPendingDataByte = LF;
                            isSecondDataBytePending = true;
                            secondPendingDataByte = data1;
                        }
                    }else if(((data0 == LF) && (data1 == LF)) || ((data0 == CR) && (data1 == CR))) {
                        // indicates LFLF or CRCR case.
                        block[x] = CR;
                        x++;
                        if(x < 1027) {
                            block[x] = LF;
                            x++;
                            if(x < 1027) {
                                block[x] = CR;
                                x++;
                                if(x < 1027) {
                                    block[x] = LF;
                                    x++;
                                }else {
                                    isFirstDataBytePending = true;
                                    firstPendingDataByte = LF;
                                }
                            }else {
                                isFirstDataBytePending = true;
                                firstPendingDataByte = CR;
                                isSecondDataBytePending = true;
                                secondPendingDataByte = LF;
                            }
                        }else {
                            isFirstDataBytePending = true;
                            firstPendingDataByte = LF;
                            isSecondDataBytePending = true;
                            secondPendingDataByte = CR;
                            isThirdDataBytePending = true;
                            thirdPendingDataByte = LF;
                        }
                    }else {
                    }
                }
            } // end while loop
        }else {
            /* file is to be send as a binary file. */

            // read data from the file to be sent.
            numBytesRead = inStream.read(block, 3, 1024);
            if(numBytesRead == 1024) {
            }else if(numBytesRead > 0) {
                // assembling last block with padding.
                for(x = numBytesRead + 3; x < 1027; x++) {
                    block[x] = SUB;
                }
            }else {
                // EOF encountered.
                noMoreData = true;
                return;
            }
        }

        // append 2 byte CRC value.
        blockCRCval = crcCalculator.getCRC16CCITTValue(block, 3, 1026);
        block[1027] = (byte) (blockCRCval >>> 8); // CRC high byte
        block[1028] = (byte) blockCRCval;         // CRC low byte
    }

    /* 
     * This algorithm strip all ^Z characters from received data. Further it will add or remove
     * CR and LF characters as needed based on operating system this application is running on.
     * It process 2 consecutive bytes at a time and handle the following cases (where X represent
     * any byte other than CR, LF and ^Z) :
     * 
     * LF LF,
     * LF CR,
     * LF ^Z,
     * LF X,
     * CR CR,
     * CR LF,
     * CR ^Z,
     * CR X,
     * ^Z ^Z,
     * ^Z CR,
     * ^Z LF,
     * ^Z X,
     * X X,
     * X CR,
     * X LF,
     * X ^Z
     * 
     * It is possible that last data byte (130th or 1026th byte) in current block can not be processed 
     * because we have to analyze next data byte which will be available to us only in the next data 
     * block received. So we save that last byte and process it next time this method is called.
     * 
     * @throws IOException if any I/O error occurs.
     */
    private void processAndWrite(byte[] block, int dataSize) throws IOException {

        mark = 3;  // init + reset
        int q = 0; // init + reset
        int processTillIndex = -1;

        if(dataSize == 1024) {
            processTillIndex = 1025;
        }else {
            processTillIndex = 129;
        }

        while(mark <= processTillIndex) {
            if(unprocessedByteInReceivedDataExist == false) {
                if(lastCharacterReceivedWasLF) {
                    data0 = LF;
                    lastCharacterReceivedWasLF = false; // reset
                }else if(lastCharacterReceivedWasCR) {
                    data0 = CR;
                    lastCharacterReceivedWasCR = false; // reset
                }else {
                    data0 = block[mark];
                    mark++;
                }
                data1 = block[mark];
                mark++;
            }else {
                // there was a pending byte from last block received to be tested against all cases.
                data0 = unprocessedByteInLastReceivedBlock;
                unprocessedByteInReceivedDataExist = false; // reset
                data1 = block[mark];
                mark++;
            }

            if(data0 == CR) {
                if(data1 == LF) {
                    // represent CRLF case.
                    if(osType == SerialComPlatform.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX)) {
                        tmpReceiveBuffer[q] = LF;
                        q = q + 1;
                    }else {
                    }
                }else if(data1 == CR) {
                    // represent CRCR case.
                    if(osType == SerialComPlatform.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        tmpReceiveBuffer[q + 2] = CR;
                        tmpReceiveBuffer[q + 3] = LF;
                        q = q + 4;
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX)) {
                        tmpReceiveBuffer[q] = LF;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else {
                    }
                }else if(data1 == SUB) {
                    // represent CRSUB case.
                    if(osType == SerialComPlatform.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX)) {
                        tmpReceiveBuffer[q] = LF;
                        q = q + 1;
                    }else {
                    }
                }else {
                    // represent CRX case.
                    if(osType == SerialComPlatform.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        tmpReceiveBuffer[q + 2] = data1;
                        q = q + 3;
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX)) {
                        tmpReceiveBuffer[q] = LF;
                        tmpReceiveBuffer[q + 1] = data1;
                        q = q + 2;
                    }else {
                    }
                }
            }else if(data0 == LF) {
                if(data1 == LF) {
                    // represent LFLF case.
                    if(osType == SerialComPlatform.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        tmpReceiveBuffer[q + 2] = CR;
                        tmpReceiveBuffer[q + 3] = LF;
                        q = q + 4;
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX)) {
                        tmpReceiveBuffer[q] = LF;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else {
                    }
                }else if(data1 == CR) {
                    // represent LFCR case.
                    if(osType == SerialComPlatform.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX)) {
                        tmpReceiveBuffer[q] = LF;
                        q = q + 1;
                    }else {
                    }
                }else if(data1 == SUB) {
                    // represent LFSUB case.
                    if(osType == SerialComPlatform.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX)) {
                        tmpReceiveBuffer[q] = LF;
                        q = q + 1;
                    }else {
                    }
                }else {
                    // represent LFX case.
                    if(osType == SerialComPlatform.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        tmpReceiveBuffer[q + 2] = data1;
                        q = q + 3;
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX)) {
                        tmpReceiveBuffer[q] = LF;
                        tmpReceiveBuffer[q + 1] = data1;
                        q = q + 2;
                    }else {
                    }
                }
            }else if(data0 == SUB) {
                if(data1 == LF) {
                    // represent SUBLF case.
                    // we need to check that whether next character is CR or LF and then only
                    // we can decide what to do with this LF. So make this LF as pending and
                    // let it process with next character in next iteration of this loop.
                    lastCharacterReceivedWasLF = true;
                }else if(data1 == CR) {
                    // represent SUBCR case.
                    lastCharacterReceivedWasCR = true;
                }else if(data1 == SUB) {
                    // represent SUBSUB case.
                    // do nothing, drop/strip this character.
                }else {
                    // represent SUBX case.
                    tmpReceiveBuffer[q] = data1;
                    q = q + 1;
                }
            }else {
                if(data1 == LF) {
                    // represent XLF case.
                    tmpReceiveBuffer[q] = data0;
                    q = q + 1;
                    lastCharacterReceivedWasLF = true;
                }else if(data1 == CR) {
                    // represent XCR case.
                    tmpReceiveBuffer[q] = data0;
                    q = q + 1;
                    lastCharacterReceivedWasCR = true;
                }else if(data1 == SUB) {
                    // represent XSUB case.
                    tmpReceiveBuffer[q] = data0;
                    q = q + 1;
                }else {
                    // represent XX case.
                    tmpReceiveBuffer[q] = data0;
                    tmpReceiveBuffer[q + 1] = data1;
                    q = q + 2;
                }
            }
        } // end while loop

        // write processed data bytes to file in file system.
        outStream.write(tmpReceiveBuffer, 0, q);

        if((mark == 1026) || (mark == 130)) {
            // indicates last byte in block array could not be processed as one more bytes was needed 
            // to test against all cases. so save this byte and process it next block of data received.
            unprocessedByteInReceivedDataExist = true;
            unprocessedByteInLastReceivedBlock = block[mark];
        }
    }
}