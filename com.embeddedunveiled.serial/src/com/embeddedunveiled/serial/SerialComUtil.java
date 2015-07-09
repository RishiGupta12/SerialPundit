/**
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

/**
 * <p>This class provides common utility functions for serial port communication related projects.</p>
 */
public final class SerialComUtil {

	/**
     * <p>Allocates a new SerialComUtil object.</p>
     */
	public SerialComUtil() {
	}
	
	/**
	 * <p>This method is for internal use.</p>
	 * @param num the long number to convert
	 * @param paddingChar the character to use for padding
	 * @param min the minimum length of the resulting String
	 * @param max the maximum length of the resulting String
	 */
	private static String toHexString(long num, char paddingChar, int min, int max) {
		/* Formats a long number into the specified length hex String. This is identical to Long.toHexString() 
		 * except that it pads (with 0's), or truncates, to the specified size. If max < min, the functionality is 
		 * exactly as Long.toHexString(). */
		StringBuffer sb = new StringBuffer(Long.toHexString(num));

		if(max < min) {
			return sb.toString();
		}
			
		while (sb.length() < max) {
			sb.insert(0, paddingChar);
		}
		
		return sb.substring(0, min);
	}
	
	/**
	 * <p>Calculates Longitudinal redundancy checksum value for the given byte array.</p>
	 * 
	 * @param data byte type buffer for whom LRC checksum is to be calculated
	 * @param offset position in supplied data buffer from where LRC calculation should start
	 * @param length position in data buffer till which LRC should be calculated from offset
	 * @return LRC checksum value for the given byte array
	 * @throws NullPointerException if <code>data</code> is <code>null</code>.
	 * @throws IndexOutOfBoundsException if offset is negative, length is negative, or length is greater than data.length - offset
	 * @throws IllegalArgumentException if data is not a byte type array
	 */
	public static byte calculateLRCCheckSum(byte[] data, int offset, int length) {
        byte checkSum = 0;
        
		if(data == null) {
			throw new NullPointerException("LRCCheckSum(), " + SerialComErrorMapper.ERR_NULL_DATA_PASSED);
		}
		if((offset < 0) || (length < 0) || (length > (data.length - offset))) {
			throw new IndexOutOfBoundsException("LRCCheckSum(), " + SerialComErrorMapper.ERR_INDEX_VIOLATION);
		}
		if(!(data instanceof byte[])) {
			throw new IllegalArgumentException(SerialComErrorMapper.ERR_ARG_NOT_BYTE_ARRAY);
		}
		
        for (int i = offset; i < offset + length; i++) {
        	checkSum ^= data[i];
        }
        return checkSum;
    }

	/**
	 * <p>Converts the given byte's value to an unsigned integer number. The least significant byte (8 bits) of the integer number
	 * will be identical to the byte (8 bits) provided, and the most significant 3 bytes (24 bits) of the integer will be zero.</p>
	 * @param data the byte to convert.
	 * @return An unsigned integer number representing the given byte.
	 */
	public static int byteToUnsignedInt(byte data) {
		return 0x000000ff & ((int) data);
	}
	
	/**
	 * <p>Converts the given byte's value to an unsigned long number. The least significant byte (8 bits) of the long number 
	 * will be identical to the byte (8 bits) provided, and the most significant 7 bytes (56 bits) of the long will be zero.</p>
	 * @param data the byte to convert.
	 * @return An unsigned long number representing the given byte.
	 */
	public static long byteToUnsignedLong(byte data) {
		return 0x00000000000000ff & ((long) data);
	}
	
	/**
	 * <p>Extract and returns the low byte from the short type number passed.</p>
	 * @return the low byte of the short value passed
	 * @param data the short value from whom low byte is to be extracted
	 */
	public static byte lowByteFromShort(short data) {
		return (byte) data;
	}

	/**
	 * <p>Extract and returns the high byte from the short type number passed.</p>
	 * @return the high byte of the short value passed
	 * @param data the short value from whom high byte is to be extracted
	 */
	public static byte highBytefromShort(short data) {
		return (byte) (data >> 8);
	}

	/**
	 * <p>Converts the given short's value to an unsigned integer number. The least significant 2 byte (16 bits) of the integer number
	 * will be identical to the least significant 2 byte (16 bits) of the short number and the most significant 2 bytes (16 bits) of 
	 * the integer will be zero.</p>
	 * @param data the short type value to convert.
	 * @return An unsigned integer number representing the given short number.
	 */
	public static int shortToUnsignedInt(short data) {
		return 0x0000ffff & ((int) data);
	}
	
	/**
	 * <p>Converts the given short's value to an unsigned long number. The least significant 2 byte (16 bits) of the long number
	 * will be identical to the least significant 2 byte (16 bits) of the short number and the most significant 6 bytes (48 bits) of 
	 * the long number will be zero.</p>
	 * @param data the short type value to convert.
	 * @return An unsigned long number representing the given short number.
	 */
	public static long shortToUnsignedLong(short data) {
		return 0x000000000000ffff & ((long) data);
	}
	
	/**
	 * <p>Converts the given short value to a byte type array in Little endian order.</p>
	 * @param data the short type value to convert.
	 * @return a byte array representing given short number
	 */
	public static byte[] shortToByteArray(short data) {
		byte[] result = new byte[2];
	    result[0] = (byte) (data & 0xff);
	    result[1] = (byte) ((data >>> 8) & 0xff);
	    return result;
	}
	
	/**
	 * <p>Converts the given integer value to an unsigned long number. The least significant 4 bytes (32 bits) of the long number
	 * will be identical to the least significant 4 bytes (32 bits) of the integer number and the most significant 4 bytes (32 bits) of 
	 * the long number will be zero.</p>
	 * @param data the int type value to convert.
	 * @return An unsigned long number representing the given int number.
	 */
	public static long intToUnsignedLong(int data) {
		return 0x00000000ffffffff & ((long) data);
	}
	
	/**
	 * <p>Converts the given integer value to a byte type array in Little endian order.</p>
	 * @param data the short type value to convert.
	 * @return a byte array representing given short number
	 */
	public static byte[] intToByteArray(int data) {
		byte[] result = new byte[4];
	    result[0] = (byte) (data & 0xff);
	    result[1] = (byte) ((data >>> 8) & 0xff);
	    result[1] = (byte) ((data >>> 16) & 0xff);
	    result[1] = (byte) ((data >>> 24) & 0xff);
	    return result;
	}
	
	/**
	 * <p>Converts the given long value to hex string.</p>
	 * @param data the long type value to convert.
	 * @return a string representing given long number in hexadecimal format
	 */
	public static String longToHexString(long num) {
		return toHexString(num, '0', 16, 16);
	}
	
	/**
	 * <p>Converts the given integer value to hex string.</p>
	 * @param data the integer type value to convert.
	 * @return a string representing given int number in hexadecimal format
	 */
	public static String intToHexString(int num) {
		return toHexString(intToUnsignedLong(num), '0', 8, 8);
	}
	
	/**
	 * <p>Converts the given short value to hex string.</p>
	 * @param data the short type value to convert.
	 * @return a string representing given short number in hexadecimal format
	 */
	public static String shortToHexString(short num) {
		return toHexString(shortToUnsignedLong(num), '0', 4, 4);
	}
	
	/**
	 * <p>Converts the given byte value to hex string.</p>
	 * @param data the byte type value to convert.
	 * @return a string representing given byte value in hexadecimal format
	 */
	public static String byteToHexString(byte num) {
		return toHexString(byteToUnsignedLong(num), '0', 2, 2);
	}
	
}
