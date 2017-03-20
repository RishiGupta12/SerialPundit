This example demonstrates a fully automatic application which will dynamically find 
serial device and start communicating with it.

!["serial communication in java"](output.jpg?raw=true "serial communication in java")

#### Running this application
   
Open and configure minicom/teraterm for 9600 8N1 settings and start it. Launch this 
application. Minicom/tertarem will show "ABCDE" as data received from this java program.
Now type 15 or more characters in minicom/teraterm and they will appear in this Java
program's console. 
   
See the output.jpg to see output of this program.
   
#### What this application does and how it does

It sends 2 byte CMD to serial device (host => device) and then read data from serial port,
buffering until serial device has sent 15 or more bytes (host <= device). The host can 
easily parse this 15 bytes considering it as a frame and then take appropriate actions.

It uses two threads; one for listening USB hot plug events and another for data exchange 
with serial port. It addresses following cases.

*Serial device already connected to system :*

- Create, start thread and register for USB hot plug events so that whenever serial device 
is removed, it will close the port and terminate another thread which was communicating with 
serial port.
   
  When the device is plugged into system again, this thread will dynamically find its device 
  node, open the port and start data communicator thread again.
   
*Serial device not connected to system :*

- Create, start thread and register for USB hot plug events so that whenever serial device 
is plugged into system, application dynamically find its device node and opens it for communication.
   
  Once the device is found, create and start another thread that will communicate data on serial port.
     
#### Going further
   
- There are many different versions of read/write methods provided by serialpundit and an 
application can use the method that is best fit for application requirement. Other variant 
of read/write are given below.

     ```java
     Non-blocking:
     
     readBytes(long handle)
     readBytes(long handle, int byteCount)
     readBytesDirect(long handle, java.nio.ByteBuffer buffer, int offset, int length)
     readSingleByte(long handle)
     readString(long handle)
     readString(long handle, int byteCount)
     
     writeBytes(long handle, byte[] buffer)
     writeBytes(long handle, byte[] buffer, int delay)
     writeBytesDirect(long handle, java.nio.ByteBuffer buffer, int offset, int length)
     writeIntArray(long handle, int[] buffer, int delay, SerialComManager.ENDIAN endianness, 
                   SerialComManager.NUMOFBYTES numOfBytes)
     writeSingleByte(long handle, byte dataByte)
     writeSingleInt(long handle, int data, int delay, SerialComManager.ENDIAN endianness, 
                   SerialComManager.NUMOFBYTES numOfBytes)
     writeString(long handle, java.lang.String data, java.nio.charset.Charset charset, int delay)
     writeString(long handle, java.lang.String data, int delay)
     
     Blocking :
     
     readBytesBlocking(long handle, int byteCount, long context)
     writeBytesBlocking(long handle, byte[] buffer, long context)
     
     Non-blocking/ Blocking :
     
     readBytes(long handle, byte[] buffer, int offset, int length, long context)
     ```
 	
  This design may be used for "send command and read response" type applications for example when 
  developing custom protocols etc.
     
- This program assumes that a fixed size frame (15 bytes) is sent from device to host. Application
can use start and end specifier for example "$" represent start of data frame while "#" represent
end of frame. The data listener thread than will buffer data until "#" is received from serial 
port. As soon as "#" is received frame received can be processed as per application requirement.
    
- This program uses common buffer for saving data received from serial port and then while processing
it. Consider using two buffers or producer-consumer like design to decouple buffering and processing.
If the serial device will not send data until it receives next command than common buffer may be used.
    
