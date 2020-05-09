// SPDX-License-Identifier: GPL-2.0
/*
 * Serial port null modem emulation driver
 *
 * Copyright (c) 2020, Rishi Gupta <gupt21@gmail.com>
 *
 * See Documentation/devicetree/bindings/serial/ttyVS.yaml for more
 * information on using this driver.
 */

/*
 * Virtual multi-port serial card:
 * This driver implements a virtual multiport serial card in such a
 * way that the virtual card can have 0 to N number of virtual serial
 * ports (tty devices). The virtual tty devices created by this card
 * are used in exactly the same way as the real tty devices using
 * standard termios and Linux/Posix APIs.
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
#include <linux/mutex.h>
#include <linux/device.h>
#include <linux/miscdevice.h>

/*
 * By default 128 devices can be created. This number can be
 * overridden through max_num_vs_dev module parameter.
 */
#define DEFAULT_VS_DEV_MAX  128

/* Pin out configurations definitions */
#define VS_CON_CTS    0x0001
#define VS_CON_DCD    0x0002
#define VS_CON_DSR    0x0004
#define VS_CON_RI     0x0008

/* Modem control register definitions */
#define VS_MCR_DTR    0x0001
#define VS_MCR_RTS    0x0002
#define VS_MCR_LOOP   0x0004

/* Modem status register definitions */
#define VS_MSR_CTS    0x0008
#define VS_MSR_DCD    0x0010
#define VS_MSR_RI     0x0020
#define VS_MSR_DSR    0x0040

/* UART frame structure definitions */
#define VS_CRTSCTS       0x0001
#define VS_XON           0x0002
#define VS_NONE          0X0004
#define VS_DATA_5        0X0008
#define VS_DATA_6        0X0010
#define VS_DATA_7        0X0020
#define VS_DATA_8        0X0040
#define VS_PARITY_NONE   0x0080
#define VS_PARITY_ODD    0x0100
#define VS_PARITY_EVEN   0x0200
#define VS_PARITY_MARK   0x0400
#define VS_PARITY_SPACE  0x0800
#define VS_STOP_1        0x1000
#define VS_STOP_2        0x2000

/* Constants for the device type (odevtyp) */
#define VS_SNM 0x0001
#define VS_CNM 0x0002
#define VS_SLB 0x0003
#define VS_CLB 0x0004

/* Represents a virtual tty device in this virtual card */
struct vs_dev {
	/* index for this device in tty core */
	unsigned int own_index;
	/* index of the device to which this device is connected */
	unsigned int peer_index;
	/* shadow modem status register */
	int msr_reg;
	/* shadow modem control register */
	int mcr_reg;
	/* rts line connections for this device */
	int rts_mappings;
	/* dtr line connections for this device */
	int dtr_mappings;
	int set_odtr_at_open;
	int set_pdtr_at_open;
	int odevtyp;
	/* mutual exclusion at device level */
	struct mutex lock;
	int is_break_on;
	/* currently active baudrate */
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

/*
 * Associates index of the device as managed by index manager
 * to its device specific data.
 */
struct vs_info {
	int index;
	struct vs_dev *vsdev;
};

/*
 * Root of database of all devices managed by this driver. Devices
 * are referenced using this root. For ex; to retreive struct vs_dev
 * of 3rd device use db[3].vsdev.
 */
static struct vs_info *db;

/*
 * Synchronization at adapter level for ex; creating/destroying
 * devices must be atomic.
 */
static DEFINE_MUTEX(adaptlock);

/* Describes this driver kernel module */
static struct tty_driver *ttyvs_driver;

static int minor_begin;
static ushort max_num_vs_dev = DEFAULT_VS_DEV_MAX;
static ushort init_num_nm_pair;
static ushort init_num_lb_dev;

static ushort total_nm_pair;
static ushort total_lb_devs;
static int last_lbdev_idx   = -1;
static int last_nmdev1_idx  = -1;
static int last_nmdev2_idx  = -1;

/*
 * Notifies tty core that a framing/parity/overrun error has happend
 * while receiving data on serial port. When frame or parity error
 * happens, -7 (randomly selected number by this driver) is sent as
 * byte that got corrupted to tty core. For emulation purpose 0 can
 * not be taken as corrupted byte because parity and break both will
 * have same sequence (octal \377 \0 \0) and therefore application
 * will not be able to differentiate between these two.
 *
 * This is also used for asserting/de-asserting ring event on line and
 * notifies tty core when a break condition has been detected on line.
 *
 * 1. Emulate framing error:
 * $ echo "1" > /sys/devices/virtual/tty/ttyVS0/event
 *
 * 2. Emulate parity error:
 * $ echo "2" > /sys/devices/virtual/tty/ttyVS0/event
 *
 * 3. Emulate overrun error:
 * $ echo "3" > /sys/devices/virtual/tty/ttyVS0/event
 *
 * 4. Emulate ring indicator (set RI signal):
 * $ echo "4" > /sys/devices/virtual/tty/ttyVS0/event
 *
 * 5. Emulate ring indicator (unset RI signal):
 * $ echo "5" > /sys/devices/virtual/tty/ttyVS0/event
 *
 * 6. Emulate break received:
 * $ echo "6" > /sys/devices/virtual/tty/ttyVS0/event
 */
static ssize_t event_store(struct device *dev,
		struct device_attribute *attr, const char *buf, size_t count)
{
	int ret, push = 1;
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);
	struct tty_struct *tty_to_write = local_vsdev->own_tty;

	if (!buf || (count <= 0))
		return -EINVAL;

	/*
	 * Ensure required structure has been allocated, initialized and
	 * port has been opened
	 */
	if (!tty_to_write || (tty_to_write->port == NULL)
			|| (tty_to_write->port->count <= 0))
		return -EIO;
	if (!test_bit(ASYNCB_INITIALIZED, &tty_to_write->port->flags))
		return -EIO;

	mutex_lock(&local_vsdev->lock);

	switch (buf[0]) {
	case '1':
		ret = tty_insert_flip_char(tty_to_write->port, -7, TTY_FRAME);
		if (ret < 0)
			goto fail;
		local_vsdev->icount.frame++;
		break;
	case '2':
		ret = tty_insert_flip_char(tty_to_write->port, -7, TTY_PARITY);
		if (ret < 0)
			goto fail;
		local_vsdev->icount.parity++;
		break;
	case '3':
		ret = tty_insert_flip_char(tty_to_write->port, 0, TTY_OVERRUN);
		if (ret < 0)
			goto fail;
		local_vsdev->icount.overrun++;
		break;
	case '4':
		local_vsdev->msr_reg |= VS_MSR_RI;
		local_vsdev->icount.rng++;
		push = -1;
		break;
	case '5':
		local_vsdev->msr_reg &= ~VS_MSR_RI;
		local_vsdev->icount.rng++;
		push = -1;
		break;
	case '6':
		ret = tty_insert_flip_char(tty_to_write->port, 0, TTY_BREAK);
		if (ret < 0)
			goto fail;
		local_vsdev->icount.brk++;
		break;
	default:
		mutex_unlock(&local_vsdev->lock);
		return -EINVAL;
	}

	if (push)
		tty_flip_buffer_push(tty_to_write->port);

	mutex_unlock(&local_vsdev->lock);
	return count;

fail:
	mutex_unlock(&local_vsdev->lock);
	return ret;
}
static DEVICE_ATTR_WO(event);

/*
 * Emulates a faulty cable condition. Data is sent successfully from
 * sender end but receiving end will not receive the data at all.
 *
 * 1. Emulate cable is faulty:
 * $ echo "1" > /sys/devices/virtual/tty/ttyVS0/faultycable
 *
 * 2. Emulate cable is not faulty (default on startup):
 * $ echo "0" > /sys/devices/virtual/tty/ttyVS0/faultycable
 */
static ssize_t faultycable_store(struct device *dev,
		struct device_attribute *attr, const char *buf, size_t count)
{
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);

	if (!buf || (count <= 0))
		return -EINVAL;

	switch (buf[0]) {
	case '0':
		local_vsdev->faulty_cable = 0;
		break;
	case '1':
		local_vsdev->faulty_cable = 1;
		break;
	default:
		return -EINVAL;
	}

	return count;
}
static DEVICE_ATTR_WO(faultycable);

/*
 * Gives index of the tty device corresponding to this sysfs node.
 * $ cat /sys/devices/virtual/tty/ttyVS0/ownidx
 */
static ssize_t ownidx_show(struct device *dev,
		struct device_attribute *attr, char *buf)
{
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);

	if (!buf)
		return -EINVAL;

	return sprintf(buf, "%u\n", local_vsdev->own_index);
}
static DEVICE_ATTR_RO(ownidx);

/*
 * Gives index of the tty device to which given tty devices is
 * connected.
 * $ cat /sys/devices/virtual/tty/ttyVS0/peeridx
 */
static ssize_t peeridx_show(struct device *dev,
		struct device_attribute *attr, char *buf)
{
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);

	if (!buf)
		return -EINVAL;

	return sprintf(buf, "%u\n", local_vsdev->peer_index);
}
static DEVICE_ATTR_RO(peeridx);

/*
 * Gives RTS line connections for the given tty device.
 * $ cat /sys/devices/virtual/tty/ttyVS0/ortsmap
 */
static ssize_t ortsmap_show(struct device *dev,
		struct device_attribute *attr, char *buf)
{
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);

	if (!buf)
		return -EINVAL;

	return sprintf(buf, "%u\n", local_vsdev->rts_mappings);
}
static DEVICE_ATTR_RO(ortsmap);

/*
 * Gives DTR line connections for the given tty device.
 * $ cat /sys/devices/virtual/tty/ttyVS0/odtrmap
 */
static ssize_t odtrmap_show(struct device *dev,
		struct device_attribute *attr, char *buf)
{
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);

	if (!buf)
		return -EINVAL;

	return sprintf(buf, "%u\n", local_vsdev->dtr_mappings);
}
static DEVICE_ATTR_RO(odtrmap);

/*
 * Gives RTS line connections of the tty device to which the
 * given tty device is connected.
 * $ cat /sys/devices/virtual/tty/ttyVS0/prtsmap
 */
static ssize_t prtsmap_show(struct device *dev,
		struct device_attribute *attr, char *buf)
{
	struct vs_dev *remote_vsdev;
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);

	if ((local_vsdev->own_index == local_vsdev->peer_index) || !buf)
		return -EINVAL;

	remote_vsdev = db[local_vsdev->peer_index].vsdev;
	return sprintf(buf, "%u\n", remote_vsdev->rts_mappings);
}
static DEVICE_ATTR_RO(prtsmap);

/*
 * Gives DTR line connections of the tty device to which the
 * given tty device is connected.
 * $ cat /sys/devices/virtual/tty/ttyVS0/pdtrmap
 */
static ssize_t pdtrmap_show(struct device *dev,
		struct device_attribute *attr, char *buf)
{
	struct vs_dev *remote_vsdev;
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);

	if ((local_vsdev->own_index == local_vsdev->peer_index) || !buf)
		return -EINVAL;

	remote_vsdev = db[local_vsdev->peer_index].vsdev;
	return sprintf(buf, "%u\n", remote_vsdev->dtr_mappings);
}
static DEVICE_ATTR_RO(pdtrmap);

/*
 * Gives type (loopback / null modem) of the given tty device.
 * $ cat /sys/devices/virtual/tty/ttyVS0/odevtyp
 */
static ssize_t odevtyp_show(struct device *dev,
		struct device_attribute *attr, char *buf)
{
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);

	if (!buf)
		return -EINVAL;

	return sprintf(buf, "%u\n", local_vsdev->odevtyp);
}
static DEVICE_ATTR_RO(odevtyp);

/*
 * Return value of 1 means, DTR signal will be asserted when given
 * this tty device is opeded. Value 0 means it will not be asserted.
 * $ cat /sys/devices/virtual/tty/ttyVS0/odtropn
 */
static ssize_t odtropn_show(struct device *dev,
		struct device_attribute *attr, char *buf)
{
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);

	if (!buf)
		return -EINVAL;

	return sprintf(buf, "%u\n", local_vsdev->set_odtr_at_open);
}
static DEVICE_ATTR_RO(odtropn);

/*
 * Return value of 1 means, DTR signal of the peer tty device will
 * be asserted when given peer tty device is opeded. Value 0 means
 * it will not be asserted.
 * $ cat /sys/devices/virtual/tty/ttyVS0/pdtropn
 */
static ssize_t pdtropn_show(struct device *dev,
		struct device_attribute *attr, char *buf)
{
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);

	if (!buf)
		return -EINVAL;

	return sprintf(buf, "%u\n", local_vsdev->set_odtr_at_open);
}
static DEVICE_ATTR_RO(pdtropn);

/*
 * Gives serial port events stats.
 * $ cat /sys/devices/virtual/tty/ttyVS0/ostats
 */
static ssize_t ostats_show(struct device *dev,
		struct device_attribute *attr, char *buf)
{
	struct vs_dev *local_vsdev = dev_get_drvdata(dev);

	if (!buf)
		return -EINVAL;

	return sprintf(buf, "%u#%u#%u#%u#%u#%u#%u#%u#%u#%u#%u#\n",
			local_vsdev->icount.tx, local_vsdev->icount.rx,
			local_vsdev->icount.cts, local_vsdev->icount.dcd,
			local_vsdev->icount.dsr, local_vsdev->icount.brk,
			local_vsdev->icount.rng, local_vsdev->icount.frame,
			local_vsdev->icount.parity, local_vsdev->icount.overrun,
			local_vsdev->icount.buf_overrun);
}
static DEVICE_ATTR_RO(ostats);

static struct attribute *vs_info_attrs[] = {
	&dev_attr_event.attr,
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

static const struct attribute_group vs_info_attr_group = {
	.attrs = vs_info_attrs,
};

/*
 * Checks if the given serial port has received its carrier detect
 * line raised or not. Return 1 if the carrier is raised otherwise 0.
 */
static int vs_port_carrier_raised(struct tty_port *port)
{
	struct vs_dev *local_vsdev = db[port->tty->index].vsdev;

	return (local_vsdev->msr_reg & VS_MSR_DCD) ? 1 : 0;
}

/* Shutdown the given serial port */
static void vs_port_shutdown(struct tty_port *port)
{
	pr_debug("shutting down the port!\n");
}

/*
 * Invoked when tty is going to be destroyed and driver should
 * release resources.
 */
static void vs_port_destruct(struct tty_port *port)
{
	pr_debug("destroying the port!\n");
}

/* Activate the given serial port as opposed to shutdown */
static int vs_port_activate(struct tty_port *port, struct tty_struct *tty)
{
	return 0;
}

static const struct tty_port_operations vs_port_ops = {
	.carrier_raised = vs_port_carrier_raised,
	.shutdown       = vs_port_shutdown,
	.activate       = vs_port_activate,
	.destruct       = vs_port_destruct,
};

/*
 * Update modem control and status registers according to the bit
 * mask(s) provided. The RTS and DTR values can be set only if the
 * current handshaking state of the tty device allows direct control
 * of the modem control lines. The pin mappings are honoured.
 *
 * Caller holds lock of thegiven virtual tty device.
 */
static int vs_update_modem_lines(struct tty_struct *tty,
			unsigned int set, unsigned int clear)
{
	int ctsint = 0;
	int dcdint = 0;
	int dsrint = 0;
	int rngint = 0;
	int mcr_ctrl_reg = 0;
	int wakeup_blocked_open = 0;
	int rts_mappings, dtr_mappings, msr_state_reg;
	struct async_icount *evicount;
	struct vs_dev *vsdev, *local_vsdev, *remote_vsdev;

	local_vsdev = db[tty->index].vsdev;

	/* Read modify write MSR register */
	if (tty->index != local_vsdev->peer_index) {
		remote_vsdev = db[local_vsdev->peer_index].vsdev;
		msr_state_reg = remote_vsdev->msr_reg;
		vsdev = remote_vsdev;
	} else {
		msr_state_reg = local_vsdev->msr_reg;
		vsdev = local_vsdev;
	}

	rts_mappings = local_vsdev->rts_mappings;
	dtr_mappings = local_vsdev->dtr_mappings;

	if (set & TIOCM_RTS) {
		mcr_ctrl_reg |= VS_MCR_RTS;
		if ((rts_mappings & VS_CON_CTS) == VS_CON_CTS) {
			msr_state_reg |= VS_MSR_CTS;
			ctsint++;
		}
		if ((rts_mappings & VS_CON_DCD) == VS_CON_DCD) {
			msr_state_reg |= VS_MSR_DCD;
			dcdint++;
			wakeup_blocked_open = 1;
		}
		if ((rts_mappings & VS_CON_DSR) == VS_CON_DSR) {
			msr_state_reg |= VS_MSR_DSR;
			dsrint++;
		}
		if ((rts_mappings & VS_CON_RI) == VS_CON_RI) {
			msr_state_reg |= VS_MSR_RI;
			rngint++;
		}
	}

	if (set & TIOCM_DTR) {
		mcr_ctrl_reg |= VS_MCR_DTR;
		if ((dtr_mappings & VS_CON_CTS) == VS_CON_CTS) {
			msr_state_reg |= VS_MSR_CTS;
			ctsint++;
		}
		if ((dtr_mappings & VS_CON_DCD) == VS_CON_DCD) {
			msr_state_reg |= VS_MSR_DCD;
			dcdint++;
			wakeup_blocked_open = 1;
		}
		if ((dtr_mappings & VS_CON_DSR) == VS_CON_DSR) {
			msr_state_reg |= VS_MSR_DSR;
			dsrint++;
		}
		if ((dtr_mappings & VS_CON_RI) == VS_CON_RI) {
			msr_state_reg |= VS_MSR_RI;
			rngint++;
		}
	}

	if (clear & TIOCM_RTS) {
		mcr_ctrl_reg &= ~VS_MCR_RTS;
		if ((rts_mappings & VS_CON_CTS) == VS_CON_CTS) {
			msr_state_reg &= ~VS_MSR_CTS;
			ctsint++;
		}
		if ((rts_mappings & VS_CON_DCD) == VS_CON_DCD) {
			msr_state_reg &= ~VS_MSR_DCD;
			dcdint++;
		}
		if ((rts_mappings & VS_CON_DSR) == VS_CON_DSR) {
			msr_state_reg &= ~VS_MSR_DSR;
			dsrint++;
		}
		if ((rts_mappings & VS_CON_RI) == VS_CON_RI) {
			msr_state_reg &= ~VS_MSR_RI;
			rngint++;
		}
	}

	if (clear & TIOCM_DTR) {
		mcr_ctrl_reg &= ~VS_MCR_DTR;
		if ((dtr_mappings & VS_CON_CTS) == VS_CON_CTS) {
			msr_state_reg &= ~VS_MSR_CTS;
			ctsint++;
		}
		if ((dtr_mappings & VS_CON_DCD) == VS_CON_DCD) {
			msr_state_reg &= ~VS_MSR_DCD;
			dcdint++;
		}
		if ((dtr_mappings & VS_CON_DSR) == VS_CON_DSR) {
			msr_state_reg &= ~VS_MSR_DSR;
			dsrint++;
		}
		if ((dtr_mappings & VS_CON_RI) == VS_CON_RI) {
			msr_state_reg &= ~VS_MSR_RI;
			rngint++;
		}
	}

	local_vsdev->mcr_reg = mcr_ctrl_reg;
	vsdev->msr_reg = msr_state_reg;

	evicount = &vsdev->icount;
	evicount->cts += ctsint;
	evicount->dsr += dsrint;
	evicount->dcd += dcdint;
	evicount->rng += rngint;

	if (vsdev->own_tty && vsdev->own_tty->port) {
		/* Wake up process blocked on TIOCMIWAIT ioctl */
		if ((vsdev->waiting_msr_chg == 1) &&
				(vsdev->own_tty->port->count > 0)) {
			wake_up_interruptible(&vsdev->own_tty->port->delta_msr_wait);
		}

		/* Wake up application blocked on carrier detect signal */
		if ((wakeup_blocked_open == 1) &&
				(vsdev->own_tty->port->blocked_open > 0)) {
			wake_up_interruptible(&vsdev->own_tty->port->open_wait);
		}
	}

	return 0;
}

/*
 * Invoked when user space process opens a serial port. The tty core
 * calls this to install tty and initialize the required resources.
 */
static int vs_install(struct tty_driver *drv, struct tty_struct *tty)
{
	int ret;
	struct tty_port *port;

	port = kcalloc(1, sizeof(struct tty_port), GFP_KERNEL);
	if (port == NULL)
		return -ENOMEM;

	/* First initialize and then set port operations */
	tty_port_init(port);
	port->ops = &vs_port_ops;

	ret = tty_port_install(port, drv, tty);
	if (ret) {
		kfree(port);
		return ret;
	}

	return 0;
}

/*
 * Invoked when there exist no user process or tty is to be
 * released explicitly for whatever reason.
 */
static void vs_cleanup(struct tty_struct *tty)
{
	tty_port_put(tty->port);
}

/*
 * Called when open system call is called on virtual tty device node.
 * The tty core allocates 'struct tty_struct' for this device and
 * set up various resources, sets up line discipline and call this
 * function. For first time allocation happens and from next time
 * onwards only re-opening happens.
 *
 * The tty core finds the tty driver serving this device node and the
 * index of this tty device as registered by this driver with tty core.
 * From this inded we retrieve the virtual tty device to work on.
 *
 * If the same serial port is opened more than once, the tty structure
 * passed to this function will be same but filp structure will be
 * different every time. Caller holds tty lock.
 *
 * This driver does not set CLOCAL by default. This means that the
 * open() system call will block until it find its carrier detect
 * line raised. Application should use O_NONBLOCK/O_NDELAY flag if
 * it does not want to wait for DCD line change.
 */
static int vs_open(struct tty_struct *tty, struct file *filp)
{
	int ret;
	struct vs_dev *remote_vsdev;
	struct vs_dev *local_vsdev = db[tty->index].vsdev;

	local_vsdev->own_tty = tty;

	/*
	 * If this device is one end of a null modem connection,
	 * provide its address to remote end.
	 */
	if (tty->index != local_vsdev->peer_index) {
		remote_vsdev = db[local_vsdev->peer_index].vsdev;
		remote_vsdev->peer_tty = tty;
	}

	memset(&local_vsdev->serial, 0, sizeof(struct serial_struct));
	memset(&local_vsdev->icount, 0, sizeof(struct async_icount));

	/*
	 * Handle DTR raising logic ourselve instead of tty_port helpers
	 * doing it. Locking virtual tty is not required here.
	 */
	if (local_vsdev->set_odtr_at_open == 1)
		vs_update_modem_lines(tty, TIOCM_DTR | TIOCM_RTS, 0);

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
 * Invoked by tty layer when release() is called on the file pointer
 * that was previously created with a call to open().
 */
static void vs_close(struct tty_struct *tty, struct file *filp)
{
	if (test_bit(TTY_IO_ERROR, &tty->flags))
		return;

	if (tty && filp && tty->port && (tty->port->count > 0))
		tty_port_close(tty->port, tty, filp);

	if (tty && C_HUPCL(tty) && tty->port && (tty->port->count < 1))
		vs_update_modem_lines(tty, 0, TIOCM_DTR | TIOCM_RTS);
}

/*
 * Invoked when write() system call is invoked on device node.
 * This function constructs evry byte as per the current uart
 * frame settings. Finally, the data is inserted into the tty
 * buffer of the receiver tty device.
 */
static int vs_write(struct tty_struct *tty,
			const unsigned char *buf, int count)
{
	int x;
	unsigned char *data = NULL;
	struct tty_struct *tty_to_write = NULL;
	struct vs_dev *rx_vsdev = NULL;
	struct vs_dev *tx_vsdev = db[tty->index].vsdev;

	if (tx_vsdev->tx_paused || !tty || tty->stopped
			|| (count < 1) || !buf || tty->hw_stopped)
		return 0;

	if (tx_vsdev->is_break_on == 1) {
		pr_debug("break condition is on!\n");
		return -EIO;
	}

	if (tx_vsdev->faulty_cable == 1)
		return count;

	if (tty->index != tx_vsdev->peer_index) {
		/* Null modem */
		tty_to_write = tx_vsdev->peer_tty;
		rx_vsdev = db[tx_vsdev->peer_index].vsdev;

		if ((tx_vsdev->baud != rx_vsdev->baud) ||
			(tx_vsdev->uart_frame != rx_vsdev->uart_frame)) {
			/*
			 * Emulate data sent but not received due to
			 * mismatched baudrate/framing.
			 */
			pr_debug("mismatched serial port settings!\n");
			tx_vsdev->icount.tx++;
			return count;
		}
	} else {
		/* Loop back */
		tty_to_write = tty;
		rx_vsdev = tx_vsdev;
	}

	if (tty_to_write) {
		if ((tty_to_write->termios.c_cflag & CSIZE) == CS8) {
			data = (unsigned char *)buf;
		} else {
			data = kcalloc(count, sizeof(unsigned char), GFP_KERNEL);
			if (!data)
				return -ENOMEM;

			/* Emulate correct number of data bits */
			switch (tty_to_write->termios.c_cflag & CSIZE) {
			case CS7:
				for (x = 0; x < count; x++)
					data[x] = buf[x] & 0x7F;
				break;
			case CS6:
				for (x = 0; x < count; x++)
					data[x] = buf[x] & 0x3F;
				break;
			case CS5:
				for (x = 0; x < count; x++)
					data[x] = buf[x] & 0x1F;
				break;
			default:
				data = (unsigned char *)buf;
			}
		}

		tty_insert_flip_string(tty_to_write->port, data, count);
		tty_flip_buffer_push(tty_to_write->port);
		tx_vsdev->icount.tx++;
		rx_vsdev->icount.rx++;

		if (data != buf)
			kfree(data);
	} else {
		/*
		 * Other end is still not opened, emulate transmission from
		 * local end but don't make other end receive it as is the
		 * case in real world.
		 */
		tx_vsdev->icount.tx++;
	}

	return count;
}

/* Invoked by tty core to transmit single data byte. */
static int vs_put_char(struct tty_struct *tty, unsigned char ch)
{
	unsigned char data;
	struct tty_struct *tty_to_write;
	struct vs_dev *rx_vsdev;
	struct vs_dev *tx_vsdev = db[tty->index].vsdev;

	if (tx_vsdev->tx_paused || !tty || tty->stopped || tty->hw_stopped)
		return 0;

	if (tx_vsdev->is_break_on == 1)
		return -EIO;

	if (tx_vsdev->faulty_cable == 1)
		return 1;

	if (tty->index != tx_vsdev->peer_index) {
		tty_to_write = tx_vsdev->peer_tty;
		rx_vsdev = db[tx_vsdev->peer_index].vsdev;
		if ((tx_vsdev->baud != rx_vsdev->baud) ||
			(tx_vsdev->uart_frame != rx_vsdev->uart_frame)) {
			tx_vsdev->icount.tx++;
			return 1;
		}
	} else {
		tty_to_write = tty;
		rx_vsdev = tx_vsdev;
	}

	if (tty_to_write != NULL) {
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
		tx_vsdev->icount.tx++;
		rx_vsdev->icount.rx++;
	} else {
		tx_vsdev->icount.tx++;
	}

	return 1;
}

/*
 * Flush the data out of serial port. This driver immediately
 * pushes data into receiver's tty buffer hence do nothing here.
 */
static void vs_flush_chars(struct tty_struct *tty)
{
	pr_debug("flushing the chars!\n");
}

/*
 * Discard the internal output buffer for this tty device. Typically
 * it may be called when executing IOCTL TCOFLUSH, closing the
 * serial port, when break is received in input stream (flushing
 * is configured) or when hangup occurs.
 *
 * On the other hand, when TCIFLUSH IOCTL is invoked, tty flip buffer
 * and line discipline queue gets emptied without involvement of tty
 * driver. The driver is generally expected not to keep data but send
 * it to tty layer as soon as possible when it receives data.
 *
 * As this driver immediately pushes data into receiver's tty buffer
 * hence do nothing here.
 *
 * @tty: tty device whose buffer should be flushed.
 */
static void vs_flush_buffer(struct tty_struct *tty)
{
	pr_debug("flushing the buffer!\n");
}

/* Provides information as a repsonse to TIOCGSERIAL IOCTL */
static int vs_get_serinfo(struct tty_struct *tty, unsigned long arg)
{
	int ret;
	struct serial_struct info;
	struct vs_dev *local_vsdev = db[tty->index].vsdev;
	struct serial_struct serial = local_vsdev->serial;

	if (!arg)
		return -EFAULT;

	memset(&info, 0, sizeof(info));

	info.type		    = PORT_UNKNOWN;
	info.line		    = serial.line;
	info.port		    = tty->index;
	info.irq			= 0;
	info.flags		    = tty->port->flags;
	info.xmit_fifo_size = 0;
	info.baud_base	    = 0;
	info.close_delay	= tty->port->close_delay;
	info.closing_wait   = tty->port->closing_wait;
	info.custom_divisor = 0;
	info.hub6		    = 0;
	info.io_type		= SERIAL_IO_MEM;

	ret = copy_to_user((void __user *)arg, &info,
				sizeof(struct serial_struct));

	return ret ? -EFAULT : 0;
}

/* Returns number of bytes that can be queued to this device now */
static int vs_write_room(struct tty_struct *tty)
{
	struct vs_dev *tx_vsdev = db[tty->index].vsdev;

	if (tx_vsdev->tx_paused || !tty ||
			tty->stopped || tty->hw_stopped)
		return 0;

	return 2048;
}

/*
 * Invoked when serial terminal settings are chaged. The old_termios
 * contains currently active settings and tty->termios contains new
 * settings to be applied.
 */
static void vs_set_termios(struct tty_struct *tty,
				struct ktermios *old_termios)
{
	u32 baud;
	int uart_frame_settings;
	unsigned int rts_mappings, dtr_mappings;
	unsigned int mask = TIOCM_DTR;
	struct vs_dev *local_vsdev = db[tty->index].vsdev;

	rts_mappings = local_vsdev->rts_mappings;
	dtr_mappings = local_vsdev->dtr_mappings;

	mutex_lock(&local_vsdev->lock);

	/*
	 * Typically B0 is used to terminate the connection.
	 * Drop RTS and DTR.
	 */
	if ((tty->termios.c_cflag & CBAUD) == B0) {
		vs_update_modem_lines(tty, 0, TIOCM_DTR | TIOCM_RTS);
		mutex_unlock(&local_vsdev->lock);
		return;
	}

	/* If coming out of B0, raise DTR and RTS. This might get
	 * overridden in next steps. Applications like minicom when
	 * opens a serial port, may drop speed to B0 and then back
	 * to normal speed again.
	 */
	if (!old_termios || (old_termios->c_cflag & CBAUD) == B0) {
		if (!(tty->termios.c_cflag & CRTSCTS) ||
				!test_bit(TTY_THROTTLED, &tty->flags)) {
			mask |= TIOCM_RTS;
			vs_update_modem_lines(tty, mask, 0);
		}
	}

	baud = tty_get_baud_rate(tty);
	if (!baud)
		baud = 9600;

	tty_encode_baud_rate(tty, baud, baud);

	local_vsdev->baud = baud;

	uart_frame_settings = 0;
	if (tty->termios.c_cflag & CRTSCTS) {
		uart_frame_settings |= VS_CRTSCTS;
	} else if ((tty->termios.c_iflag & IXON) ||
					(tty->termios.c_iflag & IXOFF)) {
		uart_frame_settings |= VS_XON;
	} else {
		uart_frame_settings |= VS_NONE;
	}

	switch (tty->termios.c_cflag & CSIZE) {
	case CS8:
		uart_frame_settings |= VS_DATA_8;
		break;
	case CS7:
		uart_frame_settings |= VS_DATA_7;
		break;
	case CS6:
		uart_frame_settings |= VS_DATA_6;
		break;
	case CS5:
		uart_frame_settings |= VS_DATA_5;
		break;
	default:
		uart_frame_settings |= VS_DATA_8;
	}

	if (tty->termios.c_cflag & CSTOPB)
		uart_frame_settings |= VS_STOP_2;
	else
		uart_frame_settings |= VS_STOP_1;

	if (tty->termios.c_cflag & PARENB) {
		if (tty->termios.c_cflag & CMSPAR) {
			if (tty->termios.c_cflag & PARODD)
				uart_frame_settings |= VS_PARITY_MARK;
			else
				uart_frame_settings |= VS_PARITY_SPACE;
		} else {
			if (tty->termios.c_cflag & PARODD)
				uart_frame_settings |= VS_PARITY_ODD;
			else
				uart_frame_settings |= VS_PARITY_EVEN;
		}
	} else {
		uart_frame_settings |= VS_PARITY_NONE;
	}

	local_vsdev->uart_frame = uart_frame_settings;

	mutex_unlock(&local_vsdev->lock);
}

/*
 * Returns the number of bytes in device's output queue. This is
 * invoked when TIOCOUTQ IOCTL is executed or by tty core as and
 * when required. Because we all push all data into receiver's
 * end tty buffer, always return 0 here.
 */
static int vs_chars_in_buffer(struct tty_struct *tty)
{
	return 0;
}

/*
 * Based on the number od interrupts check if any of the signal
 * line has changed.
 */
static int vs_check_msr_delta(struct tty_struct *tty,
		struct vs_dev *local_vsdev, unsigned long mask,
		struct async_icount *prev)
{
	int delta;
	struct async_icount now;

	/*
	 * Use tty-port initialised flag to detect all hangups
	 * including the disconnect(device destroy) event.
	 */
	if (!test_bit(ASYNCB_INITIALIZED, &tty->port->flags))
		return 1;

	mutex_lock(&local_vsdev->lock);
	now = local_vsdev->icount;
	mutex_unlock(&local_vsdev->lock);
	delta = ((mask & TIOCM_RNG && prev->rng != now.rng) ||
			 (mask & TIOCM_DSR && prev->dsr != now.dsr) ||
			 (mask & TIOCM_CAR && prev->dcd != now.dcd) ||
			 (mask & TIOCM_CTS && prev->cts != now.cts));

	*prev = now;
	return delta;
}

/* Sleeps until at-least one of the modem lines changes */
static int vs_wait_change(struct tty_struct *tty, unsigned long mask)
{
	int ret;
	struct async_icount prev;
	struct vs_dev *local_vsdev = db[tty->index].vsdev;

	mutex_lock(&local_vsdev->lock);

	local_vsdev->waiting_msr_chg = 1;
	prev = local_vsdev->icount;

	mutex_unlock(&local_vsdev->lock);

	ret = wait_event_interruptible(tty->port->delta_msr_wait,
			vs_check_msr_delta(tty, local_vsdev, mask, &prev));

	local_vsdev->waiting_msr_chg = 0;

	if (!ret && !test_bit(ASYNCB_INITIALIZED, &tty->port->flags))
		ret = -EIO;

	return ret;
}

/* Execute IOCTL commands */
static int vs_ioctl(struct tty_struct *tty,
				unsigned int cmd, unsigned long arg)
{
	switch (cmd) {
	case TIOCGSERIAL:
		return vs_get_serinfo(tty, arg);
	case TIOCMIWAIT:
		return vs_wait_change(tty, arg);
	}

	return -ENOIOCTLCMD;
}

/*
 * Invoked when tty layer's input buffers are about to get full.
 *
 * When using RTS/CTS flow control, when RTS line is de-asserted,
 * interrupt will be generated in hardware. The interrupt handler
 * will raise a flag to indicate transmission should be stopped.
 * This is achieved in this driver through tx_paused variable.
 */
static void vs_throttle(struct tty_struct *tty)
{
	struct vs_dev *local_vsdev = db[tty->index].vsdev;
	struct vs_dev *remote_vsdev = db[local_vsdev->peer_index].vsdev;

	if (tty->termios.c_cflag & CRTSCTS) {
		mutex_lock(&local_vsdev->lock);
		remote_vsdev->tx_paused = 1;
		vs_update_modem_lines(tty, 0, TIOCM_RTS);
		mutex_unlock(&local_vsdev->lock);
	} else if ((tty->termios.c_iflag & IXON) ||
				(tty->termios.c_iflag & IXOFF)) {
		vs_put_char(tty, STOP_CHAR(tty));
	} else {
		/* do nothing */
	}
}

/*
 * Invoked when the tty layer's input buffers have been emptied out,
 * and it now can accept more data. Throttle/Unthrottle is about
 * notifying remote end to start or stop data as per the currently
 * active flow control. On the other hand, Start/Stop is about what
 * action to take at local end itself to start or stop data as per
 * the currently active flow control.
 */
static void vs_unthrottle(struct tty_struct *tty)
{
	struct vs_dev *local_vsdev = db[tty->index].vsdev;
	struct vs_dev *remote_vsdev = db[local_vsdev->peer_index].vsdev;

	if (tty->termios.c_cflag & CRTSCTS) {
		/* hardware (RTS/CTS) flow control */
		mutex_lock(&local_vsdev->lock);
		remote_vsdev->tx_paused = 0;
		vs_update_modem_lines(tty, TIOCM_RTS, 0);
		mutex_unlock(&local_vsdev->lock);

		if (remote_vsdev->own_tty && remote_vsdev->own_tty->port)
			tty_port_tty_wakeup(remote_vsdev->own_tty->port);
	} else if ((tty->termios.c_iflag & IXON) ||
				(tty->termios.c_iflag & IXOFF)) {
		/* software flow control */
		vs_put_char(tty, START_CHAR(tty));
	} else {
		/* do nothing */
	}
}

/*
 * Invoked when this driver should stop sending data for example
 * as a part of flow control mechanism.
 *
 * Line discipline n_tty calls this function if this device uses
 * software flow control and an XOFF character is received from
 * other end.
 */
static void vs_stop(struct tty_struct *tty)
{
	struct vs_dev *local_vsdev = db[tty->index].vsdev;

	mutex_lock(&local_vsdev->lock);
	local_vsdev->tx_paused = 1;
	mutex_unlock(&local_vsdev->lock);
}

/*
 * Invoked when this driver should start sending data for example
 * as a part of flow control mechanism.
 *
 * Line discipline n_tty calls this function if this device uses
 * software flow control and an XON character is received from
 * other end.
 */
static void vs_start(struct tty_struct *tty)
{
	struct vs_dev *local_vsdev = db[tty->index].vsdev;

	mutex_lock(&local_vsdev->lock);
	local_vsdev->tx_paused = 0;
	mutex_unlock(&local_vsdev->lock);

	if (tty && tty->port)
		tty_port_tty_wakeup(tty->port);
}

/*
 * Obtain the modem status bits for the given tty device. Invoked
 * typically when TIOCMGET IOCTL is executed on the given
 * tty device.
 */
static int vs_tiocmget(struct tty_struct *tty)
{
	int status, msr_reg, mcr_reg;
	struct vs_dev *local_vsdev = db[tty->index].vsdev;

	mutex_lock(&local_vsdev->lock);
	mcr_reg = local_vsdev->mcr_reg;
	msr_reg = local_vsdev->msr_reg;
	mutex_unlock(&local_vsdev->lock);

	status = ((mcr_reg & VS_MCR_DTR)  ? TIOCM_DTR  : 0) |
			 ((mcr_reg & VS_MCR_RTS)  ? TIOCM_RTS  : 0) |
			 ((mcr_reg & VS_MCR_LOOP) ? TIOCM_LOOP : 0) |
			 ((msr_reg & VS_MSR_DCD)  ? TIOCM_CAR  : 0) |
			 ((msr_reg & VS_MSR_RI)   ? TIOCM_RI   : 0) |
			 ((msr_reg & VS_MSR_CTS)  ? TIOCM_CTS  : 0) |
			 ((msr_reg & VS_MSR_DSR)  ? TIOCM_DSR  : 0);

	return status;
}

/*
 * Set the modem status bits. Invoked typically when TIOCMSET IOCTL
 * is executed on the given tty device.
 */
static int vs_tiocmset(struct tty_struct *tty,
				unsigned int set, unsigned int clear)
{
	int ret;
	struct vs_dev *local_vsdev = db[tty->index].vsdev;

	mutex_lock(&local_vsdev->lock);
	ret = vs_update_modem_lines(tty, set, clear);
	mutex_unlock(&local_vsdev->lock);

	return ret;
}

/*
 * Unconditionally assert/de-assert break condition of the given
 * tty device.
 */
static int vs_break_ctl(struct tty_struct *tty, int break_state)
{
	struct tty_struct *tty_to_write;
	struct vs_dev *brk_rx_vsdev;
	struct vs_dev *brk_tx_vsdev = db[tty->index].vsdev;

	if (tty->index != brk_tx_vsdev->peer_index) {
		tty_to_write = brk_tx_vsdev->peer_tty;
		brk_rx_vsdev = db[brk_tx_vsdev->peer_index].vsdev;
	} else {
		tty_to_write = tty;
		brk_rx_vsdev = brk_tx_vsdev;
	}

	mutex_lock(&brk_tx_vsdev->lock);

	if (break_state != 0) {
		if (brk_tx_vsdev->is_break_on == 1)
			return 0;

		brk_tx_vsdev->is_break_on = 1;
		if (tty_to_write != NULL) {
			tty_insert_flip_char(tty_to_write->port, 0, TTY_BREAK);
			tty_flip_buffer_push(tty_to_write->port);
			brk_rx_vsdev->icount.brk++;
		}
	} else {
		brk_tx_vsdev->is_break_on = 0;
	}

	mutex_unlock(&brk_tx_vsdev->lock);
	return 0;
}

/*
 * Invoked by tty layer to inform this driver that it should hangup
 * the tty device (lower modem control lines after last process
 * using tty devices closes the device or exited).
 *
 * Drop DTR/RTS if HUPCL is set. This causes any attached modem to
 * hang up the line.
 *
 * On the receiving end, if CLOCAL bit is set, DCD will be ignored
 * otherwise SIGHUP may be generated to indicate a line disconnect
 * event.
 */
static void vs_hangup(struct tty_struct *tty)
{
	struct vs_dev *local_vsdev = db[tty->index].vsdev;

	mutex_lock(&local_vsdev->lock);

	/* Drops reference to tty */
	tty_port_hangup(tty->port);

	if (tty && C_HUPCL(tty))
		vs_update_modem_lines(tty, 0, TIOCM_DTR | TIOCM_RTS);

	mutex_unlock(&local_vsdev->lock);
	pr_debug("hanged up!\n");
}

/*
 * Return number of interrupts as response to TIOCGICOUNT IOCTL.
 * Both 1->0 and 0->1 transitions are counted, except for RI;
 * where only 0->1 transitions are accounted.
 */
static int vs_get_icount(struct tty_struct *tty,
				struct serial_icounter_struct *icount)
{
	struct async_icount cnow;
	struct vs_dev *local_vsdev = db[tty->index].vsdev;

	mutex_lock(&local_vsdev->lock);
	cnow = local_vsdev->icount;
	mutex_unlock(&local_vsdev->lock);

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
 * Invoked by tty layer to execute TCIOFF and TCION IOCTL commands
 * generally because user space process called tcflow() function.
 * It send a high priority character to the tty device end even if
 * stopped.
 *
 * If this function (send_xchar) is defined by tty device driver,
 * tty core will call this function. If it is not specified then
 * tty core will first instruct this driver to start transmission
 * (start()) and then invoke write() of this driver passing character
 * to be written and then it will call stop() of this driver.
 */
static void vs_send_xchar(struct tty_struct *tty, char ch)
{
	int was_paused;
	struct vs_dev *local_vsdev = db[tty->index].vsdev;

	was_paused = local_vsdev->tx_paused;
	if (was_paused)
		local_vsdev->tx_paused = 0;

	vs_put_char(tty, ch);
	if (was_paused)
		local_vsdev->tx_paused = 1;
}

/*
 * Invoked by tty core in response to tcdrain() call. As this driver
 * drains on write() itself, we return immediately from here.
 */
static void vs_wait_until_sent(struct tty_struct *tty, int timeout)
{
	pr_debug("returned wait until sent!\n");
}

/*
 * Extract pin mappings from local to remote tty devices. The
 * given 'data' is to be parsed starting from index 'x'.
 */
static int vs_extract_pin_mapping(char data[], int x)
{
	int i, mapping = 0;

	for (i = 0; i < 8; i++) {
		switch (data[x]) {
		case '8':
			mapping |= VS_CON_CTS;
			break;
		case '1':
			mapping |= VS_CON_DCD;
			break;
		case '6':
			mapping |= VS_CON_DSR;
			break;
		case '9':
			mapping |= VS_CON_RI;
			break;
		case '#':
			i = 10; /* causes return from function */
			break;
		case 'x':
		case ',':
			break;
		default:
			return -EINVAL;
		x++;
		}
	}

	return mapping;
}

static ssize_t vs_card_write(struct file *file,
			const char __user *buf, size_t length, loff_t *ppos)
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

	struct vs_dev *vsdev1 = NULL;
	struct vs_dev *vsdev2 = NULL;
	struct device *device1 = NULL;
	struct device *device2 = NULL;
	struct tty_struct *tty;

	if (length == 2) {
		memcpy(data, "gennm#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#7-8,x,x,x#4-1,6,x,x#y#y", 61);
	} else if (length == 3) {
		memcpy(data, "genlb#xxxxx#xxxxx#7-8,x,x,x#4-1,6,x,x#x-x,x,x,x#x-x,x,x,x#y#x", 61);
	} else if ((length > 60) && (length < 63)) {
		if (copy_from_user(data, buf, length) != 0)
			return -EFAULT;
	} else {
		return -EINVAL;
	}
	data[62] = '\0';

	/* Initial sanitization */
	if ((data[0] == 'g') && (data[1] == 'e') && (data[2] == 'n')) {
		if ((data[3] == 'n') && (data[4] == 'm'))
		else if ((data[3] == 'l') && (data[4] == 'b'))
			is_loopback = 1;
		else
			return -EINVAL;
		create = 1;
	} else if ((data[0] == 'd') && (data[1] == 'e') && (data[2] == 'l')) {
		create = -1;
	} else {
		return -EINVAL;
	}

	if (create == 1) {
		/* Create device(s) command sent */

		/*
		 * Extract 1st device index to be used for both
		 * null modem and loop back.
		 */
		x = 6;
		if (data[6] != 'x') {
			memset(tmp, '\0', sizeof(tmp));
			for (i = 0; i < 5; i++) {
				tmp[i] = data[x];
				x++;
			}
			ret = kstrtouint(tmp, 10, &vdev1idx);
			if (ret != 0)
				return ret;
			if ((vdev1idx < 0) || (vdev1idx > 65535))
				return -EINVAL;
		}

		vsdev1 = kcalloc(1, sizeof(struct vs_dev), GFP_KERNEL);
		if (vsdev1 == NULL)
			return -ENOMEM;

		/*
		 * Extract 2nd device index if null modem pair is to
		 * be created.
		 */
		if (is_loopback != 1) {
			x = 12;
			if (data[x] != 'x') {
				memset(tmp, '\0', sizeof(tmp));
				i = 0;
				for (i = 0; i < 5; i++) {
					tmp[i] = data[x];
					x++;
				}
				ret = kstrtouint(tmp, 10, &vdev2idx);
				if (ret != 0)
					return ret;
				if ((vdev2idx < 0) || (vdev2idx > 65535)) {
					ret = -EINVAL;
					goto fail_arg;
				}

			}

			vsdev2 = kcalloc(1, sizeof(struct vs_dev), GFP_KERNEL);
			if (vsdev2 == NULL) {
				ret = -ENOMEM;
				goto fail_arg;
			}
		}

		/* rts mappings (dev1) */
		if ((data[18] != '7') || (data[19] != '-')) {
			ret = -EINVAL;
			goto fail_arg;
		}
		ret = vs_extract_pin_mapping(data, 20);
		if (ret < 0)
			goto fail_arg;
		vdev1rts = ret;

		if ((data[27] != '#') || (data[28] != '4') || (data[29] != '-'))
			goto fail_arg;

		/* dtr mapping (dev1) */
		ret = vs_extract_pin_mapping(data, 30);
		if (ret < 0)
			goto fail_arg;
		vdev1dtr = ret;

		if (data[37] != '#')
			goto fail_arg;

		if (is_loopback != 1) {
			/* rts mappings (dev2) */
			if ((data[38] != '7') || (data[39] != '-')) {
				ret = -EINVAL;
				goto fail_arg;
			}
			ret = vs_extract_pin_mapping(data, 40);
			if (ret < 0)
				goto fail_arg;
			vdev2rts = ret;

			/* dtr mapping (dev2) */
			if ((data[47] != '#') || (data[48] != '4') || (data[49] != '-'))
				goto fail_arg;

			ret = vs_extract_pin_mapping(data, 50);
			if (ret < 0)
				goto fail_arg;
			vdev2dtr = ret;

			if (data[57] != '#')
				goto fail_arg;
		}

		/*
		 * Create serial port (tty device) with lock taken to ensure
		 * correctness of index in use and associated data.
		 */
		mutex_lock(&adaptlock);

		i = -1;
		if (vdev1idx == -1) {
			for (x = 0; x < max_num_vs_dev; x++) {
				if (db[x].index == -1) {
					i = x;
					break;
				}
			}
		} else {
			if (db[vdev1idx].index == -1) {
				i = vdev1idx;
			} else {
				ret = -EEXIST;
				mutex_unlock(&adaptlock);
				goto fail_arg;
			}
		}
		if (i == -1) {
			ret = -ENOMEM;
			mutex_unlock(&adaptlock);
			goto fail_arg;
		}

		/* Initialize meta information and create 1st serial port */
		if (data[58] == 'y')
			vsdev1->set_odtr_at_open = 1;
		else
			vsdev1->set_odtr_at_open = 0;
		vsdev1->own_tty = NULL;
		vsdev1->peer_tty = NULL;
		vsdev1->own_index = i;
		vsdev1->peer_index = i;
		vsdev1->rts_mappings = vdev1rts;
		vsdev1->dtr_mappings = vdev1dtr;
		vsdev1->msr_reg = 0;
		vsdev1->mcr_reg = 0;
		vsdev1->waiting_msr_chg = 0;
		vsdev1->tx_paused = 0;
		vsdev1->faulty_cable = 0;
		db[i].index = i;
		db[i].vsdev = vsdev1;
		mutex_init(&vsdev1->lock);

		if (is_loopback != 1) {
			y = -1;
			if (vdev2idx == -1) {
				for (x = 0; x < max_num_vs_dev; x++) {
					if (db[x].index == -1) {
						y = x;
						break;
					}
				}
			} else {
				if (db[vdev2idx].index == -1) {
					y = vdev2idx;
				} else {
					ret = -EEXIST;
					mutex_unlock(&adaptlock);
					goto fail_arg;
				}
			}
			if (y == -1) {
				ret = -ENOMEM;
				mutex_unlock(&adaptlock);
				goto fail_arg;
			}

			/*
			 * Initialize meta information and create second
			 * serial port.
			 */
			if (data[60] == 'y')
				vsdev2->set_odtr_at_open = 1;
			else
				vsdev2->set_odtr_at_open = 0;
			vsdev2->set_pdtr_at_open = vsdev1->set_odtr_at_open;
			vsdev1->set_pdtr_at_open = vsdev2->set_odtr_at_open;
			vsdev1->own_index = i;
			vsdev1->peer_index = y;
			vsdev2->own_index = y;
			vsdev2->peer_index = i;
			vsdev2->own_tty = NULL;
			vsdev2->peer_tty = NULL;
			vsdev2->rts_mappings = vdev2rts;
			vsdev2->dtr_mappings = vdev2dtr;
			vsdev2->msr_reg = 0;
			vsdev2->mcr_reg = 0;
			vsdev2->waiting_msr_chg = 0;
			vsdev2->tx_paused = 0;
			vsdev2->faulty_cable = 0;
			db[y].index = y;
			db[y].vsdev = vsdev2;
			mutex_init(&vsdev2->lock);
		}

		device1 = tty_register_device(ttyvs_driver, i, NULL);
		if (device1 == NULL) {
			ret = -ENOMEM;
			mutex_unlock(&adaptlock);
			goto fail_arg;
		}

		vsdev1->device = device1;
		dev_set_drvdata(device1, vsdev1);

		x = sysfs_create_group(&device1->kobj, &vs_info_attr_group);
		if (x < 0) {
			tty_unregister_device(ttyvs_driver, i);
			mutex_unlock(&adaptlock);
			goto fail_arg;
		}

		if (is_loopback != 1) {
			device2 = tty_register_device(ttyvs_driver, y, NULL);
			if (device2 == NULL) {
				ret = -ENOMEM;
				mutex_unlock(&adaptlock);
				goto fail_register;
			}

			vsdev2->device = device2;
			dev_set_drvdata(device2, vsdev2);

			x = sysfs_create_group(&device2->kobj, &vs_info_attr_group);
			if (x < 0) {
				tty_unregister_device(ttyvs_driver, y);
				db[y].index = -1;
				mutex_unlock(&adaptlock);
				goto fail_register;
			}

			last_nmdev1_idx = i;
			last_nmdev2_idx = y;
			++total_nm_pair;

			if ((vsdev1->dtr_mappings != (VS_CON_DSR | VS_CON_DCD))
					|| (vsdev1->rts_mappings != VS_CON_CTS)
					|| (vsdev1->set_odtr_at_open != 1)
					|| (vsdev2->dtr_mappings != (VS_CON_DSR | VS_CON_DCD))
					|| (vsdev2->rts_mappings != VS_CON_CTS)
					|| (vsdev2->set_odtr_at_open != 1)) {
				vsdev1->odevtyp = VS_CNM;
				vsdev2->odevtyp = VS_CNM;
			} else {
				vsdev1->odevtyp = VS_SNM;
				vsdev2->odevtyp = VS_SNM;
			}
		} else {
			last_lbdev_idx = i;
			++total_lb_devs;

			/* device type */
			if ((vsdev1->dtr_mappings != (VS_CON_DSR | VS_CON_DCD))
					|| (vsdev1->rts_mappings != VS_CON_CTS)
					|| (vsdev1->set_odtr_at_open != 1)) {
				vsdev1->odevtyp = VS_CLB;
			} else {
				vsdev1->odevtyp = VS_SLB;
			}
		}

		mutex_unlock(&adaptlock);
	} else {
		/* Destroy device command sent */
		if ((total_nm_pair <= 0) && (total_lb_devs <= 0))
			return length;

		/*
		 * An application may forget to close serial port or it might
		 * have been crashed resulting in unclosed port and hence
		 * leaked resources. We handle such scenarios as disconnected
		 * event as done in case of a plug and play for example usb
		 * device. Application is running, port is opened and then
		 * suddenly user removes tty device.
		 */
		if (data[8] == 'x') {

			/* Delete all virtual devices */

			mutex_lock(&adaptlock);

			/* First tty must be released and than port. */
			for (x = 0; x < max_num_vs_dev; x++) {
				if (db[x].index != -1) {

					vsdev1 = db[x].vsdev;
					if (vsdev1 != NULL) {
						sysfs_remove_group(&vsdev1->device->kobj,
									&vs_info_attr_group);
						if (vsdev1->own_tty && vsdev1->own_tty->port) {
							tty = tty_port_tty_get(vsdev1->own_tty->port);
							if (tty) {
								tty_vhangup(tty);
								tty_kref_put(tty);
							}
						}
						tty_unregister_device(ttyvs_driver, db[x].index);
						kfree(db[x].vsdev);
					}
					db[x].index = -1;
				}
			}

			total_nm_pair = 0;
			total_lb_devs = 0;
			last_lbdev_idx  = -1;
			last_nmdev1_idx = -1;
			last_nmdev2_idx = -1;

			mutex_unlock(&adaptlock);
		} else {
			/* Delete a specific virtual device */
			x = 4;
			memset(tmp, '\0', sizeof(tmp));
			i = 0;
			for (i = 0; i < 5; i++) {
				tmp[i] = data[x];
				x++;
			}

			x = -1;
			y = -1;

			ret = kstrtouint(tmp, 10, &vdev1idx);
			if (ret != 0)
				return ret;

			if ((vdev1idx >= 0) && (vdev1idx <= 65535) && (db[vdev1idx].index != -1)) {
				mutex_lock(&adaptlock);

				x = db[vdev1idx].index;
				vsdev1 = db[x].vsdev;
				sysfs_remove_group(&vsdev1->device->kobj, &vs_info_attr_group);
				tty_unregister_device(ttyvs_driver, db[x].index);
				if (vsdev1 && vsdev1->own_tty && vsdev1->own_tty->port) {
					tty = tty_port_tty_get(vsdev1->own_tty->port);
					if (tty) {
						tty_vhangup(tty);
						tty_kref_put(tty);
					}
				}

				if (vsdev1->own_index != vsdev1->peer_index) {
					y = db[vsdev1->peer_index].index;
					vsdev2 = db[y].vsdev;
					sysfs_remove_group(&vsdev2->device->kobj, &vs_info_attr_group);
					tty_unregister_device(ttyvs_driver, db[y].index);
					if (vsdev2 && vsdev2->own_tty && vsdev2->own_tty->port) {
						tty = tty_port_tty_get(vsdev2->own_tty->port);
						if (tty) {
							tty_vhangup(tty);
							tty_kref_put(tty);
						}
					}
				}

				if (x != -1) {
					kfree(db[x].vsdev);
					db[x].index = -1;
				}
				if (y != -1) {
					kfree(db[y].vsdev);
					db[y].index = -1;
					--total_nm_pair;
				} else {
					--total_lb_devs;
				}

				mutex_unlock(&adaptlock);

			} else {
				return -EINVAL;
			}
		}
	}

	return length;

fail_register:
	sysfs_remove_group(&device1->kobj, &vs_info_attr_group);
	tty_unregister_device(ttyvs_driver, i);

fail_arg:
	db[i].index = -1;

	if (vsdev2 != NULL)
		kfree(vsdev2);

	if (vsdev1 != NULL)
		kfree(vsdev1);

	return ret;
}

/*
 * Gives next available index and last used index for virtual
 * tty devices created.
 * $ head -c 52 /proc/vs_vmpscrdk
 */
static ssize_t vs_card_read(struct file *file,
				char __user *buf, size_t size, loff_t *ppos)
{
	int x = 0;
	int ret = 0;
	int val = 0;
	char data[64];
	int first_avail_idx = -1;
	int second_avail_idx = -1;
	struct vs_dev *lbvsdev = NULL;
	struct vs_dev *nm1vsdev = NULL;
	struct vs_dev *nm2vsdev = NULL;

	memset(data, '\0', 64);

	if (size != 52)
		return -EINVAL;

	mutex_lock(&adaptlock);

	/* Find next available free index */
	for (x = 0; x < max_num_vs_dev; x++) {
		if (db[x].index == -1) {
			if (first_avail_idx == -1) {
				first_avail_idx = x;
			} else {
				second_avail_idx = x;
				break;
			}
		}
	}

	if ((first_avail_idx != -1) && (second_avail_idx != -1))
		val = 2;
	else if ((first_avail_idx != -1) && (second_avail_idx == -1))
		val = 1;
	else if ((first_avail_idx == -1) && (second_avail_idx == -1))
		val = 0;

	if (last_lbdev_idx == -1) {
		if (last_nmdev1_idx == -1) {
			snprintf(data, 64,
				"xxxxx#xxxxx-xxxxx#%05d-%05d#%d#x-x#x-x#x-x#x#x#x\r\n",
				first_avail_idx, second_avail_idx, val);
		} else {
			nm1vsdev = db[last_nmdev1_idx].vsdev;
			nm2vsdev = db[last_nmdev2_idx].vsdev;
			snprintf(data, 64,
				"xxxxx#%05d-%05d#%05d-%05d#%d#x-x#%d-%d#%d-%d#x#%d#%d\r\n",
				last_nmdev1_idx, last_nmdev2_idx, first_avail_idx,
				second_avail_idx, val, nm1vsdev->rts_mappings,
				nm1vsdev->dtr_mappings, nm2vsdev->rts_mappings,
				nm2vsdev->dtr_mappings, nm1vsdev->set_odtr_at_open,
				nm2vsdev->set_odtr_at_open);
		}
	} else {
		if (last_nmdev1_idx == -1) {
			lbvsdev = db[last_lbdev_idx].vsdev;
			snprintf(data, 64,
				"%05d#xxxxx-xxxxx#%05d-%05d#%d#%d-%d#x-x#x-x#%d#x#x\r\n",
				last_lbdev_idx, first_avail_idx,
				second_avail_idx, val, lbvsdev->rts_mappings,
				lbvsdev->dtr_mappings, lbvsdev->set_odtr_at_open);
		} else {
			lbvsdev = db[last_lbdev_idx].vsdev;
			nm1vsdev = db[last_nmdev1_idx].vsdev;
			nm2vsdev = db[last_nmdev2_idx].vsdev;
			snprintf(data, 64,
				"%05d#%05d-%05d#%05d-%05d#%d#%d-%d#%d-%d#%d-%d#%d#%d#%d\r\n",
				last_lbdev_idx, last_nmdev1_idx,
				last_nmdev2_idx, first_avail_idx, second_avail_idx,
				val, lbvsdev->rts_mappings, lbvsdev->dtr_mappings,
				nm1vsdev->rts_mappings, nm1vsdev->dtr_mappings,
				nm2vsdev->rts_mappings, nm2vsdev->dtr_mappings,
				lbvsdev->set_odtr_at_open, nm1vsdev->set_odtr_at_open,
				nm2vsdev->set_odtr_at_open);
		}
	}

	mutex_unlock(&adaptlock);

	ret = copy_to_user(buf, &data, 52);
	if (ret)
		return -EFAULT;

	return 52;
}

/* Always return success as we don't have anything needed here */
static int vs_card_open(struct inode *inode, struct  file *file)
{
	return 0;
}
static int vs_card_close(struct inode *inode, struct file *file)
{
	return 0;
}

static const struct tty_operations vs_serial_ops = {
	.install	     = vs_install,
	.cleanup	     = vs_cleanup,
	.open	         = vs_open,
	.close	         = vs_close,
	.write	         = vs_write,
	.put_char	     = vs_put_char,
	.flush_chars     = vs_flush_chars,
	.write_room      = vs_write_room,
	.chars_in_buffer = vs_chars_in_buffer,
	.ioctl	         = vs_ioctl,
	.set_termios     = vs_set_termios,
	.throttle	     = vs_throttle,
	.unthrottle      = vs_unthrottle,
	.stop	         = vs_stop,
	.start	         = vs_start,
	.hangup	         = vs_hangup,
	.break_ctl       = vs_break_ctl,
	.flush_buffer    = vs_flush_buffer,
	.wait_until_sent = vs_wait_until_sent,
	.send_xchar      = vs_send_xchar,
	.tiocmget	     = vs_tiocmget,
	.tiocmset	     = vs_tiocmset,
	.get_icount      = vs_get_icount,
};

static const struct file_operations vs_vcard_fops = {
	.owner   = THIS_MODULE,
	.open    = vs_card_open,
	.release = vs_card_close,
	.read   = vs_card_read,
	.write   = vs_card_write,
};

static struct miscdevice ttyvs_card_dev = {
	.minor		= 0,
	.name		= "ttyvs_card",
	.fops		= &vs_vcard_fops,
};

static int __init ttyvs_init(void)
{
	int x, ret;

	/*
	 * Causes allocation of memory for 'struct tty_port' and
	 * 'struct cdev' for all tty devices this driver can handle.
	 */
	ttyvs_driver = tty_alloc_driver(max_num_vs_dev, 0);
	if (!ttyvs_driver)
		return -ENOMEM;

	ttyvs_driver->owner = THIS_MODULE;
	ttyvs_driver->driver_name = "ttyvs";
	ttyvs_driver->name = "ttyvs";
	ttyvs_driver->major = 0;
	ttyvs_driver->minor_start = minor_begin;
	ttyvs_driver->type = TTY_DRIVER_TYPE_SERIAL;
	ttyvs_driver->subtype = SERIAL_TYPE_NORMAL;
	ttyvs_driver->flags = TTY_DRIVER_REAL_RAW
				| TTY_DRIVER_RESET_TERMIOS
				| TTY_DRIVER_DYNAMIC_DEV;
	ttyvs_driver->init_termios = tty_std_termios;
	ttyvs_driver->init_termios.c_cflag = B9600 | CS8 | CREAD | HUPCL;
	ttyvs_driver->init_termios.c_ispeed = 9600;
	ttyvs_driver->init_termios.c_ospeed = 9600;

	tty_set_operations(ttyvs_driver, &vs_serial_ops);

	ret = tty_register_driver(ttyvs_driver);
	if (ret)
		goto failed_register;

	db = kcalloc(max_num_vs_dev, sizeof(struct vs_info), GFP_KERNEL);
	if (!db) {
		ret = -ENOMEM;
		goto failed_alloc;
	}

	/*
	 * A value of -1 at particular 'X' (db[X].index) means that ttyVSx
	 * is available to install new tty device.
	 */
	for (x = 0; x < max_num_vs_dev;  x++)
		db[x].index = -1;

	/*
	 * If module was loaded with parameters supplied, create null-modem
	 * and loopback virtual tty devices as specified.
	 */
	if (((2 * init_num_nm_pair) + init_num_lb_dev) <= max_num_vs_dev) {
		for (x = 0; x < init_num_nm_pair; x++) {
			ret = vs_card_write(NULL, NULL, 2, NULL);
			if (ret < 0)
				pr_err("Can't create null modem pair %d\n", ret);
		}
		for (x = 0; x < init_num_lb_dev; x++) {
			ret = vs_card_write(NULL, NULL, 3, NULL);
			if (ret < 0)
				pr_err("Can't create loop back device %d\n", ret);
		}
	} else {
		pr_err("Specified devices not created. Invalid total.\n");
	}

	/*
	 * Application should read/write to /dev/ttyvs_card to create/destroy
	 * tty device and query information associated with them.
	 */
	ret = misc_register(&ttyvs_card_dev);
	if (ret)
		goto failed_card;

	pr_info("serial port null modem emulation driver\n");
	return 0;

failed_card:
	kfree(db);
failed_alloc:
	tty_unregister_driver(ttyvs_driver);
failed_register:
	put_tty_driver(ttyvs_driver);
	return ret;
}

static void __exit ttyvs_exit(void)
{
	int x;
	struct vs_dev *vsdev;
	struct tty_struct *tty;

	misc_deregister(&ttyvs_card_dev);

	for (x = 0; x < max_num_vs_dev; x++) {
		if (db[x].index != -1) {
			vsdev = db[x].vsdev;
			sysfs_remove_group(&vsdev->device->kobj,
						&vs_info_attr_group);
			tty_unregister_device(ttyvs_driver, db[x].index);
			if (vsdev && vsdev->own_tty && vsdev->own_tty->port) {
				tty = tty_port_tty_get(vsdev->own_tty->port);
				if (tty) {
					tty_vhangup(tty);
					tty_kref_put(tty);
				}
				kfree(db[x].vsdev);
			}
		}
	}

	kfree(db);
	tty_unregister_driver(ttyvs_driver);
	put_tty_driver(ttyvs_driver);
}

module_init(ttyvs_init);
module_exit(ttyvs_exit);

/*
 * By default this driver supports upto 128 virtual devices.
 * Use this to increase/reduce the total number of devices to
 * be supported. For ex; to support 64 devices use as shown below:
 * $ insmod ./ttyVS.ko max_num_vs_dev=64
 */
module_param(max_num_vs_dev, ushort, 0);
MODULE_PARM_DESC(max_num_vs_dev,
		"Maximum virtual tty devices to be supported");

/*
 * Specifies number of standard null modem pairs to be created.
 * If both null modem and loopback are specified, 1st all null
 * modem devices will be created and then all loopback devices
 * will be created.
 */
module_param(init_num_nm_pair, ushort, 0);
MODULE_PARM_DESC(init_num_nm_pair,
		"Standard null modem pairs to create initially");

/*
 * Specifies number of standard loopback devices to be created.
 */
module_param(init_num_lb_dev, ushort, 0);
MODULE_PARM_DESC(init_num_lb_dev,
		"Standard loopback devices to create initially");

/*
 * Specifies the starting index of the tty device to be used by this
 * driver. This also becomes the first minor number (x) for the device
 * nodes /dev/ttVSx.
 */
module_param(minor_begin, int, 0);
MODULE_PARM_DESC(minor_begin,
		"Starting minor number of device nodes");

MODULE_AUTHOR("Rishi Gupta <gupt21@gmail.com>");
MODULE_DESCRIPTION("Serial port null modem emulation driver");
MODULE_LICENSE("GPL v2");
