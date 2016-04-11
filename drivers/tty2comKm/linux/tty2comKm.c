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
 * This driver implements a virtual multiport serial adaptor in such a way that the virtual adaptor 
 * can have from 0 to N number of virtual serial ports (tty devices). The virtual tty devices created 
 * by this adaptor are used in exactly the same way using termios and Linux/Posix APIs as the real tty 
 * devices.
 */

#include <linux/kernel.h>
#include <linux/errno.h>
#include <linux/init.h>
#include <linux/module.h>
#include <linux/moduleparam.h>
#include <linux/slab.h>
#include <linux/wait.h>
#include <linux/tty.h>
#include <linux/tty_driver.h>
#include <linux/tty_flip.h>
#include <linux/serial.h>
#include <linux/sched.h>
#include <linux/version.h>
#include <linux/spinlock.h>
#include <linux/mutex.h>
#include <asm/uaccess.h>
#include <linux/proc_fs.h>
#include <linux/device.h>

/* Module information */
#define DRIVER_VERSION "v1.0"
#define DRIVER_AUTHOR "Rishi Gupta"
#define DRIVER_DESC "Serial port null modem emulation driver (kernel mode)"

/* Default number of virtual tty ports this driver is going to support.
 * TTY devices are created on demand. */
#define VTTY_DEV_MAX 128

/* Experimental range (major number of devices) */
#define SCM_VTTY_MAJOR 240

/* Pin out configurations definitions */
#define SCM_CON_CTS    0x0001
#define SCM_CON_DCD    0x0002
#define SCM_CON_DSR    0x0004
#define SCM_CON_RI     0x0008

/* Modem control register definitions */
#define SCM_MCR_DTR    0x0001
#define SCM_MCR_RTS    0x0002
#define SCM_MCR_LOOP   0x0004

/* Modem status register definitions */
#define SCM_MSR_CTS    0x0008
#define SCM_MSR_DCD    0x0010
#define SCM_MSR_RI     0x0020
#define SCM_MSR_DSR    0x0040

/* UART frame structure definitions */
#define SCM_CRTSCTS       0x0001
#define SCM_XON           0x0002
#define SCM_NONE          0X0004
#define SCM_DATA_5        0X0008
#define SCM_DATA_6        0X0010
#define SCM_DATA_7        0X0020
#define SCM_DATA_8        0X0040
#define SCM_PARITY_NONE   0x0080
#define SCM_PARITY_ODD    0x0100
#define SCM_PARITY_EVEN   0x0200
#define SCM_PARITY_MARK   0x0400
#define SCM_PARITY_SPACE  0x0800
#define SCM_STOP_1        0x1000
#define SCM_STOP_2        0x2000

/* Represent a virtual tty device in this virtual adaptor. The peer_index will contain own 
 * index if this device is loop back configured device (peer == own). */
struct vtty_dev {
    int own_index;
    int peer_index;
    int msr_reg; // shadow modem status register
    int mcr_reg; // shadow modem control register
    int rts_mappings;
    int dtr_mappings;
    int set_dtr_atopen;
    struct mutex lock;
    int is_break_on;
    int baud;
    int uart_frame;
    struct tty_struct *own_tty;
    struct tty_struct *peer_tty;
    struct serial_struct serial;
    struct async_icount icount;
    struct device *device;
};

struct vtty_info {
    int index;
    struct vtty_dev *vttydev;
};

static int scmtty_open(struct tty_struct *tty, struct file *filp);
static int scmtty_write(struct tty_struct *tty, const unsigned char *buf, int count);
static int scmtty_put_char(struct tty_struct *tty, unsigned char ch);
static int scmtty_break_ctl(struct tty_struct *tty, int state);
static int scmtty_write_room(struct tty_struct *tty);
static int scmtty_chars_in_buffer(struct tty_struct *tty);
static int scmtty_ioctl(struct tty_struct *tty, unsigned int cmd, unsigned long arg);
static int scmtty_tiocmget(struct tty_struct *tty);
static int scmtty_tiocmset(struct tty_struct *tty, unsigned int set, unsigned int clear);
static int scmtty_get_icount(struct tty_struct *tty, struct serial_icounter_struct *icount);

static void scmtty_set_termios(struct tty_struct *tty, struct ktermios *old);
static void scmtty_throttle(struct tty_struct *tty);
static void scmtty_unthrottle(struct tty_struct *tty);
static void scmtty_stop(struct tty_struct *tty);
static void scmtty_start(struct tty_struct *tty);
static void scmtty_hangup(struct tty_struct *tty);
static void scmtty_flush_chars(struct tty_struct *tty);
static void scmtty_flush_buffer(struct tty_struct *tty);
static void scmtty_wait_until_sent(struct tty_struct *tty, int timeout);
static void scmtty_send_xchar(struct tty_struct *tty, char ch);
static void scmtty_close(struct tty_struct *tty, struct file *filp);

static int extract_mapping(char data[], int x);
static int update_modem_lines(struct tty_struct *tty, unsigned int set, unsigned int clear);
static int get_serial_info(struct tty_struct *tty, unsigned long arg);
static int wait_msr_change(struct tty_struct *tty, unsigned long mask);
static int check_msr_delta(struct vtty_dev *local_vttydev, unsigned long mask, struct async_icount *prev);

static ssize_t evt_store(struct device *dev, struct device_attribute *attr, const char *buf, size_t count);
static int scmtty_vadapt_proc_open(struct inode *inode, struct  file *file);
static int scmtty_vadapt_proc_close(struct inode *inode, struct file *file);
static ssize_t scmtty_vadapt_proc_read(struct file *file, char __user *buf, size_t size, loff_t *ppos);
static ssize_t scmtty_vadapt_proc_write(struct file *file, const char __user *buf, size_t length, loff_t * ppos);

/* These values may be overriden if module is loaded with parameters */
static ushort max_num_vtty_dev = VTTY_DEV_MAX;
static ushort init_num_nm_pair = 0;
static ushort init_num_lb_dev = 0;

/* Describes this driver kernel module */
static struct tty_driver *scmtty_driver;

/* Used when creating or destroying virtual tty devices */
static DEFINE_SPINLOCK(adaptlock);        // atomically create/destroy tty devices
struct vtty_info *index_manager = NULL;   //  keep track of indexes in use currently

/* Per device sysfs entries to emulate frame, parity and overrun error events during data reception */
static DEVICE_ATTR(evt, 0666, NULL, evt_store);

static struct attribute *scmvtty_error_events_attrs[] = {
        &dev_attr_evt.attr,
        NULL,
};

static const struct attribute_group scmvtty_error_events_attr_group = {
        .name = "scmvtty_errevt",
        .attrs = scmvtty_error_events_attrs,
};

static const struct tty_port_operations vttydev_port_ops = {
};

static int last_lbdev_idx  = -1;
static int last_nmdev1_idx = -1;
static int last_nmdev2_idx = -1;

/*
 * Notifies tty layer that a framing error has happend while receiving data on serial port.
 * 
 * 1. Emulate framing error:
 * $echo "1" > /sys/devices/virtual/tty/tty2com0/scmvtty_errevt/evt
 * 
 * 2. Emulate parity error:
 * $echo "2" > /sys/devices/virtual/tty/tty2com0/scmvtty_errevt/evt
 * 
 * 3. Emulate overrun error:
 * $echo "3" > /sys/devices/virtual/tty/tty2com0/scmvtty_errevt/evt
 * 
 * 4. Emulate ring indicator (set RI signal):
 * $echo "4" > /sys/devices/virtual/tty/tty2com0/scmvtty_errevt/evt
 * 
 * 5. Emulate ring indicator (un-set RI signal):
 * $echo "5" > /sys/devices/virtual/tty/tty2com0/scmvtty_errevt/evt
 * 
 * @dev: device associated with given sysfs entry
 * @attr: sysfs attribute corresponding to this function
 * @buf: error event passed from user space to kernel via this sysfs attribute
 * @count: number of characters in buf
 * 
 * @return number of bytes consumed from buf on success or negative error code on error
 */
static ssize_t evt_store(struct device *dev, struct device_attribute *attr, const char *buf, size_t count)
{
    struct vtty_dev *vttydev = NULL;
    struct vtty_dev *local_vttydev = NULL;
    struct tty_struct *tty_to_write = NULL;

    if(!buf || (count == 0))
        return -EINVAL;

    local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    if(local_vttydev->own_index != local_vttydev->peer_index) {
        tty_to_write = local_vttydev->peer_tty;
        vttydev = index_manager[local_vttydev->peer_index].vttydev;
    }
    else {
        tty_to_write = local_vttydev->own_tty;
        vttydev = local_vttydev;
    }

    if(tty_to_write == NULL)
        return -EIO;

    mutex_lock(&local_vttydev->lock);

    switch(buf[0]) {
    case '1' : 
        tty_insert_flip_char(tty_to_write->port, 0, TTY_FRAME);
        local_vttydev->icount.frame++;
        break;
    case '2' :
        tty_insert_flip_char(tty_to_write->port, 0, TTY_PARITY);
        local_vttydev->icount.parity++;
        break;
    case '3' :
        tty_insert_flip_char(tty_to_write->port, 0, TTY_OVERRUN);
        local_vttydev->icount.overrun++;
        break;
    case '4' :
        vttydev->msr_reg |= SCM_MSR_RI;
        vttydev->icount.rng++;
        break;
    case '5' :
        vttydev->msr_reg &= ~SCM_MSR_RI;
        vttydev->icount.rng++;
        break;
    default :
        mutex_unlock(&local_vttydev->lock);
        return -EINVAL;
    }
    tty_flip_buffer_push(tty_to_write->port);

    mutex_unlock(&local_vttydev->lock);
    return count;
}

/* 
 * Update modem control and modem status registers according to the bit mask(s) provided. The 
 * DTR and RTS values can be set only if the current handshaking state of the tty device allows 
 * direct control of the modem control lines. Update honours pin mappings.
 * 
 * @tty: tty device whose modem control register is to be updated with given value(s)
 * @set: bit mask of signals which should be asserted
 * @clear: bit mask of signals which should be de-asserted
 * 
 * @return 0 on success otherwise negative error code on failure
 */
static int update_modem_lines(struct tty_struct *tty, unsigned int set, unsigned int clear)
{
    int ctsint = 0;
    int dcdint = 0;
    int dsrint = 0;
    int rngint = 0;
    int rts_mappings = 0;
    int dtr_mappings = 0;
    unsigned int mcr_ctrl_reg = 0;
    unsigned int msr_state_reg = 0;
    struct async_icount *evicount;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    struct vtty_dev *remote_vttydev = NULL;

    if(tty->index != local_vttydev->peer_index)
        remote_vttydev = index_manager[local_vttydev->peer_index].vttydev;

    rts_mappings = local_vttydev->rts_mappings;
    dtr_mappings = local_vttydev->dtr_mappings;

    if (set & TIOCM_RTS) {
        mcr_ctrl_reg |= SCM_MCR_RTS;
        if((rts_mappings & SCM_CON_CTS) == SCM_CON_CTS) {
            msr_state_reg |= SCM_MSR_CTS;
            ctsint++;
        }
        if((rts_mappings & SCM_CON_DCD) == SCM_CON_DCD) {
            msr_state_reg |= SCM_MSR_DCD;
            dcdint++;
        }
        if((rts_mappings & SCM_CON_DSR) == SCM_CON_DSR) {
            msr_state_reg |= SCM_MSR_DSR;
            dsrint++;
        }
        if((rts_mappings & SCM_CON_RI) == SCM_CON_RI) {
            msr_state_reg |= SCM_MSR_RI;
            rngint++;
        }
    }

    if (set & TIOCM_DTR) {
        mcr_ctrl_reg |= SCM_MCR_DTR;
        if((dtr_mappings & SCM_CON_CTS) == SCM_CON_CTS) {
            msr_state_reg |= SCM_MSR_CTS;
            ctsint++;
        }
        if((dtr_mappings & SCM_CON_DCD) == SCM_CON_DCD) {
            msr_state_reg |= SCM_MSR_DCD;
            dcdint++;
        }
        if((dtr_mappings & SCM_CON_DSR) == SCM_CON_DSR) {
            msr_state_reg |= SCM_MSR_DSR;
            dsrint++;
        }
        if((dtr_mappings & SCM_CON_RI) == SCM_CON_RI) {
            msr_state_reg |= SCM_MSR_RI;
            rngint++;
        }
    }

    if (clear & TIOCM_RTS) {
        mcr_ctrl_reg &= ~SCM_MCR_RTS;
        if((rts_mappings & SCM_CON_CTS) == SCM_CON_CTS) {
            msr_state_reg &= ~SCM_MSR_CTS;
            ctsint++;
        }
        if((rts_mappings & SCM_CON_DCD) == SCM_CON_DCD) {
            msr_state_reg &= ~SCM_MSR_DCD;
            dcdint++;
        }
        if((rts_mappings & SCM_CON_DSR) == SCM_CON_DSR) {
            msr_state_reg &= ~SCM_MSR_DSR;
            dsrint++;
        }
        if((rts_mappings & SCM_CON_RI) == SCM_CON_RI) {
            msr_state_reg &= ~SCM_MSR_RI;
            rngint++;
        }
    }

    if (clear & TIOCM_DTR) {
        mcr_ctrl_reg &= ~SCM_MCR_DTR;
        if((dtr_mappings & SCM_CON_CTS) == SCM_CON_CTS) {
            msr_state_reg &= ~SCM_MSR_CTS;
            ctsint++;
        }
        if((dtr_mappings & SCM_CON_DCD) == SCM_CON_DCD) {
            msr_state_reg &= ~SCM_MSR_DCD;
            dcdint++;
        }
        if((dtr_mappings & SCM_CON_DSR) == SCM_CON_DSR) {
            msr_state_reg &= ~SCM_MSR_DSR;
            dsrint++;
        }
        if((dtr_mappings & SCM_CON_RI) == SCM_CON_RI) {
            msr_state_reg &= ~SCM_MSR_RI;
            rngint++;
        }
    }

    mutex_lock(&local_vttydev->lock);
    local_vttydev->mcr_reg = mcr_ctrl_reg;

    if(remote_vttydev == NULL) {
        local_vttydev->msr_reg = msr_state_reg;
        evicount = &local_vttydev->icount;
    }
    else {
        remote_vttydev->msr_reg = msr_state_reg;
        evicount = &remote_vttydev->icount;
    }

    evicount->cts += ctsint;
    evicount->dsr += dsrint;
    evicount->dcd += dcdint;
    evicount->rng += rngint;
    mutex_unlock(&local_vttydev->lock);

    return 0;
}

/*
 * Invoked when open() is called on a serial port's device node. The tty layEr will allocate a 
 * 'struct tty_struct' for this device, allocate and setup various structures and line disc and 
 * call this function.
 * 
 * When a tty device file (/dev/ttyXX) is opened, tty core finds the tty device driver serving this
 * device file and the index in this driver for this device file (now kernel device). The tty core
 * then checks if tty structure have been already allocated for this or not. If not allocated, 
 * allocation happens otherwise a re-open happens.
 * 
 * @tty: tty structure corresponding to filp file
 * @filp: file pointer to tty device file
 * 
 * @return 0 on success or negative error code on failure.
 */
static int scmtty_open(struct tty_struct *tty, struct file *filp)
{    
    int ret = 0;
    int ctsint = 0;
    int dcdint = 0;
    int dsrint = 0;
    int rngint = 0;
    unsigned int msr_state_reg = 0;
    struct async_icount *evicount;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    struct vtty_dev *remote_vttydev = NULL;

    local_vttydev->own_index = tty->index;
    local_vttydev->own_tty = tty;

    // If this device is one end of null modem connection, provide our address to remote end
    if(tty->index != local_vttydev->peer_index) {
        remote_vttydev = index_manager[local_vttydev->peer_index].vttydev;
        remote_vttydev->peer_tty = tty;
    }

    memset(&local_vttydev->serial, 0, sizeof(struct serial_struct));
    memset(&local_vttydev->icount, 0, sizeof(struct async_icount));

    // Set low latency so that our tty_push actually pushes data to line discipline 
    // immediately instead of scheduling it.
    tty->port->low_latency = 1;

    mutex_lock(&local_vttydev->lock);

    if(local_vttydev->set_dtr_atopen == 1) {
        local_vttydev->mcr_reg |= SCM_MCR_DTR;
        if((local_vttydev->dtr_mappings & SCM_CON_CTS) == SCM_CON_CTS) {
            msr_state_reg |= SCM_MSR_CTS;
            ctsint++;
        }
        if((local_vttydev->dtr_mappings & SCM_CON_DCD) == SCM_CON_DCD) {
            msr_state_reg |= SCM_MSR_DCD;
            dcdint++;
        }
        if((local_vttydev->dtr_mappings & SCM_CON_DSR) == SCM_CON_DSR) {
            msr_state_reg |= SCM_MSR_DSR;
            dsrint++;
        }
        if((local_vttydev->dtr_mappings & SCM_CON_RI) == SCM_CON_RI) {
            msr_state_reg |= SCM_MSR_RI;
            rngint++;
        }

    }
    index_manager[local_vttydev->peer_index].vttydev->msr_reg |= msr_state_reg;

    evicount = &index_manager[local_vttydev->peer_index].vttydev->icount;
    evicount->cts += ctsint;
    evicount->dsr += dsrint;
    evicount->dcd += dcdint;
    evicount->rng += rngint;

    ret = tty_port_open(tty->port, tty, filp);
    mutex_unlock(&local_vttydev->lock);
    return ret; 
}

/*
 * Invoked by tty layer when release() is called on the file pointer that was previously created with a 
 * call to open().
 * 
 * @tty: tty structure corresponding to filp file
 * @filp: file pointer to tty device file
 * 
 * @return 0 on success or negative error code on failure.
 */
static void scmtty_close(struct tty_struct *tty, struct file *filp)
{
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;

    mutex_lock(&local_vttydev->lock);

    if(local_vttydev->set_dtr_atopen == 1)
        index_manager[local_vttydev->peer_index].vttydev->msr_reg &= ~SCM_MSR_DCD;

    tty_port_close(tty->port, tty, filp);
    mutex_unlock(&local_vttydev->lock);
}

/* 
 * Invoked by tty layer when data is to be sent to tty device may be as a response to write() call in 
 * user space.
 * 
 * @tty: tty device who will send given data
 * @buf: data to be sent
 * @count: number of data bytes in buf
 * 
 * @return number of characters sent or negative error code on failure
 */
static int scmtty_write(struct tty_struct *tty, const unsigned char *buf, int count)
{
    struct vtty_dev *vttydev = NULL;
    struct tty_struct *tty_to_write = NULL;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;

    if (!tty)
        return 0;
    if (tty->stopped)
        return 0;
    if(local_vttydev->is_break_on == 1)
        return -EIO;

    if(tty->index != local_vttydev->peer_index) {
        tty_to_write = local_vttydev->peer_tty;
        vttydev = local_vttydev;
    }
    else {
        tty_to_write = tty;
        vttydev = index_manager[local_vttydev->peer_index].vttydev;
        if((local_vttydev->baud != vttydev->baud) || (local_vttydev->uart_frame != vttydev->uart_frame))
            return count;
    }

    mutex_lock(&local_vttydev->lock);
    if(tty_to_write != NULL) {
        tty_insert_flip_string(tty_to_write->port, buf, count);
        tty_flip_buffer_push(tty_to_write->port);
        local_vttydev->icount.tx++;
        vttydev->icount.rx++;
    }else {
        // other end is still not opened, emulate transmission from local end
        // but don't make other end receive it.
        local_vttydev->icount.tx++;
    }
    mutex_unlock(&local_vttydev->lock);

    return count;
}

/* 
 * Invoked by tty layer when a single character is to be sent to the tty device. This character may be 
 * ignored if there is no room in the device for the character to be sent.
 * 
 * @tty: tty device who will send given data
 * @buf: data to be sent
 * @count: number of data bytes in buf
 * 
 * @return number of characters sent or negative error code on failure
 */
static int scmtty_put_char(struct tty_struct *tty, unsigned char ch)
{
    struct vtty_dev *vttydev = NULL;
    struct tty_struct *tty_to_write = NULL;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;

    if (!tty)
        return 0;
    if (tty->stopped)
        return 0;
    if(local_vttydev->is_break_on == 1)
        return -EIO;

    if(tty->index != local_vttydev->peer_index) {
        tty_to_write = local_vttydev->peer_tty;
        vttydev = local_vttydev;
    }
    else {
        tty_to_write = tty;
        vttydev = index_manager[local_vttydev->peer_index].vttydev;
        if((local_vttydev->baud != vttydev->baud) || (local_vttydev->uart_frame != vttydev->uart_frame))
            return 1;
    }

    mutex_lock(&local_vttydev->lock);
    if(tty_to_write != NULL) {
        tty_insert_flip_char(tty_to_write->port, ch, TTY_NORMAL);
        tty_flip_buffer_push(tty_to_write->port);
        local_vttydev->icount.tx++;
        vttydev->icount.rx++;
    }else {
        local_vttydev->icount.tx++;
    }
    mutex_unlock(&local_vttydev->lock);

    return 1;
}

/*
 * Invoked by tty layer indicating that the driver should inform tty device to start transmitting data out 
 * of serial port physically.
 * 
 * @tty: tty device who should start transmission
 */
static void scmtty_flush_chars(struct tty_struct *tty)
{
    // This tty device already transmit data as soon as it receive it.
}

/*
 * Provides port specific information to the caller.
 * 
 * @tty: tty device associated with port in question
 * @arg: user space buffer for returning information
 * 
 * @return 0 on success otherwise a negative error code on failure
 */
static int get_serial_info(struct tty_struct *tty, unsigned long arg)
{
    struct serial_struct info;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    struct serial_struct serial = local_vttydev->serial;

    if (!arg)
        return -EFAULT;

    memset(&info, 0, sizeof(info));

    info.type           = PORT_UNKNOWN;
    info.line           = serial.line;
    info.port           = tty->index;
    info.irq            = 0;
    info.flags          = tty->port->flags;
    info.xmit_fifo_size = 0;
    info.baud_base      = 0;
    info.close_delay    = tty->port->close_delay;
    info.closing_wait   = tty->port->closing_wait;
    info.custom_divisor = 0;
    info.hub6           = 0;
    info.io_type        = SERIAL_IO_MEM;

    return (copy_to_user((void __user *)arg, &info, sizeof(struct serial_struct))) ? -EFAULT : 0;
}

/*
 * Return the number of bytes that can be queued to this device at the present time. The result should be 
 * treated as a guarantee and the driver cannot offer a value it later shrinks by more than the number of 
 * bytes written.
 * 
 * @tty: tty device enquired
 * 
 * @return number of bytes that can be queued to this device at the present time
 */
static int scmtty_write_room(struct tty_struct *tty)
{
    return 2048;
}

/*
 * Invoked when the termios structure (terminal settings) for this tty device is changed. The old_termios 
 * contains currently active settings and tty->termios contains new settings to be applied.
 * 
 * @tty: tty device whose line settings is to be updated
 * @old_termios: currently applied serial line settings
 */
static void scmtty_set_termios(struct tty_struct *tty, struct ktermios *old_termios)
{
    u32 baud = 0;
    int uart_frame_settings = 0;
    unsigned int rts_mappings = 0;
    unsigned int dtr_mappings = 0;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    struct vtty_dev *remote_vttydev = NULL;

    rts_mappings = local_vttydev->rts_mappings;
    dtr_mappings = local_vttydev->dtr_mappings;

    if(tty->index != local_vttydev->peer_index)
        remote_vttydev = index_manager[local_vttydev->peer_index].vttydev;

    // Typically B0 is used to terminate the connection. Drop RTS and DTR.
    if ((tty->termios.c_cflag & CBAUD) == B0 ) {
        update_modem_lines(tty, 0, TIOCM_DTR | TIOCM_RTS);
        return;
    }

    /* If coming out of B0, raise DTR and RTS. This might get overridden in next steps. */
    if (!old_termios || (old_termios->c_cflag & CBAUD) == B0) {
        update_modem_lines(tty, TIOCM_DTR | TIOCM_RTS, 0);
    }

    mutex_lock(&local_vttydev->lock);

    baud = tty_get_baud_rate(tty);
    if (!baud) {
        baud = 9600;
    }
    tty_encode_baud_rate(tty, baud, baud);

    local_vttydev->baud = baud;

    if (tty->termios.c_cflag & CRTSCTS) {
        uart_frame_settings |= SCM_CRTSCTS;
    }else if((tty->termios.c_iflag & IXON) || (tty->termios.c_iflag & IXOFF)) {
        uart_frame_settings |= SCM_XON;
    }else {
        uart_frame_settings |= SCM_NONE;
    }

    switch (tty->termios.c_cflag & CSIZE) {
    case CS5: uart_frame_settings |= SCM_DATA_5;
    case CS6: uart_frame_settings |= SCM_DATA_6;
    case CS7: uart_frame_settings |= SCM_DATA_7;
    case CS8: uart_frame_settings |= SCM_DATA_8;
    }

    if (tty->termios.c_cflag & CSTOPB)
        uart_frame_settings |= SCM_STOP_2;
    else
        uart_frame_settings |= SCM_STOP_1;

    if (tty->termios.c_cflag & PARENB) {
        if (tty->termios.c_cflag & CMSPAR) {
            if (tty->termios.c_cflag & PARODD)
                uart_frame_settings |= SCM_PARITY_MARK;
            else
                uart_frame_settings |= SCM_PARITY_SPACE;
        }else {
            if (tty->termios.c_cflag & PARODD)
                uart_frame_settings |= SCM_PARITY_ODD;
            else
                uart_frame_settings |= SCM_PARITY_EVEN;
        }
    }else {
        uart_frame_settings |= SCM_PARITY_NONE;
    }

    mutex_unlock(&local_vttydev->lock);
}

/*
 * Return the number of bytes of data in the device private output queue. Invoked when ioctl command 
 * TIOCOUTQ is executed or by tty layer as and when required (tty_wait_until_sent()).
 * 
 * @tty: tty device enquired
 * 
 * @return number of bytes of data in the device private output queue
 */
static int scmtty_chars_in_buffer(struct tty_struct *tty)
{
    return 0;
}

/*
 * Checks if any of the given signal line has changed based on interrupts.
 * 
 * @local_vttydev: vtty device for which check has to be made
 * @mask: bit mask of TIOCM_RNG, TIOCM_DSR, TIOCM_CAR and TIOCM_CTS
 * @prev: values of previous interrupts
 * 
 * @return 1 if changed otherwise 0 if unchanged
 */
static int check_msr_delta(struct vtty_dev *local_vttydev, unsigned long mask, struct async_icount *prev) 
{
    struct async_icount now;
    int delta;
    now = local_vttydev->icount;
    delta = ((mask & TIOCM_RNG && prev->rng != now.rng) || 
            ( mask & TIOCM_DSR && prev->dsr != now.dsr) ||
            ( mask & TIOCM_CAR && prev->dcd != now.dcd) ||
            ( mask & TIOCM_CTS && prev->cts != now.cts));
    *prev = now;
    return delta;
}
/*
 * Sleeps until at-least one of the modem lines changes.
 * 
 * @tty: tty device whose modem lines is to be monitored
 * @mask: bit mask of TIOCM_RNG, TIOCM_DSR, TIOCM_CAR and TIOCM_CTS
 * 
 * @return -ERESTARTSYS if it was interrupted by a signal and 0 if modem line changed
 */
static int wait_msr_change(struct tty_struct *tty, unsigned long mask)
{
    struct async_icount prev;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    prev = local_vttydev->icount;
    return wait_event_interruptible(tty->port->delta_msr_wait, check_msr_delta(local_vttydev, mask, &prev));
}

/*
 * Invoked to execute standard and device/driver specific ioctl commands.
 * 
 * If the requested command is not supported, this driver will return -ENOIOCTLCMD so that the tty layer 
 * can invoke generic version of the givwn ioctl command if possible.
 * 
 * @tty: tty device for whom given ioctl command is to be executed
 * @cmd: ioctl command to execute
 * @arg: arguments accompanying the command
 * 
 * @return 0 on success otherwise a negative error code on failures
 */
static int scmtty_ioctl(struct tty_struct *tty, unsigned int cmd, unsigned long arg) 
{
    switch (cmd) {
    case TIOCGSERIAL:
        return get_serial_info(tty, arg);
    case TIOCMIWAIT:
        return wait_msr_change(tty, arg);
    }
    return -ENOIOCTLCMD;
}

/*
 * Invoked when tty layer's input buffers are about to get full.
 * 
 * @tty: tty device whose buffers are about to get full
 */
static void scmtty_throttle(struct tty_struct * tty) 
{
    if (tty->termios.c_cflag & CRTSCTS) {
        update_modem_lines(tty, 0, TIOCM_RTS);
    }
    else if((tty->termios.c_iflag & IXON) || (tty->termios.c_iflag & IXOFF)) {
        scmtty_put_char(tty, STOP_CHAR(tty));
    }
    else {
    }
}

/*
 * Invoked when the tty layer's input buffers have been emptied out, and it now can accept more data.
 * 
 * Throttle/unthrottle is about notifying remote end to start or stop data as per the flow control. 
 * 
 * Start/stop is about what action to take at local end itself to start or stop data as per the flow 
 * control.
 * 
 * @tty: tty device which is ready to receive data
 */
static void scmtty_unthrottle(struct tty_struct * tty) 
{
    if (tty->termios.c_cflag & CRTSCTS) {
        /* hardware (RTS/CTS) flow control */
        update_modem_lines(tty, TIOCM_RTS, 0);
    }
    else if((tty->termios.c_iflag & IXON) || (tty->termios.c_iflag & IXOFF)) {
        /* software flow control */
        scmtty_put_char(tty, START_CHAR(tty));
    }
    else {
        // do nothing
    }
}

/*
 * Invoked when this driver should stop sending data for example as a part of flow control mechanism.
 * 
 * Line discipline n_tty calls this function if this device uses software flow control and an XOFF 
 * character is received from other end.
 * 
 * @tty: tty device who should stop sending data to other end
 */
static void scmtty_stop(struct tty_struct *tty) 
{
}

/*
 * Invoked when this driver should start sending data for example as a part of flow control mechanism.
 * 
 * Line discipline n_tty calls this function if this device uses software flow control and an XON
 * character is received from other end.
 * 
 * @tty: tty device who should start sending data to other end
 */
static void scmtty_start(struct tty_struct *tty) 
{
}

/*
 * Obtain the modem status bits for the given tty device. Invoked typically when ioctl command TIOCMGET 
 * is executed on this tty device.
 * 
 * @tty: tty device whose status is enquired
 * 
 * @return bit mask (TIOCM_XXX) of modem control and modem status registers
 */
static int scmtty_tiocmget(struct tty_struct *tty) 
{
    int status = 0;
    int msr_reg = 0;
    int mcr_reg = 0;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;

    mutex_lock(&local_vttydev->lock);
    msr_reg = local_vttydev->msr_reg;
    mcr_reg = local_vttydev->mcr_reg;
    mutex_unlock(&local_vttydev->lock);

    status= ((mcr_reg & SCM_MCR_DTR)  ? TIOCM_DTR  : 0) |
            ((mcr_reg & SCM_MCR_RTS)  ? TIOCM_RTS  : 0) |
            ((mcr_reg & SCM_MCR_LOOP) ? TIOCM_LOOP : 0) |
            ((msr_reg & SCM_MSR_DCD)  ? TIOCM_CAR  : 0) |
            ((msr_reg & SCM_MSR_RI)   ? TIOCM_RI   : 0) |
            ((msr_reg & SCM_MSR_CTS)  ? TIOCM_CTS  : 0) |
            ((msr_reg & SCM_MSR_DSR)  ? TIOCM_DSR  : 0);
    return status;
}

/*
 * Set the modem status bits. Invoked typically when ioctl command TIOCMSET is executed on this tty 
 * device.
 * 
 * @tty: tty device whose modem control register is to be updated with given value
 * @set: bit mask of signals which should be asserted
 * @clear: bit mask of signals which should be de-asserted
 * 
 * @return 0 on success otherwise negative error code on failure
 */
static int scmtty_tiocmset(struct tty_struct *tty, unsigned int set, unsigned int clear) 
{
    return update_modem_lines(tty, set, clear);
}

/*
 * Invoked by tty layer to turn break condition on and off for a tty device.
 *
 * @tty: tty device who should set or reset given break condition on its output line
 * @state: 1 if break is to be asserted or 0 for de-assertion
 * 
 * @return 0 on success otherwise negative error code on failure
 */
static int scmtty_break_ctl(struct tty_struct *tty, int state) 
{
    struct tty_struct *tty_to_write = NULL;
    struct vtty_dev *vttydev = NULL;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;

    if(tty->index != local_vttydev->peer_index) {
        tty_to_write = local_vttydev->peer_tty;
        vttydev = local_vttydev;
    }
    else {
        tty_to_write = tty;
        vttydev = index_manager[local_vttydev->peer_index].vttydev;
    }

    mutex_lock(&local_vttydev->lock);
    if(state == 1) {
        if(local_vttydev->is_break_on == 1)
            return 0;
        local_vttydev->is_break_on = 1;
        if(tty_to_write != NULL) {
            tty_insert_flip_string_fixed_flag(tty_to_write->port, 0, TTY_BREAK, 1);
            tty_flip_buffer_push(tty_to_write->port);
            vttydev->icount.brk++;
        }
    }else if(state == 0) {
        local_vttydev->is_break_on = 0;
    }else {
        mutex_unlock(&local_vttydev->lock);
        return -EINVAL;
    }

    mutex_unlock(&local_vttydev->lock);
    return 0;
}

/*
 * Invoked by tty layer to inform this driver that it should hangup the tty device 
 * (Lower modem control lines after last process closes the device).
 * 
 * @tty: tty device that has hung up
 */
static void scmtty_hangup(struct tty_struct *tty) 
{
    if(tty->termios.c_cflag & HUPCL)
        update_modem_lines(tty, 0, TIOCM_DTR | TIOCM_RTS);
}

/*
 * Invoked to execute ioctl command TIOCGICOUNT to get the number of interrupts. Both 1->0 and 
 * 0->1 transitions are counted, except for RI, where only 0->1 transitions are counted.
 * 
 * @tty: tty device whose interrupts information is to be collected
 * @icount: memory location from which tty core will copy data to user space buffer
 * 
 * @return 0 on success otherwise negative error code on failure
 */
static int scmtty_get_icount(struct tty_struct *tty, struct serial_icounter_struct *icount) 
{
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    struct async_icount cnow;

    mutex_lock(&local_vttydev->lock);
    cnow = local_vttydev->icount;
    mutex_unlock(&local_vttydev->lock);

    icount->cts = cnow.cts;
    icount->dsr = cnow.dsr;
    icount->rng = cnow.rng;
    icount->dcd = cnow.dcd;
    icount->tx = cnow.tx;
    icount->rx = cnow.rx;
    icount->frame = cnow.frame;
    icount->parity = cnow.parity;
    icount->overrun = cnow.overrun;
    icount->brk = cnow.brk;
    icount->buf_overrun = cnow.buf_overrun;

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
 * @tty: tty device whose buffer should be flushed
 */
static void scmtty_flush_buffer(struct tty_struct *tty) 
{
    // this tty device don't have local buffers.
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
 * Invoked by tty layer to execute TCIOFF and TCION IOCTL commands generally because user space process 
 * called tcflow() function. It send a high priority character to the tty device end even if stopped.
 * 
 * If this function (send_xchar) is defined by tty device driver, tty core will call this function. If 
 * it is not specified then tty core will first instruct this driver to start transmission (start()) 
 * and then invoke write() of this driver passing character to be written and then it will call stop() 
 * function of this driver.
 * 
 * @tty: tty device who is sending this character
 * @ch: character to be sent (typically it is XOFF or XON)
 */
static void scmtty_send_xchar(struct tty_struct *tty, char ch) 
{
    scmtty_put_char(tty, ch);
}

/*
 * @file file for proc file
 * @buf user space buffer that will contain data when this function returns
 * @length number of character returned in buf
 * @ppos offset
 * 
 * @return number of bytes copied to user buffer on success or negative error code on error
 */
static ssize_t scmtty_vadapt_proc_read(struct file *file, char __user *buf, size_t size, loff_t *ppos)
{
    int ret = 0;
    char data[64];

    memset(data, '\0', sizeof(data));

    spin_lock(&adaptlock);

    if(last_lbdev_idx == -1) {
        if(last_nmdev1_idx == -1) {
            snprintf(data, 64, "%s", "xxxxx#xxxxx-xxxxx");
        }else {
            snprintf(data, 64, "%s#%05d-%05d", "xxxxx", last_nmdev1_idx, last_nmdev2_idx);
        }
    }else {
        if(last_nmdev1_idx == -1) {
            snprintf(data, 64, "%05d#%s", last_lbdev_idx, "xxxxx-xxxxx");
        }else {
            snprintf(data, 64, "%05d#%05d-%05d", last_lbdev_idx, last_nmdev1_idx, last_nmdev2_idx);
        }
    }

    spin_unlock(&adaptlock);

    ret = copy_to_user(buf, &data, 18);
    if(ret)
        return -EFAULT;
    return 18;
}

/*
 * Extract pin mappings from local to remote tty devices.
 * 
 * @return: 0 on success or negative error code
 */
static int extract_mapping(char data[], int x) {
    int i = 0;
    int mapping = 0;
    for(i=0; i<8; i++) {
        if(data[x] == '8') {
            mapping |= SCM_CON_CTS;
        }else if(data[x] == '1') {
            mapping |= SCM_CON_DCD;
        }else if(data[x] == '6') {
            mapping |= SCM_CON_DSR;
        }else if(data[x] == '9') {
            mapping |= SCM_CON_RI;
        }else if(data[x] == '#') {
            break;
        }else if((data[x] == 'x') || (data[x] == ',')) {
        }else {
            return -EINVAL;
        }
        x++;
    }
    return mapping;
}

/*
 * This function is equivalent to a typical 'probe' function in linux device driver model for this virtual 
 * adaptor.
 *
 * Standard DB9 pin assignment: 1 - DCD, 2 - RX, 3 - TX, 4 - DTR, 5 - GND, 6 - DSR, 7 - RTS, 8 - CTS, 9 - RI.
 * 
 * Assignment 7-8 means connect local RTS pin to remote CTS pin. Assignment 4-1,6 means connect local DTR to 
 * remote DSR and DCD pins. Assignment 7-x means leave local RTS pin unconnected. The 'y' at last will raise 
 * remote DCD pin when local device is opened. When removing tty device, if the given device is one of the 
 * device in a null modem pair, coupled device will also be deleted automatically.
 * 
 * 1. Create standard null modem connection:
 * $echo "gennm#vdev1#vdev2#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y" > /proc/scmtty_vadaptkm
 * 
 * 2. Create standard loop back connection:
 * $echo "genlb#vdevt#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#x" > /proc/scmtty_vadaptkm
 * 
 * 3. Delete a particular tty device:
 * $echo "del#vdevt#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" > /proc/scmtty_vadaptkm
 * 
 * 4. Delete all virtual tty devices in this adaptor:
 * $echo "del#xxxxx#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" > /proc/scmtty_vadaptkm
 * 
 * @file file representing scm proc file
 * @buf command supplied by caller
 * @length length of the command
 * @ppos offset in file
 * 
 * @return number of bytes consumed by this function on success or negative error code on failure
 */
static ssize_t scmtty_vadapt_proc_write(struct file *file, const char __user *buf, size_t length, loff_t * ppos)
{
    int x = -1;
    int y = -1;
    int i = -1;
    int ret = -1;
    int create = -1;
    int vdev1idx = -1;
    int vdev2idx = -1;
    int vdev1rts = 0;
    int vdev2rts = 0;
    int vdev1dtr = 0;
    int vdev2dtr = 0;
    int is_loopback = -1;

    char tmp[8];
    char data[64];

    struct vtty_dev *vttydev1 = NULL;
    struct vtty_dev *vttydev2 = NULL;
    struct tty_port *port1 = NULL;
    struct tty_port *port2 = NULL;
    struct device *device1 = NULL;
    struct device *device2 = NULL;

    if((length > 60) || (length < 63)) {
        if(copy_from_user(data, buf, length) != 0) {
            return -EFAULT;
        }
    }else if(length == 2) {
        memcpy(data, "gennm#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y", 61);
    }else if(length == 3) {
        memcpy(data, "genlb#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#x", 61);
    }else {
        return -EINVAL;
    }
    data[62] = '\0';

    // initial sanitization
    if((data[0] == 'g') && (data[1] == 'e') && (data[2] == 'n')) {
        if((data[3] == 'n') && (data[4] == 'm')) {
        }else if((data[3] == 'l') && (data[4] == 'b')) {
            is_loopback = 1;
        }else {
            return -EINVAL;
        }
        create = 1;
    }else if((data[0] == 'd') && (data[1] == 'e') && (data[2] == 'l')) {
        create = -1;
    }else {
        return -EINVAL;
    }

    if(create == 1) {
        /* Create device command sent */

        // extract 1st device index
        x = 6;
        if(data[6] != 'x') {
            memset(tmp, '\0', sizeof(tmp));
            for(i=0; i<5; i++) {
                tmp[i] = data[x];
                x++;
            }
            ret = kstrtouint(tmp, 10, &vdev1idx);
            if(ret != 0) 
                return ret;
        }

        vttydev1 = (struct vtty_dev *) kcalloc(1, sizeof(struct vtty_dev), GFP_KERNEL);
        if(vttydev1 == NULL)
            return -ENOMEM;

        port1 = (struct tty_port *) kcalloc(1, sizeof(struct tty_port), GFP_KERNEL);
        if(port1 == NULL) {
            ret = -ENOMEM;
            goto fail_arg;
        }
        port1->ops = &vttydev_port_ops;
        tty_port_init(port1);

        // extract 2nd device index if null modem pair is to be created
        if(is_loopback != 1) {
            x = 12;
            if(data[x] != 'x') {
                memset(tmp, '\0', sizeof(tmp));
                i = 0;
                for(i=0; i<5; i++) {
                    tmp[i] = data[x];
                    x++;
                }
                ret = kstrtouint(tmp, 10, &vdev2idx);
                if(ret != 0)
                    return ret;
            }

            vttydev2 = (struct vtty_dev *) kcalloc(1, sizeof(struct vtty_dev), GFP_KERNEL);
            if(vttydev2 == NULL) {
                ret = -ENOMEM;
                goto fail_arg;
            }

            port2 = (struct tty_port *) kcalloc(1, sizeof(struct tty_port), GFP_KERNEL);
            if(port2 == NULL) {
                ret = -ENOMEM;
                goto fail_arg;
            }
            port2->ops = &vttydev_port_ops;
            tty_port_init(port2);
        }

        // rts mappings (dev1)
        if((data[18] != '7') || (data[19] != '-')) {
            ret = -EINVAL;
            goto fail_arg;
        }
        ret = extract_mapping(data, 20);
        if(ret < 0)
            goto fail_arg;
        vdev1rts = ret;

        if((data[27] != '#') || (data[28] != '4') || (data[29] != '-'))
            goto fail_arg;

        // dtr mapping (dev1)
        ret = extract_mapping(data, 30);
        if(ret < 0)
            goto fail_arg;
        vdev1dtr = ret;

        if(data[37] != '#')
            goto fail_arg;

        if(is_loopback != 1) {
            // rts mappings (dev2)
            if((data[38] != '7') || (data[39] != '-')) {
                ret = -EINVAL;
                goto fail_arg;
            }
            ret = extract_mapping(data, 40);
            if(ret < 0)
                goto fail_arg;
            vdev2rts = ret;

            // dtr mapping (dev2)
            if((data[47] != '#') || (data[48] != '4') || (data[49] != '-'))
                goto fail_arg;

            ret = extract_mapping(data, 50);
            if(ret < 0)
                goto fail_arg;
            vdev2dtr = ret;

            if(data[57] != '#')
                goto fail_arg;
        }

        // Create serial port (tty device) with lock taken to ensure correctness of index in use
        // and associated data.
        spin_lock(&adaptlock);

        i = -1;
        if(vdev1idx == -1) {
            for(x=0; x < max_num_vtty_dev; x++) {
                if(index_manager[x].index == -1) {
                    i = x;
                    break;
                }
            }
        }else {
            if(index_manager[vdev1idx].index == -1) {
                i = vdev1idx;
            }else {
                ret = -EEXIST;
                spin_unlock(&adaptlock);
                goto fail_arg;
            }
        }
        if(i == -1) {
            ret = -ENOMEM;
            spin_unlock(&adaptlock);
            goto fail_arg;
        }

        // initialize meta information and create 1st serial port
        if(data[59] == 'y')
            vttydev1->set_dtr_atopen = 1;
        else
            vttydev1->set_dtr_atopen = 0;
        vttydev1->own_index = i;
        vttydev1->peer_index = i;
        vttydev1->rts_mappings = vdev1rts;
        vttydev1->dtr_mappings = vdev1dtr;
        vttydev1->msr_reg = 0;
        vttydev1->mcr_reg = 0;
        index_manager[i].index = i;
        index_manager[i].vttydev = vttydev1;
        mutex_init(&vttydev1->lock);

        tty_port_link_device(port1, scmtty_driver, i);

        if(is_loopback != 1) {
            y = -1;
            if(vdev2idx == -1) {
                for(x=0; x < max_num_vtty_dev; x++) {
                    if(index_manager[x].index == -1) {
                        y = x;
                        break;
                    }
                }
            }else {
                if(index_manager[vdev2idx].index == -1) {
                    y = vdev2idx;
                }else {
                    ret = -EEXIST;
                    spin_unlock(&adaptlock);
                    goto fail_arg;
                }
            }
            if(y == -1) {
                ret = -ENOMEM;
                spin_unlock(&adaptlock);
                goto fail_arg;
            }

            // initialize meta information and create 2nd serial port
            if(data[61] == 'y')
                vttydev2->set_dtr_atopen = 1;
            else
                vttydev2->set_dtr_atopen = 0;
            vttydev2->own_index = i;
            vttydev1->peer_index = y;
            vttydev2->own_index = y;
            vttydev2->peer_index = i;
            vttydev2->rts_mappings = vdev2rts;
            vttydev2->dtr_mappings = vdev2dtr;
            vttydev2->msr_reg = 0;
            vttydev2->mcr_reg = 0;
            index_manager[y].index = y;
            index_manager[y].vttydev = vttydev2;
            mutex_init(&vttydev2->lock);

            tty_port_link_device(port2, scmtty_driver, y);
        }

        device1 = tty_register_device(scmtty_driver, i, NULL);
        if(device1 == NULL) {
            ret = -ENOMEM;
            spin_unlock(&adaptlock);
            goto fail_arg;
        }

        vttydev1->device = device1;
        dev_set_drvdata(device1, vttydev1);

        x = sysfs_create_group(&device1->kobj, &scmvtty_error_events_attr_group);
        if(x < 0) {
            spin_unlock(&adaptlock);
            goto fail_arg;
        }

        if(is_loopback != 1) {
            device2 = tty_register_device(scmtty_driver, y, NULL);
            if(device2 == NULL) {
                ret = -ENOMEM;
                spin_unlock(&adaptlock);
                goto fail_register;
            }

            vttydev2->device = device2;
            dev_set_drvdata(device2, vttydev2);

            x = sysfs_create_group(&device2->kobj, &scmvtty_error_events_attr_group);
            if(x < 0) {
                spin_unlock(&adaptlock);
                goto fail_register;
            }

            last_nmdev1_idx = i;
            last_nmdev2_idx = y;
        }else {
            last_lbdev_idx = i;
        }

        spin_unlock(&adaptlock);
    }
    else {
        /* Destroy device command sent */

        if(data[4] == 'x') {

            spin_lock(&adaptlock);
            for(x=0; x < max_num_vtty_dev; x++) {
                if(index_manager[x].index != -1) {
                    vttydev1 = index_manager[x].vttydev;
                    sysfs_remove_group(&vttydev1->device->kobj, &scmvtty_error_events_attr_group);
                    tty_unregister_device(scmtty_driver, index_manager[x].index);
                    kfree(index_manager[x].vttydev);
                    tty_port_destroy(scmtty_driver->ports[x]);
                    kfree(scmtty_driver->ports[x]);
                    index_manager[x].index = -1;
                }
            }
            spin_unlock(&adaptlock);

        }else {
            x = 4;
            memset(tmp, '\0', sizeof(tmp));
            i = 0;
            for(i=0; i<5; i++) {
                tmp[i] = data[x];
                x++;
            }
            ret = kstrtouint(tmp, 10, &vdev1idx);
            if(ret != 0)
                return ret;
            if(index_manager[vdev1idx].index != -1) {
                spin_lock(&adaptlock);
                x = index_manager[vdev1idx].index;
                vttydev1 = index_manager[x].vttydev;
                sysfs_remove_group(&vttydev1->device->kobj, &scmvtty_error_events_attr_group);
                tty_unregister_device(scmtty_driver, index_manager[vdev1idx].index);
                kfree(index_manager[vdev1idx].vttydev);
                tty_port_destroy(scmtty_driver->ports[x]);
                kfree(scmtty_driver->ports[x]);
                index_manager[vdev1idx].index = -1;
                spin_unlock(&adaptlock);
            }else {
                return -EINVAL;
            }
        }
    }

    return length;

    fail_register:
    sysfs_remove_group(&device1->kobj, &scmvtty_error_events_attr_group);
    tty_unregister_device(scmtty_driver, i);
    fail_arg:
    if(device1 != NULL)
        tty_unregister_device(scmtty_driver, i);
    if(vttydev2 != NULL)
        kfree(vttydev2);
    if(vttydev1 != NULL)
        kfree(vttydev1);
    if(port2 != NULL) {
        tty_port_destroy(port2);
        kfree(port2);
    }
    if(port1 != NULL) {
        tty_port_destroy(port1);
        kfree(port1);
    }
    return ret;
}

/* 
 * Invoked when user space process opens /proc/scmtty_vadaptkm file to create/destroy 
 * virtual tty device(s).
 * 
 * @return 0 on success.
 */
static int scmtty_vadapt_proc_open(struct inode *inode, struct  file *file) 
{
    return 0;
}

/* 
 * Invoked when user space process closes /proc/scmtty_vadaptkm file.
 * 
 * @return 0 on success.
 */
static int scmtty_vadapt_proc_close(struct inode *inode, struct file *file) 
{
    return 0;
}

static const struct file_operations scmtty_vadapt_proc_fops = {
        .owner = THIS_MODULE,
        .open = scmtty_vadapt_proc_open,
        .read = scmtty_vadapt_proc_read,
        .write = scmtty_vadapt_proc_write,
        .release = scmtty_vadapt_proc_close,
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

/* 
 * Invoked when this driver is loaded. If the user supplies correct number of virtual devices
 * to be created when this module is loaded, the virtual devices will be made, otherwise they
 * will not be made and have to be created using proc file.
 *
 * For example; if this driver should support upto maximum 20 devices and create 1 null-modem pair 
 * and 1 loop back device, load this driver module as shown below:
 * 
 * $insmod ./tty2comKm.ko max_num_vtty_dev=20 init_num_nm_pair=1 init_num_lb_dev=1
 *
 * @return: 0 on success or negative error code on failure.
 */
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
    scmtty_driver->name = "tty2com";
    scmtty_driver->minor_start = 0;
    scmtty_driver->major = SCM_VTTY_MAJOR;
    scmtty_driver->type = TTY_DRIVER_TYPE_SERIAL;
    scmtty_driver->subtype = SERIAL_TYPE_NORMAL;
    scmtty_driver->flags = TTY_DRIVER_REAL_RAW | TTY_DRIVER_RESET_TERMIOS | TTY_DRIVER_DYNAMIC_DEV;
    scmtty_driver->init_termios = tty_std_termios;
    scmtty_driver->init_termios.c_cflag = B9600 | CS8 | CREAD | HUPCL | CLOCAL;
    scmtty_driver->init_termios.c_ispeed = 9600;
    scmtty_driver->init_termios.c_ospeed = 9600;

    tty_set_operations(scmtty_driver, &scm_serial_ops);

    ret = tty_register_driver(scmtty_driver);
    if (ret) 
        goto failed_register;

    index_manager = (struct vtty_info *) kcalloc(max_num_vtty_dev, sizeof(struct vtty_info), GFP_KERNEL);
    if(index_manager == NULL) {
        ret = -ENOMEM;
        goto failed_alloc;
    }

    /* A value of -1 at particular 'X' (index_manager[X].index) means that tty2comportX is available for use */
    for(x=0; x < max_num_vtty_dev;  x++) {
        index_manager[x].index = -1;
    }

    /* Application should read/write to this file to create/destroy tty device and query informations associated 
     * with them */
    pde = proc_create("scmtty_vadaptkm", 0666, NULL, &scmtty_vadapt_proc_fops);
    if(pde == NULL) {
        ret = -ENOMEM;
        goto failed_proc;
    }

    /* If module was supplied parameters create null-modem and loopback virtual tty devices */
    if (((2 * init_num_nm_pair) + init_num_lb_dev) <= max_num_vtty_dev) {
        for(x=0; x < init_num_nm_pair; x++) {
            scmtty_vadapt_proc_write(NULL, NULL, 2, NULL);
        }
        for(x=0; x < init_num_lb_dev; x++) {
            scmtty_vadapt_proc_write(NULL, NULL, 3, NULL);
        }
    }

    printk(KERN_INFO "%s %s %s\n", "tty2comKm:", DRIVER_DESC, DRIVER_VERSION);
    return 0;

    failed_proc:
    kfree(index_manager);
    failed_alloc:
    tty_unregister_driver(scmtty_driver);
    failed_register:
    put_tty_driver(scmtty_driver);
    return ret;
}

/* 
 * Invoked when this driver is unloaded.
 */
static void __exit scm_tty2comKm_exit(void)
{
    int x = 0;
    struct vtty_dev *vttydev = NULL;

    remove_proc_entry("scmtty_vadaptkm", NULL);

    for(x=0; x < max_num_vtty_dev; x++) {
        if(index_manager[x].index != -1) {
            vttydev = index_manager[x].vttydev;
            sysfs_remove_group(&vttydev->device->kobj, &scmvtty_error_events_attr_group);
            tty_unregister_device(scmtty_driver, index_manager[x].index);
            kfree(vttydev);
            tty_port_destroy(scmtty_driver->ports[x]);
            kfree(scmtty_driver->ports[x]);
        }
    }
    kfree(index_manager);

    tty_unregister_driver(scmtty_driver);
    put_tty_driver(scmtty_driver);
}

module_init(scm_tty2comKm_init);
module_exit(scm_tty2comKm_exit);

module_param(max_num_vtty_dev, ushort, 0);
MODULE_PARM_DESC(max_num_vtty_dev, "Maximum number of virtual tty devices this driver can create.");

module_param(init_num_nm_pair, ushort, 0);
MODULE_PARM_DESC(init_num_nm_pair, "Number of standard null modem pairs to be created at load time.");

module_param(init_num_lb_dev, ushort, 0);
MODULE_PARM_DESC(init_num_lb_dev, "Number of standard loopback tty devices to be created at load time.");

MODULE_AUTHOR( DRIVER_AUTHOR );
MODULE_DESCRIPTION( DRIVER_DESC );
MODULE_LICENSE("GPL v2");
MODULE_VERSION( DRIVER_VERSION );
