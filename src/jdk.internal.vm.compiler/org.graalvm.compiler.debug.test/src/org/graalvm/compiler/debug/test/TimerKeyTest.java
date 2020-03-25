/*
 * Copyright (c) 2013, 2018, Oracle and/or its affiliates. All rights reserved.
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


package org.graalvm.compiler.debug.test;

import static org.graalvm.compiler.debug.DebugContext.DEFAULT_LOG_STREAM;
import static org.graalvm.compiler.debug.DebugContext.NO_CONFIG_CUSTOMIZERS;
import static org.graalvm.compiler.debug.DebugContext.NO_DESCRIPTION;
import static org.graalvm.compiler.debug.DebugContext.NO_GLOBAL_METRIC_VALUES;
import static org.junit.Assert.assertEquals;

import jdk.internal.vm.compiler.collections.EconomicMap;
import org.graalvm.compiler.debug.DebugCloseable;
import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.debug.DebugOptions;
import org.graalvm.compiler.debug.TimerKey;
import org.graalvm.compiler.options.OptionKey;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.serviceprovider.GraalServices;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("try")
public class TimerKeyTest {

    @Before
    public void checkCapabilities() {
        assumeManagementLibraryIsLoadable();
        Assume.assumeTrue("skipping management interface test", GraalServices.isCurrentThreadCpuTimeSupported());
    }

    /** @see <a href="https://bugs.openjdk.java.net/browse/JDK-8076557">JDK-8076557</a> */
    static void assumeManagementLibraryIsLoadable() {
        try {
            /* Trigger loading of the management library using the bootstrap class loader. */
            GraalServices.getCurrentThreadAllocatedBytes();
        } catch (UnsatisfiedLinkError | NoClassDefFoundError | UnsupportedOperationException e) {
            throw new AssumptionViolatedException("Management interface is unavailable: " + e);
        }
    }

    /**
     * Actively spins the current thread for at least a given number of milliseconds in such a way
     * that timers for the current thread keep ticking over.
     *
     * @return the number of milliseconds actually spent spinning which is guaranteed to be >=
     *         {@code ms}
     */
    private static long spin(long ms) {
        long start = GraalServices.getCurrentThreadCpuTime();
        do {
            long durationMS = (GraalServices.getCurrentThreadCpuTime() - start) / 1000;
            if (durationMS >= ms) {
                return durationMS;
            }
        } while (true);
    }

    /**
     * Asserts that a timer replied recursively without any other interleaving timers has the same
     * flat and accumulated times.
     */
    @Test
    public void test2() {
        EconomicMap<OptionKey<?>, Object> map = EconomicMap.create();
        map.put(DebugOptions.Time, "");
        OptionValues options = new OptionValues(map);
        DebugContext debug = DebugContext.create(options, NO_DESCRIPTION, NO_GLOBAL_METRIC_VALUES, DEFAULT_LOG_STREAM, NO_CONFIG_CUSTOMIZERS);

        TimerKey timerC = DebugContext.timer("TimerC");
        try (DebugCloseable c1 = timerC.start(debug)) {
            spin(50);
            try (DebugCloseable c2 = timerC.start(debug)) {
                spin(50);
                try (DebugCloseable c3 = timerC.start(debug)) {
                    spin(50);
                    try (DebugCloseable c4 = timerC.start(debug)) {
                        spin(50);
                        try (DebugCloseable c5 = timerC.start(debug)) {
                            spin(50);
                        }
                    }
                }
            }
        }
        if (timerC.getFlat() != null) {
            assertEquals(timerC.getFlat().getCurrentValue(debug), timerC.getCurrentValue(debug));
        }
    }
}
