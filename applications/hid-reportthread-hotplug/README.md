This application example demonstrates how an application can detect USB HID device dynamically,
find its device node and start communication with it witjhout using input report listener.

!["serial communication in java"](output.jpg?raw=true "serial communication in java")

#### Running this application
   
- Connect MCP2200 and then launch this application. It will send command 0x80 to MCP2200 and will
read input report from MCP2200.
     
- Start this application first and then connect MCP2200. It will detect the MCP2200 automatically
and send command 0x80 to MCP2200. MCP2200 will send input report to host PC, which is read by this 
application. 

  See the output.jpg to see output of this program.
   
#### What this application does and how it does

On application entry :

  *If HID device is already connected :*
	
- Create and start USB hot plug event handler thread. This thread will insert an 'add'
event manually in queue. Due to this event data handler thread will be notified and 
that thread will proceed to serve his purpose.
- Create and start data handler thread which will send output report to HID device and 
read input report from HID device.
- When notified by USB hot plug thread, find device node of HID device and open it for 
communication.
- Whenever a USB HID device is removed from system, USB hot plug listener inserts 'remove' 
event in queue which makes USB Hot plug thread to come out of waiting state and inform
data handler thread to close device handle and enter into wait state to wait for device
addition into system again.
- Whenever a USB HID device is added into system, USB hot plug listener inserts 'add' event 
in queue which makes USB Hot plug thread to come out of waiting state and inform data
handler thread to find device node of desired HID device, open it for communication.
Once the device is opened, send command (output report) to HID device periodically
and then wait until response (input report) is received from device. Once the report
is received, print it on console. 
	  
  *If HID device is not connected :*
	
- The operation is same as for HID device already connected into system except that the
USB hot plug thread does not insert 'add' event in queue manually when it starts.
	  
#### Going further
   
- Consider where operations need to be atomic or requires different levels of granularity.
For example consider if thread need to be synchronized explicitly or implicitly.
     
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
  
