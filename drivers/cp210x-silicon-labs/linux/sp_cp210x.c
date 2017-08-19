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

#include <linux/kernel.h>
#include <linux/errno.h>
#include <linux/slab.h>
#include <linux/tty.h>
#include <linux/tty_driver.h>
#include <linux/tty_flip.h>
#include <linux/module.h>
#include <linux/moduleparam.h>
#include <linux/mutex.h>
#include <linux/usb.h>
#include <linux/uaccess.h>
#include <linux/serial.h>
#include <linux/usb/serial.h>

/* CP210x chip type definitions */
#define PART_CP2101  0x01
#define PART_CP2102  0x02
#define PART_CP2103  0x03
#define PART_CP2104  0x04
#define PART_CP2105  0x05
#define PART_CP2108  0x08
#define PART_CP2109  0x09

/* IOCTLs */
#define IOCTL_GPIOGET  0x8000
#define IOCTL_GPIOSET  0x8001

/* Config/Commands request types */
#define REQTYPE_HOST_TO_INTERFACE  0x41
#define REQTYPE_INTERFACE_TO_HOST  0xc1
#define REQTYPE_HOST_TO_DEVICE     0x40
#define REQTYPE_DEVICE_TO_HOST     0xc0

/* Config/Commands request codes */
#define CP210X_IFC_ENABLE       0x00
#define CP210X_SET_BAUDDIV      0x01
#define CP210X_GET_BAUDDIV      0x02
#define CP210X_SET_LINE_CTL     0x03
#define CP210X_GET_LINE_CTL     0x04
#define CP210X_SET_BREAK        0x05
#define CP210X_IMM_CHAR         0x06
#define CP210X_SET_MHS          0x07
#define CP210X_GET_MDMSTS       0x08
#define CP210X_SET_XON          0x09
#define CP210X_SET_XOFF         0x0A
#define CP210X_SET_EVENTMASK    0x0B
#define CP210X_GET_EVENTMASK    0x0C
#define CP210X_SET_CHAR         0x0D
#define CP210X_GET_CHARS        0x0E
#define CP210X_GET_PROPS        0x0F
#define CP210X_GET_COMM_STATUS  0x10
#define CP210X_RESET            0x11
#define CP210X_PURGE            0x12
#define CP210X_SET_FLOW         0x13
#define CP210X_GET_FLOW         0x14
#define CP210X_EMBED_EVENTS     0x15
#define CP210X_GET_EVENTSTATE   0x16
#define CP210X_SET_CHARS        0x19
#define CP210X_GET_BAUDRATE     0x1D
#define CP210X_SET_BAUDRATE     0x1E
#define CP210X_VENDOR_SPECIFIC  0xFF

/* CP210X_IFC_ENABLE */
#define UART_ENABLE   0x0001
#define UART_DISABLE  0x0000

/* CP210X_VENDOR_SPECIFIC */
#define CP210X_WRITE_LATCH  0x37E1
#define CP210X_READ_LATCH   0x00C2
#define CP210X_GET_PARTNUM  0x370B

/* CP210X_(SET|GET)_BAUDDIV */
#define BAUD_RATE_GEN_FREQ  0x384000

/* CP210X_(SET|GET)_LINE_CTL */
#define BITS_DATA_MASK  0X0F00
#define BITS_DATA_5     0X0500
#define BITS_DATA_6     0X0600
#define BITS_DATA_7     0X0700
#define BITS_DATA_8     0X0800
#define BITS_DATA_9     0X0900

#define BITS_PARITY_MASK   0x00F0
#define BITS_PARITY_NONE   0x0000
#define BITS_PARITY_ODD    0x0010
#define BITS_PARITY_EVEN   0x0020
#define BITS_PARITY_MARK   0x0030
#define BITS_PARITY_SPACE  0x0040

#define BITS_STOP_MASK  0x000F
#define BITS_STOP_1     0x0000
#define BITS_STOP_1_5   0x0001
#define BITS_STOP_2     0x0002

/* CP210X_SET_BREAK */
#define BREAK_ON   0x0001
#define BREAK_OFF  0x0000

/* CP210X_(SET_MHS|GET_MDMSTS) */
#define CONTROL_DTR        0x0001
#define CONTROL_RTS        0x0002
#define CONTROL_CTS        0x0010
#define CONTROL_DSR        0x0020
#define CONTROL_RING       0x0040
#define CONTROL_DCD        0x0080
#define CONTROL_WRITE_DTR  0x0100
#define CONTROL_WRITE_RTS  0x0200

/* Function prototypes for cp210x usb-serial converter */
static int write_cp210x_register(struct usb_serial_port *port, u8 request, u8 requestType, int value, 
        int index, unsigned int *data, int size);
static int read_cp210x_register(struct usb_serial_port *port, u8 request, u8 requestType, int value, 
        int index, unsigned int *data, int size);

static ssize_t cp210x_gpio_1_store(struct device *dev, struct device_attribute *attr, const char *valbuf, size_t count);
static ssize_t cp210x_gpio_1_show(struct device *dev, struct device_attribute *attr, char *buf);
static void remove_cp210x_sysfs_attrs(struct usb_serial_port *port);
static int create_cp210x_sysfs_attrs(struct usb_serial_port *port);

static int xyz_product_probe(struct usb_serial *serial);
static int sp_cp210x_probe(struct usb_serial *serial, const struct usb_device_id *id);

static int xyz_product_port_probe(struct usb_serial_port *port);
static int sp_cp210x_port_probe(struct usb_serial_port *port);
static int sp_cp210x_port_remove(struct usb_serial_port *port);

static int update_cp210x_mctrl_lines(struct usb_serial_port *port, unsigned int set, unsigned int clear);
static int sp_cp210x_tiocmset(struct tty_struct *tty, unsigned int set, unsigned int clear);
static void sp_cp210x_dtr_rts(struct usb_serial_port *port, int on);

static int sp_cp210x_startup(struct usb_serial *serial);
static void sp_cp210x_shutdown(struct usb_serial *serial);
static void sp_cp210x_set_termios(struct tty_struct *tty, struct usb_serial_port *port, struct ktermios *old_termios);
static int sp_cp210x_ioctl(struct tty_struct *tty, unsigned int cmd, unsigned long arg);
static int sp_cp210x_tiocmget(struct tty_struct *tty);
static void sp_cp210x_break_ctl(struct tty_struct *tty, int break_state);
static int sp_cp210x_open(struct tty_struct *tty, struct usb_serial_port *port);
static void sp_cp210x_close(struct usb_serial_port *port);

static bool dbg = false;

struct cp210x_port_private {
    int cp210x_chip_type;
    int interface_enabled;
};

/* struct cp210x_products_quirk is used by products that need to do extra things. */
struct cp210x_products_quirk {
    int (*probe)(struct usb_serial *);
    int (*port_probe)(struct usb_serial_port *);
};

static struct cp210x_products_quirk xyz_product_quirk = {
        .probe  = xyz_product_probe,
        .port_probe = xyz_product_port_probe,
};

/* 
 * The struct usb_device_id structure provides a list of different types of USB devices that this 
 * driver supports. This list is used by the USB core to decide which driver will drive which device, 
 * and also by the hotplug scripts to decide which driver to load automatically when a specific USB 
 * device is plugged into the system.
 * 
 * A driver can define and register more than one usb_device_id table to handle different range of
 * products.
 */
static const struct usb_device_id id_table[] = {
        { USB_DEVICE(0x10C4, 0xEA60) },
        { USB_DEVICE(0x10C4, 0xEA61) },
        { USB_DEVICE(0x10C4, 0xEA70) },
        { USB_DEVICE(0x10C4, 0xEA80), .driver_info = (kernel_ulong_t) &xyz_product_quirk },
        { } /* terminating entry */
};
MODULE_DEVICE_TABLE(usb, id_table);

/* 
 * The struct usb_serial_driver describes a usb-serial driver.
 * 
 * Write: To send data from the host to the port, the host queues BULK-OUT packets to the endpoint of 
 * the interface associated with the port. The data from these packets will be loaded into the interface’s 
 * data buffers and passed on to the external device. The usb_serial_generic_write function is used for 
 * this purpose.
 *
 * When host has sent data to a cp210x device, it will sent data out of UART interface physically after 
 * either a default timeout of 1 ms or 18/BaudRate (whichever is shorter) has elapsed. This timeout can 
 * be changed using utilities provided by silicon labs.
 *
 * Read : To move data from the port to the host, the host issues IN requests to the port’s data IN endpoint. 
 * When data is received by the USB serial driver for a specific port, is should be placed into the specific 
 * tty structure assigned to that port's flip buffer. The read_bulk_callback function is used for this purpose.
 *
 * Overrun: The usb_serial_generic_throttle function is called when the tty layer's input buffers are getting 
 * full to prevent overrun. The tty driver should try to signal the device that no more data should be sent to 
 * it. The usb_serial_generic_unthrottle function is called when the tty layer's input buffers have been emptied 
 * out and ready to accept data. The tty driver should then signal to the device that data can be received.
 *
 * Typically usb-uart converters handles software/hardware flow control in hardware itself. The driver just 
 * need to set desired flow control and device will automatically set/unset RTS Line or send XON/XOFF characters 
 * as and when required. When tty layer instructs device driver to throttle, driver just refrains from 
 * reading data from usb port.
 *
 * TIOCMIWAIT: when application issues TIOCMIWAIT IOCTL, it wishes to sleep within the kernel until something 
 * happens to the MSR register of the tty device. This IOCTL is commonly used to wait for status line changes
 * The usb_serial_generic_tiocmiwait function is used for this purpose. 
 *
 * TIOCGICOUNT: when application wants to know the number of serial line interrupts that have occurred, it issues
 * this IOCTL. The usb_serial_generic_get_icount function is used for this purpose.
 *
 * TCFLSH: As soon as data is given from host to cp210x it will get sent out of port. Similarly, as soon as data
 * is received it will be pushed to flip buffers. So flushing happens only at tty and line discipline layers.
 *
 * Some of the functions which are not set here gets set to their generic version by usbserial driver.
 *
 * Driver name "sp_cp210x" will be seen in /sys/bus/usb/drivers/ directory. From this place scripts can get path to
 * interface: /sys/bus/usb/drivers/sp_cp210x/3-3:1.0 -> ../../../../devices/pci0000:00/0000:00:14.0/usb3/3-3/3-3:1.0
 */
static struct usb_serial_driver sp_cp210x_device = {
        .driver = {
                .owner = THIS_MODULE,
                .name  = "sp_cp210x",
        },
        .description   = "CP210X USB Serial Device",
        .id_table      = id_table,
        .num_ports     = 1,
        .bulk_in_size  = 256,
        .bulk_out_size = 256,
        .probe         = sp_cp210x_probe,
        .attach        = sp_cp210x_startup,
        .release       = sp_cp210x_shutdown,
        .port_probe    = sp_cp210x_port_probe,
        .port_remove   = sp_cp210x_port_remove,
        .open          = sp_cp210x_open,
        .close         = sp_cp210x_close,
        .ioctl         = sp_cp210x_ioctl,
        .set_termios   = sp_cp210x_set_termios,
        .break_ctl     = sp_cp210x_break_ctl,
        .throttle      = usb_serial_generic_throttle,
        .unthrottle    = usb_serial_generic_unthrottle,
        .tiocmget      = sp_cp210x_tiocmget,
        .tiocmset      = sp_cp210x_tiocmset,
        .tiocmiwait    = usb_serial_generic_tiocmiwait,
        .get_icount    = usb_serial_generic_get_icount,
        .dtr_rts       = sp_cp210x_dtr_rts,
};
static struct usb_serial_driver * const serial_drivers[] = {
        &sp_cp210x_device, NULL
};

/* 
 * There are 2 ways to create sysfs files depending upon use case.
 * 
 * 1. Create sysfs files after user space (and hence udev or similar libraries) has been informed about 
 * the presence of device. When a device is created or removed from the system the driver core (or rather 
 * kobject core) announces sysfs related things and some tool runs which save these information which is 
 * read by udev (or similar libraries). If a particular sysfs file is not required by udev, sysfs attributes 
 * can be created in probe() function. When probe() function is called userspace has been already 
 * informed that this device is present. This driver create sysfs file for controlling GPIO in probe()
 * function as udev and GPIO control is not related in any sense.
 *
 * 2. Create sysfs files before user space has been informed about the presence of device. This is typically
 * required by platform drivers or some frameworks. If cp210x will always be present in system, this approach
 * may be considered.
 */
static DEVICE_ATTR(cp210x_gpio_1, (S_IWUSR | S_IRUGO), cp210x_gpio_1_show, cp210x_gpio_1_store);

static struct attribute *sp_cp210x_attrs[] = {
        &dev_attr_cp210x_gpio_1.attr,
        NULL,
};

static const struct attribute_group sp_cp210x_attr_group = {
        .name = "sp_cp210x_gpio",
        .attrs = sp_cp210x_attrs,
};

/* 
 * Creates subdirectory and all sysfs files to be handled explicitly by this driver. The attributes are grouped 
 * to create and destroy all attributes at once easily.
 * 
 * The shell script should find directory 'sp_cp210x_gpio' in sysfs file system and read or write to sysfs file 
 * 'sp_cp210x_gpio_1'. Use gpio-cp210x.sh shell script provided with this driver for this purpose.
 *
 * For a cp210x device connected at 3rd port of 3rd root hub, interface 0 and USB configuration 1 following
 * sysfs file structure will be observed.
 *
 * 1. /sys/devices/pci0000:00/0000:00:14.0/usb3/3-3/3-3:1.0/ttyUSB0/sp_cp210x_gpio/cp210x_gpio_1
 * 
 * 2. struct usb_device is represented in the tree at /sys/devices/pci0000:00/0000:00:14.0/usb3/3-3
 *
 * 3. Interface that this driver will bound to is /sys/devices/pci0000:00/0000:00:14.0/usb3/3-3/3-3:1.0
 *
 * @port: port for which sysfs attributes are to be created.
 *
 * @return 0 on success negative error code on failure.
 */
static int create_cp210x_sysfs_attrs(struct usb_serial_port *port)
{    
    int ret;
    struct cp210x_port_private *port_priv = usb_get_serial_port_data(port);

    if((port_priv->cp210x_chip_type == PART_CP2102) || (port_priv->cp210x_chip_type == PART_CP2109))
        return 0;

    ret = sysfs_create_group(&port->dev.kobj, &sp_cp210x_attr_group);
    if (ret < 0)
        return ret;

    return 0;
}

/* 
 * Destroys directory and all sysfs files to be handled explicitly by this driver.
 *
 * @port: port for which sysfs attributes are to be deleted.
 */
static void remove_cp210x_sysfs_attrs(struct usb_serial_port *port)
{
    struct cp210x_port_private *port_priv;
    port_priv = usb_get_serial_port_data(port);

    if((port_priv->cp210x_chip_type == PART_CP2102) || (port_priv->cp210x_chip_type == PART_CP2109))
        return;

    sysfs_remove_group(&port->dev.kobj, &sp_cp210x_attr_group);
}

/* 
 * Invoked when user space application read sysfs file for GPIO_1. 
 *
 * @dev: device to be queried
 * @attr: sysfs attribute for this device
 * @buf: memory where result will be placed
 *
 * @return value of the given sysfs file
 */
static ssize_t cp210x_gpio_1_show(struct device *dev, struct device_attribute *attr, char *buf)
{
    int result = 0;
    unsigned long latch_buf = 0;
    struct usb_serial_port *port = to_usb_serial_port(dev);
    struct cp210x_port_private *port_priv = usb_get_serial_port_data(port);

    if ((PART_CP2103 == port_priv->cp210x_chip_type) || (PART_CP2104 == port_priv->cp210x_chip_type)) {
        result = read_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_HOST_TO_DEVICE, CP210X_READ_LATCH,
                0, (unsigned int*)&latch_buf, 1);
        if (result != 0)
            return result;
    }
    else if (PART_CP2105 == port_priv->cp210x_chip_type) {
        result = read_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_INTERFACE_TO_HOST, CP210X_READ_LATCH,
                port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
                (unsigned int*)&latch_buf, 1);
        if (result != 0)
            return result;
    }
    else if (PART_CP2108 == port_priv->cp210x_chip_type) {
        result = read_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_DEVICE_TO_HOST, CP210X_READ_LATCH,
                port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
                (unsigned int*)&latch_buf, 2);
        if (result != 0)
            return result;
    }
    else {
        return -ENOTSUPP;
    }

    return sprintf(buf, "%d\n", (int)(latch_buf & 0x02));
}

/* 
 * Invoked when user space application write to sysfs file for GPIO_1.
 *
 * @dev: device whose value is to be set
 * @attr: sysfs attribute for this device
 * @valbuf: data to be written to device
 * @count: number of chars in valbuf
 *
 * @return number of chars written or negative error code on failure.
 */
static ssize_t cp210x_gpio_1_store(struct device *dev, struct device_attribute *attr, const char *valbuf, 
        size_t count)
{
    int result = 0;
    unsigned int val = 0;
    unsigned long latch_buf = 0;
    struct usb_serial_port *port = to_usb_serial_port(dev);
    struct cp210x_port_private *port_priv = usb_get_serial_port_data(port);

    result = kstrtouint(valbuf, 10, &val);
    if (result != 0)
        return result;

    if(val > 0)
        latch_buf = 0x202; // 00000010 00000010
    else
        latch_buf = 0x200; // 00000010 00000000

    if ((PART_CP2103 == port_priv->cp210x_chip_type) || (PART_CP2104 == port_priv->cp210x_chip_type)) {
        result = write_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_HOST_TO_DEVICE,
                CP210X_WRITE_LATCH, latch_buf, NULL, 0);
        if (result != 0)
            return result;
    }
    else if (PART_CP2105 == port_priv->cp210x_chip_type) {
        result = write_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_HOST_TO_INTERFACE,
                CP210X_WRITE_LATCH,
                port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
                (unsigned int*)&latch_buf, 2);
        if (result != 0)
            return result;
    }
    else if (PART_CP2108 == port_priv->cp210x_chip_type) {
        result = write_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_HOST_TO_DEVICE,
                CP210X_WRITE_LATCH,
                port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
                (unsigned int*)&latch_buf, 4);
        if (result != 0)
            return result;
    }
    else {
        return -ENOTSUPP;
    }

    return count;
}

/* 
 * Invoked when a USB core finds a matching device (product) and it's port is probed.
 *
 * @port: port corresponding to the end device.
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int xyz_product_port_probe(struct usb_serial_port *port) 
{
    /*
     * Let us assume that the CP2104 based product is a temperature monitor product. It senses ambient temperature
     * and send that value to computer through cp2104 IC, The board uses a LDO (low drop out) voltage regulator
     * to power temperature sensor. The LDO takes power from Vbus pin of USB port and step down it and supply
     * power to sensor. In order to save power, this LDO is turned on when this port is probed and turned off when
     * this port is released. A GPIO_0 pin of cp2104 connects to LDO's enable pin to turn on/off it.
     *
     * int result = 0;
     * struct usb_device *usbdev = port->serial->dev;
     * struct cp210x_port_private *port_priv = usb_get_serial_port_data(port);
     *
     * if (usbdev->manufacturer && !strcmp(usbdev->manufacturer, "company name")) {
     *     if (usbdev->product && !strcmp(usbdev->product, "product name")) {
     *         result = write_cp210x_register(port, CP210X_IFC_ENABLE, REQTYPE_HOST_TO_INTERFACE, UART_ENABLE,
     *                               serial->interface->cur_altsetting->desc.bInterfaceNumber, NULL, 0);
     *         if (result != 0)
     *             return result;
     *
     *         result = write_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_HOST_TO_DEVICE, CP210X_WRITE_LATCH,
     *                                        0x101, NULL, 0);
     *         if (result != 0) {
     *             write_cp210x_register(port, CP210X_IFC_ENABLE, REQTYPE_HOST_TO_INTERFACE, UART_DISABLE,
     *                                   serial->interface->cur_altsetting->desc.bInterfaceNumber, NULL, 0);
     *         }
     *         return result;
     *      }
     *
     *      port_priv->interface_enabled = 1;
     *  }
     */
    return 0;
}

/*
 * Invoked when a USB core finds a matching device and a port in this device is probed.
 *
 * @port: port corresponding to the usb interface.
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int sp_cp210x_port_probe(struct usb_serial_port *port) 
{
    struct cp210x_products_quirk *quirk = usb_get_serial_data(port->serial);

    /* If this device has a product specific port probe defined by this driver, call it. */
    if (quirk && quirk->port_probe) {
        int ret = quirk->port_probe(port);
        if (ret != 0)
            return ret;
    }

    /* Create sysfs entries */
    create_cp210x_sysfs_attrs(port);

    return 0;
}

/*
 * Invoked by USB serial core when port is to be removed.
 *
 * @port: port removed
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int sp_cp210x_port_remove(struct usb_serial_port *port)
{
    remove_cp210x_sysfs_attrs(port);
    return 0;
}

/* 
 * Invoked when a USB core finds a matching device and it is probed if this device 
 * represent a particular product.
 *
 * @serial: usb_serial instance for this matching device.
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int xyz_product_probe(struct usb_serial *serial) 
{
    /*
     * 1. On a dual/quad serial port devices, a particular port may be dedicated for a particular functionality
     *    and may be driven from user space driver. This is typically the case with JTAG adapters where usually
     *    1st serial port will be used for JTAG purpose while others will be used for regular serial communication.
     *
     * The code given below makes this driver ignore 1st serial port and hence available for user space driver.
     *
     *  struct usb_device *usbdev = serial->dev;
     *  struct usb_interface *interface = serial->interface;
     *  if (usbdev->manufacturer && !strcmp(usbdev->manufacturer, "company name")) {
     *      if (interface == usbdev->actconfig->interface[0]) {
     *          dev_info(&usbdev->dev, "Ignoring usb-serial first port reserved for JTAG usage.\n");
     *          return -ENODEV;
     *      }
     *  }
     *
     * 2. Product specific initialization can be done like setting latency related parameters or a
     *    particular GPIO pin can be configured.
     *
     * The code given below sends a configuration control message to the given USB device.
     *
     * struct usb_device *usbdev = serial->dev;
     * int result = 0;
     * int latency = 2;
     * dev_info(&usbdev->dev, Setting latency value for usb-serial device : %d\n", latency);
     * result = write_cp210x_register(serial->port, XYZ_REQUEST, REQTYPE_HOST_TO_INTERFACE, latency,
     *                                serial->interface->cur_altsetting->desc.bInterfaceNumber,
     *                                NULL, 0);
     * if (result != 0) {
     *     return result;
     * }
     * return 0;
     *
     * 3. For non-standard or device firmware specific task such as identification of end points and then
     *    configuring something may be done here. Drivers usually need to detect endpoint addresses and
     *    buffer sizes.
     *
     * 4. For some USB devices, firmware is loaded into them when probed.
     */
    return 0;
}

/* 
 * 1. User inserts a CP210x device in system. USB host controller IC detects this device and if valid USB
 *    device is found, USB hub driver detects a new USB device. Hub driver fetches some basic information from
 *    USB device and populates some structures to represent this device and associated interfaces etc.
 * 
 * 2. The USB core than takes this device and registers USB interfaces with the kernel driver core. The kernel 
 *    driver core browse through the currently registered list of USB drivers to determine if any of them will
 *    be able to take this device and drive it.
 * 
 * 3. The cp210x driver has registered with usb core via usbserial driver for cp210x device and therefore USB 
 *    serial core accepts control of cp210x device from kernel driver core and builds up a usb_serial structure 
 *    and calls this (probe) function. After this driver initialize or do whatever is required, control is given
 *    back to USB serial core.
 * 
 * 4. The USB serial core determines number of ports in this USB device using end points information (some devices 
 *    may have more endpoint pairs than ports) to find out number of serial ports available in this USB device and 
 *    creates the struct usb_serial_port structure for every port found in this device. This will be one port, typically.
 *    
 * 5. The USB serial core calls driver's attach function if driver for this device has defined it. After the attach 
 *    function returns, individual usb_serial_port structures are registered with the kernel driver core. The 
 *    kernel driver core calls back into the USB serial core for every individual port.
 *    
 * 6. The USB serial core calls port_probe function for every port if driver has defined it. Typically, individual 
 *    port specific task like allocating and initializing data private to a port is done here by USB device driver.
 *    The USB serial core then registers ports with the tty layer.
 * 
 * This function decides whether this driver will drive the given USB interface or not. If this
 * driver wishes to handle the given USB interface it should return 0 otherwise -ENODEV. if genuine IO 
 * errors occurred, an appropriate negative errno value is returned. This function is called if the 
 * driver has registered a probe function for this device with usb-serial core.
 *
 * @serial: serial instance for this device
 * @id: USB VID Aand PID
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int sp_cp210x_probe(struct usb_serial *serial, const struct usb_device_id *id)
{
    int ret;
    struct cp210x_products_quirk *quirk = (struct cp210x_products_quirk *) id->driver_info;

    /* If this device has a product specific probe defined by this driver, call it. */
    if (quirk && quirk->probe) {
        ret = quirk->probe(serial);
        if (ret != 0)
            return ret;
    }

    /* Save driver_info as private data in USB device structure. When port is probed, port's probe function will
     * check if product's port specific probe function has been defined by this driver. If defined, that specific
     * probe function will get called. */
    usb_set_serial_data(serial, (void *)id->driver_info);

    return 0;
}

/*
 * Invoked by the USB serial core when the struct usb_serial structure is fully set up.
 * The driver may do any local initialization of the device and allocte memory.
 *
 * @serial: usb_serial instance for cp210x device
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int sp_cp210x_startup(struct usb_serial *serial)
{
    int x = 0;
    int result = 0;
    int clean = 0;
    int num_allocation = 0;
    unsigned int part_num;
    struct cp210x_port_private *port_priv;

    for (x = 0; x < serial->num_ports; x++) {

        /* Allocate memory to hold data private to a port and set its pointer for later retrieval. */
        port_priv = kzalloc(sizeof(struct cp210x_port_private), GFP_KERNEL);
        if (!port_priv) {
            result = -ENOMEM;
            clean = 1;
            break;
        }

        /* Determine CP210X chip type so that device specific task like IOCTL can be executed. */
        result = read_cp210x_register(serial->port[x], CP210X_VENDOR_SPECIFIC, REQTYPE_DEVICE_TO_HOST,
                CP210X_GET_PARTNUM,
                serial->interface->cur_altsetting->desc.bInterfaceNumber, &part_num, 1);
        if (result < 0) {
            clean = 1;
            break;
        }

        port_priv->cp210x_chip_type = part_num & 0x000000FF;

        usb_set_serial_port_data(serial->port[x], port_priv);
        num_allocation++;
    }

    if (clean == 1) {
        for (x = 0; x < num_allocation; x++) {
            port_priv = usb_get_serial_port_data(serial->port[x]);
            kfree(port_priv);
        }
        return result;
    }

    return 0;
}

/*
 * Invoked by the USB serial core when the usb_serial data structure is about to be destroyed.
 *
 * @serial: usb_serial instance corresponding to the device.
 */
static void sp_cp210x_shutdown(struct usb_serial *serial)
{
    int x = 0;
    struct cp210x_port_private *port_priv;

    for (x = 0; x < serial->num_ports; x++) {
        port_priv = usb_get_serial_port_data(serial->port[x]);
        kfree(port_priv);
    }
}

/*
 * Host sends requests to the cp210x device via the control pipe in order to write to cp210x's registers, configure 
 * and control the port etc. Different USB request as defined for cp210x device may require different size of data.
 * The 'size' is specified in bytes and values less than 16 bits wide are sent as is to cp210x device.
 *
 * @port: port corresponding to the cp210x device
 * @request: command/request to be sent to cp210x firmware
 * @requestType: define direction and two end in communication
 * @value: details as specified in app note
 * @index: generally usb interface number or 0
 * @data: data to be sent to cp210x device
 * @size: define length of data
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int write_cp210x_register(struct usb_serial_port *port, u8 request, u8 requestType, int value, 
        int index, unsigned int *data, int size)
{
    __le32 *buf = NULL;
    int result, x, length = 0;

    if (size) {
        /* Number of integers required to contain the array */
        length = (((size - 1) | 3) + 1) / 4;

        buf = kmalloc(length * sizeof(__le32), GFP_KERNEL);
        if (!buf) {
            dev_err(&port->dev, "%s - out of memory.\n", __func__);
            return -ENOMEM;
        }

        /* Array of integers into bytes */
        for (x = 0; x < length; x++)
            buf[x] = cpu_to_le32(data[x]);
    }

    /* Send a simple control message to a specified endpoint and waits for the message to complete,
     * or timeout (5000 milliseconds). */
    result = usb_control_msg(port->serial->dev, usb_sndctrlpipe(port->serial->dev, 0), request, requestType,
            value, index, buf, size, USB_CTRL_SET_TIMEOUT);

    if (buf)
        kfree(buf);

    if (result != size) {
        dev_dbg(&port->dev, "%s - Unable to write register, request=0x%x size=%d result=%d\n", __func__,
                request, size, result);
        if (result > 0)
            return -EPROTO;

        return result;
    }

    return 0;
}

/* 
 * This function reads value(s) from cp210x device using simple USB control message via the control pipe.
 *
 * @port: port corresponding to the cp210x device
 * @request: command/request to be sent to cp210x firmware
 * @requestType: define direction and two end in communication
 * @value: details as specified in app note
 * @index: generally usb interface number or 0
 * @data: data read from cp210x device
 * @size: define length of data
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int read_cp210x_register(struct usb_serial_port *port, u8 request, u8 requestType, int value, 
        int index, unsigned int *data, int size)
{
    __le32 *buf = NULL;
    int result, x, length = 0;

    /* Number of integers required to contain the array */
    length = (((size - 1) | 3) + 1) / 4;

    buf = kcalloc(length, sizeof(__le32), GFP_KERNEL);
    if (!buf) {
        dev_err(&port->dev, "%s - out of memory.\n", __func__);
        return -ENOMEM;
    }

    result = usb_control_msg(port->serial->dev, usb_rcvctrlpipe(port->serial->dev, 0), request, requestType,
            value, port->serial->interface->cur_altsetting->desc.bInterfaceNumber, buf, size,
            USB_CTRL_GET_TIMEOUT);

    /* Convert data into an array of integers */
    for (x = 0; x < length; x++)
        data[x] = le32_to_cpu(buf[x]);

    kfree(buf);

    if (result != size) {
        dev_dbg(&port->dev, "%s - Unable to read resister, request=0x%x size=%d result=%d\n", __func__,
                request, size, result);
        if (result > 0)
            return -EPROTO;

        return result;
    }

    return 0;
}

/* 
 * Invoked whenever serial port settings are to be updated. The old_termios contains currently 
 * active settings and tty->termios contains new settings to be applied. Typically, if a particular
 * value/setting specified in tty->termios is not supported by cp210x device/driver, it will be set 
 * to nearest supported value. For exammple if application asks for 7 data bits and device does
 * not support it, it will be set to 8 data bits. It is for this reason application should check
 * whether their is a difference between termios structure it sent and the setting in termios structure
 * when this function returns.
 *
 * @tty: tty device for this port
 * @port: serial port
 * @old_termios: previous/current termios settings
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static void sp_cp210x_set_termios(struct tty_struct *tty, struct usb_serial_port *port, 
        struct ktermios *old_termios)
{
    int result = 0;
    u32 baud = 0;
    unsigned int bits = 0;
    int update_data_size = 0;

    unsigned char splchar[6];

    /* Each variable is 4 bytes (32 bits) in size and ordered with offset as shown below.
     * <--ulXoffLimit--><--ulXonLimit--><--ulFlowReplace--><--ulControlHandshake--> */
    unsigned int flowctrl[4] = { 0, 0, 0, 0};

    struct usb_device *usbdev = port->serial->dev;
    struct usb_interface *interface = port->serial->interface;
    struct cp210x_port_private *port_priv = usb_get_serial_port_data(port);

    /* B0, is used to terminate the connection.  If B0 is specified, the modem control lines shall
       no longer be asserted. No flow control and drop DTR, RTS. It is also used to wakeup some 
       modems for example Siemens MC35i. */
    if ((tty->termios.c_cflag & CBAUD) == B0 ) {
        flowctrl[0] |= 0x01;
        flowctrl[1]  = 0x40;
        result = write_cp210x_register(port, CP210X_SET_FLOW, REQTYPE_HOST_TO_INTERFACE, 0,
                port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
                flowctrl, 0x0010);
        update_cp210x_mctrl_lines(port, 0, TIOCM_DTR | TIOCM_RTS);
        return;
    }

    /* If spring back to life from B0, raise DTR and RTS. This might get overridden in next steps. */
    if (!old_termios || (old_termios->c_cflag & CBAUD) == B0) {
        update_cp210x_mctrl_lines(port, TIOCM_DTR | TIOCM_RTS, 0);
    }

    /* Update baudrate (as per AN205 app note) */
    baud = tty_get_baud_rate(tty);
    if (!baud) {
        baud = 9600;
    }

    if (baud <= 300)
        baud = 300;
    else if (baud <= 600)     baud = 600;
    else if (baud <= 1200)    baud = 1200;
    else if (baud <= 1800)    baud = 1800;
    else if (baud <= 2400)    baud = 2400;
    else if (baud <= 4000)    baud = 4000;
    else if (baud <= 4803)    baud = 4800;
    else if (baud <= 7207)    baud = 7200;
    else if (baud <= 9612)    baud = 9600;
    else if (baud <= 14428)   baud = 14400;
    else if (baud <= 16062)   baud = 16000;
    else if (baud <= 19250)   baud = 19200;
    else if (baud <= 28912)   baud = 28800;
    else if (baud <= 38601)   baud = 38400;
    else if (baud <= 51558)   baud = 51200;
    else if (baud <= 56280)   baud = 56000;
    else if (baud <= 58053)   baud = 57600;
    else if (baud <= 64111)   baud = 64000;
    else if (baud <= 77608)   baud = 76800;
    else if (baud <= 117028)  baud = 115200;
    else if (baud <= 129347)  baud = 128000;
    else if (baud <= 156868)  baud = 153600;
    else if (baud <= 237832)  baud = 230400;
    else if (baud <= 254234)  baud = 250000;
    else if (baud <= 273066)  baud = 256000;
    else if (baud <= 491520)  baud = 460800;
    else if (baud <= 567138)  baud = 500000;
    else if (baud <= 670254)  baud = 576000;
    else if (baud < 1000000)
        baud = 921600;
    else if (baud > 2000000)
        baud = 2000000;
    else {
        baud = 9600;
    }

    result = write_cp210x_register(port, CP210X_SET_BAUDRATE, REQTYPE_HOST_TO_INTERFACE, 0,
            port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
            &baud, 4);
    if(result < 0) {
        dev_dbg(&port->dev, "%s - failed to set baudrate with err code: %d\n", __func__, result);
        if (old_termios != NULL)
            baud = tty_termios_baud_rate(old_termios);
        else
            baud = 0;
    }

    tty_encode_baud_rate(tty, baud, baud);

    /* Update flow control (AN571 app note). */
    flowctrl[0] &= ~0x7B;

    if (tty->termios.c_cflag & CRTSCTS) {
        /* hardware (RTS/CTS) flow control, DTR will always be on. */
        flowctrl[0] &= ~0x7B;
        flowctrl[0] |=  0x09;
        flowctrl[1]  =  0x80;
    }
    else if((tty->termios.c_iflag & IXON) || (tty->termios.c_iflag & IXOFF)) {
        /* software flow control */
        flowctrl[0] |= 0x01;
        flowctrl[1] |= 0x07;

        /* set xon/xoff limit based on chip type */
        if ((PART_CP2105 == port_priv->cp210x_chip_type) && (interface == usbdev->actconfig->interface[1])) {
            /* ECI */
            flowctrl[2] |= 280;
            flowctrl[3] |= 280;
        }else {
            /* SCI */
            flowctrl[2] |= 500;
            flowctrl[3] |= 500;
        }

        splchar[4] = tty->termios.c_cc[VSTART];
        splchar[5] = tty->termios.c_cc[VSTOP];

        result = usb_control_msg(port->serial->dev, usb_sndctrlpipe(port->serial->dev, 0), CP210X_SET_CHARS,
                REQTYPE_HOST_TO_INTERFACE, 0,
                port->serial->interface->cur_altsetting->desc.bInterfaceNumber, splchar,
                0x0006, USB_CTRL_SET_TIMEOUT);
        if (result != 0x0006) {
            dev_dbg(&port->dev, "%s - failed with err code: %d\n", __func__, result);
        }
    }
    else {
        /* no flow control */
        flowctrl[0] &= ~0x7B;
        flowctrl[0] |=  0x01;
        flowctrl[1]  =  0x40;
    }

    result = write_cp210x_register(port, CP210X_SET_FLOW, REQTYPE_HOST_TO_INTERFACE, 0,
            port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
            flowctrl, 0x0010);

    /* Update number of data bits in UART frame */
    bits &= ~BITS_DATA_MASK; /* reset */

    switch (tty->termios.c_cflag & CSIZE) {
    case CS5: bits |= BITS_DATA_5;
    if(baud >= 921600) {
        bits |= BITS_DATA_8;
        tty->termios.c_cflag |= CS8;
        update_data_size = 1;
        dev_dbg(&port->dev, "for baudrate >= 921600, only 7/8 data bits are supported.");
    }
    break;

    case CS6: bits |= BITS_DATA_6;
    if(baud >= 921600) {
        bits |= BITS_DATA_8;
        tty->termios.c_cflag |= CS8;
        update_data_size = 1;
        dev_dbg(&port->dev, "for baudrate >= 921600, only 7/8 data bits are supported.");
    }
    break;

    case CS7: bits |= BITS_DATA_7;
    break;

    case CS8: bits |= BITS_DATA_8;
    break;

    default:  bits |= BITS_DATA_8;
    tty->termios.c_cflag |= CS8;
    update_data_size = 1;
    dev_dbg(&port->dev, "cp210x device supports only 5,6,7,8 number of data bits.\n");
    }

    /* Update number of stop bits */
    bits &= ~BITS_STOP_MASK;

    if (tty->termios.c_cflag & CSTOPB)
        bits |= BITS_STOP_2;
    else
        bits |= BITS_STOP_1;

    /* Update parity type */
    bits &= ~BITS_PARITY_MASK;

    if (tty->termios.c_cflag & PARENB) {
        if (tty->termios.c_cflag & CMSPAR) {
            if (tty->termios.c_cflag & PARODD)
                bits |= BITS_PARITY_MARK;
            else
                bits |= BITS_PARITY_SPACE;
        }else {
            if (tty->termios.c_cflag & PARODD)
                bits |= BITS_PARITY_ODD;
            else
                bits |= BITS_PARITY_EVEN;
        }
    }

    result = write_cp210x_register(port, CP210X_SET_LINE_CTL, REQTYPE_HOST_TO_INTERFACE, bits,
            port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
            NULL, 0);
    if(result < 0) {
        /* If failed revert back settings */
        if(update_data_size == 1)
            tty->termios.c_cflag |= (old_termios->c_cflag & CSIZE);
        dev_dbg(&port->dev, "%s - failed with err code: %d\n", __func__, result);
    }
}

/* 
 * Invoked by tty layer when application invokes device/driver specific IOCTL command.
 *
 * @tty: tty device
 * @cmd: command to be executed
 * @arg: arguments to be passed to this command
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int sp_cp210x_ioctl(struct tty_struct *tty, unsigned int cmd, unsigned long arg)
{
    int result = 0;
    unsigned long latch_buf = 0;
    struct usb_serial_port *port = tty->driver_data;
    struct cp210x_port_private *port_priv = usb_get_serial_port_data(port);

    switch (cmd) {

    case IOCTL_GPIOSET:

        if ((PART_CP2103 == port_priv->cp210x_chip_type) || (PART_CP2104 == port_priv->cp210x_chip_type)) {
            if (copy_from_user(&latch_buf, (unsigned int*)arg, 2))
                return -EFAULT;
            result = write_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_HOST_TO_DEVICE,
                    CP210X_WRITE_LATCH, latch_buf, NULL, 0);
            if (result != 0)
                return result;
            return 0;
        }
        else if (PART_CP2105 == port_priv->cp210x_chip_type) {
            if (copy_from_user(&latch_buf, (unsigned int*)arg, 2))
                return -EFAULT;
            result = write_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_HOST_TO_INTERFACE,
                    CP210X_WRITE_LATCH,
                    port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
                    (unsigned int*)&latch_buf, 2);
            return result;
        }
        else if (PART_CP2108 == port_priv->cp210x_chip_type) {
            if (copy_from_user(&latch_buf, (unsigned int*)arg, 4))
                return -EFAULT;
            result = write_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_HOST_TO_DEVICE,
                    CP210X_WRITE_LATCH,
                    port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
                    (unsigned int*)&latch_buf, 4);
            return result;
        }
        else {
            return -ENOTSUPP;
        }
        break;

    case IOCTL_GPIOGET:

        if ((PART_CP2103 == port_priv->cp210x_chip_type) || (PART_CP2104 == port_priv->cp210x_chip_type)) {
            result = read_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_HOST_TO_DEVICE,
                    CP210X_READ_LATCH, 0, (unsigned int*)&latch_buf, 1);
            if (result != 0)
                return result;
            if (copy_to_user((unsigned int*)arg, &latch_buf, 1))
                return -EFAULT;
            return 0;
        }
        else if (PART_CP2105 == port_priv->cp210x_chip_type) {
            result = read_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_INTERFACE_TO_HOST,
                    CP210X_READ_LATCH,
                    port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
                    (unsigned int*)&latch_buf, 1);
            if (result != 0)
                return result;
            if (copy_to_user((unsigned int*)arg, &latch_buf, 1))
                return -EFAULT;
            return 0;
        }
        else if (PART_CP2108 == port_priv->cp210x_chip_type) {
            result = read_cp210x_register(port, CP210X_VENDOR_SPECIFIC, REQTYPE_DEVICE_TO_HOST,
                    CP210X_READ_LATCH,
                    port->serial->interface->cur_altsetting->desc.bInterfaceNumber,
                    (unsigned int*)&latch_buf, 2);
            if (result != 0)
                return result;
            if (copy_to_user((unsigned int*)arg, &latch_buf, 2))
                return -EFAULT;
            return 0;
        }
        else {
            return -ENOTSUPP;
        }
        break;

    default:
        break;
    }

    return -ENOIOCTLCMD;
}

/* 
 * Invoked when application issue TIOCMGET IOCTL command.
 *
 * @tty: tty device
 *
 * @return bit mask of line/modem's status on success otherwise negative error code on failure.
 */
static int sp_cp210x_tiocmget(struct tty_struct *tty)
{
    struct usb_serial_port *port = tty->driver_data;
    unsigned int control;
    int result;

    result = read_cp210x_register(port, CP210X_GET_MDMSTS, REQTYPE_INTERFACE_TO_HOST, CP210X_GET_PARTNUM,
            0, &control, 1);
    if (result < 0)
        return result;

    result= ((control & CONTROL_DTR)  ? TIOCM_DTR : 0) |
            ((control & CONTROL_RTS)  ? TIOCM_RTS : 0) |
            ((control & CONTROL_CTS)  ? TIOCM_CTS : 0) |
            ((control & CONTROL_DSR)  ? TIOCM_DSR : 0) |
            ((control & CONTROL_RING) ? TIOCM_RI  : 0) |
            ((control & CONTROL_DCD)  ? TIOCM_CD  : 0);
    return result;
}

/* 
 * This function sets the modem handshaking states for the selected CP210x interface according 
 * to the value of bit mask set. DTR and RTS values can be set only if the current handshaking 
 * state of the interface allows direct control of the modem control lines.
 *
 * @port: serial port
 * @set: bit mask of lines to be asserted
 * @clear: bit mask of lines to be de-asserted
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int update_cp210x_mctrl_lines(struct usb_serial_port *port, unsigned int set, unsigned int clear)
{
    unsigned int control = 0;

    if (set & TIOCM_RTS) {
        control |= CONTROL_RTS;
        control |= CONTROL_WRITE_RTS;
    }
    if (set & TIOCM_DTR) {
        control |= CONTROL_DTR;
        control |= CONTROL_WRITE_DTR;
    }
    if (clear & TIOCM_RTS) {
        control &= ~CONTROL_RTS;
        control |= CONTROL_WRITE_RTS;
    }
    if (clear & TIOCM_DTR) {
        control &= ~CONTROL_DTR;
        control |= CONTROL_WRITE_DTR;
    }

    return write_cp210x_register(port, CP210X_SET_MHS, REQTYPE_HOST_TO_INTERFACE, control,
            port->serial->interface->cur_altsetting->desc.bInterfaceNumber, NULL, 0);
}

/* 
 * Invoked when application issue TIOCMSET (TIOCMBIC,TIOCMBIS) IOCTL command.
 *
 * @tty: tty device
 * @set: bit mask of lines to be asserted
 * @clear: bit mask of lines to be de-asserted
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int sp_cp210x_tiocmset(struct tty_struct *tty, unsigned int set, unsigned int clear)
{
    return update_cp210x_mctrl_lines(tty->driver_data, set, clear);
}

/* 
 * Invoked by tty layer when RTS/DTR lines need to be set/reset.
 *
 * @port: serial port
 * @on: 0 for de-assertion and 1 for assertion of DTR and RTS lines.
 */
static void sp_cp210x_dtr_rts(struct usb_serial_port *port, int on)
{
    if (on)
        update_cp210x_mctrl_lines(port, TIOCM_DTR | TIOCM_RTS, 0);
    else
        update_cp210x_mctrl_lines(port, 0, TIOCM_DTR | TIOCM_RTS);
}

/* 
 * Invoked by tty layer to turn on or off BREAK condition on port.
 *
 * @tty: tty device
 * @break_state: 0 to turn off break and non zero to set break condition unconditionally
 */
static void sp_cp210x_break_ctl(struct tty_struct *tty, int break_state)
{
    int result = 0;
    unsigned int state = 0;
    struct usb_serial_port *port = tty->driver_data;

    if (break_state == 0)
        state = BREAK_OFF;
    else
        state = BREAK_ON;

    result = write_cp210x_register(port, CP210X_SET_BREAK, REQTYPE_HOST_TO_INTERFACE, state,
            port->serial->interface->cur_altsetting->desc.bInterfaceNumber, NULL, 0);
    if (result != 0)
        dev_dbg(&port->dev, "%s - failed with err code: %d\n", __func__, result);
}

/*
 * 1. After the tty device node has been bound to individual serial port, when application opens
 *    serial port, kernel finds that this node is registered with it and it then call tty layer's
 *    open function. 
 *
 * 2. Because USB serial core has registered this node with tty layer, USB serial core's open
 *    function gets called. This function determines which specific driver is managing this node
 *    and it then call open function of that specific driver of USB-UART interface. This specific
 *    driver typically initialize interface for communication and allocate memory if needed.
 *
 * 3. The module count for the specified USB serial driver is incremented in order prevent it from 
 *    being unloaded when an application is using it.
 *
 * @port: serial port
 *
 * @return 0 on success otherwise negative error code on failure.
 */
static int sp_cp210x_open(struct tty_struct *tty, struct usb_serial_port *port)
{
    int result = 0;
    struct cp210x_port_private *port_priv = usb_get_serial_port_data(port);

    /* If the interface is not enabled, enable it. */
    if(port_priv->interface_enabled == 0) {
        result = write_cp210x_register(port, CP210X_IFC_ENABLE, REQTYPE_HOST_TO_INTERFACE, UART_ENABLE,
                port->serial->interface->cur_altsetting->desc.bInterfaceNumber, NULL, 0);
        if (result < 0)
            return result;
    }

    /* The usbserial driver initializes default termios settings in usb_serial_init function
     * (9600 8N1 raw mode). We apply them to a cp210x device as is, to start with a sane state. */
    if (tty)
        sp_cp210x_set_termios(tty, port, NULL);

    /* This will clear throttle, and submit read urb (issue an asynchronous transfer request
     * for an endpoint). */
    return usb_serial_generic_open(tty, port);
}

/* 
 * 1. The application call close system call on serial port's device node. This causes tty_release function
 *    in tty core to be invoked.
 *
 * 2. The tty_release function determines if this is the last reference held on this device node as same
 *    device node can be opened many times (non-exclusive mode). If it is last reference then close function
 *    of USB serial core is called.
 *
 * 3. The USB serial core calls close function of specific USB interface driver and possibly cancel pending
 *    USB transfers if any.
 *
 * 4. The USB serial core then decrements the module count for the USB serial driver and if count reaches
 *    zero, it may be unloaded.
 *
 * @port: serial port
 */
static void sp_cp210x_close(struct usb_serial_port *port)
{	
    usb_serial_generic_close(port);

    /* if close is invoked by application immediately after sending data and data is unsent physically from
     * cp210x, purge it. */
    write_cp210x_register(port, CP210X_PURGE, REQTYPE_HOST_TO_INTERFACE, 0x000F,
            port->serial->interface->cur_altsetting->desc.bInterfaceNumber, NULL, 0);

    write_cp210x_register(port, CP210X_IFC_ENABLE, REQTYPE_HOST_TO_INTERFACE, UART_DISABLE,
            port->serial->interface->cur_altsetting->desc.bInterfaceNumber, NULL, 0);
}

/* Helper macro for registering a usb-serial driver (module_init/module_exit). This basically registers a 
 * USB interface driver with the USB core. The list of unattached interfaces will be rescanned whenever a 
 * new driver is added, allowing the new driver to be attached to any recognized interfaces. */
module_usb_serial_driver(serial_drivers, id_table);

MODULE_AUTHOR("Rishi Gupta");
MODULE_DESCRIPTION("CP210x USB-UART device's driver - v1.0");
MODULE_LICENSE("GPL");

module_param(dbg, bool, S_IRUGO | S_IWUSR);
MODULE_PARM_DESC(dbg, "Debuging enabled or not");
