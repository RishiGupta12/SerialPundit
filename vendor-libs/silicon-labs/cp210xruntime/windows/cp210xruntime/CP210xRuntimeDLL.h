
// The following ifdef block is the standard way of creating macros which make exporting
// from a DLL simpler. All files within this DLL are compiled with the CP210xRUNTIMEDLL_EXPORTS
// symbol defined on the command line. this symbol should not be defined on any project
// that uses this DLL. This way any other project whose source files include this file see
// CP210xRUNTIMEDLL_API functions as being imported from a DLL, wheras this DLL sees symbols
// defined with this macro as being exported.
#ifdef CP210xRUNTIMEDLL_EXPORTS
#define CP210xRUNTIMEDLL_API __declspec(dllexport)
#else
#define CP210xRUNTIMEDLL_API __declspec(dllimport)
#endif

#ifndef _CP210x_STANDARD_DEF_
#define _CP210x_STANDARD_DEF_

// GetDeviceVersion() return codes
#define     CP210x_CP2101_VERSION               0x01
#define     CP210x_CP2102_VERSION               0x02
#define     CP210x_CP2103_VERSION               0x03
#define		CP210x_CP2104_VERSION				0x04
#define		CP210x_CP2105_VERSION				0x05
#define		CP210x_CP2108_VERSION				0x08
#define		CP210x_CP2109_VERSION				0x09

// Return codes
#define     CP210x_SUCCESS                      0x00
#define     CP210x_DEVICE_NOT_FOUND             0xFF
#define     CP210x_INVALID_HANDLE               0x01
#define     CP210x_INVALID_PARAMETER            0x02
#define     CP210x_DEVICE_IO_FAILED             0x03
#define     CP210x_FUNCTION_NOT_SUPPORTED       0x04
#define     CP210x_GLOBAL_DATA_ERROR            0x05
#define     CP210x_FILE_ERROR                   0x06
#define     CP210x_COMMAND_FAILED               0x08
#define     CP210x_INVALID_ACCESS_TYPE          0x09

// Type definitions
typedef     int     CP210x_STATUS;

// Buffer size limits
#define     CP210x_MAX_PRODUCT_STRLEN           126
#define     CP210x_MAX_SERIAL_STRLEN            63

// Type definitions
typedef     char    CP210x_PRODUCT_STRING[CP210x_MAX_PRODUCT_STRLEN];
typedef     char    CP210x_SERIAL_STRING[CP210x_MAX_SERIAL_STRLEN];

// Mask and Latch value bit definitions
#define     CP210x_GPIO_0                       0x0001
#define     CP210x_GPIO_1                       0x0002
#define     CP210x_GPIO_2                       0x0004
#define     CP210x_GPIO_3                       0x0008
#define     CP210x_GPIO_4                       0x0010
#define     CP210x_GPIO_5                       0x0020
#define     CP210x_GPIO_6                       0x0040
#define     CP210x_GPIO_7                       0x0080
#define     CP210x_GPIO_8                       0x0100
#define     CP210x_GPIO_9                       0x0200
#define     CP210x_GPIO_10                      0x0400
#define     CP210x_GPIO_11                      0x0800
#define     CP210x_GPIO_12                      0x1000
#define     CP210x_GPIO_13                      0x2000
#define     CP210x_GPIO_14                      0x4000
#define     CP210x_GPIO_15                      0x8000

#ifdef __cplusplus
extern "C" {
#endif // __cplusplus

CP210xRUNTIMEDLL_API CP210x_STATUS WINAPI
CP210xRT_ReadLatch( HANDLE cyHandle,
                    LPWORD  lpLatch);

CP210xRUNTIMEDLL_API CP210x_STATUS WINAPI
CP210xRT_WriteLatch(HANDLE cyHandle,
                    WORD    mask,
                    WORD    latch);


CP210xRUNTIMEDLL_API CP210x_STATUS WINAPI
CP210xRT_GetPartNumber(HANDLE cyHandle,
                       LPBYTE   lpbPartNum);

CP210xRUNTIMEDLL_API CP210x_STATUS WINAPI
CP210xRT_GetDeviceProductString(HANDLE  cyHandle,
                                LPVOID  lpProduct,
                                LPBYTE  lpbLength,
                                BOOL    bConvertToASCII = TRUE
                                );

CP210xRUNTIMEDLL_API CP210x_STATUS WINAPI
CP210xRT_GetDeviceSerialNumber(HANDLE   cyHandle,
                               LPVOID   lpSerialNumber,
                               LPBYTE   lpbLength,
                               BOOL     bConvertToASCII = TRUE
                               );

CP210xRUNTIMEDLL_API CP210x_STATUS WINAPI
CP210xRT_GetDeviceInterfaceString(HANDLE	cyHandle,
                                  LPVOID lpInterfaceString,
                                  LPBYTE lpbLength,
                                  BOOL bConvertToASCII);

#ifdef __cplusplus
}
#endif // __cplusplus

#endif // _CP210x_STANDARD_DEF_
