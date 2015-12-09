
This application design fully automatic such that it will find device identified by 
USB VID and PID. It will then open the HID device and start receiving input reports.

It also handle the scenario gracefully if USB HID device is removed from system suddenly.

On application entry :

	HID device is already connected;

	- Find its device node.
	- Open the HID device.
	- Register for USB hotplug events, so that application gets notified whenever USB 
	  device is removed from system.
	- Register worker thread that will read input report from HID device and blocks if 
	  there is no report to read.
	- Whenever device is removed from system, close its handle and stop worker thread 
	  from within USB hotplug events listener callback.
	  
	HID device is not connected;

	- Register for USB hotplug events.
	- Whenever HID device is connected, find device node of this device and open it
	  from the hotplug listener.
	- From within USB hotplug listener, then create worker thread and start to read 
	  input reports.
	- Whenever device is removed from system, close its handle and stop worker thread 
	  from within USB hotplug events listener callback.
  
On application exit :

	- Unregister USB hot plug listener
	- Stop worker thread
	- Closes HID devices
