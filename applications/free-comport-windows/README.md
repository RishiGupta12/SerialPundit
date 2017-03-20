This application suggest a design through which testing can be automated or may reduce the overhead 
on tester for testing products which requires test to be run after the DUT (device under test) has 
been plugged into system.

#### Running this application
   
Connect the USB-UART device of interest in computer. 
   
#### What this application does and how it does

- As soon as DUT with given USB VID/PID is added/removed in system, tests cases are made to execute.
  When a particular number of devices for example 1000 has been tested, application will exit itself 
  automatically.
   
- Further, if testing device is like USB-UART cable, it also demonstrates releasing COM port numbers 
  assigned by Windows operating system automatically when the DUT testing has finished.

