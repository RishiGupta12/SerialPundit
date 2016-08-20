/*
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
 */

package com.serialpundit.serial.vendor;

/**
 * <p>Represents the FT_PROGRAM_DATA structure declared in ftd2xx.h header file.</p>
 * 
 * @author Rishi Gupta
 */
public final class FTprogramData {

    private int[] info;
    private String manufacturer;
    private String manufacturerID;
    private String description;
    private String serialNumber;

    /**
     * <p>Construct and allocates a new FTprogramData object with given details.</p>
     * 
     * @param manufacturer manufacturer of the device.
     * @param manufacturerID manufacturer ID of the device vendor.
     * @param description description of this device.
     * @param serialNumber serial number of this device.
     */
    public FTprogramData(int[] info, char[] manufacturer, char[] manufacturerID, char[] description, 
            char[] serialNumber) {
        this.info = info;
        this.manufacturer = new String(manufacturer);
        this.manufacturerID = new String(manufacturerID);
        this.description = new String(description);
        this.serialNumber = new String(serialNumber);
    }

    /** 
     * <p>Gives the value of Signature1.</p>
     * 
     * @return integer value of Signature1.
     */
    public int getSignature1() {
        return info[0];
    }

    /** 
     * <p>Gives the value of Signature1.</p>
     * 
     * @return integer value of Signature1.
     */
    public int getSignature2() {
        return info[1];
    }

    /** 
     * <p>Gives the value of Signature1.</p>
     * 
     * @return FT_PROGRAM_DATA version.
     */
    public int getVersion() {
        return info[2];
    }

    /** 
     * <p>Gives the value of Vendor Id for this device.</p>
     * 
     * @return USB VendorId for this device.
     */
    public int getVendorId() {
        return info[3];
    }

    /** 
     * <p>Gives the value of Product Id for this device.</p>
     * 
     * @return USB ProductId for this device.
     */
    public int getProductId() {
        return info[4];
    }

    /** 
     * <p>Retrieves the manufacturer string for this FT device.</p>
     * 
     * @return manufacturer string for this FT device.
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /** 
     * <p>Retrieves the manufacturer ID string for this FT device.</p>
     * 
     * @return manufacturer ID string for this FT device.
     */
    public String getmanufacturerID() {
        return manufacturerID;
    }

    /** 
     * <p>Retrieves the description for this FT device.</p>
     * 
     * @return description string for this FT device.
     */
    public String getDescription() {
        return description;
    }

    /** 
     * <p>Retrieves the serial number string for this FT device.</p>
     * 
     * @return serial number string for this FT device.
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /** 
     * <p>Retrieves the maximum power (0 < MaxPower <= 500) required for this FT device.</p>
     * 
     * @return maximum power value required for this FT device.
     */
    public int getMaxPower() {
        return info[9];
    }

    /** 
     * <p>Retrieves the PnP feature (0 = disabled, 1 = enabled) value for this FT device.</p>
     * 
     * @return 0 if disabled or 1 if enabled.
     */
    public int getPnP() {
        return info[10];
    }

    /** 
     * <p>Finds whether device is self or bus powered (0 = bus powered, 1 = self powered).</p>
     * 
     * @return 0 id bus powered or 1 is self powered.
     */
    public int isSelfPowered() {
        return info[11];
    }

    /** 
     * <p>Finds whether device is able to support remote wake up or not (0 = not capable, 1 = capable).</p>
     * 
     * @return 0 if not capable or 1 if capable.
     */
    public int isRemoteWakeup() {
        return info[12];
    }


    /* Rev4 (FT232B) extensions */


    /** 
     * <p>Finds whether device is Rev4 or not (non-zero if Rev4 chip, zero otherwise).</p>
     * 
     * <p>Mainly applicable for Rev4 (FT232B) extensions.</p>
     * 
     * @return non-zero value if Rev4 chip or 0 if chip is not rev4.
     */
    public int isRev4() {
        return info[13];
    }

    /** 
     * <p>Finds whether in endpoint is isochronous or not.</p>
     * 
     * <p>Mainly applicable for Rev4 (FT232B) extensions.</p>
     * 
     * @return non-zero value if in endpoint is isochronous.
     */
    public int isIsoIn() {
        return info[14];
    }

    /** 
     * <p>Finds whether out endpoint is isochronous or not.</p>
     * 
     * <p>Mainly applicable for Rev4 (FT232B) extensions.</p>
     * 
     * @return non-zero value if out endpoint is isochronous.
     */
    public int isIsoOut() {
        return info[15];
    }

    /** 
     * <p>Finds whether pull down is enabled or not.</p>
     * 
     * <p>Mainly applicable for Rev4 (FT232B) extensions.</p>
     * 
     * @return non-zero value if pull down is enabled.
     */
    public int isPullDownEnabled() {
        return info[16];
    }

    /** 
     * <p>Finds whether serial number is to be used or not.</p>
     * 
     * <p>Mainly applicable for Rev4 (FT232B) extensions.</p>
     * 
     * @return non-zero value if serial number is to be used.
     */
    public int isSerNumEnabled() {
        return info[17];
    }

    /** 
     * <p>Finds whether device uses USB version or not.</p>
     * 
     * <p>Mainly applicable for Rev4 (FT232B) extensions.</p>
     * 
     * @return non-zero value if chip uses USB version.
     */
    public int hasUSBVersionEnabled() {
        return info[18];
    }

    /** 
     * <p>Retrieves the USB version for the device.</p>
     * 
     * <p>Mainly applicable for Rev4 (FT232B) extensions.</p>
     * 
     * @return binary coded USB version number (0x0200 means USB 2.0).
     */
    public int getUSBVersion() {
        return info[19];
    }


    /* Rev 5 (FT2232) extensions */


    /** 
     * <p>Finds whether device is Rev5 or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if Rev5 chip, zero otherwise.
     */
    public int isRev5() {
        return info[20];
    }

    /** 
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if in endpoint is isochronous.
     */
    public int isIsoInA() {
        return info[21];
    }

    /** 
     * <p>Finds whether in endpoint is isochronous or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if in endpoint is isochronous.
     */
    public int isIsoInB() {
        return info[22];
    }

    /** 
     * <p>Finds whether out endpoint is isochronous or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if out endpoint is isochronous.
     */
    public int isIsoOutA() {
        return info[23];
    }

    /** 
     * <p>Finds whether out endpoint is isochronous or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if out endpoint is isochronous.
     */
    public int isIsoOutB() {
        return info[24];
    }

    /** 
     * <p>Finds whether pull down is enabled or not.</p>
     * 
     * @return non-zero value if pull down is enabled.
     */
    public int isPullDownEnabled5() {
        return info[25];
    }

    /** 
     * <p>Finds whether serial number is to be used or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if serial number is to be used.
     */
    public int isSerNumEnabled5() {
        return info[26];
    }

    /** 
     * <p>Finds whether device uses USB version or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if chip uses USB version.
     */
    public int hasUSBVersionEnabled5() {
        return info[27];
    }

    /** 
     * <p>Retrieves the USB version for the device.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return binary coded USB version number (0x0200 means USB 2.0).
     */
    public int getUSBVersion5() {
        return info[28];
    }

    /** 
     * <p>Finds whether interface is high current or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if interface is high current.
     */
    public int isAIsHighCurrent() {
        return info[29];
    }

    /** 
     * <p>Finds whether interface is high current or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if interface is high current.
     */
    public int isBIsHighCurrent() {
        return info[30];
    }

    /** 
     * <p>Finds whether interface is 245 FIFO or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if interface is 245 FIFO.
     */
    public int isIFAIsFifo() {
        return info[31];
    }

    /** 
     * <p>Finds whether interface is 245 FIFO CPU target or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if interface is 245 FIFO CPU target.
     */
    public int isIFAIsFifoTar() {
        return info[32];
    }

    /** 
     * <p>Finds whether interface is fast serial or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if interface is Fast serial.
     */
    public int isIFAIsFastSer() {
        return info[33];
    }

    /** 
     * <p>Finds whether interface is to use VCP drivers or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if interface is to use VCP drivers.
     */
    public int isAIsVCP() {
        return info[34];
    }

    /** 
     * <p>Finds whether interface is 245 FIFO or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if interface is 245 FIFO.
     */
    public int isIFBIsFifo() {
        return info[35];
    }

    /** 
     * <p>Finds whether interface is 245 FIFO CPU target or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if interface is 245 FIFO CPU target.
     */
    public int isIFBIsFifoTar() {
        return info[36];
    }

    /** 
     * <p>Finds whether interface is fast serial or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if interface is Fast serial.
     */
    public int isIFBIsFastSer() {
        return info[37];
    }

    /** 
     * <p>Finds whether interface is to use VCP drivers or not.</p>
     * 
     * <p>Mainly applicable for Rev 5 (FT2232) extensions.</p>
     * 
     * @return non-zero value if interface is to use VCP drivers.
     */
    public int isBIsVCP() {
        return info[38];
    }


    /* Rev 6 (FT232R) extensions */


    /** 
     * <p>Finds whether external oscillator is to be used or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return 1 if external oscillator is to be used.
     */
    public int useExtOsc() {
        return info[39];
    }

    /** 
     * <p>Finds whether more current is to be supplied at IC pins or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return 1 if high current is to be supplied.
     */
    public int useHighDriveIOs() {
        return info[40];
    }

    /** 
     * <p>Gives the end point size of this device.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return end pint size.
     */
    public int getEndpointSize() {
        return info[41];
    }

    /** 
     * <p>Finds whether pull down is enabled or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return non-zero value if pull down is enabled.
     */
    public int isPullDownEnableR() {
        return info[42];
    }

    /** 
     * <p>Finds whether serial number is to be used or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return non-zero value if serial number is to be used.
     */
    public int isSerNumEnableR() {
        return info[43];
    }

    /** 
     * <p>Checks whether polarity if TXD line is inverted or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return non-zero value if TXD line is inverted.
     */
    public int invertTXD() {
        return info[44];
    }

    /** 
     * <p>Checks whether polarity if RXD line is inverted or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return non-zero value if RXD line is inverted.
     */
    public int invertRXD() {
        return info[45];
    }

    /** 
     * <p>Checks whether polarity if RTS line is inverted or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return non-zero value if RTS line is inverted.
     */
    public int invertRTS() {
        return info[46];
    }

    /** 
     * <p>Checks whether polarity if CTS line is inverted or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return non-zero value if CTS line is inverted.
     */
    public int invertCTS() {
        return info[47];
    }

    /** 
     * <p>Checks whether polarity if DTR line is inverted or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return non-zero value if DTR line is inverted.
     */
    public int invertDTR() {
        return info[48];
    }

    /** 
     * <p>Checks whether polarity if DSR line is inverted or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return non-zero value if DSR line is inverted.
     */
    public int invertDSR() {
        return info[49];
    }

    /** 
     * <p>Checks whether polarity if DCD line is inverted or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return non-zero value if DCD line is inverted.
     */
    public int invertDCD() {
        return info[50];
    }

    /** 
     * <p>Checks whether polarity if RI line is inverted or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return non-zero value if RI line is inverted.
     */
    public int invertRI() {
        return info[51];
    }

    /** 
     * <p>Gives the CBUS 0 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return CBUS 0 configuration value.
     */
    public int getCbus0() {
        return info[52];
    }

    /** 
     * <p>Gives the CBUS 1 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return CBUS 1 configuration value.
     */
    public int getCbus1() {
        return info[53];
    }

    /** 
     * <p>Gives the CBUS 2 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return CBUS 2 configuration value.
     */
    public int getCbus2() {
        return info[54];
    }

    /** 
     * <p>Gives the CBUS 3 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return CBUS 3 configuration value.
     */
    public int getCbus3() {
        return info[55];
    }

    /** 
     * <p>Gives the CBUS 4 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return CBUS 4 configuration value.
     */
    public int getCbus4() {
        return info[56];
    }

    /** 
     * <p>Finds whether D2XX driver is in use or not.</p>
     * 
     * <p>Mainly applicable for Rev 6 (FT232R) extensions.</p>
     * 
     * @return non-zero value if using D2XX driver.
     */
    public int isRIsD2XX() {
        return info[57];
    }

    /** 
     * <p>Finds whether pull down is enabled or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if pull down is enabled.
     */
    public int isPullDownEnable7() {
        return info[58];
    }

    /** 
     * <p>Finds whether serial number is to be used or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if serial number is to be used.
     */
    public int isSerNumEnable7() {
        return info[59];
    }

    /** 
     * <p>Finds whether AL pins have slow slew or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if AL pins have slow slew.
     */
    public int isALSlowSlew() {
        return info[60];
    }

    /** 
     * <p>Finds whether if AL pins are Schmitt input or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if AL pins are Schmitt input.
     */
    public int isALSchmittInput() {
        return info[61];
    }

    /** 
     * <p>Retrieves the AL drive current value.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return AL drive current value.
     */
    public int getALDriveCurrent() {
        return info[62];
    }

    /** 
     * <p>Finds whether AL pins have slow slew or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if AH pins have slow slew.
     */
    public int isAHSlowSlew() {
        return info[63];
    }

    /** 
     * <p>Finds whether if AH pins are Schmitt input or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if AH pins are Schmitt input.
     */
    public int isAHSchmittInput() {
        return info[64];
    }

    /** 
     * <p>Retrieves the AH drive current value.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return AH drive current value.
     */
    public int getAHDriveCurrent() {
        return info[65];
    }

    /** 
     * <p>Finds whether BL pins have slow slew or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if BL pins have slow slew.
     */
    public int isBLSlowSlew() {
        return info[66];
    }

    /** 
     * <p>Finds whether if BL pins are Schmitt input or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if BL pins are Schmitt input.
     */
    public int isBLSchmittInput() {
        return info[67];
    }

    /** 
     * <p>Retrieves the BL drive current value.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return BL drive current value.
     */
    public int getBLDriveCurrent() {
        return info[68];
    }

    /** 
     * <p>Finds whether BL pins have slow slew or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if BH pins have slow slew.
     */
    public int isBHSlowSlew() {
        return info[69];
    }

    /** 
     * <p>Finds whether if BH pins are Schmitt input or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if BH pins are Schmitt input.
     */
    public int isBHSchmittInput() {
        return info[70];
    }

    /** 
     * <p>Retrieves the BH drive current value.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return BH drive current value.
     */
    public int getBHDriveCurrent() {
        return info[71];
    }

    /** 
     * <p>Finds whether interface is 245 FIFO or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if interface is 245 FIFO.
     */
    public int isIFAIsFifo7() {
        return info[72];
    }

    /** 
     * <p>Finds whether interface is 245 FIFO CPU target or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if interface is 245 FIFO CPU target.
     */
    public int isIFAIsFifoTar7() {
        return info[73];
    }

    /** 
     * <p>Finds whether interface is fast serial or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if interface is Fast serial.
     */
    public int isIFAIsFastSer7() {
        return info[74];
    }

    /** 
     * <p>Finds whether interface is to use VCP drivers or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if interface is to use VCP drivers.
     */
    public int isAIsVCP7() {
        return info[75];
    }

    /** 
     * <p>Finds whether interface is 245 FIFO or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if interface is 245 FIFO.
     */
    public int isIFBIsFifo7() {
        return info[76];
    }

    /** 
     * <p>Finds whether interface is 245 FIFO CPU target or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if interface is 245 FIFO CPU target.
     */
    public int isIFBIsFifoTar7() {
        return info[77];
    }

    /** 
     * <p>Finds whether interface is fast serial or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if interface is Fast serial.
     */
    public int isIFBIsFastSer7() {
        return info[78];
    }

    /** 
     * <p>Finds whether interface is to use VCP drivers or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if interface is to use VCP drivers.
     */
    public int isBIsVCP7() {
        return info[79];
    }

    /** 
     * <p>Finds whether power saving is enabled or not.</p>
     * 
     * <p>Mainly applicable for Rev 7 (FT2232H) extensions.</p>
     * 
     * @return non-zero value if using BCBUS7 to save power for self-powered designs.
     */
    public int isPowerSaveEnabled() {
        return info[80];
    }


    /* Rev 8 (FT4232H) Extensions */


    /** 
     * <p>Finds whether pull down is enabled or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if pull down is enabled.
     */
    public int isPullDownEnabled8() {
        return info[81];
    }

    /** 
     * <p>Finds whether serial number is to be used or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if serial number is to be used.
     */
    public int isSerNumEnabled8() {
        return info[82];
    }

    /** 
     * <p>Finds whether A pins have slow slew or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if A pins have slow slew.
     */
    public int isASlowSlew() {
        return info[83];
    }

    /** 
     * <p>Finds whether if A pins are Schmitt input or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if A pins are Schmitt input.
     */
    public int isASchmittInput() {
        return info[84];
    }

    /** 
     * <p>Retrieves the A drive current value. Valid values are 4mA, 8mA, 12mA, 16mA.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return A drive current value.
     */
    public int getADriveCurrent() {
        return info[85];
    }

    /** 
     * <p>Finds whether B pins have slow slew or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     *  
     * @return non-zero value if B pins have slow slew.
     */
    public int isBSlowSlew() {
        return info[86];
    }

    /** 
     * <p>Finds whether if B pins are Schmitt input or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if B pins are Schmitt input.
     */
    public int isBSchmittInput() {
        return info[87];
    }

    /** 
     * <p>Retrieves the B drive current value. Valid values are 4mA, 8mA, 12mA, 16mA.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return B drive current value.
     */
    public int getBDriveCurrent() {
        return info[88];
    }

    /** 
     * <p>Finds whether C pins have slow slew or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if C pins have slow slew.
     */
    public int isCSlowSlew() {
        return info[89];
    }

    /** 
     * <p>Finds whether if C pins are Schmitt input or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if C pins are Schmitt input.
     */
    public int isCSchmittInput() {
        return info[90];
    }

    /** 
     * <p>Retrieves the C drive current value. Valid values are 4mA, 8mA, 12mA, 16mA.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return C drive current value.
     */
    public int getCDriveCurrent() {
        return info[91];
    }

    /** 
     * <p>Finds whether D pins have slow slew or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if D pins have slow slew.
     */
    public int isDSlowSlew() {
        return info[92];
    }

    /** 
     * <p>Finds whether if D pins are Schmitt input or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if D pins are Schmitt input.
     */
    public int isDSchmittInput() {
        return info[93];
    }

    /** 
     * <p>Retrieves the D drive current value. Valid values are 4mA, 8mA, 12mA, 16mA.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return D drive current value.
     */
    public int getDDriveCurrent() {
        return info[94];
    }

    /** 
     * <p>Checks whether port A uses RI as RS485 TXDEN or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if port A uses RI as RS485 TXDEN.
     */
    public int isARIIsTXDEN() {
        return info[95];
    }

    /** 
     * <p>Checks whether port B uses RI as RS485 TXDEN or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if port B uses RI as RS485 TXDEN.
     */
    public int isBRIIsTXDEN() {
        return info[96];
    }

    /** 
     * <p>Checks whether port C uses RI as RS485 TXDEN or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if port C uses RI as RS485 TXDEN.
     */
    public int isCRIIsTXDEN() {
        return info[97];
    }

    /** 
     * <p>Checks whether port D uses RI as RS485 TXDEN or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if port D uses RI as RS485 TXDEN.
     */
    public int isDRIIsTXDEN() {
        return info[98];
    }

    /** 
     * <p>Finds whether interface is to use VCP drivers or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if interface is to use VCP drivers.
     */
    public int isAIsVCP8() {
        return info[99];
    }

    /** 
     * <p>Finds whether interface is to use VCP drivers or not.</p>
     * 
     * @return non-zero value if interface is to use VCP drivers.
     */
    public int isBIsVCP8() {
        return info[100];
    }

    /** 
     * <p>Finds whether interface is to use VCP drivers or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if interface is to use VCP drivers.
     */
    public int isCIsVCP8() {
        return info[101];
    }

    /** 
     * <p>Finds whether interface is to use VCP drivers or not.</p>
     * 
     * <p>Mainly applicable for Rev 8 (FT4232H) extensions.</p>
     * 
     * @return non-zero value if interface is to use VCP drivers.
     */
    public int isDIsVCP8() {
        return info[102];
    }


    /* Rev 9 (FT232H) Extensions */


    /** 
     * <p>Finds whether pull down is enabled or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if pull down is enabled.
     */
    public int isPullDownEnableH() {
        return info[103];
    }

    /** 
     * <p>Finds whether serial number is to be used or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if serial number is to be used.
     */
    public int isSerNumEnableH() {
        return info[104];
    }

    /** 
     * <p>Finds whether AC pins have slow slew or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if AC pins have slow slew.
     */
    public int isACSlowSlewH() {
        return info[105];
    }

    /** 
     * <p>Finds whether if AC pins are Schmitt input or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if AC pins are Schmitt input.
     */
    public int isACSchmittInputH() {
        return info[106];
    }

    /** 
     * <p>Retrieves the AC drive current value. Valid values are 4mA, 8mA, 12mA, 16mA.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return AC drive current value.
     */
    public int getACDriveCurrentH() {
        return info[107];
    }

    /** 
     * <p>Finds whether AD pins have slow slew or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if AD pins have slow slew.
     */
    public int isADSlowSlewH() {
        return info[108];
    }

    /** 
     * <p>Finds whether if AD pins are Schmitt input or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if AD pins are Schmitt input.
     */
    public int isADSchmittInputH() {
        return info[109];
    }

    /** 
     * <p>Retrieves the AD drive current value. Valid values are 4mA, 8mA, 12mA, 16mA.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return AD drive current value.
     */
    public int getADDriveCurrentH() {
        return info[110];
    }

    /** 
     * <p>Gives the CBUS 0 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return CBUS 0 configuration value.
     */
    public int getCbus0H() {
        return info[111];
    }

    /** 
     * <p>Gives the CBUS 1 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return CBUS 1 configuration value.
     */
    public int getCbus1H() {
        return info[112];
    }

    /** 
     * <p>Gives the CBUS 2 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return CBUS 2 configuration value.
     */
    public int getCbus2H() {
        return info[113];
    }

    /** 
     * <p>Gives the CBUS 3 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return CBUS 3 configuration value.
     */
    public int getCbus3H() {
        return info[114];
    }

    /** 
     * <p>Gives the CBUS 4 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return CBUS 4 configuration value.
     */
    public int getCbus4H() {
        return info[115];
    }

    /** 
     * <p>Gives the CBUS 5 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return CBUS 5 configuration value.
     */
    public int getCbus5H() {
        return info[116];
    }

    /** 
     * <p>Gives the CBUS 6 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return CBUS 6 configuration value.
     */
    public int getCbus6H() {
        return info[117];
    }

    /** 
     * <p>Gives the CBUS 7 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return CBUS 7 configuration value.
     */
    public int getCbus7H() {
        return info[118];
    }

    /** 
     * <p>Gives the CBUS 8 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return CBUS 8 configuration value.
     */
    public int getCbus8H() {
        return info[119];
    }

    /** 
     * <p>Gives the CBUS 9 configuration value.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return CBUS 9 configuration value.
     */
    public int getCbus9H() {
        return info[120];
    }

    /** 
     * <p>Finds whether interface is 245 FIFO or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if interface is 245 FIFO.
     */
    public int isIsFifoH() {
        return info[121];
    }

    /** 
     * <p>Finds whether interface is 245 FIFO CPU target or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if interface is 245 FIFO CPU target.
     */
    public int isIsFifoTarH() {
        return info[122];
    }

    /** 
     * <p>Finds whether interface is fast serial or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if interface is Fast serial.
     */
    public int isIsFastSerH() {
        return info[123];
    }

    /** 
     * <p>Finds whether interface is 1248 or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if interface is 1248.
     */
    public int isIsFT1248H() {
        return info[124];
    }

    /** 
     * <p>Retrives clock polarity for 1248 device.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return clock polarity - clock idle high (1) or clock idle low (0).
     */
    public int getFT1248CpolH() {
        return info[125];
    }

    /** 
     * <p>Finds whether data for 1248 is LSB or MSB.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return 1 for LSB, 0 for MSB.
     */
    public int isFT1248LsbH() {
        return info[126];
    }

    /** 
     * <p>Finds flow control for FT1248.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return flow control setting for device.
     */
    public int enableFT1248FlowControlH() {
        return info[127];
    }

    /** 
     * <p>Finds whether interface is to use VCP drivers or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if interface is to use VCP drivers.
     */
    public int isIsVCPH() {
        return info[128];
    }

    /** 
     * <p>Finds whether power saving is enabled or not.</p>
     * 
     * <p>Mainly applicable for Rev 9 (FT232H) extensions.</p>
     * 
     * @return non-zero value if using BCBUS7 to save power for self-powered designs.
     */
    public int isPowerSaveEnableH() {
        return info[129];
    }
}
