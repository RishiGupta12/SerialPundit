<image align="right" width="256" heigth="265" src="https://github.com/RishiGupta12/SerialPundit/blob/master/images/tty2com.png">

The tty2com is a null modem / loop back kernel mode driver providing full duplex communication and handshaking signals. It creates virtual serial ports that appears same as the real once to the application software.

Desktop application, Eclipse IDE plugin, command line interface, Java APIs and Python APIs are provided to manage virtual ports.

## Applications
- Fully automate Junit testing and continuous integration
- Serial communication protocols development, simulation and analysis
- Serial port based application's scalability and performance testing
- Can be used as a medium for inter/intra-process communication
- Serial port communication sniffer or test sniffer application itself
- GPS coordinates and robotics emulator/simulator
- Application development when hardware is still not available (test driven development)
- Test/debug different serial port device emulators like modem and faxes etc
- Testing high level user space drivers & corner case testing by injecting handcrafted data
- Segregate hardware issues from software bugs quickly during product development
- Multiple producers/consumers sharing same channel application design development/testing
- Development cost reduction across team
- Add new functionality to existing application by inserting plugin between serial hardware and existing application   
  [hw device]--[hw comport]--[plugin software (process data)]--[virtual comport]--[Existing application]
- Write user space drivers for ex; multiplex several virtual connections on single physical GSM Modem line
- Protocol converter engine software
- Provide serial port where most modern systems does not have physical RS232 ports
- Capture the output of guest OS running on virtual machine and re-direct it to a terminal program on host
- Share virtual COM ports data with disk files and named pipes
- Analyze and reverse-engineer serial protocols
- Cases where socat utility does not meet requirements for unix-like OS
- Cases where available physical serial ports do not meet the current requirements
- Product demo where data from hardware needs to be sent to the GUI application

## Features
- Create standard, loopback or custom pinout connected serial devices.
- Software, hardware and no flow controls emulation.
- Parity, frame, overrun, line break and ring indicator events emulation.
- Control signals (RTS,DTR,CTS,DCD,RI,LOOP,DSR) and all serial port settings.
- 5,6,7,8 data bits, 1,1.5,2 stop bits and 1 start bit configurations supported.
- Create/destroy virtual serial ports dynamically directly from your application without reboot.
- Operating system specific serial port APIs and IOCTL supported.
- Serial ports can be deleted even when left opened by an application (useful in automated testing).
- Plug and play device emulated.
- Ports can be created automatically at system boots.
- Create large number of serial ports.
- Speed is directly proportional to your software/hardware configuration.
- Virtual box and VMware virtual machine supported.
- Multithreaded environment supported.
- Mismatched line settings causes garbled data as in real life and appropriate error event generation.
- Leverages OS specific feature and technologies like WDM, WMI, power management, PnP etc.
- Access restriction to created virtual serial ports.
- API to control virtual ports directly from your application.
- Dynamically specify minor number of device nodes for Linux OS.
- Emulate faulty cable conditions.
- Emulate noises like electromagnetic interference, drop/add data char, modify data due to line transient etc.

## Desktop application

!["serial communication in java"](/drivers/tty2comKm/application/tty2com1.png?raw=true "serial port null modem emulation")

A GUI application is provided to manage virtual ports graphically. Virtual ports can also be managed from shell terminal.

## Command line interface
See instructions in operating system specific directory for CLI commands.

## Build and Run
See instructions in operating system specific directory for build scripts, udev rules, steps to install etc.

## Pins mapping

There are three connection configurations supported by this driver.

#### Standard nulll modem
```
 tty2com0            tty2com1
     TXD -------------- RXD
     RXD -------------- TXD
     DTR -------------- DSR,DCD
 DSR,DCD -------------- DTR
     RTS -------------- CTS
     CTS -------------- RTS
     GND -------------- GND
```
- TX of local port is connected to RX of remote port and vice-versa
- DTR of the local port is connected to DSR and DCD of the remote port
- RTS of the local port is connected to CTS of the remote port

#### Standard loop back
```
   tty2com0            
     RXD -----]
     TXD -----]
     
     DTR -----]
 DSR,DCD -----]
 
     RTS -----]
     CTS -----]
```
- TX of local port is connected to RX of local port and vice-versa
- DTR of the local port is connected to DSR and DCD of the local port
- RTS of the local port is connected to CTS of the local port

#### Custom

The local pins RTS, DTR, DCD, DSR and RI can be connected to local/remote pins as desired.

*Custom null modem :*   
```
 tty2com0            tty2com1
     TXD -------------- RXD
     RXD -------------- TXD
     RTS -------------- DSR,DCD
 DSR,DCD -------------- DTR
     CTS -------------- RTS
     GND -------------- GND
```

*Custom loop back :*   
```
   tty2com0            
     RXD -----]
     TXD -----]
     
     RTS -----]
 DSR,DCD -----]
 
     DTR -----]
     CTS -----]
```

You can create all the configurations as per the application requirements.
