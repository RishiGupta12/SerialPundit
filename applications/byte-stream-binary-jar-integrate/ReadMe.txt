
This application example demonstrates how to use InputStream/OutputStream for exchanging data with 
serial port. It also shows how this library can be integrated into existing binary-only 3rd party
jar using standard InputStream/OutputStream via interface.

1. Running this application
   ~~~~~~~~~~~~~~~~~~~~~~~~
   
   Open and configure minicom/teraterm for 115200 8N1 settings and start it. Start this program.
   Type any character in minicom and value typed will be shown in GUI window in graphical form. 
   
   See the output.jpg to see output of this program.
   
2. What this application does and how it does
   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   
   - Open serial port and configure it for desired settings.
   - Create InputStream and OutputStream for this serial port.
   - Create Java swing JFrame and UI related things and display them.
   - Create and start a worker thread that will read data from input stream and display it
     in text box. Further convert this ASCII value to corresponding integer and draw graph
     that shows value of each bit.
   - When close button is pressed in GUI window, close input and output byte stream, and
     then close serial port, and finally exit application explicitly.
	  
3. Going further
   ~~~~~~~~~~~~~
   
   - Full fledged GUI application can be developed using layouts and by using sensors.
   
   
