/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */


package org.graalvm.compiler.core.test;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

import org.objectweb.asm.Opcodes;

import sun.misc.Unsafe;

public abstract class CustomizedBytecodePatternTest extends GraalCompilerTest implements Opcodes {

    protected Class<?> getClass(String className) throws ClassNotFoundException {
        return new CachedLoader(CustomizedBytecodePatternTest.class.getClassLoader(), className).findClass(className);
    }

    /**
     * @param className
     * @param lookUp lookup object with boot class load capability (required for jdk 9 and above)
     * @return loaded class
     * @throws ClassNotFoundException
     */
    protected Class<?> getClassBL(String className, MethodHandles.Lookup lookUp) throws ClassNotFoundException {
        byte[] gen = generateClass(className.replace('.', '/'));
        Method defineClass = null;
        Class<?> loadedClass = null;
        try {
            if (Java8OrEarlier) {
                defineClass = Unsafe.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
                loadedClass = (Class<?>) defineClass.invoke(UNSAFE, className, gen, 0, gen.length, null, null);
            } else {
                defineClass = MethodHandles.lookup().getClass().getDeclaredMethod("defineClass", byte[].class);
                loadedClass = (Class<?>) defineClass.invoke(lookUp, gen);
            }
        } catch (Exception e) {
            throw new ClassNotFoundException();
        }
        return loadedClass;
    }

    private class CachedLoader extends ClassLoader {

        final String className;
        Class<?> loaded;

        CachedLoader(ClassLoader parent, String className) {
            super(parent);
            this.className = className;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.equals(className)) {
                if (loaded == null) {
                    byte[] gen = generateClass(name.replace('.', '/'));
                    loaded = defineClass(name, gen, 0, gen.length);
                }
                return loaded;
            } else {
                return super.findClass(name);
            }
        }

    }

    protected abstract byte[] generateClass(String internalClassName);

}
