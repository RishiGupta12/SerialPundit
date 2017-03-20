This example demonstrates a basic serial port terminal application that can be developed using 
Java Swing and AWT GUI widgets.

!["serial communication in java"](serial-terminal-in-java.jpg?raw=true "serial communication in java")

#### Running this application

Launch this application. Select serial port from list and configure all the parameters. To send 
data type the data and click on send button. Data received will be automatically shown in receive 
window.
   
#### What this application does and how it does

- Set up the GUI widgets, add their respective listeners and show the UI. Center the window in computer 
screen.
- If open button is pressed, open the given serial port and create a worker thread that will read 
data from serial port and set text field of receive text area.
- If user presses send button send data to serial port.
- When user clicks on close button of window, close the serial port (if open) and terminate worker 
thread if it exist.
     
#### Going further
- A full fledged serial terminal application can be developed using custom GUI look and feel.

