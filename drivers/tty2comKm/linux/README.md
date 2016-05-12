####Cloning
---------------------

To clone driver follow steps as given in clone-driver.sh script.

####Building
---------------------

- Prebuilt driver tty2comKm.ko (x86_64) is provided in repository.

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

- To load driver witth parameters for example to support 1000 virtual serial ports and create 1 loop back 
device and 1 null modem pair, load as shown below:

```
$ insmod ./tty2comKm.ko max_num_vtty_dev=1000 init_num_nm_pair=1 init_num_lb_dev=1
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

####Udev rules
---------------------
The udev rules are provided and gets installed automatically when install.sh is executed.

####Debugging
---------------------

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

- Module information from sysfs  
```
$ ls -l /sys/module/tty2comKm/
total 0
-r--r--r-- 1 root root 4096 Apr 13 17:55 coresize
drwxr-xr-x 2 root root    0 Apr 13 17:55 holders
-r--r--r-- 1 root root 4096 Apr 13 17:55 initsize
-r--r--r-- 1 root root 4096 Apr 13 17:55 initstate
drwxr-xr-x 2 root root    0 Apr 13 17:55 notes
-r--r--r-- 1 root root 4096 Apr 13 17:55 refcnt
drwxr-xr-x 2 root root    0 Apr 13 17:55 sections
-r--r--r-- 1 root root 4096 Apr 13 17:55 srcversion
-r--r--r-- 1 root root 4096 Apr 13 17:55 taint
--w------- 1 root root 4096 Apr 13 17:54 uevent
-r--r--r-- 1 root root 4096 Apr 13 17:55 version
```

