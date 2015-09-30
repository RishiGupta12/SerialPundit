/***************************************************************************************************
 * Author : Rishi Gupta
 *
 * This file is part of 'serial communication manager' library.
 *
 * The 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with serial communication manager. If not, see <http://www.gnu.org/licenses/>.
 *
 ***************************************************************************************************/

#ifndef SCM_CP210XMANUFACTURING_H_
#define SCM_CP210XMANUFACTURING_H_

#include <jni.h>

/* The values of constants in this file must match their value in java layer. */

/* Mask and Latch value bit definitions.*/
#define SCM_CP210x_RETURN_SERIAL_NUMBER 0x00
#define SCM_CP210x_RETURN_DESCRIPTION   0x01
#define SCM_CP210x_RETURN_FULL_PATH     0x02

/* Flush Buffer definitions */
/* When these bits are set, the device will flush that buffer */
#define SCM_FC_OPEN_TX	 	0x01
#define SCM_FC_OPEN_RX	 	0x02
#define SCM_FC_CLOSE_TX	 	0x04
#define SCM_FC_CLOSE_RX 	0x08

/* Define bit locations for Mode/Latch for Reset and Suspend structures */
#define SCM_PORT_RI_ON   	0x0001
#define SCM_PORT_DCD_ON	  	0x0002
#define SCM_PORT_DTR_ON	  	0x0004
#define SCM_PORT_DSR_ON	  	0x0008
#define SCM_PORT_TXD_ON	  	0x0010
#define SCM_PORT_RXD_ON	  	0x0020
#define SCM_PORT_RTS_ON	  	0x0040
#define SCM_PORT_CTS_ON	  	0x0080

#define SCM_PORT_GPIO_0_ON		0x0100
#define SCM_PORT_GPIO_1_ON		0x0200
#define SCM_PORT_GPIO_2_ON		0x0400
#define SCM_PORT_GPIO_3_ON		0x0800

#define SCM_PORT_SUSPEND_ON			0x4000	// Can't configure latch value
#define SCM_PORT_SUSPEND_BAR_ON		0x8000	// Can't configure latch value

/* Define bit locations for EnhancedFxn */
#define SCM_EF_GPIO_0_TXLED					0x01	//  Under device control
#define SCM_EF_GPIO_1_RXLED					0x02	//  Under device control
#define SCM_EF_GPIO_2_RS485					0x04	//  Under device control
#define SCM_EF_RS485_INVERT					0x08	//  RS485 Invert bit
#define SCM_EF_WEAKPULLUP					0x10	//  Weak Pull-up on
#define SCM_EF_RESERVED_1					0x20	//	Reserved, leave bit 5 cleared
#define SCM_EF_SERIAL_DYNAMIC_SUSPEND		0x40	//  For 8 UART/Modem signals
#define SCM_EF_GPIO_DYNAMIC_SUSPEND			0x80	//  For 4 GPIO signals

/* CP2105 Define bit locations for Mode/Latch for Reset and Suspend structures */
#define SCM_PORT_RI_SCI_ON			0x0001
#define SCM_PORT_DCD_SCI_ON			0x0002
#define SCM_PORT_DTR_SCI_ON			0x0004
#define SCM_PORT_DSR_SCI_ON			0x0008
#define SCM_PORT_TXD_SCI_ON			0x0010
#define SCM_PORT_RXD_SCI_ON			0x0020
#define SCM_PORT_RTS_SCI_ON			0x0040
#define SCM_PORT_CTS_SCI_ON			0x0080
#define SCM_PORT_GPIO_0_SCI_ON		0x0002
#define SCM_PORT_GPIO_1_SCI_ON		0x0004
#define SCM_PORT_GPIO_2_SCI_ON		0x0008
#define SCM_PORT_SUSPEND_SCI_ON		0x0001	/*  Can't configure latch value */
//
#define SCM_PORT_RI_ECI_ON			0x0100
#define SCM_PORT_DCD_ECI_ON			0x0200
#define SCM_PORT_DTR_ECI_ON			0x0400
#define SCM_PORT_DSR_ECI_ON			0x0800
#define SCM_PORT_TXD_ECI_ON			0x1000
#define SCM_PORT_RXD_ECI_ON			0x2000
#define SCM_PORT_RTS_ECI_ON			0x4000
#define SCM_PORT_CTS_ECI_ON			0x8000
#define SCM_PORT_GPIO_0_ECI_ON		0x0400
#define SCM_PORT_GPIO_1_ECI_ON		0x0800
#define SCM_PORT_SUSPEND_ECI_ON		0x0100	/*  Can't configure latch value */

/* CP2105 Define bit locations for EnhancedFxn_ECI */
#define SCM_EF_GPIO_0_TXLED_ECI			0x01	/*  Under device control */
#define SCM_EF_GPIO_1_RXLED_ECI			0x02	/*  Under device control */
#define SCM_EF_GPIO_1_RS485_ECI			0x04	/*  Under device control */
#define SCM_EF_RS485_INVERT				0x08	/*  Under device control */
#define SCM_EF_INVERT_SUSPEND_ECI		0x10	/*  RS485 Invert bit */
#define SCM_EF_DYNAMIC_SUSPEND_ECI		0x40	/*  For GPIO signals */

/* CP2105 Define bit locations for EnhancedFxn_SCI */
#define SCM_EF_GPIO_0_TXLED_SCI			0x01	/*  Under device control */
#define SCM_EF_GPIO_1_RXLED_SCI			0x02	/*  Under device control */
#define SCM_EF_INVERT_SUSPEND_SCI		0x10	/*  RS485 Invert bit */
#define SCM_EF_DYNAMIC_SUSPEND_SCI		0x40	/*  For GPIO signals */

/* CP2105 Define bit locations for EnhancedFxn_Device */
#define SCM_EF_WEAKPULLUP				0x10	/*  Weak Pull-up on */

/* CP2108 Define bit locations for Mode/Latch for Reset and Suspend structures */
/* PB0 */
#define  SCM_PORT_TX0       0x0001
#define  SCM_PORT_RX0       0x0002
#define  SCM_PORT_RTS0      0x0004
#define  SCM_PORT_CTS0      0x0008
#define  SCM_PORT_DTR0      0x0010
#define  SCM_PORT_DSR0      0x0020
#define  SCM_PORT_DCD0      0x0040
#define  SCM_PORT_RI0       0x0080
#define  SCM_PORT_TX1       0x0100
#define  SCM_PORT_RX1       0x0200
#define  SCM_PORT_RTS1      0x0400
#define  SCM_PORT_CTS1      0x0800
#define  SCM_PORT_DTR1      0x1000
#define  SCM_PORT_DSR1      0x2000
#define  SCM_PORT_DCD1      0x4000
#define  SCM_PORT_RI1       0x8000

/* PB1 */
#define  SCM_PORT_GPIO_0    0x0001
#define  SCM_PORT_GPIO_1    0x0002
#define  SCM_PORT_GPIO_2    0x0004
#define  SCM_PORT_GPIO_3    0x0008
#define  SCM_PORT_GPIO_4    0x0010
#define  SCM_PORT_GPIO_5    0x0020
#define  SCM_PORT_GPIO_6    0x0040
#define  SCM_PORT_GPIO_7    0x0080
#define  SCM_PORT_GPIO_8    0x0100
#define  SCM_PORT_GPIO_9    0x0200
#define  SCM_PORT_GPIO_10   0x0400
#define  SCM_PORT_GPIO_11   0x0800
#define  SCM_PORT_GPIO_12   0x1000
#define  SCM_PORT_GPIO_13   0x2000
#define  SCM_PORT_GPIO_14   0x4000
#define  SCM_PORT_GPIO_15   0x8000

/* PB2 */
#define  SCM_PORT_SUSPEND   0x0001
#define  SCM_PORT_SUSPEND_BAR   0x0002
#define  SCM_PORT_DTR2      0x0004
#define  SCM_PORT_DSR2      0x0008

/* PB3 */
#define  SCM_PORT_TX2       0x0001
#define  SCM_PORT_RX2       0x0002
#define  SCM_PORT_RTS2      0x0004
#define  SCM_PORT_CTS2      0x0008
#define  SCM_PORT_DCD2      0x0010
#define  SCM_PORT_RI2       0x0020
#define  SCM_PORT_DTR3      0x0040
#define  SCM_PORT_DSR3      0x0080
#define  SCM_PORT_DCD3      0x0100
#define  SCM_PORT_RI3       0x0200

/* PB4 */
#define  SCM_PORT_RTS3      0x0001
#define  SCM_PORT_CTS3      0x0002
#define  SCM_PORT_TX3       0x0004
#define  SCM_PORT_RX3       0x0008

/* CP2108 Define bit locations for EnhancedFxn_IFCx */
#define SCM_EF_IFC_GPIO_TXLED   0x01
#define SCM_EF_IFC_GPIO_RXLED   0x02
#define SCM_EF_IFC_GPIO_RS485   0x04

/* If the next bit is clear, GPIO1 is low while sending UART data. */
/* If it is set, GPIO1 is high while sending UART data, and low otherwise. */
#define SCM_EF_IFC_GPIO_RS485_LOGIC 	0x08
#define SCM_EF_IFC_GPIO_CLOCK       	0x10
#define SCM_EF_IFC_DYNAMIC_SUSPEND  	0x40

/* CP2108 Define bit locations for EnhancedFxn_Device. */
#define SCM_EF_DEVICE_WEAKPULLUP_RESET      0x10
#define SCM_EF_DEVICE_WEAKPULLUP_SUSPEND    0x20
#define SCM_EF_DEVICE_DYNAMIC_SUSPEND       0x40


/* Constant string defines */
#define FAILTHOWEXP "JNI call ThrowNew failed to throw exception !"
#define SCOMEXPCLASS "com/embeddedunveiled/serial/SerialComException"
#define E_UNKNOWN "Unknown error occurred !"
#define E_FINDCLASSSCOMEXPSTR "Can not find class com/embeddedunveiled/serial/SerialComException, Probably out of memory."
#define E_NEWSTRUTFSTR "JNI call NewStringUTF failed !"
#define E_GETSTRUTFCHARSTR "JNI call GetStringUTFChars failed !"
#define E_GETINTARRELEMTSTR "JNI call GetIntArrayElements failed !"
#define E_NEWINTARRAYSTR "JNI call NewIntArray failed !"
#define E_SETINTARRREGIONSTR "JNI call SetIntArrayRegion failed. Probably index out of bound !"
#define E_NEWBYTEARRAYSTR "JNI call NewByteArray failed !"
#define E_SETBYTEARRREGIONSTR "JNI call SetByteArrayRegion failed !"

/* function prototypes (declared in reverse order of use) */
int LOGE(const char *msga, const char *msgb);
int LOGEN(const char *msga, const char *msgb, unsigned int error_num);
void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *);

#endif /* SCM_CP210XMANUFACTURING_H_ */
