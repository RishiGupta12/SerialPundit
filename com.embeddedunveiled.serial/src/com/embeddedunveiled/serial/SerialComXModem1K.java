/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 *
 * The 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.embeddedunveiled.serial.SerialComManager.FTPPROTO;
import com.embeddedunveiled.serial.SerialComManager.FTPVAR;
import com.embeddedunveiled.serial.SerialComCRCUtil;

/**
 * <p>Implements state machine for XMODEM-1k file transfer protocol in Java. 
 * Increase in block size increases overall throughput.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComXModem1K {

	private final byte SOH = 0x01;  // Start of header character
	private final byte STX = 0x02;  // Start of text character
	private final byte EOT = 0x04;  // End-of-transmission character
	private final byte ACK = 0x06;  // Acknowledge byte character
	private final byte NAK = 0x15;  // Negative-acknowledge character
	private final byte CAN = 0x18;  // Cancel
	private final byte SUB = 0x1A;  // Substitute/CTRL+Z
	private final byte C   = 0x43;  // ASCII capital C character
	private final byte CR  = 0x0D;  // Carriage return
	private final byte LF  = 0x0A;  // Line feed
	private final byte BS  = 0X08;  // Back space

	private SerialComManager scm;
	private long handle;
	private File fileToProcess;
	private long lengthOfFileToProcess;
	private boolean textMode;
	private ISerialComXmodemProgress progressListener;
	private SerialComXModemAbort transferState;
	private int osType;

	private int blockNumber;
	private byte[] block = new byte[1029];  // 1029 bytes xmodem-1k block/packet
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
	private byte abortSequence[] = new byte[] { CAN, CAN, CAN, CAN, CAN, BS, BS, BS, BS, BS };
<<<<<<< HEAD
=======
	SerialComCRCUtil crcCalculator = new SerialComCRCUtil();
>>>>>>> upstream/master

	/**
	 * <p>Allocates a new SerialComXModem1K object with given details and associate it with the given 
	 * instance of SerialComManager class.</p>
	 * 
	 * @param scm SerialComManager instance associated with this handle.
	 * @param handle of the port on which file is to be communicated.
	 * @param fileToProcess File instance representing file to be communicated.
<<<<<<< HEAD
	 * @param textMode if true file will be sent as text file (ASCII mode), if false file will be sent as binary file.
	 * @param progressListener object of class which implements ISerialComProgressXmodem interface and is interested in knowing
	 *         how many blocks have been sent/received till now.
	 * @param transferState if application wish to abort sending/receiving file at instant of time due to any reason, it can call 
	 *         abortTransfer method on this object. It can be null of application does not wish to abort sending/receiving file
	 *         explicitly.
=======
	 * @param textMode if true file will be sent as text file (ASCII mode), if false file will be sent 
	 *         as binary file.
	 * @param progressListener object of class which implements ISerialComXmodemProgress interface and 
	 *         is interested in knowing how many blocks have been sent/received till now.
	 * @param transferState if application wish to abort sending/receiving file at instant of time due 
	 *         to any reason, it can call abortTransfer method on this object. It can be null of application 
	 *         does not wish to abort sending/receiving file explicitly.
>>>>>>> upstream/master
	 * @param osType operating system on which this application is running.
	 */
	public SerialComXModem1K(SerialComManager scm, long handle, File fileToProcess, boolean textMode,
			ISerialComXmodemProgress progressListener, SerialComXModemAbort transferState, int osType) {
		this.scm = scm;
		this.handle = handle;
		this.fileToProcess = fileToProcess;
		this.textMode = textMode;
		this.progressListener = progressListener;
		this.transferState = transferState;
		this.osType = osType;
	}

	/**
	 * <p>Represents actions to execute in state machine to implement xmodem-1k 
	 * protocol for sending files.</p>
	 * 
	 * @return true on success, false if application instructed to abort.
	 * @throws SecurityException if unable to read from file to be sent.
	 * @throws IOException if any I/O error occurs.
	 * @throws SerialComException if any I/0 error on serial port communication occurs.
	 */
	public boolean sendFileX() throws SecurityException, IOException, SerialComException {

		// Finite state machine's state.
		final int CONNECT = 0;
		final int BEGINSEND = 1;
		final int WAITACK = 2;
		final int RESEND = 3;
		final int SENDNEXT = 4;
		final int ENDTX = 5;
		final int ABORT = 6;

		boolean cReceived = false;
		boolean eotAckReceptionTimerInitialized = false;
		String errMsg = null;
		int retryCount = 0;
		int state = -1;
		byte[] data = null;
		long responseWaitTimeOut = 0;
		long eotAckWaitTimeOutValue = 0;
		int percentOfBlocksSent = 0;

		lengthOfFileToProcess = fileToProcess.length();
		inStream = new BufferedInputStream(new FileInputStream(fileToProcess));

		state = CONNECT;
		while(true) {
			switch(state) {
			case CONNECT:
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
								state = BEGINSEND;
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
						scm.writeBytes(handle, abortSequence, 0);
						return false;
					}
				}
				break;
			case BEGINSEND:
				blockNumber = 1; // Block numbering starts from 1 for the first block sent, not 0.
<<<<<<< HEAD
				assembleBlock(crcCalculator);
				
=======
				assembleBlock();

>>>>>>> upstream/master
				// if the file is empty goto ENDTX state.
				if(noMoreData == true) {
					state = ENDTX;
					break;
				}
<<<<<<< HEAD
				
=======

>>>>>>> upstream/master
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
				responseWaitTimeOut = System.currentTimeMillis() + 60000; // 1 minute.

				while(true) {
					// check if application (file sender) wish to cancel sending file.
					if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
						inStream.close();
						scm.writeBytes(handle, abortSequence, 0);
						return false;
<<<<<<< HEAD
					}

					// delay before next attempt to read from serial port.
					try {
						if(noMoreData != true) {
							Thread.sleep(120);
						}else {
							Thread.sleep(1500);
						}
					} catch (InterruptedException e) {
					}

					// try to read data from serial port.
					try {
=======
					}

					// delay before next attempt to read from serial port.
					try {
						if(noMoreData != true) {
							Thread.sleep(120);
						}else {
							Thread.sleep(1500);
						}
					} catch (InterruptedException e) {
					}

					// try to read data from serial port.
					try {
>>>>>>> upstream/master
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

				if((state != ABORT) && (state != ENDTX)) {
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
							percentOfBlocksSent = (int) ((12800 * numberOfBlocksSent) / lengthOfFileToProcess);
							if(percentOfBlocksSent >= 100) {
								percentOfBlocksSent = 100;
							}
							progressListener.onXmodemSentProgressUpdate(numberOfBlocksSent, percentOfBlocksSent);
						}
					}else {
						// successfully sent file, let's go back home happily.
						if(data[0] == ACK) {
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
				retryCount = 0; // reset
				blockNumber++;
<<<<<<< HEAD
				assembleBlock(crcCalculator);
				
=======
				assembleBlock();

>>>>>>> upstream/master
				// indicates there is no more data to be sent.
				if(noMoreData == true) {
					state = ENDTX;
					break;
				}
<<<<<<< HEAD
				
=======

>>>>>>> upstream/master
				// reaching here means there is data to be sent to receiver.
				try {
					scm.writeBytes(handle, block, 0);
				} catch (SerialComException exp) {
					inStream.close();
					throw exp;
				}
<<<<<<< HEAD
				
=======

>>>>>>> upstream/master
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
				/* if IOexception occurs, control will not reach here instead exception would have been
				 * thrown already. */
				inStream.close();
				throw new SerialComTimeOutException(errMsg);
			default:
				break;
			}
		}
	}

	/* 
	 * Prepares xmodem/crc block [STX][blk #][255-blk #][1024 data bytes][2 byte CRC]
	 * of 1029 bytes in total using CRC-16-CCITT.
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
<<<<<<< HEAD
	private void assembleBlock(SerialComCRC scCRC) throws IOException {
=======
	private void assembleBlock() throws IOException {
>>>>>>> upstream/master
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

			// read data from file to be sent.
			numBytesRead = inStream.read(block, 3, 1024);
			if((numBytesRead > 0) && (numBytesRead < 1024)) {
				// assembling last block with padding.
				x = numBytesRead;
				for(x = x + 0; x < 1027; x++) {
					block[x] = SUB;
				}
			}else if(numBytesRead < 0){
				// EOF encountered.
				noMoreData = true;
				return;
			}else {
			}
		}

		// append 2 byte CRC value.
<<<<<<< HEAD
		blockCRCval = scCRC.getCRCval(block, 3, 1026);
=======
		blockCRCval = crcCalculator.getCRC16CCITTValue(block, 3, 1026);
>>>>>>> upstream/master
		block[1027] = (byte) (blockCRCval >>> 8); // CRC high byte
		block[1028] = (byte) blockCRCval;         // CRC low byte
	}

	/**
	 * <p>Represents actions to execute in state machine to implement xmodem 
	 * protocol for receiving files.</p>
	 * 
	 * @return true on success, false if application instructed to abort.
	 * @throws IOException if any I/O error occurs.
	 * @throws SerialComException if any I/0 error on serial port communication occurs.
	 */
	public boolean receiveFileX() throws IOException, SerialComException {

		// Finite state machine's states.
		final int CONNECT = 0;
		final int RECEIVEDATA = 1;
		final int VERIFY = 2;
		final int REPLY = 3;
		final int ABORT = 4;

		int z = 0;
		int delayVal = 300;
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
		boolean handlingLargeBlock = false;
		boolean partialReadInProgress = false;
		byte[] data = null;
		String errMsg = null;
		int blockCRCval = 0;

		/* The data bytes get flushed automatically to file system physically whenever BufferedOutputStream's internal
		   buffer gets full and request to write more bytes have arrived. */
		outStream = new BufferedOutputStream(new FileOutputStream(fileToProcess));

		// Clear receive buffer before start
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
						state = RECEIVEDATA;
					} catch (SerialComException exp) {
						outStream.close();
						throw exp;
					}
				}else {
					// fall back to xmodem-128 checksum mode.
					return scm.receiveFile(handle, fileToProcess, FTPPROTO.XMODEM, FTPVAR.CHKSUM, textMode, progressListener, transferState);
				}
				break;
			case RECEIVEDATA:
				// when the receiver is waiting for next block of data following conditions might occur :
				// case 1: sender sent data block only (133 or 1029 length block).
				// case 2: sender sent abort command only (consecutive CAN characters or may have back 
				//         space totaling 2 to 16 characters).
				// case 3: sender sent data block followed immediately by abort command (1031 to 1045(1029+16) total).
				while(true) {
					// check if application (file receiver) wish to cancel receiving file.
					if((transferState != null) && (transferState.isTransferToBeAborted() == true)) {
						outStream.close();
						scm.writeBytes(handle, abortSequence, 0);
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
						firstBlock = false;

						if(data[0] == CAN) {
							if(lastCharacterReceivedWasCAN == true) {
								// (case 2) received 2nd consecutive CAN means sender wish to abort file transfer.
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
									// (case 2) received 2 consecutive CAN means sender wish to abort file transfer.
									try {
										scm.writeSingleByte(handle, ACK);
									} catch (Exception e) {
									}
									errMsg = "Abort command received from file sending application !";
									state = ABORT;
									break;
								}else {
									// this is not valid block as 1st character is CAN instead of SOH/STX.
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
								errMsg = "Invalid data sequence (<CAN> <EOT>) received from file sender !";
								state = ABORT;
								break;
							}

							// indicates that sender has sent the complete file.
							isCorrupted = false;
							rxDone = true;
							state = REPLY;
							break;
						}else if((data[0] == STX) || (handlingLargeBlock == true)) {
							if(lastCharacterReceivedWasCAN == true) {
								// Probably line has noise; abort transfer.
								errMsg = "Invalid data sequence (<CAN> <" + data[0] + ">) received from file sender !";
								state = ABORT;
								break;
							}

							/* At the beginning of block data[0] will be either STX or SOH but 
							 * when receiving partial data data[0] will not be STX/SOH. The 
							 * handlingLargeBlock check tells whether we are receiving 133 or 
							 * 1029 size block. */
							handlingLargeBlock = true;
							if((partialReadInProgress == false) && (data.length == 1029)) {
								// complete block read in one go.
								for(int i=0; i < 1029; i++) {
									block[i] = data[i];
								}
								state = VERIFY;
								break;
							}else {
								// partial block read.
								partialReadInProgress = true;
								for(z=0; z < data.length; z++) {
									if(bufferIndex >= 1029) {
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
												delayVal = 300;  // reset.
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
												delayVal = 300;  // reset.
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
								if(bufferIndex >= 1029) {
									delayVal = 300;  // reset.
									bufferIndex = 0; // reset.
									partialReadInProgress = false; // reset.
									state = VERIFY;
									break;
								}else {
									// next remaining data bytes should arrive early, 
									// go back to read more data from port.
									delayVal = 120;
									continue;
								}	
							}
						}else if((data[0] == SOH) || (handlingLargeBlock == false)) {
							if(lastCharacterReceivedWasCAN == true) {
								// Probably line has noise; abort transfer.
								errMsg = "Invalid data sequence (<CAN> <" + data[0] + ">) received from file sender !";
								state = ABORT;
								break;
							}

							handlingLargeBlock = false;
							if((partialReadInProgress == false) && (data.length == 133)) {
								// complete block read in one go.
								for(int i=0; i < 133; i++) {
									block[i] = data[i];
								}
								state = VERIFY;
								break;
							}else {
								// partial block read.
								partialReadInProgress = true;
								for(z=0; z < data.length; z++) {
									if(bufferIndex >= 133) {
										// this indicates either file sender has sent abort command immediately
										// after sending data block or line has noise; extraneous characters.
										if((data.length - z) >= 2) {
											// check if we received 2 consecutive CAN characters, if yes then abort.
											if((data[z] == CAN) && (data[z+1] == CAN)) {
												errMsg = "Abort command received from file sending application !";
												state = ABORT;
												break;
											}else {
												// extraneous characters, line has noise. go to verification
												// state as we have received full data block thereby
												// discarding extraneous characters.
												delayVal = 300;  // reset.
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
												delayVal = 300;  // reset.
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
								if(bufferIndex >= 133) {
									delayVal = 300;  // reset.
									bufferIndex = 0; // reset.
									partialReadInProgress = false; // reset.
									state = VERIFY;
									break;
								}else {
									// next remaining data bytes should arrive early, 
									// go back to read more data from port.
									delayVal = 100;
									continue;
								}	
							}
						}else {
						}
					}else {
						if(firstBlock == false) {
							if(System.currentTimeMillis() > nextDataRecvTimeOut) {
								errMsg = "Timedout while trying to receive next data byte from file sender !";
								state = ABORT;
								break;
							}
						}else {
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
				// check duplicate block.
				if(block[1] == (blockNumber - 1)){
					isDuplicateBlock = true;
					duplicateBlockRetryCount++;
					if(duplicateBlockRetryCount > 10) {
						errMsg = "Maximum number of retries reached while receiving same data block !";
						state = ABORT;
					}
					break;
				}
				// verify block number sequence.
				if(block[1] != blockNumber){
					isCorrupted = true;
					break;
				}
				// verify block number.
				if(block[1] != ~block[2]){
					isCorrupted = true;
					break;
				}
				// verify CRC value.
				if(handlingLargeBlock == true) {
<<<<<<< HEAD
					blockCRCval = crcCalculator.getCRCval(block, 3, 1026);
=======
					blockCRCval = crcCalculator.getCRC16CCITTValue(block, 3, 1026);
>>>>>>> upstream/master
					if((block[1027] != (byte)(blockCRCval >>> 8)) || (block[1028] != (byte)blockCRCval)){
						isCorrupted = true;
					}
				}else {
<<<<<<< HEAD
					blockCRCval = crcCalculator.getCRCval(block, 3, 130);
=======
					blockCRCval = crcCalculator.getCRC16CCITTValue(block, 3, 130);
>>>>>>> upstream/master
					if((block[131] != (byte)(blockCRCval >>> 8)) || (block[132] != (byte)blockCRCval)){
						isCorrupted = true;
					}
				}
				break;
			case REPLY:
				try {
					if(rxDone == false) {
						if(isCorrupted == false) {
							scm.writeSingleByte(handle, ACK);
							if(textMode == true) {
								// for ASCII mode, parse and then flush.
								if(handlingLargeBlock == true) {
									processAndWrite(block, 1024);
								}else {
									processAndWrite(block, 128);
								}
							}else {
								// for binary mode, just flush data as is to file physically.
								if(handlingLargeBlock == true) {
									outStream.write(block, 3, 1024);
								}else {
									outStream.write(block, 3, 128);
								}
							}

							// update GUI that a block has been received if application has provided 
							// a listener for this purpose.
							if(progressListener != null) {
								numberOfBlocksReceived++;
								progressListener.onXmodemReceiveProgressUpdate(numberOfBlocksReceived);
							}

							if(isDuplicateBlock != true) {
								blockNumber++;
								if(blockNumber > 0xFF) {
									blockNumber = 0x00;
								}
							}
						}else {
							scm.writeSingleByte(handle, NAK);
						}
						handlingLargeBlock = false; // reset.
						state = RECEIVEDATA;
					}else {
						// file reception successfully finished, let us go back home.
						scm.writeSingleByte(handle, ACK);
						outStream.flush();
						outStream.close();
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

		if(dataSize == 1024) {
			if(mark == 1026) {
				// indicates last byte in block array could not be processed as one more bytes was needed to test against
				// all cases. so save this byte and process it next block of data received.
				unprocessedByteInReceivedDataExist = true;
				unprocessedByteInLastReceivedBlock = block[1026];
			}
		}else {
			if(mark == 130) {
				// indicates last byte in block array could not be processed as one more bytes was needed to test against
				// all cases. so save this byte and process it next block of data received.
				unprocessedByteInReceivedDataExist = true;
				unprocessedByteInLastReceivedBlock = block[130];
			}
		}
	}
}
