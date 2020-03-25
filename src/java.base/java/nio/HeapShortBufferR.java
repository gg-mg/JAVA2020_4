/*
 * Copyright (c) 2000, 2019, Oracle and/or its affiliates. All rights reserved.
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

// -- This file was mechanically generated: Do not edit! -- //

package java.nio;

import java.util.Objects;

/**



 * A read-only HeapShortBuffer.  This class extends the corresponding
 * read/write class, overriding the mutation methods to throw a {@link
 * ReadOnlyBufferException} and overriding the view-buffer methods to return an
 * instance of this class rather than of the superclass.

 */

class HeapShortBufferR
    extends HeapShortBuffer
{
    // Cached array base offset
    private static final long ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(short[].class);

    // Cached array base offset
    private static final long ARRAY_INDEX_SCALE = UNSAFE.arrayIndexScale(short[].class);

    // For speed these fields are actually declared in X-Buffer;
    // these declarations are here as documentation
    /*




    */

    HeapShortBufferR(int cap, int lim) {            // package-private








        super(cap, lim);
        this.isReadOnly = true;

    }

    HeapShortBufferR(short[] buf, int off, int len) { // package-private








        super(buf, off, len);
        this.isReadOnly = true;

    }

    protected HeapShortBufferR(short[] buf,
                                   int mark, int pos, int lim, int cap,
                                   int off)
    {








        super(buf, mark, pos, lim, cap, off);
        this.isReadOnly = true;

    }

    public ShortBuffer slice() {
        int rem = this.remaining();
        return new HeapShortBufferR(hb,
                                        -1,
                                        0,
                                        rem,
                                        rem,
                                        this.position() + offset);
    }

    @Override
    public ShortBuffer slice(int index, int length) {
        Objects.checkFromIndexSize(index, length, limit());
        return new HeapShortBufferR(hb,
                                        -1,
                                        0,
                                        length,
                                        length,
                                        index + offset);
    }

    public ShortBuffer duplicate() {
        return new HeapShortBufferR(hb,
                                        this.markValue(),
                                        this.position(),
                                        this.limit(),
                                        this.capacity(),
                                        offset);
    }

    public ShortBuffer asReadOnlyBuffer() {








        return duplicate();

    }


















































    public boolean isReadOnly() {
        return true;
    }

    public ShortBuffer put(short x) {




        throw new ReadOnlyBufferException();

    }

    public ShortBuffer put(int i, short x) {




        throw new ReadOnlyBufferException();

    }

    public ShortBuffer put(short[] src, int offset, int length) {









        throw new ReadOnlyBufferException();

    }

    public ShortBuffer put(ShortBuffer src) {


























        throw new ReadOnlyBufferException();

    }

    public ShortBuffer put(int index, short[] src, int offset, int length) {






        throw new ReadOnlyBufferException();

    }




















    public ShortBuffer compact() {









        throw new ReadOnlyBufferException();

    }

















































































































































































































































































































































































    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }







}
