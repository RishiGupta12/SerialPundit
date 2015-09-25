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

#ifndef SCM_FTDI_D2XX_LIB_H_
#define SCM_FTDI_D2XX_LIB_H_

#include <jni.h>
#include "WinTypes.h"    /* FTDI D2XX header file from vendor   */
#include "ftd2xx.h"      /* FTDI D2XX header file from vendor   */

/* these constants (SCM_) values must match their values in java layer */

#define SCM_DEVICE_UNKNOWN  0X01
#define SCM_DEVICE_AM       0x02
#define SCM_DEVICE_BM       0x03
#define SCM_DEVICE_100AX    0X04
#define SCM_DEVICE_232B     0X05
#define SCM_DEVICE_232R     0X06
#define SCM_DEVICE_232H     0X07
#define SCM_DEVICE_2232C    0X08
#define SCM_DEVICE_2232H    0X09
#define SCM_DEVICE_4232H    0X10
#define SCM_DEVICE_X_SERIES 0X11

#define SCM_OPEN_BY_SERIAL_NUMBER 0x08
#define SCM_OPEN_BY_DESCRIPTION   0x10
#define SCM_OPEN_BY_LOCATION      0x20

#define SCM_GENERIC_READ  0x01
#define SCM_GENERIC_WRITE 0x02

#define SCM_SETRTS      1
#define SCM_CLRRTS      2
#define SCM_SETDTR      3
#define SCM_CLRDTR      4
#define SCM_SETBREAK    5
#define SCM_CLRBREAK    6

#define SCM_MS_CTS_ON   0x01
#define SCM_MS_DSR_ON   0x02
#define SCM_MS_RING_ON  0x04
#define SCM_MS_RLSD_ON  0x08

#define SCM_EV_RXCHAR   0x0001
#define SCM_EV_RXFLAG   0x0002
#define SCM_EV_TXEMPTY  0x0004
#define SCM_EV_CTS      0x0008
#define SCM_EV_DSR      0x0010
#define SCM_EV_RLSD     0x0020
#define SCM_EV_BREAK    0x0040
#define SCM_EV_ERR      0x0080
#define SCM_EV_RING     0x0100
#define SCM_EV_PERR     0x0200
#define SCM_EV_RX80FULL 0x0400
#define SCM_EV_EVENT1   0x0800
#define SCM_EV_EVENT2   0x1000

#define SCM_PURGE_TXABORT  0x0001
#define SCM_PURGE_RXABORT  0x0002
#define SCM_PURGE_TXCLEAR  0x0004
#define SCM_PURGE_RXCLEAR  0x0008

#define SCM_CE_RXOVER   0x0001
#define SCM_CE_OVERRUN  0x0002
#define SCM_CE_RXPARITY 0x0004
#define SCM_CE_FRAME    0x0008
#define SCM_CE_BREAK    0x0010
#define SCM_CE_TXFULL   0x0100
#define SCM_CE_PTO      0x0200
#define SCM_CE_IOE      0x0400
#define SCM_CE_DNS      0x0800
#define SCM_CE_OOP      0x1000
#define SCM_CE_MODE     0x8000

/* Constant string defines */
#define FAILTHOWEXP "JNI call ThrowNew failed to throw exception !"
#define SCOMEXPCLASS "com/embeddedunveiled/serial/SerialComException"
#define JAVALSTRING "java/lang/String"

#define E_UNKNOWN "Unknown error occurred !"
#define E_ENBLPARCHKSTR "Parity checking in configureComPortData method needs to be enabled first !"
#define E_GETJVMSTR "JNI call GetJavaVM failed !"
#define E_FINDCLASSSCOMEXPSTR "Can not find class com/embeddedunveiled/serial/SerialComException, Probably out of memory."
#define E_FINDCLASSSSTRINGSTR "Can not find class java/lang/String, Probably out of memory !"
#define E_NEWOBJECTARRAYSTR "JNI call NewObjectArray failed. Probably out of memory !"
#define E_NEWBYTEARRAYSTR "JNI call NewByteArray failed !"
#define E_NEWINTARRAYSTR "JNI call NewIntArray failed !"
#define E_NEWLONGARRAYSTR "JNI call NewLongArray failed !"
#define E_SETOBJECTARRAYSTR "JNI call SetObjectArrayElement failed. Either index violation or wrong class used !"
#define E_SETBYTEARRREGIONSTR "JNI call SetByteArrayRegion failed. Probably index out of bound !"
#define E_SETINTARRREGIONSTR "JNI call SetIntArrayRegion failed. Probably index out of bound !"
#define E_SETLONGARRREGIONSTR "JNI call SetLongArrayRegion failed. Probably index out of bound !"
#define E_SETCHARARRREGIONSTR "JNI call SetCharArrayRegion failed. Probably index out of bound !"
#define E_NEWSTRUTFSTR "JNI call NewStringUTF failed !"
#define E_GETSTRUTFCHARSTR "JNI call GetStringUTFChars failed !"
#define E_GETBYTEARRELEMTSTR "JNI call GetByteArrayElements failed !"
#define E_GETBYTEARRREGIONSTR "JNI call GetByteArrayRegion failed !"
#define E_GETINTARRELEMTSTR "JNI call GetIntArrayElements failed !"
#define E_NEWGLOBALREFSTR "JNI Call NewGlobalRef failed !"
#define E_DELGLOBALREFSTR "JNI Call DeleteGlobalRef failed !"
#define E_CALLOCSTR "calloc() failed to allocate requested memory !"
#define E_REALLOCSTR "realloc() failed to allocate requested memory !"
#define E_ATTACHCURRENTTHREADSTR "JNI call AttachCurrentThread failed !"
#define E_GETOBJECTCLASSSTR "JNI call GetObjectClass failed !"
#define E_GETMETHODIDSTR "JNI call GetMethodID failed !"
#define E_DETACHCURTHREAD "JNI call DetachCurrentThread failed !"
#define E_SIGNALINSTFAILSTR "Failed to install signal handler !"
#define E_CALLVOIDMETHDSTR "JNI call CallVoidMethod failed !"
#define E_UDEVNEWSTR "Could not create udev context !"
#define E_UDEVNETLINKSTR "Could not create udev monitor !"
#define E_IOSRVMATUSBDEVSTR "Function call IOServiceMatching('IOUSBDevice') failed !"
#define E_GETDIRCTBUFADDRSTR "JNI call GetDirectBufferAddress failed !"
#define E_VIOVNTINVALIDSTR "The length of data supplied exceeds maximum limit !"
#define E_IllegalARG "Illegal argument !"
#define E_INVALIDHANDLE "INVALID_HANDLE_VALUE !"

#define E_SETRTS "Could not set RTS line !"
#define E_CLRRTS "Could not clear RTS line !"
#define E_SETDTR "Could not set DTR line !"
#define E_CLRDTR "Could not clear DTR line !"
#define E_SETBREAK "Could not set line break !"
#define E_CLRBREAK "Could not clear line break !"
#define E_GETMODEMSTATUS "Could not get modem status !"
#define E_SETUPCOMM "Could not set the input/output buffer size !"
#define E_SETUPCOMMSTATE "Could not set the state from DCB structure !"
#define E_GETUPCOMMSTATE "Could not get the DCB structure !"
#define E_SETCOMMTIMEOUTS "Could not set the timeout values !"
#define E_GETCOMMTIMEOUTS "Could not get the timeout values !"
#define E_SETCOMMBREAK "Could not put the communications line in the BREAK state !"
#define E_CLEARCOMMBREAK "Could not put the communications line in non-BREAK state !"
#define E_SETCOMMMASK "Could not set given event mask !"
#define E_GETCOMMMASK "Could not get the event mask !"
#define E_PURGECOMM "Could not purge the port !"
#define E_GETLASTERROR "Could not get the last error value !"
#define E_CLEARCOMMERROR "Could not get the error and status information !"
#define E_W32WRITEFILE "Could not write given data to device !"
#define E_W32READFILE "Could not write given data to device !"
#define E_WAITCOMEVENT "Could not wait for an event to occur !"


/* This holds information for implementing dynamically growing array in C language. */
struct jstrarray_list {
	jstring *base;      /* pointer to an array of pointers to string */
	int index;         /* array element index                       */
	int current_size;  /* size of this array                        */
};

/* function prototypes (declared in reverse order of use) */
int LOGE(const char *msga, const char *msgb);
int LOGEN(const char *msga, const char *msgb, unsigned int error_num);
void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *);
void free_jstrarraylist(struct jstrarray_list *al);
void insert_jstrarraylist(struct jstrarray_list *al, jstring element);
void init_jstrarraylist(struct jstrarray_list *al, int initial_size);

#endif /* SCM_FTDI_D2XX_LIB_H_ */
