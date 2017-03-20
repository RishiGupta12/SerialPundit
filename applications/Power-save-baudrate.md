#### Saving power in embedded systems

If SerialPundit is used for serial communication in battery powered embedded systems then a 
power saving can be achieved by calculating delay between transmitter and receive end.

Suppose a tx end send 128 byte frame at 9600 baudrate. Now it will take ~106 milli seconds to 
transmit it. Assuming line delay (physical wire propagation) is minimum ~50 milli seconds then 
~156 milli seconds it will take for this complete frame to be received.

The transmitter can use this fact to sleep for ~160 milli seconds after sending this frame as it 
knows that the receiver will send response only after ~160 milli seconds. Because the transmitter 
is not polling the serial port battery power will be saved.

