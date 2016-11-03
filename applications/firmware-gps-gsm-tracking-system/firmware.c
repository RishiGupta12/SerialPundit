/************************************************************************************************
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ************************************************************************************************/

/* 
 * 1. Compiler     --> C18 compiler 
 * 2. GSM modem    --> BENQ MOD 9001 GSM/GPRS MODEM
 * 3. GPS receiver --> ALTINA SIRF III G-mouse GGM 309 GPS receiver
 *
 * Data at UART is received using interrupts. When communicating with GPS receiver, we use ISR to 
 * save data bytes in array. When a required number of bytes (747) have been received we disable 
 * the generation of UART RX interrupt.
 */
#include <p18f4550.h>   

/* Turn off features not needed to save power */
#pragma config WDT=OFF, FOSC=ECPLLIO_EC, PLLDIV=1, CPUDIV=OSC1_PLL2
#pragma config MCLRE=ON, CCP2MX=OFF,VREGEN=OFF, IESO=ON,DEBUG=OFF 
#pragma config LVP=OFF, FCMEN=ON, BOR=ON, BORV=3, PWRT=ON, PBADEN=OFF
#pragma config STVREN=ON, XINST=OFF,EBTR0=OFF,EBTR1=OFF,EBTR2=OFF,EBTR3=OFF
#pragma config CP0=OFF, CP1=OFF, CP2=OFF, CP3=OFF, CPB=OFF, CPD=OFF,EBTRB=OFF
#pragma config WRT0=OFF, WRT1=OFF, WRT2=OFF,WRT3=OFF, WRTB=OFF, WRTC=OFF, WRTD=OFF

#define  CR         0X0D
#define  LF         0X0A   
#define  CTRLZ      0X1A
#define  SPACE      0X20
#define  COMMA      0X2C
#define  NULL       0X00
#define  CLR        0x00
#define  OUTPUT     0X00
#define  ON         0x01
#define  OFF        0x00
#define  BIT_OUTPUT 0x00
#define  BIT_CLR    0x00
#define  LED_ON     0x01
#define  LED_OFF    0x00

unsigned int k, loc;
volatile unsigned int x;
signed char a;
unsigned char msg_index, success, gsm;

/* Command to be sent to the GSM modem */
const rom unsigned char at_cmd_1[] = "ATE0\r";
const rom unsigned char at_cmd_2[] = "AT\r";
const rom unsigned char at_cmd_3[] = "AT+CMGF=1\r"; 
const rom unsigned char at_cmd_4[] = "AT+CSMP=17,168,0,0\r";
const rom unsigned char at_cmd_5[] = "AT+CPMS=";
const rom unsigned char at_cmd_6[] = "AT+CNMI=1,1,0,0,1\r";
const rom unsigned char at_cmd_7[] = "AT+CMGD=1,4\r";
const rom unsigned char at_cmd_8[] = "AT+CMGR=";
const rom unsigned char at_cmd_9[] = "AT+CMGS=";

/* Buffers to hold data to be processed */
unsigned char gsm_buf[150];
unsigned char mob_no_buf[12];
unsigned char gps_buf[750];
unsigned char msg_buf[45]; 

/* Function prototypes */
void start_up_delay(void);
void safe_op(void);
void gsm_uart_init(void);
void high_isr(void); 
void gpio_port(void); 
void modem_init(void);
void tx_char(unsigned char);
void cmd_1(void);
void cmd_2(void);
void cmd_3(void);
void cmd_4(void);
void cmd_5(void);
void cmd_6(void);
void long_delay(void);
void clr_buf(void);
void clean_sim(void);
void wait_4_msg(void);  
void get_index(void);
void read_msg(void);
void check_msg(void);
void get_mob_no(void);
void send_msg_cmd(void);
void gps_handler(void);
void gps_uart_init(void);
void save_nmea_data(void);
unsigned int search_gpgga(unsigned int);
unsigned int ext_req_field(unsigned int);
void send_loc(void); 

/* Install/Define critical interrupt handler */
#pragma code high_vector = 0x08
void interrupt_at_high_vector(void) {
	_asm
	GOTO high_isr
	_endasm
}
#pragma code

/* Based on with whom data should be read; GPS receiver or GSM modem, the data read is placed 
 * in appropriate buffer. Then other function process data in these buffers whenever required. */
#pragma interrupt high_isr 
void high_isr(void) {
	if(gsm == ON) {
		INTCONbits.GIEH = 0;
		gsm_buf[x] = RCREG;              
		x++;             
		PIR1bits.RCIF = 0;
		INTCONbits.GIEH = 1;          
	}else {
		INTCONbits.GIEH = 0;
		gps_buf[x] = RCREG;              
		x++;             
		PIR1bits.RCIF = 0;
		INTCONbits.GIEH = 1;          
	} 
}

/* When the system is powered on, let it settle. */
void start_up_delay(void) {
	unsigned int count_s;
	count_s = 0;
	while(count_s != 305) {       
		for(k=65000; k>5; k--);   
		count_s++;
	}  
}

void long_delay(void) {
	unsigned char count_a;
	count_a = 0;
	while(count_a != 25) {       
		for(k=65000; k>5; k--);   
		count_a++;
	}  
}

/* Set appropriate bits for proper operation */
void safe_op(void) {
	UCONbits.USBEN=0;       //  USB module off
	UCFGbits.UTRDIS=1;      //  USB transeiver off
	SPPCONbits.SPPEN=0;     //  streaming parallel port off
	CCP1CON=0;              //  ECCP1 module off
	CCP2CON=0;              //  ECCP2 module off
	ADCON0bits.ADON=0;      //  A/D module off
	SSPCON1bits.SSPEN=0;    //  SPI & I2C OFF
}

/* Prepare the port for communication with GSM modem */
void gsm_uart_init(void) {
	INTCONbits.GIEH = 0;
	while(INTCONbits.GIEH != 0) {
		INTCONbits.GIEH = 0;
	}
	TRISCbits.TRISC7 = 1;
	TRISCbits.TRISC6 = 1;      
	RCSTA=0B10010000;         // Enable serial port and continuous receive
	BAUDCON=0B00001000;       // BRG16 = 1
	TXSTA=0B00100110;         // SYNC=0, BRGH=1
	SPBRG=103;                // 48 mhz ---> 115200bps
	RCONbits.IPEN = 1;        // enable priority levels on interrupts
	IPR1bits.RCIP = 1;        // Make receive interrupt high priority
	IPR1bits.TXIP = 0;        // Make transmit interrupt low priority 
	PIR1bits.RCIF = 0;        // clear receive flag
	PIR1bits.TXIF = 0;        // clear transmit flag
	PIE1bits.RCIE = 1;        // Enable receive interrupt         
	INTCONbits.PEIE = 1;      // Enable peripherals interrupt        
	INTCONbits.GIEH = 1;      // Enable all unmasked interrupt 
}

/* Prepare the port for communication with GPS receiver */
void gps_uart_init(void) {   
	INTCONbits.GIEH = 0;
	while(INTCONbits.GIEH != 0) {
		INTCONbits.GIEH = 0;
	}
	TRISCbits.TRISC7 = 1;      
	RCSTA=0B10010000;         // Enable serial port& continuous receive
	BAUDCON=0B00000000;       // BRG16 = 0
	TXSTA=0B00000000;         // asynchronous mode & BRGH = 0
	SPBRG=155;                // 155 for 48MHz ---> 4800 baud
	RCONbits.IPEN = 1;        // enable priority levels on interrupts
	IPR1bits.RCIP = 1;        // Make receive interrupt high priority
	PIR1bits.RCIF = 0;        // clear receive flag
	PIE1bits.RCIE = 1;        // Enable receive interrupt   
	INTCONbits.PEIE = 1;      // Enable peripherals interrupt            
	INTCONbits.GIEH = 1;      // Enable global interrupt 
}

/* Configure GPIO pins to control multiplexer (share RX pin of PIC18F4550 between 
 * GSM Modem and GPS receiver) */
void gpio_port(void) {         
	TRISBbits.TRISB0 = BIT_OUTPUT;   
	TRISBbits.TRISB1 = BIT_OUTPUT;          
	PORTBbits.RB1 = BIT_CLR;          // G  
	ADCON1 = 0XFF;
	TRISAbits.TRISA0 = BIT_OUTPUT;
	TRISAbits.TRISA1 = BIT_OUTPUT; 
	TRISAbits.TRISA6 = BIT_OUTPUT;
	TRISBbits.TRISB1 = BIT_OUTPUT; 
	TRISCbits.TRISC0 = BIT_OUTPUT;
	TRISCbits.TRISC1 = BIT_OUTPUT;
	TRISCbits.TRISC2 = BIT_OUTPUT;
	PORTAbits.RA0 = BIT_CLR;
	PORTAbits.RA1 = BIT_CLR;
	PORTAbits.RA6 = BIT_CLR;
	PORTCbits.RC0 = BIT_CLR;
	PORTCbits.RC1 = BIT_CLR;
	PORTCbits.RC2 = BIT_CLR ;            
}

/* Prepare the GSM Modem for english character based SMS send/recieve from SIM */
void modem_init(void) {
	cmd_1();
	if(gsm_buf[7]=='O' && gsm_buf[8]=='K')
		success = 1; 
	else
		success = 0; 
	if (success == 1)
	{;}
	else
		modem_init();

	cmd_2(); 
	if(gsm_buf[2]=='O' && gsm_buf[3]=='K')
		success = 1; 
	else
		success = 0; 
	if (success == 1)
	{;}
	else
		modem_init();

	cmd_3();
	if(gsm_buf[2]=='O' && gsm_buf[3]=='K')
		success = 1; 
	else
		success = 0; 
	if (success == 1)
	{;}
	else
		modem_init();

	cmd_4();
	if(gsm_buf[2]=='O' && gsm_buf[3]=='K')
		success = 1; 
	else
		success = 0; 
	if (success == 1)
	{;}
	else
		modem_init();

	cmd_5();
	if(gsm_buf[6]=='S' && gsm_buf[7]==':')
		success = 1; 
	else
		success = 0; 
	if (success == 1)
	{;}
	else
		modem_init();

	cmd_6(); 
	if(gsm_buf[2]=='O' && gsm_buf[3]=='K')
		success = 1; 
	else
		success = 0; 
	if (success == 1)
	{;}
	else
		modem_init();        
}

/* Turn Echo off ("ATE0\r") */
void cmd_1(void) { 
	x = 0;
	while(x != 0) 
		x=0;
	for(a=0; a<5; a++)             
		tx_char(at_cmd_1[a]);                            
	long_delay();   
}

/* Just ping modem for basic AT command ("AT\r") */
void cmd_2(void) { 
	x = 0;
	while (x != 0) 
		x=0;
	for(a=0; a<3; a++)             
		tx_char(at_cmd_2[a]);                       
	long_delay();            
}

/* Put in SMS text mode ("AT+CMGF=1\r") */
void cmd_3(void) { 
	x = 0;
	while( x!= 0) 
		x=0;
	for(a=0; a<10; a++)             
		tx_char(at_cmd_3[a]);                         
	long_delay();            
}

/* Configure to send English character SMS and some more parameters 
 * ("AT+CSMP=17,168,0,0\r") */
void cmd_4(void) {
	x = 0;
	while(x!= 0) 
		x=0;  
	for(a=0; a<19; a++)             
		tx_char(at_cmd_4[a]);                          
	long_delay();                           
}

/* Set SMS message storage area as SIM for every purpose ("AT+CPMS=") */
void cmd_5(void) { 
	x = 0;
	while(x != 0) 
		x=0;
	for(a=0; a<8; a++)             
		tx_char(at_cmd_5[a]);              
	tx_char('"');
	tx_char('S');
	tx_char('M');
	tx_char('"');
	tx_char(',');
	tx_char('"');
	tx_char('S'); 
	tx_char('M');
	tx_char('"');
	tx_char(',');
	tx_char('"'); 
	tx_char('S');
	tx_char('M');
	tx_char('"'); 
	tx_char(CR);              
	long_delay();                                                    
}

/* Set the new message indicators ("AT+CNMI=1,1,0,0,1\r") */
void cmd_6(void) { 
	x = 0;
	while(x != 0) 
		x=0;
	for(a=0; a<18; a++)             
		tx_char(at_cmd_6[a]);                
	long_delay();                                          
}

/* Transmits a single character out of UART port */
void tx_char(unsigned char temp) {
	TXREG = temp; 
	while(PIR1bits.TXIF != 1);
	for(k=4000; k>5; k--);
	PIR1bits.TXIF=0;          
}

/* Clear the buffer */
void clr_buf(void) {
	for(a=149; a>0; a--)
		gsm_buf[a] = CLR;
}

/* Delete all SMS from SIM */
void clean_sim(void) {
	x = 0;
	while(x!= 0) 
		x=0;
	for(a=0; a<12; a++)             
		tx_char(at_cmd_7[a]);                
	long_delay();  
	long_delay();
	cmd_2();                  
	if(gsm_buf[13]=='3' && gsm_buf[13]=='1' && gsm_buf[13]=='4' )                            
		long_delay();                                                       
}

/* Wait until a SMS arrives */
void wait_4_msg(void) {
	unsigned char dummy;
	x = 0;
	while(x != 0) 
		x=0;
	dummy = RCREG;
	PIR1bits.RCIF = 0;
	while(PIR1bits.RCIF != 1);
	long_delay();                 
}

/* Get the index of SMS received */
void get_index(void) {
	x = 0;
	while(x!= 0) 
		x=0;
	while(gsm_buf[x] != 'S')             
		x++;             
	msg_index = gsm_buf[x+4];           
}

/* Once a SMS message has arrived and its index has been found, read it from 
 * SIM to local buffer ("AT+CMGR="). */
void read_msg(void) { 
	x = 0;
	while( x!= 0) 
		x=0;
	for(a=0;a<8;a++)             
		tx_char(at_cmd_8[a]);                    
	tx_char(msg_index);        
	tx_char(CR);               
	long_delay();          
	long_delay();
	long_delay(); 
}

/* Once a SMS message is received, validate it to contain LOC? string to authenticate sender */
void check_msg(void) {
	signed char b;
	unsigned char count_c;
	b = 0;
	count_c = 0;     
	while(count_c != 8) {       
		if(gsm_buf[b] == '"') {
			count_c++;
			b++;
		}else{
			b++;
		}
	}
	while(gsm_buf[b]!= LF)          
		b++;          
	if(gsm_buf[b+1]=='L' && gsm_buf[b+2]=='O' && gsm_buf[b+3]=='C' && gsm_buf[b+4]=='?')
		success = 1;          
	else          
		success = 0;                     
}

/* Extract mobile number of device who wish to receive location info */
void get_mob_no(void) {
	unsigned char temp;                
	temp = 25; // buf[28]=3                                   
	a = 0;                                            
	while(a != 10) {                      
		mob_no_buf[a] = gsm_buf[temp];
		a++;
		temp++;
	}                     
}

/* Instruct GSM modem that we need to send an SMS */
void send_msg_cmd(void) {
	for(a=0; a<8; a++)             
		tx_char(at_cmd_9[a]);                   
	tx_char('"');
	tx_char('+');
	tx_char('9');
	tx_char('1'); 
	for(a=0; a<10; a++)             
		tx_char(mob_no_buf[a]);                    
	tx_char('"');
	tx_char(CR);
	long_delay();               
} 

/* Configure the UART for communication with GPS receiver, toggle the multiplxer GPIO,
 * save data from GPS receiver, search for GPGGA string and extract latitude, longitude 
 * and altitude from this string. */
void gps_handler(void) {                    
	gps_uart_init();        
	PORTBbits.RB0 = 0;           // A/B // gps gets connected to UART port.    
	save_nmea_data();                            
	loc = search_gpgga(0);  
	ext_req_field(loc);             
}

/* Keep clearing interrupt bit until handful of data characters have been received 
 * from GPS receiver that can be parsed to look for GPGGA string */
void save_nmea_data(void) {      
	x = 0;
	while(x!= 0) 
		x=0;
	while(x != 747);
	INTCONbits.GIEH = 0;        
}

/* From the data received try to figure out starting position of GPGGA string */
unsigned int search_gpgga(unsigned int y) {

	/* The y has starting address from where it should start searching '$' character */
	while(gps_buf[y] != '$') {
		y++;
	}

	/* check for gpgga string */
	if(gps_buf[y+1]=='G' && gps_buf[y+2]=='P' && gps_buf[y+3]=='G'
			&& gps_buf[y+4]=='G' && gps_buf[y+5]=='A') {
		return(y+5); // return address of 'A'
	}else {
		search_gpgga(y+10); // start search from new location in buffer
	}
}

/* Send location info to given mobile number using SMS */
void send_loc(void) {
	unsigned char c;
	c = 0;
	/* We have appended null character already at the end so send data 
	 * until it is found */
	while(msg_buf[c] != NULL) {
		tx_char(msg_buf[c]);
		c++;                                      
	}

	tx_char(CTRLZ); 
	long_delay();    
}

/* Parse the data received from GPS receiver and extract required fields */
unsigned int ext_req_field(unsigned int p) { 
	unsigned char comma_count;  // variable to hold count of commas.
	unsigned char z;

	// cross check returned value also.
	if( gps_buf[p] == 'A')  {
		p=p+2;
		while(gps_buf[p] != COMMA){
			p++;                           
		}

		p++;                // p now have address of 1st digit of
		z=0;                // latitude coordinate.
		msg_buf[z]='L';     // append 'LA' to msg_buf to indicate
		z++;                // that following value is latitude.
		msg_buf[z]='A';
		z++;
		msg_buf[z]=SPACE;
		z++;

		/* Save latitude coordinates */
		while(gps_buf[p] != COMMA) {
			msg_buf[z]=gps_buf[p]; 
			z++;
			p++;
		}

		p=p+3;               // p now have address of 1st digit of
		msg_buf[z]=COMMA;
		z++;
		msg_buf[z]=CR;
		z++;
		msg_buf[z]=LF;
		z++;
		msg_buf[z]='L';      // longitude coordinate.
		z++;
		msg_buf[z]='O';      // append 'LO' to msg_buf to indicate
		z++;                 // that following value is longitude.
		msg_buf[z]=SPACE;
		z++;

		/* Save longitude coordinate */
		while(gps_buf[p] != COMMA) {
			msg_buf[z]=gps_buf[p];
			z++;
			p++;
		}

		p++;
		comma_count = 0;

		/* After this loop ends the p will contain  address of first digit of altitude coordinates */ 
		while(comma_count != 4) {
			if(gps_buf[p] == COMMA ) {
				comma_count++;
				p++;
			}else {
				p++;
			}
		} 
		msg_buf[z]=COMMA;
		z++;
		msg_buf[z]=CR;
		z++; 
		msg_buf[z]=LF;
		z++;          
		msg_buf[z]='A';        // append 'AL' to msg_buf to indicate
		z++;                   // that following value is altitude.
		msg_buf[z]='L'; 
		z++;
		msg_buf[z]=SPACE;
		z++;

		/* Save altitude coordinate */
		while(gps_buf[p] != COMMA ) {
			msg_buf[z] = gps_buf[p];
			z++;
			p++;
		} 
		msg_buf[z]=COMMA;
		z++; 
		msg_buf[z]= NULL;// append null character to mark end

	}else {
		/* Something went wrong, start searching $gpgga again */
		search_gpgga(p+10);
	}
}

/* Entry point */
void main(void) {
	start_up_delay();      
	safe_op();
	gsm_uart_init();    
	gpio_port(); 
	PORTBbits.RB0 = 1; // A/B // gsm modem connected      
	gsm = ON;  
	modem_init();
	PORTAbits.RA1 = LED_ON;

	/* Keep looping until powered off */
	while(1) {
		clr_buf(); 
		clean_sim();
		PORTAbits.RA0 = LED_ON;
		wait_4_msg();       
		get_index();  
		read_msg();
		check_msg();
		PORTAbits.RA6 = LED_ON;
		if(success == 1) {  
			get_mob_no();      
			send_msg_cmd();           
			gsm = OFF;
			PORTCbits.RC0 = LED_ON; 
			gps_handler();
			PORTCbits.RC1 = LED_ON; 
			gsm = ON;
			gsm_uart_init();  
			PORTBbits.RB0 = 1;        // A/B ---> gsm modem connected  
			INTCONbits.GIEH = 1;      // Enable all unmasked interrupt           
			send_loc(); 
			PORTCbits.RC2 = LED_ON;
			for(k=60000; k>5; k--); 
			PORTAbits.RA0 = LED_OFF;
			PORTAbits.RA6 = LED_OFF;
			PORTCbits.RC0 = LED_OFF;
			PORTCbits.RC1 = LED_OFF;
			PORTCbits.RC2 = LED_OFF;  
		}else {
			PORTAbits.RA0 = LED_OFF;
			PORTAbits.RA6 = LED_OFF;
		} 
	}            	                 
}
