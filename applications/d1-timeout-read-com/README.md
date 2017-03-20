This example demonstrates how to read data from serial port and block until either data bytes are 
available at serial port or given timeout occurs.

#### Running this application
   
Open and configure minicom/teraterm for 9600 8N1 settings and start it. Launch this application. 
Minicom/tertarem will show "test" as data received from this java program. 

If you type anything on minicom/teraterm screen, it will print data. If you do not type anything 
read method will return after given timeout has expired.
   
#### What this application does and how it does

- Open and configure given serial port at 9600 8N1 settings.
- Configure timeout values.
- Call read method which will return if there is data immediately or block until either data comes 
or given timeout expires.
- Once data is recieved close the serial port.

#### Going further
   
- There are many different versions of read methods provided by serialpundit and an application 
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
     
