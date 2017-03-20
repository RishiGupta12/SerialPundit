This example demonstrates how to write an application that remains compatible with previous
product/firmware and also provides features of new product/firmware. It also demonstrates
how to invoke IOCTL operations using serialpundit and use writeString method with specified
character set.

#### Running this application
   
Connect USB device of interest in computer. Unload default drivers for CP2012 and then load 
custom driver for CP2102. Now run this program so that CP2102 uses our custom driver. 
   
See the output.jpg to see output of this program.
   
#### What this application does and how it does

When this application starts, it setup classes which are required to find firmware version of
the USB device of interest. It reads the firmware version (bcdDevice) and according to this
version it creates different object and starts worker thread.
   
Suppose bar code reader does not contain indicator LED to indicate that it is connected to USB 
port. After 6 months company adds an LED indicator and the new bar code reader toggles this LED
using GPIO (general purpose I/O) pins. Now the new application is written in such a way if old
bar code reader is connected to system, it will not access LED, however if new bar code reader
is connected to system it will use API provided by serialpundit to toggle LED.
     
#### Going further
- This program uses only firmware version to differentiate USB devices. Modern USB devices often
provides user accessible EEPROM for application use. The serialpundit contain API to read/write to 
such EEPROMs. Consider using these APIs in your application.
     
- Typically, default drivers provided by operating system may not contain code to access GPIO or
EEPROM. Custom or user space drivers need to be used in such cases. Serialpundit provide support
for both kernel and user space drivers along with direct use of vendor provided libraries like
d2xx or usbxpress etc.

