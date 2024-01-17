package com.github.pelmenstar1.complexRangeModel.bits

class BitArrayRangeSet {
    private val words: LongArray
    private val limitStart: Int
    private val limitEnd: Int

    val size: Int
        get() {
            var count = 0
            forEachRange { _, _ ->
                count++
            }

            return count
        }

    constructor(limitStart: Int, limitEnd: Int) {
        require(limitStart <= limitEnd) {
            "limitStart can't be greater than limitEnd"
        }

        this.limitStart = limitStart
        this.limitEnd = limitEnd

        val wordCount = (limitEnd - limitStart + 63) / 64
        words = LongArray(wordCount)
    }

    constructor(limitStart: Int, limitEnd: Int, words: LongArray) {
        this.limitStart = limitStart
        this.limitEnd = limitEnd
        this.words = words
    }

    fun isEmpty(): Boolean {
        // Unused bits in 'words' must be all 0. So we can simply check if all elements in 'words' are zero.
        return words.all { it == 0L }
    }

    fun contains(start: Int, endInclusive: Int): Boolean {
        if (isValidRange(start, endInclusive)) {
            forEachRange { s, e ->
                if (start == s && endInclusive == e) {
                    return true
                }
            }
        }

        return false
    }

    operator fun get(index: Int): PackedIntRange {
        if (index < 0) {
            throw IndexOutOfBoundsException()
        }

        var count = 0

        forEachRange { start, endInclusive ->
            if (count == index) {
                return PackedIntRange(start, endInclusive)
            }

            count++
        }

        throw IndexOutOfBoundsException()
    }

    fun add(start: Int, endInclusive: Int) {
        ensureValidRange(start, endInclusive)

        val startBitIndex = start - limitStart
        val endBitIndex = endInclusive - limitStart

        val startWordIndex = getWordIndex(startBitIndex)
        val endWordIndex = getWordIndex(endBitIndex)

        if (startWordIndex == endWordIndex) {
            val mask = rangeMask(getInWordBitIndex(startBitIndex), getInWordBitIndex(endBitIndex))

            words[startWordIndex] = words[startWordIndex] or mask
        } else {
            val firstMask = WORD_MASK shl startBitIndex
            val lastMask = WORD_MASK ushr (-endBitIndex - 1)

            words[startWordIndex] = words[startWordIndex] or firstMask
            words[endWordIndex] = words[endWordIndex] or lastMask

            for (i in (startWordIndex + 1) until endWordIndex) {
                words[i] = WORD_MASK
            }
        }
    }

    fun remove(start: Int, endInclusive: Int) {
        ensureValidRange(start, endInclusive)

        val startBitIndex = start - limitStart
        val endBitIndex = endInclusive - limitStart

        val startWordIndex = getWordIndex(startBitIndex)
        val endWordIndex = getWordIndex(endBitIndex)

        if (startWordIndex == endWordIndex) {
            val mask = rangeMask(getInWordBitIndex(startBitIndex), getInWordBitIndex(endBitIndex))

            words[startWordIndex] = words[startWordIndex] and mask.inv()
        } else {
            val firstMask = WORD_MASK shl startBitIndex
            val lastMask = WORD_MASK ushr (-endBitIndex - 1)

            words[startWordIndex] = words[startWordIndex] and firstMask.inv()
            words[endWordIndex] = words[endWordIndex] and lastMask.inv()

            for (i in (startWordIndex + 1) until endWordIndex) {
                words[i] = 0
            }
        }
    }

    private fun isValidRange(start: Int, endInclusive: Int): Boolean {
        return start >= limitStart && endInclusive <= limitEnd
    }

    private fun ensureValidRange(start: Int, endInclusive: Int) {
        if (!isValidRange(start, endInclusive)) {
            throw IllegalArgumentException("Specified range is out of the limiting range")
        }
    }

    internal inline fun forEachRange(action: (start: Int, endInclusive: Int) -> Unit) {
        var startIndex = 0

        while (true) {
            val setBitIndex = findNextSetIndex(words, startIndex)
            if (setBitIndex < 0) {
                break
            }

            var clearBitIndex = findNextUnsetIndex(words, setBitIndex)
            if (clearBitIndex < 0) {
                clearBitIndex = limitEnd - limitStart + 1
            }

            val start = setBitIndex + limitStart
            val end = clearBitIndex + limitStart - 1

            action(start, end)
            startIndex = clearBitIndex
        }
    }

    fun copy(): BitArrayRangeSet {
        return BitArrayRangeSet(limitStart, limitEnd, words.copyOf())
    }

    override fun equals(other: Any?): Boolean {
        return other is BitArrayRangeSet && words.contentEquals(other.words)
    }

    override fun hashCode(): Int {
        return words.contentHashCode()
    }

    override fun toString(): String {
        return buildString {
            append("BitArrayRangeSet(")

            var isFirst = true
            forEachRange { start, endInclusive ->
                if (!isFirst) {
                    append(", ")
                }

                isFirst = false

                append('[')
                append(start)
                append(", ")
                append(endInclusive)
                append(']')
            }

            append(')')
        }
    }

    operator fun iterator() = RangeIterator()

    inner class RangeIterator : Iterator<PackedIntRange> {
        private var startIndex = 0

        override fun hasNext(): Boolean {
            return findNextSetIndex(words, startIndex) >= 0
        }

        override fun next(): PackedIntRange {
            val setIndex = findNextSetIndex(words, startIndex)
            if (setIndex < 0) {
                throw IllegalStateException("End of the iterator")
            }

            var unsetIndex = findNextUnsetIndex(words, setIndex)
            if (unsetIndex < 0) {
                unsetIndex = limitEnd - limitStart + 1
            }

            val start = setIndex + limitStart
            val end = unsetIndex + limitStart - 1

            startIndex = unsetIndex

            return PackedIntRange(start, end)
        }
    }

    companion object {
        private const val WORD_SHIFT = 6
        private const val WORD_BIT_COUNT = 1 shl WORD_SHIFT
        private const val WORD_MASK = -1L

        private fun getWordIndex(bitIndex: Int) = bitIndex shr WORD_SHIFT
        private fun getInWordBitIndex(bitIndex: Int) = bitIndex and (WORD_BIT_COUNT - 1)
    }
}