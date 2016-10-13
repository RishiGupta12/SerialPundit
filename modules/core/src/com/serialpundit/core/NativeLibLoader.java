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

package com.serialpundit.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/** 
 * <p>Loads given class and native library from top most class loader to prevent native library 
 * from being loaded more than once. This is used mainly in hot deployment of web applications.</p>
 * 
 * @author Rishi Gupta
 */
public class NativeLibLoader {

    private final String cName;
    private final InputStream in;

    /**
     * <p>Constructs and allocate a new NativeLibLoader object with the given details.</p>
     * 
     * <p>This method gives flexibility to the caller by letting the caller place the class to be 
     * loaded at his desired place. For example; the class whose name is represented by className 
     * argument can be inside a jar file. The caller dynamically creates an input stream of this 
     * class by using getResourceAsStream method.</p>
     * 
     * @param className fully qualified class name which will be loaded by root class loader.
     * @throws IllegalArgumentException if className is null or empty, inStream is null.
     */
    public NativeLibLoader(final String className, final InputStream inStream) throws IllegalArgumentException {
        if((className == null) || (className.length() == 0)) {
            throw new IllegalArgumentException("Class name can not be null or empty string !");
        }
        if(inStream == null) {
            throw new IllegalArgumentException("Input stream can not be null !");
        }
        cName = className;
        in = inStream;
    }

    // 1. Same native library can not be loaded using different class loaders at the same time.
    // 2. Same library may be renamed and loaded by different class loaders at the same time. This 
    //    means we can have completely separate instance of same native library linked to different 
    //    instance of class in the same application.
    // 3. Child class loader (application class loader in our case) will be able to see class loaded 
    //    by parent class loader (parent is top most class loader in our case).

    /**
     * <p>Loads the native library represented by 'libToLoad' using the top most class loader.</p>
     * 
     * @param libToLoad absolute path of the native shared library to be loaded.
     * @throws UnsatisfiedLinkError 
     */
    public boolean load(final String libToLoad) throws UnsatisfiedLinkError {

        byte[] classData = null;
        Method defineClass = null;

        // Check if the intended class has been already loaded. If yes then shared native library have been  
        // already loaded and therefore no need to load. Classes are searched upto top most class loader.
        try {
            Class.forName(cName);
            return true;
        } catch (ClassNotFoundException e) {
        }

        try {
            // Find top most class loader. Walk upwards starting from current application class loader so that
            // class loaded by top most loader will be visible to current application's class loader also.
            ClassLoader rcl = null;
            for(ClassLoader cl = getClass().getClassLoader(); cl != null; cl = cl.getParent()) {
                rcl = cl;
            }

            // Extract .class file from file system to byte array. A .class file == byte code + some extra information.
            byte[] buf = new byte[1024];
            int len = 0;
            int totalLength = 0;
            ByteArrayOutputStream op =  new ByteArrayOutputStream();
            while((len = in.read(buf)) != -1) {
                op.write(buf, 0, len);
                totalLength = totalLength + len;
                // prevention from out of memory etc.
                if(totalLength > 102400) {
                    op.close();
                    throw new IOException("Size of .class file can not be greater than 100KB !");
                }
            }
            op.flush();
            classData = op.toByteArray();
            op.close();

            // Create a class loader dynamically to load caller given class.
            Class<?> classLoader = null;
            classLoader = Class.forName("java.lang.ClassLoader");
            defineClass = classLoader.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });
            defineClass.setAccessible(true);

            // Create a new instance of caller given class using reflection and the created class loader. The defineClass 
            // method in general parses the Java byte code format data into a run-time data structure, checks for validity etc.
            defineClass.invoke(rcl, cName, classData, 0, classData.length);

            // Load the caller provided class using root class loader.
            Class<?> loadedClass = null;
            loadedClass = rcl.loadClass(cName);

            // Finally invoke load method of caller provided class to load/link native shared library.
            Method loadMethod = null;
            loadMethod = loadedClass.getDeclaredMethod("load", new Class[] { String.class });
            loadMethod.invoke(null, libToLoad);

        } catch (Exception e) {
            throw (UnsatisfiedLinkError) new UnsatisfiedLinkError("Could not load " + libToLoad).initCause(e);
        } finally {
            defineClass.setAccessible(false); // reset to original access level
        }

        return true;
    }
}
