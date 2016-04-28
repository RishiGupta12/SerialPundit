/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComTimeOutException;
import com.embeddedunveiled.serial.util.SerialComCRCUtil;

/**
 * <p>Implements state machine based file transfer based on YMODEM-CRC file transfer protocol.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComYModem1K {

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

    /**
     * <p>Allocates a new SerialComYModem1K object with given details and associate it with the given 
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
    public SerialComYModem1K(SerialComManager scm, long handle, File[] filesToSend, boolean textMode,
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
     * <p>Allocates a new SerialComYModem1K object with given details and associate it with the given 
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
    public SerialComYModem1K(SerialComManager scm, long handle, File filesToReceive, boolean textMode,
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
                block0 = null; // free memory
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

                if((state != ABORT) && (state != ENDTX)) {
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
                                    percentOfBlocksSent = (int) ((102400 * numberOfBlocksSent) / lengthOfFileToSend);
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
        if(osType != SerialComManager.OS_WINDOWS) {
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
     * Prepares ymodem/crc block of 1029 bytes in total using CRC-16-CCITT as given below :
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
     * ymodem-crc protocol for receiving files.</p>
     * 
     * @return true on success, false if application instructed to abort.
     * @throws IOException if any I/O error occurs.
     * @throws SerialComException if any I/0 error on serial port communication occurs.
     */
    public boolean receiveFileY() throws IOException {

        // Finite state machine's states.
        final int CONNECT   = 0x01;
        final int BLOCKRCV  = 0x02;
        final int VERIFY    = 0x03;
        final int REPLY     = 0x04;
        final int ABORT     = 0x05;

        int k = 0;
        int i = 0;
        int x = 0;
        int z = 0;
        int crcl = 0;
        int block0CharReceivedIndex = 0;
        int delayVal = 250;
        int retryCount = 0;
        int duplicateBlockRetryCount = 0;
        int state = -1;
        int blockNumber = 1;
        int blockCRCval = 0;
        int bufferIndex = 0;
        long connectTimeOut = 0;
        long nextDataRecvTimeOut = 0;
        boolean rxDone = false;
        boolean firstBlock = false;
        boolean isCorrupted = false;
        boolean isDuplicateBlock = false;
        boolean handlingLargeBlock = false;
        boolean partialReadInProgress = false;
        byte[] data = null;
        String errMsg = null;
        int percentOfBlocksReceived = 0;
        boolean receiveBlock0OrFinalBlock = true;
        byte[] block0 = new byte[1029];
        long currentlyProcessingFileLength = 0;
        long currentlyProcessingFileModifyInfo = 0;
        long currentlyProcessingFileMode = 0;
        boolean isFileOpen = false;
        boolean eotACKHasBeenSent = false;
        long totalNumberOfDataBytesReadTillNow = 0;
        String nameOfFileBeingReceived = null;
        final String receiverDirAbsolutePath = filesToReceive.getAbsolutePath();

        // Clear receive buffer before start.
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
                if(retryCount < 3) {
                    try {
                        scm.writeSingleByte(handle, C);
                        firstBlock = true;
                        connectTimeOut = System.currentTimeMillis() + 3000; // update timeout, 3 seconds.
                        state = BLOCKRCV;
                    } catch (SerialComException exp) {
                        outStream.close();
                        throw exp;
                    }
                }else {
                    // ymodem has no option to fall back to checksum mode like xmodem, so abort.
                    errMsg = "Abort command received from file sender's end !";
                    state = ABORT;
                }
                break;

            case BLOCKRCV:        
                // when the receiver is waiting for a block of data following conditions might occur :
                // case 1: sender sent data block only (133/1029 length block).
                // case 2: sender sent abort command only (consecutive CAN characters or may have BACK 
                //         SPACE totaling 2 to 16 characters).
                // case 3: sender sent data block followed immediately by abort command (135 to 149(133+16) total).
                // case 4: sender sent abort command in between data block.
                // case 5: sender can send block0 (133/1029), file data block(133) or final null block(1029).
                while(true) {
                    // check if application (file receiver) wish to cancel receiving file.
                    if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
                        outStream.close();
                        scm.writeBytes(handle, ABORT_CMD, 0);
                        return false;
                    }

                    // let the data arrive from other end, also minimize JNI transitions.
                    try {
                        Thread.sleep(delayVal);
                    } catch (InterruptedException e) {
                    }

                    try {
                        data = scm.readBytes(handle, 1029);
                    } catch (SerialComException exp) {
                        outStream.close();
                        throw exp;
                    }

                    if((data != null) && (data.length > 0)) {

                        if(receiveBlock0OrFinalBlock != true) {
                            firstBlock = false;
                        }

                        if(data[0] == CAN) {
                            if(lastCharacterReceivedWasCAN == true) {
                                // received 2nd consecutive CAN means sender wish to abort file transfer.
                                try {
                                    scm.writeSingleByte(handle, ACK);
                                } catch (Exception e) {
                                }
                                errMsg = "Abort command received from file sending application !";
                                state = ABORT;
                                break;
                            }
                            if(data.length >= 2) {
                                if(data[1] == CAN) {
                                    // received 2 consecutive CAN means sender wish to abort file transfer.
                                    try {
                                        scm.writeSingleByte(handle, ACK);
                                    } catch (Exception e) {
                                    }
                                    errMsg = "Abort command received from file sending application !";
                                    state = ABORT;
                                    break;
                                }else {
                                    // this is not valid block as 1st character is CAN instead of STX.
                                    // probably it is noise, so send NAK.
                                    isCorrupted = true;
                                    state = REPLY;
                                    break;
                                }
                            }else {
                                // this is 1st CAN character, wait to check next character; whether it is CAN or not.
                                lastCharacterReceivedWasCAN = true;
                            }
                        }else if(data[0] == EOT) {
                            if(eotACKHasBeenSent == true) {
                                // line may have noise or sender missed EOT sent, resent ACK to acknowledge EOT.
                                scm.writeSingleByte(handle, ACK);
                                try {
                                    Thread.sleep(50);
                                } catch (Exception e) {
                                }
                                state = CONNECT;
                                break;
                            }
                            if(lastCharacterReceivedWasCAN == true) {
                                // EOT after CAN was not expected, probably line has noise; abort transfer.
                                errMsg = "Unexpected data sequence (<CAN> <EOT>) received from file sender !";
                                state = ABORT;
                                break;
                            }

                            // indicates that sender has sent the complete file.
                            isCorrupted = false;
                            rxDone = true;
                            state = REPLY;
                            break;
                        }else {
                            eotACKHasBeenSent = false; // reset
                            if(receiveBlock0OrFinalBlock == false) {
                                /* processing file data block */
                                if((data[0] == STX) || (handlingLargeBlock == true)) {
                                    k = 1029;
                                    /* At the beginning of block data[0] will be either STX or SOH but 
                                     * when receiving partial data data[0] will not be STX/SOH. The 
                                     * handlingLargeBlock check tells whether we are receiving 133 or 
                                     * 1029 size block. */
                                    handlingLargeBlock = true;
                                }else {
                                    k = 133;
                                    handlingLargeBlock = false;
                                }

                                if(lastCharacterReceivedWasCAN == true) {
                                    // Probably line has noise; abort transfer.
                                    errMsg = "Invalid data sequence (<CAN> <" + data[0] + ">) received from file sender !";
                                    state = ABORT;
                                    break;
                                }

                                if((partialReadInProgress == false) && (data.length == k)) {
                                    // complete block read in one go.
                                    for(int m=0; m < k; m++) {
                                        block[m] = data[m];
                                    }
                                    state = VERIFY;
                                    break;
                                }else {
                                    // partial block read.
                                    partialReadInProgress = true;
                                    for(z=0; z < data.length; z++) {
                                        if(bufferIndex >= k) {
                                            // this indicates either file sender has sent abort command immediately
                                            // after sending data block or line has noise; extraneous characters.
                                            if((data.length - z) >= 2) {
                                                // check if we received 2 consecutive CAN characters, if yes then abort.
                                                if((data[z] == CAN) && (data[z+1] == CAN)) {
                                                    errMsg = "Abort command received from file sender !";
                                                    state = ABORT;
                                                    break;
                                                }else {
                                                    // extraneous characters, line has noise. go to verification
                                                    // state as we have received full data block thereby
                                                    // discarding extraneous characters.
                                                    delayVal = 250;  // reset
                                                    bufferIndex = 0; // reset
                                                    partialReadInProgress = false; // reset
                                                    state = VERIFY;
                                                    break;
                                                }
                                            }else {
                                                // there is only 1 byte which may be CAN or unwanted noise character.
                                                // process data block received and 
                                                if(data[z] == CAN) {
                                                    // this is 1st CAN character, wait to check next character;
                                                    // whether it is CAN or not. this will be processed in next
                                                    // iteration of state machine loop.
                                                    lastCharacterReceivedWasCAN = true;
                                                }else {
                                                    // extraneous characters, line has noise. go to verification
                                                    // state as we have received full data block thereby
                                                    // discarding this extraneous character.
                                                    delayVal = 250;  // reset
                                                    bufferIndex = 0; // reset
                                                    partialReadInProgress = false; // reset
                                                    state = VERIFY;
                                                    break;
                                                }
                                            }
                                        }
                                        block[bufferIndex] = data[z];
                                        bufferIndex++;
                                    }
                                    if(bufferIndex >= k) {
                                        delayVal = 250;  // reset
                                        bufferIndex = 0; // reset
                                        partialReadInProgress = false; // reset
                                        state = VERIFY;
                                        break;
                                    }else {
                                        // next remaining data bytes should arrive early, 
                                        // go back to read more data from port.
                                        delayVal = 80;
                                        continue;
                                    }   
                                }
                            }else {
                                /* processing file information block 0 or final full null block */
                                for(int q=0; q < data.length; q++) {
                                    block0[block0CharReceivedIndex] = data[q];
                                    block0CharReceivedIndex++;
                                }
                                delayVal = 50;
                                if(block0[0] == SOH) {
                                    crcl = 131;
                                    if(block0CharReceivedIndex >= 133) {
                                        block0CharReceivedIndex = 0; // reset
                                        break;
                                    }
                                }else if(block0[0] == STX) {
                                    crcl = 1027;
                                    if(block0CharReceivedIndex >= 1029) {
                                        block0CharReceivedIndex = 0; // reset
                                        break;
                                    }
                                }else {
                                    isCorrupted = true;
                                    state = REPLY;
                                    break;
                                }
                            }
                        }
                    }else {
                        if(firstBlock == false) {
                            // reaching here means that we are waiting for receiving next block from file sender.
                            if(System.currentTimeMillis() > nextDataRecvTimeOut) {
                                errMsg = "Timedout while trying to receive next data block from file sender !";
                                state = ABORT;
                                break;
                            }
                        }else {
                            // reaching here means that we are still waiting for 1st block from file sender.
                            if(System.currentTimeMillis() > connectTimeOut) {
                                retryCount++;
                                state = CONNECT;
                                break;
                            }
                        }
                    }					
                }

                if(receiveBlock0OrFinalBlock == true) {

                    if(block0[0] == STX) {
                        k = 1024;
                    }else {
                        k = 128;
                    }
                    blockCRCval = crcCalculator.getCRC16CCITTValue(block0, 3, (crcl - 1));
                    if((block0[crcl] != (byte)(blockCRCval >>> 8)) || (block0[crcl + 1] != (byte)blockCRCval)) {
                        isCorrupted = true;
                        state = REPLY;
                        break;
                    }

                    // if this is final block, all files have been received successfully. 
                    for(i=3; i < crcl; i++) {
                        if(block0[i] != (byte) 0x00) {
                            break;
                        }
                    }
                    if(i >= crcl) {
                        // let's go back home happily.
                        scm.writeSingleByte(handle, ACK);
                        return true;
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
                        errMsg = "Sender did not sent file name !";
                        state = ABORT;
                        break;
                    }
                    File namefile = new File(receiverDirAbsolutePath, nameOfFileBeingReceived);
                    if(!namefile.exists()) {
                        namefile.createNewFile();
                    }
                    outStream = new BufferedOutputStream(new FileOutputStream(namefile));
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

                    retryCount = 0; // reset
                    receiveBlock0OrFinalBlock = false; // reset
                    scm.writeSingleByte(handle, ACK);
                    state = CONNECT;
                }
                break;

            case VERIFY:
                isCorrupted = false;      // reset
                isDuplicateBlock = false; // reset
                state = REPLY;

                // verify block start.
                if(handlingLargeBlock == true) {
                    k = 1026;
                    if(block[0] != STX) {
                        isCorrupted = true;
                        break;
                    }
                }else {
                    k = 130;
                    if(block[0] != SOH) {
                        isCorrupted = true;
                        break;
                    }
                }
                // check duplicate block.
                if(block[1] == ((blockNumber - 1) & 0xFF)) {
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
                blockCRCval = crcCalculator.getCRC16CCITTValue(block, 3, k);
                if((block[k + 1] != (byte)(blockCRCval >>> 8)) || (block[k + 2] != (byte)blockCRCval)){
                    isCorrupted = true;
                }
                break;

            case REPLY:
                if(handlingLargeBlock == true) {
                    k = 1024;
                }else {
                    k = 128;
                }
                try {
                    if(rxDone == false) {
                        if(isCorrupted == false) {
                            scm.writeSingleByte(handle, ACK);
                            totalNumberOfDataBytesReadTillNow = totalNumberOfDataBytesReadTillNow + k;
                            if(textMode == true) {
                                // for ASCII mode, parse and then flush.
                                processAndWrite(block, k);
                            }else {
                                // for binary mode, just flush data as is to file physically.
                                if(currentlyProcessingFileLength != 0) {
                                    if(totalNumberOfDataBytesReadTillNow <= currentlyProcessingFileLength) {
                                        outStream.write(block, 3, k);
                                    }else {
                                        outStream.write(block, 3, (int)(k - (totalNumberOfDataBytesReadTillNow - currentlyProcessingFileLength)));
                                    }
                                }else {
                                    outStream.write(block, 3, k);
                                }
                            }

                            // update GUI that a block has been received if application has provided 
                            // a listener for this purpose.
                            if(progressListener != null) {
                                numberOfBlocksReceived++;
                                if(currentlyProcessingFileLength > 0) {
                                    percentOfBlocksReceived = (int) ((k * numberOfBlocksReceived * 100) / currentlyProcessingFileLength);
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
                        handlingLargeBlock = false; // reset
                        state = BLOCKRCV;
                    }else {
                        // sender might send EOT more than 1 time for any reason, so release resources only once.
                        scm.writeSingleByte(handle, ACK);
                        eotACKHasBeenSent = true;
                        if(isFileOpen == true) {
                            outStream.flush();
                            outStream.close();
                            isFileOpen = false;
                        }
                        // reset and wait to receive block0 or final block.
                        rxDone = false;
                        receiveBlock0OrFinalBlock = true;
                        blockNumber = 1; // reset
                        numberOfBlocksReceived = 0; // reset
                        totalNumberOfDataBytesReadTillNow = 0; // reset
                        state = CONNECT;
                        break;
                    }
                } catch (SerialComException exp) {
                    outStream.close();
                    throw exp;
                } catch (IOException exp) {
                    outStream.close();
                    throw exp;
                }
                nextDataRecvTimeOut = System.currentTimeMillis() + 1000; // update timeout for next byte 1 second.
                break;

            case ABORT:
                /* if an IOexception occurs, control will not reach here instead exception would have been
                 * thrown already. */
                if(outStream != null) {
                    outStream.flush();
                    outStream.close();
                }
                throw new SerialComTimeOutException(errMsg);

            default:
                break;
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
                    if(osType == SerialComManager.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else if((osType == SerialComManager.OS_MAC_OS_X) || (osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS)
                            || (osType == SerialComManager.OS_FREEBSD) || (osType == SerialComManager.OS_NETBSD) || (osType == SerialComManager.OS_OPENBSD)
                            || (osType == SerialComManager.OS_ANDROID)) {
                        tmpReceiveBuffer[q] = LF;
                        q = q + 1;
                    }else {
                    }
                }else if(data1 == CR) {
                    // represent CRCR case.
                    if(osType == SerialComManager.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        tmpReceiveBuffer[q + 2] = CR;
                        tmpReceiveBuffer[q + 3] = LF;
                        q = q + 4;
                    }else if((osType == SerialComManager.OS_MAC_OS_X) || (osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS)
                            || (osType == SerialComManager.OS_FREEBSD) || (osType == SerialComManager.OS_NETBSD) || (osType == SerialComManager.OS_OPENBSD)
                            || (osType == SerialComManager.OS_ANDROID)) {
                        tmpReceiveBuffer[q] = LF;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else {
                    }
                }else if(data1 == SUB) {
                    // represent CRSUB case.
                    if(osType == SerialComManager.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else if((osType == SerialComManager.OS_MAC_OS_X) || (osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS)
                            || (osType == SerialComManager.OS_FREEBSD) || (osType == SerialComManager.OS_NETBSD) || (osType == SerialComManager.OS_OPENBSD)
                            || (osType == SerialComManager.OS_ANDROID)) {
                        tmpReceiveBuffer[q] = LF;
                        q = q + 1;
                    }else {
                    }
                }else {
                    // represent CRX case.
                    if(osType == SerialComManager.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        tmpReceiveBuffer[q + 2] = data1;
                        q = q + 3;
                    }else if((osType == SerialComManager.OS_MAC_OS_X) || (osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS)
                            || (osType == SerialComManager.OS_FREEBSD) || (osType == SerialComManager.OS_NETBSD) || (osType == SerialComManager.OS_OPENBSD)
                            || (osType == SerialComManager.OS_ANDROID)) {
                        tmpReceiveBuffer[q] = LF;
                        tmpReceiveBuffer[q + 1] = data1;
                        q = q + 2;
                    }else {
                    }
                }
            }else if(data0 == LF) {
                if(data1 == LF) {
                    // represent LFLF case.
                    if(osType == SerialComManager.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        tmpReceiveBuffer[q + 2] = CR;
                        tmpReceiveBuffer[q + 3] = LF;
                        q = q + 4;
                    }else if((osType == SerialComManager.OS_MAC_OS_X) || (osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS)
                            || (osType == SerialComManager.OS_FREEBSD) || (osType == SerialComManager.OS_NETBSD) || (osType == SerialComManager.OS_OPENBSD)
                            || (osType == SerialComManager.OS_ANDROID)) {
                        tmpReceiveBuffer[q] = LF;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else {
                    }
                }else if(data1 == CR) {
                    // represent LFCR case.
                    if(osType == SerialComManager.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else if((osType == SerialComManager.OS_MAC_OS_X) || (osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS)
                            || (osType == SerialComManager.OS_FREEBSD) || (osType == SerialComManager.OS_NETBSD) || (osType == SerialComManager.OS_OPENBSD)
                            || (osType == SerialComManager.OS_ANDROID)) {
                        tmpReceiveBuffer[q] = LF;
                        q = q + 1;
                    }else {
                    }
                }else if(data1 == SUB) {
                    // represent LFSUB case.
                    if(osType == SerialComManager.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        q = q + 2;
                    }else if((osType == SerialComManager.OS_MAC_OS_X) || (osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS)
                            || (osType == SerialComManager.OS_FREEBSD) || (osType == SerialComManager.OS_NETBSD) || (osType == SerialComManager.OS_OPENBSD)
                            || (osType == SerialComManager.OS_ANDROID)) {
                        tmpReceiveBuffer[q] = LF;
                        q = q + 1;
                    }else {
                    }
                }else {
                    // represent LFX case.
                    if(osType == SerialComManager.OS_WINDOWS) {
                        tmpReceiveBuffer[q] = CR;
                        tmpReceiveBuffer[q + 1] = LF;
                        tmpReceiveBuffer[q + 2] = data1;
                        q = q + 3;
                    }else if((osType == SerialComManager.OS_MAC_OS_X) || (osType == SerialComManager.OS_LINUX) || (osType == SerialComManager.OS_SOLARIS)
                            || (osType == SerialComManager.OS_FREEBSD) || (osType == SerialComManager.OS_NETBSD) || (osType == SerialComManager.OS_OPENBSD)
                            || (osType == SerialComManager.OS_ANDROID)) {
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
