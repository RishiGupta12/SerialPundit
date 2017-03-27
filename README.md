SerialPundit : Serial port communication and much more...
-----------------------------------

<image align="right" width="370" heigth="247" src="https://github.com/RishiGupta12/SerialPundit/blob/master/images/sp.jpg">

SerialPundit is a Software Developer Kit (SDK) for serial port and HID communication. It has been ported to Linux, MAC, Windows and embedded SE Linux platforms for both 64/32 bit architectures.

It is powerfully configurable for both poll and event driven application designs be it low volume low frequency data or high volume high frequency data.

It eliminates the complexities of serial port programming for different operating systems, allowing engineers to concentrate on their application logic, facilitating faster engineering and development, reducing development cost and time to market.

It is consistent, portable, efficient, testable, reliable, extensible and modifiable.

## USB module features
- USB Hotplug notification; calls listener whenever a USB-UART/HID device is added/removed.
- Find COM port assigned to a USB device dynamically from USB-IF VID, PID and serial number.
- Find information about all USB devices connected to system.
- Custom baud rate setting and high baud rates (3 Mbps) settings supported.
- USB power related information reporting like selective suspend or auto suspend.
- Adapt application behavior based on firmware identified on USB device.
- Statically find if a particular USB device is connected to system or not.
- List all USB-HID devices connected to system.

## Com port module features
* Communication
  * Access to EIA232 standard DTR, CD, CTS, RTS and DSR signals.
  * Hardware and software flow-control options.
  * Both polling based and event listener based data/event read supported.
  * Parity and framing errors reporting supported.
  * Break condition send and receive API.
  * ARM Primecell AMBA compliant UART supported.
  * Standard and custom baud rates support.

* File transfer protocols
  * X/Y/Z modem FTP API implemented with finite state machine.
  * Progress listener for GUI applications (number of blocks, percentage, file name etc.).
  * Both text and binary mode transfer can take place.
  * Abort command support to cancel transfer at any time by sender or receiver.
  * Send and receive in files in parallel in various format and multithreading handling.

* Vendor libraries integration
  * Vendor libraries like D2XX from ftdi, SimpleIO from microchip, USBXpress from silicon labs etc supported.

* Others
  * Application specific parameters tuning for performance with and without modification of drivers.
  * Java NIO direct byte buffer API for faster data transfer.
  * IOCTL calls for GPIO control and power management.
  * Find which driver is driving a particular serial port (adapt to system).
  * Completely port reentrant, allowing it to be time sliced.
  * Extraction of shared libraries at user defined locations for isolated environment and security.
  * Input and Output byte stream (blocking and non  *blocking) for serial port communication.
  * CRC and other utility class for common functions for quick application development.
  * Optimized read and write methods for single byte, multiple byte or string sending/receiving.
  * Fast set/unset break condition for DMX512 based devices to control stage lighting and effects.
  * OSGI budles can be created easily for integrating in existing OSGI based application.
  * Two lock concurrent queue algorithm based ring buffer for faster data/event processing.
  * Easily integrate into existing application server software like Apache mina for cloud and IoT etc.

## HID module features
- HID class API for communicating with USB-HID devices like MCP2200, CP2110 etc.
- Dynamically find HID devices or get hotplug event notifications.
- Input report listener for asynchronous operations.
- Feature report, input report and output report in raw format available.
- Find information like serial number, product name, manufacturer etc.

The project is :   

- Functional, stress, stability, unit tested (Junit for continuous integration).
- Maven repository release for maven integration.
- Extensive error detection and handling for reliable operations.
- Fully Optimized for power, performance and memory.
- Support for fault tolerant and recoverable application design.
- Extensively documented both Java (online javadocs) and C code.
- Scripts for quick builds, tests, installation etc. 

## Drivers and Services

- [Null modem emulator](drivers/tty2comKm); tty2comKm is a feature rich null modem emulation driver supporting null modem, loopback, custom pinout and dynamic virtual serial port creation. Corresponding Desktop application, Eclipse IDE plugin, command line interface and Java/Python APIs are provided to manage virtual ports.

- [CP210x driver](drivers/cp210x-silicon-labs); kernel mode driver for usb-serial cp210x IC and products based on it with access to GPIO pins, IOCTL operations support, configuration and installations scripts, udev rules etc.

## Getting started

The folder prebuilt-release in this repository contains ready-to-use jar file (sp-tty.jar) that can be imported in any project and referenced right away. Simply add sp-tty.jar in your project as an external library. 

To add a jar in Eclipse, right-click on the Project &#8594; Build Path &#8594; Configure Build Path. Under Libraries tab, click Add Jars or Add External JARs and give the sp-core.jar and sp-tty.jar.

```java
import com.serialpundit.serial.SerialComManager;
import com.serialpundit.serial.SerialComManager.BAUDRATE;
import com.serialpundit.serial.SerialComManager.DATABITS;
import com.serialpundit.serial.SerialComManager.FLOWCONTROL;
import com.serialpundit.serial.SerialComManager.PARITY;
import com.serialpundit.serial.SerialComManager.STOPBITS;

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

## Resources
- [Several applications](applications) demonstrating serialpundit API usage, application notes and design ideas etc.
- [Support and discussion group](https://groups.google.com/d/forum/serialpundit) for feature request, asking questions and raising bugs etc.
- [Tools and utilities](tools-and-utilities) various scripts, configuration files, tools and udev rules etc.
- [Documentaion](javadocs) updated javadocs for API references.
- [Video](https://www.youtube.com/watch?v=fYLQbelGunQ) showing UART signals captured using digital oscilloscope.


## Help the project grow [<img src="https://github.com/RishiGupta12/SerialPundit/blob/master/images/help.jpg">](https://www.paypal.com/cgi-bin/webscr?cmd=_xclick&business=gupt21@gmail.com&lc=IN&item_name=Serial Project&button_subtype=services&currency_code=USD&bn=PP-BuyNowBF:btn_buynowCC_LG.gif:NonHosted)

- Suggest features and enhancements
- Report bugs and fixes
- Help with missing documentation or improvements
- Suggest more test cases and scenarios
- Publish an article on your blog to educate others about this project
- Provide peer support on mailing lists, forums or newsgroups

## Author, License and Copyright
- SerialPundit is designed, developed and maintained by Rishi Gupta. He does Linux driver development, embedded systems design, firmware development, circuit designing, prototyping, board bring up etc.     
  Linkdin profile : http://in.linkedin.com/pub/rishi-gupta/20/9b8/a10    
  
- SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial license for commercial use of this library. Contact author for commercial license.

  See LICENSE file in repository for full license text for AGPL v3.0.
  
