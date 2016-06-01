/*
 * This file is part of SerialPundit project and software.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.embeddedunveiled.serial.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

/** 
 * <p>Provides Java system properties to the callers in a unified way.</p>
 * 
 * @author Rishi Gupta
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
     * 
     * @return operating system identified.
     * @throws SecurityException if security manager does not allow access to system property.
     */
    public String getOSName() throws SecurityException {
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

    /** <p>Identifies operating system architecture (not of JRE).</p>
     * 
     * @return architecture of processor.
     * @throws SecurityException if security manager does not allow access to system property.
     */
    public String getOSArch() throws SecurityException {
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
     * 
     * @return vendor of JVM.
     * @throws SecurityException if security manager does not allow access to system property.
     */
    public String getJavaVmVendor() throws SecurityException {
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
     * 
     * @return home directory for Java stuff.
     * @throws SecurityException if security manager does not allow access to system property.
     */
    public String getJavaHome() throws SecurityException {
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
     * 
     * @return tmp directory for Java operations.
     * @throws SecurityException if security manager does not allow access to system property.
     */
    public String getJavaIOTmpDir() throws SecurityException {
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
     * 
     * @return home directory of current user.
     * @throws SecurityException if security manager does not allow access to system property.
     */
    public String getUserHome() throws SecurityException {
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
     * 
     * @return platform specific file separator.
     * @throws SecurityException if security manager does not allow access to system property.
     */
    public String getfileSeparator() throws SecurityException {
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
