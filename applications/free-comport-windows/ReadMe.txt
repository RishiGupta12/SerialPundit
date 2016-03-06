
This application suggest a design through which testing can be automated or may reduce the overhead 
on tester for testing products which requires test to be run after the DUT has been plugged into system.

- As soon as DUT with given USB VID/PID is added/removed in system, tests cases are made to execute.
  When a particular number of devices for example 1000 has been tested, application will exit itself 
  automatically.
   
- Further, if testing device is like USB-UART cable, it also demonstrates releasing COM port numbers 
  assigned by Windows operating system automatically when the DUT testing has finished.
