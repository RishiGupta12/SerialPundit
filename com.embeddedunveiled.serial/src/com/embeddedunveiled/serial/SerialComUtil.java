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

/**
 * <p>Provides common utility functions for serial port communication related projects.</p>
 */
public final class SerialComUtil {

	private static final String HEXNUM = "0123456789ABCDEF";

	/**
	 * <p>Allocates a new SerialComUtil object.</p>
	 */
	public SerialComUtil() {
	}

	/**
	 * <p>This method is for internal use.</p>
	 * 
	 * @param num the long number to convert.
	 * @param paddingChar the character to use for padding.
	 * @param min the minimum length of the resulting String.
	 * @param max the maximum length of the resulting String.
	 */
	private static String toHexString(final long num, final char paddingChar, int min, int max) {
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
	 * <p>This method creates hex string from byte array. This is useful in bluetooth low energy applications where characteristics
	 * returned are to be interpreted or for example Internet of things applications where sensor data is getting exchanged.</p>
	 * 
	 * @param data byte array to be converted into string.
	 * @param separator to be inserted after each hex value.
	 * @return constructed hex string if data.length > 0 otherwise empty string.
	 * @throws IllegalArgumentException if data is null.
	 */
	public static String byteArrayToHexString(final byte[] data, final String separator) {
		StringBuilder sBuilder;

		if(data == null) {
			throw new IllegalArgumentException("Argument data can not be null !");
		}

		if(data.length > 0) {
			sBuilder = new StringBuilder(2 * data.length);
			if(separator != null) {
				for (final byte b : data) {
					sBuilder.append(HEXNUM.charAt((b & 0xF0) >> 4)).append(HEXNUM.charAt((b & 0x0F)));
					sBuilder.append(separator);
				}
				sBuilder.deleteCharAt(sBuilder.length() - 1); //remove separator at the end of string.
			}else {
				for (final byte b : data) {
					sBuilder.append(HEXNUM.charAt((b & 0xF0) >> 4)).append(HEXNUM.charAt((b & 0x0F)));
				}
			}
			return sBuilder.toString();
		}

		return new String();
	}

	/**
	 * <p>Converts given string in hexa-decimal representation to equivalent byte array.</p>
	 * 
	 * @param hexStringData string in hexa-decimal format to be converted into equivalent byte array.
	 * @return constructed byte array from given hex string.
	 * @throws IllegalArgumentException if hexStringData is null.
	 */
	public static byte[] hexStringToByteArray(final String hexStringData) {
		int i = 0;
		int j = 0;
		if(hexStringData == null) {
			throw new IllegalArgumentException("Argument hexStringData can not be null !");
		}

		String hexStr = hexStringData.trim().replaceAll("0x", "");
		hexStr = hexStr.replaceAll("\\s+","");
		byte[] data = new byte[hexStr.length()/2];

		while(i <= (hexStr.length() - 1)) {
			byte character = (byte) Integer.parseInt(hexStr.substring(i, i+2), 16);
			data[j] = character;
			j++;
			i += 2;
		}
		return data;
	}

	/**
	 * <p>Converts the given byte's value to an unsigned integer number. The least significant byte (8 bits) of the integer number
	 * will be identical to the byte (8 bits) provided, and the most significant 3 bytes (24 bits) of the integer will be zero.</p>
	 * 
	 * @param data the byte to convert.
	 * @return An unsigned integer number representing the given byte.
	 */
	public static int byteToUnsignedInt(byte data) {
		return 0x000000ff & ((int) data);
	}

	/**
	 * <p>Converts the given byte's value to an unsigned long number. The least significant byte (8 bits) of the long number 
	 * will be identical to the byte (8 bits) provided, and the most significant 7 bytes (56 bits) of the long will be zero.</p>
	 * 
	 * @param data the byte to convert.
	 * @return An unsigned long number representing the given byte.
	 */
	public static long byteToUnsignedLong(byte data) {
		return 0x00000000000000ff & ((long) data);
	}

	/**
	 * <p>Extract and returns the low byte from the short type number passed.</p>
	 * 
	 * @return the low byte of the short value passed.
	 * @param data the short value from whom low byte is to be extracted.
	 */
	public static byte lowByteFromShort(short data) {
		return (byte) data;
	}

	/**
	 * <p>Extract and returns the high byte from the short type number passed.</p>
	 * 
	 * @return the high byte of the short value passed.
	 * @param data the short value from whom high byte is to be extracted.
	 */
	public static byte highBytefromShort(short data) {
		return (byte) (data >> 8);
	}

	/**
	 * <p>Converts the given short's value to an unsigned integer number. The least significant 2 byte (16 bits) of the integer number
	 * will be identical to the least significant 2 byte (16 bits) of the short number and the most significant 2 bytes (16 bits) of 
	 * the integer will be zero.</p>
	 * 
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
	 * 
	 * @param data the short type value to convert.
	 * @return An unsigned long number representing the given short number.
	 */
	public static long shortToUnsignedLong(short data) {
		return 0x000000000000ffff & ((long) data);
	}

	/**
	 * <p>Converts the given short value to a byte type array in Little endian order.</p>
	 * 
	 * @param data the short type value to convert.
	 * @return a byte array representing given short number.
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
	 * 
	 * @param data the int type value to convert.
	 * @return An unsigned long number representing the given int number.
	 */
	public static long intToUnsignedLong(int data) {
		return 0x00000000ffffffff & ((long) data);
	}

	/**
	 * <p>Converts the given integer value to a byte type array in Little endian order.</p>
	 * 
	 * @param data the short type value to convert.
	 * @return a byte array representing given short number.
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
	 * <p>This converts a number represented in hex string to decimal number. It parses the string 
	 * argument as a signed long with radix as 16.</p>
	 * 
	 * @param hexNumStr hex-string to be converted.
	 * @return a long type number repressing given hex string.
	 * @throws NumberFormatException if the given hex string can not be converted into numerical representation.
	 */
	public static long hexStrToLongNumber(final String hexNumStr) {
		return Long.parseLong(hexNumStr, 16);
	}

	/**
	 * <p>Converts the given long value to hex string.</p>
	 * 
	 * @param num the long type value to convert.
	 * @return a string representing given long number in hexadecimal format.
	 */
	public static String longToHexString(long num) {
		return toHexString(num, '0', 16, 16);
	}

	/**
	 * <p>Converts the given integer value to hex string.</p>
	 * 
	 * @param num the integer type value to convert.
	 * @return a string representing given int number in hexadecimal format.
	 */
	public static String intToHexString(int num) {
		return toHexString(intToUnsignedLong(num), '0', 8, 8);
	}

	/**
	 * <p>Converts the given short value to hex string.</p>
	 * 
	 * @param num the short type value to convert.
	 * @return a string representing given short number in hexadecimal format.
	 */
	public static String shortToHexString(short num) {
		return toHexString(shortToUnsignedLong(num), '0', 4, 4);
	}

	/**
	 * <p>Converts the given byte value to hex string.</p>
	 * 
	 * @param num the byte type value to convert.
	 * @return a string representing given byte value in hexadecimal format.
	 */
	public static String byteToHexString(byte num) {
		return toHexString(byteToUnsignedLong(num), '0', 2, 2);
	}

	/**
	 * <p>Appends given byte array to another given byte array and return newly constructed 
	 * byte array.</p>
	 * 
	 * @param dataA one of the array that need to be added first.
	 * @param dataB array that will be appended to array represented by dataA.
	 * @return a byte array constructed out of appending dataB array to dataA array.
	 */
	public static byte[] concat(byte[] dataA, byte[] dataB) {
		byte[] result = new byte[dataA.length + dataB.length];
		System.arraycopy(dataA, 0, result, 0, dataA.length);
		System.arraycopy(dataB, 0, result, dataA.length, dataB.length);
		return result;
	}

}
