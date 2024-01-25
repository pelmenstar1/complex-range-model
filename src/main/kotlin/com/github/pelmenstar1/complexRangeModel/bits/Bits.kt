package com.github.pelmenstar1.complexRangeModel.bits

internal fun nBitsSet(n: Int): Long {
    return if (n == 64) -1L else (1L shl n) - 1
}

internal fun rangeMask(start: Int, endInclusive: Int): Long {
    return nBitsSet(endInclusive - start + 1) shl start
}