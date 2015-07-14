/**
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

package com.embeddedunveiled.serial;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

/** 
 * <p>This class provide java system properties to the callers in a unified way. </p>
 */
public final class SerialComSystemProperty {
	
	SecurityManager securityManager = null;
	
	/** 
	 * <p>Allocates a new SerialComSystemProperty object.</p>
	 */
	public SerialComSystemProperty() {
		securityManager = System.getSecurityManager();
	}
	
	/** <p>Identifies operating system this library is running on.</p>
	 * @return operating system identified
	 * @throws SecurityException if security manager does not allow access to system property
	 */
	String getOSName() throws SecurityException {
		if(securityManager == null) {
			return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).trim();
		}else {
			return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
				public String run() {
					return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).trim();
				}
			});
		}
	}
	
	/** <p>Identifies CPU architecture of the platform in use.</p>
	 * @return architecture of processor
	 * @throws SecurityException if security manager does not allow access to system property
	 */
	String getOSArch() throws SecurityException {
		if(securityManager == null) {
			return System.getProperty("os.arch").toLowerCase(Locale.ENGLISH).trim();
		}else {
			return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
				public String run() {
					return System.getProperty("os.arch").toLowerCase(Locale.ENGLISH).trim();
				}
			});
		}
	}
	
	/** <p>Gives the vendor of java virtual machine in use.</p>
	 * @return vendor of JVM
	 * @throws SecurityException if security manager does not allow access to system property
	 */
	String getJavaVmVendor() throws SecurityException {
		if(securityManager == null) {
			return System.getProperty("java.vm.vendor").toLowerCase(Locale.ENGLISH).trim();
		}else {
			return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
				public String run() {
					return System.getProperty("java.vm.vendor").toLowerCase(Locale.ENGLISH).trim();
				}
			});
		}
	}
	
	/** <p>Locate home directory for Java.</p>
	 * @return home directory for Java stuff
	 * @throws SecurityException if security manager does not allow access to system property
	 */
	String getJavaHome() throws SecurityException {
		if(securityManager == null) {
			return System.getProperty("java.home");
		}else {
			return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
				public String run() {
					return System.getProperty("java.home");
				}
			});
		}
	}
	
	/** <p>Gives system/user temp directory as returned by JVM.</p>
	 * @return tmp directory for Java operations
	 * @throws SecurityException if security manager does not allow access to system property
	 */
	String getJavaIOTmpDir() throws SecurityException {
		if(securityManager == null) {
			return System.getProperty("java.io.tmpdir");
		}else {
			return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
				public String run() {
					return System.getProperty("java.io.tmpdir");
				}
			});
		}
	}
	
	/** <p>Gives home directory of the user currently associated with this process.</p>
	 * @return home directory of current user
	 * @throws SecurityException if security manager does not allow access to system property
	 */
	String getUserHome() throws SecurityException {
		if(securityManager == null) {
			return System.getProperty("user.home");
		}else {
			return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
				public String run() {
					return System.getProperty("user.home");
				}
			});
		}
	}
	
	/** <p>Gives platform specific file separator.</p>
	 * @return platform specific file separator
	 * @throws SecurityException if security manager does not allow access to system property
	 */
	String getfileSeparator() throws SecurityException {
		if(securityManager == null) {
			return System.getProperty("file.separator");
		}else {
			return (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
				public String run() {
					return System.getProperty("file.separator");
				}
			});
		}
	}
	
}
