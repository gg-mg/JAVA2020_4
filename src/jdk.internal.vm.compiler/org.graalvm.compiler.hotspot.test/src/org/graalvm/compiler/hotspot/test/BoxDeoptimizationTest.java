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
 *
 *
 */
package org.graalvm.compiler.hotspot.test;

import org.graalvm.compiler.api.directives.GraalDirectives;
import org.graalvm.compiler.core.test.GraalCompilerTest;
import org.graalvm.compiler.serviceprovider.JavaVersionUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class BoxDeoptimizationTest extends GraalCompilerTest {
    private static boolean isJDK13OrLater = JavaVersionUtil.JAVA_SPECIFICATION_VERSION >= 13;

    public static void testInteger() {
        Object[] values = {42, new Exception()};
        GraalDirectives.deoptimize();
        Assert.assertSame(values[0], Integer.valueOf(42));
    }

    @Test
    public void test1() {
        Assume.assumeTrue(isJDK13OrLater);
        test("testInteger");
    }

    public static void testLong() {
        Object[] values = {42L, new Exception()};
        GraalDirectives.deoptimize();
        Assert.assertSame(values[0], Long.valueOf(42));
    }

    @Test
    public void test2() {
        Assume.assumeTrue(isJDK13OrLater);
        test("testLong");
    }

    public static void testChar() {
        Object[] values = {'a', new Exception()};
        GraalDirectives.deoptimize();
        Assert.assertSame(values[0], Character.valueOf('a'));
    }

    @Test
    public void test3() {
        Assume.assumeTrue(isJDK13OrLater);
        test("testChar");
    }

    public static void testShort() {
        Object[] values = {(short) 42, new Exception()};
        GraalDirectives.deoptimize();
        Assert.assertSame(values[0], Short.valueOf((short) 42));
    }

    @Test
    public void test4() {
        Assume.assumeTrue(isJDK13OrLater);
        test("testShort");
    }

    public static void testByte() {
        Object[] values = {(byte) 42, new Exception()};
        GraalDirectives.deoptimize();
        Assert.assertSame(values[0], Byte.valueOf((byte) 42));
    }

    @Test
    public void test5() {
        Assume.assumeTrue(isJDK13OrLater);
        test("testByte");
    }

    public static void testBoolean() {
        Object[] values = {true, new Exception()};
        GraalDirectives.deoptimize();
        Assert.assertSame(values[0], Boolean.valueOf(true));
    }

    @Test
    public void test6() {
        Assume.assumeTrue(isJDK13OrLater);
        test("testBoolean");
    }
}
