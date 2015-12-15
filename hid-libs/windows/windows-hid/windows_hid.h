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

#ifndef WINDOWS_HID_H_
#define WINDOWS_HID_H_

#pragma once
#pragma comment (lib, "Setupapi.lib")
#pragma comment (lib, "cfgmgr32.lib")
#pragma comment (lib, "Hid.lib")

#include "stdafx.h"
#include <tchar.h>
#include <setupapi.h>
#include <Hidsdi.h>
#include <Hidpi.h>
#include <jni.h>

#define MANUFACTURER_STRING   0X01
#define PRODUCT_STRING        0X02
#define SERIAL_STRING         0x03

/* Constant string defines */
#define SCOMEXPCLASS "com/embeddedunveiled/serial/SerialComException"
#define JAVALSTRING "java/lang/String"
#define E_NEWGLOBALREFSTR "JNI Call NewGlobalRef failed !"
#define E_GETSTRUTFCHARSTR "JNI call GetStringUTFChars failed !"
#define E_GETSTRCHARSTR "JNI call GetStringChars failed !"
#define FAILTHOWEXP "JNI call ThrowNew failed to throw exception !"
#define E_FINDCLASSSCOMEXPSTR "Can not find class com/embeddedunveiled/serial/SerialComException. Probably out of memory !"
#define E_GETBYTEARRELEMTSTR "JNI call GetByteArrayElements failed !"
#define E_GETBYTEARRREGIONSTR "JNI call GetByteArrayRegion failed !"
#define E_MALLOCSTR "malloc() failed to allocate requested memory !"
#define E_NEWSTRUTFSTR "JNI call NewStringUTF failed !"
#define E_NEWSTR "JNI call NewString failed !"
#define E_FINDCLASSSSTRINGSTR "Can not find class java/lang/String. Probably out of memory !"
#define E_NEWOBJECTARRAYSTR "JNI call NewObjectArray failed. Probably out of memory !"
#define E_SETOBJECTARRAYSTR "JNI call SetObjectArrayElement failed. Either index violation or wrong class used !"
#define E_SETBYTEARRAYREGION "JNI call SetByteArrayRegion failed. Probably index out of bound !"
#define E_NEWBYTEARRAYSTR "JNI call NewByteArray failed !"
#define E_SETBYTEARRREGIONSTR "JNI call SetByteArrayRegion failed !"
#define E_CALLOCSTR "calloc() failed to allocate requested memory !"
#define E_MALLOCSTR "malloc() failed to allocate requested memory !"

#define E_INDEXSTR "Could not get the indexed string at given index !"
#define E_SERIALNUMBER "Could not get the serial number of given device !"
#define E_PRODUCT "Could not get product information of given device !"
#define E_MANUFACTURER "Could not get manufacturer name of given device !"
#define E_GETFEATUREREPORT "Could not get feature report from given device !"
#define E_SETFEATUREREPORT "Could not send the feature report to given device !"
#define E_REPORTINBUFSIZE "Could not set the input report ring buffer size !"
#define E_PARSEDDATA "Could not get pre-parsed data for this device !"
#define E_DEVCAPABILITIES "Could not get capabilities of this device !"
#define E_INVALIDOUTLEN "Length of given report is greater than the maximum output report length for this device !"
#define E_INVALIDFETLEN "Length of given report is greater than the maximum feature report length for this device !"
#define E_INVALIDINLEN "Length of given report buffer is smaller than the size required for this input report !"
#define E_FLUSHIN "Could not flush the input report buffer. Please retry !"
#define E_PHYSICALDESC "Could not get physical descriptor. Please retry !"
#define E_HidDGetInputReport "Could not fetch input report. Please retry !"
#define E_HidDSetOutputReport "Could not send output report. Please retry !"

#define EXP_UNBLOCKHIDIO "I/O operation unblocked !"

/* Holds information about a HID device */
struct hid_dev_info {
	HANDLE handle;                      /* specifies an open handle to a top-level collection. */
	TCHAR instance[1024];                /* device instance of this HID device */
	OVERLAPPED *overlapped;             /* used for blocking I/O context only */
	PHIDP_PREPARSED_DATA parsed_data;
	HIDP_CAPS collection_capabilities;
};

/* Associate a hid device instance with its hardware ID */
struct hiddev_inst_cont_id {
	TCHAR instance[512];
	TCHAR hwid[512];
};

/* Holds information for implementing dynamically growing array of structures in C language. */
struct hiddev_instance_list {
	struct hiddev_inst_cont_id **base; /* pointer to an array of pointers to structure */
	int index;                         /* array element index                          */
	int current_size;                  /* size of this array                           */
};

/* Holds information for implementing dynamically growing array of jstring in C language. */
struct jstrarray_list {
	jstring *base;      /* pointer to an array of pointers to string */
	int index;          /* array element index                       */
	int current_size;   /* size of this array                        */
};

/* function prototypes (declared in reverse order of use) */
int LOGE(const char *msga, const char *msgb);
int LOGEN(const char *msga, const char *msgb, unsigned int error_num);
void throw_serialcom_exception(JNIEnv *env, int type, int error_code, const char *);

void free_jstrarraylist(struct jstrarray_list *al);
int insert_jstrarraylist(struct jstrarray_list *al, jstring element);
int init_jstrarraylist(struct jstrarray_list *al, int initial_size);

void free_hiddev_instance_list(struct hiddev_instance_list *al);
int insert_hiddev_instance_list(struct hiddev_instance_list *al, struct hiddev_inst_cont_id *element);
int init_hiddev_instance_list(struct hiddev_instance_list *al, int initial_size);

jstring clean_throw_exp_usbenumeration(JNIEnv *env, int task, int subtask, DWORD error_code, const char *expmsg, struct jstrarray_list *list, struct hiddev_instance_list *hiddevinst_list, HDEVINFO *usb_dev_info_set, HDEVINFO *hid_dev_info_set);
jobjectArray enumerate_usb_hid_devices(JNIEnv *env, jint vendor_filter);

jstring find_driver_for_given_hiddevice(JNIEnv *env, jstring hidDevNode);
jstring get_hiddev_info_string(JNIEnv *env, jlong handle, int info_required);

#endif /* WINDOWS_HID_H_ */

