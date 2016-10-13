####Cloning
---------------------

To clone driver follow steps as given in clone-driver.sh script.

####Building
---------------------

- Build is done using make tool. Run build.sh shell script to build this driver.

####Installing
---------------------

If you want to permanently install driver in system run install.sh script which will install driver
at suitable place and make it ready to be used with modprobe tool.

####Unistalling
---------------------

Run uninstall.sh script to uninstall this driver from system.

####Loading/Running
---------------------

- Run load.sh script which will load this driver with default parameters.

- To load driver with parameters for example to support 1000 virtual serial ports and create 1 loop back 
device and 1 null modem pair, load as shown below:
```
$ insmod ./tty2comKm.ko max_num_vtty_dev=1000 init_num_nm_pair=1 init_num_lb_dev=1
```
- To load the driver automatically at boot time execute install.sh script and then copy the tty2comKm.conf file in /etc/modules-load.d folder.
```sh
$ sudo cp ./tty2comKm.conf /etc/modules-load.d
```
Further if we want to pass parameters to driver at load time during system boot, copy the tty2comKmParam.conf file in /etc/modprobe.d folder.
```sh
$ sudo cp ./tty2comKmParam.conf /etc/modprobe.d
```

####Create / destroy
---------------------

- Create standard null modem connection
```
$echo "gennm#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y" > /proc/scmtty_vadaptkm
```

- Create standard loop back connection
```
$echo "genlb#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#x" > /proc/scmtty_vadaptkm
```

- Delete a particular tty2comXX device
```
$echo "del#vdevX#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" > /proc/scmtty_vadaptkm
```

- Delete all tty2comXX devices
```
$echo "del#xxxxx#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" > /proc/scmtty_vadaptkm
```

####Meta information
```sh
$ head -c 32 /proc/scmtty_vadaptkm
```

####Udev rules
---------------------
The udev rules are provided and gets installed automatically when shell script install.sh is executed. 
The file 99-tty2comKm.rules contains udev rules required by this driver.
```
ACTION=="add", SUBSYSTEM=="tty", KERNEL=="tty2com[0-9]*", MODE="0666", RUN+="/bin/chmod 0666 %S/%p/evt"
```

##Getting information

- Dynamic debugging
If the kernel is compiled with CONFIG_DYNAMIC_DEBUG=y and debug log level for printk is set, this driver will print extra 
debugging information at runtime. To set printk log level to debug and enable extra logs from this driver, run following 
command.
```sh
echo 8 > /proc/sys/kernel/printk
echo -n "module tty2comKm +p" > /sys/kernel/debug/dynamic_debug/control
```

- Information about running module
```
# modinfo tty2comKm.ko
filename:       /home/rishi/tty2comKm.ko
version:        v1.0
license:        GPL v2
description:    Serial port null modem emulation driver (kernel mode)
author:         Rishi Gupta
srcversion:     99B3ABD56D2DE085A736F67
depends:        
vermagic:       4.4.0-34-generic SMP mod_unload modversions 
parm:           max_num_vtty_dev:Maximum number of virtual tty devices this driver can create. (ushort)
parm:           init_num_nm_pair:Number of standard null modem pairs to be created at load time. (ushort)
parm:           minor_begin:int
parm:           init_num_lb_dev:Number of standard loopback tty devices to be created at load time. (ushort)
```

- Kernel logs  
```
$ dmesg
tty2comKm: Serial port null modem emulation driver (kernel mode) v1.0
```

- Loaded modules in system  
```
$ lsmod | grep tty2comKm
tty2comKm              22833  0
```

