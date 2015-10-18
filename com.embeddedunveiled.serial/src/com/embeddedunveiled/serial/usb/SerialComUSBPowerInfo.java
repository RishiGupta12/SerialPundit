/*
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
 */

package com.embeddedunveiled.serial.usb;

/**
 * <p>Encapsulates various power related information about a USB device.</p>
 * 
 * @author Rishi Gupta
 */
public final class SerialComUSBPowerInfo {

	private String autoSuspendDelay;
	private String currentControlConfig;
	private String currentRuntimeStatus;

	/**
	 * <p>Construct and allocates a new SerialComUSBPowerInfo object with the given details.</p>
	 * 
	 * @param autoSuspendDelay delay in milliseconds before USB device is suspended after inactivity.
	 * @param currentControlConfig value indicating whether device is to be kept on always or is allowed 
	 *         to suspend.
	 * @param currentRuntimeStatus current power status (on or suspended) of this USB device.
	 */
	public SerialComUSBPowerInfo(String autoSuspendDelay, String currentControlConfig, String currentRuntimeStatus) {
		this.autoSuspendDelay = autoSuspendDelay;
		this.currentControlConfig = currentControlConfig;
		this.currentRuntimeStatus = currentRuntimeStatus;
	}

	/** 
	 * <p>Retrieves time after which this USB device will be allowed to auto suspend if idle.</p>
	 * 
	 * @return time in milliseconds as string.
	 */
	String getAutoSuspendDelay() {
		return autoSuspendDelay;
	}

	/** 
	 * <p>Retrieves value indicating whether this USB device will be prevented from auto suspending or 
	 * is allowed to auto suspend.</p>
	 * 
	 * @return current control setting string.
	 */
	String getCurrentControlConfig() {
		return currentControlConfig;
	}

	/** 
	 * <p>Retrieves value indicating whether this USB device is active or auto suspended.</p>
	 * 
	 * @return current power status string.
	 */
	String getCurrentRuntimeStatus() {
		return currentRuntimeStatus;
	}

	/** 
	 * <p>Prints information about power management of this USB device on console.</p>
	 */
	public void dumpDevicePowerInfo() {
		System.out.println(
				"Auto suspend delay (ms) : " + autoSuspendDelay + 
				"Control configuration : " + currentControlConfig + 
				"Current power status : " + currentRuntimeStatus);
	}
}
