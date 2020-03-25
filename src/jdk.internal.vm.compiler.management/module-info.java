/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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
 *
 *
 */

/**
 * Registers Graal Compiler specific management interfaces for the JVM.
 *
 * @moduleGraph
 * @since 10
 */
module jdk.internal.vm.compiler.management {
    // source file: file:///t:/workspace/open/src/jdk.internal.vm.compiler.management/share/classes/module-info.java
    //              file:///t:/workspace/build/windows-x64/support/gensrc/jdk.internal.vm.compiler.management/module-info.java.extra
    requires java.management;
    requires jdk.management;
    requires jdk.internal.vm.ci;
    requires jdk.internal.vm.compiler;

    provides org.graalvm.compiler.hotspot.HotSpotGraalManagementRegistration with org.graalvm.compiler.hotspot.management.HotSpotGraalManagement;
    provides org.graalvm.compiler.serviceprovider.JMXService with org.graalvm.compiler.hotspot.management.JMXServiceProvider;
}
