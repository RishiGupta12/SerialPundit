####Run time debugging
---

**Que: How can we know that native library is getting loaded at runtime.**

Ans: Following error will occur if native library fails to load :

java.lang.NullPointerException
	at com.embeddedunveiled.serial.SerialComJNINativeInterface.loadNativeLibrary(SerialComJNINativeInterface.java:138)
	at com.embeddedunveiled.serial.SerialComJNINativeInterface.<clinit>(SerialComJNINativeInterface.java:37)
	at com.embeddedunveiled.serial.SerialComManager.<init>(SerialComManager.java:188)
	at example.Test1.main(Test1.java:16)
Exception in thread "main" java.lang.UnsatisfiedLinkError: com.embeddedunveiled.serial.SerialComJNINativeInterface.initNativeLib()I
	at com.embeddedunveiled.serial.SerialComJNINativeInterface.initNativeLib(Native Method)
	at com.embeddedunveiled.serial.SerialComJNINativeInterface.<init>(SerialComJNINativeInterface.java:45)
	at com.embeddedunveiled.serial.SerialComManager.<init>(SerialComManager.java:188)
	at example.Test1.main(Test1.java:16)

####Build time errors
---

####Functional debugging
---

# Data loss or buffer under-flow/over-flow conditions :

- If the UART chipset in use supports on-chip flow control on both sides of the connection, 
  then the driver may not be working as expected or there may be some limitaions.
  
- When using using XON/XOFF flow control without on-chip support, try disabling the FIFO.

- Check that RTS-CTS pins are connected properly and they meet expected timings.

####Hardware trouble shooting
---
