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
#pragma once

#ifndef WINDOWS_SERIAL_LIB_H_
#define WINDOWS_SERIAL_LIB_H_

#include <jni.h>
#include <windows.h>

/* Error offset added for actual error code reporting by java layer. */
#define ERR_OFFSET 320

/* This is the maximum number of threads and hence data listeners instance we support. */
#define MAX_NUM_THREADS 1024

/* Structure representing data that is passed to each data looper thread
 * with info corresponding to that file descriptor. */
struct looper_thread_params {
	JavaVM *jvm;
	HANDLE hComm;
	HANDLE thread_handle;
	jobject looper;
	int data_enabled;
	int event_enabled;
	int thread_exit;
	CRITICAL_SECTION *csmutex;
	HANDLE wait_event_handles[2];
	int init_done;
};

struct port_info {
	JavaVM *jvm;
	const char *portName;
	HANDLE hComm;
	HANDLE wait_handle;
	int thread_exit;
	jobject port_listener;
	CRITICAL_SECTION *csmutex;
	HWND window_handle;
	JNIEnv* env;
	jclass port_monitor_class;
	jmethodID port_monitor_mid;
	struct port_info *info;
};

/* ERROR MAPPING FROM WINDOWS TO JAVA LAYER
 *
 * This is taken from /usr/include/asm-generic/errno.h and /usr/include/asm-generic/errno-base.h
 * Basically, java layer defines error code and its meaning for this serial library. Now because unix-like OS and Windows
 * has different error numbers, the native library maps OS specific error number to the error number defined by java layer.
 * For the sake of easiness, java layer uses unix-like error numbers which means, only Windows error numbers need to be
 * mapped to java layer specific error numbers. */
#define EPERM		 1	/* Operation not permitted */
#define ENOENT		 2	/* No such file or directory */
#define	 ESRCH		 3	/* No such process */
#define	 EINTR		 4	/* Interrupted system call */
#define EIO		     5	/* I/O error */
#define ENXIO		 6	/* No such device or address */
#define E2BIG		 7	/* Argument list too long */
#define ENOEXEC		 8	/* Exec format error */
#define EBADF		 9	/* Bad file number */
#define ECHILD		10	/* No child processes */
#define	 EAGAIN		11	/* Try again */
#define	 ENOMEM		12	/* Out of memory */
#define	 EACCES		13	/* Permission denied */
#define EFAULT		14	/* Bad address */
#define	 ENOTBLK	15	/* Block device required */
#define	 EBUSY		16	/* Device or resource busy */
#define	 EEXIST		17	/* File exists */
#define	 EXDEV		18	/* Cross-device link */
#define	 ENODEV		19	/* No such device */
#define	 ENOTDIR	20	/* Not a directory */
#define	 EISDIR		21	/* Is a directory */
#define	 EINVAL		22	/* Invalid argument */
#define	 ENFILE		23	/* File table overflow */
#define EMFILE		24	/* Too many open files */
#define	 ENOTTY		25	/* Not a typewriter */
#define	 ETXTBSY	26	/* Text file busy */
#define	 EFBIG		27	/* File too large */
#define	 ENOSPC		28	/* No space left on device */
#define	 ESPIPE		29	/* Illegal seek */
#define EROFS		30	/* Read-only file system */
#define EMLINK		31	/* Too many links */
#define	 EPIPE		32	/* Broken pipe */
#define	 EDOM		33	/* Math argument out of domain of func */
#define	 ERANGE		34	/* Math result not representable */
#define EDEADLK		35	/* Resource deadlock would occur */
#define	 ENAMETOOLONG	36	/* File name too long */
#define	 ENOLCK		37	/* No record locks available */
#define	 ENOSYS		38	/* Function not implemented */
#define	 ENOTEMPTY	39	/* Directory not empty */
#define	 ELOOP		40	/* Too many symbolic links encountered */
#define	 EWOULDBLOCK	EAGAIN	/* Operation would block */
#define ENOMSG		42	/* No message of desired type */
#define EIDRM		43	/* Identifier removed */
#define ECHRNG		44	/* Channel number out of range */
#define	 EL2NSYNC	45	/* Level 2 not synchronized */
#define	 EL3HLT		46	/* Level 3 halted */
#define	 EL3RST		47	/* Level 3 reset */
#define	 ELNRNG		48	/* Link number out of range */
#define	 EUNATCH	49	/* Protocol driver not attached */
#define	 ENOCSI		50	/* No CSI structure available */
#define	 EL2HLT		51	/* Level 2 halted */
#define EBADE		52	/* Invalid exchange */
#define EBADR		53	/* Invalid request descriptor */
#define EXFULL		54	/* Exchange full */
#define	 ENOANO		55	/* No anode */
#define	 EBADRQC	56	/* Invalid request code */
#define	 EBADSLT	57	/* Invalid slot */
#define	 EDEADLOCK	EDEADLK
#define	 EBFONT		59	/* Bad font file format */
#define	 ENOSTR		60	/* Device not a stream */
#define ENODATA		61	/* No data available */
#define	 ETIME		62	/* Timer expired */
#define	 ENOSR		63	/* Out of streams resources */
#define	 ENONET		64	/* Machine is not on the network */
#define	 ENOPKG		65	/* Package not installed */
#define	 EREMOTE	66	/* Object is remote */
#define ENOLINK		67	/* Link has been severed */
#define EADV		68	/* Advertise error */
#define ESRMNT		69	/* Srmount error */
#define	 ECOMM		70	/* Communication error on send */
#define	 EPROTO		71	/* Protocol error */
#define	 EMULTIHOP	72	/* Multihop attempted */
#define	 EDOTDOT	73	/* RFS specific error */
#define	 EBADMSG	74	/* Not a data message */
#define	 EOVERFLOW	75	/* Value too large for defined data type */
#define	 ENOTUNIQ	76	/* Name not unique on network */
#define	 EBADFD		77	/* File descriptor in bad state */
#define EREMCHG		78	/* Remote address changed */
#define	 ELIBACC	79	/* Can not access a needed shared library */
#define ELIBBAD		80	/* Accessing a corrupted shared library */
#define	 ELIBSCN	81	/* .lib section in a.out corrupted */
#define	 ELIBMAX	82	/* Attempting to link in too many shared libraries */
#define ELIBEXEC	83	/* Cannot exec a shared library directly */
#define EILSEQ		84	/* Illegal byte sequence */
#define	 ERESTART	85	/* Interrupted system call should be restarted */
#define	 ESTRPIPE	86	/* Streams pipe error */
#define	 EUSERS		87	/* Too many users */
#define	 ENOTSOCK	88	/* Socket operation on non-socket */
#define	 EDESTADDRREQ	89	/* Destination address required */
#define	 EMSGSIZE	90	/* Message too long */
#define	 EPROTOTYPE	91	/* Protocol wrong type for socket */
#define	 ENOPROTOOPT	92	/* Protocol not available */
#define	 EPROTONOSUPPORT	93	/* Protocol not supported */
#define	 ESOCKTNOSUPPORT	94	/* Socket type not supported */
#define EOPNOTSUPP	95	/* Operation not supported on transport endpoint */
#define	 EPFNOSUPPORT	96	/* Protocol family not supported */
#define	 EAFNOSUPPORT	97	/* Address family not supported by protocol */
#define	 EADDRINUSE	    98	/* Address already in use */
#define	 EADDRNOTAVAIL	99	/* Cannot assign requested address */
#define	 ENETDOWN	    100	/* Network is down */
#define	 ENETUNREACH	101	/* Network is unreachable */
#define ENETRESET	    102	/* Network dropped connection because of reset */
#define	 ECONNABORTED	103	/* Software caused connection abort */
#define ECONNRESET	    104	/* Connection reset by peer */
#define	 ENOBUFS		105	/* No buffer space available */
#define	 EISCONN		106	/* Transport endpoint is already connected */
#define	 ENOTCONN	    107	/* Transport endpoint is not connected */
#define	 ESHUTDOWN	    108	/* Cannot send after transport endpoint shutdown */
#define	 ETOOMANYREFS	109	/* Too many references: cannot splice */
#define	 ETIMEDOUT	    110	/* Connection timed out */
#define	 ECONNREFUSED	111	/* Connection refused */
#define	 EHOSTDOWN	    112	/* Host is down */
#define	 EHOSTUNREACH	113	/* No route to host */
#define EALREADY	    114	/* Operation already in progress */
#define EINPROGRESS   	115	/* Operation now in progress */
#define ESTALE		    116	/* Stale NFS file handle */
#define EUCLEAN		    117	/* Structure needs cleaning */
#define	 ENOTNAM		118	/* Not a XENIX named type file */
#define	 ENAVAIL		119	/* No XENIX semaphores available */
#define	 EISNAM		120	/* Is a named type file */
#define	 EREMOTEIO	121	/* Remote I/O error */
#define	 EDQUOT		122	/* Quota exceeded */
#define	 ENOMEDIUM	123	/* No medium found */
#define	 EMEDIUMTYPE	124	/* Wrong medium type */
#define ECANCELED	125	/* Operation Canceled */
#define ENOKEY		126	/* Required key not available */
#define	 EKEYEXPIRED	127	/* Key has expired */
#define	 EKEYREVOKED	128	/* Key has been revoked */
#define	 EKEYREJECTED	129	/* Key was rejected by service */
/* for robust mutexes */
#define EOWNERDEAD	130	/* Owner died */
#define	 ENOTRECOVERABLE	131	/* State not recoverable */
#define ERFKILL		132	/* Operation not possible due to RF-kill */
#define EHWPOISON	133	/* Memory page has hardware error */

/* Serial library specific additions. */
#define ETOOMANYOP 239

#endif /* WINDOWS_SERIAL_LIB_H_ */
