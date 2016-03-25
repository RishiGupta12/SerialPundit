/************************************************************************************************
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************************************/
 
/* 
 * Virtual multi-port serial adaptor :
 *
 * The adaptor itself and the tty ports created by this adaptor, both are virtual.
 *
 * This driver implements a virtual multiport serial adaptor in such that the virtual adaptor can have 
 * from 0 to N number of virtual serial(tty) ports. This adaptor presents itself as miscellaneous device 
 * to kernel and user space applications can communicate with this adaptor through /dev/scm_vadaptor device 
 * node. The virtual tty ports created by this adaptor are used in exactly same way as real tty devices.
 */
 
#include <linux/kernel.h>
#include <linux/errno.h>
#include <linux/init.h>
#include <linux/module.h>
#include <linux/slab.h>
#include <linux/wait.h>
#include <linux/tty.h>
#include <linux/tty_driver.h>
#include <linux/tty_flip.h>
#include <linux/serial.h>
#include <linux/sched.h>
#include <linux/version.h>
#include <linux/spinlock.h>
#include <linux/miscdevice.h>
#include <asm/uaccess.h>

/* Module information */
#define DRIVER_VERSION "v1.0"
#define DRIVER_AUTHOR "Rishi Gupta"
#define DRIVER_DESC "Serial port null modem emulator driver (kernel mode)"

static int scm_vadaptor_open(struct inode *inode, struct file *file);
static int scm_vadaptor_close(struct inode *inode, struct file *file);
static ssize_t scm_vadaptor_read(struct file *file, char __user *buf, size_t size, loff_t *off);
static ssize_t scm_vadaptor_write(struct file *file, const char __user *buf, size_t size, loff_t *off);

/* Number of virtual tty ports this driver is going to handle */
#define SCMTTY_NUM_DEVICES 4096

/* Experimental range (major number of devices) */
#define SCMTTY_MAJOR 240

/* Modem control lines definitions */
#define SCM_MCR_DTR		0x01
#define SCM_MCR_RTS		0x02
#define SCM_MCR_LOOP	0x04
#define SCM_MSR_CTS		0x08
#define SCM_MSR_CD		0x10
#define SCM_MSR_RI		0x20
#define SCM_MSR_DSR		0x40

struct scmtty_dev_private {
     struct tty_struct *tty; /* pointer to the tty structure for this device */
     spinlock_t lock;
     int open_count;         /* number of times this port has been opened */
};

/* Information asscociated with individual tty port is stored in scmtty_dev_private structure. 
 * When a device pair is created, two scmtty_dev_private structure are allocated and pointers 
 * to them are stored in this array for later use whenever required. */
struct scmtty_private *scmttyinfo[SCMTTY_NUM_DEVICES];

static struct tty_driver *scmtty_driver;

static int scm_vadaptor_open(struct inode *inode, struct file *file)
{
}

static int scm_vadaptor_close(struct inode *inode, struct file *file)
{
}

static ssize_t scm_vadaptor_read(struct file *file, char __user *buf, size_t size, loff_t *off)
{
}

static ssize_t scm_vadaptor_write(struct file *file, const char __user *buf, size_t size, loff_t *off)
{
}

static const struct tty_operations scm_serial_ops = {
	.open            = scmtty_open,
	.close           = scmtty_close,
	.write           = scmtty_write,
	.put_char        = scmtty_put_char,
	.flush_chars     = scmtty_flush_chars,
	.write_room      = scmtty_write_room,
	.chars_in_buffer = scmtty_chars_in_buffer,
	.ioctl           = scmtty_ioctl,
	.set_termios     = scmtty_set_termios,
	.throttle        = scmtty_throttle,
	.unthrottle      = scmtty_unthrottle,
	.stop            = scmtty_stop,
	.start           = scmtty_start,
	.hangup          = scmtty_hangup,
	.break_ctl       = scmtty_break,
	.flush_buffer    = scmtty_flush_buffer,
	.wait_until_sent = scmtty_wait_until_sent,
	.send_xchar      = scmtty_send_xchar,
	.tiocmget        = scmtty_tiocmget,
	.tiocmset        = scmtty_tiocmset,
	.get_icount      = scmtty_get_icount,
};

static const struct file_operations scm_vadaptor_fops = {
    .owner       = THIS_MODULE,
    .write       = scm_vadaptor_write,
    .read        = scm_vadaptor_read,
    .open        = scm_vadaptor_open,
    .release     = scm_vadaptor_close,
    .llseek      = no_llseek,
};

/* A minor number is assigned and placed in the minor field of the structure.
 * Access this device through /dev/scm_vadaptor device node */
static struct miscdevice scm_vadaptor = {
    .minor       = MISC_DYNAMIC_MINOR,
    .name        = "scm_vadaptor",
    .fops        = &scm_vadaptor_fops,
    .nodename    = "scm_vadaptor",
    .mode        = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH,
};
 
static int __init scm_init(void)
{
	int ret = 0;
		
	scmtty_driver = tty_alloc_driver(SCMTTY_NUM_DEVICES, );
	if (!scmtty_driver)
    	return -ENOMEM;
    
    scmtty_driver->owner = THIS_MODULE;
    scmtty_driver->driver_name = "ttynullcomkm";
    scmtty_driver->name = "scmtty";
    scmtty_driver->major = SCMTTY_MAJOR;
    scmtty_driver->type = TTY_DRIVER_TYPE_SERIAL;
	scmtty_driver->subtype = SERIAL_TYPE_NORMAL;
	scmtty_driver->init_termios = tty_std_termios;
	scmtty_driver->init_termios.c_cflag = B9600 | CS8 | CREAD | HUPCL | CLOCAL;
	scmtty_driver->init_termios.c_ispeed = 9600;
	scmtty_driver->init_termios.c_ospeed = 9600;
    scmtty_driver->flags = TTY_DRIVER_DYNAMIC_DEV | TTY_DRIVER_RESET_TERMIOS | TTY_DRIVER_REAL_RAW;
    
    tty_set_operations(scmtty_driver, &scm_serial_ops);
    
	ret = tty_register_driver(scmtty_driver);
	if (ret) {
		put_tty_driver(scmtty_driver);
		return ret;
	}
    
	ret = misc_register(&scm_vadaptor);
	if (ret) {
		tty_unregister_driver(scmtty_driver);
		put_tty_driver(scmtty_driver);
		return ret;
	}
	
    printk(KERN_INFO "%s %s %s\n", "ttynullcomkm:", DRIVER_DESC, DRIVER_VERSION);
    return 0;
}
 
static void __exit scm_exit(void)
{
	tty_unregister_driver(scmtty_driver);
	put_tty_driver(scmtty_driver);	
	misc_deregister(&scm_vadaptor);
}
 
module_init(scm_init);
module_exit(scm_exit);

MODULE_AUTHOR( DRIVER_AUTHOR );
MODULE_DESCRIPTION( DRIVER_DESC );
MODULE_LICENSE("AGPL");
MODULE_VERSION( DRIVER_VERSION );


