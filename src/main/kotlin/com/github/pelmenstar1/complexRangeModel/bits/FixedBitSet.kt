package com.github.pelmenstar1.complexRangeModel.bits

internal class FixedBitSet {
    private val words: LongArray

    constructor(minimumBitCount: Int) {
        words = LongArray((minimumBitCount + 63) / 64)
    }

    internal constructor(words: LongArray) {
        this.words = words
    }

    fun set(index: Int) {
        setMask(getWordIndex(index), mask = 1L shl index)
    }

    fun setMask(wordIndex: Int, mask: Long) {
        words[wordIndex] = words[wordIndex] or mask
    }

    fun set(startIndex: Int, endIndex: Int) {
        val startWordIndex = getWordIndex(startIndex)
        val endWordIndex = getWordIndex(endIndex)

        if (startWordIndex == endWordIndex) {
            val mask = rangeMask(getInWordBitIndex(startIndex), getInWordBitIndex(endIndex))

            setMask(startWordIndex, mask)
        } else {
            val startMask = WORD_MASK shl startIndex
            val endMask = WORD_MASK ushr (-endIndex - 1)

            setMask(startWordIndex, startMask)
            setMask(endWordIndex, endMask)

            for (i in (startWordIndex + 1) until endWordIndex) {
                words[i] = WORD_MASK
            }
        }
    }

    fun findNextSetBitIndex(startIndex: Int): Int {
        var wordIndex = getWordIndex(startIndex)
        if (wordIndex >= words.size) {
            return -1
        }

        var word = words[wordIndex] and (WORD_MASK shl startIndex)

        while (true) {
            if (word != 0L) {
                return wordIndex * WORD_BIT_COUNT + word.countTrailingZeroBits()
            }

            wordIndex++
            if (wordIndex == words.size) return -1

            word = words[wordIndex]
        }
    }

    fun findNextUnsetBitIndex(startIndex: Int): Int {
        var wordIndex = getWordIndex(startIndex)
        if (wordIndex >= words.size) {
            return -1
        }

        var word = words[wordIndex].inv() and (WORD_MASK shl startIndex)

        while (true) {
            if (word != 0L) {
                return wordIndex * 64 + word.countTrailingZeroBits()
            }

            wordIndex++
            if (wordIndex == words.size) return -1

            word = words[wordIndex].inv()
        }
    }

    inline fun forEachRange(block: (start: Int, endInclusive: Int) -> Unit) {
        var start = 0

        while(true) {
            val rangeStart = findNextSetBitIndex(start)
            if (rangeStart < 0) {
                break
            }

            var rangeEnd = findNextUnsetBitIndex(rangeStart)
            if (rangeEnd < 0) {
                rangeEnd = words.size * WORD_BIT_COUNT
            }
            rangeEnd--

            start = rangeEnd + 1
            block(rangeStart, rangeEnd)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is FixedBitSet && words.contentEquals(other.words)
    }

    override fun hashCode(): Int {
        return words.contentHashCode()
    }

    companion object {
        private const val WORD_SHIFT = 6
        private const val WORD_BIT_COUNT = 1 shl WORD_SHIFT
        private const val WORD_MASK = -1L

        private fun getWordIndex(bitIndex: Int) = bitIndex / 64
        private fun getInWordBitIndex(bitIndex: Int) = bitIndex % 64
    }
}