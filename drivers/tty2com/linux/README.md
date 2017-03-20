#### Cloning
---------------------

To clone driver follow steps as given in clone-driver.sh script.

#### Building
---------------------

- Build is done using make tool. Run build.sh shell script to build this driver.

#### Installing
---------------------

If you want to permanently install driver in system run install.sh script which will install driver
at suitable place and make it ready to be used with modprobe tool.

#### Unistalling
---------------------

Run uninstall.sh script to uninstall this driver from system.

#### Loading/Running
---------------------

- Run load.sh script which will load this driver with default parameters.

- To load driver with parameters for example to support 1000 virtual serial ports and create 1 loop back 
device and 1 null modem pair, load as shown below:
```
$ insmod ./tty2com.ko max_num_vtty_dev=1000 init_num_nm_pair=1 init_num_lb_dev=1
```
- To load the driver automatically at boot time execute install.sh script and then copy the tty2com.conf file in /etc/modules-load.d folder.
```sh
$ sudo cp ./tty2com.conf /etc/modules-load.d
```
Further if we want to pass parameters to driver at load time during system boot, copy the tty2comParam.conf file in /etc/modprobe.d folder.
```sh
$ sudo cp ./tty2comParam.conf /etc/modprobe.d
```

#### Create / destroy
---------------------

- Create standard null modem connection
```
$echo "gennm#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y" > /proc/sp_vmpscrdk
```

- Create standard loop back connection
```
$echo "genlb#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#x" > /proc/sp_vmpscrdk
```

- Delete a particular tty2comXX device
```
$echo "del#vdevX#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" > /proc/sp_vmpscrdk
```

- Delete all tty2comXX devices
```
$echo "del#xxxxx#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" > /proc/sp_vmpscrdk
```

#### Meta information
```sh
$ head -c 46 /proc/sp_vmpscrdk
```

#### Udev rules
---------------------
The udev rules are provided and gets installed automatically when shell script install.sh is executed. 
The file 99-tty2com.rules contains udev rules required by this driver.
```
ACTION=="add", SUBSYSTEM=="tty", KERNEL=="tty2com[0-9]*", MODE="0666", RUN+="/bin/chmod 0666 %S/%p/evt"
```

## Getting information

- Dynamic debugging
If the kernel is compiled with CONFIG_DYNAMIC_DEBUG=y and debug log level for printk is set, this driver will print extra 
debugging information at runtime. To set printk log level to debug and enable extra logs from this driver, run following 
command.
```sh
echo 8 > /proc/sys/kernel/printk
echo -n "module tty2com +p" > /sys/kernel/debug/dynamic_debug/control
```

- Information about running module
```
# modinfo tty2com.ko
filename:       /home/rishi/tty2com.ko
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
tty2com: Serial port null modem emulation driver v1.0
```

- Loaded modules in system  
```
$ lsmod | grep tty2com
tty2com              22833  0
```

