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

package com.serialpundit.usb;

import com.serialpundit.core.util.SerialComUtil;

/**
 * <p>Encapsulates various power related information about a USB device.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComUSBPowerInfo {

    private String autoSuspendDelay;
    private String currentControlConfig;
    private String currentRuntimeStatus;
    private String isSelectiveSuspendSupported;
    private String isSelectiveSuspendEnabled;
    private String selectiveSuspendTimeoutValue;

    /**
     * <p>Construct and allocates a new SerialComUSBPowerInfo object with the given details.</p>
     * 
     * @param autoSuspendDelay delay in milliseconds before USB device is suspended after inactivity.
     * @param currentControlConfig value indicating whether device is to be kept on always or is allowed 
     *         to suspend.
     * @param currentRuntimeStatus current power status (on or suspended) of this USB device.
     * @param isSelectiveSuspendSupported string indicating window's selective suspend supported or not.
     * @param isSelectiveSuspendEnabled string indicating window's selective suspend is enabled or not.
     * @param selectiveSuspendTimeoutValue string indicating window's selective suspend timeout value.
     */
    public SerialComUSBPowerInfo(String autoSuspendDelay, String currentControlConfig, String currentRuntimeStatus,
            String isSelectiveSuspendSupported, String isSelectiveSuspendEnabled, String selectiveSuspendTimeoutValue) {
        this.autoSuspendDelay = autoSuspendDelay;
        this.currentControlConfig = currentControlConfig;
        this.currentRuntimeStatus = currentRuntimeStatus;
        this.isSelectiveSuspendSupported = isSelectiveSuspendSupported;
        this.isSelectiveSuspendEnabled = isSelectiveSuspendEnabled;
        this.selectiveSuspendTimeoutValue = selectiveSuspendTimeoutValue;
    }

    /** 
     * <p>Retrieves time after which this USB device will be allowed to auto suspend if idle.</p>
     * 
     * @return time in milliseconds as string.
     */
    public String getAutoSuspendDelay() {
        return autoSuspendDelay;
    }

    /** 
     * <p>Retrieves value indicating whether this USB device will be prevented from auto suspending or 
     * is allowed to auto suspend.</p>
     * 
     * @return current control setting string.
     */
    public String getCurrentControlConfig() {
        return currentControlConfig;
    }

    /** 
     * <p>Retrieves value indicating whether this USB device is active or auto suspended.</p>
     * 
     * @return current power status string.
     */
    public String getCurrentRuntimeStatus() {
        return currentRuntimeStatus;
    }

    /** 
     * <p>Tells whether device support selective suspend or not.</p>
     * 
     * @return true if selective suspend supported otherwise false.
     */
    public Boolean isSelectiveSuspendSupported() {
        if(isSelectiveSuspendSupported.equals("true")) {
            return true;
        }
        return false;
    }

    /** 
     * <p>Tells whether selective suspend is enabled or not.</p>
     * 
     * @return true if selective suspend is enabled otherwise false.
     */
    public Boolean isSelectiveSuspendEnabled() {
        if(isSelectiveSuspendEnabled.equals("true")) {
            return true;
        }
        return false;
    }

    /** 
     * <p>Gives current selective suspend timeout value for this device.</p>
     * 
     * @return timeout value.
     * @throws NumberFormatException if the timeout value hex string can not be converted into 
     *         numerical representation.
     */
    public int getSelectiveSuspendTimeout() {
        if("---".equals(selectiveSuspendTimeoutValue)) {
            return 0;
        }
        return (int) SerialComUtil.hexStrToLongNumber(selectiveSuspendTimeoutValue);
    }

    /** 
     * <p>Prints information about power management of this USB device on console.</p>
     */
    public void dumpDevicePowerInfo() {
        System.out.println(
                "Auto suspend delay (ms) : " + autoSuspendDelay + 
                "\nControl configuration : " + currentControlConfig + 
                "\nCurrent power status : " + currentRuntimeStatus + 
                "\nIs selective suspend supported : " + isSelectiveSuspendSupported +
                "\nIs selective suspend enabled : " + isSelectiveSuspendEnabled +
                "\nSelective suspend timeout value : " + selectiveSuspendTimeoutValue);
    }
}
