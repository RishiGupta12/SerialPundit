This application demonstrates how to establish communication between a shell script and 
Java application to send/receive a file using Xmodem file transfer protocol. It uses screen 
utility (in detached session) in Linux.

!["serial communication in java"](output.jpg?raw=true "serial communication in java")

#### Running this application
- Copy scm-1.0.4.jar in xmodem-screen-script folder. Do not change the directory structure.
- Run the script as screen.sh giving ports and file names as shown below. 
  ```sh
  ./screen.sh RECEIVE_PORT RECEIVE_FILE SEND_PORT SEND_FILE
  ```
  See the output.jpg to see output of this program.
   
#### What this application does and how it does
- Extract and validate parameters supplied to screen.sh script.
- Compile Java source files and create app.jar representing Java application that will 
receive the file.
- Schedule this java application to be runned after 3 seconds.
- Launch screen in detached session which will launch sz utility to actually do the file transfer.
- Issue command to start sending file.
- Exit after 10 seconds.
     
#### Going further
- The shell script will self terminate after 10 seconds. This can be made interactive.

