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

import com.serialpundit.core.util.SerialComCRCUtil;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComTimeOutException;
import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComManager;

/**
 * <p>Implements YMODEM-CRC file transfer protocol state machine in Java.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComYModemCRC {

    private final byte SOH   = 0x01;  // Start of header character
    private final byte STX   = 0x02;  // Start of text character
    private final byte EOT   = 0x04;  // End-of-transmission character
    private final byte ACK   = 0x06;  // Acknowledge byte character
    private final byte NAK   = 0x15;  // Negative-acknowledge character
    private final byte CAN   = 0x18;  // Cancel
    private final byte SUB   = 0x1A;  // Substitute/CTRL+Z
    private final byte C     = 0x43;  // ASCII capital C character
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
    private byte[] block = new byte[133];     // 133  bytes ymodem info or data block/packet
    private byte[] block0 = null;             // can be 133 or 1029 depending upon size of file info
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
    private byte[] tmpSendBuffer = new byte[1024];
    private byte[] tmpReceiveBuffer = new byte[512];
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

    /**
     * <p>Allocates a new SerialComYModemCRC object with given details and associate it with the given 
     * instance of SerialComManager class. This is used for sending files.</p>
     * 
     * @param scm SerialComManager instance associated with this handle.
     * @param handle of the port on which file is to be communicated.
     * @param filesToSend all the files to be sent to the receiver end.
     * @param textMode if true file will be sent as text file (ASCII mode), if false file will be sent 
     *         as binary file.
     * @param progressListener object of class which implements ISerialComYmodemProgress interface and is 
     *         interested in knowing how many blocks have been sent/received till now.
     * @param transferState if application wish to abort sending/receiving file at instant of time due to 
     *         any reason, it can call abortTransfer method on this object. It can be null if application  
     *         does not wish to abort sending/receiving file explicitly.
     * @param osType operating system on which this application is running.
     */
    public SerialComYModemCRC(SerialComManager scm, long handle, File[] filesToSend, boolean textMode,
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
     * <p>Allocates a new SerialComYModemCRC object with given details and associate it with the given 
     * instance of SerialComManager class. This is used for receiving files.</p>
     * 
     * @param scm SerialComManager instance associated with this handle.
     * @param handle of the port on which file is to be communicated.
     * @param filesToReceive folder in which all files received will be placed.
     * @param textMode if true file will be sent as text file (ASCII mode), if false file will be sent 
     *         as binary file.
     * @param progressListener object of class which implements ISerialComYmodemProgress interface and is 
     *         interested in knowing how many blocks have been sent/received till now.
     * @param transferState if application wish to abort sending/receiving file at instant of time due to 
     *         any reason, it can call abortTransfer method on this object. It can be null if application 
     *         does not wish to abort sending/receiving file explicitly.
     * @param osType operating system on which this application is running.
     */
    public SerialComYModemCRC(SerialComManager scm, long handle, File filesToReceive, boolean textMode,
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
     * <p>Represents actions to execute in state machine to implement ymodem/crc protocol
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
        final int RESEND     = 0x05;
        final int SENDNEXT   = 0x06;
        final int ENDTX      = 0x07;
        final int ABORT      = 0x08;
        final int FINISHTX   = 0x09;

        boolean cReceived = false;
        boolean eotAckReceptionTimerInitialized = false;
        boolean needToSendBlock0 = true;
        boolean waitForBlock0ACK = true;
        boolean waitForFinalBlockACK = false;
        String errMsg = null;
        int retryCount = 0;
        int state = -1;
        byte[] data = null;
        long responseWaitTimeOut = 0;
        long eotAckWaitTimeOutValue = 0;
        int percentOfBlocksSent = 0;
        boolean finAckReceptionTimerInitialized = false;
        long finAckWaitTimeOutValue = 0;
        boolean showSentProgress = false;

        currentlySendingFileName = filesToSend[currentlyProcessingFilenumber].getName();
        lengthOfFileToSend = filesToSend[currentlyProcessingFilenumber].length();
        inStream = new BufferedInputStream(new FileInputStream(filesToSend[currentlyProcessingFilenumber]));

        state = CONNECT;
        while(true) {
            switch(state) {

            case CONNECT:
                cReceived = false;
                responseWaitTimeOut = System.currentTimeMillis() + 60000;
                while(cReceived != true) {
                    try {
                        data = scm.readBytes(handle, 1024);
                    } catch (SerialComException exp) {
                        inStream.close();
                        throw exp;
                    }
                    if((data != null) && (data.length > 0)) {
                        /* Instead of purging receive buffer and then waiting for C, receive all data because
                         * this approach might be faster. The other side might have opened first time and may 
                         * have flushed garbage data. So receive buffer may contain garbage + C character. */
                        for(int x=0; x < data.length; x++) {
                            if(data[x] == C) {
                                cReceived = true;
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
                            Thread.sleep(100); // delay before next attempt to check C character reception.
                        } catch (InterruptedException e) {
                        }
                        // abort if timed-out while waiting for C character.
                        if((cReceived != true) && (System.currentTimeMillis() >= responseWaitTimeOut)) {
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
                if(retryCount > 10) {
                    errMsg = "Maximum number of retries reached while sending block 0 to receiver end !";
                    state = ABORT;
                    break;
                }
                assembleBlock0();
                try {
                    scm.writeBytes(handle, block0, 0);
                } catch (SerialComException exp) {
                    inStream.close();
                    throw exp;
                }
                needToSendBlock0 = false;
                waitForBlock0ACK = true;
                showSentProgress = false;
                state = WAITACK;
                break;

            case BEGINSEND:
                blockNumber = 1; // Block numbering starts from 1 for the first data block sent.
                assembleBlock();

                // if the file is empty goto ENDTX state.
                if(noMoreData == true) {
                    state = ENDTX;
                    break;
                }

                try {
                    scm.writeBytes(handle, block, 0);
                } catch (SerialComException exp) {
                    inStream.close();
                    throw exp;
                }
                showSentProgress = true;
                state = WAITACK;
                break;

            case RESEND:
                if(retryCount > 10) {
                    errMsg = "Maximum number of retries reached while sending same data block !";
                    state = ABORT;
                    break;
                }
                try {
                    scm.writeBytes(handle, block, 0);
                } catch (SerialComException exp) {
                    inStream.close();
                    throw exp;
                }
                showSentProgress = true;
                state = WAITACK;
                break;

            case WAITACK:
                responseWaitTimeOut = System.currentTimeMillis() + 60000; // 1 minute.

                while(true) {
                    // check if application (file sender) wish to cancel sending file.
                    if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
                        inStream.close();
                        scm.writeBytes(handle, ABORT_CMD, 0);
                        return false;
                    }

                    // delay before next attempt to read from serial port
                    try {
                        if(noMoreData != true) {
                            Thread.sleep(120);
                        }else {
                            // give sufficient time to remote end to close file resources and be able to 
                            // acknowledge us happily.
                            Thread.sleep(250);
                        }
                    } catch (InterruptedException e) {
                    }

                    // try to read data from serial port
                    try {
                        data = scm.readBytes(handle, 1);
                    } catch (SerialComException exp) {
                        inStream.close();
                        throw exp;
                    }

                    /* if data received process it. if long timeout occurred abort otherwise retry reading from serial port.
                     * if nothing received at all abort. */
                    if((data != null) && (data.length > 0)) {
                        break;
                    }else {
                        if(noMoreData == true) {
                            state = ENDTX;
                            break;
                        }
                        if(System.currentTimeMillis() >= responseWaitTimeOut) {
                            if(noMoreData == true) {
                                errMsg = "Timedout while waiting for EOT reception acknowledgement from file receiver !";
                            }else if(waitForBlock0ACK == true) {
                                errMsg = "Timedout while waiting for block0 reception acknowledgement from file receiver !";
                            }else {
                                errMsg = "Timedout while waiting for block reception acknowledgement from file receiver !";
                            }
                            state = ABORT;
                            break;
                        }
                    }
                }

                if(state == WAITACK) {
                    if(noMoreData != true) {	
                        if(data[0] == ACK) {
                            if(lastCharacterReceivedWasCAN == true) {
                                // <CAN> <ACK> is invalid sequence.
                                if(waitForBlock0ACK != true) {
                                    retryCount++;
                                    state = RESEND;
                                }else {
                                    retryCount++;
                                    state = BLOCK0SEND;
                                }
                                break;
                            }
                            if(waitForBlock0ACK != true) {
                                state = SENDNEXT;
                            }else {
                                state = CONNECT;
                                waitForBlock0ACK = false;
                            }
                        }else if(data[0] == NAK) {
                            // indicates both <NAK> only and <CAN> <NAK> sequence reception.
                            if(waitForBlock0ACK != true) {
                                retryCount++;
                                state = RESEND;
                            }else {
                                retryCount++;
                                state = BLOCK0SEND;
                            }
                        }else if(data[0] == CAN) {
                            if(data.length >= 2) {
                                if(data[1] == CAN) {
                                    errMsg = "Received abort command from file receiving end !";
                                    state = ABORT;
                                    break;
                                }else {
                                    // probably it is noise, so re-send block.
                                    if(waitForBlock0ACK != true) {
                                        retryCount++;
                                        state = RESEND;
                                    }else {
                                        retryCount++;
                                        state = BLOCK0SEND;
                                    }
                                }
                            }
                            if(lastCharacterReceivedWasCAN == true) {
                                errMsg = "Received abort command from file receiving end !";
                                state = ABORT;
                                break;
                            }
                            lastCharacterReceivedWasCAN = true;
                        }else {
                            errMsg = "Invalid data byte : " + data[0] + " received from file receiver !";
                            state = ABORT;
                            break;
                        }

                        // update GUI that a block has been sent if application has provided a listener
                        // for this purpose.
                        if(showSentProgress == true) {
                            if(progressListener != null) {
                                numberOfBlocksSent++;
                                if(lengthOfFileToSend != 0) {
                                    percentOfBlocksSent = (int) ((12800 * numberOfBlocksSent) / lengthOfFileToSend);
                                }else {
                                    percentOfBlocksSent = 100;
                                }
                                if(percentOfBlocksSent >= 100) {
                                    percentOfBlocksSent = 100;
                                }
                                progressListener.onYmodemSentProgressUpdate(currentlySendingFileName, numberOfBlocksSent, percentOfBlocksSent);
                            }
                        }
                    }else {
                        // if there is no more data to be sent, we are looking for ACK after sending EOT.
                        if(data[0] == ACK) {
                            if(waitForFinalBlockACK == true) {
                                // successfully sent all files, let's go back home happily.
                                return true;
                            }else {
                                inStream.close();
                                currentlyProcessingFilenumber++;

                                if(currentlyProcessingFilenumber >= filesToSend.length) {
                                    state = FINISHTX;
                                    break;
                                }

                                // send next file, reset all stuff
                                lengthOfFileToSend = filesToSend[currentlyProcessingFilenumber].length();
                                inStream = new BufferedInputStream(new FileInputStream(filesToSend[currentlyProcessingFilenumber]));
                                cReceived = false;
                                eotAckReceptionTimerInitialized = false;
                                retryCount = 0;
                                responseWaitTimeOut = 0;
                                eotAckWaitTimeOutValue = 0;
                                percentOfBlocksSent = 0;
                                needToSendBlock0 = true;
                                noMoreData = false;
                                alreadySentEOFchar = false;
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
                                state = CONNECT;
                            }
                            break;
                        }else if(data[0] == CAN) {
                            // receiver might have sent us abort command while we were sending EOT to it.
                            errMsg = "Received abort command from file receiving end !";
                            state = ABORT;
                            break;
                        }else {
                            if(waitForFinalBlockACK != true) {
                                if(System.currentTimeMillis() >= eotAckWaitTimeOutValue) {
                                    errMsg = "Timedout while waiting for EOT reception acknowledgement !";
                                    state = ABORT;
                                }else {
                                    // re-send EOT (try more times), this also handles the NAK received case.
                                    state = ENDTX;
                                }
                            }else {
                                if(System.currentTimeMillis() >= finAckWaitTimeOutValue) {
                                    errMsg = "Timedout while waiting for final ymodem null block reception acknowledgement !";
                                    state = ABORT;
                                }else {
                                    // re-send ymodem final null block (try more times), this also handles the NAK received case.
                                    state = FINISHTX;
                                }
                            }
                        }
                    }
                }
                else {
                }
                break;

            case SENDNEXT:
                retryCount = 0; // reset
                blockNumber++;
                assembleBlock();

                // indicates there is no more data to be sent.
                if(noMoreData == true) {
                    state = ENDTX;
                    break;
                }

                // reaching here means there is data to be sent to receiver.
                try {
                    scm.writeBytes(handle, block, 0);
                } catch (SerialComException exp) {
                    inStream.close();
                    throw exp;
                }
                showSentProgress = true;
                state = WAITACK;
                break;

            case ENDTX:
                if(eotAckReceptionTimerInitialized != true) {
                    eotAckWaitTimeOutValue = System.currentTimeMillis() + 60000; // 1 minute
                    eotAckReceptionTimerInitialized = true;
                }
                try {
                    scm.writeSingleByte(handle, EOT);
                } catch (SerialComException exp) {
                    inStream.close();
                    throw exp;
                }
                numberOfBlocksSent = 0; // reset
                showSentProgress = false;
                state = WAITACK;
                break;

            case FINISHTX:
                if(finAckReceptionTimerInitialized != true) {
                    finAckWaitTimeOutValue = System.currentTimeMillis() + 60000; // 1 minute
                    finAckReceptionTimerInitialized = true;
                    assembleFinalBlock();
                }
                try {
                    scm.writeBytes(handle, block, 0);
                } catch (SerialComException exp) {
                    inStream.close();
                    throw exp;
                }
                waitForFinalBlockACK = true;
                showSentProgress = false;
                state = WAITACK;
                break;

            case ABORT:
                /* if any IOexception occurs, control will not reach here instead exception would 
                 * have been already thrown. This state is entered explicitly to abort executing 
                 * actions in state machine. */
                inStream.close();
                throw new SerialComException(errMsg);

            default:
                break;
            }
        }
    }

    /*
     * Prepares ymodem/crc block 0 of 133 bytes in total using CRC-16-CCITT as given below :
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
            block0 = block;
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
     * Prepares ymodem/crc block of 133 bytes in total using CRC-16-CCITT as given below :
     * [SOH][blk #][255-blk #][128 data bytes][2 byte CRC]
     * 
     * For text mode transfer, lines are terminated by CR+LF, EOF will be indicate
     * by one or more ^Z. If the data ends exactly on a 128-byte boundary, i.e. 
     * CR in 127, and LF in 128, a subsequent sector containing the ^Z EOF character(s)
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

        block[0] = SOH;
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

            while(x < 131) {
                // entering into this loop means that at-least one byte 
                // of space exist in block[] array.

                if((mark == limit) || (mark == -1)) {
                    // indicates we need to read more data from file as all data bytes in
                    // tmpSendBuffer has been sent to file receiver end.
                    limit = inStream.read(tmpSendBuffer, 0, 1024);
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
                        for(x = x + 0; x < 131; x++) {
                            block[x] = SUB;
                        }
                        alreadySentEOFchar = true;
                    }
                }else if((data0 != -1) && (data1 == -1)) {
                    // indicates last byte of data in file.
                    if((data0 == LF) || (data0 == CR)) {
                        block[x] = CR;
                        x++;
                        if(x < 131) {
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
                            if(x < 131) {
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
                        if(x < 131) {
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
                        if(x < 131) {
                            block[x] = LF;
                            x++;
                            if(x < 131) {
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
                        if(x < 131) {
                            block[x] = LF;
                            x++;
                            if(x < 131) {
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
                        if(x < 131) {
                            block[x] = LF;
                            x++;
                            if(x < 131) {
                                block[x] = CR;
                                x++;
                                if(x < 131) {
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
            numBytesRead = inStream.read(block, 3, 128);
            if(numBytesRead == 128) {
            }else if(numBytesRead > 0) {
                // assembling last block with padding.
                for(x = numBytesRead + 3; x < 131; x++) {
                    block[x] = SUB;
                }
            }else {
                // EOF encountered.
                noMoreData = true;
                return;
            }
        }

        // append 2 byte CRC value.
        blockCRCval = crcCalculator.getCRC16CCITTValue(block, 3, 130);
        block[131] = (byte) (blockCRCval >>> 8); // CRC high byte
        block[132] = (byte) blockCRCval;         // CRC low byte
    }

    /**
     * <p>Represents actions to execute in state machine to implement 
     * ymodem-crc protocol for receiving files.</p>
     * 
     * @return true on success, false if application instructed to abort.
     * @throws IOException if any I/O error occurs.
     * @throws SerialComException if any I/0 error on serial port communication occurs.
     */
    public boolean receiveFileY() throws IOException {

        // Finite state machine's states.
        final int CONNECT       = 0x01;
        final int BLOCK0RCV     = 0x02;
        final int PBLOCK0       = 0x03;
        final int BEGINRCV      = 0x04;
        final int DATABLOCKRCV  = 0x05;
        final int VERIFY        = 0x06;
        final int REPLY         = 0x07;
        final int ABORT         = 0x08;

        int i = 0;
        int x = 0;
        int crcl = 0;
        int state = -1;
        int blockNumber = 1;
        int blockCRCval = 0;
        int block0Index = 0;
        int dataBlockIndex = 0;
        byte[] data = null;
        String errMsg = null;
        int spaceLeftInBlock0 = 0;
        int spaceLeftInDataBlock = 0;
        byte[] block1029 = new byte[1029];
        long currentlyProcessingFileLength = 0;
        long currentlyProcessingFileModifyInfo = 0;
        long currentlyProcessingFileMode = 0;
        boolean isFileOpen = false;
        String nameOfFileBeingReceived = null;
        final String receiverDirAbsolutePath = filesToReceive.getAbsolutePath();
        boolean isCorrupted = false;
        boolean isDuplicateBlock = false;
        int duplicateBlockRetryCount = 0;
        long totalNumberOfDataBytesReadTillNow = 0;
        int percentOfBlocksReceived = 0;

        // Clear receive buffer before start. Ymodem is fully receiver driven by design.
        try {
            scm.clearPortIOBuffers(handle, true, false);
        } catch (SerialComException exp) {
            outStream.close();
            throw exp;
        }

        state = CONNECT; // entry point to state machine.
        while(true) {
            switch(state) {

            case CONNECT:

                // reset some globals
                blockNumber = 1;
                numberOfBlocksReceived = 0;
                totalNumberOfDataBytesReadTillNow = 0;
                lastCharacterReceivedWasLF = false;
                lastCharacterReceivedWasCR = false;
                unprocessedByteInReceivedDataExist = false;

                for(x=0; x < 3; x++) {
                    try {
                        scm.writeSingleByte(handle, C);
                    } catch (SerialComException e) {
                        throw e;
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                    }
                    try {
                        data = scm.readBytes(handle, 133);
                    } catch (IOException e) {
                        throw e;
                    }
                    if(data != null && data.length > 0) {
                        if(data[0] == EOT) {
                            // sender sent EOT again because he missed (noise) our acknowledgement of EOT previously sent.
                            try {
                                scm.writeSingleByte(handle, ACK);
                                continue;
                            } catch (IOException e) {
                                throw e;
                            }
                        }
                        break;
                    }
                    if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
                        outStream.close();
                        scm.writeBytes(handle, ABORT_CMD, 0);
                        return false;
                    }
                }
                if(x >= 3) {
                    throw new SerialComTimeOutException("Timed out while connecting with file sender !");
                }
                state = BLOCK0RCV;
                break;

            case BLOCK0RCV:

                for(x=0; x < 3; x++) {
                    if(data != null && data.length > 0) {
                        if(data[0] == SOH) {
                            block0 = block;
                            spaceLeftInBlock0 = 133;
                            crcl = 131;
                            break;
                        }else if(data[0] == STX) {
                            block0 = block1029;
                            spaceLeftInBlock0 = 1029;
                            crcl = 1027;
                            break;
                        }else {
                            // the sender might forget to flush his serial port buffers and therefore initial
                            // data may be garbage + block0.
                            try {
                                scm.writeSingleByte(handle, NAK);
                            } catch (IOException e) {
                                throw e;
                            }
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                            }
                            try {
                                data = scm.readBytes(handle, 133);
                            } catch (IOException e) {
                                throw e;
                            }
                        }
                    }
                }
                if(x >= 3) {
                    throw new SerialComTimeOutException("Timed out while waiting for block 0 from file sender !");
                }

                for(x=0; x < data.length; x++) {
                    block0[x] = data[x];
                    spaceLeftInBlock0--;
                }

                if(spaceLeftInBlock0 > 0) {
                    block0Index = block0.length - spaceLeftInBlock0;
                    for(i = 0; i < 20; i++) {
                        // check if application (file receiver) wish to cancel receiving file.
                        if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
                            outStream.close();
                            scm.writeBytes(handle, ABORT_CMD, 0);
                            return false;
                        }
                        // let the data arrive from other end, also minimize JNI transitions.
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                        try {
                            data = scm.readBytes(handle, spaceLeftInBlock0);
                        } catch (IOException e) {
                            throw e;
                        }
                        if((data != null) && (data.length > 0)) {
                            for(x=0; x < data.length; x++) {
                                block0[block0Index] = data[x];
                                block0Index++;
                                spaceLeftInBlock0--;
                            }
                        }
                        if(spaceLeftInBlock0 <= 0) {
                            break;
                        }
                    }
                }
                if(i >= 20) {
                    throw new SerialComTimeOutException("Timed out while receiving block 0 from file sender !");
                }
                state = PBLOCK0;
                break;

            case PBLOCK0:

                if((block0[1] != (byte)0x00) || (block0[2] != (byte)0xFF)) {
                    throw new SerialComException("Invalid information block 0 received !");
                }

                blockCRCval = crcCalculator.getCRC16CCITTValue(block0, 3, (crcl - 1));
                if((block0[crcl] != (byte)(blockCRCval >>> 8)) || (block0[crcl + 1] != (byte)blockCRCval)) {
                    throw new SerialComException("Invalid CRC (corrupted block 0 or final block received) !");
                }

                // While we were expecting block 0 file information block, sender might indicate end of session 
                // as there are no more files to be sent, so check it.
                if((block0[crcl] == (byte)0x00) && (block0[crcl + 1] == (byte)0x00)) {
                    for(i=3; i < block0.length; i++) {
                        if(block0[i] != (byte)0x00) {
                            break;
                        }
                    }
                    if(i >= block0.length) {
                        // All file shave been received, let's go back home happily.
                        scm.writeSingleByte(handle, ACK);
                        return true;
                    }
                }

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
                    throw new SerialComException("Sender did not sent file name !");
                }
                File namefile = new File(receiverDirAbsolutePath, nameOfFileBeingReceived);
                if(!namefile.exists()) {
                    namefile.createNewFile();
                }
                try {
                    outStream = new BufferedOutputStream(new FileOutputStream(namefile));
                } catch (FileNotFoundException e) {
                    throw e;
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

                scm.writeSingleByte(handle, ACK);
                state = BEGINRCV;
                break;

            case BEGINRCV:

                try {
                    scm.writeSingleByte(handle, C);  
                    state = DATABLOCKRCV;
                } catch (SerialComException e) {
                    throw e;
                }
                state = DATABLOCKRCV;
                break;

            case DATABLOCKRCV:

                spaceLeftInDataBlock = 133; // reset
                dataBlockIndex = 0;         // reset
                state = VERIFY;

                for(i = 0; i < 10; i++) {
                    // check if application (file receiver) wish to cancel receiving file.
                    if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
                        outStream.close();
                        scm.writeBytes(handle, ABORT_CMD, 0);
                        return false;
                    }
                    // let the data arrive from other end, also minimize JNI transitions.
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    try {
                        data = scm.readBytes(handle, spaceLeftInDataBlock);
                    } catch (IOException e) {
                        throw e;
                    }
                    if((data != null) && (data.length > 0)) {
                        for(x=0; x < data.length; x++) {
                            block[dataBlockIndex] = data[x];
                            dataBlockIndex++;
                            spaceLeftInDataBlock--;
                        }
                        // sender might indicate end of transmission instead of sending next data block
                        if(block[0] == EOT) {
                            try {
                                scm.writeSingleByte(handle, ACK);
                                if(isFileOpen == true) {
                                    outStream.flush();
                                    outStream.close();
                                }
                                isFileOpen = false;
                                state = CONNECT;
                            } catch (IOException e) {
                                state = ABORT;
                                errMsg = e.getMessage();
                            }
                            break;
                        }
                        // sender might send transfer abort command instead of next data block
                        if(block[dataBlockIndex - 1] == CAN) {
                            if(dataBlockIndex > 1) {
                                if(block[dataBlockIndex - 2] == CAN) {
                                    state = ABORT;
                                    errMsg = "Abort command received from file sending application !";
                                    break;
                                }
                            }
                        }
                    }
                    if(spaceLeftInDataBlock <= 0) {
                        break;
                    }
                }
                if((state == DATABLOCKRCV) && (i >= 10)) {
                    throw new SerialComTimeOutException("Timed out while receiving data block from file sender !"); 
                }
                break;

            case VERIFY:

                isCorrupted = false;      // reset
                isDuplicateBlock = false; // reset
                state = REPLY;

                // verify block start.
                if(block[0] != SOH) {
                    isCorrupted = true;
                    break;
                }
                // check duplicate block.
                if(block[1] == (byte)(blockNumber - 1)){
                    isDuplicateBlock = true;
                    duplicateBlockRetryCount++;
                    if(duplicateBlockRetryCount > 10) {
                        errMsg = "Maximum number of retries reached while receiving same data block !";
                        state = ABORT;
                    }
                    break;
                }
                // verify block number sequence and block number itself.
                if((block[1] != (byte) blockNumber) || (block[2] != (byte) ~blockNumber)) {
                    isCorrupted = true;
                    break;
                }
                // verify CRC value.
                blockCRCval = crcCalculator.getCRC16CCITTValue(block, 3, 130);
                if((block[131] != (byte)(blockCRCval >>> 8)) || (block[132] != (byte)blockCRCval)){
                    isCorrupted = true;
                }
                break;

            case REPLY:

                try {
                    if(isCorrupted == false) {

                        scm.writeSingleByte(handle, ACK);
                        totalNumberOfDataBytesReadTillNow = totalNumberOfDataBytesReadTillNow + 128;

                        if(textMode == true) {
                            // for ASCII mode, parse and then flush.
                            processAndWrite(block);
                        }else {
                            // for binary mode, just flush data as is to file physically.
                            if(currentlyProcessingFileLength != 0) {
                                if(totalNumberOfDataBytesReadTillNow <= currentlyProcessingFileLength) {
                                    outStream.write(block, 3, 128);
                                }else {
                                    outStream.write(block, 3, (int)(128 - (totalNumberOfDataBytesReadTillNow - currentlyProcessingFileLength)));
                                }
                            }else {
                                outStream.write(block, 3, 128);
                            }
                        }

                        // update GUI that a block has been received if application has provided 
                        // a listener for this purpose.
                        if(progressListener != null) {
                            numberOfBlocksReceived++;
                            if(currentlyProcessingFileLength > 0) {
                                percentOfBlocksReceived = (int) ((12800 * numberOfBlocksReceived) / currentlyProcessingFileLength);
                            }else {
                                percentOfBlocksReceived = 100;
                            }
                            if(percentOfBlocksReceived >= 100) {
                                percentOfBlocksReceived = 100;
                            }
                            progressListener.onYmodemReceiveProgressUpdate(nameOfFileBeingReceived, numberOfBlocksReceived, percentOfBlocksReceived);
                        }

                        if(isDuplicateBlock == false) {
                            blockNumber++;
                            if(blockNumber > 0xFF) {
                                blockNumber = 0x00;
                            }
                            duplicateBlockRetryCount = 0; // reset
                        }
                    }else {
                        scm.writeSingleByte(handle, NAK);
                    }
                    state = DATABLOCKRCV;
                } catch (IOException e) {
                    throw e;
                }
                break;

            case ABORT:
                if(isFileOpen == true) {
                    outStream.flush();
                    outStream.close();
                }
                throw new SerialComException(errMsg);

            default:
                // never reached
            }
        }
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
     * It is possible that last data byte (130th byte) in current block can not be processed because
     * we have to analyze next data byte which will be available to us only in the next data block
     * received. So we save that last byte and process it next time this method is called.
     * 
     * @throws IOException if any I/O error occurs.
     */
    private void processAndWrite(byte[] block) throws IOException {
        mark = 3;  // init + reset
        int q = 0; // init + reset

        while(mark <= 129) {
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

        if(mark == 130) {
            // indicates last byte in block array could not be processed as one more bytes was needed to 
            // test against all cases. so save this byte and process it next block of data received.
            unprocessedByteInReceivedDataExist = true;
            unprocessedByteInLastReceivedBlock = block[130];
        }
    }
}
