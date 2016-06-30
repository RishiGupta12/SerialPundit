/*
 * This file is part of SerialPundit project and software.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.serialpundit.serial.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.serialpundit.core.util.RingArrayBlockingQueue;
import com.serialpundit.core.util.SerialComCRCUtil;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComTimeOutException;
import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComManager;

/**
 * <p>Implements state machine to implement YMODEM-G file transfer protocol in Java.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComYModemG {

    private final byte SOH   = 0x01;  // Start of header character
    private final byte STX   = 0x02;  // Start of text character
    private final byte EOT   = 0x04;  // End-of-transmission character
    private final byte ACK   = 0x06;  // Acknowledge byte character
    private final byte NAK   = 0x15;  // Negative-acknowledge character
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
    private byte[] block = new byte[1029];     // 1029 bytes ymodem info or data block/packet
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
    private boolean lastCharacterReceivedWasCAN = false;
    private int currentlyProcessingFilenumber = 0;
    private final byte ABORT_CMD[] = new byte[] { CAN, CAN, CAN, CAN, CAN, BS, BS, BS, BS, BS };
    private final SerialComCRCUtil crcCalculator = new SerialComCRCUtil();

    /* Job of this inner class is to read data and insert in queue. This should read as soon as data 
     * comes to make sure that serial port buffers does not get full. */
    private class DataCollector implements Runnable {

        private RingArrayBlockingQueue<byte[]> dataQueue = null;

        public DataCollector(RingArrayBlockingQueue<byte[]> dataQueue) {
            this.dataQueue = dataQueue;
        }

        @Override
        public void run() {

            long connectTimeOut = 0;
            byte[] data = null;

            // Stage 1 initiate connection
            connectTimeOut = System.currentTimeMillis() + 60000; // 1 minute
            while(true) {
                try {
                    scm.writeSingleByte(handle, G);
                } catch (SerialComException exp) {
                    //throw exp; TODO
                }
                try {
                    data = scm.readBytes(handle, 1500);
                } catch (SerialComException exp) {
                    // todo throw exp;
                }
                if((data != null) && (data.length > 0)) {
                    try {
                        dataQueue.offer(data);
                    } catch (Exception e) {
                        // todo throw exp;
                    }
                    try {
                        scm.writeSingleByte(handle, G);
                    } catch (SerialComException exp) {
                        //throw exp; TODO
                    }
                    break;
                }else {
                    if(System.currentTimeMillis() >= connectTimeOut) {
                        //todo abort
                    }
                }
            }

            // Stage 2 keep reading data
            while(true) { // todo how to come out of this loop and terminate thread
                try {
                    data = scm.readBytes(handle, 1024);
                } catch (SerialComException exp) {
                    // todo throw exp;
                }
                if((data != null) && (data.length > 0)) {
                    try {
                        dataQueue.offer(data);
                    } catch (Exception e) {
                        // todo throw exp;
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /* Job of this inner class is to consume data from queue, process it and write to file. */
    private class DataProcessor implements Runnable {

        private byte[] localData;
        int block0index = 0;
        private boolean assembleBuffer = true;
        private boolean receivingBlock0 = true;

        private RingArrayBlockingQueue<byte[]> dataQueue = null;

        public DataProcessor(RingArrayBlockingQueue<byte[]> dataQueue) {
            this.dataQueue = dataQueue;
        }

        @Override
        public void run() {
            
            // Process block 0
            if(receivingBlock0) {
                try {
                    localData = dataQueue.take();
                } catch (InterruptedException e) {
                }
                if(localData != null && localData.length > 0) {
                    try {
                        if(localData[0] == SOH) {
                            block0 = new byte[133];
                        }else if(localData[0] == STX) {
                            block0 = new byte[1029];
                        }else {
                            //todo thro werror
                        }
                    } catch (Exception e) {
                        //todo thro werror
                    }
                    while(assembleBuffer == true) {
                        if(localData.length < block0.length) {
                            for(int x=0; x < localData.length; x++) {
                                block0[block0index] = localData[x];
                                block0index++;
                            }
                        }else if(localData.length == block0.length) {
                            
                        }else {
                            
                        }
                    }
                }
            }
            
            // Process data block
            
            // Process final block
            
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
     * <p>Represents actions to execute in state machine to implement ymodem-1k protocol
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
                            Thread.sleep(100); // delay before next attempt to check G character reception.
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
                    Thread.sleep(200);
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
                            lastCharacterReceivedWasCAN = false;
                            data0 = 0;
                            data1 = 0;
                            try {
                                Thread.sleep(300); // give some time to receiver to breath
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
     * Prepares ymodem-1k block of 1029 bytes in total using CRC-16-CCITT as given below :
     * [SOH][blk #][255-blk #][1024 data bytes][2 byte CRC]
     * 
     * For text mode transfer, lines are terminated by CR+LF, EOF will be indicate
     * by one or more ^Z. If the data ends exactly on a 1024-byte boundary, i.e. 
     * CR in 1023, and LF in 1024, a subsequent sector containing the ^Z EOF character(s)
     * will be sent. This method handles text/ascii mode in operating system independent
     * way. 
     * 
     * Algorithm for processing assumes that a text file may contain following combinations
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
     * Algorithm takes 2 bytes at a time into consideration and check it against above combination.
     * Based on cases above it will add/remove CR and LF etc characters.
     * 
     * For text mode data is first read into tmpSendBuffer and then parsed. The mark points to 
     * current byte which needs to be sent to other end. The limit refers to number of bytes 
     * currently available in tmpSendBuffer.
     * 
     * If we need to add extra LF or CR characters, it may be added in current block if there
     * is space or will be added in next block if current block is full.
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

                // when control reached here, both data0 and data1 will have valid values.
                // so algorithm will work with 2 given bytes.
                if(data0 < 0) {
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
                }else if((data0 >= 0) && (data1 < 0)) {
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

    /**
     * <p>Represents actions to execute in state machine to implement 
     * ymodem-g protocol for receiving files.</p>
     * 
     * @return true on success, false if application instructed to abort.
     * @throws IOException if any I/O error occurs.
     * @throws SerialComException if any I/0 error on serial port communication occurs.
     */
    public boolean receiveFileY() throws IOException {

        Thread dataCollectorThread;
        Thread dataProcessorThread;
        RingArrayBlockingQueue<byte[]> dataQueue = new RingArrayBlockingQueue<byte[]>(1024);

        return false;
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
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX) || (osType == SerialComPlatform.OS_SOLARIS)
                            || (osType == SerialComPlatform.OS_FREEBSD) || (osType == SerialComPlatform.OS_NETBSD) || (osType == SerialComPlatform.OS_OPENBSD)
                            || (osType == SerialComPlatform.OS_ANDROID)) {
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
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX) || (osType == SerialComPlatform.OS_SOLARIS)
                            || (osType == SerialComPlatform.OS_FREEBSD) || (osType == SerialComPlatform.OS_NETBSD) || (osType == SerialComPlatform.OS_OPENBSD)
                            || (osType == SerialComPlatform.OS_ANDROID)) {
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
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX) || (osType == SerialComPlatform.OS_SOLARIS)
                            || (osType == SerialComPlatform.OS_FREEBSD) || (osType == SerialComPlatform.OS_NETBSD) || (osType == SerialComPlatform.OS_OPENBSD)
                            || (osType == SerialComPlatform.OS_ANDROID)) {
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
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX) || (osType == SerialComPlatform.OS_SOLARIS)
                            || (osType == SerialComPlatform.OS_FREEBSD) || (osType == SerialComPlatform.OS_NETBSD) || (osType == SerialComPlatform.OS_OPENBSD)
                            || (osType == SerialComPlatform.OS_ANDROID)) {
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
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX) || (osType == SerialComPlatform.OS_SOLARIS)
                            || (osType == SerialComPlatform.OS_FREEBSD) || (osType == SerialComPlatform.OS_NETBSD) || (osType == SerialComPlatform.OS_OPENBSD)
                            || (osType == SerialComPlatform.OS_ANDROID)) {
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
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX) || (osType == SerialComPlatform.OS_SOLARIS)
                            || (osType == SerialComPlatform.OS_FREEBSD) || (osType == SerialComPlatform.OS_NETBSD) || (osType == SerialComPlatform.OS_OPENBSD)
                            || (osType == SerialComPlatform.OS_ANDROID)) {
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
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX) || (osType == SerialComPlatform.OS_SOLARIS)
                            || (osType == SerialComPlatform.OS_FREEBSD) || (osType == SerialComPlatform.OS_NETBSD) || (osType == SerialComPlatform.OS_OPENBSD)
                            || (osType == SerialComPlatform.OS_ANDROID)) {
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
                    }else if((osType == SerialComPlatform.OS_MAC_OS_X) || (osType == SerialComPlatform.OS_LINUX) || (osType == SerialComPlatform.OS_SOLARIS)
                            || (osType == SerialComPlatform.OS_FREEBSD) || (osType == SerialComPlatform.OS_NETBSD) || (osType == SerialComPlatform.OS_OPENBSD)
                            || (osType == SerialComPlatform.OS_ANDROID)) {
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