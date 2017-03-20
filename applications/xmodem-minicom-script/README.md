This application demonstrates how to establish communication between a shell script and 
Java application to send/receive a file using Xmodem file transfer protocol. It uses minicom, xdotool, 
lrzsz etc tools.

!["serial communication in java"](output.jpg?raw=true "serial communication in java")

#### Running this application
- Copy scm-1.0.4.jar in xmodem-minicom-script folder. Do not change the directory structure.
- Run the script as minicom.sh giving ports and file names as shown below. Transfer will occur 
at 9600 baudrate 8N1 settings.
  ```sh
  ./minicom.sh RECEIVE_PORT RECEIVE_FILE SEND_PORT SEND_FILE
  ```
  
  See the output.jpg to see output of this program.
   
#### What this application does and how it does
- Extract and validate parameters supplied to minicom.sh script.
- Compile Java source files and create app.jar representing Java application that will 
receive the file.
- Schedule this java application to be runned after 13 seconds as setting up minicom for 
file transfer takes time.
- Schedule simulating keypress and typing file name with the help of xdotool.
- Launch minicom which will receive keyevents and file name and launch 'sx' utility to 
actually do the file transfer.
- Initiate exit after 20 seconds.
     
#### Going further
- The shell script will self terminate after 20 seconds. This can be made interactive.

