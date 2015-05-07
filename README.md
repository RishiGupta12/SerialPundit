Serial communication manager (SCM)
-----------------------------------

![scm](images/scm.jpg "scm")

The 'serial communication manager (scm)' is a java library for communication over serial port. It supports RS-232 control signals handshaking, monitoring and has been ported to Linux, MAC, Solaris and Windows operating system. It is consistent, portable, efficient, reliable, testable, extensible, modifiable, scalable library.

##Features
- Notification whenever a serial port is added/removed from system
- Linux, Windows, Mac OSX operating systems support
- ARM platform support (Embedded SE Java)
- Access to EIA232 standard DTR, CD, CTS, RTS and DSR signals
- Hardware and software flow-control options
- Custom baud rate setting support
- Both 32 and 64 bit library support
- Concurrent event driven non-blocking I/O
- Both poll based and listener based data read supported
- Find what all serial style ports are present in system 
- Extensive error detection and handling for reliable operations
- Fully documented and tested both java and native code
- Support for fault tolerant and recoverable application design

##Getting started

The folder prebuilt contains ready-to-use jar file (scm-1.0.1.jar) that can be imported in any project and referenced right away.

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
	
		try {
			SerialComManager scm = new SerialComManager();
			
			long handle = scm.openComPort("/dev/ttyUSB1", true, true, false);
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

##Wiki, Java docs, Example usage, Build guide, Discussion, Trouble shooting

http://www.embeddedunveiled.com/

##Author, License, and Copyright
The 'serial communication manager (scm)' is designed, developed and maintained by Rishi gupta. The Linkdin profile of the author can be found here : http://in.linkedin.com/pub/rishi-gupta/20/9b8/a10

This library is licensed under the LGPL, See LICENSE AND COPYING for full license text.
