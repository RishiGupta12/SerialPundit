This application example demonstrates how an application can detect HID device dynamically,
find its device node and start communication with it using input report listener.

!["serial communication in java"](output.jpg?raw=true "serial communication in java")

#### Running this application
   
- Connect MCP2200 and launch this application. It will send command 0x80 to MCP and will
read input report from MCP2200.
     
- Start this application first and than connect MCP2200. It will detect the MCP2200 automatically
and send command 0x80 to MCP2200. MCP2200 will send input report to host PC, which is 
read by this application. 
   
  See the output.jpg to see output of this program.
   
#### What this application does and how it does

On application entry :

*If HID device is already connected :*
	
- Create and start USB hot plug event handler thread. This thread will insert an 'add' 
event manually in queue. Due to this event data handler thread will be notified and 
that thread will proceed to serve his purpose.
- Register USB Hot plug listener.
- Find device node of HID device and open it for communication.
- Register input report listener, so that whenever input reports are received from 
HID device listener will be invoked.
- Whenever a device is removed from system, USB hot plug listener inserts 'remove' event
in queue, input report listener is unregistered and HID device handle is closed.
- Whenever a USB HID device is added into system, USB hot plug listener inserts 'add' event 
in queue, find device node of this device, open it and register input report listener
for it.
	  
*If HID device is not connected :*

- The operation is same as for HID device already connected into system except that the
USB hot plug thread does not insert 'add' event in queue manually when it starts.
     
#### Going further
- The onNewInputReportAvailable method is invoked by a dedicated Java worker thread whose
job is to read input report from HID device and deliver it registered input report
listener. Therefore application may do job that may take time from within onNewInputReportAvailable
method or buffer input report locally.
     
- If developing application that uses USB hotplug detection, consider all possible cases
where user can unexpectedly plug or unplug device. Appropriate checks and corresponding
handling strategy should be deployed to handle this. For example; if the device is unplugged
sending output report is likely to throw exception. Now because we never know when user 
can unplug we should add a check like as shown below.

  ```Java
  if(hidDevHandle != -1) {
     scrh.writeOutputReportR(hidDevHandle, (byte) -1, outputReportBuffer);
  }
  ```

