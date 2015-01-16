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

Ans: For Linux use *file* command.
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
               
####Functional debugging
---

**Que: How to debug data loss or buffer under-flow/over-flow conditions ?**

Ans: - If the UART chipset in use supports on-chip flow control on both sides of the connection, 
  then the driver may not be working as expected or there may be some limitaions.
  - When using using XON/XOFF flow control without on-chip support, try disabling the FIFO.
  - Check that RTS-CTS pins are connected properly and they meet expected timings.

####Hardware trouble shooting
---
