package com.github.pelmenstar1.complexRangeModel.bits

private const val WORD_MASK = -1L

internal fun startMask(startIndex: Int) = WORD_MASK shl startIndex
internal fun endMask(endIndex: Int) = WORD_MASK ushr (-endIndex - 1)

internal fun rangeMask(startIndex: Int, endIndex: Int): Long {
    return startMask(startIndex) and endMask(endIndex)
}