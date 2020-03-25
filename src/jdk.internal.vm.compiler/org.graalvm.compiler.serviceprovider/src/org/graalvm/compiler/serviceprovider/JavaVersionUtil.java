/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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


package org.graalvm.compiler.serviceprovider;

import jdk.vm.ci.services.Services;

/**
 * Interface to query which JDK version Graal is running on.
 */
public final class JavaVersionUtil {

    private static int getJavaSpecificationVersion() {
        String value = Services.getSavedProperties().get("java.specification.version");
        if (value.startsWith("1.")) {
            value = value.substring(2);
        }
        return Integer.parseInt(value);
    }

    /**
     * The integer value corresponding to the value of the {@code java.specification.version} system
     * property after any leading {@code "1."} has been stripped.
     */
    public static final int JAVA_SPECIFICATION_VERSION = getJavaSpecificationVersion();

    /**
     * Determines if the Java runtime is version 8 or earlier.
     */
    public static final boolean Java8OrEarlier = JAVA_SPECIFICATION_VERSION <= 8;

    /**
     * Determines if the Java runtime is version 11 or earlier.
     */
    public static final boolean Java11OrEarlier = JAVA_SPECIFICATION_VERSION <= 11;

    private JavaVersionUtil() {
    }
}
