Serial communication manager (SCM)
-----------------------------------

!["serial communication in java"](images/scm.jpg?raw=true "serial communication in java")

The 'serial communication manager (abbreviated as SCM)' is an easy to use java library for communication over serial port. It supports RS-232 control signals handshaking, monitoring and has been ported to Linux, MAC, and Windows operating system for both 32 bit and 64 bit. It is consistent, portable, efficient, reliable, testable, extensible, modifiable, scalable and feature rich library. It has been also ported to ARM platform (Embedded SE Java).

It is powerfully configurable for both poll and event driven application designs be it low volume low frequency data or high volume high frequency data.

It eliminates the complexities of serial port programming for different operating systems, allowing engineers to concentrate on their application logic, facilitating faster engineering and development, reducing development cost and time to market.

##Features

#####Serial over USB
- Notification on USB Hotplug, calls listener whenever a USB-UART IC is added/removed.
- Find COM port assigned to a USB device dynamically from USB-IF VID, PID and serial number.
- Find information about all USB devices connected to system.
- Custom baud rate setting and high baud rates (3 Mbps) settings supported.
- USB power related information reporting like selective suspend or auto suspend.

#####Legacy
- Access to EIA232 standard DTR, CD, CTS, RTS and DSR signals.
- Hardware and software flow-control options.
- Data and event listeners callbacks.

#####File transfer protocols
- X/Y/Z modem FTP API implemented with finite state machine.
- Progress listener for GUI applications.
- Both text and binary mode transfer can take place.
- Abort command support to cancel transfer at any time.
- Send and receive in files in parallel.

#####Vendor libraries integration
- Vendor libraries like D2XX from ftdi, SimpleIO from microchip, USBXpress from silicon labs etc supported.

#####Feature rich
- Java NIO direct byte buffer API for faster data transfer.
- Both polling based and event listener based data/event read supported.
- IOCTL calls for GPIO control and power management.
- Find which driver is driving a particular serial port.
- Completely port re-entrant, allowing it to be time-sliced.
- Extraction of shared libraries at user defined locations for isolated environment and security.
- Input and Output byte stream (blocking and non-blocking) for serial port communication.
- CRC and other utility class for common functions for quick application development.
- Optimized read and write methods for single byte, multiple byte or string sending/receiving.
- Application specific parameters tuning for performance with and without modification of drivers.

#####HID (human interface device)
- HID class API for communicating with composite USB devices like MCP2200, CP2110 etc.
- Dynamically find HID devices or get hotplug event notifications.

#####Cloud and IoT ready

- Easily integrate into existing application server software like Apache mina etc.

#####Build for performance and quality
- Functional, stress, stability, unit tested (Junit for continuous integration).
- Maven repository release for maven integration.
- Extensive error detection and handling for reliable operations.
- Fully Optimized for power, performance and memory.
- Support for fault tolerant and recoverable application design.
- Extensively documented both Java (online javadocs) and C code.
- Scripts for quickly building and testing project.

##Drivers and Services

#####Null modem emulator
- [Feature](drivers) rich null modem emulation driver supporting null modem, loopback, custom pinout and dynamic virtual serial port creation. Corresponding Java API provided to control virtual serial devices from Java application.

#####Usb-uart
- [Driver](drivers) for cp210x IC and products based on it with access to GPIO, IOCTL support, configuration scripts etc.

#####ComDBFree

- [ComDBFree](services) is a service for OEM/ODM to fully automate factory testing by managing COM ports assignment in windows dynamically from test application/setup itself.

##Getting started

The folder prebuilt-release in this repository contains ready-to-use jar file (scm-1.0.4.jar) that can be imported in any project and referenced right away. Simply add scm-1.0.4.jar in your project as an external library. 

To add a jar in Eclipse, right-click on the Project → Build Path → Configure Build Path. Under Libraries tab, click Add Jars or Add External JARs and give the scm-1.0.4.jar.

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

##API usage
[Several](applications) full applications demonstrating usage of features and API of this library.

##Support and discussion
[Google group](https://groups.google.com/d/forum/serial-communication-manager) for feature request, asking questions and raising bugs etc.

##Help the project grow [<img src="https://github.com/RishiGupta12/serial-communication-manager/blob/master/images/help.jpg">](https://www.paypal.com/cgi-bin/webscr?cmd=_xclick&business=gupt21%40gmail%2ecom&lc=IN&item_name=Serial%20Project&button_subtype=services&currency_code=USD&bn=PP%2dBuyNowBF%3abtn_buynowCC_LG%2egif%3aNonHosted)

- Suggest features and enhancements
- Report bugs and fixes
- Help with missing documentation
- Suggest more test cases
- Publish an article on your blog to educate others about this project
- Provide peer support on mailing lists, forums or newsgroups

##Author, License and Copyright
- The 'serial communication manager (SCM)' is designed, developed and maintained by Rishi gupta.              
  Linkdin profile : http://in.linkedin.com/pub/rishi-gupta/20/9b8/a10    
  Xing profile:
  
- This library is DUAL licensed. It is made available under the terms of the GNU Affero General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial license for commercial use of this library. Contact author for commercial license.

  See LICENSE file in repository for full license text for AGPL v3.0.

