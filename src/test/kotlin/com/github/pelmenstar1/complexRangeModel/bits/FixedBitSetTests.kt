package com.github.pelmenstar1.complexRangeModel.bits

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class FixedBitSetTests {
    @Test
    fun findNextSetIndexTest() {
        fun testCase(words: LongArray, startIndex: Int, expected: Int) {
            val actual = FixedBitSet(words).findNextSetBitIndex(startIndex)

            assertEquals(expected, actual)
        }

        testCase(
            words = longArrayOf(0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000),
            startIndex = 0,
            expected = -1
        )

        testCase(
            words = longArrayOf(0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00000001),
            startIndex = 0,
            expected = 0
        )

        testCase(
            words = longArrayOf(0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_10000000),
            startIndex = 0,
            expected = 7
        )

        testCase(
            words = longArrayOf(0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_10000001),
            startIndex = 1,
            expected = 7
        )

        testCase(
            words = longArrayOf(
                0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_10000001,
                0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_10000000
            ),
            startIndex = 64,
            expected = 71
        )
    }

    @Test
    fun findNextUnsetIndexTest() {
        fun testCase(words: LongArray, startIndex: Int, expected: Int) {
            val actual = FixedBitSet(words).findNextUnsetBitIndex(startIndex)

            assertEquals(expected, actual)
        }

        testCase(
            words = longArrayOf(0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_11111111UL.toLong()),
            startIndex = 0,
            expected = -1
        )

        testCase(
            words = longArrayOf(0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_11111110UL.toLong()),
            startIndex = 0,
            expected = 0
        )

        testCase(
            words = longArrayOf(0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_01111111UL.toLong()),
            startIndex = 0,
            expected = 7
        )

        testCase(
            words = longArrayOf(0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_01111110UL.toLong()),
            startIndex = 1,
            expected = 7
        )

        testCase(
            words = longArrayOf(
                0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_01111110UL.toLong(),
                0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_01111111UL.toLong()
            ),
            startIndex = 64,
            expected = 71
        )
    }

    @Test
    fun isEmptyTest() {
        fun testCase(words: LongArray, expected: Boolean, bitRange: IntRange? = null) {
            propertyTestCase(words, FixedBitSet::isEmpty, expected, bitRange)
        }

        fun testCase(wordCount: Int, ranges: List<IntRange>, expected: Boolean, bitRange: IntRange? = null) {
            propertyTestCase(wordCount, ranges, FixedBitSet::isEmpty, expected, bitRange)
        }

        testCase(words = LongArray(1), expected = true)
        testCase(words = LongArray(5), expected = true)
        testCase(words = longArrayOf(0, 1), expected = false)
        testCase(words = longArrayOf(0, 0, 1, 0, 0), expected = false)
        testCase(wordCount = 2, ranges = listOf(0..2, 66..127), expected = true, bitRange = 3..65)
    }

    @Test
    fun countSetBitsTest() {
        fun testCase(wordCount: Int, ranges: List<IntRange>, expected: Int, bitRange: IntRange? = null) {
            propertyTestCase(wordCount, ranges, FixedBitSet::countSetBits, expected, bitRange)
        }

        testCase(wordCount = 1, ranges = emptyList(), expected = 0)
        testCase(wordCount = 1, ranges = emptyList(), expected = 0)
        testCase(wordCount = 1, ranges = listOf(0..0), expected = 1)
        testCase(wordCount = 1, ranges = listOf(0..2), expected = 3)
        testCase(wordCount = 1, ranges = listOf(1..4), expected = 4)
        testCase(wordCount = 2, ranges = listOf(1..4, 65..66), expected = 6)
        testCase(wordCount = 1, ranges = listOf(0..63), expected = 10, bitRange = 1..10)
        testCase(wordCount = 2, ranges = listOf(0..127), expected = 4, bitRange = 62..65)
    }

    @Test
    fun maxSetBitsTest() {
        fun testCase(wordCount: Int, expected: Int, bitRange: IntRange? = null) {
            propertyTestCase(LongArray(wordCount), FixedBitSet::maxSetBits, expected, bitRange)
        }

        testCase(wordCount = 0, expected = 0)
        testCase(wordCount = 1, expected = 64)
        testCase(wordCount = 3, expected = 192)
        testCase(wordCount = 1, expected = 10, bitRange = 1..10)
    }

    @Test
    fun getTest() {
        fun testCase(words: LongArray, index: Int, expected: Boolean) {
            propertyTestCase(words, { get(index) }, expected)
        }

        testCase(words = LongArray(1), index = 5, expected = false)
        testCase(words = longArrayOf(0b11111), index = 3, expected = true)
        testCase(words = longArrayOf(0b10), index = 0, expected = false)
        testCase(words = longArrayOf(0, 0b10), index = 65, expected = true)
    }

    @Test
    fun countRangesTest() {
        fun testCase(words: LongArray, expected: Int) {
            propertyTestCase(words, FixedBitSet::countRanges, expected)
        }

        testCase(words = LongArray(0), expected = 0)
        testCase(words = longArrayOf(0b0001111), expected = 1)
        testCase(words = longArrayOf(0b1100111), expected = 2)
        testCase(words = longArrayOf(0b1110001111, 0b110011), expected = 4)
    }

    @Test
    fun setSingleBitTest() {
        fun testCase(words: LongArray, index: Int, expectedWords: LongArray) {
            val bitSet = FixedBitSet(words)
            bitSet.set(index)

            val actual = bitSet.words
            assertContentEquals(expectedWords, actual)
        }

        testCase(words = longArrayOf(0), index = 1, expectedWords = longArrayOf(0b10))
        testCase(words = longArrayOf(0b11, 0), index = 64, expectedWords = longArrayOf(0b11, 0b1))
    }

    @Test
    fun setRangeTest() {
        fun testCase(words: LongArray, range: IntRange, expectedWords: LongArray) {
            val bitSet = FixedBitSet(words)
            bitSet.set(range.first, range.last)

            val actualWords = bitSet.words
            assertContentEquals(expectedWords, actualWords)
        }

        testCase(words = longArrayOf(0), range = 1..5, expectedWords = longArrayOf(0b111110))
        testCase(
            words = longArrayOf(0, 0),
            range = 50..70,
            expectedWords = longArrayOf(
                0b11111111_11111100_00000000_00000000_00000000_00000000_00000000_00000000UL.toLong(),
                0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_01111111
            )
        )
        testCase(
            words = longArrayOf(0, 0, 0, 0, 0),
            range = 120..260,
            expectedWords = longArrayOf(
                0,
                0b11111111_00000000_00000000_00000000_00000000_00000000_00000000_00000000UL.toLong(),
                -1,
                -1,
                0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_00011111
            )
        )
    }

    @Test
    fun forEachRangeTest() {
        fun testCase(wordCount: Int, ranges: List<IntRange>) {
            forEachTestHelper(wordCount, ranges, FixedBitSet::forEachRange)
        }

        testCase(wordCount = 1, ranges = listOf(0..5, 10..63))
        testCase(wordCount = 1, ranges = emptyList())
        testCase(wordCount = 2, ranges = listOf(0..6, 50..65, 70..80, 122..127))
        testCase(wordCount = 1, ranges = listOf(1..1, 3..3, 5..5))
    }

    @Test
    fun forEachRangeReversedTest() {
        fun testCase(wordCount: Int, ranges: List<IntRange>) {
            forEachTestHelper(
                wordCount,
                ranges,
                FixedBitSet::forEachRangeReversed,
                transformActual = { it.reverse() }
            )
        }

        testCase(wordCount = 1, ranges = listOf(0..5, 10..63))
        testCase(wordCount = 1, ranges = emptyList())
        testCase(wordCount = 2, ranges = listOf(0..6, 50..65, 70..80, 122..127))
        testCase(wordCount = 1, ranges = listOf(1..1, 3..3, 5..5))
    }

    private fun forEachTestHelper(
        wordCount: Int, ranges: List<IntRange>,
        method: FixedBitSet.((Int, Int) -> Unit) -> Unit,
        transformActual: (MutableList<IntRange>) -> Unit = { }
    ) {
        val bitSet = fixedBitSet(wordCount) {
            ranges.forEach { set(it) }
        }

        val actualRanges = ArrayList<IntRange>()
        bitSet.method { start, endInclusive ->
            actualRanges.add(start..endInclusive)
        }

        transformActual(actualRanges)

        assertEquals(ranges, actualRanges)
    }

    @Test
    fun includesRangeTest() {
        fun testCase(wordCount: Int, ranges: List<IntRange>, range: IntRange, expected: Boolean) {
            propertyTestCase(wordCount, ranges, { includesRange(range.first, range.last) }, expected)
        }

        testCase(wordCount = 1, ranges = listOf(1..5, 8..9), range = 1..5, expected = true)
        testCase(wordCount = 1, ranges = listOf(1..5, 8..9), range = 2..3, expected = true)
        testCase(wordCount = 1, ranges = listOf(1..5, 8..9), range = 4..9, expected = false)
        testCase(wordCount = 2, ranges = listOf(50..70), range = 52..68, expected = true)
        testCase(wordCount = 3, ranges = listOf(50..130), range = 52..129, expected = true)
    }

    @Test
    fun containsRangeTest() {
        fun testCase(wordCount: Int, ranges: List<IntRange>, range: IntRange, expected: Boolean) {
            propertyTestCase(wordCount, ranges, { containsRange(range.first, range.last) }, expected)
        }

        testCase(wordCount = 1, ranges = listOf(1..5, 8..9), range = 1..5, expected = true)
        testCase(wordCount = 1, ranges = listOf(1..5, 8..9), range = 2..3, expected = false)
        testCase(wordCount = 1, ranges = listOf(1..5, 8..9), range = 4..9, expected = false)
        testCase(wordCount = 2, ranges = listOf(50..70), range = 50..70, expected = true)
        testCase(wordCount = 3, ranges = listOf(50..130), range = 50..130, expected = true)
    }

    private fun<T> propertyTestCase(words: LongArray, property: FixedBitSet.() -> T, expected: T, bitRange: IntRange? = null) {
        val bitSet = FixedBitSet(words, bitRange?.first ?: 0, bitRange?.last ?: (words.size * 64 - 1))
        val actual = bitSet.property()

        assertEquals(expected, actual)
    }

    private fun<T> propertyTestCase(wordCount: Int, ranges: List<IntRange>, property: FixedBitSet.() -> T, expected: T, bitRange: IntRange? = null) {
        val bitSet = fixedBitSet(wordCount, bitRange) {
            ranges.forEach { set(it) }
        }
        val actual = bitSet.property()

        assertEquals(expected, actual)
    }

    private fun fixedBitSet(wordCount: Int, bitRange: IntRange? = null, block: BitSetBuilder.() -> Unit): FixedBitSet {
        return BitSetBuilder(wordCount)
            .also(block)
            .bitSet
            .select(bitRange?.first ?: 0, bitRange?.last ?: (wordCount * 64 - 1))
    }

    private class BitSetBuilder(wordCount: Int) {
        val bitSet = FixedBitSet(LongArray(wordCount))

        fun set(range: IntRange) {
            bitSet.set(range.first, range.last)
        }
    }
}