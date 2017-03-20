This example demonstrates how to read data from serial port using data listener and buffer 
it locally until a particular number of data bytes have been received from serial port.

!["serial communication in java"](output.jpg?raw=true "serial communication in java")

#### Running this application
   
Open and configure minicom/teraterm for 9600 8N1 settings and start it. Launch this 
application. Minicom/tertarem will show "ABCDE" as data received from this java program.
Now type 15 or more characters in minicom/teraterm and they will appear in this Java
program's console. 
   
See the output.jpg to see output of this program.
   
#### What this application does and how it does

It sends 5 byte CMD to serial device (host => device) and then read data from serial 
port, buffering until serial device has sent 15 or more bytes (host <= device). The 
host can easily parse this 15 bytes considering it as a frame and then take appropriate 
actions.

It uses two threads; one for listening USB hot plug events and another for data exchange
with serial port. It addresses following cases.

*Serial device already connected to system :*
- Create and start USB hot plug event handler thread. This thread will insert an 'add' 
event manually in queue. Due to this event data handler thread will be notified and that 
thread will proceed to serve his purpose.
- Register USB Hot plug listener.
- Find device node of USB CDC device and open it for communication.
- Register data listener, so that whenever data bytes from serial ports are received this 
listener will be invoked.
- Whenever a device is removed from system, USB hot plug listener inserts 'remove' event in queue, 
data listener is unregistered and serial device's handle is closed.
- Whenever a USB CDC device is added into system, USB hot plug listener inserts 'add' event in queue, 
find device node of this device, open it and register data listener for it.
   
*Serial device not connected to system :*
- The operation is same as for serial device already connected into system except that the USB hot plug 
thread does not insert 'add' event in queue manually when it starts.
     
#### Going further
   
- This program assumes that a fixed size frame (15 bytes) is sent from device to host. Application can 
use start and end specifier for example "$" represent start of data frame while "#" represent end of 
frame. The data listener thread than will buffer data until "#" is received from serial port. As soon 
as "#" is received frame received can be processed as per application requirement.
    
- This program uses common buffer for saving data received from serial port and then while processing it. 
Consider using two buffers or producer-consumer like design to decouple buffering and processing. If the 
serial device will not send data until it receives next command than common buffer may be used.
     
- When developing application that uses USB hotplug detection, consider all possible cases where user 
can unexpectedly plug or unplug device. Appropriate checks and corresponding handling strategy should 
be deployed to handle scenarios that may occur. For example; if the device is unplugged writing to 
serial port is likely to throw exception. Now because we never know when user can unplug we should add 
a check like as shown below.
  ```java
  if(isDevConnected.get() == true) {
     scm.writeBytes(comPortHandle, CMD);
  }
  ```
     
