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

package com.sun.source.tree;

/**
 * A tree node for a {@code yield} statement.
 *
 * For example:
 * <pre>
 *   yield <em>expression</em> ;
 * </pre>
 *
 * @jls section TODO
 *
 * @since 13
 *
 * @deprecated This class is modeling yield from switch expressions,
 * which are part of a preview feature and may be removed if
 * the preview feature is removed.
 */
@Deprecated(forRemoval=true, since="13")
public interface YieldTree extends StatementTree {

    /**
     * Returns the expression for this {@code yield} statement.
     *
     * @return the expression
     */
    ExpressionTree getValue();
}
