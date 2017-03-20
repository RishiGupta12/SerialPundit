This example demonstrates how to read data from serial port and buffer it locally until a 
particular number of data bytes has been received from serial port.

!["serial communication in java"](output.jpg?raw=true "serial communication in java")

#### Running this application
   
Open and configure minicom/teraterm for 9600 8N1 settings and start it. Launch this application. 
Minicom/tertarem will show "test" as data received from this java program. Now type 10 or more 
characters in minicom/teraterm and they will appear in this Java program's console.
   
See the output.jpg to see output of this program.
   
#### What this application does and how it does

It sends 4 bytes ("test") to serial device (host => device) and then read data from serial port, 
buffering until serial device has sent 10 or more bytes (host <= device). When 10 or more bytes 
have been received it will print on console.
     
#### Going further
   
- There are many different versions of read methods provided by this library and an application 
can use the method that is best fit for application requirement. Other variant of read are :
     ```java
     Non-blocking
     readBytes(long handle)
     readBytes(long handle, int byteCount)
     readBytesDirect(long handle, java.nio.ByteBuffer buffer, int offset, int length)
     readSingleByte(long handle)
     readString(long handle)
     readString(long handle, int byteCount)
     
     Blocking
     readBytesBlocking(long handle, int byteCount, long context)
     
     Non-blocking/ Blocking
     readBytes(long handle, byte[] buffer, int offset, int length, long context)
     ``` 
- The purpose of this program is to give a simple example of getting started and do some basic 
communication with serial device. See other examples for developing full fledged applications.
     
