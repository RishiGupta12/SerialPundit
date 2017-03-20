#### Purpose
---------------------

Apart from the regular expected job, this driver :

- Provides access to GPIO pins or other pins via IOCTL not provided by default Linux kernel driver.

- Expose GPIO pin via sysfs interface files so that GPIO pin can be turned on/off from shell script.

- Provides support for software flow control (xon/xoff) using cp210x devices.

- Provides support for custom (non-standard/device specific) baudrate setting using correct divisor.

- Is a common driver for enter range of cp210x devices and final end product. This driver can be very quickly adapted to end product requirements.

- Identifies cp210x variant and configure parameters to leverage cp210x chip type specific features. For example; it identifies that the connected device is cp2105 and handle the baudrate related settings internally to support both SCI and ECI.

- Changes the behavior of DTR/RTS lines when port is opened. By default, the RTS and DTR lines gets asserted when a serial port is opened in Linux. Sometimes this may give false impression to remote device that cp210x is ready for communication while actually it may not be ready.

- Makes baudrate aliasing possible using standard baudrates for end product. Application set standard baudrate for example B4800 and this driver when sees this baudrate configures cp210x device to operate at 115200 speed.

- Works as sniffers in early development cycle of application and end product.

- Handles product/port specific quirks for seamless device/driver operations.

- Provides support for workarounds for known bugs in hardware/firmware/end product till they are fixed.

- Handles B0 baudrate to terminate connection and gracefully changing from B0 to any BXXXXXXX etc.


#### GPIO in CP210X
---------------------

* Each CP210X device supports different GPIO and configurations. For example in CP2105, GPIO pins are multiplexed with some of the UART interface pins. Therefore their use is mutually exclusive. The datasheet has to be referenced for the device of interest.

* CP210X devices provides 0 to maximum 16 GPIO pins. CP2102 does not contain any GPIO, while CP2108 provides 16 GPIO pins.
Each GPIO pin has a mask and value associated with it. When calling IOCTL routine, mask bit corresponding to the GPIO of interest need to be set and value bit corresponding to the GPIO of interest need to be set also.
   
   For example; to set GPIO.0 pin of CP2104 to high, specify mask and value of that bit as 1. The 0 to 15th bit is value of GPIO while 15th to 31st bit is corresponding bit mask.  
   
   scm.ioctlSetValue(handle, 0x8001, 0x00010001);  

* By default, GPIO pins in CP210X devices are controlled manually by host computer. However it is possible that a CP210x device can automatically control certain GPIO pin latches for a predetermined function. For example; pin GPIO.0 can be controlled automatically by CP210X to indicate transmission of data over UART interface.

* Before using GPIO pins, they may need to be configured (input, open-drain/push-pull output). This configuration may be one time programmable only. Consult datasheet and application notes from vendor.

#### Build / Install / Run
--------------------------

See operating system specific directory for instructions.

