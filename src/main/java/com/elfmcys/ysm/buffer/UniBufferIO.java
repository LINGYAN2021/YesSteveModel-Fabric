package com.elfmcys.ysm.buffer;

import java.nio.ByteBuffer;

public final class UniBufferIO {
    private UniBufferIO() {
    }

    public static void copy(UniBuffer source, int sourceOffset,
                            UniBuffer target, int targetOffset, int length) {
        checkRange(source.size(), sourceOffset, length);
        checkRange(target.size(), targetOffset, length);
        if (length == 0) {
            return;
        }
        if (source instanceof ArrayBuffer sourceArray) {
            var sourceIndex = sourceArray.arrayOffset() + sourceOffset;
            if (target instanceof ArrayBuffer targetArray) {
                System.arraycopy(sourceArray.array(), sourceIndex,
                        targetArray.array(), targetArray.arrayOffset() + targetOffset, length);
            } else {
                view((NativeBuffer) target, targetOffset, length)
                        .put(sourceArray.array(), sourceIndex, length);
            }
            return;
        }
        var sourceView = view((NativeBuffer) source, sourceOffset, length);
        if (target instanceof ArrayBuffer targetArray) {
            sourceView.get(targetArray.array(), targetArray.arrayOffset() + targetOffset, length);
        } else {
            view((NativeBuffer) target, targetOffset, length).put(sourceView);
        }
    }

    public static boolean equals(UniBuffer source, int sourceOffset,
                                 byte[] target, int targetOffset, int length) {
        checkRange(source.size(), sourceOffset, length);
        checkRange(target.length, targetOffset, length);
        for (var index = 0; index < length; index++) {
            if (get(source, sourceOffset + index) != target[targetOffset + index]) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(UniBuffer left, int leftOffset,
                                 UniBuffer right, int rightOffset, int length) {
        checkRange(left.size(), leftOffset, length);
        checkRange(right.size(), rightOffset, length);
        for (var index = 0; index < length; index++) {
            if (get(left, leftOffset + index) != get(right, rightOffset + index)) {
                return false;
            }
        }
        return true;
    }

    private static byte get(UniBuffer buffer, int index) {
        if (buffer instanceof ArrayBuffer array) {
            return array.array()[array.arrayOffset() + index];
        }
        return ((NativeBuffer) buffer).nio().get(index);
    }

    private static ByteBuffer view(NativeBuffer buffer, int offset, int length) {
        var view = buffer.nio();
        view.position(offset).limit(offset + length);
        return view;
    }

    private static void checkRange(int size, int offset, int length) {
        if (offset < 0 || length < 0 || offset > size - length) {
            throw new IndexOutOfBoundsException();
        }
    }
}
