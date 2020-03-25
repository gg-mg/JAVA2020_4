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


package org.graalvm.compiler.hotspot.phases;

import org.graalvm.compiler.debug.DebugCloseable;
import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.hotspot.GraalHotSpotVMConfig;
import org.graalvm.compiler.hotspot.gc.g1.G1BarrierSet;
import org.graalvm.compiler.hotspot.gc.shared.BarrierSet;
import org.graalvm.compiler.hotspot.gc.shared.CardTableBarrierSet;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.extended.ArrayRangeWrite;
import org.graalvm.compiler.nodes.java.AbstractCompareAndSwapNode;
import org.graalvm.compiler.nodes.java.LoweredAtomicReadAndWriteNode;
import org.graalvm.compiler.nodes.memory.ReadNode;
import org.graalvm.compiler.nodes.memory.WriteNode;
import org.graalvm.compiler.phases.Phase;

public class WriteBarrierAdditionPhase extends Phase {

    private BarrierSet barrierSet;

    public WriteBarrierAdditionPhase(GraalHotSpotVMConfig config) {
        this.barrierSet = createBarrierSet(config);
    }

    @SuppressWarnings("try")
    @Override
    protected void run(StructuredGraph graph) {
        for (Node n : graph.getNodes()) {
            try (DebugCloseable scope = n.graph().withNodeSourcePosition(n)) {
                if (n instanceof ReadNode) {
                    barrierSet.addReadNodeBarriers((ReadNode) n, graph);
                } else if (n instanceof WriteNode) {
                    barrierSet.addWriteNodeBarriers((WriteNode) n, graph);
                } else if (n instanceof LoweredAtomicReadAndWriteNode) {
                    LoweredAtomicReadAndWriteNode loweredAtomicReadAndWriteNode = (LoweredAtomicReadAndWriteNode) n;
                    barrierSet.addAtomicReadWriteNodeBarriers(loweredAtomicReadAndWriteNode, graph);
                } else if (n instanceof AbstractCompareAndSwapNode) {
                    barrierSet.addCASBarriers((AbstractCompareAndSwapNode) n, graph);
                } else if (n instanceof ArrayRangeWrite) {
                    ArrayRangeWrite node = (ArrayRangeWrite) n;
                    if (node.writesObjectArray()) {
                        barrierSet.addArrayRangeBarriers(node, graph);
                    }
                }
            }
        }
    }

    @Override
    public boolean checkContract() {
        return false;
    }

    private BarrierSet createBarrierSet(GraalHotSpotVMConfig config) {
        if (config.useG1GC) {
            return createG1BarrierSet(config);
        } else {
            return createCardTableBarrierSet(config);
        }
    }

    protected BarrierSet createCardTableBarrierSet(GraalHotSpotVMConfig config) {
        return new CardTableBarrierSet(config);
    }

    protected BarrierSet createG1BarrierSet(GraalHotSpotVMConfig config) {
        return new G1BarrierSet(config);
    }
}
