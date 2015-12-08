
This application design fully automatic such that it will find device identified by 
USB VID and PID. It will then open the HID device and start receiving inout reports.

It does following things :

- find device node
- open hid device
- register for USB hotplug events
- register worker thread that will read input report from HID device and blocks if 
  there is no report
  
On application exit :

- unregister USB hot plug listener
- stop worker thread
- closes HID devices
