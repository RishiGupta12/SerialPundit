////////////////////////////////////////////////////////////////////////////////
// Types.h
////////////////////////////////////////////////////////////////////////////////

#ifndef TYPES_H
#define TYPES_H

////////////////////////////////////////////////////////////////////////////////
// Typedefs
////////////////////////////////////////////////////////////////////////////////

typedef int BOOL;
typedef unsigned char BYTE;
typedef unsigned short WORD;
typedef unsigned int UINT;
typedef unsigned int DWORD;

typedef char *LPSTR;
typedef const char* LPCSTR;
typedef BOOL *LPBOOL;
typedef BYTE *LPBYTE;
typedef WORD *LPWORD;
typedef UINT *LPUINT;
typedef DWORD *LPDWORD;
typedef void *LPVOID;

typedef void *HANDLE;
typedef HANDLE *LPHANDLE;

////////////////////////////////////////////////////////////////////////////////
// Definitions
////////////////////////////////////////////////////////////////////////////////

#define INFINITE					0xFFFFFFFF
#define TRUE                                            1
#define FALSE                                           0

#define MIN(a,b)                                        ((a)<(b)?(a):(b))
#define MAX(a,b)                                        ((a)>(b)?(a):(b))

#define MAKEWORD(a,b)                                   ((WORD)(((BYTE)(a))|(((WORD)((BYTE)(b)))<<8)))
#define LOWORD(l)                                       ((WORD)(l))
#define HIWORD(l)                                       ((WORD)(((DWORD)(l) >> 16) & 0xFFFF))
#define LOBYTE(w)                                       ((BYTE)(w))
#define HIBYTE(w)                                       ((BYTE)(((WORD)(w)>>8)&0xFF))

#define MAX_PATH                                        512
#define CALLBACK

#endif // TYPES_H
