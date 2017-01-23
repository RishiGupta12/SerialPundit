/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
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
import java.io.FileOutputStream;
import java.io.IOException;

import com.serialpundit.core.util.SerialComCRCUtil;
import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComTimeOutException;
import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.SerialComManager;

/**
 * <p>Implements XMODEM-128/XMODEM-CHECKSUM file transfer protocol state machine in Java.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComXModem {

    private final byte SOH = 0x01;  // Start of header character
    private final byte EOT = 0x04;  // End-of-transmission character
    private final byte ACK = 0x06;  // Acknowledge byte character
    private final byte NAK = 0x15;  // Negative-acknowledge character
    private final byte CAN = 0x18;  // Cancel
    private final byte SUB = 0x1A;  // Substitute/CTRL+Z
    private final byte CR  = 0x0D;  // Carriage return
    private final byte LF  = 0x0A;  // Line feed
    private final byte BS  = 0X08;  // Back space

    private SerialComManager scm;
    private long handle;
    private File fileToProcess;
    private boolean textMode;
    private ISerialComXmodemProgress progressListener;
    private SerialComFTPCMDAbort transferState;
    private int osType;

    private int blockNumber;
    private byte[] block = new byte[132];  // 132 bytes xmodem block/packet
    private BufferedInputStream inStream;   // sent file from local to remote system
    private BufferedOutputStream outStream; // received file from remote to local system
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
    private final byte ABORT_CMD[] = new byte[] { CAN, CAN, CAN, CAN, CAN, BS, BS, BS, BS, BS };
    private final SerialComCRCUtil checksumCalculator = new SerialComCRCUtil();

    /**
     * <p>Allocates a new SerialComXModem object with given details and associate it with the given 
     * instance of SerialComManager class.</p>
     * 
     * @param scm SerialComManager instance associated with this handle.
     * @param handle of the port on which file is to be communicated.
     * @param fileToProcess File instance representing file to be communicated.
     * @param textMode if true file will be sent as text file (ASCII mode), if false file will be sent 
     *         as binary file.
     * @param progressListener object of class which implements ISerialComXmodemProgress interface and is 
     *         interested in knowing how many blocks have been sent/received till now.
     * @param transferState if application wish to abort sending/receiving file at instant of time due to 
     *         any reason, it can call abortTransfer method on this object. It can be null if application 
     *         does not wish to abort sending/receiving file explicitly.
     * @param osType operating system on which this application is running.
     */
    public SerialComXModem(SerialComManager scm, long handle, File fileToProcess, boolean textMode,
            ISerialComXmodemProgress progressListener, SerialComFTPCMDAbort transferState, int osType) {
        this.scm = scm;
        this.handle = handle;
        this.fileToProcess = fileToProcess;
        this.textMode = textMode;
        this.progressListener = progressListener;
        this.transferState = transferState;
        this.osType = osType;
    }

    /**
     * <p>Represents actions to execute in state machine to implement xmodem protocol for sending files.</p>
     * 
     * @return true on success, false if application instructed to abort.
     * @throws SecurityException if unable to read from file to be sent.
     * @throws IOException if any I/O error occurs.
     * @throws SerialComException if any I/0 error on serial port communication occurs.
     */
    public boolean sendFileX() throws IOException {

        // Finite state machine's states.
        final int CONNECT   = 0X00;
        final int BEGINSEND = 0X01;
        final int WAITACK   = 0X02;
        final int RESEND    = 0X03;
        final int SENDNEXT  = 0X04;
        final int ENDTX     = 0X05;
        final int ABORT     = 0X06;

        boolean nakReceived = false;
        boolean eotAckReceptionTimerInitialized = false;
        String errMsg = null;
        int retryCount = 0;
        int state = -1;
        byte[] data = null;
        long responseWaitTimeOut = 0;
        long eotAckWaitTimeOutValue = 0;
        int percentOfBlocksSent = 0;

        long lengthOfFileToSend = fileToProcess.length();
        inStream = new BufferedInputStream(new FileInputStream(fileToProcess));

        state = CONNECT;
        while(true) {
            switch(state) {

            case CONNECT:
                responseWaitTimeOut = System.currentTimeMillis() + 60000;
                while(nakReceived != true) {
                    try {
                        data = scm.readBytes(handle, 1024);
                    } catch (SerialComException exp) {
                        inStream.close();
                        throw exp;
                    }
                    if((data != null) && (data.length > 0)) {
                        /* Instead of purging receive buffer and then waiting for NAK, receive all data because
                         * this approach might be faster. The other side might have opened first time and may 
                         * have flushed garbage data. So receive buffer may contain garbage + NAK character. */
                        for(int x=0; x < data.length; x++) {
                            if(NAK == data[x]) {
                                nakReceived = true;
                                state = BEGINSEND;
                                break;
                            }
                        }
                    }else {
                        try {
                            Thread.sleep(100);  // delay before next attempt to check NAK character reception
                        } catch (InterruptedException e) {
                        }
                        // abort if timed-out while waiting for NAK character
                        if((nakReceived != true) && (System.currentTimeMillis() >= responseWaitTimeOut)) {
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

            case BEGINSEND:
                blockNumber = 1; // Block numbering starts from 1 for the first block sent, not 0.
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
                state = WAITACK;
                break;

            case WAITACK:
                responseWaitTimeOut = System.currentTimeMillis() + 60000; // 1 minute

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
                            Thread.sleep(1500);
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
                                retryCount++;
                                state = RESEND;
                                break;
                            }
                            state = SENDNEXT;
                        }else if(data[0] == NAK) {
                            // indicates both <NAK> only and <CAN> <NAK> sequence reception.
                            retryCount++;
                            state = RESEND;
                        }else if(data[0] == CAN) {
                            if(data.length >= 2) {
                                if(data[1] == CAN) {
                                    errMsg = "Received abort command from file receiving end !";
                                    state = ABORT;
                                    break;
                                }else {
                                    // probably it is noise, so re-send block.
                                    retryCount++;
                                    state = RESEND;
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
                        if(progressListener != null) {
                            numberOfBlocksSent++;                            
                            if(lengthOfFileToSend != 0) {
                                percentOfBlocksSent = (int) ((12800 * numberOfBlocksSent) / lengthOfFileToSend);
                            }else {
                                percentOfBlocksSent = 100;
                            }
                            if(percentOfBlocksSent >= 100) {
                                // if the last block is not multiple of 128, than percent will go > 100,
                                // so trim it. for example for a 1008 byte file, 1024 bytes (128*8) will
                                // be sent resulting in 102.19 %.
                                percentOfBlocksSent = 100;
                            }
                            progressListener.onXmodemSentProgressUpdate(numberOfBlocksSent, percentOfBlocksSent);
                        }
                    }else {
                        if(data[0] == ACK) {
                            // successfully sent file, let's go back home happily.
                            inStream.close();
                            return true;
                        }else {
                            if(System.currentTimeMillis() >= eotAckWaitTimeOutValue) {
                                errMsg = "Timedout while waiting for EOT reception acknowledgement from file receiver !";
                                state = ABORT;
                            }else {
                                state = ENDTX;
                            }
                        }
                    }
                }
                break;

            case SENDNEXT:
                retryCount = 0; // reset.
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

                state = WAITACK;
                break;

            case ABORT:
                /* if any IOexception occurs, control will not reach here instead exception would 
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
     * Prepares xmodem block [SOH][blk #][255-blk #][128 data bytes][cksum] of 
     * 132 bytes in total.
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

        // append checksum of this block.
        block[131] = checksumCalculator.getChecksumValue(block, 3, 130);
    }

    /**
     * <p>Represents actions to execute in state machine to implement xmodem protocol for receiving files.</p>
     * 
     * @return true on success, false if application instructed to abort.
     * @throws IOException if any I/O error occurs.
     * @throws SerialComException if any I/0 error on serial port communication occurs.
     */
    public boolean receiveFileX() throws IOException {

        // Finite state machine's states.
        final int CONNECT     = 0X00;
        final int RECEIVEDATA = 0X01;
        final int VERIFY      = 0X02;
        final int REPLY       = 0X03;
        final int ABORT       = 0X04;

        int z = 0;
        int delayVal = 200;
        int retryCount = 0;
        int duplicateBlockRetryCount = 0;
        int state = -1;
        int blockNumber = 1;
        int bufferIndex = 0;
        long connectTimeOut = 0;
        long nextDataRecvTimeOut = 0;
        boolean rxDone = false;
        boolean firstBlock = false;
        boolean isCorrupted = false;
        boolean isDuplicateBlock = false;
        boolean partialReadInProgress = false;
        byte[] data = null;
        String errMsg = null;
        boolean isFileOpen = true;

        /* The data bytes get flushed automatically to file system physically whenever BufferedOutputStream's
		   internal buffer gets full and request to write more bytes have arrived. */
        outStream = new BufferedOutputStream(new FileOutputStream(fileToProcess));
        isFileOpen = true;

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
                if(retryCount > 10) {
                    errMsg = "Timedout while trying to connect to file sender !";
                    state = ABORT;
                    break;
                }
                try {
                    scm.writeSingleByte(handle, NAK);
                    firstBlock = true;
                    connectTimeOut = System.currentTimeMillis() + 10000; // update timeout, 10 seconds.
                    state = RECEIVEDATA;
                } catch (SerialComException exp) {
                    outStream.close();
                    throw exp;
                }
                break;

            case RECEIVEDATA:
                // when the receiver is waiting for next block of data following conditions might occur :
                // case 1: sender sent data block only (132 length block).
                // case 2: sender sent abort command only (consecutive CAN characters or may have back 
                //         space totaling 2 to 16 characters).
                // case 3: sender sent data block followed immediately by abort command (134 to 148 total).
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
                        data = scm.readBytes(handle, 132);
                    } catch (SerialComException exp) {
                        outStream.close();
                        throw exp;
                    }

                    if((data != null) && (data.length > 0)) {
                        firstBlock = false;

                        if(data[0] == CAN) {
                            if(lastCharacterReceivedWasCAN == true) {
                                // received 2nd consecutive CAN means sender wish to abort file transfer.
                                // sender may or may not wait for ACK in response to abort command.
                                // we sent it whether sender wish to receive it or not.
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
                                    // this is not valid block as 1st character is CAN instead of SOH.
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
                            if((partialReadInProgress == false) && (data.length == 132)) {
                                // complete block read in one go.
                                for(int i=0; i < 132; i++) {
                                    block[i] = data[i];
                                }
                                state = VERIFY;
                                break;
                            }else {
                                // partial block read.
                                partialReadInProgress = true;
                                for(z=0; z < data.length; z++) {
                                    if(bufferIndex >= 132) {
                                        // this indicates either file sender has sent abort command immediately
                                        // after sending data block or line has noise; extraneous characters.
                                        if((data.length - z) >= 2) {
                                            // check if we received 2 consecutive CAN characters, if yes then abort.
                                            if((data[z] == CAN) && (data[z+1] == CAN)) {
                                                try {
                                                    scm.writeSingleByte(handle, ACK);
                                                } catch (Exception e) {
                                                }
                                                errMsg = "Abort command received from file sending application !";
                                                state = ABORT;
                                                break;
                                            }else {
                                                // extraneous characters, line has noise. go to verification
                                                // state as we have received full data block thereby
                                                // discarding extraneous characters.
                                                delayVal = 250;  // reset.
                                                bufferIndex = 0; // reset.
                                                partialReadInProgress = false; // reset.
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
                                                delayVal = 250;  // reset.
                                                bufferIndex = 0; // reset.
                                                partialReadInProgress = false; // reset.
                                                state = VERIFY;
                                                break;
                                            }
                                        }
                                    }
                                    block[bufferIndex] = data[z];
                                    bufferIndex++;
                                }
                                if(bufferIndex >= 132) {
                                    delayVal = 220;  // reset.
                                    bufferIndex = 0; // reset.
                                    partialReadInProgress = false; // reset.
                                    state = VERIFY;
                                    break;
                                }else {
                                    // next remaining data bytes should arrive early, 
                                    // go back to read more data from port.
                                    delayVal = 80;
                                    continue;
                                }	
                            }
                        }
                    }else {
                        if(firstBlock == false) {
                            // reaching here means that we are waiting for receiving next data byte from file sender.
                            if(System.currentTimeMillis() > nextDataRecvTimeOut) {
                                errMsg = "Timedout while trying to receive next data byte (block) from file sender !";
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
                break;

            case VERIFY:
                isCorrupted = false;      // reset.
                isDuplicateBlock = false; // reset.
                state = REPLY;

                // check start of block.
                if(block[0] != SOH) {
                    isCorrupted = true;
                    break;
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
                // verify block number sequence and block number.
                if((block[1] != (byte) blockNumber) || (block[2] != (byte) ~blockNumber)) {
                    isCorrupted = true;
                    break;
                }
                // verify checksum.
                if(block[131] != checksumCalculator.getChecksumValue(block, 3, 130)) {
                    isCorrupted = true;
                }
                break;

            case REPLY:
                try {
                    if(rxDone == false) {
                        if(isCorrupted == false) {
                            // Send ACK 1st, so that till the time ACK reaches other end, other end prepares
                            // and send next block and it reaches to us, we perform IO operation (parse and
                            // write received data bytes to file physically).
                            scm.writeSingleByte(handle, ACK);
                            if(textMode == true) {
                                // for ASCII mode, parse and then flush.
                                processAndWrite(block);
                            }else {
                                // for binary mode, just flush data as is to file physically.
                                outStream.write(block, 3, 128);
                            }

                            // update GUI that a block has been received if application has provided 
                            // a listener for this purpose.
                            if(progressListener != null) {
                                numberOfBlocksReceived++;
                                progressListener.onXmodemReceiveProgressUpdate(numberOfBlocksReceived);
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
                        state = RECEIVEDATA;
                    }else {
                        // file reception successfully finished, let's go back home happily.
                        // sender might send EOT more than 1 time for any reason, so release resources only once.
                        scm.writeSingleByte(handle, ACK);
                        if(isFileOpen == true) {
                            outStream.flush();
                            outStream.close();
                            isFileOpen = false;
                        }
                        return true;
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
                outStream.close();
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
            // indicates last byte in block array could not be processed as one more bytes was needed to test against
            // all cases. so save this byte and process it next block of data received.
            unprocessedByteInReceivedDataExist = true;
            unprocessedByteInLastReceivedBlock = block[130];
        }
    }
}
