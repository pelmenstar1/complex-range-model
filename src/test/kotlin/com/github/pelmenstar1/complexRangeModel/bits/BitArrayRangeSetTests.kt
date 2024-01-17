package com.github.pelmenstar1.complexRangeModel.bits

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class BitArrayRangeSetTests {
    @Test
    fun addAndGetTest() {
        fun testCase(limitRange: IntRange, ranges: Array<IntRange>) {
            val set = createSet(limitRange, ranges)

            for (i in ranges.indices) {
                val actual = set[i]
                val expected = PackedIntRange(ranges[i])

                assertEquals(expected, actual)
            }
        }

        testCase(limitRange = 0..10, ranges = arrayOf(0..1))
        testCase(limitRange = 0..10, ranges = arrayOf(0..10))
        testCase(limitRange = 0..10, ranges = arrayOf(0..1, 3..5))
        testCase(limitRange = 5..10, ranges = arrayOf(5..6, 8..9))
        testCase(limitRange = 5..100, ranges = arrayOf(5..6, 8..9, 70..80))
        testCase(limitRange = 0..63, ranges = arrayOf(0..63))
        testCase(limitRange = 5..100, ranges = arrayOf(5..6, 8..9, 50..80))
        testCase(limitRange = 5..200, ranges = arrayOf(5..6, 8..9, 50..200))
    }

    @Test
    fun isEmptyTest() {
        fun testCase(limitRange: IntRange, ranges: Array<IntRange>) {
            val set = createSet(limitRange, ranges)
            val expected = ranges.isEmpty()
            val actual = set.isEmpty()

            assertEquals(expected, actual)
        }

        testCase(limitRange = 0..10, ranges = emptyArray())
        testCase(limitRange = 0..100, ranges = emptyArray())
        testCase(limitRange = 0..10, ranges = arrayOf(0..1))
    }

    @Test
    fun containsTest() {
        fun testCase(limitRange: IntRange, ranges: Array<IntRange>, input: IntRange, expected: Boolean) {
            val set = createSet(limitRange, ranges)
            val actual = set.contains(input.first, input.last)

            assertEquals(expected, actual)
        }

        testCase(
            limitRange = 0..10,
            ranges = arrayOf(0..5),
            input = 0..5,
            expected = true
        )

        testCase(
            limitRange = 0..100,
            ranges = arrayOf(50..70),
            input = 50..70,
            expected = true
        )

        testCase(
            limitRange = 0..10,
            ranges = arrayOf(0..5),
            input = 0..4,
            expected = false
        )

        testCase(
            limitRange = 0..10,
            ranges = arrayOf(0..5),
            input = 100..200,
            expected = false
        )
    }

    @Test
    fun removeTest() {
        fun testCase(limitRange: IntRange, ranges: Array<IntRange>, rangeToRemove: IntRange, expectedRanges: Array<IntRange>) {
            val set = createSet(limitRange, ranges)
            set.remove(rangeToRemove.first, rangeToRemove.last)

            val actualRanges = toIntRanges(set)
            assertContentEquals(expectedRanges, actualRanges)
        }

        testCase(
            limitRange = 0..10,
            ranges = arrayOf(0..10),
            rangeToRemove = 0..10,
            expectedRanges = emptyArray()
        )
        testCase(
            limitRange = 0..10,
            ranges = arrayOf(0..10),
            rangeToRemove = 5..6,
            expectedRanges = arrayOf(0..4, 7..10)
        )
        testCase(
            limitRange = 0..100,
            ranges = arrayOf(0..70),
            rangeToRemove = 0..60,
            expectedRanges = arrayOf(61..70)
        )

        testCase(
            limitRange = 0..1000,
            ranges = arrayOf(100..900),
            rangeToRemove = 200..300,
            expectedRanges = arrayOf(100..199, 301..900)
        )
    }

    @Test
    fun iteratorTest() {
        fun testCase(limitRange: IntRange, ranges: Array<IntRange>) {
            val set = createSet(limitRange, ranges)
            val actualRangesList = ArrayList<IntRange>()
            val iter = set.iterator()

            while(iter.hasNext()) {
                actualRangesList.add(iter.next().toIntRange())
            }

            val actualRanges = actualRangesList.toTypedArray()
            assertContentEquals(ranges, actualRanges)
        }

        testCase(limitRange = 0..10, ranges = arrayOf(0..1))
        testCase(limitRange = 0..10, ranges = arrayOf(0..10))
        testCase(limitRange = 0..10, ranges = arrayOf(0..1, 3..5))
        testCase(limitRange = 5..10, ranges = arrayOf(5..6, 8..9))
        testCase(limitRange = 5..100, ranges = arrayOf(5..6, 8..9, 70..80))
        testCase(limitRange = 0..63, ranges = arrayOf(0..63))
        testCase(limitRange = 5..100, ranges = arrayOf(5..6, 8..9, 50..80))
        testCase(limitRange = 5..200, ranges = arrayOf(5..6, 8..9, 50..200))
    }

    @Test
    fun toStringTest() {
        fun testCase(limitRange: IntRange, ranges: Array<IntRange>, expected: String) {
            val set = createSet(limitRange, ranges)
            val actual = set.toString()

            assertEquals(expected, actual)
        }

        testCase(limitRange = 0..10, ranges = emptyArray(), expected = "BitArrayRangeSet()")
        testCase(limitRange = 0..10, ranges = arrayOf(0..1), expected = "BitArrayRangeSet([0, 1])")
        testCase(limitRange = 0..10, ranges = arrayOf(0..10), expected = "BitArrayRangeSet([0, 10])")
        testCase(limitRange = 0..10, ranges = arrayOf(0..1, 3..5), expected = "BitArrayRangeSet([0, 1], [3, 5])")
        testCase(limitRange = 5..10, ranges = arrayOf(5..6, 8..9), expected = "BitArrayRangeSet([5, 6], [8, 9])")
        testCase(limitRange = 5..100, ranges = arrayOf(5..6, 8..9, 70..80), expected = "BitArrayRangeSet([5, 6], [8, 9], [70, 80])")
        testCase(limitRange = 0..63, ranges = arrayOf(0..63), expected = "BitArrayRangeSet([0, 63])")
        testCase(limitRange = 5..100, ranges = arrayOf(5..6, 8..9, 50..80), expected = "BitArrayRangeSet([5, 6], [8, 9], [50, 80])")
        testCase(limitRange = 5..200, ranges = arrayOf(5..6, 8..9, 50..200), expected = "BitArrayRangeSet([5, 6], [8, 9], [50, 200])")
    }

    private fun createSet(limitRange: IntRange, ranges: Array<IntRange>): BitArrayRangeSet {
        return BitArrayRangeSet(limitRange.first, limitRange.last).apply {
            ranges.forEach { add(it.first, it.last) }
        }
    }

    private fun toIntRanges(set: BitArrayRangeSet): Array<IntRange> {
        return Array(set.size) { set[it].toIntRange() }
    }
}