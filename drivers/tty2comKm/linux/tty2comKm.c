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
 * This driver implements a virtual multiport serial adaptor in such a way that the virtual adaptor can have 
 * from 0 to N number of virtual serial(tty) ports. The virtual tty ports created by this adaptor are used in 
 * exactly same way as real tty devices.
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
#include <asm/uaccess.h>
#include <linux/proc_fs.h>
#include <linux/seq_file.h>

/* Module information */
#define DRIVER_VERSION "v1.0"
#define DRIVER_AUTHOR "Rishi Gupta"
#define DRIVER_DESC "Serial port null modem emulator driver (kernel mode)"

static int scmtty_open(struct tty_struct * tty, struct file * filp);
static int scmtty_write(struct tty_struct * tty, const unsigned char *buf, int count);
static int scmtty_put_char(struct tty_struct *tty, unsigned char ch);
static int scmtty_break_ctl(struct tty_struct *tty, int state);
static int scmtty_write_room(struct tty_struct *tty);
static int scmtty_chars_in_buffer(struct tty_struct *tty);
static int scmtty_ioctl(struct tty_struct *tty, unsigned int cmd, unsigned long arg);
static int scmtty_tiocmget(struct tty_struct *tty);
static int scmtty_tiocmset(struct tty_struct *tty, unsigned int set, unsigned int clear);
static int scmtty_get_icount(struct tty_struct *tty, struct serial_icounter_struct *icount);

static void scmtty_set_termios(struct tty_struct *tty, struct ktermios * old);
static void scmtty_throttle(struct tty_struct * tty);
static void scmtty_unthrottle(struct tty_struct * tty);
static void scmtty_stop(struct tty_struct *tty);
static void scmtty_start(struct tty_struct *tty);
static void scmtty_hangup(struct tty_struct *tty);
static void scmtty_flush_chars(struct tty_struct *tty);
static void scmtty_flush_buffer(struct tty_struct *tty);
static void scmtty_wait_until_sent(struct tty_struct *tty, int timeout);
static void scmtty_send_xchar(struct tty_struct *tty, char ch);
static void scmtty_close(struct tty_struct * tty, struct file * filp);

/* Default number of virtual tty ports this driver is going to support.
 * TTY devices are created on demand. */
#define VTTY_DEV_MAX 128

/* Experimental range (major number of devices) */
#define SCM_VTTY_MAJOR 240

/* Modem control lines definitions */
#define SCM_MCR_DTR    0x01
#define SCM_MCR_RTS    0x02
#define SCM_MCR_LOOP   0x04
#define SCM_MSR_CTS    0x08
#define SCM_MSR_CD     0x10
#define SCM_MSR_RI     0x20
#define SCM_MSR_DSR    0x40

/* Represent a virtual tty device in virtual adaptor */
struct vtty_dev {
    ushort own_index;
    ushort peer_index;
    ushort msr;  // modem status register
    ushort mcr;  // modem control register
    spinlock_t lock;
    int open_count;
    struct tty_struct *own_tty;
    struct tty_struct *peer_tty;
    struct serial_struct serial;
    struct serial_icounter_struct icount;
};

/* These values may be overriden if module is loaded with parameters */
static ushort max_num_vtty_dev = VTTY_DEV_MAX;
static ushort init_num_nm_pair = VTTY_DEV_MAX;
static ushort init_num_lb_dev = VTTY_DEV_MAX;

/* Describes this driver kernel module */
static struct tty_driver *scmtty_driver;

/* Used when creating or destroying virtual tty devices */
DEFINE_SPINLOCK(adaptlock);  // atomically create/destroy tty devices
int *index_counter = NULL;  //  keep track of indexes in use currently 

/*
 * Invoked when open() is called on a serial port's device node. The tty layr will allocate a 
 * 'struct tty_struct' for this device, allocate and setup various structures and line disc and 
 * call this function.
 * 
 * @tty: pointer to the tty_struct structure assigned to this device
 * @filp: file pointer to tty
 * 
 * @return 0 on success or negative error code on failure.
 */
static int scmtty_open(struct tty_struct * tty, struct file * filp)
{
    printk(KERN_INFO "rishi : %d\n", tty->index);
    //http://lxr.free-electrons.com/source/drivers/tty/tty_port.c#L568
    //http://lxr.free-electrons.com/source/net/irda/ircomm/ircomm_tty.c#L84
    return 0;
}

/*
 * Invoked by tty layer when release() is called on the file pointer that was previously created with a 
 * call to open().
 * 
 * @tty:
 * @filp:
 * 
 * @return 0 on success or negative error code on failure.
 */
static void scmtty_close(struct tty_struct * tty, struct file * filp)
{

}

/* 
 * Invoked by tty layer when data is to be sent to tty device may be as a response to write() call in 
 * user space.
 * 
 * @tty:
 * @buf:
 * @count:
 * 
 * @return number of characters consumed (sent to tty device) or negative error code on failure.
 */
static int scmtty_write(struct tty_struct * tty, const unsigned char *buf, int count)
{
    return 0;
}

/* 
 * Invoked by tty layer when a single character is to be sent to the tty device. This character may be 
 * ignored if there is no room in the device for the character to be sent.
 * 
 * @tty:
 * @buf:
 * @count:
 * 
 * @return number of characters consumed (sent to tty device) or negative error code on failure.
 */
static int scmtty_put_char(struct tty_struct *tty, unsigned char ch)
{
    return 0;
}

/*
 * Invoked by tty layer indicating that the driver should inform tty device to start transmitting data out 
 * of serial port physically.
 * 
 * @tty:
 */
static void scmtty_flush_chars(struct tty_struct *tty)
{

}

/*
 * Return the number of bytes that can be queued to this device at the present time. The result should be 
 * treated as a guarantee and the driver cannot offer a value it later shrinks by more than the number of 
 * bytes written.
 * 
 * @tty:
 * 
 * @return 
 */
static int scmtty_write_room(struct tty_struct *tty)
{
    return 0;
}

/*
 * Return the number of bytes of data in the device private output queue. Invoked when ioctl command TIOCOUTQ 
 * is executed or by tty layer as and when required (tty_wait_until_sent()).
 * 
 * @tty:
 * 
 * @return 
 */
static int scmtty_chars_in_buffer(struct tty_struct *tty)
{
    return 0;
}

/*
 * Invoked to execute standard and device/driver specific ioctl commands.
 * 
 * If the requested command is not supported, this driver returns ENOIOCTLCMD so that the tty layer can use 
 * generic version of the ioctl if possible.
 * 
 * @tty:
 * @cmd:
 * @arg:
 * 
 * @return 0 on success otherwise a negative error code on failure.
 */
static int scmtty_ioctl(struct tty_struct *tty, unsigned int cmd, unsigned long arg) 
{
    //wait : https://github.com/projectara/greybus/blob/master/uart.c
    //https://github.com/torvalds/linux/blob/master/drivers/usb/class/cdc-acm.c
    //https://github.com/torvalds/linux/blob/master/drivers/tty/serial/serial_core.c


    //uart line info https://github.com/torvalds/linux/blob/master/drivers/tty/serial/serial_core.c
    // 

    return -ENOIOCTLCMD;
}

/*
 * Invoked when the termios structure (terminal settings) for this tty device is changed.
 * 
 * @tty:
 * @old:
 */
static void scmtty_set_termios(struct tty_struct *tty, struct ktermios * old)
{

}

/*
 * Invoked when tty layer's input buffers are about to get full.
 * 
 * @tty:
 */
static void scmtty_throttle(struct tty_struct * tty) 
{

}

/*
 * Invoked when the tty layer's input buffers have been emptied out, and it now can accept more data.
 * 
 * @tty:
 */
static void scmtty_unthrottle(struct tty_struct * tty) 
{

}

/*
 * Invoked when this driver should stop sending data for example as a part of flow control mechanism.
 * 
 * @tty:
 */
static void scmtty_stop(struct tty_struct *tty) 
{

}

/*
 * Invoked when this driver should resume sending data for example as a part of flow control mechanism.
 * 
 * @tty:
 */
static void scmtty_start(struct tty_struct *tty) 
{

}

/*
 * Invoked by tty layer to inform this driver that it should hangup the tty device.
 * 
 * @tty: 
 */
static void scmtty_hangup(struct tty_struct *tty) 
{

}

/*
 * Invoked by tty layer to turn break condition on and off for a tty device.
 */
static int scmtty_break_ctl(struct tty_struct *tty, int state) 
{
    return 0;
}

/*
 * Discard the internal output buffer for this tty device. Typically it may be called when executing IOCTL 
 * TCOFLUSH, closing the serial port, when break is received in input stream (flushing is configured) or 
 * when hangup occurs. 
 * 
 * On the other hand, when IOCTL command TCIFLUSH is invoked, tty flip buffer and line discipline queue gets 
 * emptied without involvement of tty driver. The driver is generally expected to not to keep data but send 
 * it to tty layer as soon as possible.
 * 
 * @tty:
 */
static void scmtty_flush_buffer(struct tty_struct *tty) 
{

}

/*
 * Invoked by tty layer in response to tcdrain() call.
 * 
 * @tty:
 * @timeout:
 */
static void scmtty_wait_until_sent(struct tty_struct *tty, int timeout) 
{

}

/*
 * Invoked by tty layer to inform this driver to send XON or XOFF character.
 * 
 * @tty:
 * @ch:
 */
static void scmtty_send_xchar(struct tty_struct *tty, char ch) 
{

}

/*
 * Obtain the modem status bits for the given tty device. Invoked typically when ioctl command TIOCMGET 
 * is executed on this tty device.
 * 
 * @return 
 */
static int scmtty_tiocmget(struct tty_struct *tty) 
{
    return 0;
}

/*
 * Set the modem status bits. Invoked typically when ioctl command TIOCMSET is executed on this tty 
 * device.
 * 
 * @tty:
 * @set:
 * @clear:
 * 
 * @return 
 */
static int scmtty_tiocmset(struct tty_struct *tty, unsigned int set, unsigned int clear) 
{
    return 0;
}

/*
 * Invoked to execute ioctl command TIOCGICOUNT to get the number of interrupts.
 * 
 * @tty:
 * @icount:
 */
static int scmtty_get_icount(struct tty_struct *tty, struct serial_icounter_struct *icount) 
{
    //http://lxr.free-electrons.com/source/net/irda/ircomm/ircomm_tty_ioctl.c#L391
    //http://lxr.free-electrons.com/source/drivers/usb/serial/generic.c#L514
    //http://lxr.free-electrons.com/source/drivers/usb/serial/usb-serial.c#L519
    //https://github.com/torvalds/linux/blob/master/drivers/tty/serial/serial_core.c             VIMP
    //https://github.com/martinezjavier/ldd3/blob/master/tty/tiny_tty.c

    return 0;
}

/*
 * @file
 * @buf
 * @length
 * @ppos
 * 
 * @return number of bytes consumed by this function on success or negative error code on error.
 */
static ssize_t scmtty_vadapt_proc_read(struct file *file, char __user *buf, size_t size, loff_t *ppos) 
{
    int ret = 0;
    ret = copy_to_user(buf, "rishi", sizeof("rishi"));//TODO
    return ret;
}

//"gennm-xxxxxx-

/*
 * 1. # echo "gensnm" > /proc/scmtty_vadaptkm
 * 3. # echo "gensnm-xxxxxx" > /proc/scmtty_vadaptkm
 * 2. # echo "genslb" > /proc/scmtty_vadaptkm
 * 4. # echo "genslb-xxxxxx" > /proc/scmtty_vadaptkm
 * 
 * @file
 * @buf
 * @length
 * @ppos
 * 
 * @return number of bytes consumed by this function on success or negative error code on error.
 */
static ssize_t scmtty_vadapt_proc_write(struct file *file, const char __user *buf, size_t length, loff_t *ppos) 
{
    struct device *device;
    char cmd[8];

    if(length != 8) {
        return -EINVAL;
    }

    if(copy_from_user(cmd, buf, 8) != 0) {
        return -EFAULT;
    }

    // The (driver->flags & TTY_DRIVER_DYNAMIC_ALLOC) will evaluate to zero as we are allocating devices 
    // dynamically. This will create a kernel device and register it as appropriate, associating the 
    // device at given index of tty driver.

    spin_lock(&adaptlock);
    spin_unlock(&adaptlock);

    if(cmd[4] == 'x') {
        //TODO INDEX
        device = tty_register_device(scmtty_driver, 0, NULL);
    }else {
        device = tty_register_device(scmtty_driver, 0, NULL);
    }

    return length;
}

/* 
 * Invoked when user space process opens /proc/scmtty_vadaptkm file to create/destroy virtual tty 
 * device(s).
 */
static int proc_show(struct seq_file *m, void *v) {
    return 0;
}
static int scmtty_vadapt_proc_open(struct inode *inode, struct  file *file) {
    return single_open(file, proc_show, NULL);
}

static const struct file_operations scmtty_vadapt_proc_fops = {
        .owner = THIS_MODULE,
        .open = scmtty_vadapt_proc_open,
        .read = scmtty_vadapt_proc_read,
        .write = scmtty_vadapt_proc_write,
        .llseek = seq_lseek,
        .release = single_release,
};

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
        .break_ctl       = scmtty_break_ctl,
        .flush_buffer    = scmtty_flush_buffer,
        .wait_until_sent = scmtty_wait_until_sent,
        .send_xchar      = scmtty_send_xchar,
        .tiocmget        = scmtty_tiocmget,
        .tiocmset        = scmtty_tiocmset,
        .get_icount      = scmtty_get_icount,
};

/* Invoked when this driver is loaded */
static int __init scm_tty2comKm_init(void)
{
    int x = 0;
    int ret = 0;
    struct proc_dir_entry *pde = NULL;

    /* Causes allocation of memory for 'struct tty_port' and 'struct cdev' for all tty devices this 
     * driver can handle. */
    scmtty_driver = tty_alloc_driver(max_num_vtty_dev, 0);
    if (!scmtty_driver)
        return -ENOMEM;

    scmtty_driver->owner = THIS_MODULE;
    scmtty_driver->driver_name = "tty2comKm";
    scmtty_driver->name = "tty2comport";
    scmtty_driver->major = SCM_VTTY_MAJOR,
            scmtty_driver->type = TTY_DRIVER_TYPE_SERIAL,
            scmtty_driver->subtype = SERIAL_TYPE_NORMAL,
            scmtty_driver->flags = TTY_DRIVER_REAL_RAW | TTY_DRIVER_RESET_TERMIOS | TTY_DRIVER_DYNAMIC_DEV,
            scmtty_driver->init_termios = tty_std_termios;
    scmtty_driver->init_termios.c_cflag = B9600 | CS8 | CREAD | HUPCL | CLOCAL;
    scmtty_driver->init_termios.c_ispeed = 9600;
    scmtty_driver->init_termios.c_ospeed = 9600;

    tty_set_operations(scmtty_driver, &scm_serial_ops);

    ret = tty_register_driver(scmtty_driver);
    if (ret) {
        goto failed_register;
    }

    /* A value of -1 means that index is available for use */
    index_counter = (int *) kmalloc((max_num_vtty_dev * sizeof(int)), GFP_KERNEL);
    if(index_counter == NULL) {
        ret = -ENOMEM;
        goto failed_alloc;
    }
    for(x=0; x < max_num_vtty_dev;  x++) {
        index_counter[x] = -1;
    }

    /* Application should read/write to this file to create/destroy tty device and query informations */
    pde = proc_create("scmtty_vadaptkm", 0666, NULL, &scmtty_vadapt_proc_fops);
    if(pde == NULL) {
        ret = -ENOMEM;
        goto failed_proc;
    }

    printk(KERN_INFO "%s %s %s\n", "tty2comKm:", DRIVER_DESC, DRIVER_VERSION);
    return 0;

failed_proc:
    kfree(index_counter);
failed_alloc:
    tty_unregister_driver(scmtty_driver);
failed_register;
    put_tty_driver(scmtty_driver);
    return ret;
}

/* Invoked when this driver is unloaded */
static void __exit scm_tty2comKm_exit(void)
{
    int x = 0;
    int y = sizeof(index_counter) / sizeof(ushort);
    
    remove_proc_entry("scmtty_vadaptkm", NULL);
    
    for(x=0; x <= y;  x++) {
        if(index_counter[x] != -1)
            tty_unregister_device(scmtty_driver, x);
    }
    
    tty_unregister_driver(scmtty_driver);
    put_tty_driver(scmtty_driver);
    
    printk(KERN_INFO "%s\n", "tty2comKm: kernel driver unloaded !");
}

module_init(scm_tty2comKm_init);
module_exit(scm_tty2comKm_exit);

module_param(max_num_vtty_dev, ushort, 0);
MODULE_PARM_DESC(max_num_vtty_dev, "Maximum number of virtual tty devices this driver can create.");

module_param(init_num_nm_pair, ushort, 0);
MODULE_PARM_DESC(init_num_nm_pair, "Number of standard null modem pairs this driver should create at load time.");

module_param(init_num_lb_dev, ushort, 0);
MODULE_PARM_DESC(init_num_lb_dev, "Number of standard loopback tty devices this driver should create at load time.");

MODULE_AUTHOR( DRIVER_AUTHOR );
MODULE_DESCRIPTION( DRIVER_DESC );
MODULE_LICENSE("GPL v3");
MODULE_VERSION( DRIVER_VERSION );

