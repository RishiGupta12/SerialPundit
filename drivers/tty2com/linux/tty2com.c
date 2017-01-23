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
 * Virtual multi-port serial card :
 *
 * This driver implements a virtual multiport serial card in such a way that the virtual card 
 * can have from 0 to N number of virtual serial ports (tty devices). The virtual tty devices created 
 * by this card are used in exactly the same way using termios and Linux/Posix APIs as the real tty 
 * devices.
 */

#define pr_fmt(fmt) KBUILD_MODNAME ": " fmt

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
#define DRIVER_DESC "Serial port null modem emulation driver "

/* 
 * Default number of virtual tty ports this driver is going to support. TTY devices are created 
 * on demand. Users can override this value at module load time for example to support 5000 tty 
 * virtual devices :
 * $ insmod ./tty2com.ko max_num_vtty_dev=5000
 *
 * Major number is assigned dynamically by kernel for device nodes served by this driver.
 */
#define DEFAULT_VTTY_DEV_MAX  128

/* Pin out configurations definitions */
#define SP_CON_CTS    0x0001
#define SP_CON_DCD    0x0002
#define SP_CON_DSR    0x0004
#define SP_CON_RI     0x0008

/* Modem control register definitions */
#define SP_MCR_DTR    0x0001
#define SP_MCR_RTS    0x0002
#define SP_MCR_LOOP   0x0004

/* Modem status register definitions */
#define SP_MSR_CTS    0x0008
#define SP_MSR_DCD    0x0010
#define SP_MSR_RI     0x0020
#define SP_MSR_DSR    0x0040

/* UART frame structure definitions */
#define SP_CRTSCTS       0x0001
#define SP_XON           0x0002
#define SP_NONE          0X0004
#define SP_DATA_5        0X0008
#define SP_DATA_6        0X0010
#define SP_DATA_7        0X0020
#define SP_DATA_8        0X0040
#define SP_PARITY_NONE   0x0080
#define SP_PARITY_ODD    0x0100
#define SP_PARITY_EVEN   0x0200
#define SP_PARITY_MARK   0x0400
#define SP_PARITY_SPACE  0x0800
#define SP_STOP_1        0x1000
#define SP_STOP_2        0x2000

/* Constants values for device type (odevtyp) */
#define SNM 0x0001
#define CNM 0x0002
#define SLB 0x0003
#define CLB 0x0004

/* Represent a virtual tty device in this virtual card. The peer_index will contain own 
 * index if this device is loop back configured device (peer_index == own_index). */
struct vtty_dev {
    unsigned int own_index;
    unsigned int peer_index;
    int msr_reg; /* shadow modem status register */
    int mcr_reg; /* shadow modem control register */
    int rts_mappings;
    int dtr_mappings;
    int set_odtr_at_open;
    int set_pdtr_at_open;
    int odevtyp;
    struct mutex lock;
    int is_break_on;
    int baud;
    int uart_frame;
    int waiting_msr_chg;
    int tx_paused;
    int faulty_cable;
    struct tty_struct *own_tty;
    struct tty_struct *peer_tty;
    struct serial_struct serial;
    struct async_icount icount;
    struct device *device;
};

/* Current driver design is such that the vtty_info for a device with index x will be placed at
 * index x in array index_manager. */
struct vtty_info {
    int index;
    struct vtty_dev *vttydev;
};

static int sp_install(struct tty_driver *driver, struct tty_struct *tty);
static int sp_open(struct tty_struct *tty, struct file *filp);
static int sp_write(struct tty_struct *tty, const unsigned char *buf, int count);
static int sp_put_char(struct tty_struct *tty, unsigned char ch);
static int sp_break_ctl(struct tty_struct *tty, int state);
static int sp_write_room(struct tty_struct *tty);
static int sp_chars_in_buffer(struct tty_struct *tty);
static int sp_ioctl(struct tty_struct *tty, unsigned int cmd, unsigned long arg);
static int sp_tiocmget(struct tty_struct *tty);
static int sp_tiocmset(struct tty_struct *tty, unsigned int set, unsigned int clear);
static int sp_get_icount(struct tty_struct *tty, struct serial_icounter_struct *icount);

static void sp_cleanup(struct tty_struct *tty);
static void sp_set_termios(struct tty_struct *tty, struct ktermios *old);
static void sp_throttle(struct tty_struct *tty);
static void sp_unthrottle(struct tty_struct *tty);
static void sp_stop(struct tty_struct *tty);
static void sp_start(struct tty_struct *tty);
static void sp_hangup(struct tty_struct *tty);
static void sp_flush_chars(struct tty_struct *tty);
static void sp_flush_buffer(struct tty_struct *tty);
static void sp_wait_until_sent(struct tty_struct *tty, int timeout);
static void sp_send_xchar(struct tty_struct *tty, char ch);
static void sp_close(struct tty_struct *tty, struct file *filp);

static int sp_extract_pin_mapping(char data[], int x);
static int sp_update_modem_lines(struct tty_struct *tty, unsigned int set, unsigned int clear);
static int sp_get_serial_info(struct tty_struct *tty, unsigned long arg);
static int sp_wait_msr_change(struct tty_struct *tty, unsigned long mask);
static int sp_check_msr_delta(struct tty_struct *tty, struct vtty_dev *local_vttydev, unsigned long mask, struct async_icount *prev);

static ssize_t sp_evt_store(struct device *dev, struct device_attribute *attr, const char *buf, size_t count);
static ssize_t sp_faultycable_store(struct device *dev, struct device_attribute *attr, const char *buf, size_t count);
static ssize_t sp_ownidx_show(struct device *dev, struct device_attribute *attr, char *buf);
static ssize_t sp_peeridx_show(struct device *dev, struct device_attribute *attr, char *buf);
static ssize_t sp_ortsmap_show(struct device *dev, struct device_attribute *attr, char *buf);
static ssize_t sp_odtrmap_show(struct device *dev, struct device_attribute *attr, char *buf);
static ssize_t sp_prtsmap_show(struct device *dev, struct device_attribute *attr, char *buf);
static ssize_t sp_pdtrmap_show(struct device *dev, struct device_attribute *attr, char *buf);
static ssize_t sp_odevtyp_show(struct device *dev, struct device_attribute *attr, char *buf);
static ssize_t sp_odtropn_show(struct device *dev, struct device_attribute *attr, char *buf);
static ssize_t sp_pdtropn_show(struct device *dev, struct device_attribute *attr, char *buf);
static ssize_t sp_ostats_show(struct device *dev, struct device_attribute *attr, char *buf);

static int sp_vcard_proc_open(struct inode *inode, struct  file *file);
static int sp_vcard_proc_close(struct inode *inode, struct file *file);
static ssize_t sp_vcard_proc_read(struct file *file, char __user *buf, size_t size, loff_t *ppos);
static ssize_t sp_vcard_proc_write(struct file *file, const char __user *buf, size_t length, loff_t * ppos);

static int sp_port_carrier_raised(struct tty_port *port);
static void sp_port_shutdown(struct tty_port *port);
static int sp_port_activate(struct tty_port *port, struct tty_struct *tty);
static void sp_port_destruct(struct tty_port *port);
/*static void sp_port_dtr_rts(struct tty_port *port, int raise);*/

/* These 4 values can be overriden if this driver is loaded with parameters provided */
static int minor_begin = 0;
static ushort max_num_vtty_dev = DEFAULT_VTTY_DEV_MAX;
static ushort init_num_nm_pair = 0;
static ushort init_num_lb_dev  = 0;

static ushort total_nm_pair = 0;
static ushort total_lb_devs = 0;
static int last_lbdev_idx   = -1;
static int last_nmdev1_idx  = -1;
static int last_nmdev2_idx  = -1;

/* Describes this driver kernel module */
static struct tty_driver *spvtty_driver;

/* Used when creating or destroying virtual tty devices */
static DEFINE_MUTEX(adaptlock);           /*  atomically create/destroy tty devices  */
struct vtty_info *index_manager = NULL;   /*  keep track of indexes in use currently */

/* Per device sysfs entries to emulate frame, parity and overrun error events during data
 * reception and providing some informations about device. The 'proc entries' are used to
 * interact with driver state as a whole while 'sysfs enteries' are used to interact with
 * individual device's state. Use 99-tty2com.rules file to correctly set permissions on
 * sysfs entries. To align with sysfs spirit 'one-value-per-file' approach is followed so
 * that user space does not have to know data format and their offsets in returned result
 * buffer. */
static DEVICE_ATTR(evt, (S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP), NULL, sp_evt_store);
static DEVICE_ATTR(faultycable, (S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP), NULL, sp_faultycable_store);
static DEVICE_ATTR(ownidx,  S_IRUGO, sp_ownidx_show,  NULL);
static DEVICE_ATTR(peeridx, S_IRUGO, sp_peeridx_show, NULL);
static DEVICE_ATTR(ortsmap, S_IRUGO, sp_ortsmap_show, NULL);
static DEVICE_ATTR(odtrmap, S_IRUGO, sp_odtrmap_show, NULL);
static DEVICE_ATTR(prtsmap, S_IRUGO, sp_prtsmap_show, NULL);
static DEVICE_ATTR(pdtrmap, S_IRUGO, sp_pdtrmap_show, NULL);
static DEVICE_ATTR(odevtyp, S_IRUGO, sp_odevtyp_show, NULL);
static DEVICE_ATTR(odtropn, S_IRUGO, sp_odtropn_show, NULL);
static DEVICE_ATTR(pdtropn, S_IRUGO, sp_pdtropn_show, NULL);
static DEVICE_ATTR(ostats,  S_IRUGO, sp_ostats_show, NULL);

static struct attribute *spvtty_info_attrs[] = {
        &dev_attr_evt.attr,
        &dev_attr_faultycable.attr,
        &dev_attr_ownidx.attr,
        &dev_attr_peeridx.attr,
        &dev_attr_ortsmap.attr,
        &dev_attr_odtrmap.attr,
        &dev_attr_prtsmap.attr,
        &dev_attr_pdtrmap.attr,
        &dev_attr_odevtyp.attr,
        &dev_attr_odtropn.attr,
        &dev_attr_pdtropn.attr,
        &dev_attr_ostats.attr,
        NULL,
};

static const struct attribute_group sp_info_attr_group = {
        .attrs = spvtty_info_attrs,
};

static const struct tty_port_operations spvtty_port_ops = {
        .carrier_raised = sp_port_carrier_raised,
        .shutdown       = sp_port_shutdown,
        .activate       = sp_port_activate,
        .destruct       = sp_port_destruct,
        /*.dtr_rts        = sp_port_dtr_rts,  */
};

/*
 * Notifies tty layer that a framing/parity/overrun error has happend while receiving data on serial port. 
 * The serial port on which the given error/event is to be emulated must have been opened before causing 
 * error event. In the example given below tty2com0 port will indicate to tty layer that it received 
 * corrupted data.
 *
 * The -7 is taken as corrupted character in this driver. For emulation purpose 0 can not be taken as corrupted
 * charcter because parity and break both wil have same sequence (\377 \0 \0) and therefore application may not
 * be able to differentiate.
 * 
 * 1. Emulate framing error:
 * $ echo "1" > /sys/devices/virtual/tty/tty2com0/evt
 * 
 * 2. Emulate parity error:
 * $ echo "2" > /sys/devices/virtual/tty/tty2com0/evt
 * 
 * 3. Emulate overrun error:
 * $ echo "3" > /sys/devices/virtual/tty/tty2com0/evt
 * 
 * 4. Emulate ring indicator (set RI signal):
 * $ echo "4" > /sys/devices/virtual/tty/tty2com0/evt
 * 
 * 5. Emulate ring indicator (un-set RI signal):
 * $ echo "5" > /sys/devices/virtual/tty/tty2com0/evt
 * 
 * 6. Emulate break received:
 * $ echo "6" > /sys/devices/virtual/tty/tty2com0/evt
 *
 * A "framing error" occurs when the designated "start" and "stop" bits are not found. A Parity Error occurs
 * when the parity of the number of 1 bits disagrees with that specified by the parity bit. A "break condition"
 * occurs when the receiver input is at the "space" (logic low, i.e., '0') level for longer than some duration
 * of time, typically, for more than a character time. This is not necessarily an error, but appears to the
 * receiver as a character of all zero bits with a framing error.
 *
 * @dev: device associated with given sysfs entry
 * @attr: sysfs attribute corresponding to this function
 * @buf: error event passed from user space to kernel via this sysfs attribute
 * @count: number of characters in buf
 * 
 * @return number of bytes consumed from buf on success or negative error code on error
 */
static ssize_t sp_evt_store(struct device *dev, struct device_attribute *attr, const char *buf, size_t count)
{
    int ret = 0;
    int push = 1;
    struct vtty_dev *local_vttydev = NULL;
    struct tty_struct *tty_to_write = NULL;

    if(!buf || (count <= 0))
        return -EINVAL;

    local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);
    tty_to_write = local_vttydev->own_tty;

    /* Ensure required structure has been allocated, initialized and port has been opened. */
    if((!tty_to_write) || (tty_to_write->port == NULL) || (tty_to_write->port->count <= 0))
        return -EIO;
    if(!test_bit(ASYNCB_INITIALIZED, &tty_to_write->port->flags))
        return -EIO;

    mutex_lock(&local_vttydev->lock);

    switch(buf[0]) {
    case '1' : 
        ret = tty_insert_flip_char(tty_to_write->port, -7, TTY_FRAME);
        if(ret < 0)
            goto fail;
        local_vttydev->icount.frame++;
        break;
    case '2' :
        ret = tty_insert_flip_char(tty_to_write->port, -7, TTY_PARITY);
        if(ret < 0)
            goto fail;
        local_vttydev->icount.parity++;
        break;
    case '3' :
        ret = tty_insert_flip_char(tty_to_write->port, 0, TTY_OVERRUN);
        if(ret < 0)
            goto fail;
        local_vttydev->icount.overrun++;
        break;
    case '4' :
        local_vttydev->msr_reg |= SP_MSR_RI;
        local_vttydev->icount.rng++;
        push = -1;
        break;
    case '5' :
        local_vttydev->msr_reg &= ~SP_MSR_RI;
        local_vttydev->icount.rng++;
        push = -1;
        break;
    case '6' :
        ret = tty_insert_flip_char(tty_to_write->port, 0, TTY_BREAK);
        if(ret < 0)
            goto fail;
        local_vttydev->icount.brk++;
        break;
    default :
        mutex_unlock(&local_vttydev->lock);
        return -EINVAL;
    }

    if (push)
        tty_flip_buffer_push(tty_to_write->port);

    mutex_unlock(&local_vttydev->lock);
    return count;

    fail:
    mutex_unlock(&local_vttydev->lock);
    return ret;
}

/*
 * Emulate a faulty cable condition.
 * 
 * @dev: device associated with given sysfs entry
 * @attr: sysfs attribute corresponding to this function
 * @buf: cable state passed from user space to kernel via this sysfs attribute
 * @count: number of characters in buf
 * 
 * @return number of bytes consumed from buf on success or negative error code on error
 */
static ssize_t sp_faultycable_store(struct device *dev, struct device_attribute *attr, const char *buf, size_t count)
{
    struct vtty_dev *local_vttydev = NULL;

    if(!buf || (count <= 0))
        return -EINVAL;

    local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    switch(buf[0]) {
    case '0' :
        local_vttydev->faulty_cable = 0;
        break;
    case '1' :
        local_vttydev->faulty_cable = 1;
        break;
    default :
        return -EINVAL;
    }

    return count;
}

/*
 * Gives serial port stats.
 *
 * $ cat /sys/devices/virtual/tty/tty2com0/ostats
 *
 * @dev: tty device
 * @attr: sysfs attributes
 * @buf: memory where result of invoking this function will be returned to caller.
 *
 * @return serial port stats on success otherwise negative error code.
 */
static ssize_t sp_ostats_show(struct device *dev, struct device_attribute *attr, char *buf)
{
    struct vtty_dev *local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    if(!buf)
        return -EINVAL;

    return sprintf(buf, "%u#%u#%u#%u#%u#%u#%u#%u#%u#%u#%u#\n", local_vttydev->icount.tx, local_vttydev->icount.rx, local_vttydev->icount.cts, 
            local_vttydev->icount.dcd, local_vttydev->icount.dsr, local_vttydev->icount.brk, local_vttydev->icount.rng, local_vttydev->icount.frame, 
            local_vttydev->icount.parity, local_vttydev->icount.overrun, local_vttydev->icount.buf_overrun);
}

/*
 * Gives index of tty device to which this sysfs attribute belongs.
 *
 * $ cat /sys/devices/virtual/tty/tty2com0/ownidx
 *
 * @dev: tty device
 * @attr: sysfs attributes
 * @buf: memory where result of invoking this function will be returned to caller.
 *
 * @return index of tty device on success otherwise negative error code.
 */
static ssize_t sp_ownidx_show(struct device *dev, struct device_attribute *attr, char *buf)
{
    struct vtty_dev *local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    if(!buf)
        return -EINVAL;

    /* Capacity of buf is typically 4096 (PAGE_SIZE) as passed by the kernel. */
    return sprintf(buf, "%u\n", local_vttydev->own_index);
}

/*
 * Gives index of tty device to which given tty device is paired.
 *
 * $ cat /sys/devices/virtual/tty/tty2com0/peeridx
 *
 * @dev: tty device
 * @attr: sysfs attributes
 * @buf: memory where result of invoking this function will be returned to caller.
 *
 * @return index of paired tty device on success otherwise negative error code.
 */
static ssize_t sp_peeridx_show(struct device *dev, struct device_attribute *attr, char *buf)
{
    struct vtty_dev *local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    if(!buf)
        return -EINVAL;

    return sprintf(buf, "%u\n", local_vttydev->peer_index);
}

/*
 * Gives mapping of RTS line of the given tty device to which this sysfs attribute belongs.
 *
 * $ cat /sys/devices/virtual/tty/tty2com0/ortsmap
 *
 * @dev: tty device
 * @attr: sysfs attributes
 * @buf: memory where result of invoking this function will be returned to caller.
 *
 * @return mapping of RTS line of the given tty device on success otherwise negative error code.
 */
static ssize_t sp_ortsmap_show(struct device *dev, struct device_attribute *attr, char *buf)
{
    struct vtty_dev *local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    if(!buf)
        return -EINVAL;

    return sprintf(buf, "%u\n", local_vttydev->rts_mappings);
}

/*
 * Gives mapping of DTR line of the given tty device to which this sysfs attribute belongs.
 *
 * $ cat /sys/devices/virtual/tty/tty2com0/odtrmap
 *
 * @dev: tty device
 * @attr: sysfs attributes
 * @buf: memory where result of invoking this function will be returned to caller.
 *
 * @return mapping of DTR line of the given tty device on success otherwise negative error code.
 */
static ssize_t sp_odtrmap_show(struct device *dev, struct device_attribute *attr, char *buf)
{
    struct vtty_dev *local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    if(!buf)
        return -EINVAL;

    return sprintf(buf, "%u\n", local_vttydev->dtr_mappings);
}

/*
 * Gives mapping of RTS line of the paired tty device.
 *
 * $ cat /sys/devices/virtual/tty/tty2com0/prtsmap
 *
 * @dev: tty device
 * @attr: sysfs attributes
 * @buf: memory where result of invoking this function will be returned to caller.
 *
 * @return mapping of RTS line of the paired tty device on success otherwise negative error code.
 */
static ssize_t sp_prtsmap_show(struct device *dev, struct device_attribute *attr, char *buf)
{
    struct vtty_dev *remote_vttydev = NULL;
    struct vtty_dev *local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    if((local_vttydev->own_index == local_vttydev->peer_index) || (!buf))
        return -EINVAL;

    remote_vttydev = index_manager[local_vttydev->peer_index].vttydev;
    return sprintf(buf, "%u\n", remote_vttydev->rts_mappings);
}

/*
 * Gives mapping of DTR line of the paired tty device.
 *
 * $ cat /sys/devices/virtual/tty/tty2com0/pdtrmap
 *
 * @dev: tty device
 * @attr: sysfs attributes
 * @buf: memory where result of invoking this function will be returned to caller.
 *
 * @return mapping of DTR line of the paired tty device on success otherwise negative error code.
 */
static ssize_t sp_pdtrmap_show(struct device *dev, struct device_attribute *attr, char *buf)
{
    struct vtty_dev *remote_vttydev = NULL;
    struct vtty_dev *local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    if((local_vttydev->own_index == local_vttydev->peer_index) || (!buf))
        return -EINVAL;

    remote_vttydev = index_manager[local_vttydev->peer_index].vttydev;
    return sprintf(buf, "%u\n", remote_vttydev->dtr_mappings);
}

/*
 * Gives device type based on pin mappings.
 *
 * $ cat /sys/devices/virtual/tty/tty2com0/odevtyp
 *
 * @dev: tty device
 * @attr: sysfs attributes
 * @buf: memory where result of invoking this function will be returned to caller.
 *
 * @return device type based on pin mappings on success otherwise negative error code.
 */
static ssize_t sp_odevtyp_show(struct device *dev, struct device_attribute *attr, char *buf)
{
    struct vtty_dev *local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    if(!buf)
        return -EINVAL;

    return sprintf(buf, "%u\n", local_vttydev->odevtyp);
}

/*
 * Tells whether DTR will be raised when this serial port is opened or not. 1 means DTR will be
 * raised at open and 0 means it will not be raised.
 *
 * $ cat /sys/devices/virtual/tty/tty2com0/odtropn
 *
 * @dev: tty device
 * @attr: sysfs attributes
 * @buf: memory where result of invoking this function will be returned to caller.
 *
 * @return DTR state when serial port is opened on success otherwise negative error code.
 */
static ssize_t sp_odtropn_show(struct device *dev, struct device_attribute *attr, char *buf)
{
    struct vtty_dev *local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    if(!buf)
        return -EINVAL;

    return sprintf(buf, "%u\n", local_vttydev->set_odtr_at_open);
}

/*
 * Tells whether DTR pin of peer device will be raised if that device is opened or not. 1 means DTR will be
 * raised at open and 0 means it will not be raised.
 *
 * $ cat /sys/devices/virtual/tty/tty2com0/pdtropn
 *
 * @dev: tty device
 * @attr: sysfs attributes
 * @buf: memory where result of invoking this function will be returned to caller.
 *
 * @return DTR state when serial port is opened on success otherwise negative error code.
 */
static ssize_t sp_pdtropn_show(struct device *dev, struct device_attribute *attr, char *buf)
{
    struct vtty_dev *local_vttydev = (struct vtty_dev *) dev_get_drvdata(dev);

    if(!buf)
        return -EINVAL;

    return sprintf(buf, "%u\n", local_vttydev->set_pdtr_at_open);
}

/* 
 * Update modem control and modem status registers according to the bit mask(s) provided. The 
 * DTR and RTS values can be set only if the current handshaking state of the tty device allows 
 * direct control of the modem control lines. Update honours pin mappings.
 *
 * Caller holds the lock associated with the given tty's virtual tty info.
 * 
 * @tty: tty device whose modem control register is to be updated with given value(s)
 * @set: bit mask of signals which should be asserted
 * @clear: bit mask of signals which should be de-asserted
 * 
 * @return 0 on success otherwise negative error code on failure
 */
static int sp_update_modem_lines(struct tty_struct *tty, unsigned int set, unsigned int clear)
{
    int ctsint = 0;
    int dcdint = 0;
    int dsrint = 0;
    int rngint = 0;
    int rts_mappings = 0;
    int dtr_mappings = 0;
    int mcr_ctrl_reg = 0;
    int msr_state_reg = 0;
    int wakeup_blocked_open = 0;
    struct async_icount *evicount;
    struct vtty_dev *vttydev = NULL;
    struct vtty_dev *local_vttydev = NULL;
    struct vtty_dev *remote_vttydev = NULL;

    local_vttydev = index_manager[tty->index].vttydev;
    if(tty->index != local_vttydev->peer_index)
        remote_vttydev = index_manager[local_vttydev->peer_index].vttydev;

    /* Read modify write MSR register */
    if(remote_vttydev != NULL) {
        msr_state_reg = remote_vttydev->msr_reg;
        vttydev = remote_vttydev;
    }
    else {
        msr_state_reg = local_vttydev->msr_reg;
        vttydev = local_vttydev;
    }

    rts_mappings = local_vttydev->rts_mappings;
    dtr_mappings = local_vttydev->dtr_mappings;

    if(set & TIOCM_RTS) {
        mcr_ctrl_reg |= SP_MCR_RTS;
        if((rts_mappings & SP_CON_CTS) == SP_CON_CTS) {
            msr_state_reg |= SP_MSR_CTS;
            ctsint++;
        }
        if((rts_mappings & SP_CON_DCD) == SP_CON_DCD) {
            msr_state_reg |= SP_MSR_DCD;
            dcdint++;
            wakeup_blocked_open = 1;
        }
        if((rts_mappings & SP_CON_DSR) == SP_CON_DSR) {
            msr_state_reg |= SP_MSR_DSR;
            dsrint++;
        }
        if((rts_mappings & SP_CON_RI) == SP_CON_RI) {
            msr_state_reg |= SP_MSR_RI;
            rngint++;
        }
    }

    if(set & TIOCM_DTR) {
        mcr_ctrl_reg |= SP_MCR_DTR;
        if((dtr_mappings & SP_CON_CTS) == SP_CON_CTS) {
            msr_state_reg |= SP_MSR_CTS;
            ctsint++;
        }
        if((dtr_mappings & SP_CON_DCD) == SP_CON_DCD) {
            msr_state_reg |= SP_MSR_DCD;
            dcdint++;
            wakeup_blocked_open = 1;
        }
        if((dtr_mappings & SP_CON_DSR) == SP_CON_DSR) {
            msr_state_reg |= SP_MSR_DSR;
            dsrint++;
        }
        if((dtr_mappings & SP_CON_RI) == SP_CON_RI) {
            msr_state_reg |= SP_MSR_RI;
            rngint++;
        }
    }

    if(clear & TIOCM_RTS) {
        mcr_ctrl_reg &= ~SP_MCR_RTS;
        if((rts_mappings & SP_CON_CTS) == SP_CON_CTS) {
            msr_state_reg &= ~SP_MSR_CTS;
            ctsint++;
        }
        if((rts_mappings & SP_CON_DCD) == SP_CON_DCD) {
            msr_state_reg &= ~SP_MSR_DCD;
            dcdint++;
        }
        if((rts_mappings & SP_CON_DSR) == SP_CON_DSR) {
            msr_state_reg &= ~SP_MSR_DSR;
            dsrint++;
        }
        if((rts_mappings & SP_CON_RI) == SP_CON_RI) {
            msr_state_reg &= ~SP_MSR_RI;
            rngint++;
        }
    }

    if (clear & TIOCM_DTR) {
        mcr_ctrl_reg &= ~SP_MCR_DTR;
        if((dtr_mappings & SP_CON_CTS) == SP_CON_CTS) {
            msr_state_reg &= ~SP_MSR_CTS;
            ctsint++;
        }
        if((dtr_mappings & SP_CON_DCD) == SP_CON_DCD) {
            msr_state_reg &= ~SP_MSR_DCD;
            dcdint++;
        }
        if((dtr_mappings & SP_CON_DSR) == SP_CON_DSR) {
            msr_state_reg &= ~SP_MSR_DSR;
            dsrint++;
        }
        if((dtr_mappings & SP_CON_RI) == SP_CON_RI) {
            msr_state_reg &= ~SP_MSR_RI;
            rngint++;
        }
    }

    local_vttydev->mcr_reg = mcr_ctrl_reg;
    vttydev->msr_reg = msr_state_reg;

    evicount = &vttydev->icount;
    evicount->cts += ctsint;
    evicount->dsr += dsrint;
    evicount->dcd += dcdint;
    evicount->rng += rngint;

    if(vttydev->own_tty && vttydev->own_tty->port) {

        /* Wake up process blocked on TIOCMIWAIT ioctl */
        if((vttydev->waiting_msr_chg == 1) && (vttydev->own_tty->port->count > 0)) {
            wake_up_interruptible(&vttydev->own_tty->port->delta_msr_wait);
        }

        /* Wake up application blocked on carrier detect signal */
        if((wakeup_blocked_open == 1) && (vttydev->own_tty->port->blocked_open > 0)) {
            wake_up_interruptible(&vttydev->own_tty->port->open_wait);
        }
    }

    return 0;
}

/*
 * Invoked when user space process opens a serial port. Tty core calls this to install tty and
 * initialize the required resources.
 *
 * @driver: this driver.
 * @tty: the tty being created.
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int sp_install(struct tty_driver *driver, struct tty_struct *tty)
{
    int ret = 0;
    struct tty_port *port = NULL;

    port = (struct tty_port *) kcalloc(1, sizeof(struct tty_port), GFP_KERNEL);
    if(port == NULL)
        return -ENOMEM;

    /* First initialize and then set port operations */
    tty_port_init(port);
    port->ops = &spvtty_port_ops;

    ret = tty_port_install(port, driver, tty);
    if (ret) {
        kfree(port);
        return ret;
    }

    return 0;
}

/*
 * Invoked when there exist no user process or tty is to be released explicitly for whatever reason.
 *
 * @tty: the tty being destroyed.
 */
static void sp_cleanup(struct tty_struct *tty)
{
    tty_port_put(tty->port);
}

/*
 * Invoked when open() is called on a serial port's device node. The tty layer will allocate a 
 * 'struct tty_struct' for this device and setup various structures and line disc and call this 
 * function.
 * 
 * When a tty device file (/dev/ttyXX) is opened, tty core finds the tty device driver serving this
 * device file and the index in this driver for this device file (now kernel device). The tty core
 * then checks if tty structure have been already allocated for this or not. If not allocated, 
 * allocation happens otherwise a re-open happens.
 * 
 * If the same serial port is opened more than once, the tty structure passed to this function will 
 * be same but filp structure will be different every time. Caller holds tty lock.
 * 
 * @tty: tty structure corresponding to filp file.
 * @filp: file pointer for handle to tty.
 * 
 * @return 0 on success or negative error code on failure.
 */
static int sp_open(struct tty_struct *tty, struct file *filp)
{    
    int ret = 0;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    struct vtty_dev *remote_vttydev = NULL;

    local_vttydev->own_tty = tty;

    /* If this device is one end of a null modem connection, provide its address to remote end */
    if (tty->index != local_vttydev->peer_index) {
        remote_vttydev = index_manager[local_vttydev->peer_index].vttydev;
        remote_vttydev->peer_tty = tty;
    }

    memset(&local_vttydev->serial, 0, sizeof(struct serial_struct));
    memset(&local_vttydev->icount, 0, sizeof(struct async_icount));

    /* Handle DTR raising logic ourselve instead of tty_port helpers doing it. */
    if (local_vttydev->set_odtr_at_open == 1) {
        sp_update_modem_lines(tty, TIOCM_DTR | TIOCM_RTS, 0);
    }

    /* Associate tty with port and do port level opening. */
    ret = tty_port_open(tty->port, tty, filp);
    if (ret < 0) 
        return ret;

    tty->port->close_delay  = 0;
    tty->port->closing_wait = ASYNC_CLOSING_WAIT_NONE;
    tty->port->drain_delay  = 0;

    return ret;
}

/*
 * Invoked by tty layer when release() is called on the file pointer that was previously created with a 
 * call to open().
 * 
 * @tty: tty structure corresponding to filp file.
 * @filp: file pointer for handle to tty.
 * 
 * @return 0 on success or negative error code on failure.
 */
static void sp_close(struct tty_struct *tty, struct file *filp)
{
    if(test_bit(TTY_IO_ERROR, &tty->flags))
        return;

    if(tty && filp && tty->port && (tty->port->count > 0))
        tty_port_close(tty->port, tty, filp);

    if(tty && C_HUPCL(tty) && tty->port && (tty->port->count < 1))
        sp_update_modem_lines(tty, 0, TIOCM_DTR | TIOCM_RTS);
}

/* 
 * Invoked by tty layer via the line discipline when data is to be sent to tty device may be 
 * as a response to write() call in user space. The data bytes are inserted into the tty buffer and get
 * scheduled to be sent to receiver. This function will construct correct UART frame before sending.
 * 
 * @tty: tty device who will send given data.
 * @buf: data to be sent.
 * @count: number of data bytes in buf.
 * 
 * @return number of characters sent or negative error code on failure.
 */
static int sp_write(struct tty_struct *tty, const unsigned char *buf, int count)
{
    int x = 0;
    unsigned char *data = NULL;
    struct tty_struct *tty_to_write = NULL;
    struct vtty_dev *rx_vttydev = NULL;
    struct vtty_dev *tx_vttydev = index_manager[tty->index].vttydev;

    if (tx_vttydev->tx_paused || !tty || tty->stopped || (count < 1) || !buf || tty->hw_stopped)
        return 0;

    if (tx_vttydev->is_break_on == 1) {
        dev_dbg(tty->dev, "break condition is on !");
        return -EIO;
    }

    if(tx_vttydev->faulty_cable == 1)
        return count;

    if (tty->index != tx_vttydev->peer_index) {
        /* null modem */
        tty_to_write = tx_vttydev->peer_tty;
        rx_vttydev = index_manager[tx_vttydev->peer_index].vttydev;

        if((tx_vttydev->baud != rx_vttydev->baud) || (tx_vttydev->uart_frame != rx_vttydev->uart_frame)) {
            /* Emulate data sent but not received */
            dev_dbg(tty->dev, "mismatched serial port settings !");
            tx_vttydev->icount.tx++;
            return count;
        }
    }
    else {
        /* loop back */
        tty_to_write = tty;
        rx_vttydev = tx_vttydev;
    }

    if (tty_to_write != NULL) {

        /* The UART hardware receiver samples received electrical signals at the middle of a bit in uart frame.
         * Emulate correct number/size of data bits and hence uart frame. */
        if((tty_to_write->termios.c_cflag & CSIZE) == CS8) {
            data = (unsigned char *)buf;
        }
        else {
            data = (unsigned char *) kcalloc(count, sizeof(unsigned char), GFP_KERNEL);
            if(data == NULL)
                return -ENOMEM;

            switch (tty_to_write->termios.c_cflag & CSIZE) {
            case CS7:
                for(x=0; x < count; x++) {
                    data[x] = buf[x] & 0x7F;
                }
                break;
            case CS6:
                for(x=0; x < count; x++) {
                    data[x] = buf[x] & 0x3F;
                }
                break;
            case CS5:
                for(x=0; x < count; x++) {
                    data[x] = buf[x] & 0x1F;
                }
                break;
            default:
                data = (unsigned char *)buf;
            }
        }

        tty_insert_flip_string(tty_to_write->port, data, count);
        tty_flip_buffer_push(tty_to_write->port);
        tx_vttydev->icount.tx++;
        rx_vttydev->icount.rx++;

        if(data != buf)
            kfree(data);
    }else {
        /* Other end is still not opened, emulate transmission from local end
           but don't make other end receive it as is the case in real world. */
        tx_vttydev->icount.tx++;
    }

    return count;
}

/*
 * Invoked by tty layer when a single character is to be sent to the tty device. This character may be
 * ignored if there is no room in the device for the character to be sent.
 *
 * @tty: tty device who will send given data.
 * @buf: data to be sent.
 * @count: number of data bytes in buf.
 *
 * @return number of characters sent or negative error code on failure.
 */
static int sp_put_char(struct tty_struct *tty, unsigned char ch)
{
    unsigned char data;
    struct tty_struct *tty_to_write = NULL;
    struct vtty_dev *rx_vttydev = NULL;
    struct vtty_dev *tx_vttydev = index_manager[tty->index].vttydev;

    if (tx_vttydev->tx_paused || !tty || tty->stopped || tty->hw_stopped)
        return 0;

    if (tx_vttydev->is_break_on == 1)
        return -EIO;

    if(tx_vttydev->faulty_cable == 1)
        return 1;

    if (tty->index != tx_vttydev->peer_index) {
        tty_to_write = tx_vttydev->peer_tty;
        rx_vttydev = index_manager[tx_vttydev->peer_index].vttydev;
        if((tx_vttydev->baud != rx_vttydev->baud) || (tx_vttydev->uart_frame != rx_vttydev->uart_frame)) {
            tx_vttydev->icount.tx++;
            return 1;
        }
    }
    else {
        tty_to_write = tty;
        rx_vttydev = tx_vttydev;
    }

    if(tty_to_write != NULL) {
        switch (tty_to_write->termios.c_cflag & CSIZE) {
        case CS8:
            data = ch;
            break;
        case CS7:
            data = ch & 0x7F;
            break;
        case CS6:
            data = ch & 0x3F;
            break;
        case CS5:
            data = ch & 0x1F;
            break;
        default:
            data = ch;
        }
        tty_insert_flip_string(tty_to_write->port, &data, 1);
        tty_flip_buffer_push(tty_to_write->port);
        tx_vttydev->icount.tx++;
        rx_vttydev->icount.rx++;
    }else {
        tx_vttydev->icount.tx++;
    }

    return 1;
}

/*
 * Invoked by tty layer indicating that the driver should inform tty device to start transmitting data out
 * of serial port physically. This tty device already transmit data as soon as it receive it.
 *
 * @tty: tty device who should start transmission.
 */
static void sp_flush_chars(struct tty_struct *tty)
{
}

/*
 * Provides port specific information to the caller as a result of executing  TIOCGSERIAL
 * ioctl command.
 *
 * @tty: tty device associated with port in question.
 * @arg: user space buffer for returning information.
 *
 * @return 0 on success otherwise a negative error code on failure.
 */
static int sp_get_serial_info(struct tty_struct *tty, unsigned long arg)
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
 * @tty: tty device enquired.
 *
 * @return number of bytes that can be queued to this device at the present time.
 */
static int sp_write_room(struct tty_struct *tty)
{
    struct vtty_dev *tx_vttydev = index_manager[tty->index].vttydev;

    if (tx_vttydev->tx_paused || !tty || tty->stopped || tty->hw_stopped)
        return 0;

    return 2048;
}

/*
 * Invoked when the termios structure (terminal settings) for this tty device is changed. The old_termios
 * contains currently active settings and tty->termios contains new settings to be applied.
 *
 * @tty: tty device whose line settings is to be updated.
 * @old_termios: currently applied serial line settings.
 */
static void sp_set_termios(struct tty_struct *tty, struct ktermios *old_termios)
{
    u32 baud = 0;
    int uart_frame_settings = 0;
    unsigned int rts_mappings = 0;
    unsigned int dtr_mappings = 0;
    unsigned int mask = TIOCM_DTR;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    struct vtty_dev *remote_vttydev = NULL;

    rts_mappings = local_vttydev->rts_mappings;
    dtr_mappings = local_vttydev->dtr_mappings;

    if(tty->index != local_vttydev->peer_index)
        remote_vttydev = index_manager[local_vttydev->peer_index].vttydev;

    mutex_lock(&local_vttydev->lock);

    /* Typically B0 is used to terminate the connection. Drop RTS and DTR. */
    if ((tty->termios.c_cflag & CBAUD) == B0 ) {
        sp_update_modem_lines(tty, 0, TIOCM_DTR | TIOCM_RTS);
        mutex_unlock(&local_vttydev->lock);
        return;
    }

    /* If coming out of B0, raise DTR and RTS. This might get overridden in next steps. Applications like
     * minicom when opens a serial port, may drop speed to B0 and then back to normal speed again. */
    if (!old_termios || (old_termios->c_cflag & CBAUD) == B0) {
        if (!(tty->termios.c_cflag & CRTSCTS) || !test_bit(TTY_THROTTLED, &tty->flags)) {
            mask |= TIOCM_RTS;
            sp_update_modem_lines(tty, mask, 0);
        }
    }

    baud = tty_get_baud_rate(tty);
    if (!baud) {
        baud = 9600;
    }
    tty_encode_baud_rate(tty, baud, baud);

    local_vttydev->baud = baud;

    if (tty->termios.c_cflag & CRTSCTS) {
        uart_frame_settings |= SP_CRTSCTS;
    }else if((tty->termios.c_iflag & IXON) || (tty->termios.c_iflag & IXOFF)) {
        uart_frame_settings |= SP_XON;
    }else {
        uart_frame_settings |= SP_NONE;
    }

    switch (tty->termios.c_cflag & CSIZE) {
    case CS8: uart_frame_settings |= SP_DATA_8;
    case CS7: uart_frame_settings |= SP_DATA_7;
    case CS6: uart_frame_settings |= SP_DATA_6;
    case CS5: uart_frame_settings |= SP_DATA_5;
    default:  uart_frame_settings |= SP_DATA_8;
    }

    if (tty->termios.c_cflag & CSTOPB)
        uart_frame_settings |= SP_STOP_2;
    else
        uart_frame_settings |= SP_STOP_1;

    if (tty->termios.c_cflag & PARENB) {
        if (tty->termios.c_cflag & CMSPAR) {
            if (tty->termios.c_cflag & PARODD)
                uart_frame_settings |= SP_PARITY_MARK;
            else
                uart_frame_settings |= SP_PARITY_SPACE;
        }else {
            if (tty->termios.c_cflag & PARODD)
                uart_frame_settings |= SP_PARITY_ODD;
            else
                uart_frame_settings |= SP_PARITY_EVEN;
        }
    }else {
        uart_frame_settings |= SP_PARITY_NONE;
    }

    local_vttydev->uart_frame = uart_frame_settings;

    mutex_unlock(&local_vttydev->lock);
}

/*
 * Return the number of bytes of data in the device private output queue to be sent out. Invoked 
 * when ioctl command TIOCOUTQ is executed or by tty layer as and when required (tty_wait_until_sent()).
 *
 * @tty: tty device enquired.
 *
 * @return number of bytes of data in the device private output queue.
 */
static int sp_chars_in_buffer(struct tty_struct *tty)
{
    return 0;
}

/*
 * Checks if any of the given signal line has changed based on interrupts.
 *
 * @local_vttydev: vtty device for which check has to be made.
 * @mask: bit mask of TIOCM_RNG, TIOCM_DSR, TIOCM_CAR and TIOCM_CTS.
 * @prev: values of previous interrupts
 *.
 * @return 1 if changed otherwise 0 if unchanged.
 */
static int sp_check_msr_delta(struct tty_struct *tty, struct vtty_dev *local_vttydev, unsigned long mask, struct async_icount *prev)
{
    struct async_icount now;
    int delta = 0;

    /* Use tty-port initialised flag to detect all hangups including the disconnect(device destroy) event */
    if (!test_bit(ASYNCB_INITIALIZED, &tty->port->flags))
        return 1;

    mutex_lock(&local_vttydev->lock);
    now = local_vttydev->icount;
    mutex_unlock(&local_vttydev->lock);
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
 * @tty: tty device whose modem lines is to be monitored.
 * @mask: bit mask of TIOCM_RNG, TIOCM_DSR, TIOCM_CAR and TIOCM_CTS.
 *
 * @return -ERESTARTSYS if it was interrupted by a signal and 0 if modem line changed.
 */
static int sp_wait_msr_change(struct tty_struct *tty, unsigned long mask)
{
    int ret = 0;
    struct async_icount prev;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;

    mutex_lock(&local_vttydev->lock);
    local_vttydev->waiting_msr_chg = 1;
    prev = local_vttydev->icount;
    mutex_unlock(&local_vttydev->lock);

    ret = wait_event_interruptible(tty->port->delta_msr_wait, sp_check_msr_delta(tty, local_vttydev, mask, &prev));

    local_vttydev->waiting_msr_chg = 0;

    if (!ret && !test_bit(ASYNCB_INITIALIZED, &tty->port->flags))
        ret = -EIO;

    return ret;
}

/*
 * Invoked to execute standard and device/driver specific ioctl commands.
 *
 * If the requested command is not supported, this driver will return -ENOIOCTLCMD so that the tty layer
 * can invoke generic version of the givwn ioctl command if possible.
 *
 * @tty: tty device for whom given ioctl command is to be executed.
 * @cmd: ioctl command to execute.
 * @arg: arguments accompanying the command.
 *
 * @return 0 on success otherwise a negative error code on failures.
 */
static int sp_ioctl(struct tty_struct *tty, unsigned int cmd, unsigned long arg)
{
    switch (cmd) {
    case TIOCGSERIAL:
        return sp_get_serial_info(tty, arg);
    case TIOCMIWAIT:
        return sp_wait_msr_change(tty, arg);
    }

    return -ENOIOCTLCMD;
}

/*
 * Invoked when tty layer's input buffers are about to get full.
 * 
 * When using RTS/CTS flow control, when RTS line is de-asserted, interrupt will be generated 
 * in hardware. The interrupt handler will raise a flag to indicate transmission should be stopped. 
 * This is achieved in this driver through tx_paused variable.
 *
 * @tty: tty device whose buffers are about to get full.
 */
static void sp_throttle(struct tty_struct *tty)
{
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    struct vtty_dev *remote_vttydev = index_manager[local_vttydev->peer_index].vttydev;

    if (tty->termios.c_cflag & CRTSCTS) {
        mutex_lock(&local_vttydev->lock);
        remote_vttydev->tx_paused = 1;
        sp_update_modem_lines(tty, 0, TIOCM_RTS);
        mutex_unlock(&local_vttydev->lock);
    }
    else if((tty->termios.c_iflag & IXON) || (tty->termios.c_iflag & IXOFF)) {
        sp_put_char(tty, STOP_CHAR(tty));
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
 * @tty: tty device which is ready to receive data.
 */
static void sp_unthrottle(struct tty_struct *tty)
{
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    struct vtty_dev *remote_vttydev = index_manager[local_vttydev->peer_index].vttydev;

    if (tty->termios.c_cflag & CRTSCTS) {
        /* hardware (RTS/CTS) flow control */
        mutex_lock(&local_vttydev->lock);
        remote_vttydev->tx_paused = 0;
        sp_update_modem_lines(tty, TIOCM_RTS, 0);
        mutex_unlock(&local_vttydev->lock);

        if (remote_vttydev->own_tty && remote_vttydev->own_tty->port)
            tty_port_tty_wakeup(remote_vttydev->own_tty->port);
    }
    else if((tty->termios.c_iflag & IXON) || (tty->termios.c_iflag & IXOFF)) {
        /* software flow control */
        sp_put_char(tty, START_CHAR(tty));
    }
    else {
        /* do nothing */
    }
}

/*
 * Invoked when this driver should stop sending data for example as a part of flow control mechanism.
 *
 * Line discipline n_tty calls this function if this device uses software flow control and an XOFF
 * character is received from other end.
 *
 * @tty: tty device who should stop sending data to other end.
 */
static void sp_stop(struct tty_struct *tty)
{
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    mutex_lock(&local_vttydev->lock);
    local_vttydev->tx_paused = 1;
    mutex_unlock(&local_vttydev->lock);
}

/*
 * Invoked when this driver should start sending data for example as a part of flow control mechanism.
 *
 * Line discipline n_tty calls this function if this device uses software flow control and an XON
 * character is received from other end.
 *
 * @tty: tty device who should start sending data to other end.
 */
static void sp_start(struct tty_struct *tty)
{
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    mutex_lock(&local_vttydev->lock);
    local_vttydev->tx_paused = 0;
    mutex_unlock(&local_vttydev->lock);

    if (tty && tty->port)
        tty_port_tty_wakeup(tty->port);
}

/*
 * Obtain the modem status bits for the given tty device. Invoked typically when ioctl command TIOCMGET
 * is executed on this tty device.
 *
 * @tty: tty device whose status is enquired.
 *
 * @return bit mask (TIOCM_XXX) of modem control and modem status registers.
 */
static int sp_tiocmget(struct tty_struct *tty)
{
    int status = 0;
    int msr_reg = 0;
    int mcr_reg = 0;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;

    mutex_lock(&local_vttydev->lock);
    mcr_reg = local_vttydev->mcr_reg;
    msr_reg = local_vttydev->msr_reg;
    mutex_unlock(&local_vttydev->lock);

    status= ((mcr_reg & SP_MCR_DTR)  ? TIOCM_DTR  : 0) |
            ((mcr_reg & SP_MCR_RTS)  ? TIOCM_RTS  : 0) |
            ((mcr_reg & SP_MCR_LOOP) ? TIOCM_LOOP : 0) |
            ((msr_reg & SP_MSR_DCD)  ? TIOCM_CAR  : 0) |
            ((msr_reg & SP_MSR_RI)   ? TIOCM_RI   : 0) |
            ((msr_reg & SP_MSR_CTS)  ? TIOCM_CTS  : 0) |
            ((msr_reg & SP_MSR_DSR)  ? TIOCM_DSR  : 0);
    return status;
}

/*
 * Set the modem status bits. Invoked typically when ioctl command TIOCMSET is executed on this tty
 * device.
 *
 * @tty: tty device whose modem control register is to be updated with given value.
 * @set: bit mask of signals which should be asserted.
 * @clear: bit mask of signals which should be de-asserted.
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int sp_tiocmset(struct tty_struct *tty, unsigned int set, unsigned int clear)
{
    int ret = 0;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;
    mutex_lock(&local_vttydev->lock);
    ret = sp_update_modem_lines(tty, set, clear);
    mutex_unlock(&local_vttydev->lock);
    return ret;
}

/*
 * Invoked by tty layer to turn break condition on and off for a tty device unconditionally.
 *
 * @tty: tty device who should set or reset given break condition on its output line.
 * @state: 1 if break is to be asserted or 0 for de-assertion.
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int sp_break_ctl(struct tty_struct *tty, int break_state)
{
    struct tty_struct *tty_to_write = NULL;
    struct vtty_dev *brk_rx_vttydev = NULL;
    struct vtty_dev *brk_tx_vttydev = index_manager[tty->index].vttydev;

    if(tty->index != brk_tx_vttydev->peer_index) {
        tty_to_write = brk_tx_vttydev->peer_tty;
        brk_rx_vttydev = index_manager[brk_tx_vttydev->peer_index].vttydev;
    }
    else {
        tty_to_write = tty;
        brk_rx_vttydev = brk_tx_vttydev;
    }

    mutex_lock(&brk_tx_vttydev->lock);

    if (break_state != 0) {
        if(brk_tx_vttydev->is_break_on == 1)
            return 0;

        brk_tx_vttydev->is_break_on = 1;
        if(tty_to_write != NULL) {
            tty_insert_flip_char(tty_to_write->port, 0, TTY_BREAK);
            tty_flip_buffer_push(tty_to_write->port);
            brk_rx_vttydev->icount.brk++;
        }
    }
    else {
        brk_tx_vttydev->is_break_on = 0;
    }

    mutex_unlock(&brk_tx_vttydev->lock);
    return 0;
}

/*
 * Invoked by tty layer to inform this driver that it should hangup the tty device (lower
 * modem control lines after last process using tty devices closes the device or exited).
 *
 * Drop DTR/RTS if HUPCL is set. This causes any attached modem to hang up the line.
 *
 * On the receiving end, if CLOCAL bit is set, DCD will be ignored otherwise SIGHUP may be
 * generated to indicate a line disconnect event.
 *
 * @tty: tty device that has hung up.
 */
static void sp_hangup(struct tty_struct *tty)
{
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;

    mutex_lock(&local_vttydev->lock);

    /* Drops reference to tty */
    tty_port_hangup(tty->port);

    if (tty && C_HUPCL(tty))
        sp_update_modem_lines(tty, 0, TIOCM_DTR | TIOCM_RTS);

    mutex_unlock(&local_vttydev->lock);

    dev_dbg(tty->dev, "hanged up !");
}

/*
 * Invoked to execute ioctl command TIOCGICOUNT to get the number of interrupts. Both 1->0 and
 * 0->1 transitions are counted, except for RI, where only 0->1 transitions are counted.
 *
 * @tty: tty device whose interrupts information is to be collected.
 * @icount: memory location from which tty core will copy data to user space buffer.
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int sp_get_icount(struct tty_struct *tty, struct serial_icounter_struct *icount)
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
 * emptied without involvement of tty driver. The driver is generally expected not to keep data but send
 * it to tty layer as soon as possible when it receives data.
 *
 * The virtual tty device created by this driver does not have any local buffer.
 *
 * @tty: tty device whose buffer should be flushed.
 */
static void sp_flush_buffer(struct tty_struct *tty)
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
 * @tty: tty device who is sending this character.
 * @ch: character to be sent (typically it is XOFF or XON).
 */
static void sp_send_xchar(struct tty_struct *tty, char ch)
{
    int was_paused = 0;
    struct vtty_dev *local_vttydev = index_manager[tty->index].vttydev;

    was_paused = local_vttydev->tx_paused;
    if(was_paused)
        local_vttydev->tx_paused = 0;

    sp_put_char(tty, ch);
    if(was_paused)
        local_vttydev->tx_paused = 1;
}

/*
 * Invoked by tty layer in response to tcdrain() call.
 *
 * @tty: tty device who should try to empty its output buffer.
 * @timeout: timeout value.
 */
static void sp_wait_until_sent(struct tty_struct *tty, int timeout)
{
}

/*
 * Asserts or de-asserts serial lines as specified by raise argument. It is handled by 
 * this driver exlicitly as of now because this will eventually become entirely internal 
 * to the tty port as per linux kernel development plan.
 *
 * @port serial port whose lines is to be updated
 * @raise 1 if line should be asserted, 0 if line should be de-asserted
static void sp_port_dtr_rts(struct tty_port *port, int raise) 
{
    if(raise == 1)
        sp_update_modem_lines(port->tty, TIOCM_DTR | TIOCM_RTS, 0);
    else
        sp_update_modem_lines(port->tty, 0, TIOCM_DTR | TIOCM_RTS);
}
 */

/*
 * Checks if the given port has received its carrier detect line raised.
 *
 * @port: serial port whose carrier detect line is to be checked.
 *
 * @return 1 if the carrier is raised otherwise 0.
 */
static int sp_port_carrier_raised(struct tty_port *port)
{
    struct vtty_dev *local_vttydev = index_manager[port->tty->index].vttydev;
    return (local_vttydev->msr_reg & SP_MSR_DCD) ? 1 : 0;
}

/*
 * Shutdowns the given serial port, typically it is used to shutdown hardware.
 *
 * @port: tty port to shut down.
 */
static void sp_port_shutdown(struct tty_port *port)
{
}

/*
 * Activate the given serial port as opposed to shutdown. Typically used to turn something on hardware.
 *
 * @port: tty port to activate.
 * @tty: tty corresponding to the given port.
 * @return 0 on success.
 */
static int sp_port_activate(struct tty_port *port, struct tty_struct *tty)
{
    return 0;
}

/*
 * Invoked when tty is going to be destroyed and driver should release resources.
 *
 * @port: port which is going to be freed.
 */
static void sp_port_destruct(struct tty_port *port)
{
    //kfree(port);
}

/*
 * Gives next available index and last used index for virtual tty devices created. Invoke as shown below:
 * $ head -c 52 /proc/sp_vmpscrdk
 * 
 * @file: file for proc file.
 * @buf: user space buffer that will contain data when this function returns.
 * @size: number of character returned in buf.
 * @ppos: offset position from where to read data.
 *
 * @return number of bytes copied to user buffer on success or negative error code on error.
 */
static ssize_t sp_vcard_proc_read(struct file *file, char __user *buf, size_t size, loff_t *ppos)
{
    int x = 0;
    int ret = 0;
    int val = 0;
    char data[64];
    int first_avail_idx = -1;
    int second_avail_idx = -1;
    struct vtty_dev *lbvttydev = NULL;
    struct vtty_dev *nm1vttydev = NULL;
    struct vtty_dev *nm2vttydev = NULL;

    memset(data, '\0', 64);

    if(size != 52)
        return -EINVAL;

    mutex_lock(&adaptlock);

    /* Find next available free index */
    for(x = 0; x < max_num_vtty_dev; x++) {
        if(index_manager[x].index == -1) {
            if(first_avail_idx == -1) {
                first_avail_idx = x;
            }else {
                second_avail_idx = x;
                break;
            }
        }
    }

    if((first_avail_idx != -1) && (second_avail_idx != -1)) {
        val = 2;
    }else if((first_avail_idx != -1) && (second_avail_idx == -1)) {
        val = 1;
    }else if((first_avail_idx == -1) && (second_avail_idx == -1)) {
        val = 0;
    }else {
        /* will not happen */
    }

    if(last_lbdev_idx == -1) {
        if(last_nmdev1_idx == -1) {
            snprintf(data, 64, "xxxxx#xxxxx-xxxxx#%05d-%05d#%d#x-x#x-x#x-x#x#x#x\r\n", first_avail_idx, second_avail_idx, val);
        }else {
            nm1vttydev = index_manager[last_nmdev1_idx].vttydev;
            nm2vttydev = index_manager[last_nmdev2_idx].vttydev;
            snprintf(data, 64, "xxxxx#%05d-%05d#%05d-%05d#%d#x-x#%d-%d#%d-%d#x#%d#%d\r\n", last_nmdev1_idx, last_nmdev2_idx,
                    first_avail_idx, second_avail_idx, val, nm1vttydev->rts_mappings, nm1vttydev->dtr_mappings,
                    nm2vttydev->rts_mappings, nm2vttydev->dtr_mappings, nm1vttydev->set_odtr_at_open, nm2vttydev->set_odtr_at_open);
        }
    }else {
        if(last_nmdev1_idx == -1) {
            lbvttydev = index_manager[last_lbdev_idx].vttydev;
            snprintf(data, 64, "%05d#xxxxx-xxxxx#%05d-%05d#%d#%d-%d#x-x#x-x#%d#x#x\r\n", last_lbdev_idx, first_avail_idx,
                    second_avail_idx, val, lbvttydev->rts_mappings, lbvttydev->dtr_mappings, lbvttydev->set_odtr_at_open);
        }else {
            lbvttydev = index_manager[last_lbdev_idx].vttydev;
            nm1vttydev = index_manager[last_nmdev1_idx].vttydev;
            nm2vttydev = index_manager[last_nmdev2_idx].vttydev;
            snprintf(data, 64, "%05d#%05d-%05d#%05d-%05d#%d#%d-%d#%d-%d#%d-%d#%d#%d#%d\r\n", last_lbdev_idx, last_nmdev1_idx,
                    last_nmdev2_idx, first_avail_idx, second_avail_idx, val, lbvttydev->rts_mappings, lbvttydev->dtr_mappings,
                    nm1vttydev->rts_mappings, nm1vttydev->dtr_mappings, nm2vttydev->rts_mappings, nm2vttydev->dtr_mappings, 
                    lbvttydev->set_odtr_at_open, nm1vttydev->set_odtr_at_open, nm2vttydev->set_odtr_at_open);
        }
    }

    mutex_unlock(&adaptlock);

    ret = copy_to_user(buf, &data, 52);
    if(ret)
        return -EFAULT;

    return 52;
}

/*
 * Extract pin mappings from local to remote tty devices.
 *
 * @data: dat to be parsed.
 * @x: starting index in array for parsing.
 * 
 * @return 0 on success or negative error code on failure.
 */
static int sp_extract_pin_mapping(char data[], int x) 
{
    int i = 0;
    int mapping = 0;

    for(i=0; i<8; i++) {
        if(data[x] == '8') {
            mapping |= SP_CON_CTS;
        }else if(data[x] == '1') {
            mapping |= SP_CON_DCD;
        }else if(data[x] == '6') {
            mapping |= SP_CON_DSR;
        }else if(data[x] == '9') {
            mapping |= SP_CON_RI;
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
 * card.
 *
 * Standard DB9 pin assignment: 1 - DCD, 2 - RX, 3 - TX, 4 - DTR, 5 - GND, 6 - DSR, 7 - RTS, 8 - CTS, 9 - RI.
 *
 * Assignment 7-8 means connect local RTS pin to remote CTS pin. Assignment 4-1,6 means connect local DTR to
 * remote DSR and DCD pins. Assignment 7-x means leave local RTS pin unconnected. The 'y' at last will raise
 * remote DCD pin when local device is opened. When removing tty device, if the given device is one of the
 * device in a null modem pair, coupled device will also be deleted automatically. The command string is case
 * sensitive. The last and second last x/y defines if DTR should be raised or not when port is opened.
 *
 * 1. Create standard null modem connection:
 * $echo "gennm#vdev1#vdev2#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y" > /proc/sp_vmpscrdk
 *
 * 2. Create standard loop back connection:
 * $echo "genlb#vdevt#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#x" > /proc/sp_vmpscrdk
 *
 * 3. Delete a particular tty device:
 * $echo "del#vdevt#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" > /proc/sp_vmpscrdk
 *
 * 4. Delete all virtual tty devices in this card:
 * $echo "del#xxxxx#xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" > /proc/sp_vmpscrdk
 *
 * @file: file representing sp proc file.
 * @buf: command supplied by the caller.
 * @length: length of the command.
 * @ppos: offset in file.
 *
 * @return number of bytes consumed by this function on success or negative error code on failure.
 */
static ssize_t sp_vcard_proc_write(struct file *file, const char __user *buf, size_t length, loff_t * ppos)
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
    struct device *device1 = NULL;
    struct device *device2 = NULL;
    struct tty_struct *tty;

    if(length == 2) {
        memcpy(data, "gennm#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y", 61);
    }else if(length == 3) {
        memcpy(data, "genlb#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#x", 61);
    }else if((length > 60) && (length < 63)) {
        if(copy_from_user(data, buf, length) != 0) {
            return -EFAULT;
        }
    }else {
        return -EINVAL;
    }
    data[62] = '\0';

    /* Initial sanitization */
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
        /* Create device(s) command sent */

        /* Extract 1st device index (for both null modem and loop back) */
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
            if((vdev1idx < 0) || (vdev1idx > 65535))
                return -EINVAL;
        }

        vttydev1 = (struct vtty_dev *) kcalloc(1, sizeof(struct vtty_dev), GFP_KERNEL);
        if(vttydev1 == NULL)
            return -ENOMEM;

        /* Extract 2nd device index if null modem pair is to be created */
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
                if((vdev2idx < 0) || (vdev2idx > 65535)) {
                    ret = -EINVAL;
                    goto fail_arg;
                }

            }

            vttydev2 = (struct vtty_dev *) kcalloc(1, sizeof(struct vtty_dev), GFP_KERNEL);
            if(vttydev2 == NULL) {
                ret = -ENOMEM;
                goto fail_arg;
            }
        }

        /* rts mappings (dev1) */
        if((data[18] != '7') || (data[19] != '-')) {
            ret = -EINVAL;
            goto fail_arg;
        }
        ret = sp_extract_pin_mapping(data, 20);
        if(ret < 0)
            goto fail_arg;
        vdev1rts = ret;

        if((data[27] != '#') || (data[28] != '4') || (data[29] != '-'))
            goto fail_arg;

        /* dtr mapping (dev1) */
        ret = sp_extract_pin_mapping(data, 30);
        if(ret < 0)
            goto fail_arg;
        vdev1dtr = ret;

        if(data[37] != '#')
            goto fail_arg;

        if(is_loopback != 1) {
            /* rts mappings (dev2) */
            if((data[38] != '7') || (data[39] != '-')) {
                ret = -EINVAL;
                goto fail_arg;
            }
            ret = sp_extract_pin_mapping(data, 40);
            if(ret < 0)
                goto fail_arg;
            vdev2rts = ret;

            /* dtr mapping (dev2) */
            if((data[47] != '#') || (data[48] != '4') || (data[49] != '-'))
                goto fail_arg;

            ret = sp_extract_pin_mapping(data, 50);
            if(ret < 0)
                goto fail_arg;
            vdev2dtr = ret;

            if(data[57] != '#')
                goto fail_arg;
        }

        /* Create serial port (tty device) with lock taken to ensure correctness of index in use
           and associated data */
        mutex_lock(&adaptlock);

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
                mutex_unlock(&adaptlock);
                goto fail_arg;
            }
        }
        if(i == -1) {
            ret = -ENOMEM;
            mutex_unlock(&adaptlock);
            goto fail_arg;
        }

        /* Initialize meta information and create 1st serial port */
        if(data[58] == 'y')
            vttydev1->set_odtr_at_open = 1;
        else
            vttydev1->set_odtr_at_open = 0;
        vttydev1->own_tty = NULL;
        vttydev1->peer_tty = NULL;
        vttydev1->own_index = i;
        vttydev1->peer_index = i;
        vttydev1->rts_mappings = vdev1rts;
        vttydev1->dtr_mappings = vdev1dtr;
        vttydev1->msr_reg = 0;
        vttydev1->mcr_reg = 0;
        vttydev1->waiting_msr_chg = 0;
        vttydev1->tx_paused = 0;
        vttydev1->faulty_cable = 0;
        index_manager[i].index = i;
        index_manager[i].vttydev = vttydev1;
        mutex_init(&vttydev1->lock);

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
                    mutex_unlock(&adaptlock);
                    goto fail_arg;
                }
            }
            if(y == -1) {
                ret = -ENOMEM;
                mutex_unlock(&adaptlock);
                goto fail_arg;
            }

            /* Initialize meta information and create 2nd serial port */
            if(data[60] == 'y')
                vttydev2->set_odtr_at_open = 1;
            else
                vttydev2->set_odtr_at_open = 0;
            vttydev2->set_pdtr_at_open = vttydev1->set_odtr_at_open;
            vttydev1->set_pdtr_at_open = vttydev2->set_odtr_at_open;
            vttydev1->own_index = i;
            vttydev1->peer_index = y;
            vttydev2->own_index = y;
            vttydev2->peer_index = i;
            vttydev2->own_tty = NULL;
            vttydev2->peer_tty = NULL;
            vttydev2->rts_mappings = vdev2rts;
            vttydev2->dtr_mappings = vdev2dtr;
            vttydev2->msr_reg = 0;
            vttydev2->mcr_reg = 0;
            vttydev2->waiting_msr_chg = 0;
            vttydev2->tx_paused = 0;
            vttydev2->faulty_cable = 0;
            index_manager[y].index = y;
            index_manager[y].vttydev = vttydev2;
            mutex_init(&vttydev2->lock);
        }

        device1 = tty_register_device(spvtty_driver, i, NULL);
        if(device1 == NULL) {
            ret = -ENOMEM;
            mutex_unlock(&adaptlock);
            goto fail_arg;
        }

        vttydev1->device = device1;
        dev_set_drvdata(device1, vttydev1);

        x = sysfs_create_group(&device1->kobj, &sp_info_attr_group);
        if(x < 0) {
            tty_unregister_device(spvtty_driver, i);
            mutex_unlock(&adaptlock);
            goto fail_arg;
        }

        if(is_loopback != 1) {
            device2 = tty_register_device(spvtty_driver, y, NULL);
            if(device2 == NULL) {
                ret = -ENOMEM;
                mutex_unlock(&adaptlock);
                goto fail_register;
            }

            vttydev2->device = device2;
            dev_set_drvdata(device2, vttydev2);

            x = sysfs_create_group(&device2->kobj, &sp_info_attr_group);
            if(x < 0) {
                tty_unregister_device(spvtty_driver, y);
                index_manager[y].index = -1;
                mutex_unlock(&adaptlock);
                goto fail_register;
            }

            last_nmdev1_idx = i;
            last_nmdev2_idx = y;
            ++total_nm_pair;

            if((vttydev1->dtr_mappings != (SP_CON_DSR | SP_CON_DCD)) || (vttydev1->rts_mappings != SP_CON_CTS)
                    || (vttydev1->set_odtr_at_open != 1) || (vttydev2->dtr_mappings != (SP_CON_DSR | SP_CON_DCD))
                    || (vttydev2->rts_mappings != SP_CON_CTS) || (vttydev2->set_odtr_at_open != 1)) {
                vttydev1->odevtyp = CNM;
                vttydev2->odevtyp = CNM;
            }else {
                vttydev1->odevtyp = SNM;
                vttydev2->odevtyp = SNM;
            }            
        }else {
            last_lbdev_idx = i;
            ++total_lb_devs;

            /* device type */
            if((vttydev1->dtr_mappings != (SP_CON_DSR | SP_CON_DCD)) || (vttydev1->rts_mappings != SP_CON_CTS)
                    || (vttydev1->set_odtr_at_open != 1)) {
                vttydev1->odevtyp = CLB;
            }else {
                vttydev1->odevtyp = SLB;
            }
        }

        mutex_unlock(&adaptlock);
    }
    else {
        /* Destroy device command sent */

        if((total_nm_pair <= 0) && (total_lb_devs <= 0))
            return length;

        /* An application may forget to close serial port or it might have been crashed resulting in
         * unclosed port and hence leaked resources. We handle such scenarios as disconnected event
         * as done in case of a plug and play for example usb device. Application is running, port
         * is opened and then suddenly user removes tty device. */

        if(data[8] == 'x') {

            /* Delete all virtual devices */

            mutex_lock(&adaptlock);

            /* First tty must be released and than port. */
            for(x=0; x < max_num_vtty_dev; x++) {
                if (index_manager[x].index != -1) {

                    vttydev1 = index_manager[x].vttydev;
                    if (vttydev1 != NULL) {

                        sysfs_remove_group(&vttydev1->device->kobj, &sp_info_attr_group);

                        if (vttydev1->own_tty && vttydev1->own_tty->port) {
                            tty = tty_port_tty_get(vttydev1->own_tty->port);
                            if (tty) {
                                tty_vhangup(tty);
                                tty_kref_put(tty);
                            }
                        }
                        tty_unregister_device(spvtty_driver, index_manager[x].index);
                        kfree(index_manager[x].vttydev);
                    }
                    index_manager[x].index = -1;
                }
            }

            total_nm_pair = 0;
            total_lb_devs = 0;
            last_lbdev_idx  = -1;
            last_nmdev1_idx = -1;
            last_nmdev2_idx = -1;

            mutex_unlock(&adaptlock);
        }
        else {

            /* Delete a specific virtual device */

            x = 4;
            memset(tmp, '\0', sizeof(tmp));
            i = 0;
            for(i=0; i<5; i++) {
                tmp[i] = data[x];
                x++;
            }

            x = -1;
            y = -1;

            ret = kstrtouint(tmp, 10, &vdev1idx);
            if(ret != 0)
                return ret;

            if((vdev1idx >= 0) && (vdev1idx <= 65535) && (index_manager[vdev1idx].index != -1)) {
                mutex_lock(&adaptlock);

                x = index_manager[vdev1idx].index;
                vttydev1 = index_manager[x].vttydev;
                sysfs_remove_group(&vttydev1->device->kobj, &sp_info_attr_group);
                tty_unregister_device(spvtty_driver, index_manager[x].index);
                if (vttydev1 && vttydev1->own_tty && vttydev1->own_tty->port) {
                    tty = tty_port_tty_get(vttydev1->own_tty->port);
                    if (tty) {
                        tty_vhangup(tty);
                        tty_kref_put(tty);
                    }
                }

                if (vttydev1->own_index != vttydev1->peer_index) {
                    y = index_manager[vttydev1->peer_index].index;
                    vttydev2 = index_manager[y].vttydev;
                    sysfs_remove_group(&vttydev2->device->kobj, &sp_info_attr_group);
                    tty_unregister_device(spvtty_driver, index_manager[y].index);
                    if (vttydev2 && vttydev2->own_tty && vttydev2->own_tty->port) {
                        tty = tty_port_tty_get(vttydev2->own_tty->port);
                        if (tty) {
                            tty_vhangup(tty);
                            tty_kref_put(tty);
                        }
                    }
                }

                if (x != -1) {
                    kfree(index_manager[x].vttydev);
                    index_manager[x].index = -1;
                }
                if (y != -1) {
                    kfree(index_manager[y].vttydev);
                    index_manager[y].index = -1;
                    --total_nm_pair;
                }else {
                    --total_lb_devs;
                }

                mutex_unlock(&adaptlock);

            }else {
                return -EINVAL;
            }
        }
    }

    return length;

    fail_register:
    sysfs_remove_group(&device1->kobj, &sp_info_attr_group);
    tty_unregister_device(spvtty_driver, i);

    fail_arg:
    index_manager[i].index = -1;

    if(vttydev2 != NULL)
        kfree(vttydev2);

    if(vttydev1 != NULL)
        kfree(vttydev1);

    return ret;
}

/*
 * Invoked when user space process opens /proc/sp_vmpscrdk file to create/destroy
 * virtual tty device(s).
 *
 * @inode: inode in file system corresponding to this file
 * @file: file representing sp proc file
 *
 * @return 0 on success.
 */
static int sp_vcard_proc_open(struct inode *inode, struct  file *file)
{
    return 0;
}

/*
 * Invoked when user space process closes /proc/sp_vmpscrdk file.
 *
 * @inode: inode in file system corresponding to this file.
 * @file: file representing sp proc file.
 *
 * @return 0 on success.
 */
static int sp_vcard_proc_close(struct inode *inode, struct file *file)
{
    return 0;
}

static const struct file_operations sp_vcard_proc_fops = {
        .owner   = THIS_MODULE,
        .open    = sp_vcard_proc_open,
        .read    = sp_vcard_proc_read,
        .write   = sp_vcard_proc_write,
        .release = sp_vcard_proc_close,
};

static const struct tty_operations sp_serial_ops = {
        .install         = sp_install,
        .cleanup         = sp_cleanup,
        .open            = sp_open,
        .close           = sp_close,
        .write           = sp_write,
        .put_char        = sp_put_char,
        .flush_chars     = sp_flush_chars,
        .write_room      = sp_write_room,
        .chars_in_buffer = sp_chars_in_buffer,
        .ioctl           = sp_ioctl,
        .set_termios     = sp_set_termios,
        .throttle        = sp_throttle,
        .unthrottle      = sp_unthrottle,
        .stop            = sp_stop,
        .start           = sp_start,
        .hangup          = sp_hangup,
        .break_ctl       = sp_break_ctl,
        .flush_buffer    = sp_flush_buffer,
        .wait_until_sent = sp_wait_until_sent,
        .send_xchar      = sp_send_xchar,
        .tiocmget        = sp_tiocmget,
        .tiocmset        = sp_tiocmset,
        .get_icount      = sp_get_icount,
};

/*
 * Invoked when this driver is loaded. If the user supplies correct number of virtual devices
 * to be created when this module is loaded, the virtual devices will be made, otherwise they
 * will not be made and have to be created using proc file.
 *
 * For example; if this driver should support upto maximum 20 devices and create 1 null-modem pair
 * and 1 loop back device, load this driver module as shown below:
 *
 * $ insmod ./tty2com.ko max_num_vtty_dev=20 init_num_nm_pair=1 init_num_lb_dev=1
 *
 * First all the null modem pair will be created and then loop back device will be created if
 * creating virtual devices at module load time is specified. For example for above command line:
 * 1. null modem pair : /dev/tty2com0 <---> /dev/tty2com1
 * 2. loop back       : /dev/tty2com2
 *
 * Further if the minor number used by this driver conflicts with an existing other driver, specifying 
 * a different minor number can be done as shown below :
 *
 * $ insmod ./tty2com.ko max_num_vtty_dev=5000 init_num_nm_pair=1 init_num_lb_dev=1 minor_begin=20
 * 
 * This driver does not set CLOCAL by default. This means that the open() system call will block
 * until it find its carrier detect line raised. Application should use O_NONBLOCK/O_NDELAY flag
 * if it does not want to wait for DCD line change.
 *
 * @return: 0 on success or negative error code on failure.
 */
static int __init sp_tty2com_init(void)
{
    int x = 0;
    int ret = 0;
    struct proc_dir_entry *pde = NULL;

    /* Causes allocation of memory for 'struct tty_port' and 'struct cdev' for all tty devices this
     * driver can handle. */
    spvtty_driver = tty_alloc_driver(max_num_vtty_dev, 0);
    if (!spvtty_driver)
        return -ENOMEM;

    spvtty_driver->owner = THIS_MODULE;
    spvtty_driver->driver_name = "tty2com";
    spvtty_driver->name = "tty2com";
    spvtty_driver->major = 0;
    spvtty_driver->minor_start = minor_begin;
    spvtty_driver->type = TTY_DRIVER_TYPE_SERIAL;
    spvtty_driver->subtype = SERIAL_TYPE_NORMAL;
    spvtty_driver->flags = TTY_DRIVER_REAL_RAW | TTY_DRIVER_RESET_TERMIOS | TTY_DRIVER_DYNAMIC_DEV;
    spvtty_driver->init_termios = tty_std_termios;
    spvtty_driver->init_termios.c_cflag = B9600 | CS8 | CREAD | HUPCL;
    spvtty_driver->init_termios.c_ispeed = spvtty_driver->init_termios.c_ospeed = 9600;

    tty_set_operations(spvtty_driver, &sp_serial_ops);

    ret = tty_register_driver(spvtty_driver);
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
    pde = proc_create("sp_vmpscrdk", (S_IRUGO | S_IWUGO), NULL, &sp_vcard_proc_fops);
    if(pde == NULL) {
        ret = -ENOMEM;
        goto failed_proc;
    }

    /* If module was supplied parameters, create null-modem and loopback virtual tty devices */
    if (((2 * init_num_nm_pair) + init_num_lb_dev) <= max_num_vtty_dev) {
        for(x=0; x < init_num_nm_pair; x++) {
            ret = sp_vcard_proc_write(NULL, NULL, 2, NULL);
            if(ret < 0)
                pr_warning("Can't create null modem pair at index %d,  error code: %d\n", x, ret);
        }
        for(x=0; x < init_num_lb_dev; x++) {
            ret = sp_vcard_proc_write(NULL, NULL, 3, NULL);
            if(ret < 0)
                pr_warning("Can't create loop back device at index %d, error code: %d\n", x, ret);
        }
    }else {
        pr_warning("Not creating specified devices due to invalid total !\n");
    }

    pr_info("%s %s\n", DRIVER_DESC, DRIVER_VERSION);
    return 0;

    failed_proc:
    kfree(index_manager);
    failed_alloc:
    tty_unregister_driver(spvtty_driver);
    failed_register:
    put_tty_driver(spvtty_driver);
    return ret;
}

/*
 * Invoked when this driver is unloaded. A tty device may have been registered but application might
 * not have opened it. This driver follows similar approach as followed in a plug and play device
 * driver's disconnected logic. For clean exit, kicking out dependents, releasing resources hangup is
 * used.
 */
static void __exit sp_tty2com_exit(void)
{
    int x = 0;
    struct vtty_dev *vttydev = NULL;
    struct tty_struct *tty;

    remove_proc_entry("sp_vmpscrdk", NULL);

    for(x=0; x < max_num_vtty_dev; x++) {
        if (index_manager[x].index != -1) {

            vttydev = index_manager[x].vttydev;
            sysfs_remove_group(&vttydev->device->kobj, &sp_info_attr_group);
            tty_unregister_device(spvtty_driver, index_manager[x].index);

            if (vttydev && vttydev->own_tty && vttydev->own_tty->port) {
                tty = tty_port_tty_get(vttydev->own_tty->port);
                if (tty) {
                    tty_vhangup(tty);
                    tty_kref_put(tty);
                }
                kfree(index_manager[x].vttydev);
            }
        }
    }

    kfree(index_manager);

    tty_unregister_driver(spvtty_driver);
    put_tty_driver(spvtty_driver);

    pr_info("Good bye !\n");
}

module_init(sp_tty2com_init);
module_exit(sp_tty2com_exit);

module_param(max_num_vtty_dev, ushort, 0);
MODULE_PARM_DESC(max_num_vtty_dev, "Maximum number of virtual tty devices this driver can create.");

module_param(init_num_nm_pair, ushort, 0);
MODULE_PARM_DESC(init_num_nm_pair, "Number of standard null modem pairs to be created at load time.");

module_param(init_num_lb_dev, ushort, 0);
MODULE_PARM_DESC(init_num_lb_dev, "Number of standard loopback tty devices to be created at load time.");

module_param(minor_begin, int, 0);
MODULE_PARM_DESC(minor_begin, "Minor number of device nodes i.e. starting index of device nodes.");

MODULE_AUTHOR( DRIVER_AUTHOR );
MODULE_DESCRIPTION( DRIVER_DESC );
MODULE_LICENSE("GPL v2");
MODULE_VERSION( DRIVER_VERSION );
