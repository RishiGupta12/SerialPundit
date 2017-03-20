#### Building
---------------------

- Build is done using make tool. Run build.sh shell script to build this driver.


#### Installing
---------------------

If you want to permanently install driver in system run install.sh script which will install driver
at suitable place and make it ready to be used with modprobe tool.


#### Loading/Running
---------------------

When cp210x device is inserted into system, default Linux kernel drivers will take control of the 
cp210x device.

Run load.sh script which will unload default driver and load this driver for cp210x the inserted 
device.


#### Debugging
---------------------

``` sh
$ dmesg
```
[ 2908.049762] usb 3-3: new full-speed USB device number 8 using xhci_hcd  
[ 2908.067860] usb 3-3: New USB device found, idVendor=10c4, idProduct=ea60  
[ 2908.067870] usb 3-3: New USB device strings: Mfr=1, Product=2, SerialNumber=3  
[ 2908.067876] usb 3-3: Product: CP2102 USB to UART Bridge Controller  
[ 2908.067880] usb 3-3: Manufacturer: Silicon Labs  
[ 2908.067884] usb 3-3: SerialNumber: 0001  
[ 2908.069042] sp_cp210x 3-3:1.0: CP210X USB Serial Device converter detected  
[ 2908.070747] usb 3-3: CP210X USB Serial Device converter now attached to ttyUSB0  
[ 2908.114399] usbcore: registered new interface driver cp210x  
[ 2908.114444] usbserial: USB Serial support registered for cp210x  

``` sh
$ lsmod | grep cp210x
```
sp_cp210x             13977  0 
usbserial              44971  1 sp_cp210x


``` sh
$ ls -l /sys/devices/pci0000:00/0000:00:14.0/usb3/3-3
```
total 0
drwxr-xr-x 6 root root     0 Feb  8 15:08 3-3:1.0  
-rw-r--r-- 1 root root  4096 Feb  8 15:09 authorized  
-rw-r--r-- 1 root root  4096 Feb  8 15:09 avoid_reset_quirk  
-r--r--r-- 1 root root  4096 Feb  8 15:08 bcdDevice  
-rw-r--r-- 1 root root  4096 Feb  8 15:09 bConfigurationValue  
-r--r--r-- 1 root root  4096 Feb  8 15:08 bDeviceClass  
-r--r--r-- 1 root root  4096 Feb  8 15:08 bDeviceProtocol  
-r--r--r-- 1 root root  4096 Feb  8 15:08 bDeviceSubClass  
-r--r--r-- 1 root root  4096 Feb  8 15:09 bmAttributes  
-r--r--r-- 1 root root  4096 Feb  8 15:09 bMaxPacketSize0  
-r--r--r-- 1 root root  4096 Feb  8 15:09 bMaxPower  
-r--r--r-- 1 root root  4096 Feb  8 15:08 bNumConfigurations  
-r--r--r-- 1 root root  4096 Feb  8 15:09 bNumInterfaces  
-r--r--r-- 1 root root  4096 Feb  8 15:08 busnum  
-r--r--r-- 1 root root  4096 Feb  8 15:09 configuration  
-r--r--r-- 1 root root 65553 Feb  8 15:08 descriptors  
-r--r--r-- 1 root root  4096 Feb  8 15:09 dev  
-r--r--r-- 1 root root  4096 Feb  8 15:08 devnum  
-r--r--r-- 1 root root  4096 Feb  8 15:09 devpath  
lrwxrwxrwx 1 root root     0 Feb  8 15:08 driver -> ../../../../../bus/usb/drivers/usb  
drwxr-xr-x 3 root root     0 Feb  8 15:17 ep_00  
-r--r--r-- 1 root root  4096 Feb  8 15:08 idProduct  
-r--r--r-- 1 root root  4096 Feb  8 15:08 idVendor  
-r--r--r-- 1 root root  4096 Feb  8 15:09 ltm_capable  
-r--r--r-- 1 root root  4096 Feb  8 15:08 manufacturer  
-r--r--r-- 1 root root  4096 Feb  8 15:08 maxchild  
lrwxrwxrwx 1 root root     0 Feb  8 15:09 port -> ../3-0:1.0/port3  
drwxr-xr-x 2 root root     0 Feb  8 15:17 power  
-r--r--r-- 1 root root  4096 Feb  8 15:08 product  
-r--r--r-- 1 root root  4096 Feb  8 15:09 quirks  
-r--r--r-- 1 root root  4096 Feb  8 15:08 removable  
--w------- 1 root root  4096 Feb  8 15:09 remove  
drwxr-xr-x 2 root root     0 Feb  8 17:16 **sp_cp210x_gpio**  
-r--r--r-- 1 root root  4096 Feb  8 15:08 serial  
-r--r--r-- 1 root root  4096 Feb  8 15:08 speed  
lrwxrwxrwx 1 root root     0 Feb  8 15:08 subsystem -> ../../../../../bus/usb  
-rw-r--r-- 1 root root  4096 Feb  8 15:08 uevent  
-r--r--r-- 1 root root  4096 Feb  8 15:09 urbnum  
-r--r--r-- 1 root root  4096 Feb  8 15:09 version  

``` sh
$ udevadm info --attribute-walk --name /dev/ttyUSB0
```
Udevadm info starts with the device specified by the devpath and then
walks up the chain of parent devices. It prints for every device
found, all possible attributes in the udev rules key format.
A rule to match, can be composed by the attributes of the device
and the attributes from one single parent device.  

  looking at device '/devices/pci0000:00/0000:00:14.0/usb3/3-3/3-3:1.0/ttyUSB0/tty/ttyUSB0':  
    KERNEL=="ttyUSB0"  
    SUBSYSTEM=="tty"  
    DRIVER==""  

  looking at parent device '/devices/pci0000:00/0000:00:14.0/usb3/3-3/3-3:1.0/ttyUSB0':  
    KERNELS=="ttyUSB0"  
    SUBSYSTEMS=="usb-serial"  
    DRIVERS=="sp_cp210x"  
    ATTRS{port_number}=="0"  

  looking at parent device '/devices/pci0000:00/0000:00:14.0/usb3/3-3/3-3:1.0':  
    KERNELS=="3-3:1.0"  
    SUBSYSTEMS=="usb"  
    DRIVERS=="sp_cp210x"  
    ATTRS{bInterfaceClass}=="ff"  
    ATTRS{bInterfaceSubClass}=="00"  
    ATTRS{bInterfaceProtocol}=="00"  
    ATTRS{bNumEndpoints}=="02"  
    ATTRS{supports_autosuspend}=="1"  
    ATTRS{bAlternateSetting}==" 0"  
    ATTRS{bInterfaceNumber}=="00"  
    ATTRS{interface}=="CP2102 USB to UART Bridge Controller"  
    
    
  looking at parent device '**/devices/pci0000:00/0000:00:14.0/usb3/3-3**':  
    KERNELS=="3-3"  
    SUBSYSTEMS=="usb"  
    DRIVERS=="usb"  
    ATTRS{bDeviceSubClass}=="00"  
    ATTRS{bDeviceProtocol}=="00"  
    ATTRS{devpath}=="3"  
    ATTRS{idVendor}=="10c4"  
    ATTRS{speed}=="12"  
    ATTRS{bNumInterfaces}==" 1"  
    ATTRS{bConfigurationValue}=="1"  
    ATTRS{bMaxPacketSize0}=="64"  
    ATTRS{busnum}=="3"  
    ATTRS{devnum}=="6"  
    ATTRS{configuration}==""  
    ATTRS{bMaxPower}=="100mA"  
    ATTRS{authorized}=="1"  
    ATTRS{bmAttributes}=="80"  
    ATTRS{bNumConfigurations}=="1"  
    ATTRS{maxchild}=="0"  
    ATTRS{bcdDevice}=="0100"  
    ATTRS{avoid_reset_quirk}=="0"  
    ATTRS{quirks}=="0x0"  
    ATTRS{serial}=="0001"  
    ATTRS{version}==" 1.10"  
    ATTRS{urbnum}=="16"  
    ATTRS{ltm_capable}=="no"  
    ATTRS{manufacturer}=="Silicon Labs"  
    ATTRS{removable}=="removable"  
    ATTRS{idProduct}=="ea60"  
    ATTRS{bDeviceClass}=="00"   
    ATTRS{product}=="CP2102 USB to UART Bridge Controller"  


  looking at parent device '/devices/pci0000:00/0000:00:14.0/usb3':  
    KERNELS=="usb3"  
    SUBSYSTEMS=="usb"  
    DRIVERS=="usb"  
    ATTRS{bDeviceSubClass}=="00"  
    ATTRS{bDeviceProtocol}=="01"  
    ATTRS{devpath}=="0"  
    ATTRS{idVendor}=="1d6b"  
    ATTRS{speed}=="480"  
    ATTRS{bNumInterfaces}==" 1"  
    ATTRS{bConfigurationValue}=="1"  
    ATTRS{bMaxPacketSize0}=="64"  
    ATTRS{authorized_default}=="1"  
    ATTRS{busnum}=="3"  
    ATTRS{devnum}=="1"  
    ATTRS{configuration}==""  
    ATTRS{bMaxPower}=="0mA"  
    ATTRS{authorized}=="1"  
    ATTRS{bmAttributes}=="e0"  
    ATTRS{bNumConfigurations}=="1"  
    ATTRS{maxchild}=="14"  
    ATTRS{bcdDevice}=="0311"  
    ATTRS{avoid_reset_quirk}=="0"  
    ATTRS{quirks}=="0x0"  
    ATTRS{serial}=="0000:00:14.0"  
    ATTRS{version}==" 2.00"  
    ATTRS{urbnum}=="138"  
    ATTRS{ltm_capable}=="no"  
    ATTRS{manufacturer}=="Linux 3.11.0-26-generic xhci_hcd"  
    ATTRS{removable}=="unknown"  
    ATTRS{idProduct}=="0002"  
    ATTRS{bDeviceClass}=="09"  
    ATTRS{product}=="xHCI Host Controller"  


  looking at parent device '/devices/pci0000:00/0000:00:14.0':  
    KERNELS=="0000:00:14.0"  
    SUBSYSTEMS=="pci"  
    DRIVERS=="xhci_hcd"  
    ATTRS{irq}=="44"  
    ATTRS{subsystem_vendor}=="0x17aa"  
    ATTRS{broken_parity_status}=="0"  
    ATTRS{class}=="0x0c0330"  
    ATTRS{consistent_dma_mask_bits}=="32"  
    ATTRS{dma_mask_bits}=="64"  
    ATTRS{local_cpus}=="00000000,00000000,00000000,00000000,00000000,00000000,00000000,000000ff"  
    ATTRS{device}=="0x8c31"  
    ATTRS{msi_bus}==""  
    ATTRS{local_cpulist}=="0-7"  
    ATTRS{vendor}=="0x8086"  
    ATTRS{subsystem_device}=="0x3978"  
    ATTRS{numa_node}=="-1"  
    ATTRS{d3cold_allowed}=="1"  


  looking at parent device '/devices/pci0000:00':  
    KERNELS=="pci0000:00"  
    SUBSYSTEMS==""  
    DRIVERS==""  

