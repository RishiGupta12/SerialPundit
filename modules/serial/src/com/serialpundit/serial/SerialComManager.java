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

package com.serialpundit.serial;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;

import com.serialpundit.core.SerialComPlatform;
import com.serialpundit.core.SerialComSystemProperty;
import com.serialpundit.core.SerialComException;
import com.serialpundit.serial.comdb.SerialComDBRelease;
import com.serialpundit.serial.ftp.ISerialComXmodemProgress;
import com.serialpundit.serial.ftp.ISerialComYmodemProgress;
import com.serialpundit.serial.ftp.SerialComFTPCMDAbort;
import com.serialpundit.serial.ftp.SerialComXModem;
import com.serialpundit.serial.ftp.SerialComXModem1K;
import com.serialpundit.serial.ftp.SerialComXModemCRC;
import com.serialpundit.serial.ftp.SerialComYModem1K;
import com.serialpundit.serial.ftp.SerialComYModemCRC;
import com.serialpundit.serial.ftp.SerialComYModemG;
import com.serialpundit.serial.mapper.SerialComPortMapper;
import com.serialpundit.serial.nullmodem.SerialComNullModem;
import com.serialpundit.serial.vendor.SerialComVendorLib;
import com.serialpundit.serial.internal.ISerialIOStream;
import com.serialpundit.serial.internal.SerialComCompletionDispatcher;
import com.serialpundit.serial.internal.SerialComDBReleaseJNIBridge;
import com.serialpundit.serial.internal.SerialComLooper;
import com.serialpundit.serial.internal.SerialComPortHandleInfo;
import com.serialpundit.serial.internal.SerialComPortJNIBridge;
import com.serialpundit.serial.internal.SerialComPortMapperJNIBridge;
import com.serialpundit.serial.internal.SerialComPortsList;
import com.serialpundit.serial.internal.ISerialComFTPProgress;


/**
 * <p>Root of SerialPundit.</p>
 * 
 * <ul><li><p>Native layer if fails to throw exception when an error occurs would log error message to STDERR file. 
 *     It is assumed that Java application running on production systems will deploy a Java logger which will 
 *     redirect STDERR messages to a log file. This way if an error occurs and native layer could not throw 
 *     exception for example in out of memory case it will still be logged for later analysis.</p></li>
 * </ul>
 * 
 * @author Rishi Gupta
 * @version 1.0.4
 */
public final class SerialComManager {

    /**<p>Production release version of the SerialPundit. </p>*/
    public static final String JAVA_LIB_VERSION = "1.0.4";

    /** <p>Pre-defined enum constants for baud rate values. </p>*/
    public enum BAUDRATE {
        B0(0), B50(50), B75(75), B110(110), B134(134), B150(150), B200(200), B300(300), B600(600), 
        B1200(1200), B1800(1800), B2400(2400), B4800(4800), B9600(9600), B14400(14400), B19200(19200), 
        B28800(28800), B38400(38400), B56000(56000), B57600(57600), B115200(115200), B128000(128000), 
        B153600(153600), B230400(230400), B256000(256000), B460800(460800), B500000(500000), 
        B576000(576000), B921600(921600), B1000000(1000000), B1152000(1152000), B1500000(1500000), 
        B2000000(2000000), B2500000(2500000), B3000000(3000000), B3500000(3500000), B4000000(4000000),
        BCUSTOM(251);
        private int value;
        private BAUDRATE(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for number of data bits in a serial frame. </p>*/
    public enum DATABITS {
        /** <p>Serial frame will contain 5 data bits. </p>*/
        DB_5(5),
        /** <p>Serial frame will contain 6 data bits. </p>*/
        DB_6(6),
        /** <p>Serial frame will contain 7 data bits. </p>*/
        DB_7(7),
        /** <p>Serial frame will contain 8 data bits. </p>*/
        DB_8(8);
        private int value;
        private DATABITS(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for number of stop bits in a serial frame. </p>*/
    public enum STOPBITS {
        /** <p>Number of stop bits in one frame is 1. </p>*/
        SB_1(1),
        /** <p>Number of stop bits in one frame is 1.5. </p>*/
        SB_1_5(4),
        /** <p>Number of stop bits in one frame is 2. </p>*/
        SB_2(2);
        private int value;
        private STOPBITS(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for enabling type of parity in a serial frame. </p>*/
    public enum PARITY {
        /** The uart frame does not contain any parity bit. Errors are handled by application for example 
         * using CRC algorithm.*/
        P_NONE(1),
        /** <p>The number of bits in the frame with the value one is odd. If the sum of bits 
         * with a value of 1 is odd in the frame, the parity bit's value is set to zero. 
         * If the sum of bits with a value of 1 is even in the frame, the parity bit value 
         * is set to 1, making the total count of 1's in the frame an odd number. </p>*/
        P_ODD(2),
        /** <p>The number of bits in the frame with the value one is even. The number 
         * of bits whose value is 1 in a frame is counted. If that total is odd, 
         * the parity bit value is set to 1, making the total count of 1's in the frame 
         * an even number. If the count of ones in a frame a is already even, 
         * the parity bit's value remains 0. </p>
         * <p>Odd parity may be more fruitful since it ensures that at least one state 
         * transition occurs in each character, which makes it more reliable as compared 
         * even parity. </p>
         * <p>Even parity is a special case of a cyclic redundancy check (CRC), 
         * where the 1-bit CRC is generated by the polynomial x+1.</p>*/
        P_EVEN(3),
        /** <p>The parity bit is set to the mark signal condition (logical 1). An application
         * may use the 9th (parity) bit for some form of addressing or special signaling. 
         * The mark parity is also known as stick parity.</p>*/
        P_MARK(4),
        /** <p>The parity bit is set to the space signal condition (logical 0). The mark 
         * and space parity is uncommon, as it adds no error detection information. The space parity 
         * is also known as stick parity.</p>*/
        P_SPACE(5);
        private int value;
        private PARITY(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for controlling data flow between DTE and DCE or two serial devices.</p>*/
    public enum FLOWCONTROL {
        /** <p>No flow control; application is responsible to manage data buffers. Application can 
         * assert or de-assert RTS/CTS or DTR/DSR signals explicitly. </p>*/
        NONE(1),
        /** <p>Operating system (or driver) will assert or de-assert RTS/CTS lines as per the amount of 
         * data in input buffers. </p>*/
        RTS_CTS(2),
        /** <p>Operating system (or driver) will assert or de-assert DTR/DSR lines as per the amount of 
         * data in input buffers. </p>*/
        DTR_DSR(3),
        /** <p>Operating system (or driver) will send XON or XOFF characters as per the amount of data 
         * in input buffers. Upon reception of XOFF system will stop transmitting data and vice-versa. </p>*/
        XON_XOFF(4);
        private int value;
        private FLOWCONTROL(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for defining endianness of data to be sent over serial port.</p>*/
    public enum ENDIAN {
        /** <p>Little endian data format. The least significant byte (LSB) value is at the lowest 
         * address.</p>*/
        E_LITTLE(1),
        /** <p>Big endian data format. The most significant byte (MSB) value is at the lowest address.</p>*/
        E_BIG(2),
        /** <p>Platform default. </p>*/
        E_DEFAULT(3);
        private int value;
        private ENDIAN(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for defining number of bytes given data can be represented in.</p>*/
    public enum NUMOFBYTES {
        /** <p>Integer value requires 16 bits. </p>*/
        NUM_2(2),
        /** <p>Integer value requires 32 bits. </p>*/
        NUM_4(4);
        private int value;
        private NUMOFBYTES(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for defining file transfer protocol to use.</p>*/
    public enum FTPPROTO {
        /** <p>XMODEM protocol with three variants checksum, CRC and 1k.</p>*/
        XMODEM(1),
        /** <p>YMODEM protocol with two variants CRC and 1k.</p>*/
        YMODEM(2),
        /** <p>coming soon</p>*/
        ZMODEM(3);
        private int value;
        private FTPPROTO(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for defining variant of file transfer protocol to use. </p>*/
    public enum FTPVAR {
        /** <p>1 byte checksum with 128 byte data block variant for X modem protocol. If you are new 
         * to FTP protocols start with this. </p>*/
        CHKSUM(1),
        /** <p>2 bytes CRC with 128 byte data block variant for X/Y modem protocols. </p>*/
        CRC(2),
        /** <p>2 byte CRC with 128/1024 bytes data block variant for X/Y modem protocols. </p>*/
        VAR1K(3),
        /** <p>2 byte CRC with 128/1024 bytes data block Ymodem-G variant of Y modem protocol. </p>*/
        VARG(4);
        private int value;
        private FTPVAR(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Pre-defined enum constants for defining behavior of byte stream. </p>*/
    public enum SMODE {
        /** <p>Read / Write operation will block till data is available. </p>*/
        BLOCKING(1), 
        /** <p>Read / Write operation will not block till data is available. </p>*/
        NONBLOCKING(2);
        private int value;
        private SMODE(int value) {
            this.value = value;	
        }
        public int getValue() {
            return this.value;
        }
    }

    /** <p>Default number of bytes (1024) to read from serial port. </p>*/
    public static final int DEFAULT_READBYTECOUNT = 1024;

    /** <p>Clear to send mask bit constant for UART control line. Integer constant with value 0x01. </p>*/
    public static final int CTS =  0x01;  // 0000001

    /** <p>Data set ready mask bit constant for UART control line. Integer constant with value 0x02. </p>*/
    public static final int DSR =  0x02;  // 0000010

    /** <p>Data carrier detect mask bit constant for UART control line. Integer constant with value 0x04. </p>*/
    public static final int DCD =  0x04;  // 0000100

    /** <p>Ring indicator mask bit constant for UART control line. Integer constant with value 0x08. </p>*/
    public static final int RI  =  0x08;  // 0001000

    /** <p>Loop indicator mask bit constant for UART control line. Integer constant with value 0x10. </p>*/
    public static final int LOOP = 0x10;  // 0010000

    /** <p>Request to send mask bit constant for UART control line. Integer constant with value 0x20. </p>*/
    public static final int RTS =  0x20;  // 0100000

    /** <p>Data terminal ready mask bit constant for UART control line. Integer constant with value 0x40. </p>*/
    public static final int DTR  = 0x40;  // 1000000

    /** <p>The value indicating instance of SerialComInByteStream class is desired. Integer constant with value 0x32. </p>*/
    public static final int InputStream = 0X32;

    /** <p>The value indicating instance of SerialComOutByteStream class is desired. Integer constant with value 0x33. </p>*/
    public static final int OutputStream = 0X33;


    /** <p>The exception message indicating that a blocked read method has been unblocked 
     * and made to return to caller explicitly (irrespective there was data to read or not). </p>*/
    public static final String EXP_UNBLOCKIO  = "I/O operation unblocked !";

    // This provides guaranteed log(n) time cost for the containsKey, get, put and remove operations.
    // It maps opened handle of serial device to its information object. This map may be accessed in 
    // locked state for maintaining integrity and consistency whenever required.
    private final TreeMap<Long, SerialComPortHandleInfo> mPortHandleInfo = new TreeMap<Long, SerialComPortHandleInfo>();

    private int osType = SerialComPlatform.OS_UNKNOWN;
    private int cpuArch = SerialComPlatform.ARCH_UNKNOWN;
    private int abiType = SerialComPlatform.ABI_UNKNOWN;

    private SerialComPlatform mSerialComPlatform;
    private final SerialComSystemProperty mSerialComSystemProperty;
    private final SerialComPortJNIBridge mComPortJNIBridge;
    private final SerialComCompletionDispatcher mEventCompletionDispatcher;
    private final SerialComPortsList mSerialComPortsList;
    private final Object lockB = new Object();

    private static final Object lockA = new Object();
    private static boolean nativeLibLoadAndInitAlready = false;
    private static SerialComVendorLib mSerialComVendorLib;
    private static SerialComNullModem mSerialComNullModem;
    private static SerialComPortMapperJNIBridge mSerialComPortMapperJNIBridge;
    private static SerialComDBReleaseJNIBridge mSerialComDBReleaseJNIBridge;

    // Whenever an exception/error occurs in native function, it throws that exception.
    // When java method return from native call, extra check is added to make error
    // detection more robust. If for some unexpected reason JVM does not throw exception
    // then an extra check will make exception to be thrown in java layer.

    /**
     * <p>Allocates a new SerialComManager object. Identify operating system type, CPU architecture, prepares 
     * environment required for running this library, initiates extraction and loading of native libraries.</p>
     * 
     * <p>The native shared library will be extracted in folder named 'sp_tuartx1' inside system/user 'temp' 
     * folder or user home folder if access to 'temp' folder is denied.</p>
     * 
     * <p>On ARM Linux embedded platform it is possible to run entire 32 bit user space on a 64 bit ARMv8 processor. 
     * In such scenarios, the 32 bit and 64 bit shared libraries must not be mixed. Consider either full 32 bit user 
     * space root file system or entire root file system to be 64 bit.</p>
     * 
     * <p>Please contact author of this library if you want to use CPU optimized native shared libraries in your 
     * embedded product for achieving maximum performance.</p>
     * 
     * @throws IOException if file operations on "/proc/cpuinfo" fails for Linux on ARM platform, if java system 
     *         properties can not be accessed, if file "/proc/cpuinfo" can not be found for Linux on ARM platform, 
     *         if native libraries are not found or can not be loaded/linked and initialized. If appropriate 
     *         files/directories can not be read or written.
     * @throws IllegalArgumentException if directoryPath is null, directoryPath is empty, loadedLibName is null 
     *         or empty.
     */
    public SerialComManager() throws IOException {

        mSerialComSystemProperty = new SerialComSystemProperty();
        mSerialComPlatform = new SerialComPlatform(mSerialComSystemProperty);

        if(osType == SerialComPlatform.OS_UNKNOWN) {
            osType = mSerialComPlatform.getOSType();
            if(osType == SerialComPlatform.OS_UNKNOWN) {
                throw new SerialComException("Could not identify operating system. Please report your environemnt to us so that we can add support for it !");
            }
        }
        if(cpuArch == SerialComPlatform.ARCH_UNKNOWN) {
            cpuArch = mSerialComPlatform.getCPUArch(osType);
            if(cpuArch == SerialComPlatform.ARCH_UNKNOWN) {
                throw new SerialComException("Could not identify CPU architecture. Please report your environemnt to us so that we can add support for it !");
            }
        }
        if((cpuArch == SerialComPlatform.ARCH_ARMV8) || (cpuArch == SerialComPlatform.ARCH_ARMV7) || (cpuArch == SerialComPlatform.ARCH_ARMV6) || 
                (cpuArch == SerialComPlatform.ARCH_ARMV5)) {
            if(osType == SerialComPlatform.OS_LINUX) {
                abiType = mSerialComPlatform.getABIType();
            }else {
                throw new SerialComException("Please report to us your environemnt to us !");
            }
        }

        synchronized(SerialComManager.lockA) {
            mComPortJNIBridge = new SerialComPortJNIBridge();
            if(nativeLibLoadAndInitAlready == false) {
                SerialComPortJNIBridge.loadNativeLibrary(null, null, mSerialComSystemProperty, osType, cpuArch, abiType, false);
                mComPortJNIBridge.initNativeLib();
                nativeLibLoadAndInitAlready = true;
            }
        }

        mEventCompletionDispatcher = new SerialComCompletionDispatcher(mComPortJNIBridge, mPortHandleInfo);
        mSerialComPortsList = new SerialComPortsList(mComPortJNIBridge, osType);
    }

    /**
     * <p>Allocates a new SerialComManager object. Identify operating system type, CPU architecture, prepares 
     * environment required for running this library, initiates extraction and loading of native libraries.</p>
     *
     * <p>By default native shared library will be extracted in temp folder. If this constructor is used then,
     * It extracts native shared library in the folder specified by argument directoryPath and gives library name 
     * specified by loadedLibName. If the argument createDirectory is true, it will create directory (including 
     * parent if it does not exist) as specified by directoryPath, otherwise user should make sure that this directory 
     * exist before calling this constructor.</p>
     *
     * <ul>
     * <li>Sometimes system administrator may have put some restriction on tmp/temp folder or the there may some 
     * other inevitable situations like anti-virus program causing trouble when using temp folder. This constructor
     * will help in handling such situations.</li>
     * 
     * <li><p>Two or more absolutely independent vendors may package this library into their product's jar file. Now 
     * when using default constructor both will extract and use the same folder and library name resulting in inconsistent
     * software. This constructor handle this situation by providing vendor specific isolated environment.</p></li>
     * 
     * <li><p>This may also increase security as the folder may be given specific user permissions. To extract library in 
     * directory "/home/xxx/yyy", name it lib2 and create directory yyy an example is given below.</p>
     * SerialComManager scm = new SerialComManager("lib2", "/home/xxx/yyy", true);</li>
     * 
     * <li>If hotDeploy is true than a different strategy is used to load the shared native libraries. This option can be 
     * true if this software is used on tomcat server for hot deployment purpose or may be during development. If two or 
     * more web applications use this library and both do the hot deployment, than loadedLibName should be different 
     * for both the applications. This will prevent unnecessary conflicts or unforeseen side effects.</li>
     * </ul>
     * 
     * @param loadedLibName library name without extension (do not append .so, .dll or .dylib etc.).
     * @param directoryPath absolute path of directory to be used for purpose of extraction.
     * @param createDirectory true if directory is to be created otherwise false if given directory already exist.
     * @param hotDeploy true if hot deployment is to be supported otherwise false.
     * @throws IOException if file operations on "/proc/cpuinfo" fails for Linux on ARM platform, if java system 
     *         properties can not be accessed, if file "/proc/cpuinfo" can not be found for Linux on ARM platform, 
     *         if native libraries are not found or can not be loaded/linked and initialized. If appropriate 
     *         files/directories can not be read or written.
     * @throws IllegalArgumentException if directoryPath is null, directoryPath is empty, loadedLibName is null 
     *         or empty.
     */
    public SerialComManager(String loadedLibName, String directoryPath, final boolean createDirectory, 
            boolean hotDeploy) throws IOException {

        if(directoryPath == null) {
            throw new IllegalArgumentException("Argument directoryPath can not be null !");
        }
        if(directoryPath.length() == 0) {
            throw new IllegalArgumentException("Argument directoryPath can not be empty string !");
        }
        if(createDirectory == true) {
            File extractionDirectory = new File(directoryPath);
            extractionDirectory.mkdirs();
        }

        if(loadedLibName == null) {
            throw new IllegalArgumentException("Argument loadedLibName can not be null !");
        }
        if(loadedLibName.length() == 0) {
            throw new IllegalArgumentException("Argument loadedLibName can not be empty string !");
        }

        mSerialComSystemProperty = new SerialComSystemProperty();
        mSerialComPlatform = new SerialComPlatform(mSerialComSystemProperty);

        if(osType == SerialComPlatform.OS_UNKNOWN) {
            osType = mSerialComPlatform.getOSType();
            if(osType == SerialComPlatform.OS_UNKNOWN) {
                throw new SerialComException("Could not identify operating system. Please report your environment to us !");
            }
        }
        if(cpuArch == SerialComPlatform.ARCH_UNKNOWN) {
            cpuArch = mSerialComPlatform.getCPUArch(osType);
            if(cpuArch == SerialComPlatform.ARCH_UNKNOWN) {
                throw new SerialComException("Could not identify CPU architecture. Please report your environment to us !");
            }
        }
        if((cpuArch == SerialComPlatform.ARCH_ARMV8) || (cpuArch == SerialComPlatform.ARCH_ARMV7) || (cpuArch == SerialComPlatform.ARCH_ARMV6) || 
                (cpuArch == SerialComPlatform.ARCH_ARMV5)) {
            if(osType == SerialComPlatform.OS_LINUX) {
                abiType = mSerialComPlatform.getABIType();
            }else {
                throw new SerialComException("Please report to us your environment to us !");
            }
        }

        synchronized(SerialComManager.lockA) {
            mComPortJNIBridge = new SerialComPortJNIBridge();
            if(nativeLibLoadAndInitAlready == false) {
                SerialComPortJNIBridge.loadNativeLibrary(directoryPath, loadedLibName, mSerialComSystemProperty, osType, cpuArch, abiType, hotDeploy);
                mComPortJNIBridge.initNativeLib();
                nativeLibLoadAndInitAlready = true;
            }
        }

        mEventCompletionDispatcher = new SerialComCompletionDispatcher(mComPortJNIBridge, mPortHandleInfo);
        mSerialComPortsList = new SerialComPortsList(mComPortJNIBridge, osType);
    }

    /**
     * <p>Gives library versions of java and native library implementations for serial port communication.</p>
     * 
     * @return Java and C library versions implementing this library.
     * @throws SerialComException if native library version could not be determined.
     */
    public String getLibraryVersions() throws SerialComException {
        String version = null;
        String nativeLibversion = mComPortJNIBridge.getNativeLibraryVersion();
        if(nativeLibversion != null) {
            version = "Java lib version: " + JAVA_LIB_VERSION + "\n" + "Native lib version: " + nativeLibversion;
        }else {
            version = "Java lib version: " + JAVA_LIB_VERSION + "\n" + "Native lib version: ?????";
        }
        return version;
    }

    /**
     * <p>Returns all available UART style ports available on this system, otherwise an empty array of strings, 
     * if no serial style port is found in the system.</p>
     * 
     * <p>This should find regular UART ports, hardware/software virtual COM ports, port server, USB-UART 
     * converter, bluetooth/3G dongles, ports connected through USB hub/expander, serial card, serial controller, 
     * pseudo terminals, printers and virtual modems etc.</p>
     * 
     * <p>This method may be used to find valid serial ports for communications before opening them for writing 
     * more robust application.</p>
     * 
     * <p>Note : The BIOS may ignore UART ports on a PCI card and therefore BIOS settings has to be corrected 
     * if you modified default BIOS in OS.</p>
     * 
     * @return Available serial ports name for windows, full path with name for Unix like OS, returns 
     *         empty array if no ports are found.
     * @throws SerialComException if an I/O error occurs.
     */
    public String[] listAvailableComPorts() throws SerialComException {
        String[] availablePorts = mSerialComPortsList.listAvailableComPorts();
        if(availablePorts != null) {
            return availablePorts;
        }else {
            return new String[] { };
        }	
    }	

    /** 
     * <p>Opens a serial port for communication. If an attempt is made to open a port which is already 
     * opened, an exception will be thrown.</p>
     * 
     * <ul>
     * <li>For Linux and Mac OS X, if exclusiveOwnerShip is true, before this method returns, the caller 
     * will either be exclusive owner or not. If the caller is successful in becoming exclusive owner than 
     * all the attempt to open the same port again will cause native code to return error. Note that a root 
     * owned process (root user) will still be able to open the port.
     * 
     * <p>For Windows the exclusiveOwnerShip must be true as it does not allow sharing COM ports. An 
     * exception is thrown if exclusiveOwnerShip is set to false. For Solaris, exclusiveOwnerShip should be 
     * set to false as of now. On Unix-like system this method uses ioctl command TIOCEXCL for exclusive access.
     * and not lock files</p></li>
     *
     * <li>This method will clear both input and output buffers of drivers (or operating system).</li>
     * 
     * <li><p>When the serial port is opened DTR and RTS lines will be raised by default by this library. Sometimes, 
     * DTR acts as a modem on-hook/off-hook control for other end. Modern modems are highly flexible in their dependency, 
     * working and configurations. It is best to consult modem manual. If the application design need DTR/RTS 
     * not to be asserted when port is opened custom drivers can be used or hardware can be modified for this 
     * purpose. Alternatively, if the application is to be run on Windows operating system only, then modifying 
     * INF file or registry key may help in not raising DTR/RTS when port is opened. Typically in Windows DTR/RTS 
     * is raised due to enumeration sequence (serenum).</p>
     * 
     * <p>In Unix jargon a dial-in TTY device is used for terminals, modems and printers etc. and requires DCD to be 
     * high for operation. When used with a modem, the port will wait for carrier before sending out the login prompt to 
     * end user. It is for this reason typically DTR of one end is connected to DSR of other end. When the terminal is 
     * turned off, any associated jobs are killed, and the user is logged out. Unlike dial-in the dial-out TTY device does 
     * not require DCD to be high. Once connection is made DCD may go high. Loss of the DCD signal may cause the jobs 
     * to be killed and the user will be automatically logged off.</p></li>
     * 
     * <li>On some hardware when opening a serial port TXD, RXD, RTS or DTR lines may show glitch which may unintentionally 
     * trigger other end. This may be due to hardware or a driver bug.</li>
     * </ul>
     * 
     * <p>This method is thread safe.</p>
     * 
     * @param portName name of the port to be opened for communication.
     * @param enableRead allows application to read bytes from this port.
     * @param enableWrite allows application to write bytes to this port.
     * @param exclusiveOwnerShip application wants to become exclusive owner of this port or not.
     * @return handle of the port successfully opened.
     * @throws IllegalStateException if trying to become exclusive owner when port is already opened.
     * @throws IllegalArgumentException if portName is null or invalid length, or if both enableRead and 
     *         enableWrite are set to false, if trying to open port in Windows without being exclusive owner.
     * @throws SerialComException if the port can be opened for some reason.
     */
    public long openComPort(final String portName, boolean enableRead, boolean enableWrite, boolean exclusiveOwnerShip) throws SerialComException {

        long handle = 0;
        SerialComPortHandleInfo handleInfo = null;

        if(portName == null) {
            throw new IllegalArgumentException("Argument portName can not be null !");
        }
        String portNameVal = portName.trim();
        if(portNameVal.length() == 0) {
            throw new IllegalArgumentException("Name of the port to be opened can not be empty string !");
        }
        if((enableRead == false) && (enableWrite == false)) {
            throw new IllegalArgumentException("Arguments enableRead and enableWrite both can not be set to false !");
        }

        if(osType == SerialComPlatform.OS_WINDOWS) {
            // For windows COM port can not be shared, so throw exception
            if(exclusiveOwnerShip == false) {
                throw new IllegalArgumentException("Windows OS does not allow COM port sharing; exclusiveOwnerShip must be true !");
            }
        }

        synchronized(lockB) {
            /* Try to reduce transitions from java to JNI layer as it is possible here by performing check in java layer itself. */
            if(exclusiveOwnerShip == true) {
                for (Map.Entry<Long, SerialComPortHandleInfo> entry : mPortHandleInfo.entrySet()) {
                    handleInfo = entry.getValue();
                    if(handleInfo != null) {
                        if(handleInfo.containsPort(portNameVal)) {
                            throw new IllegalStateException("The port " + portNameVal + " is already opened. Exclusive ownership can not be claimed !");
                        }
                    }
                }
            }

            handle = mComPortJNIBridge.openComPort(portNameVal, enableRead, enableWrite, exclusiveOwnerShip);
            if(handle < 0) {
                /* JNI should have already thrown exception, this is an extra check to increase reliability of program. */
                throw new SerialComException("Could not open the port " + portNameVal + ". Please retry !");
            }

            mPortHandleInfo.put(handle, new SerialComPortHandleInfo(portNameVal, handle, null, null, null));
        }

        return handle;
    }

    /**
     * <p>Close the serial port. Application should unregister listeners if it has registered any before 
     * calling this method.</p>
     * 
     * <p>DTR line is dropped when port is closed.</p>
     * 
     * <p>If a blocking operation is in progress like readBytesBlocking() etc. than first such operation must be 
     * unblocked and then only closeComPort() should be called. It is invalid application design to violate this rule.</p> 
     * 
     * <p>This method is thread safe.</p>
     * 
     * @param handle of the port to be closed.
     * @return Return true if the serial port is closed.
     * @throws SerialComException if invalid handle is passed or when it fails in closing the port.
     * @throws IllegalStateException if application tries to close port while data/event listeners, 
     *          or input/output byte streams exist.
     */
    public boolean closeComPort(long handle) throws SerialComException {

        SerialComPortHandleInfo handleInfo = null;

        synchronized(lockB) {
            handleInfo = mPortHandleInfo.get(handle);
            if(handleInfo == null) {
                throw new SerialComException("Given handle is alien to me !");
            }

            /* Proper clean up requires that sw/hw resources should be freed before closing the serial port */
            if(handleInfo.getDataListener() != null) {
                throw new IllegalStateException("Closing port without unregistering data listener is not allowed to prevent inconsistency !");
            }
            if(handleInfo.getEventListener() != null) {
                throw new IllegalStateException("Closing port without unregistering event listener is not allowed to prevent inconsistency !");
            }
            if(handleInfo.getSerialComInByteStream() != null) {
                throw new IllegalStateException("Input byte stream must be closed before closing the serial port !");
            }
            if(handleInfo.getSerialComOutByteStream() != null) {
                throw new IllegalStateException("Output byte stream must be closed before closing the serial port !");
            }

            int ret = mComPortJNIBridge.closeComPort(handle);
            if(ret < 0) {
                throw new SerialComException("Could not close the given serial port. Please retry !");
            }

            /* delete info about this port/handle from global information object. */
            mPortHandleInfo.remove(handle);
        }

        return true;
    }

    /**
     * <p>This method writes bytes from the specified byte type buffer. If the method returns false, the 
     * application should try to re-send bytes. The data has been transmitted out of serial port when this 
     * method returns.</p>
     * 
     * <ul>
     * <li>When using flow control, if the transmission has been stopped because other serial port's buffer 
     * is getting full, this method may return 0, indicating that no data bytes were sent out of serial port.</li>
     * 
     * <li><p>If large amount of data need to be written, it is advisable to break it into chunks of data of 
     * size for example 2KB each.</p></li>
     * 
     * <li>Writing empty buffer i.e. zero length array is not allowed.</p></li>
     * 
     * <li><p>It should be noted that on Linux system reading from the terminal after a disconnect causes 
     * an end-of-file condition, and writing causes an EIO error to be returned. The terminal device must 
     * be closed and reopened to clear the condition.</p></li>
     *
     * <li>The delay parameter may be used where explicit flow control is not implemented and the firmware
     * is not fast enough to accommodate large and fast data sent to serial device. To control speed and amount 
     * of data sent to serial device either add delay between successive bytes or add delay after host has sent 
     * some particular number of bytes.</li>
     * </ul>
     *
     * @param handle handle of the opened port on which to write bytes.
     * @param buffer byte type buffer containing bytes to be written to port.
     * @param delay  time gap (in milliseconds) between transmitting two successive bytes.
     * @return number of bytes written.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if buffer is null or delay is negative.
     */
    public int writeBytes(long handle, final byte[] buffer, int delay) throws SerialComException {
        if((buffer == null) || (buffer.length == 0)) {
            throw new IllegalArgumentException("Argument buffer can not be null or empty !");
        }
        if(delay < 0) {
            throw new IllegalArgumentException("Argument delay can not be negative !");
        }

        int ret = mComPortJNIBridge.writeBytes(handle, buffer, delay);
        if(ret < 0) {
            throw new SerialComException("Could not write data to serial port. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Utility method to call writeBytes without delay between successive bytes.</p>
     * <p>The writeBytes(handle, buffer) method for class SerialComManager has the same effect
     * as: </p>
     * <p>writeBytes(handle, buffer, 0) </p>
     * 
     * @param handle handle of the opened port on which to write bytes.
     * @param buffer byte type buffer containing bytes to be written to port.
     * @return number of bytes written.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if buffer is null.
     */
    public int writeBytes(long handle, byte[] buffer) throws SerialComException {
        return writeBytes(handle, buffer, 0);
    }

    /**
     * <p>This method writes a single byte to the specified port. The data has been transmitted 
     * out of serial port when this method returns.</p>
     * 
     * @param handle handle of the opened port on which to write byte.
     * @param dataByte byte to be written to port.
     * @return number of bytes written.
     * @throws SerialComException if an I/O error occurs.
     */
    public int writeSingleByte(long handle, byte dataByte) throws SerialComException {
        int ret = mComPortJNIBridge.writeSingleByte(handle, dataByte);
        if(ret < 0) {
            /* extra check */
            throw new SerialComException("Could not write given byte to serial port. Please retry !");
        }
        return ret;
    }

    /**
     * <p>This method writes a string to the specified port. The library internally converts string 
     * to byte buffer. The data has been transmitted out of serial port when this method returns.</p>
     * 
     * @param handle handle of the opened port on which to write byte.
     * @param data the string to be send to port.
     * @param delay interval between two successive bytes while sending string.
     * @return true on success false otherwise.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if data is null or empty string.
     */
    public boolean writeString(long handle, String data, int delay) throws SerialComException {
        int ret = 0;
        if((data == null) || (data.length() == 0)) {
            throw new IllegalArgumentException("Argument data can not be null or an empty string !");
        }
        byte[] dataToBeSent = data.getBytes();
        ret = writeBytes(handle, dataToBeSent, delay);
        if(ret != dataToBeSent.length) {
            return false;
        }
        return true;
    }

    /**
     * <p>This method writes a string to the specified port. The library internally converts string to byte buffer. 
     * The data has been transmitted out of serial port when this method returns.</p>
     * 
     * @param handle handle of the opened port on which to write byte.
     * @param data the string to be send to port.
     * @param charset the character set into which given string will be encoded.
     * @param delay time gap between transmitting two successive bytes in this string.
     * @return true on success false otherwise.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if data is null or empty string.
     * @throws UnsupportedEncodingException if string encoding/decoding fails.
     */
    public boolean writeString(long handle, final String data, Charset charset, int delay) throws UnsupportedEncodingException, SerialComException {
        int ret = 0;
        if((data == null) || (data.length() == 0)) {
            throw new IllegalArgumentException("Argument data can not be null or an empty string !");
        }
        byte[] dataToBeSent = data.getBytes(charset);
        ret = writeBytes(handle, dataToBeSent, delay);
        if(ret != dataToBeSent.length) {
            return false;
        }
        return true;
    }

    /** 
     * <p>Different CPU and OS will have different endianness. It is therefore we handle the endianness 
     * conversion as per the requirement. If the given integer is in range −32,768 to 32,767, only two 
     * bytes will be needed. In such case we might like to send only 2 bytes to serial port. On the other 
     * hand application might be implementing some custom protocol so that the data must be 4 bytes 
     * (irrespective of its range) in order to be interpreted correctly by the receiver terminal. This method 
     * assumes that integer value can be represented by 32 or less number of bits. On x86_64 architecture, 
     * loss of precision will occur if the integer value is of more than 32 bit.</p>
     * 
     * <p>The data has been transmitted physically out of serial port when this method returns.</p>
     * 
     * <p>In java numbers are represented in 2's complement, so number 650 whose binary representation is 
     * 0000001010001010 is printed byte by byte, then will be printed as 1 and -118, because 10001010 in 2's 
     * complement is negative number.</p>
     * 
     * @param handle handle of the opened port on which to write byte.
     * @param data an integer number to be sent to port.
     * @param delay interval between two successive bytes .
     * @param endianness big or little endian sequence to be followed while sending bytes representing 
     *         this integer.
     * @param numOfBytes number of bytes this integer can be represented in.
     * @return number of bytes written.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if endianness or numOfBytes is null.
     */
    public int writeSingleInt(long handle, int data, int delay, ENDIAN endianness, NUMOFBYTES numOfBytes) throws SerialComException {
        byte[] buffer = null;

        if(endianness == null) {
            throw new IllegalArgumentException("Argument endianness can not be null !");
        }
        if(numOfBytes == null) {
            throw new IllegalArgumentException("Argument numOfBytes can not be null !");
        }

        if(numOfBytes.getValue() == 2) {             // conversion to two bytes data
            buffer = new byte[2];
            if(endianness.getValue() == 1) {         // Little endian
                buffer[1] = (byte) (data >>> 8);
                buffer[0] = (byte)  data;
            }else {                                 // big endian/default (java is big endian by default)
                buffer[1] = (byte)  data;
                buffer[0] = (byte) (data >>> 8);
            }
            return writeBytes(handle, buffer, delay);
        }else {                                     // conversion to four bytes data
            buffer = new byte[4];
            if(endianness.getValue() == 1) {        // Little endian
                buffer[3] = (byte) (data >>> 24);
                buffer[2] = (byte) (data >>> 16);
                buffer[1] = (byte) (data >>> 8);
                buffer[0] = (byte)  data;
            }else {                                 // big endian/default (java is big endian by default)
                buffer[3] = (byte)  data;
                buffer[2] = (byte) (data >>> 8);
                buffer[1] = (byte) (data >>> 16);
                buffer[0] = (byte) (data >>> 24);
            }
            return writeBytes(handle, buffer, delay);
        }
    }

    /** 
     * <p>This method send an array of integers on the specified port. The data has been transmitted 
     * out of serial port when this method returns.</p>
     * 
     * @param handle handle of the opened port on which to write byte.
     * @param buffer an array of integers to be sent to port.
     * @param delay interval between two successive bytes .
     * @param endianness big or little endian sequence to be followed while sending bytes representing 
     *         this integer.
     * @param numOfBytes number of bytes this integer can be represented in.
     * @return number of bytes written.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if endianness or numOfBytes is null.
     */
    public int writeIntArray(long handle, final int[] buffer, int delay, ENDIAN endianness, NUMOFBYTES numOfBytes) throws SerialComException {
        byte[] localBuf = null;

        if(endianness == null) {
            throw new IllegalArgumentException("Argument endianness can not be null !");
        }
        if(numOfBytes == null) {
            throw new IllegalArgumentException("Argument numOfBytes can not be null !");
        }

        if(numOfBytes.getValue() == 2) {
            localBuf = new byte[2 * buffer.length];
            if(endianness.getValue() == 1) {                 // little endian
                int a = 0;
                for(int b=0; b<buffer.length; b++) {
                    localBuf[a] = (byte)  buffer[b];
                    a++;
                    localBuf[a] = (byte) (buffer[b] >>> 8);
                    a++;
                }
            }else {                                         // big/default endian
                int c = 0;
                for(int d=0; d<buffer.length; d++) {
                    localBuf[c] = (byte) (buffer[d] >>> 8);
                    c++;
                    localBuf[c] = (byte)  buffer[d];
                    c++;
                }
            }
            return writeBytes(handle, localBuf, delay);
        }else {
            localBuf = new byte[4 * buffer.length];
            if(endianness.getValue() == 1) {                  // little endian
                int e = 0;
                for(int f=0; f<buffer.length; f++) {
                    localBuf[e] = (byte)  buffer[f];
                    e++;
                    localBuf[e] = (byte) (buffer[f] >>> 8);
                    e++;
                    localBuf[e] = (byte) (buffer[f] >>> 16);
                    e++;
                    localBuf[e] = (byte) (buffer[f] >>> 24);
                    e++;
                }
            }else {                                          // big/default endian
                int g = 0;
                for(int h=0; h<buffer.length; h++) {
                    localBuf[g] = (byte)  buffer[h];
                    g++;
                    localBuf[g] = (byte) (buffer[h] >>> 8);
                    g++;
                    localBuf[g] = (byte) (buffer[h] >>> 16);
                    g++;
                    localBuf[g] = (byte) (buffer[h] >>> 24);
                    g++;
                }
            }
            return writeBytes(handle, localBuf, delay);
        }
    }

    /**
     * <p>Writes the bytes from the given direct byte buffer using facilities of the underlying JVM 
     * and operating system. When this method returns data would have sent out of serial port physically.</p>
     * 
     * <p>Consider using this method when developing applications based on Bluetooth serial port profile 
     * or applications like printing document using printer.</p>
     * 
     * <p>This method does not modify the direct byte buffer attributes position, capacity, limit and mark. 
     * The application design is expected to take care of this as and when required in appropriate manner. 
     * Further, this method does not consume or modify the data in the given buffer.</p>
     * 
     * @param handle handle of the serial port on which to write bytes.
     * @param buffer direct byte buffer containing bytes to be written to port.
     * @param offset location from where to start sending data out of serial port.
     * @param length number of bytes from offset to sent to serial port.
     * @return number of bytes sent to serial port.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if buffer is null, or if position or limit is negative, 
     *          or if given buffer is not direct byte buffer, or if length > (buffer.capacity() - offset).
     */
    public int writeBytesDirect(long handle, ByteBuffer buffer, int offset, int length) throws SerialComException {
        if(buffer == null) {
            throw new IllegalArgumentException("Argument buffer can not be null !");
        }
        if((offset < 0) || (length < 0)) {
            throw new IllegalArgumentException("Argument offset or length can not be negative !");
        }
        if(!buffer.isDirect()) {
            throw new IllegalArgumentException("Given buffer is not a direct byte buffer !");
        }
        if(length > (buffer.capacity() - offset)) {
            throw new IllegalArgumentException("Index violation detected !");
        }
        if(length == 0) {
            return 0;
        }

        int ret = mComPortJNIBridge.writeBytesDirect(handle, buffer, offset, length);
        if(ret < 0) {
            throw new SerialComException("Could not write given data to serial port. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Write bytes from given buffer to the given handle in blocking mode.</p>
     * 
     * <ul>
     * <li>Writing empty buffer i.e. zero length array is not allowed.</li>
     * 
     * <li><p>Same context should not be used for both reading and writing.</p></li>
     * </ul>
     * 
     * @param handle handle of the opened port on which to write bytes.
     * @param buffer byte type buffer containing bytes to be written to port.
     * @param context context value obtained form call to createBlockingIOContext method.
     * @return number of bytes sent to serial port.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if buffer is null.
     */
    public int writeBytesBlocking(long handle, byte[] buffer, long context) throws SerialComException {
        if(buffer == null) {
            throw new IllegalArgumentException("Argument buffer can not be null !");
        }
        if(buffer.length == 0) {
            return 0;
        }

        int ret = mComPortJNIBridge.writeBytesBlocking(handle, buffer, context);
        if(ret < 0) {
            throw new SerialComException("Could not write data to serial port. Please retry !");
        }
        return ret;
    }

    /**
     * <p>Reads the bytes from the serial port into the given direct byte buffer using facilities of 
     * the underlying JVM and operating system.</p>
     * 
     * <p>This method does not modify the direct byte buffer attributes position, capacity, limit and mark. 
     * The application design is expected to take care of this as and when required in appropriate manner.</p>
     * 
     * @param handle handle of the serial port from which to read data bytes.
     * @param buffer direct byte buffer into which data bytes will be placed.
     * @param offset location in byte buffer from which to start saving data.
     * @param length number of bytes from offset to read in buffer.
     * @return number of bytes read from serial port, 0 if length is 0.
     * @throws SerialComException if an I/O error occurs.
     * @throws IllegalArgumentException if buffer is null, or if position or limit is negative, or if 
     *          given buffer is not direct byte buffer.
     */
    public int readBytesDirect(long handle, ByteBuffer buffer, int offset, int length) throws SerialComException {
        if(buffer == null) {
            throw new IllegalArgumentException("Argument buffer can not be null !");
        }
        if((offset < 0) || (length < 0)) {
            throw new IllegalArgumentException("Argument offset or length can not be negative !");
        }
        if(!buffer.isDirect()) {
            throw new IllegalArgumentException("Argument buffer is not direct byte buffer !");
        }
        if(length > (buffer.capacity() - offset)) {
            throw new IllegalArgumentException("Index violation detected !");
        }
        if(length == 0) {
            return 0;
        }

        int ret = mComPortJNIBridge.readBytesDirect(handle, buffer, offset, length);
        if(ret < 0) {
            throw new SerialComException("Could not read data from serial port and place into direct byte buffer. Please retry !");
        }
        return ret;
    }

    /** 
     * <p>Prepares a context that should be passed to readBytesBlocking, writeBytesBlocking,  
     * readBytes, unblockBlockingIOOperation and destroyBlockingIOContext methods.</p>
     * 
     * @return context value that should be passed to destroyBlockingIOContext, readBytesBlocking 
     *          and writeBytesBlocking methods.
     * @throws SerialComException if an I/O error occurs.
     */
    public long createBlockingIOContext() throws SerialComException {
        long ret = mComPortJNIBridge.createBlockingIOContext();
        if(ret < 0) {
            throw new SerialComException("Could not create blocking I/O context. Please retry !");
        }
        return ret;
    }

    /** 
     * <p>Unblocks any blocked operation if it exist. This causes closing of serial port possible 
     * gracefully and return the worker thread that called blocking read/write to return and proceed 
     * as per application design.</p>
     * 
     * @param context context obtained from call to createBlockingIOContext method for blocking 
     *         I/O operations.
     * @return true if blocked operation was unblocked successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean unblockBlockingIOOperation(long context) throws SerialComException {
        int ret = mComPortJNIBridge.unblockBlockingIOOperation(context);
        if(ret < 0) {
            throw new SerialComException("Could not unblock the blocked I/O operation. Please retry !");
        }
        return true;
    }

    /** 
     * <p>Destroys the context that was created by a call to createBlockingIOContext method for 
     * blocking I/O operations uses.</p>
     * 
     * @param context context obtained from call to createBlockingIOContext method for blocking 
     *         I/O operations.
     * @return true if the context gets destroyed successfully.
     * @throws SerialComException if an I/O error occurs.
     */
    public boolean destroyBlockingIOContext(long context) throws SerialComException {
        int ret = mComPortJNIBridge.destroyBlockingIOContext(context);
        if(ret < 0) {
            throw new SerialComException("Could not destroy blocking I/O context. Please retry !");
        }
        return true;
    }

    /** 
     * <p>Read specified number of bytes from given serial port and stay blocked till bytes arrive 
     * at serial port.</p>
     * <p>1. If data is read from serial port, array of bytes containing data is returned.</p>
     * <p>2. If there was no data in serial port to read, null is returned. Note that this case is 
     * not possible however for blocking read call.</p>
     * 
     * <ul>
     * <li>The number of bytes to read must be greater than or equal to 1 and less than or equal to 
     * 2048 (1 <= byteCount <= 2048). This method may return less than the requested number of bytes 
     * due to reasons like, there is less data in operating system buffer (serial port) or operating 
     * system returned less data which is also legal.</li>
     * 
     * <li><p>When no data at serial port has arrived and application wishes to unblock and return the control 
     * to the caller for example because application now wants to close the serial port, it should 
     * call unblockBlockingIOOperation() passing the same context to this method.</p></li>
     * 
     * <li>If using blocking context, it is advised to catch SerialComException exception and check if 
     * this contain message SerialComManager.EXP_UNBLOCKIO. This message indicates that  no error occurred 
     * while waiting/reading data from serial port but application has explicitly asked to return.</li>
     * </ul>
     * 
     * @param handle of the serial port from which to read bytes.
     * @param byteCount number of bytes to read from serial port.
     * @param context context obtained by a call to createBlockingIOContext method.
     * @return array of bytes read from port if read succeeds or null if read fails.
     * @throws SerialComException if an I/O error occurs or if byteCount is greater than 2048.
     */
    public byte[] readBytesBlocking(long handle, int byteCount, long context) throws SerialComException {
        if(byteCount > 2048) {
            throw new SerialComException("Number of bytes to read can not be greater than 2048 !");
        }
        byte[] buffer = null;
        buffer = mComPortJNIBridge.readBytesBlocking(handle, byteCount, context);

        if(buffer != null) {
            // data read from serial port, pass to application
            return buffer;
        }else {
            // not possible for blocking call, just keeping it
            return null;
        }
    }

    /** 
     * <p>Read specified number of data bytes from the given serial port.</p>
     * 
     * <ul>
     * <li>If data is read from serial port, array of bytes containing data is returned.</li>
     * 
     * <li><p>If there was no data at serial port to read, null is returned.</p></li>
     * 
     * <li>The number of bytes to read must be greater than or equal to 1 and less than or equal to 
     * 2048 (1 <= byteCount <= 2048). This method may return less than the requested number of bytes 
     * due to reasons like, there is less data in operating system buffer (serial port) or operating 
     * system returned less data which is also legal.</li>
     * </ul>
     * 
     * @param handle of the serial port from which to read bytes.
     * @param byteCount number of bytes to read from serial port.
     * @return array of bytes read from port or null.
     * @throws SerialComException if an I/O error occurs or if byteCount is greater than 2048.
     */
    public byte[] readBytes(long handle, int byteCount) throws SerialComException {
        if(byteCount > 2048) {
            throw new SerialComException("Number of bytes to read can not be greater than 2048 !");
        }
        byte[] buffer = mComPortJNIBridge.readBytes(handle, byteCount);
        if(buffer != null) {
            return buffer; // data read from serial port, pass it the to application
        }else {
            return null;  // serial port does not have any data
        }
    }

    /** 
     * <p>If user does not specify any count, library try to read DEFAULT_READBYTECOUNT (1024 bytes) 
     * bytes as default value.</p>
     * 
     * <p>It has same effect as readBytes(handle, 1024)</p>
     * 
     * @param handle of the port from which to read bytes.
     * @return array of bytes read from port or null.
     * @throws SerialComException if an I/O error occurs.
     */
    public byte[] readBytes(long handle) throws SerialComException {
        return readBytes(handle, DEFAULT_READBYTECOUNT);
    }

    /**
     * <p>Reads data from serial port and converts it into string.</p>
     * 
     * <p> It constructs a new string by decoding the specified array of bytes using the platform's 
     * default character set. The length of the new string is a function of the character set, and hence 
     * may not be equal to the length of the byte array read from serial port.</p>
     * 
     * @param handle of port from which to read bytes.
     * @param byteCount number of bytes to read from this port.
     * @return string constructed from data read from serial port or null.
     * @throws SerialComException if an I/O error occurs or if byteCount is greater than 2048.
     */
    public String readString(long handle, int byteCount) throws SerialComException {
        byte[] buffer = readBytes(handle, byteCount);
        if(buffer != null) {
            return new String(buffer);
        }
        return null;
    }

    /**
     * <p>This method reads data from serial port and converts it into string.</p>
     * 
     * <p> It Constructs a new string by decoding the specified array of bytes using the platform's 
     * default character set. The length of the new string is a function of the charset, and hence may not be 
     * equal to the length of the byte array read from serial port.</p>
     * 
     * <p>Note that the length of data bytes read using this method can not be greater than 
     * DEFAULT_READBYTECOUNT i.e. 1024.</p>
     * 
     * @param handle of the port from which to read bytes.
     * @return string constructed from data read from serial port or null.
     * @throws SerialComException if an I/O error occurs.
     */
    public String readString(long handle) throws SerialComException {
        return readString(handle, DEFAULT_READBYTECOUNT);
    }

    /** 
     * <p>This is a utility method to read a single byte from serial port.</p>
     * 
     * <p>Its effect is same as readBytes(handle, 1)</p>
     * 
     * @param handle of the port from which to read byte.
     * @return array of length 1 representing 1 byte data read from serial port or null.
     * @throws SerialComException if an I/O error occurs.
     */
    public byte[] readSingleByte(long handle) throws SerialComException {
        return readBytes(handle, 1);
    }

    /** 
     * <p>Reads data bytes from serial port into given buffer also providing info about framing, parity etc errors. 
     * This method may be used in application design which needs to poll serial port continuously for presence of data.</p>
     * 
     * <ul>
     * <li>This method can be used in blocking mode or non-blocking mode. If context is -1, this method will 
     * not block. For blocking behavior pass context value obtained from a call to createBlockingIOContext method. 
     * If a valid context is passed, this method will block until there is data to read from serial port.</li>
     * 
     * <li><p>When no data at serial port has arrived and application wishes to unblock and return the control 
     * to the caller for example because application now wants to close the serial port, it should 
     * call unblockBlockingIOOperation() passing the same context which is passed to this method.</p></li>
     * 
     * <li>If using blocking context, it is advised to catch SerialComException exception and check if 
     * this contain message SerialComManager.EXP_UNBLOCKIO. This message indicates that  no error occurred 
     * while waiting/reading data from serial port but application has explicitly asked to return.</li>
     * 
     * <li><p>To find if a framing or parity errors has happened while receiving data or not, call methods as 
     * appropriate on lineErr reference.</p></li>
     * </ul>
     * 
     * @param handle of the port from which to read data bytes.
     * @param buffer data byte buffer in which bytes from serial port will be saved.
     * @param offset index in given byte array at which first data byte will be placed.
     * @param length number of bytes to read into given buffer (0 <= length <= 2048).
     * @param context context obtained by call to createBlockingIOContext method for blocking behavior 
     *         or -1 for non-blocking behavior.
     * @param lineErr instance of class SerialComLineErrors that will carry line error information or null 
     *         if parity/framing errors should not be checked.
     *         
     * @return number of bytes read from serial port.
     * @throws SerialComException if an I/O error occurs.
     * @throws NullPointerException if <code>buffer</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException if offset is negative, length is negative, or length is 
     *          greater than buffer.length - offset.
     */
    public int readBytes(long handle, byte[] buffer, int offset, int length, long context, SerialComLineErrors lineErr) throws SerialComException {
        if(buffer == null) {
            throw new NullPointerException("Null data buffer passed to read operation !");
        }
        if((offset < 0) || (length < 0) || (length > (buffer.length - offset))) {
            throw new IndexOutOfBoundsException("Index violation detected in given byte array !");
        }
        if(length > 2048) {
            throw new IllegalArgumentException("Argument length can not be greater than 2048 !");
        }
        if(length == 0) {
            return 0;
        }

        int numberOfBytesRead = mComPortJNIBridge.readBytesP(handle, buffer, offset, length, context, lineErr);
        if(numberOfBytesRead < 0) {
            throw new SerialComException("Could not read data from serial port. Please retry !");
        }
        return numberOfBytesRead;
    }

    /**
     * <p>This method configures the rate at which communication will occur and the format of UART frame.
     * This method must be called before configureComPortControl method.</p>
     * 
     * <ul>
     * <li><p>[1] Most of the DTE/DCE (hardware) does not support different baud rates for transmission and reception 
     * and therefore this method takes only single value applicable to both transmission and reception.</p></li>
     * 
     * <li>All serial devices/drivers/operating systems does not support all the baud rates (maximum change in signal 
     * per second), stop bits, data bits etc. Please consult hardware and software manuals as appropriate.</li>
     * 
     * <li><p>If parity is enabled, the parity bit will be removed from UART frame before passing it to this library. 
     * All hardware/driver/operating systems does not support all parity. Please check your manual. Note that the datasheet 
     * of some uart devices does not mention that mark/space parity is supported, however they properly support it as 
     * mentioned in their register setting details. If a parity error is detected in received data, the error is not cleared 
     * until LSR/LCR registers are read by driver. So if parity detection is used application should enquire its status.</p></li>
     * 
     * <li>Some USB-UART devices supports non-standard baudrates. How to set these baudrate is device/driver and operating
     * system specific.</li>
     * </ul>
     * 
     * @param handle of opened port to which this configuration applies to.
     * @param dataBits number of data bits in one frame (refer DATABITS enum in SerialComManager class for this).
     * @param stopBits number of stop bits in one frame (refer STOPBITS enum in SerialComManager class for this).
     * @param parity of the frame (refer PARITY enum in SerialComManager class for this).
     * @param baudRate of the frame (refer BAUDRATE enum in SerialComManager class for this).
     * @param custBaud custom baudrate if the desired rate is not included in BAUDRATE enum.
     * @return true on success false otherwise.
     * @throws SerialComException if invalid handle is passed or an error occurs in configuring the port.
     * @throws IllegalArgumentException if dataBits or stopBits or parity or baudRate is null, or if custBaud is zero or negative.
     */
    public boolean configureComPortData(long handle, DATABITS dataBits, STOPBITS stopBits, PARITY parity, BAUDRATE baudRate, int custBaud) throws SerialComException {

        int baudRateTranslated = 0;
        int custBaudTranslated = 0;
        int baudRateGiven = 0;

        if(dataBits == null) {
            throw new IllegalArgumentException("Argument dataBits can not be null !");
        }
        if(stopBits == null) {
            throw new IllegalArgumentException("Argument stopBits can not be null !");
        }
        if(parity == null) {
            throw new IllegalArgumentException("Argument parity can not be null !");
        }
        if(baudRate == null) {
            throw new IllegalArgumentException("Argument baudRate can not be null !");
        }

        if(mPortHandleInfo.get(handle) == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        baudRateGiven = baudRate.getValue();
        if(baudRateGiven != 251) {
            baudRateTranslated = baudRateGiven;
            custBaudTranslated = 0;
        }else {
            // custom baud rate
            if(custBaud <= 0) {
                throw new IllegalArgumentException("Argument baudRate can not be negative or zero !");
            }
            baudRateTranslated = baudRateGiven;
            custBaudTranslated = custBaud;
        }

        int ret = mComPortJNIBridge.configureComPortData(handle, dataBits.getValue(), stopBits.getValue(), parity.getValue(), baudRateTranslated, custBaudTranslated);
        if(ret < 0) {
            /* extra check */
            throw new SerialComException("Could not configure the serial port. Please retry !");
        }

        return true;
    }

    /**
     * <p>This method configures the way data communication will be controlled between DTE and DCE. 
     * This specifies flow control and actions that will be taken when an error is encountered in 
     * communication.</p>
     * 
     * <ul>
     * <li>Some serial devices does not support some flow controls scheme. Please refer to their manuals.</li>
     * 
     * <li><p>It is advisable not to use same XON and XOFF character as opertaing system framework or driver 
     * may check for special characters one after the other and will process them. For example while processing 
     * data received at serial port, if driver sees XON it will instruct device to start transmission. But when 
     * moving further in the processing function, it checks for XOFF and sees that XOFF character has been received 
     * and therefore it will stop the transmission.</p></li>
     * </ul>
     * 
     * @param handle of opened port to which need to be configured.
     * @param flowctrl flow control, how data flow will be controlled (refer FLOWCONTROL enum for this).
     * @param xon character representing on condition if software flow control is used.
     * @param xoff character representing off condition if software flow control is used.
     * @param ParFraError true if parity and frame errors are to be checked false otherwise.
     * @param overFlowErr true if overflow error is to be detected false otherwise.
     * @return true on success false otherwise.
     * @throws SerialComException if invalid handle is passed or an error occurs in configuring the port.
     * @throws IllegalArgumentException if flowctrl is null.
     */
    public boolean configureComPortControl(long handle, FLOWCONTROL flowctrl, char xon, char xoff, boolean ParFraError, boolean overFlowErr) throws SerialComException {

        if(flowctrl == null) {
            throw new IllegalArgumentException("Argument flowctrl can not be null !");
        }

        if(mPortHandleInfo.get(handle) == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        int xonCh = (int) xon;
        int xoffCh = (int) xoff;

        int ret = mComPortJNIBridge.configureComPortControl(handle, flowctrl.getValue(), ((byte) xonCh), ((byte) xoffCh), ParFraError, overFlowErr);
        if(ret < 0) {
            /* extra check */
            throw new SerialComException("Could not configure serial port. Please retry !");
        }

        return true;
    }

    /**
     * <p>This method gives currently applicable settings associated with particular serial port.
     * The values are bit mask so that application can manipulate them to get required information.</p>
     * 
     * <p>For Unix-like OS the order is : c_iflag, c_oflag, c_cflag, c_lflag, c_line, c_cc[0], c_cc[1], c_cc[2], c_cc[3]
     * c_cc[4], c_cc[5], c_cc[6], c_cc[7], c_cc[8], c_cc[9], c_cc[10], c_cc[11], c_cc[12], c_cc[13], c_cc[14],
     * c_cc[15], c_cc[16], c_ispeed and c_ospeed.</p>
     * 
     * <p>For Windows OS the order is :DCBlength, BaudRate, fBinary, fParity, fOutxCtsFlow, fOutxDsrFlow, fDtrControl,
     * fDsrSensitivity, fTXContinueOnXoff, fOutX, fInX, fErrorChar, fNull, fRtsControl, fAbortOnError, fDummy2,
     * wReserved, XonLim, XoffLim, ByteSize, Parity, StopBits, XonChar, XoffChar, ErrorChar, StopBits, EvtChar,
     * wReserved1.</p>
     * 
     * @param handle of the opened port.
     * @return array of string giving configuration.
     * @throws SerialComException if invalid handle is passed or an error occurs while reading current settings.
     */
    public String[] getCurrentConfiguration(long handle) throws SerialComException {

        if(mPortHandleInfo.get(handle) == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        if(osType != SerialComPlatform.OS_WINDOWS) {
            // for unix-like os
            int[] config = mComPortJNIBridge.getCurrentConfigurationU(handle);
            String[] configuration = new String[config.length];
            if(config[0] < 0) {
                throw new SerialComException("Could not determine current configuration. Please retry !");
            }
            // if an error occurs, config[0] will contain error code, otherwise actual data
            for(int x=0; x<config.length; x++) {
                configuration[x] = "" + config[x];
            }
            return configuration;
        }else {
            // for windows os
            String[] configuration = mComPortJNIBridge.getCurrentConfigurationW(handle);
            return configuration;
        }
    }

    /**
     * <p>This method assert/de-assert RTS line of serial port. Set "true" for asserting signal, 
     * false otherwise. This changes the state of RTS line electrically.</p>
     * 
     * <ul>
     * <li>RTS and DTR lines can be asserted or de-asserted even when using no flow control on 
     * serial port.</li>
     * 
     * <li><p>The RS-232 standard defines the voltage levels that correspond to logical one and logical 
     * zero levels for the data transmission and the control signal lines. Valid signals are either 
     * in the range of +3 to +15 volts or the range −3 to −15 volts with respect to the ground/common 
     * pin; consequently, the range between −3 to +3 volts is not a valid RS-232 level.</p></li>
     * 
     * <li>In asserted condition, voltage at pin number 7 (RTS signal) will be greater than 3 volts. 
     * Voltage 5.0 volts was observed when using USB-UART converter : 
     * http://www.amazon.in/Bafo-USB-Serial-Converter-DB9/dp/B002SCRCDG.</li>
     * 
     * <li><p>On some hardware IC, signals may be active low and therefore for actual voltage datasheet 
     * should be consulted. Also please check if the driver supports setting RTS/DTR lines or not.</p></li>
     * </ul>
     * 
     * @param handle of the opened port.
     * @param enabled if true RTS will be asserted and vice-versa.
     * @return true on success.
     * @throws SerialComException if system is unable to complete requested operation.
     */
    public boolean setRTS(long handle, boolean enabled) throws SerialComException {
        int ret = mComPortJNIBridge.setRTS(handle, enabled);
        if(ret < 0) {
            throw new SerialComException("Could not set RTS line to desired state. Please retry !");
        }
        return true;
    }

    /**
     * <p>This method assert/de-assert DTR line of serial port. Set "true" for asserting signal, false 
     * otherwise. This changes the state of RTS line electrically.</p>
     * 
     * <ul>
     * <li>RTS and DTR lines can be asserted or de-asserted even when a serial port is configured as 
     * 'flow control none'.</li>
     * 
     * <li><p>It is possible to establish PPP connections to transmit binary data over a two (or more) wire 
     * interface with full handshaking and modem control signaling if the driver is configured for this. 
     * Refer application note from FTDI for details : AN232B-09 Using the Modem Emulation Mode in FTDI's 
     * VCP Driver.</p></li>
     * 
     * <li>If the DTR/DSR line is not used DTR can be connected to DSR locally like a loop back connection. 
     * The vendor written firmware treats change in DSR line as hardware interrupt and executes interrupt 
     * service routine. The application firmware can lower/raise the DTR line whenever needed.</li>
     * </ul>
     * 
     * @param handle of the opened port.
     * @param enabled if true DTR will be asserted and vice-versa.
     * @return true on success.
     * @throws SerialComException if system is unable to complete requested operation.
     */
    public boolean setDTR(long handle, boolean enabled) throws SerialComException {
        int ret = mComPortJNIBridge.setDTR(handle, enabled);
        if(ret < 0) {
            throw new SerialComException("Could not set DTR line to desired state. Please retry !");
        }
        return true;
    }

    /**
     * <p>This method associate a data looper with the given listener. This looper will keep delivering new data whenever
     * it is made available from native data collection and dispatching subsystem.
     * Note that listener will start receiving new data, even before this method returns.</p>
     * 
     * <p>Application (listener) should implement ISerialComDataListener and override onNewSerialDataAvailable method.</p>
     * 
     * <p>The SerialPundit can manage upto 1024 listeners corresponding to 1024 port handles. Application should not register 
     * data listener more than once for the same port otherwise it will lead to inconsistent state.</p>
     * <p>This method is thread safe.</p>
     * 
     * @param handle of the serial port for which given listener will listen for availability of data bytes.
     * @param dataListener instance of class which implements ISerialComDataListener interface.
     * @return true on success false otherwise.
     * @throws SerialComException if invalid handle passed, handle is null or data listener already exist for this handle.
     * @throws IllegalArgumentException if dataListener is null.
     */
    public boolean registerDataListener(long handle, final ISerialComDataListener dataListener) throws SerialComException {

        SerialComPortHandleInfo handleInfo = null;

        if(dataListener == null) {
            throw new IllegalArgumentException("Argument dataListener can not be null !");
        }

        synchronized(lockB) {
            handleInfo = mPortHandleInfo.get(handle);
            if(handleInfo == null) {
                throw new SerialComException("Given handle is alien to me !");
            }
            if(handleInfo.getDataListener() != null) {
                throw new SerialComException("Data listener already exist for this handle. A handle can have only one data listener !");
            }

            return mEventCompletionDispatcher.setUpDataLooper(handle, handleInfo, dataListener);
        }
    }

    /**
     * <p>This method destroys complete java and native looper subsystem associated with this particular data listener. This has no
     * effect on event looper subsystem. This method returns only after native thread has been terminated successfully.</p>
     * 
     * <p>This method is thread safe.</p>
     * 
     * @param handle handle of the serial port for which this data listener was registered.
     * @param dataListener instance of class which implemented ISerialComDataListener interface.
     * @return true on success false otherwise.
     * @throws SerialComException if null value is passed in dataListener field.
     * @throws IllegalArgumentException if dataListener is null.
     */
    public boolean unregisterDataListener(long handle, final ISerialComDataListener dataListener) throws SerialComException {

        SerialComPortHandleInfo handleInfo = null;

        if(dataListener == null) {
            throw new IllegalArgumentException("Argument dataListener can not be null !");
        }

        synchronized(lockB) {
            handleInfo = mPortHandleInfo.get(handle);
            if(handleInfo == null) {
                throw new SerialComException("Given handle is alien to me !");
            }
            if(mEventCompletionDispatcher.destroyDataLooper(handle, handleInfo, dataListener)) {
                return true;
            }
        }

        return false;
    }

    /**
     * <p>This method associate a event looper with the given listener. This looper will keep delivering new event whenever
     * it is made available from native event collection and dispatching subsystem.</p>
     * 
     * <p>Application (listener) should implement ISerialComEventListener and override onNewSerialEvent method.</p>
     * 
     * <p>By default all four events are dispatched to listener. However, application can mask events through setEventsMask()
     * method. In current implementation, native code sends all the events irrespective of mask and we actually filter
     * them in java layers, to decide whether this should be sent to application or not (as per the mask set by
     * setEventsMask() method).</p>
     * 
     * <p>Before calling this method, make sure that port has been configured for hardware flow control using configureComPortControl
     * method. Application should not register event listener more than once for the same port otherwise it will lead to inconsistent 
     * state.</p>
     * 
     * <p>This method is thread safe.</p>
     * 
     * @param handle of the port opened.
     * @param eventListener instance of class which implements ISerialComEventListener interface.
     * @return true on success false otherwise.
     * @throws SerialComException if invalid handle passed, handle is null or event listener already exist for this handle.
     * @throws IllegalArgumentException if eventListener is null. 
     */
    public boolean registerLineEventListener(long handle, final ISerialComEventListener eventListener) throws SerialComException {

        SerialComPortHandleInfo handleInfo = null;

        if(eventListener == null) {
            throw new IllegalArgumentException("Argument eventListener can not be null !");
        }

        synchronized(lockB) {
            handleInfo = mPortHandleInfo.get(handle);
            if(mPortHandleInfo.get(handle) == null) {
                throw new SerialComException("Given handle is alien to me !");
            }

            if(handleInfo.getEventListener() != null) {
                throw new SerialComException("Event listener already exist for this handle. A handle can have only one event listener !");
            }

            return mEventCompletionDispatcher.setUpEventLooper(handle, handleInfo, eventListener);
        }
    }

    /**
     * <p>This method destroys complete java and native looper subsystem associated with this particular event listener. This has no
     * effect on data looper subsystem.</p>
     * 
     * <p>This method is thread safe.</p>
     * 
     * @param handle handle for which this listener was registered.
     * @param eventListener instance of class which implemented ISerialComEventListener interface.
     * @return true on success false otherwise.
     * @throws SerialComException if an error occurs.
     * @throws IllegalArgumentException if eventListener is null.
     */
    public boolean unregisterLineEventListener(long handle, final ISerialComEventListener eventListener) throws SerialComException {

        SerialComPortHandleInfo handleInfo = null;

        if(eventListener == null) {
            throw new IllegalArgumentException("Argument eventListener can not be null !");
        }
        synchronized(lockB) {
            handleInfo = mPortHandleInfo.get(handle);
            if(handleInfo == null) {
                throw new SerialComException("Given handle is alien to me !");
            }
            if(mEventCompletionDispatcher.destroyEventLooper(handle, handleInfo, eventListener)) {
                return true;
            }
        }

        return false;
    }


    /**
     * <p>This pauses delivering events to application. The events kept accumulating in queue.</p>
     * 
     * @param eventListener instance of class which implemented ISerialComEventListener interface.
     * @return true on success false otherwise.
     * @throws SerialComException if null is passed for eventListener field.
     * @throws IllegalArgumentException if eventListener is null.
     */
    public boolean pauseListeningEvents(final ISerialComEventListener eventListener) throws SerialComException {
        if(eventListener == null) {
            throw new IllegalArgumentException("Argument eventListener can not be null !");

        }
        if(mEventCompletionDispatcher.pauseListeningEvents(eventListener)) {
            return true;
        }

        return false;
    }

    /**
     * <p>Resume delivering events kept in queue to application.</p>
     * 
     * @param eventListener is an instance of class which implements ISerialComEventListener.
     * @return true on success false otherwise.
     * @throws SerialComException if error occurs.
     * @throws IllegalArgumentException if eventListener is null.
     */
    public boolean resumeListeningEvents(final ISerialComEventListener eventListener) throws SerialComException {
        if(eventListener == null) {
            throw new IllegalArgumentException("Argument eventListener can not be null !");

        }
        if(mEventCompletionDispatcher.resumeListeningEvents(eventListener)) {
            return true;
        }

        return false;
    }

    /**
     * <p>This method gives more fine tune control to application for tuning performance and behavior of read
     * operations to leverage OS specific facility for read operation. The read operations can be optimized for
     * receiving for example high volume data speedily or low volume data but received in burst mode.</p>
     * 
     * <p>If more than one client has opened the same port, then all the clients will be affected by new 
     * settings. When this method is called application should make sure that previous read or write operation 
     * is not in progress.</p>
     * 
     * <p>Under multithreading scenarios, if thread is blocked on read method due to timeout configured using 
     * this method and other thread tries to close port, the read method may cause the the thread which called 
     * read methid not to return. It is therefore either timeout should be kept short or two threads must be 
     * synchronized etc.</p>
     * 
     * <p>[1] Time out for read call for unix like OS can be set using vtime. Use formula vtime = time out in 
     * milliseconds / 100. For example to wait for 100 milliseconds, vtime = 1 and vmin = 0.</p>
     * 
     * @param handle of the opened port.
     * @param vmin c_cc[VMIN] field of termios structure (applicable for unix like OS only).
     * @param vtime c_cc[VTIME] field of termios structure (10th of a second, applicable for unix like OS only).
     * @param rit ReadIntervalTimeout field of COMMTIMEOUTS structure (applicable for windows OS only).
     * @param rttm ReadTotalTimeoutMultiplier field of COMMTIMEOUTS structure (applicable for windows OS only).
     * @param rttc ReadTotalTimeoutConstant field of COMMTIMEOUTS structure (applicable for windows OS only).
     * @return true on success false otherwise.
     * @throws SerialComException if wrong handle is passed or operation can not be done successfully.
     * @throws IllegalArgumentException if invalid combination of arguments is passed.
     */
    public boolean fineTuneReadBehaviour(long handle, int vmin, int vtime, int rit, int rttm, int rttc) throws SerialComException {
        int ret = 0;		
        if(osType == SerialComPlatform.OS_WINDOWS) {
            if((rit < 0) || (rttm < 0) || (rttc < 0)) {
                throw new IllegalArgumentException("Argument(s) rit, rttm and rttc can not be neagative !");
            }
        }else {
            if((vmin == 0) && (vtime == 0)) {
                throw new IllegalArgumentException("Both vmin and vtime can not be zero !");
            }
            if((vmin < 0) || (vtime < 0)) {
                throw new IllegalArgumentException("The vmin and vtime can not be negative !");
            }
        }

        if(mPortHandleInfo.get(handle) == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        ret = mComPortJNIBridge.fineTuneRead(handle, vmin, vtime, rit, rttm, rttc);
        if(ret < 0) {
            throw new SerialComException("Could not set the given parameters. Please retry !");
        }
        return true;
    }

    /**
     * <p>Defines for which line events registered event listener will be called.</p>
     * 
     * <p>In future we may shift modifying mask in the native code itself, so as to prevent JNI transitions.
     * This filters what events should be sent to application. Note that, although we sent only those event
     * for which user has set mask, however native code send all the events to java layer as of now.</p>
     * 
     * @param eventListener instance of class which implemented ISerialComEventListener interface.
     * @return true on success false otherwise.
     * @throws SerialComException if invalid listener is passed.
     * @throws IllegalArgumentException if eventListener is null.
     */
    public boolean setEventsMask(final ISerialComEventListener eventListener, int newMask) throws SerialComException {

        SerialComPortHandleInfo handleInfo = null;
        SerialComLooper looper = null;
        ISerialComEventListener mEventListener = null;

        if(eventListener == null) {
            throw new IllegalArgumentException("Argument eventListener can not be null !");
        }

        for (Map.Entry<Long, SerialComPortHandleInfo> entry : mPortHandleInfo.entrySet()) {
            handleInfo = entry.getValue();
            if(handleInfo != null) {
                if(handleInfo.containsEventListener(eventListener)) {
                    looper = handleInfo.getLooper();
                    mEventListener = handleInfo.getEventListener();
                    break;
                }
            }
        }

        if(looper != null && mEventListener != null) {
            looper.setEventsMask(newMask);
            return true;
        }else {
            throw new SerialComException("This listener is not registered !");
        }
    }

    /**
     * <p>This method return currently applicable mask for events on serial port.</p>
     * 
     * @param eventListener instance of class which implemented ISerialComEventListener interface.
     * @return an integer containing bit fields representing mask.
     * @throws SerialComException if null or wrong listener is passed.
     * @throws IllegalArgumentException if eventListener is null.
     */
    public int getEventsMask(final ISerialComEventListener eventListener) throws SerialComException {

        SerialComPortHandleInfo handleInfo = null;
        SerialComLooper looper = null;
        ISerialComEventListener mEventListener = null;

        if(eventListener == null) {
            throw new IllegalArgumentException("Argument eventListener can not be null !");
        }

        for (Map.Entry<Long, SerialComPortHandleInfo> entry : mPortHandleInfo.entrySet()) {
            handleInfo = entry.getValue();
            if(handleInfo != null) {
                if(handleInfo.containsEventListener(eventListener)) {
                    looper = handleInfo.getLooper();
                    mEventListener = handleInfo.getEventListener();
                    break;
                }
            }
        }

        if(looper != null && mEventListener != null) {
            return looper.getEventsMask();
        }else {
            throw new SerialComException("This listener is not registered !");
        }
    }

    /**
     * <p>Discards data sent to port but not transmitted, or data received but not read. Some device/OS/driver might
     * not have support for this, but most of them may have.
     * If there is some data to be pending for transmission, it will be discarded and therefore no longer sent.
     * If the application wants to make sure that all data has been transmitted before discarding anything, it must
     * first flush data and then call this method.</p>
     * 
     * @param handle of the opened port.
     * @param clearRxBuffer if true receive buffer will be cleared otherwise will be left untouched.
     * @param clearTxBuffer if true transmit buffer will be cleared otherwise will be left untouched.
     * @return true on success.
     * @throws SerialComException if invalid handle is passed or operation can not be completed successfully.
     * @throws IllegalArgumentException if both purgeTxBuffer and purgeRxBuffer are false.
     */
    public boolean clearPortIOBuffers(long handle, boolean clearRxBuffer, boolean clearTxBuffer) throws SerialComException {

        if(mPortHandleInfo.get(handle) == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        if((clearRxBuffer == false) && (clearTxBuffer == false)) {
            throw new IllegalArgumentException("Both arguments clearRxBuffer and clearTxBuffer can not be false !");
        }

        int ret = mComPortJNIBridge.clearPortIOBuffers(handle, clearRxBuffer, clearTxBuffer);
        if(ret < 0) {
            throw new SerialComException("Could not clear serial port buffers. Please retry !");
        }

        return true;
    }

    /**
     * <p>Assert a break condition on the specified port for the duration expressed in milliseconds.
     * If the line is held in the logic low condition (space in UART jargon) for longer than a character 
     * time, this is a break condition that can be detected by the UART.</p>
     * 
     * <ul>
     * 
     * <li>A "break condition" occurs when the receiver input is at the "space" level for longer than some duration
     * of time, typically, for more than a character time. This is not necessarily an error, but appears to the
     * receiver as a character of all zero bits with a framing error. The term "break" derives from current loop
     * Signaling, which was the traditional signaling used for tele-typewriters. The "spacing" condition of a 
     * current loop line is indicated by no current flowing, and a very long period of no current flowing is often
     * caused by a break or other fault in the line.</li>
     * 
     * <li><p>Recognizing break condition on line is the responsibility of the UART IC, but if for some reason (such as a 
     * limited UART that does not implement this functionality) the UART fails to do so, reception of a break will 
     * manifest itself as a large number of framing errors. Also sometimes default drivers built into operating system 
     * may not provide break detection facility.</p></li>
     * 
     * 
     * <li>If the duration parameter is 0, it will result in fastest way this library can set and unset break 
     * condition.</li>
     * 
     * <li><p>All UART devices (or driver) may not support all break timings. For example CP2105 can set break for from 
     * 1 to 125 ms or for infinite time. Developers should consult data sheet to know device capabilities. Sometimes 
     * hardware may also report break as all 0 bits byte data.</p></li>
     * 
     * <li>In some application designs break signals can be used for packet synchronization. For example; presence 
     * of break indicate start of packet or end of packet. If the host processor is in sleep mode, some UART hardware 
     * may wake the host processor when they detect a break condition or start bit.</li>
     * 
     * <li><p>Dedicated ICs like ELM627 can be used to detect break condition and toggle GPIO lines which may in 
     * turn be connected to reset pin of micro-controller. Such schemes are used where the hardware can not detect break 
     * condition on line.</p></li>
     * </ul>
     * 
     * @param handle of the opened serial port.
     * @param duration the time in milliseconds for which break will be active.
     * @return true on success.
     * @throws SerialComException if invalid handle is passed or operation can not be successfully completed.
     * @throws IllegalArgumentException if duration is negative.
     */
    public boolean sendBreak(long handle, int duration) throws SerialComException {

        if(mPortHandleInfo.get(handle) == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        if(duration < 0) {
            throw new IllegalArgumentException("Argument duration can not be negative !");
        }

        int ret = mComPortJNIBridge.sendBreak(handle, duration);
        if(ret < 0) {
            throw new SerialComException("Could not set the break condition for given duration !");
        }

        return true;
    }

    /**
     * <p>This method gives the number of interrupts on serial line that have occurred. The interrupt count is in following
     * order in array beginning from index 0 and ending at index 11 :
     * CTS, DSR, RING, CARRIER DETECT, RECEIVER BUFFER, TRANSMIT BUFFER, FRAME ERROR, OVERRUN ERROR, PARITY ERROR,
     * BREAK AND BUFFER OVERRUN.</p>
     * 
     * <p>This is applicable for Linux onle. For other operating systems, this will return 0 for all the indexes.</p>
     * 
     * @param handle of the port opened on which interrupts might have occurred.
     * @return array of integers containing values corresponding to each interrupt source.
     * @throws SerialComException if invalid handle is passed or operation can not be performed successfully.
     */
    public int[] getInterruptCount(long handle) throws SerialComException {

        int[] interruptsCount;

        if(mPortHandleInfo.get(handle) == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        interruptsCount = mComPortJNIBridge.getInterruptCount(handle);
        if(interruptsCount == null) {
            throw new SerialComException("Unknown error occurred !");
        }
        return interruptsCount;
    }

    /**
     * <p>Gives status of serial port's control lines as supported by underlying operating system.
     * The sequence of status in returned array is :</p>
     * 
     * <p>Linux OS &nbsp;&nbsp;&nbsp;: CTS, DSR, DCD, RI, LOOP, RTS, DTR respectively.</p>
     * <p>MAC OS X &nbsp;&nbsp;:       CTS, DSR, DCD, RI, 0,    RTS, DTR respectively.</p>
     * <p>Windows OS :                 CTS, DSR, DCD, RI, 0,    0,   0   respectively.</p>
     * 
     * @param handle of the port whose status is to be read.
     * @return status of UART port control lines.
     * @throws SerialComException if invalid handle is passed or operation can not be completed successfully.
     */
    public int[] getLinesStatus(long handle) throws SerialComException {

        int[] status = null;

        if(mPortHandleInfo.get(handle) == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        status = mComPortJNIBridge.getLinesStatus(handle);
        if(status == null) {
            throw new SerialComException("Failed to get line status for the given handle. Please retry !");
        }

        return status;
    }

    /**
     * <p>Gives the name of the driver who is driving the given serial port. The use case scenarios are: </p>
     * 
     * <ul>
     * <li>Suppose exactly same two temperature sensors are connected to system via usb-uart converters. For 
     * example 1st sensor senses ambient temperature and is connected to computer via FT232. Second temperature 
     * sensor is connected via CP2102 to computer. Now when ft232 and cp2102 is plugged into computer we do not know 
     * which /dev/ttyUSBxx belong to which sensor (usb-uart). In this case use this API to find which device node 
     * belongs to which converter. For cp2102 driver will be cp210x where as for ft232 it will be ftdi_sio.</li>
     * 
     * <li><p>If exactly same usb-uart devices are used to connected two sensors then use findComPortFromUSBAttributes 
     * API in USB module giving unique serial numbers of ft232. This will tell which device node belongs to which 
     * sensor.</p></li>
     * 
     * <li>It can be used where legacy serial ports have to be found. For example in Windows suppose there is a permanent 
     * RS232 port on mother board and a FT232 USB-UART is also attached. Let us say FT232 is COM1 and the one on mother 
     * board is COM2. Now calling this method by passing COM1 and then COM2 can easily tell that COM2 is the port we need.</li>
     * 
     * <li><p>This can also be used where two end products based on same usb-uart converter are connected to the compueter 
     * and they use different drivers.</p></li>
     * </ul>
     * 
     * @param comPortName name only for windows (for ex; COM52), full path for unix-like os (for ex; /dev/ttyUSB0).
     * @return name of driver serving given serial port.
     * @throws SerialComException if operation can not be completed successfully.
     * @throws IllegalArgumentException if argument comPortName is null or is an empty string.
     */
    public String findDriverServingComPort(String comPortName) throws SerialComException {
        if(comPortName == null) {
            throw new IllegalArgumentException("Argument comPortName can not be null !");
        }
        if(comPortName.length() == 0) {
            throw new IllegalArgumentException("Argument comPortName can not be empty string !");
        }
        if(comPortName.length() > 256) {
            // linux have 256 as maximum length of file name.
            throw new IllegalArgumentException("Argument comPortName string can not be greater than 256 in length !");
        }

        String driverName = mComPortJNIBridge.findDriverServingComPort(comPortName);
        if(driverName == null) {
            throw new SerialComException("Failed to find driver serving the given serial port. Please retry !");
        }
        return driverName;
    }

    /**
     * <p>Gives the address and IRQ number associated with the given serial port. Applicable manily for serial port 
     * built into the mother board itself.</p>
     * 
     * @param handle handle of the opened serial port.
     * @return string containing address and irq number in hexadecimal represenation.
     * @throws SerialComException if operation can not be completed successfully.
     */
    public String findIRQnumberForComPort(long handle) throws SerialComException {
        String addressAndIRQ = mComPortJNIBridge.findIRQnumberForComPort(handle);
        if(addressAndIRQ == null) {
            throw new SerialComException("Failed to find IRQ and address for the given serial port. Please retry !");
        }
        return addressAndIRQ;
    }

    /**
     * <p>Get number of bytes in input and output port buffers used by operating system for instance tty buffers
     * in Unix like systems. Sequence of data in array is : Input buffer byte count, Output buffer byte count.</p>
     * 
     * <p>It should be noted that some chipset specially USB to UART converters might have FIFO buffers in chipset
     * itself. For example FT232R has internal buffers controlled by FIFO CONTROLLERS. For this reason this method
     * should be tested carefully if application is using USB-UART converters. This is driver and OS specific scenario.</p>
     * 
     * @param handle of the opened port for which counts need to be determined.
     * @return array containing number of bytes in input and output buffer.
     * @throws SerialComException if invalid handle is passed or operation can not be completed successfully.
     */
    public int[] getByteCountInPortIOBuffer(long handle) throws SerialComException {

        if(mPortHandleInfo.get(handle) == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        int[] numBytesInfo = mComPortJNIBridge.getByteCount(handle);
        if(numBytesInfo == null) {
            throw new SerialComException("Could not determine number of bytes in buffer. Please retry !");
        }

        return numBytesInfo;
    }

    /**
     * <p>This method gives the port name with which given handle is associated. If the given handle is
     * unknown to this library, null is returned. A serial port is known to this if it was opened using 
     * SerialComManager's openComPort() method.</p>
     * 
     * @param handle for which the port name is to be found.
     * @return port name corresponding to the given handle.
     * @throws SerialComException if invalid handle is passed.
     */
    public String getPortName(long handle) throws SerialComException {

        SerialComPortHandleInfo handleInfo = null;

        handleInfo = mPortHandleInfo.get(handle);
        if(handleInfo == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        return handleInfo.getOpenedPortName();
    }

    /**
     * <p>Send given file(s) using specified file transfer protocol.</p>
     * 
     * <p>For Xmodem trasnfer the element at 0th index of fileToSend array must represent a regular file. 
     * If the length of fileToSend array is more than one, only 1st file will be sent.</p>
     * 
     * <p>For Ymodem transfer, the fileToSend array should contain all the files to be transffered to 
     * receiver end where all the elements in fileToSend array represent regular files.</p>
     * 
     * <p>Xmodem protocol is widely used for flashing executable images in microcontroller via UART. For 
     * example the secondary bootloader in LPC2000 can update the user application code in on chip flash 
     * via UART with 1K XMODEM protocol. Xmodem protocol is also used for taling to network routers.</p>
     * 
     * <p>If you want to make user defined firmware, consider creating an IDE where end user can place widget, 
     * define layout etc. graphically and then when he clicks on save button a firmware hex file gets generated. 
     * This file is then flashed into end product as firmware.</p>
     * 
     * @param handle of the port on which file is to be sent.
     * @param fileToSend File instance representing file to be sent.
     * @param ftpProto file transfer protocol to use for communication over serial port.
     * @param ftpVariant variant of file transfer protocol to use.
     * @param textMode if true file will be sent as text file (ASCII mode), if false file will be sent as 
     *         binary file. The text file must contain only valid ASCII characters.
     * @param progressListener object of class which implements ISerialComXmodemProgress interface and is 
     *        interested in knowing how many blocks have been sent to file receiver till now. If progressListener 
     *        is null, update will not be delivered to application.
     * @param transferState if application wish to abort sending file at instant of time due to any reason, it can 
     *        call abortTransfer method on this object. If the application does not wishes to abort sending file 
     *        explicitly transferState can be null.
     * @return true on success, false if application instructed to abort.
     * @throws SerialComException if invalid handle is passed, if receiver sent abort command.
     * @throws SecurityException If a security manager exists and its SecurityManager.checkRead(java.lang.String) 
     *         method denies read access to the file.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or 
     *         for some other reason cannot be opened for reading.
     * @throws SerialComTimeOutException if timeout occurs as per file transfer protocol.
     * @throws IOException if error occurs while reading data from file to be sent.
     * @throws IllegalArgumentException if fileToSend or ftpProto or ftpVariant or ftpMode argument is null.
     */
    public boolean sendFile(long handle, final File[] fileToSend, FTPPROTO ftpProto, FTPVAR ftpVariant, 
            boolean textMode, ISerialComFTPProgress progressListener, SerialComFTPCMDAbort transferState) throws IOException {

        if((fileToSend == null) || (fileToSend.length == 0)) {
            throw new IllegalArgumentException("Argument fileToSend can not be null or of zero length !");
        }
        if(ftpProto == null) {
            throw new IllegalArgumentException("Argument ftpProto can not be null !");
        }
        if(ftpVariant == null) {
            throw new IllegalArgumentException("Argument ftpVariant can not be null !");
        }

        if(mPortHandleInfo.get(handle) == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        int protocol = ftpProto.getValue();
        int variant = ftpVariant.getValue();

        if(protocol == 1) {
            if(!fileToSend[0].isFile()) {
                throw new IllegalArgumentException("For Xmodem fileToSend[0] must be an existing regular file !");
            }
            if(progressListener != null) {
                if(!(progressListener instanceof ISerialComXmodemProgress)) {
                    throw new IllegalArgumentException("Implement ISerialComXmodemProgress for non-null progressListener !");
                }
            }
            switch(variant) {
            case 1:
                SerialComXModem xmodem = new SerialComXModem(this, handle, fileToSend[0], textMode, (ISerialComXmodemProgress)progressListener, transferState, osType);
                return xmodem.sendFileX();
            case 2:
                SerialComXModemCRC xmodemc = new SerialComXModemCRC(this, handle, fileToSend[0], textMode, (ISerialComXmodemProgress)progressListener, transferState, osType);
                return xmodemc.sendFileX();
            case 3:
                SerialComXModem1K xmodemk = new SerialComXModem1K(this, handle, fileToSend[0], textMode, (ISerialComXmodemProgress)progressListener, transferState, osType);
                return xmodemk.sendFileX();
            default:
                throw new IllegalArgumentException("This variant is not applicable for Xmodem transfer !");
            }
        }
        else if(protocol == 2) {
            switch(variant) {
            case 2:
                SerialComYModemCRC ymodemc = new SerialComYModemCRC(this, handle, fileToSend, textMode, (ISerialComYmodemProgress)progressListener, transferState, osType);
                return ymodemc.sendFileY();
            case 3:
                SerialComYModem1K ymodemk = new SerialComYModem1K(this, handle, fileToSend, textMode, (ISerialComYmodemProgress)progressListener, transferState, osType);
                return ymodemk.sendFileY();
            case 4:
                SerialComYModemG ymodemg = new SerialComYModemG(this, handle, fileToSend, textMode, (ISerialComYmodemProgress)progressListener, transferState, osType);
                return ymodemg.sendFileY();
            default:
                throw new IllegalArgumentException("This variant is not applicable for Ymodem transfer !");
            }
        }
        else if(protocol == 3) {
        }
        else {
        }

        return false;
    }

    /**
     * <p>Receives file using specified file transfer protocol.</p>
     * 
     * <p>For Xmodem transfer the fileToReceive must represent a regular file. For Ymodem transfer fileToReceive 
     * must represent a directory in which received files will be saved.</p>
     * 
     * @param handle of the serial port on which file is to be sent.
     * @param fileToReceive File instance representing file/folder to be sent.
     * @param ftpProto file transfer protocol (FTPPROTO_XXX) to use for communication over serial port.
     * @param ftpVariant variant of file transfer protocol (FTPVAR_XXX) to use.
     * @param textMode if true file will be received as text file (ASCII mode), if false file will be received 
     *        as binary file.
     * @param progressListener object of class which implements ISerialComXmodemProgress interface and is interested 
     *        in knowing how many blocks have been received from file sender till now. If progressListener is null, 
     *        update will not be delivered to application.
     * @param transferState if application wish to abort receiving file at instant of time due to any reason, it can 
     *        call abortTransfer method on this object. If the application does not wishes to abort receiving file 
     *        explicitly transferState can be null.
     * @return true on success, false if application instructed to abort.
     * @throws SerialComException if invalid handle is passed, if sender sent abort command.
     * @throws SecurityException If a security manager exists and its SecurityManager.checkRead(java.lang.String) method 
     *         denies read access to the file.
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some 
     *         other reason cannot be opened for reading.
     * @throws SerialComTimeOutException if timeout occurs as per file transfer protocol.
     * @throws IOException if error occurs while reading data from file to be sent.
     * @throws IllegalArgumentException if fileToReceive or ftpProto or ftpVariant or ftpMode argument is null.
     */
    public boolean receiveFile(long handle, final File fileToReceive, FTPPROTO ftpProto, FTPVAR ftpVariant, 
            boolean textMode, ISerialComFTPProgress progressListener, SerialComFTPCMDAbort transferState) throws IOException {

        if(fileToReceive == null) {
            throw new IllegalArgumentException("Argument fileToReceive can not be null !");
        }
        if(ftpProto == null) {
            throw new IllegalArgumentException("Argument ftpProto can not be null !");
        }
        if(ftpVariant == null) {
            throw new IllegalArgumentException("Argument ftpVariant can not be null !");
        }

        if(mPortHandleInfo.get(handle) == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        int protocol = ftpProto.getValue();
        int variant = ftpVariant.getValue();

        if(protocol == 1) {
            if(!fileToReceive.isFile()) {
                throw new IllegalArgumentException("The fileToReceive must be an existing regular file for Xmodem transfer !");
            }
            if(progressListener != null) {
                if(!(progressListener instanceof ISerialComXmodemProgress)) {
                    throw new IllegalArgumentException("Implement ISerialComXmodemProgress for non-null progressListener !");
                }
            }
            switch(variant) {
            case 1:
                SerialComXModem xmodem = new SerialComXModem(this, handle, fileToReceive, textMode, (ISerialComXmodemProgress)progressListener, transferState, osType);
                return xmodem.receiveFileX();
            case 2:
                SerialComXModemCRC xmodemc = new SerialComXModemCRC(this, handle, fileToReceive, textMode, (ISerialComXmodemProgress)progressListener, transferState, osType);
                return xmodemc.receiveFileX();
            case 3:
                SerialComXModem1K xmodemk = new SerialComXModem1K(this, handle, fileToReceive, textMode, (ISerialComXmodemProgress)progressListener, transferState, osType);
                return xmodemk.receiveFileX();
            default:
                throw new IllegalArgumentException("This variant is not applicable for Xmodem transfer !");
            }
        }
        else if(protocol == 2) {
            if(!fileToReceive.isDirectory() || !fileToReceive.canWrite()) {
                throw new IllegalArgumentException("The fileToReceive must be a writable directory for Ymodem transfer !");
            }            
            switch(variant) {
            case 2:
                SerialComYModemCRC ymodemc = new SerialComYModemCRC(this, handle, fileToReceive, textMode, (ISerialComYmodemProgress)progressListener, transferState, osType);
                return ymodemc.receiveFileY();
            case 3:
                SerialComYModem1K ymodemk = new SerialComYModem1K(this, handle, fileToReceive, textMode, (ISerialComYmodemProgress)progressListener, transferState, osType);
                return ymodemk.receiveFileY();
            case 4:
                SerialComYModemG ymodemg = new SerialComYModemG(this, handle, fileToReceive, textMode, (ISerialComYmodemProgress)progressListener, transferState, osType);
                return ymodemg.receiveFileY();
            default:
                throw new IllegalArgumentException("This variant is not applicable for Ymodem transfer !");
            }
        }
        else if(protocol == 3) {

        }
        else {
        }

        return false;
    }

    /**
     * <p>Factory method to create stream of type specified by streamType in blocking or non-blocking mode.</p>
     * 
     * <p>If streamType is SerialComManager.OutputStream, an instance of class SerialComOutByteStream is returned. 
     * If streamType is SerialComManager.InputStream, an instance of class SerialComInByteStream is returned.</p>
     * 
     * <p>If streamMode is SMODE.NONBLOCKING, read/write to serial port is non-blocking. If streamMode is SMODE.BLOCKING, 
     * read/write to serial port is blocking.</p>
     * 
     * @param streamType one of the values; SerialComManager.OutputStream or SerialComManager.InputStream.
     * @param handle handle of the opened serial port which this stream will wrap internally.
     * @param streamMode enum value SMODE.BLOCKING or SMODE.NONBLOCKING.
     * @return instance of stream (SerialComInByteStream/SerialComOutByteStream) as per given streamType.
     * @throws SerialComException if input stream already exist for this handle or invalid handle is passed.
     * @throws IllegalArgumentException if streamMode is null or invalid streamType is passed.
     */
    public ISerialIOStream getIOStreamInstance(int streamType, long handle, SMODE streamMode) throws SerialComException {

        if(streamMode == null) {
            throw new IllegalArgumentException("Argument streamMode can not be null !");
        }

        SerialComPortHandleInfo handleInfo = null;
        handleInfo = mPortHandleInfo.get(handle);
        if(handleInfo == null) {
            throw new SerialComException("Given handle is alien to me !");
        }

        switch(streamType) {

        case SerialComManager.InputStream :

            SerialComInByteStream scis = null;
            scis = handleInfo.getSerialComInByteStream();
            if(scis == null) {
                scis = new SerialComInByteStream(this, handleInfo, handle, streamMode);
                handleInfo.setSerialComInByteStream(scis);
            }else {
                // if 2nd attempt is made to create already existing input stream, throw exception
                throw new SerialComException("Input byte stream already exist for this handle !");
            }

            return scis;

        case SerialComManager.OutputStream :

            SerialComOutByteStream scos = handleInfo.getSerialComOutByteStream();
            if(scos == null) {
                scos = new SerialComOutByteStream(this, handleInfo, handle, streamMode);
                handleInfo.setSerialComOutByteStream(scos);
            }else {
                // if 2nd attempt is made to create already existing output stream, throw exception
                throw new SerialComException("Output byte stream already exist for this handle !");
            }

            return scos;

        default :
            throw new IllegalArgumentException("Argument streamType is invalid !");
        }
    }

    /**
     * <p>Gives an instance of the class which implements API defined by vendor in their propriety library.</p>
     * 
     * <p>For example; if vendorLibIdentifier is SerialComVendorLib.VLIB_FTDI_D2XX, an instance of SerialComFTDID2XX 
     * class is returned.</p>
     * 
     * @param vendorLibIdentifier one of the constant VLIB_XXXX_XXXX in SerialComVendorLib class.
     * @param libDirectory absolute directory path where vendor library is placed.
     * @param vlibName full name of the vendor library (for ex. libftd2xx.so.1.1.12).
     * @return an object of SerialComVendorLib class on which vendor specific API calls can be made otherwise null.
     * @throws IOException if java system properties can not be accessed, if invalid vendorLibIdentifier is passed. 
     *         if native libraries are not found or can not be loaded/linked, if libDirectory does not exist, or is not 
     *         a regular directory or is not writtable, If native library can not be initialized.
     * @throws IllegalArgumentException if vlibName is null or empty string.
     */
    public SerialComVendorLib getVendorLibFromFactory(int vendorLibIdentifier, String libDirectory, String vlibName) throws IOException {

        File baseDir = new File(libDirectory.trim());
        if(!baseDir.exists()) {
            throw new SerialComException("The directory " + libDirectory + " does not exist !");
        }
        if(!baseDir.isDirectory()) {
            throw new SerialComException("The location " + libDirectory + " is not a directory !");
        }
        if(!baseDir.canWrite()) {
            throw new SerialComException("The directory " + libDirectory + " is not writeable (permissions ??) !");
        }
        if(vlibName == null) {
            throw new IllegalArgumentException("Argument vlibName can not be null !");
        }
        if(vlibName.length() == 0) {
            throw new IllegalArgumentException("Argument vlibName can not be empty string !");
        }

        if(mSerialComVendorLib != null) {
            return mSerialComVendorLib.getVendorLibInstance(vendorLibIdentifier, baseDir, vlibName, cpuArch, osType, mSerialComSystemProperty);
        }

        mSerialComVendorLib = new SerialComVendorLib();
        return mSerialComVendorLib.getVendorLibInstance(vendorLibIdentifier, baseDir, vlibName, cpuArch, osType, mSerialComSystemProperty);
    }

    /**
     * <p>Allocate, initialize and return an instance of SerialComPortMapper class on whom APIs can 
     * be called to map or unmap a serial port alias.</p>
     * 
     * <p>This method will extract native library in directory as specified by directoryPath argument 
     * or default directory will be used if directoryPath is null. The native library loaded will be given 
     * name as specified by loadedLibName argument or default name will be used if loadedLibName is null.</p>
     * 
     * @param directoryPath absolute path of directory to be used for extraction.
     * @param loadedLibName library name without extension (do not append .so, .dll or .dylib etc.).
     * @return instance of SerialComPortMapper class on which various methods can be invoked.
     * @throws IOException if file operations on "/proc/cpuinfo" fails for Linux on ARM platform, if java system 
     *         properties can not be accessed, if file "/proc/cpuinfo" can not be found for Linux on ARM platform, 
     *         if native libraries are not found or can not be loaded/linked. If appropriate files/directories can 
     *         not be read or written, If native library can not be initialized.
     * @throws IllegalArgumentException if directoryPath is null, directoryPath is empty, loadedLibName is null 
     *         or empty.
     */
    public SerialComPortMapper getSerialComPortMapperInstance(String directoryPath, String loadedLibName) throws SerialComException {

        if(mSerialComPortMapperJNIBridge == null) {
            mSerialComPortMapperJNIBridge = new SerialComPortMapperJNIBridge();
            SerialComPortMapperJNIBridge.loadNativeLibrary(directoryPath, loadedLibName, mSerialComSystemProperty, osType, cpuArch, abiType);
        }

        return new SerialComPortMapper(mSerialComPortMapperJNIBridge);
    }

    /**
     * <p>Allocate, initialize and return an instance of SerialComDBRelease class on whom APIs can 
     * be called to release COM ports in use. This methed is applicable for Windows operating system only.</p>
     * 
     * <p>The Windows operating system maintains a database of all COM ports. This database is typically supports 
     * com port numbers from 1(COM1) to 256(COM256). This database can get exhausted for example if more than 256 
     * USB-UART devices are connected one after the other in system. This generally happens in factory testing 
     * environment. Using the APIs in SerialComDBRelease class, test application can manage the port assignment and 
     * its release programatically making production testing faster and less cumbersome.</p>
     * 
     * <p>This method will extract native library in directory as specified by directoryPath 
     * argument or default directory will be used if directoryPath is null. The native library 
     * loaded will be given name as specified by loadedLibName argument or default name will be 
     * used if loadedLibName is null.</p>
     * 
     * @param directoryPath absolute path of directory to be used for extraction.
     * @param loadedLibName library name without extension (do not append .so, .dll or .dylib etc.).
     * @return instance of SerialComDBRelease on which various methods can be invoked. 
     * @throws IOException if file operations on "/proc/cpuinfo" fails for Linux on ARM platform, if java system 
     *         properties can not be accessed, if file "/proc/cpuinfo" can not be found for Linux on ARM platform, 
     *         if native libraries are not found or can not be loaded/linked. If appropriate files/directories can 
     *         not be read or written, If native library can not be initialized.
     * @throws IllegalArgumentException if directoryPath is null, directoryPath is empty, loadedLibName is null 
     *         or empty.
     */
    public SerialComDBRelease getSerialComDBReleaseInstance(String directoryPath, String loadedLibName) throws IOException {

        if(mSerialComDBReleaseJNIBridge == null) {
            mSerialComDBReleaseJNIBridge = new SerialComDBReleaseJNIBridge();
            SerialComDBReleaseJNIBridge.loadNativeLibrary(directoryPath, loadedLibName, mSerialComSystemProperty, osType, cpuArch, abiType);
        }

        return new SerialComDBRelease(mSerialComDBReleaseJNIBridge);
    }

    /**
     * <p>Provides an instance of SerialComNullModem class for managing virtual serial device, null modem,
     * loop back and custom pinout connected virtual serial devices.</p>
     * 
     * @return an instance of SerialComNullModem class.
     * @throws IOException if any error occurs while handling null modem driver specific files.
     */
    public SerialComNullModem getSerialComNullModemInstance() throws IOException {

        if(mSerialComNullModem == null) {
            mSerialComNullModem = new SerialComNullModem(mComPortJNIBridge);
        }

        return mSerialComNullModem;
    }
}
