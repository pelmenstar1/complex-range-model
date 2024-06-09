package com.github.pelmenstar1.complexRangeModel.bits

internal class FixedBitSet {
    val words: LongArray
    val bitStartIndex: Int
    val bitEndIndex: Int // inclusive

    constructor(minimumBitCount: Int) {
        words = LongArray((minimumBitCount + 63) / 64)
        bitStartIndex = 0
        bitEndIndex = words.size * WORD_BIT_COUNT - 1
    }

    internal constructor(words: LongArray) {
        this.words = words
        bitStartIndex = 0
        bitEndIndex = words.size * WORD_BIT_COUNT - 1
    }

    internal constructor(words: LongArray, bitStartIndex: Int, bitEndIndex: Int) {
        this.words = words
        this.bitStartIndex = bitStartIndex
        this.bitEndIndex = bitEndIndex
    }

    fun isEmpty(): Boolean {
        return aggregateBool(
            bitStartIndex, bitEndIndex,
            partialWord = { value, mask -> value and mask == 0L },
            completeWord = { value -> value == 0L }
        )
    }

    operator fun get(index: Int): Boolean {
        return (words[getWordIndex(index)] and (1L shl index)) != 0L
    }

    fun countSetBits(): Int {
        return aggregateBits(bitStartIndex, bitEndIndex, 0) { acc, value -> acc + value.countOneBits() }
    }

    fun maxSetBits(): Int {
        return bitEndIndex - bitStartIndex + 1
    }

    fun countRanges(): Int {
        var count = 0
        forEachRange { _, _ -> count++ }

        return count
    }

    fun set(index: Int) {
        setMask(getWordIndex(index), mask = 1L shl index)
    }

    fun containsRange(startIndex: Int, endIndex: Int): Boolean {
        if (includesRange(startIndex, endIndex)) {
            // Check that bits on sides are zero, so that given range [startIndex, endIndex] is a distinct range and not a part of another wider range.
            return (startIndex == 0 || !get(startIndex - 1)) && (endIndex == maxSetBits() || !get(endIndex + 1))
        }

        return false
    }

    fun includesRange(startIndex: Int, endIndex: Int): Boolean {
        return aggregateBool(
            startIndex, endIndex,
            partialWord = { value, mask -> (value and mask) == mask },
            completeWord = { value -> value == WORD_MASK }
        )
    }

    fun set(startIndex: Int, endIndex: Int) {
        modifyRange(startIndex, endIndex, fillValue = WORD_MASK, aggregate = { word, mask -> word or mask })
    }

    fun unset(startIndex: Int, endIndex: Int) {
        modifyRange(startIndex, endIndex, fillValue = 0, aggregate = { word, mask -> word and mask.inv() })
    }

    private inline fun modifyRange(startIndex: Int, endIndex: Int, fillValue: Long, aggregate: (Long, Long) -> Long) {
        useStartEndMask(
            startIndex, endIndex,
            singleWord = { index, mask -> setMask(index, mask, aggregate) },
            multipleWords = { startWordIndex, endWordIndex, startMask, endMask ->
                setMask(startWordIndex, startMask, aggregate)
                setMask(endWordIndex, endMask, aggregate)

                words.fill(fillValue, startWordIndex + 1, endWordIndex)
            }
        )
    }

    private inline fun<T> useStartEndMask(
        startIndex: Int, endIndex: Int,
        singleWord: (wordIndex: Int, mask: Long) -> T,
        multipleWords: (startWordIndex: Int, endWordIndex: Int, startMask: Long, endMask: Long) -> T
    ): T {
        val startWordIndex = getWordIndex(startIndex)
        val endWordIndex = getWordIndex(endIndex)

        val startMask = startMask(startIndex)
        val endMask = endMask(endIndex)

        return if (startWordIndex == endWordIndex) {
            singleWord(startWordIndex, startMask and endMask)
        } else {
            multipleWords(startWordIndex, endWordIndex, startMask, endMask)
        }
    }

    private inline fun<A> aggregateBits(startIndex: Int, endIndex: Int, initial: A, combine: (A, Long) -> A): A {
        val ws = words

        return useStartEndMask(
            startIndex, endIndex,
            singleWord = { i, mask -> combine(initial, ws[i] and mask) },
            multipleWords = { startWordIndex, endWordIndex, startMask, endMask ->
                var acc = combine(initial, ws[startWordIndex] and startMask)
                acc = combine(acc, ws[endWordIndex] and endMask)

                for (i in (startWordIndex + 1) until endWordIndex) {
                    acc = combine(acc, ws[i])
                }

                acc
            }
        )
    }

    private inline fun aggregateBool(
        startIndex: Int, endIndex: Int,
        partialWord: (value: Long, mask: Long) -> Boolean,
        completeWord: (value: Long) -> Boolean
    ): Boolean {
        val ws = words

        useStartEndMask(
            startIndex, endIndex,
            singleWord = { i, mask -> return partialWord(ws[i], mask) },
            multipleWords = { startWordIndex, endWordIndex, startMask, endMask ->
                if (partialWord(ws[startWordIndex], startMask) && partialWord(ws[endWordIndex], endMask)) {
                    for (i in (startWordIndex + 1) until endWordIndex) {
                        if (!completeWord(ws[i])) {
                            return false
                        }
                    }

                    return true
                }
                return false
            }
        )
    }

    fun setMask(wordIndex: Int, mask: Long) {
        words[wordIndex] = words[wordIndex] or mask
    }

    private inline fun setMask(wordIndex: Int, mask: Long, aggregate: (Long, Long) -> Long) {
        words[wordIndex] = aggregate(words[wordIndex], mask)
    }

    fun findNextSetBitIndex(startIndex: Int): Int {
        return findNextBitBase(startIndex, mapWord = { it })
    }

    fun findNextUnsetBitIndex(startIndex: Int): Int {
        return findNextBitBase(startIndex, mapWord = Long::inv)
    }

    private inline fun findNextBitBase(startIndex: Int, mapWord: (Long) -> Long): Int {
        if (startIndex > bitEndIndex) {
            return -1
        }

        val ws = words

        var wordIndex = getWordIndex(startIndex)
        val endWordIndex = getWordIndex(bitEndIndex)

        var word = mapWord(ws[wordIndex]) and startMask(startIndex)

        while (true) {
            if (word != 0L) {
                val result = wordIndex * WORD_BIT_COUNT + word.countTrailingZeroBits()

                return if (result <= bitEndIndex) result else -1
            }

            if (wordIndex == endWordIndex) return -1
            wordIndex++

            word = mapWord(ws[wordIndex])
        }
    }

    fun findPreviousSetBitIndex(startIndex: Int): Int {
        return findPreviousBitBase(startIndex, mapWord = { it })
    }

    fun findPreviousUnsetBitIndex(startIndex: Int): Int {
        return findPreviousBitBase(startIndex, mapWord = Long::inv)
    }

    private inline fun findPreviousBitBase(startIndex: Int, mapWord: (Long) -> Long): Int {
        if (startIndex <= 0) {
            return -1
        }

        val startWordIndex = getWordIndex(startIndex)

        var i = startWordIndex
        val ws = words
        val endWordIndex = getWordIndex(bitStartIndex)

        var word = mapWord(ws[i]) and endMask (startIndex)

        while (true) {
            if (word != 0L) {
                val result = i * WORD_BIT_COUNT - word.countLeadingZeroBits() + WORD_BIT_COUNT - 1

                return if (result >= bitStartIndex) result else -1
            }

            if (i == endWordIndex) {
                return -1
            }

            i--
            word = mapWord(ws[i])
        }
    }

    fun lastSetBitIndex(): Int {
        return findPreviousSetBitIndex(bitEndIndex)
    }

    inline fun forEachRange(block: (start: Int, endInclusive: Int) -> Unit) {
        var start = bitStartIndex

        while(true) {
            val rangeStart = findNextSetBitIndex(start)
            if (rangeStart < 0) {
                break
            }

            var unsetBitIndex = findNextUnsetBitIndex(rangeStart)
            if (unsetBitIndex < 0) {
                unsetBitIndex = bitEndIndex + 1
            }

            start = unsetBitIndex
            block(rangeStart, unsetBitIndex - 1)
        }
    }

    inline fun forEachRangeReversed(block: (start: Int, endInclusive: Int) -> Unit) {
        var start = bitEndIndex

        while (true) {
            val setBitIndex = findPreviousSetBitIndex(start)
            if (setBitIndex < 0) {
                break
            }

            var unsetBitIndex = findPreviousUnsetBitIndex(setBitIndex)
            unsetBitIndex = maxOf(unsetBitIndex, -1)

            block(unsetBitIndex + 1, setBitIndex)
            start = unsetBitIndex - 1
        }
    }

    fun copyOf(): FixedBitSet {
        return FixedBitSet(words.copyOf(), bitStartIndex, bitEndIndex)
    }

    fun select(startIndex: Int, endIndex: Int): FixedBitSet {
        return FixedBitSet(words, startIndex, endIndex)
    }

    override fun equals(other: Any?): Boolean {
        return other is FixedBitSet && words.contentEquals(other.words)
    }

    override fun hashCode(): Int {
        return aggregateBits(bitStartIndex, bitEndIndex, initial = 31) { hash, value ->
            hash + (value xor (value ushr 32)).toInt()
        }
    }

    companion object {
        private const val WORD_SHIFT = 6
        private const val WORD_BIT_COUNT = 1 shl WORD_SHIFT
        private const val WORD_MASK = -1L

        fun getWordIndex(bitIndex: Int) = bitIndex shr WORD_SHIFT

        private fun startMask(startIndex: Int) = WORD_MASK shl startIndex
        private fun endMask(endIndex: Int) = WORD_MASK ushr (-endIndex - 1)
    }
}