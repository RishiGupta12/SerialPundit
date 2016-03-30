It is a kernel mode null modem emulator driver providing full duplex communication and handshaking signals.

It creates virtual serial ports pair that appears same as real once to the underlying operating system.

##Use cases
- Fully automate Junit testing and continuous integration
- Custom protocols development and debugging
- Serial port based application's scalability and performance testing
- Can be used as a medium for inter/intra-process communication
- Serial port communication sniffer or test sniffer application itself
- GPS coordinates emulator/simulator
- Application development when hardware is still not available
- Test/debug different serial port device emulators like modem and faxes etc
- Testing high level user space drivers
- Segregate hardware issues from software bugs quickly during product development
- Multiple producers/consumers sharing same channel application design development/testing
- Development cost reduction across team

##Features
- Emulate plug and play device
- Create standard, loopback or custom connected serial devices
- Ports can be created automatically at system boots
- Serial ports can be deleted even when opened by an application
- Emulate all serial port settings and control signals
- Emulate none, software and hardware flow controls
- Create large number of serial ports
- Supports standard, loopback and custom connections
- Operating system specific serial port APIs and IOCTL supported
- Speed is directly proportional to your software/hardware configuration

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
See instructions in operating system specific directory for instructions and uses in subdirectories off this directory.

