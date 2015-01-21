Serial communication manager (SCM)
-----------------------------------

![scm](images/scm.jpg "scm")

The 'serial communication manager (scm)' is a java library designed and developed to exchange data on serial port. It supports RS-232 control signals handshaking, monitoring and has been ported to Linux, MAC, Solaris and Windows operating system. It is scalable, testable, efficient, reliable and small footprint library.

##Features
- Notification whenever a serial port is removed from system
- Linux, Windows, Mac OS, Solaris OS supported
- Both 32 and 64 bit library support
- Both poll based and listener based data read supported
- Concurrent event driven non-blocking IO
- Leverages OS specific facilities
- Find what all serial style ports are present in system reliably
- Extensive error handling and reliable operations support
- Fully documented both java and native code

##Getting started

The folder prebuilt contains ready-to-use jar file (scm.jar) that can be imported in any project and referenced right away.

##Examples usage
```java
package example;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

public class Example {
	public static void main(String[] args) {
	
		long handle = 0;
		SerialComManager scm = new SerialComManager();
		
		try {
			handle = scm.openComPort("/dev/ttyUSB1", true, true, false);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			scm.writeString(handle, "testing hello", 0) == true);
			String data = scm.readString(handle);
			System.out.println("data read is :" + data);
			scm.closeComPort(handle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
```
More examples could be found here : https://github.com/RishiGupta12/serial-com-manager/blob/master/example_usage.txt

##Java docs

For online browsing javadocs are here : 

For offline browsing they are part of repository : https://github.com/RishiGupta12/serial-com-manager/tree/master/javadoc

##Wiki, Help, Discussion
The wiki page for the project is maintained here https://code.google.com/p/serial-com-manager/w/list

Mailing list and discussion group is here https://groups.google.com/d/forum/serial-com-manager

##Build guide

Detailed build guide for all operating system is here :
https://github.com/RishiGupta12/serial-com-manager/blob/master/BUILD_GUIDE.md

##Programs to test library

For Linus use gtkterm, minicom, for Windows use teraterm and for MAC use. We usually test by making connections as shown below :

/dev/ttyUSB0             /dev/ttyUSB1
         RXD <---------> TXD
         TXD <---------> RXD
         DTR <---------> DSR,RNG
     DSR,RNG <---------> DTR
         CTS <---------> RTS
         RTS <---------> CTS
         GND <---------> GND

##Author, License, and Copyright
The 'serial communication manager (scm)' is designed, developed and maintained by Rishi gupta. The Linkdin profile of the author can be found here : http://in.linkedin.com/pub/rishi-gupta/20/9b8/a10

This library is licensed under the LGPL, See LICENSE AND COPYING for full license text.
