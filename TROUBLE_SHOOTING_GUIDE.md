This is a collection of FAQ-style troubleshooting tips.


####Run time debugging
---

**Que: How can we know that native library is getting loaded at runtime.**

Ans: Following error will occur if native library fails to load :

java.lang.NullPointerException
	at com.embeddedunveiled.serial.SerialComJNINativeInterface.loadNativeLibrary(SerialComJNINativeInterface.java:138)
	at com.embeddedunveiled.serial.SerialComJNINativeInterface.<clinit>(SerialComJNINativeInterface.java:37)
	at com.embeddedunveiled.serial.SerialComManager.<init>(SerialComManager.java:188)
	at example.Test1.main(Test1.java:16)
Exception in thread "main" java.lang.**UnsatisfiedLinkError**: com.embeddedunveiled.serial.SerialComJNINativeInterface.initNativeLib()I
	at com.embeddedunveiled.serial.SerialComJNINativeInterface.initNativeLib(Native Method)
	at com.embeddedunveiled.serial.SerialComJNINativeInterface.<init>(SerialComJNINativeInterface.java:45)
	at com.embeddedunveiled.serial.SerialComManager.<init>(SerialComManager.java:188)
	at example.Test1.main(Test1.java:16)
	


####Build time errors
---

**Que: How to verify that shared library is a 32 bit or 64 bit file.**

Ans: For Linux and MAC OS use *file* command.
   ```sh
   $ file linux_1.0.0_x86_64.so
   ```
This will give something like this :

....ELF 64-bit LSB shared object, **x86-64**, version 1 (SYSV), dynamically linked, BuildID[sha1]=0x679e009d839e9d13595f0590d10ac7a81dfe257f, not stripped

For Windows use *dumpbin* utility that comes with visual studio.
   ```sh
   C:\YOUR-PATH\VC\bin>dumpbin /headers D:\windows_1.0.0_x86_64.dll
   ```
This will give something like this :

FILE HEADER VALUES
            **8664 machine (x64)**
               7 number of sections
               
**Que: Eclipse crashes sometimes or a particular functionality does not work.**

Ans: See eclipse log file to find specific error <YOUR-WORKSPACE>/.metadata/.log
               

####Functional debugging
---

**Que: How to debug data loss or buffer under-flow/over-flow conditions ?**

Ans: - If the UART chipset in use supports on-chip flow control on both sides of the connection, 
  then the driver may not be working as expected or there may be some limitaions.
  - When using using XON/XOFF flow control without on-chip support, try disabling the FIFO.
  - Check that RTS-CTS pins are connected properly and they meet expected timings.
  
  

####Hardware trouble shooting
---

**Que: Sometimes I observe power related issues when using USB-UART bridge.**

Ans: Try to investigate issues like the one stated here.
     http://support.microsoft.com/kb/935892/en-us
     
**Que: How can I check the driver for USB-UART converter IC FT232R has been loaded successfully.**

Ans: Try running following command and you should see something like this :
   ```sh
   $ dmesg | grep -i ftdi
   ```
usbcore: registered new interface driver ftdi_sio
.....
usb 3-3: FTDI USB Serial Device converter now attached to ttyUSB0

**Que: When using USB hub sometimes only one port show up.**

Ans: This is not due to library, rather a USB HUB and OS specific issue. Try different USB hub or insert USB-UART converter slowly one after the other.

**Que: Same device with different COM port numbers is confusing to me**

Ans: This is Windows specific issue. The same device will often appear at different comport numbers when it is on different USB hub ports.
