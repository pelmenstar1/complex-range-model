package com.github.pelmenstar1.complexRangeModel.bits

internal fun nBitsSet(n: Int): Long {
    return if (n == 64) -1L else (1L shl n) - 1
}

internal fun rangeMask(start: Int, endInclusive: Int): Long {
    return nBitsSet(endInclusive - start + 1) shl start
}

internal fun findNextSetIndex(bits: LongArray, startIndex: Int): Int {
    var wordIndex = startIndex / 64
    if (wordIndex >= bits.size) {
        return -1
    }

    var word = bits[wordIndex] and ((-1L) shl startIndex)

    while (true) {
        if (word != 0L) {
            return wordIndex * 64 + word.countTrailingZeroBits()
        }

        wordIndex++
        if (wordIndex == bits.size) return -1

        word = bits[wordIndex]
    }
}

internal fun findNextUnsetIndex(bits: LongArray, startIndex: Int): Int {
    var wordIndex = startIndex / 64
    if (wordIndex >= bits.size) {
        return -1
    }

    var word = bits[wordIndex].inv() and ((-1L) shl startIndex)

    while (true) {
        if (word != 0L) {
            return wordIndex * 64 + word.countTrailingZeroBits()
        }

        wordIndex++
        if (wordIndex == bits.size) return -1

        word = bits[wordIndex].inv()
    }
}