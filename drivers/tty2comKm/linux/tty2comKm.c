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
 * from 0 to N number of virtual serial(tty) devices. The virtual tty devices created by this adaptor are used 
 * in exactly the same way as the real tty devices using termios APIs.
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
#include <linux/mutex.h>
#include <asm/uaccess.h>
#include <linux/proc_fs.h>

/* Module information */
#define DRIVER_VERSION "v1.0"
#define DRIVER_AUTHOR "Rishi Gupta"
#define DRIVER_DESC "Serial port null modem emulation driver (kernel mode)"

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

/* Pin out configurations definitions */
#define CON_CTS   0x01
#define CON_DCD   0x02
#define CON_DSR   0x04
#define CON_RI    0x08

/* Modem control lines definitions */
#define SCM_MCR_DTR    0x01
#define SCM_MCR_RTS    0x02
#define SCM_MCR_LOOP   0x04
#define SCM_MSR_CTS    0x08
#define SCM_MSR_CD     0x10
#define SCM_MSR_RI     0x20
#define SCM_MSR_DSR    0x40

/* Represent a virtual tty device in this virtual adaptor */
struct vtty_dev {
    int peer_index;
    int msr_reg; // modem status register
    int mcr_reg; // modem control register
    int open_count;
    int rts_mapping;
    int dtr_mapping;
    int set_dcd_atopen;
    spinlock_t lock;
    struct tty_struct *peer_tty;
    struct serial_struct serial;
    struct serial_icounter_struct icount;
    struct device *device; //TODO CHK IF NEEDED OR NOT
};

struct vtty_info {
    int index;
    struct vtty_dev *vttydev;
};

/* These values may be overriden if module is loaded with parameters */
static ushort max_num_vtty_dev = VTTY_DEV_MAX;
static ushort init_num_nm_pair = 0;
static ushort init_num_lb_dev = 0;

/* Describes this driver kernel module */
static struct tty_driver *scmtty_driver;

/* Used when creating or destroying virtual tty devices */
static DEFINE_SPINLOCK(adaptlock);        // atomically create/destroy tty devices
struct vtty_info *index_manager = NULL;   //  keep track of indexes in use currently 

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
    //ret = copy_to_user(buf, "rishi", sizeof("rishi"));//TODO
    return ret;
}

static int extract_mapping(char data[], int x) {
    int i = 0;
    int mapping = 0;
    for(i=0; i<8; i++) {
        if(data[x] == '8') {
            mapping |= CON_CTS;
        }else if(data[x] == '1') {
            mapping |= CON_DCD;
        }else if(data[x] == '6') {
            mapping |= CON_DSR;
        }else if(data[x] == '9') {
            mapping |= CON_RI;
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
 * Standard DB9 pin assignment: 1 - DCD, 2 - RX, 3 - TX, 4 - DTR, 5 - GND, 6 - DSR, 7 - RTS, 8 - CTS, 9 - RI
 * Assignment 7-8 means connect local RTS pin to remote CTS pin. Assignment 4-1,6 meane connect local DTR to 
 * remote DSR and DCD pins. Assignment 7-x means leave local RTS pin unconnected. The 'y' at last will raise 
 * DCD pin when device is opened. When removing tty device, if the given device is one of the device in null 
 * modem pair, coupled device will also be deleted aautomatically.
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
 * @file
 * @buf
 * @length
 * @ppos
 * 
 * @return number of bytes consumed by this function on success or negative error code on failure.
 */
static ssize_t scmtty_vadapt_proc_write(struct file *file, const char __user *buf, size_t length, loff_t * ppos)
{
    int x = 0;
    int y = 0;
    int i = 0;
    int ret = 0;
    int create = 0;
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

    if(length == 62) {
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
        vttydev1 = (struct vtty_dev *) kcalloc(1, sizeof(struct vtty_dev *), GFP_KERNEL);
        if(vttydev1 == NULL)
            return -ENOMEM;

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
            vttydev2 = (struct vtty_dev *) kcalloc(1, sizeof(struct vtty_dev *), GFP_KERNEL);
            if(vttydev2 == NULL) {
                ret = -ENOMEM;
                goto fail_arg;
            }
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

        // The (driver->flags & TTY_DRIVER_DYNAMIC_ALLOC) will evaluate to zero as we are allocating 
        // devices dynamically. This will create a kernel device and register it with kernel subsystem(s) 
        // as appropriate, associating the device at given index of this driver.
        spin_lock(&adaptlock);

        i = -1;
        if(vdev1idx == -1) {
            for(x=0; x < max_num_vtty_dev;  x++) {
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
                goto fail_arg;
            }
        }
        if(i == -1) {
            ret = -ENOMEM;
            goto fail_arg;
        }
        device1 = tty_register_device(scmtty_driver, i, NULL);
        if(device1 == NULL) {
            ret = -ENOMEM;
            goto fail_arg;
        }
        index_manager[i].index = i;
        index_manager[i].vttydev = vttydev1;

        y = -1;
        if(is_loopback != 1) {
            if(vdev2idx == -1) {
                for(x=0; x < max_num_vtty_dev;  x++) {
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
                    goto fail_arg;
                }
            }
            if(y == -1) {
                ret = -ENOMEM;
                goto fail_arg;
            }
            device2 = tty_register_device(scmtty_driver, y, NULL);
            if(device2 == NULL) {
                ret = -ENOMEM;
                goto fail_register;
            }
            index_manager[y].index = y;
            index_manager[y].vttydev = vttydev2;
        }

        spin_unlock(&adaptlock);
        
        // do minimum required initialization
        spin_lock_init(&vttydev1->lock);
        if(data[59] == 'y')
            vttydev1->set_dcd_atopen = 1;
        else
            vttydev1->set_dcd_atopen = 0;
        
        if(is_loopback != 1) {
            spin_lock_init(&vttydev2->lock);
            if(data[61] == 'y')
                vttydev2->set_dcd_atopen = 1;
            else
                vttydev2->set_dcd_atopen = 0;
        }
    }
    else {
        if(data[4] == 'x') {
            for(x=0; x < max_num_vtty_dev;  x++) {
                if(index_manager[x].index != -1) {
                    tty_unregister_device(scmtty_driver, index_manager[x].index);
                    kfree(index_manager[x].vttydev);
                    index_manager[x].index = -1;
                }
            }
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
                tty_unregister_device(scmtty_driver, index_manager[vdev1idx].index);
                kfree(index_manager[vdev1idx].vttydev);
                index_manager[vdev1idx].index = -1;
            }else {
                return -EINVAL;
            }
        }
    }

    return length;

    fail_register:
    tty_unregister_device(scmtty_driver, i);
    fail_arg:
    if(vttydev2 != NULL)
        kfree(vttydev2);
    if(vttydev1 != NULL)
        kfree(vttydev1);
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
 * For example; to support maximum 20 devices, 1 null-modem pair and 1 loop back device run
 * following command:
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

    remove_proc_entry("scmtty_vadaptkm", NULL);

    for(x=0; x < max_num_vtty_dev;  x++) {
        if(index_manager[x].index != -1) {
            tty_unregister_device(scmtty_driver, index_manager[x].index);
            kfree(index_manager[x].vttydev);
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
MODULE_LICENSE("Proprietary");
MODULE_VERSION( DRIVER_VERSION );
   
