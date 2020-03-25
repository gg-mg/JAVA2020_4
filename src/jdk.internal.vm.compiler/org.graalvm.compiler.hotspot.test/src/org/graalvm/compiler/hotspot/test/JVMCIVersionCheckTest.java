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


package org.graalvm.compiler.hotspot.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.graalvm.compiler.core.test.GraalCompilerTest;
import org.graalvm.compiler.hotspot.JVMCIVersionCheck;
import org.junit.Assert;
import org.junit.Test;

public class JVMCIVersionCheckTest extends GraalCompilerTest {

    @Test
    public void test01() {
        Properties sprops = System.getProperties();
        Map<String, String> props = new HashMap<>(sprops.size());
        for (String name : sprops.stringPropertyNames()) {
            props.put(name, sprops.getProperty(name));
        }

        for (int i = 0; i < 100; i++) {
            int minMajor = i;
            int minMinor = 100 - i;
            for (int j = 0; j < 100; j++) {
                int major = j;
                int minor = 100 - j;

                boolean ok = (major > minMajor) || (major == minMajor && minor >= minMinor);
                for (String sep : new String[]{".", "-b"}) {
                    String javaVmVersion = String.format("prefix-jvmci-%03d%s%03d-suffix", major, sep, minor);
                    if (ok) {
                        JVMCIVersionCheck.check(props, minMajor, minMinor, "1.8", javaVmVersion, false);
                    } else {
                        try {
                            JVMCIVersionCheck.check(props, minMajor, minMinor, "1.8", javaVmVersion, false);
                            Assert.fail("expected to fail checking " + javaVmVersion + " against " + minMajor + "." + minMinor);
                        } catch (InternalError e) {
                            // pass
                        }
                    }
                }
            }
        }

        // Test handling of version components bigger than Integer.MAX_VALUE
        for (String sep : new String[]{".", "-b"}) {
            for (String version : new String[]{"0" + sep + Long.MAX_VALUE, Long.MAX_VALUE + sep + 0}) {
                String javaVmVersion = String.format("prefix-jvmci-%s-suffix", version);
                try {
                    JVMCIVersionCheck.check(props, 0, 59, "1.8", javaVmVersion, false);
                    Assert.fail("expected to fail checking " + javaVmVersion + " against 0.59");
                } catch (InternalError e) {
                    // pass
                }
            }
        }
    }
}
