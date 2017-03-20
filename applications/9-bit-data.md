This application note explains how to achieve actual 9 bit data communication and mark/space parity bit emulation.

## 9-bit data width communication

In serial port communication, the uart frame consist of a start bit, data bits and stop bits. Typically the length of data bits varies from 5 to 8 bits with most of the application using 8-N-1 configuration. Some custom protocols and multidrop bus (MDB) uses and additional bit to carry extra information. This additional bit appears after last data bit but before stop bit in given uart frame.

The MDB uses this additional bit to differentiate between address and data byte. Custom protocols uses this bit to indicate that the given data byte is actual data or control information. Some rare custom protocols defines that this bit must always be 0 or 1 in uart frame, some define a particular sequence for example; in the transmitted packet, 3rd uart frame must have this bit set to 1 with all other frames having this bit set to 0. Another such sequence can be 9th bit is 1 in 1st frame to represent address, than all other frmaes will have this bit as 0 and then the last frame will have this bit again as 1 to indicate end of message.

There are three ways in which this additional bit can be added in transmitted uart frame:

- **Use uart hardware** that supports 9-bit data width configuration for example; OX16C950 for host computer and ATmega328, MAX3109, dsPIC33E families microcontroller from microchip for embedded system etc. Most of the host computer hardware, driver and operating system does not support 9-bit mode and therefore custom drivers and libraries have to be developed and used. UART hardware itself may be little more expensive also.

  This method has some variance and implementation specific complexity. Some hardware provide 9 bit wide FIFO for ex; dsPIC30F3012, while some uses a bit in another register as 9th data bit for ex; ATmega328. This increases the need to complete things in a timely manner and a more complicated driver etc. Most hardware does not support 9 bit data with parity bit also available.

- **Emulate 9th bit** while using the standard supported uart configuration on available hardware and software resources. This involves enabling the parity bit in uart frame and explicitly dynamically setting this bit. If the 9th bit is to be set to 0, count the number of 1's from 0th to 8th data bit. If this number is even then configure the uart controller for 8-E-1 communication otherwise 8-O-1 (8 data bits, odd parity and 1 stop bit). This has to be done for every uart frame to be transmitted.
  
  Similarly, If the 9th bit is to be set to 1, count the number of 1's from 0th to 8th data bit. If this number is even then configure the uart controller for 8-O-1 communication otherwise 8-E-1. An important point to note is that the parity setting is common for both tx and rx part of uart hardware and therefore we can not simultaneously transmit and receive data. For a master/slave configuration, however this is not an issue.
  
  Special attention must be paid to the timing as if a byte is transmitted or received while the parity setting is changed, inconsistent result may be observed. The tx buffer must be emptied physically  by uart hardware for each byte to make sure that desired parity settings come in effect as set for each byte i.e. setting parity, uart frame construction and physically transmitting the frame must be an atomic operation. The protocol itself may have defined a time window and we need to make sure that it is met. Further this trick may not be applied if parity bit is also used in addition to 9 bit data.
  
- **Bit bang the GPIO** if available. This is mainly applicable to embedded system microcontroller where firmware manually constructs uart frame and send it over the same GPIO pin physically.

There are one way in which 9th bit can be received by using 8 bit uart hardware. 

- **XOR parity error** The trick given below can be applied to determine the value of 9th bit where the sender is configured to send 9 bit data, no parity while the receiver supports only 8 bit data communication.

  1. Configure receiver for 8 bit data and even parity.
  2. When data is received calculate what should be the parity bit (0 or 1) for this particular 8 bits received.
  3. Call Operating system specific API to check if any parity error has happened or not when receiving data.
  4. Calculate XOR from the value obtained in step two and three. The result will be the 9th bit sent by sender.

## Emulating Mark and Space parity bit

In serial port communication, the uart frame may use an additional bit known as parity bit to detect corrupted data byte. This bit is placed between last data bit and stop bit. There are 5 types of parity namely; even, odd, mark, space and no parity. Mark and space parity is supported by most available hardware however some tweaks may be needed in case the underlying operating system, driver or system library does not provide enough support for mark and space parities.

If no parity is used, uart frame contains only data bits, start and stop bit(s). If even parity is used, the parity bit is set to 0 if there is an even number of 1's in in data bits. If odd parity is used, the parity bit is set to 0 if there is an odd number of 1's in the data bits. If mark parity is used, the parity bit is always 1 irrespective of the data bits contents. If space parity is used, the parity bit is always 0 irrespective of the data bits contents.

The emulation trick described above can also be used to emulate mark and space parity if the underlying driver or software does not provide explicit support for it.

- To emulate 7M1 (7 data bits, Mark parity, 1 stop bit), configure uart for 8N1 and set 8th data bit to 1 always.
- To emulate 7S1 (7 data bits, Space parity, 1 stop bit), configure uart for 8N1 and set 8th data bit to 0 always.
- To emulate 8M1 (8 data bits, Mark parity, 1 stop bit), configure uart for 8N2. Since stop bit is always 1, it will emulate 8M1.
- To emulate 8S1 (8 data bits, Space parity, 1 stop bit), use the dynamic parity setting method described above.

