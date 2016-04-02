It is a kernel mode null modem emulator driver providing full duplex communication and handshaking signals.

It creates virtual serial ports pair that appears same as the real once to the application software.

##Use cases
- Fully automate Junit testing and continuous integration
- Custom protocols development and debugging
- Serial port based application's scalability and performance testing
- Can be used as a medium for inter/intra-process communication
- Serial port communication sniffer or test sniffer application itself
- GPS coordinates and robotics emulator/simulator
- Application development when hardware is still not available
- Test/debug different serial port device emulators like modem and faxes etc
- Testing high level user space drivers
- Segregate hardware issues from software bugs quickly during product development
- Multiple producers/consumers sharing same channel application design development/testing
- Development cost reduction across team
- Add new functionality to existing application by inserting plugin between serial hardware and existing application   
  [hw device]--[hw comport]--[plugin software (process data)]--[virtual comport]--[Existing application]
- Write user space drivers for example multiplex several connections on single GSM Modem

##Features
- Emulate a plug and play device
- Create standard, loopback or custom pinout connected serial devices
- Create/destroy virtual serial ports dynamically (no reboot required)
- Ports can be created automatically at system boots
- Serial ports can be deleted even when opened by an application (useful in automated testing)
- Emulate all serial port settings and control signals
- Emulate none, software and hardware flow controls
- Create large number of serial ports
- Operating system specific serial port APIs and IOCTL supported
- Speed is directly proportional to your software/hardware configuration
- Virtual box and VMware virtual machine supported
- API to control virtual ports directly from your application

##Pins mapping

There are three connection configurations supported by this driver.

####Standard
- TX of local port is connected to RX of remote port and vice-versa
- DTR of the local port is connected to DSR and DCD of the remote port
- RTS of the local port is connected to CTS of the remote port

####Loopback

####Custom


##Demo application


##Build and Run
See instructions in operating system specific directory for instructions and uses in subdirectories of this directory.

