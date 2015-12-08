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

package com.embeddedunveiled.serial.datalogger;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;

/**
 * 
 * @author Rishi Gupta
 */
public final class SerialComToKeyStrokeToApp {
	
	/** <p>The value specifying any character. Integer constant with value 0x01.</p>*/
	public static final int CHAR_ANY = 0x01;
	
	/** <p>The value specifying a numerical character (ASCII characters from value 48 to 57). 
	 * Integer constant with value 0x02.</p>*/
	public static final int CHAR_NUMERIC = 0x02;
	
	/** <p>The value specifying a alphabetical character. (ASCII characters from value 65 to 90 
	 * and 97 to 122). Integer constant with value 0x03.</p>*/
	public static final int CHAR_ALPHABETICAL = 0x03;
	
	/** <p>The value specifying a alphanumerical character. (ASCII characters from value 65 to 90, 
	 * 97 to 122 and 48 to 57). Integer constant with value 0x04.</p>*/
	public static final int CHAR_ALPHANUMERICAL = 0x04;
	
	/** <p>The value specifying a special character. Integer constant with value 0x05.</p>*/
	public static final int CHAR_SPECIAL = 0x05;

	private SerialComManager scm;
	private long comPortHandle;
	private long context;
	private int startOfPacketIndex;
	private int endOfPacketIndex;
	private int sizeOfPacketInBytes;
	private int totalNumOfBytesRead;
	private int numOfBytesRead;
	private int numOfBytesToRead;
	private boolean packetReceptionSynchronized;
	private Thread mDataCollectorThread;
	private Thread mKeyStrokeSenderThread;
	private AtomicBoolean exitDataCollectorThread = new AtomicBoolean(false);
	private AtomicBoolean exitKeyStrokeSenderThread = new AtomicBoolean(false);
	private final Robot robot;
	private final TreeMap<Integer, Integer> preReplaceCharTree;
	private final TreeMap<Integer, Integer> preStripOutCharTree;
	private final TreeMap<Integer, Integer> preDropPacketTree;
	private byte[] packetBuffer;

	/**
	 * 
	 */
	private class RawDataCollectorAndPreProcessor implements Runnable {

		@Override
		public void run() {

			try {
				context = scm.createBlockingIOContext();
			} catch (SerialComException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while(true) {
				// get raw data bytes from given serial port
//				try {
//					if(packetReceptionSynchronized == true) {
////						numOfBytesRead = scm.readBytes(comPortHandle, packetBuffer, offset, length, context);
//					}else {
//						
//					}
//				} catch (SerialComException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

				// pre-process translation


				// insert in queue
				break;
			}

			try {
				scm.unblockBlockingIOOperation(context);
			} catch (SerialComException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	private class PostProcessorAndKeyStrokeGenerator implements Runnable {

		@Override
		public void run() {

		}
	}

	/**
	 * @throws AWTException 
	 * 
	 */
	public SerialComToKeyStrokeToApp(SerialComManager scm, long handle) throws AWTException {
		this.scm = scm;
		comPortHandle = handle;
		robot = new Robot();
		preReplaceCharTree = new TreeMap<Integer, Integer>();
		preStripOutCharTree = new TreeMap<Integer, Integer>();
		preDropPacketTree = new TreeMap<Integer, Integer>();
	}

	/**
	 */
	public boolean setStartOfDataPacketEvent(int charType, int specialCharValue) {
		return false;
	}

	/**
	 */
	public boolean setEndOfDataPacketEvent() {
		return false;
	}

	/**
	 */
	public boolean setOverallDataPacketStructure() {
		return false;
	}

	/**
	 */
	public boolean sendStringAfterEachDataPacketReception() {
		return false;
	}

	/**
	 * <p></p>
	 * 
	 * @param rawDataByteValue ASCII value of character to be replaced.
	 * @param dataByteValueAfterReplacement ASCII value of character to set for replaced character.
	 * 
	 * @return true if replacement rule gets added successfully.
	 */
	public boolean addPreProcessRawDataByteReplaceRule(int rawDataByteValue, int dataByteValueAfterReplacement) {
		if(!((rawDataByteValue >= 0) && (rawDataByteValue <= 127))) {
			throw new IllegalArgumentException("Argument rawDataByteValue can have values from 0 to 127 only !");
		}

		if((dataByteValueAfterReplacement >= 0) && (dataByteValueAfterReplacement <= 127)) {
			preReplaceCharTree.put(rawDataByteValue, dataByteValueAfterReplacement);
		}else {
			throw new IllegalArgumentException("Argument dataByteValueAfterReplacement can have values from 0 to 127 only !");
		}

		return true;
	}

	/**
	 * <p></p>
	 * 
	 * @param rawDataByteValueToStripOut ASCII value of character to be striped from raw data received from 
	 *         serial port.
	 * 
	 * @return true if strip out rule gets added successfully.
	 */
	public boolean addPreProcessRawDataByteStripOutRule(int rawDataByteValueToStripOut) {
		if(!((rawDataByteValueToStripOut >= 0) && (rawDataByteValueToStripOut <= 127))) {
			throw new IllegalArgumentException("Argument rawDataByteValueToStripOut can have values from 0 to 127 only !");
		}

		preStripOutCharTree.put(rawDataByteValueToStripOut, -1);
		return true;
	}

	/**
	 * <p></p>
	 * 
	 * @param rawDataByteValueToIndicateDropPacket ASCII value of character to search for defining dropping 
	 *         of packet.
	 * 
	 * @return true if drop packet rule gets added successfully.
	 */
	public boolean addPreProcessRawDataByteDropPacketRule(int rawDataByteValueToIndicateDropPacket) {
		if(!((rawDataByteValueToIndicateDropPacket >= 0) && (rawDataByteValueToIndicateDropPacket <= 127))) {
			throw new IllegalArgumentException("Argument rawDataByteValueToIndicateDropPacket can have values from 0 to 127 only !");
		}

		preDropPacketTree.put(rawDataByteValueToIndicateDropPacket, -2);
		return true;
	}

	/**
	 */
	public boolean setBeforeSendingKeyStrokeTranslationRules() {
		return false;
	}

	/**
	 */
	public boolean startReadingComPortAndSendingKeyStrokes() {
		return false;
	}

	/**
	 */
	public boolean stopReadingComPortAndSendingKeyStrokes() {
		return false;
	}

	/**
	 * @throws IOException 
	 * @throws IllegalArgumentException
	 */
	public void takeScreenShot(String folderPath, String fileName, String fileExtension) throws IOException {
		if((folderPath == null) || (folderPath.length() == 0)) {
			throw new IllegalArgumentException("The folderPath can not be null or empty string!");
		}
		if((fileName == null) || (fileName.length() == 0)) {
			throw new IllegalArgumentException("The fileName can not be null or empty string!");
		}
		if((fileExtension == null) || (fileExtension.length() == 0)) {
			throw new IllegalArgumentException("The fileExtension can not be null or empty string!");
		}
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle screenRectangle = new Rectangle(screenSize);
		BufferedImage capturedImage = robot.createScreenCapture(screenRectangle);
		String imageFile = folderPath.concat(fileName).concat(".").concat(fileExtension);
		ImageIO.write(capturedImage, fileExtension, new File(imageFile));
	}
}
