package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class BitArrayComplexRangeBuildTests {
    @Test
    fun buildRangeTest() {
        fun testCase(limitRange: IntRange, fragments: Array<IntRange>, expectedFragments: Array<IntRange> = fragments) {
            val actual = createComplexRange(limitRange, fragments)

            val actualRanges = actual.fragments().map { it.start.value..it.endInclusive.value }.toTypedArray()
            assertContentEquals(expectedFragments, actualRanges)
        }

        testCase(limitRange = 1..40, fragments = arrayOf(1..2, 6..9))
        testCase(limitRange = 1..40, fragments = arrayOf(1..2, 2..3), expectedFragments = arrayOf(1..3))
        testCase(limitRange = 0..63, fragments = arrayOf(0..63))
        testCase(limitRange = 0..127, fragments = arrayOf(0..63, 63..127), expectedFragments = arrayOf(0..127))
    }

    @Test
    fun throwsWhenInvalidRangeTest() {
        fun testCase(limitRange: IntRange, range: IntRange) {
            assertFailsWith<IllegalArgumentException> { createComplexRange(limitRange, arrayOf(range)) }
        }

        testCase(limitRange = 4..5, range = 3..4)
        testCase(limitRange = 4..5, range = 3..6)
        testCase(limitRange = 4..5, range = 6..7)
    }

    private fun createComplexRange(limitRange: IntRange, ranges: Array<IntRange>): IntComplexRange {
        return BitIntComplexRange(limitRange.first, limitRange.last) {
            ranges.forEach { fragment(it) }
        }
    }
}