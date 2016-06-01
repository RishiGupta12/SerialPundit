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

package com.embeddedunveiled.serial.datalogger;

import com.embeddedunveiled.serial.SerialComManager;

/**
 * 
 * @author Rishi Gupta
 */
public final class SerialComDataLogger {

    public static final int DP_START_ANY_CHAR = 0x01;
    public static final int DP_START_ALPHABETIC_CHAR = 0x02;
    public static final int DP_START_NUMERIC_CHAR = 0x03;
    public static final int DP_START_ALPHANUMERIC_CHAR = 0x04;
    public static final int DP_START_SPECIAL_CHAR = 0x05;

    public static final int DP_STOP_CRLF = 0x06;
    public static final int DP_STOP_TIME_DELAY = 0x07;
    public static final int DP_STOP_FIXED_LENGTH = 0x08;
    public static final int DP_STOP_CHAR = 0x09;

    public static final int DP_TYPE_SINGLE_FIELD = 0X10;
    public static final int DP_TYPE_MULTIPLE_DELIMITED_FIELDS = 0X11;
    public static final int DP_TYPE_MUPLTIPLE_FIXED_LENGTH_FIELDS = 0X12;

    public static final int FIELD_FILTER_NONE = 0X13;
    public static final int FIELD_FILTER_DROP = 0X14;
    public static final int FIELD_FILTER_NUM_ONLY = 0X15;

    public static final int FIELD_CONVERT_HEX_TO_DEC = 0X16;
    public static final int FIELD_CONVERT_OCT_TO_DEC = 0X17;
    public static final int FIELD_CONVERT_BIN_TO_HEX = 0X18;

    private final SerialComManager scm;
    private final SerialComLogSource source;
    private final ISerialComToFileErrListener errListener;
    private int dataPacketStartEvent;
    private int dataPacketStopEvent;
    private int dataPacketdelayTime;
    private char dataPacketEndChar;
    private int dataPacketLength;
    private int dataPacketStructure;
    private char fieldDelimiter;
    private int[] lengthOfEachField;
    private int dataFieldFilter;
    private int dataFieldFormConversion;

    public SerialComDataLogger(SerialComManager scm, SerialComLogSource scls, ISerialComToFileErrListener errListener) {
        this.scm = scm;
        this.source = scls;
        this.errListener = errListener;
    }

    public void setDataPacketStartEvent(int dataPacketStartEvent) {
        if((dataPacketStartEvent < 0x01) || (dataPacketStartEvent > 0x05)) {
            throw new IllegalArgumentException("Argument dataPacketStartEvent is invalid !");
        }
        this.dataPacketStartEvent = dataPacketStartEvent;
    }

    public void setDataPacketStopEvent(int dataPacketStopEvent, int dataPacketdelayTime, char dataPacketEndChar, 
            int dataPacketLength) {
        if((dataPacketStartEvent < 0x06) || (dataPacketStartEvent > 0x09)) {
            throw new IllegalArgumentException("Argument dataPacketStopEvent is invalid !");
        }
        this.dataPacketStopEvent = dataPacketStopEvent;

        if(dataPacketStopEvent == DP_STOP_TIME_DELAY) {
            if(dataPacketdelayTime < 0) {
                throw new IllegalArgumentException("Argument dataPacketdelayTime should be positive number !");
            }
        }

        if(dataPacketStopEvent == DP_STOP_FIXED_LENGTH) {
            if(dataPacketLength < 1) {
                throw new IllegalArgumentException("Argument dataPacketLength should be equal or greater than 1 !");
            }
        }

        this.dataPacketdelayTime = dataPacketdelayTime;
        this.dataPacketEndChar = dataPacketEndChar;
        this.dataPacketLength = dataPacketLength;
    }

    public void setDataPacketOverallStructure(int dataPacketStructure, char fieldDelimiter, int[] lengthOfEachField) {
        if((dataPacketStructure < 0x10) || (dataPacketStructure > 0x12)) {
            throw new IllegalArgumentException("Argument dataPacketStructure is invalid !");
        }

        if(dataPacketStructure == DP_TYPE_MUPLTIPLE_FIXED_LENGTH_FIELDS) {
            if(lengthOfEachField == null) {
                throw new IllegalArgumentException("Argument lengthOfEachField can not be null for DP_TYPE_MUPLTIPLE_FIXED_LENGTH_FIELDS !");
            }
            // lengthOfEachField.length will be taken as number of data fields that this data packet will contain.
            if(lengthOfEachField.length < 1) {
                throw new IllegalArgumentException("Argument lengthOfEachField must specify at least one length for DP_TYPE_MUPLTIPLE_FIXED_LENGTH_FIELDS !");
            }
        }
        this.dataPacketStructure = dataPacketStructure;
        this.fieldDelimiter = fieldDelimiter;
        this.lengthOfEachField = lengthOfEachField;
    }

    public void setDataFieldFilter(int dataFieldFilter) {
        if((dataFieldFilter < 0x13) || (dataFieldFilter > 0x15)) {
            throw new IllegalArgumentException("Argument dataFieldFilter is invalid !");
        }
        this.dataFieldFilter = dataFieldFilter;
    }

    public void setDataFieldFormConverter(int dataFieldFormConversion) {
        if((dataFieldFormConversion < 0x16) || (dataFieldFormConversion > 0x18)) {
            throw new IllegalArgumentException("Argument dataFieldFormConversion is invalid !");
        }
        this.dataFieldFormConversion = dataFieldFormConversion;
    }

}






