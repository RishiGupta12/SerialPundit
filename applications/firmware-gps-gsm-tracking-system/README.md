This firmware example demonstrates how to interface GPS receiver and GSM modem with 
PIC18F4550 microcontroller for making a vehicle tracking system.

#####Running this firmware
   
Build this firmware using C18 compiler and flash it using ICD tool. Connect GSM modem 
and GPS receiver (may require clear area) and power on board.

Send a SMS to VTS board containing LOC? string. The board will find the location and send 
it to the mobile who sent LOC? string.
   
#####What this firmware does and how it does

- Turn features of microcontroller not needed to save power.
- Configure UART port to communicate with GSM Modem.
- Initialize GSM modem to send and receive English character SMS and store SMS in SIM 
memory only.
- Delete all previous SMS from SIM memory.
- Wait until a SMS message is received. Once received, check if it contain LOC? string.
- If message done not contain LOC? string, delete this SMS message. 
- If it contains LOC? string, configure the PIC18F4550 UART to communicate with GPS receiver.
- Read more than 700 characters from GPS receiver using interrupt service routine to prevent 
data loss.
- Once data has been received, parse it for GPGGA string.
- Once GPGGA string is found, extract latitude, longitude and altitude.
- Send this location information to mobile phone using SMS message.
- Loop back to wait for next SMS message after configuring PIC18F4550 to communicate with 
GSM Modem.

#####Hardware

GSM modem    &#8594; BENQ MOD 9001 GSM/GPRS MODEM

GPS receiver &#8594; ALTINA SIRF III G-mouse GGM 309 GPS receiver

#####Going further
- This example demonstrates communication with a mobile phone to send and receive location 
information. A dedicated GSM modem can be interfaced to computer at central server location 
all all the vehicle can be tracked globally from this center.

