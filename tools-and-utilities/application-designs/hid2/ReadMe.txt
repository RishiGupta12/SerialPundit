
This application design fully automatic such that it will find device identified by 
USB VID and PID. It will then open the HID device and start receiving input reports.

It also handle the scenario gracefully if USB HID device is removed from system suddenly.

On application entry :

	HID device is already connected;

	- Find its device node.
	- Open the HID device.
	- Register for USB hotplug events, so that application gets notified whenever USB 
	  device is removed from system.
	- Register input report listener, so that whenever input reports are received from
	  HID device listener will be invoked.
	- Whenever device is removed from system, close its handle and unregister input report
	  listener.
	  
	HID device is not connected;

	- Register for USB hotplug events.
	- Whenever HID device is connected, find device node of this device and open it
	  from the hotplug listener.
	- From within USB hotplug listener, register input report listener so that whenever 
	  input reports are received fromHID device listener will be invoked.
	- Whenever device is removed from system, close its handle and unregister input report
	  listener.
	  
On device removal :

	- Unregister input report listener.
	- Close HID device.
  
On application exit :

	- Unregister USB hot plug listener.
	- Unregister input report listener.
	- Close HID device.
