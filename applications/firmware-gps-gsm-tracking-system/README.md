This firmware example demonstrates how to interface GPS receiver and GSM modem with 
PIC18F4550 microcontroller for making a vehicle tracking system.

### Running this firmware
   
Build this firmware using C18 compiler and flash it using ICD tool. Connect GSM modem 
and GPS receiver (may require clear area) and power on board.

Send a SMS to VTS board containing LOC? string. The board will find the location and send 
it to the mobile who sent LOC? string.
   
### What this firmware does and how it does

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

### Hardware prerequisites

- GSM modem    &#8594; BENQ MOD 9001 GSM/GPRS MODEM
- GPS receiver &#8594; ALTINA SIRF III G-mouse GGM 309 GPS receiver

### Interfacing GSM MODEM with PIC 18F4550 microcontroller 
- The PIC microcontroller was interfaced to GSM MODEM using hardware EUASRT builtin in 18F4550. 
The EUSART was configured to operate in 115200 8N1 configuration using interrupts. An ISR was 
written at high interrupt vector and the EUSART receive interrupt was set as a high priority 
interrupts. 

- The AT commands were sent  in a specified sequence that basically configured the 
MODEM to operate in SMS text mode,new message receive  Acknowledge, english characters, 
message sending,receiving & deleting from SIM. 

- A considerable delay was introduced between two commands so that MODEM & SIM can finish their 
internal processing. A locking mechanism was adopted to ensure that proper response is received 
from MODEM for every command sent to it. 

- At hardware level a voltage level converter IC MAX232 was used from MAXIM.Finally a 2x1 multiplexer 
helped in switching between GPS and GSM modem. A virtual handshaking was also implemented. For this 
pin 4 & 7 of DB9 Connecter was tied to +5 volt supply. These pins represent RTS and DTE in RS232 
interface. A +5 volt supply represent valid RS232 logic level (voltage level).

### Interfacing GPS receiver with PIC 18F4550 microcontroller 
- The PIC microcontroller was interfaced to GPS receiver using hardware EUASRT builtin in 18F4550. 
The EUSART was configured to operate in 4800 8N1 configuration using interrupts. An ISR was 
written at high interrupt vector and the EUSART receive interrupt was set as a high  priority 
interrupts.

- The data is first continuously saved in a 750 bytes long buffer using receive interrupts. After 
this GPGGA string is searched. When found required fields like latitude, longitude and altitude 
are extracted & saved in another buffer.

- At hardware level a voltage level converter IC MAX232 was used from MAXIM. Finally a 2x1 multiplexer 
helped in switching between GPS and GSM modem.

### NMEA Format
GPS receivers require different signals in order to function. These variables are broadcast after position 
and time have been successfully calculated and determined. To ensure that the different types of appliances 
are portable there are either international standards for data exchange (NMEA and RTCM), or the manufacturer 
provides defined (proprietary) formats and protocols.

In order to relay computed GPS variables such as position, velocity, course etc. to a peripheral (e.g. computer,
screen, transceiver), GPS modules have a serial interface (TTL or RS-232 level). Information is broadcasted via 
this interface in a special data format. This format is standardized by the National Marine Electronics Association 
(NMEA) to ensure that data exchange takes place with out any problems.

Nowadays, data is relayed according to the NMEA-0183 specification. NMEA has specified data sets for
various applications e.g. GNSS (Global Navigation Satellite System), GPS, Loran, Omega, Transit and also 
for various manufacturers. 

The followings even data sets are widely used with GPS modules to relay GPS information :
1. GGA(GPS Fix Data, fixed data for the Global Positioning System)
2. GLL(Geographic Position Latitude/Longitude)
3. GSA(GNSS DOP and Active Satellites, degradation of accuracy and the number of active satellites in the Global Satellite Navigation System)
4. GSV(GNSS Satellites in View, satellites in view in the Global Satellite Navigation System)
5. RMC(Recommended Minimum Specific GNSS Data)
6. VTG(Course over Ground and Ground Speed, horizontal course and horizontal velocity)
7. ZDA(Time & Date)

Each GPS data set is formed in the same way and has the following structure :

$GPDTS,Inf_1,Inf_2,Inf_3,Inf_4,Inf_5,Inf_6,Inf_n*CS<CR><LF>

*Field Description:*
$--- Start of the data set
GP--- Information originating from a GPS appliance
DTS--- Data set identifier (e.g.RMC)
Inf_1--- bis Inf_n  Information with number 1...n (e.g.175.4 for course data)
, --- Comma use data separator for different items of information
*--- Asterisk used as a separator for the checksum
CS--- Checksum (control word) for checking the entire data set
<CR><LF>--- End of the data set: carriage return (<CR>) and line feed, (<LF>)

The maximum number of characters used must not exceed 79. For the purposes of determining this number, 
the start sign $ and end signs <CR><LF>are not counted.

The following lines are transmittted by any GPS receiver :

$GPRMC,130303.0,A,4717.115,N,00833.912,E,000.03,043.4,200601,01.3,W*7D<CR><LF>
$GPZDA,130304.2,20,06,2001,,*56<CR><LF>
$GPGGA,130304.0,4717.115,N,00833.912,E,1,08,0.94,00499,M,047,M,,*59<CR><LF>
$GPGLL,4717.115,N,00833.912,E,130304.0,A*33<CR><LF>
$GPVTG,205.5,T,206.8,M,000.04,N,000.08,K*4C<CR><LF>
$GPGSA,A,3,13,20,11,29,01,25,07,04,,,,,1.63,0.94,1.33*04<CR><LF>
$GPGSV,2,1,8,13,15,208,36,20,80,358,39,11,52,139,43,29,13,044,36*42<CR><LF>
$GPGSV,2,2,8,01,52,187,43,25,25,074,39,07,37,286,40,04,09,306,33*44<CR><LF>
$GPRMC,130304.0,A,4717.115,N,00833.912,E,000.04,205.5,200601,01.3,W*7C<CR><LF>
$GPZDA,130305.2,20,06,2001,,*57<CR><LF>
$GPGGA,130305.0,4717.115,N,00833.912,E,1,08,0.94,00499,M,047,M,,*58<CR><LF>
$GPGLL,4717.115,N,00833.912,E,130305.0,A*32<CR><LF>
$GPVTG,014.2,T,015.4,M,000.03,N,000.05,K*4F<CR><LF>
$GPGSA,A,3,13,20,11,29,01,25,07,04,,,,,1.63,0.94,1.33*04<CR><LF>
$GPGSV,2,1,8,13,15,208,36,20,80,358,39,11,52,139,43,29,13,044,36*42<CR><LF>
$GPGSV,2,2,8,01,52,187,43,25,25,074,39,07,37,286,40,04,09,306,33*44<CR><LF>

### GPGGA String
For this project, we are mainly concerned with the geographical location, that is, latitude, longitude 
and altitude. Therefore, we only need to look out for sentences beginning with $GPGGA. The GPGGA data set 
(GPS Fix Data) contain information on time, longitude and latitude, the quality of the system, the number 
of satellites used and the height. 

An example of a GPGGA data set is:
$GPGGA,130305.0,4717.115,N,00833.912,E,1,08,0.94,00499,M,047,M,,*58<CR><LF>

| Name                   | Example        | Units      | Description                       |
| :------------:         |:-------------: | :--------: | :--------:                        |
| Message ID             | $GPGGA         |            | GGA protocol header               |
| UTC Time               | 161229.487     |            | hhmmss.sss                        |
| Latitude               | 3723.2475      |            | ddmm.mmmm                         |
| N/S Indicator          | N              |            | N=north or S=south                |
| Longitude              | 12158.3416     |            | ddmm.mmmm                         |
| E/W Indicator          | E              |            | E=east or W=west                  |
| Position Fix Indicator | 1              |            |                                   |
| Satellites Used        | 7              |            | Range 0 to 12                     |
| HDOP                   | 1              |            | Horizontal Dilution of Precision  |
| MSL Altitude           | 9              | meters     |                                   |
| Units                  | M              | meters     |                                   |
| Age of Diff. Corr.     |                | seconds    | Null fields when DGPS is not used |
| Checksum               | *18            |            |                                   |
| <CR> <LF>              |                |            |                                   |


### Going further
- This example demonstrates communication with a mobile phone to send and receive location 
information. A dedicated GSM modem can be interfaced to computer at central server location 
all all the vehicle can be tracked globally from this center.

