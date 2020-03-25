/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019, Red Hat Inc. All rights reserved.
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


package org.graalvm.compiler.hotspot.gc.shared;

import org.graalvm.compiler.hotspot.GraalHotSpotVMConfig;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.extended.ArrayRangeWrite;
import org.graalvm.compiler.nodes.java.AbstractCompareAndSwapNode;
import org.graalvm.compiler.nodes.java.LoweredAtomicReadAndWriteNode;
import org.graalvm.compiler.nodes.memory.ReadNode;
import org.graalvm.compiler.nodes.memory.WriteNode;

public abstract class BarrierSet {
    private final GraalHotSpotVMConfig vmConfig;

    protected BarrierSet(GraalHotSpotVMConfig vmConfig) {
        this.vmConfig = vmConfig;
    }

    public final GraalHotSpotVMConfig getVMConfig() {
        return vmConfig;
    }

    public abstract void addReadNodeBarriers(ReadNode node, StructuredGraph graph);

    public abstract void addWriteNodeBarriers(WriteNode node, StructuredGraph graph);

    public abstract void addAtomicReadWriteNodeBarriers(LoweredAtomicReadAndWriteNode node, StructuredGraph graph);

    public abstract void addCASBarriers(AbstractCompareAndSwapNode node, StructuredGraph graph);

    public abstract void addArrayRangeBarriers(ArrayRangeWrite write, StructuredGraph graph);
}
