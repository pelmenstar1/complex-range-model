package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class BitIntComplexRangeBuilderTests : BaseComplexRangeBuilderTests() {
    override fun createComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): IntComplexRange {
        return BitIntComplexRange(0, 100, block)
    }

    @Test
    fun buildRangeTest() {
        fun testCase(limitRange: IntRange, fragments: Array<IntRange>, expectedFragments: Array<IntRange> = fragments) {
            val actual = createComplexRange(limitRange, fragments)

            val actualRanges = getFragmentRanges(actual)
            assertContentEquals(expectedFragments, actualRanges)
        }

        testCase(limitRange = 1..40, fragments = arrayOf(1..2, 6..9))
        testCase(limitRange = 1..40, fragments = arrayOf(1..2, 2..3), expectedFragments = arrayOf(1..3))
        testCase(limitRange = 0..63, fragments = arrayOf(0..63))
        testCase(limitRange = 0..127, fragments = arrayOf(0..63, 63..127), expectedFragments = arrayOf(0..127))
    }

    @Test
    fun buildRangeWithValuesTest() {
        fun testCase(limitRange: IntRange, vals: Array<Int>, expected: Array<IntRange>) {
            val actual = BitIntComplexRange(limitRange.first, limitRange.last) {
                values(vals.map { IntFragmentElement(it) })
            }

            val actualRanges = getFragmentRanges(actual)
            assertContentEquals(expected, actualRanges)
        }

        testCase(limitRange = 1..40, vals = arrayOf(3, 4, 6, 7), expected = arrayOf(3..4, 6..7))
        testCase(limitRange = 1..100, vals = arrayOf(3, 4, 3, 66, 68), expected = arrayOf(3..4, 66..66, 68..68))
        testCase(limitRange = 0..63, vals = (0..63).toList().toTypedArray(), expected = arrayOf(0..63))
    }


    @Test
    fun buildRangeWithValuesThrowsOnInvalidValueTest() {
        assertFailsWith<IllegalArgumentException> {
            BitIntComplexRange(0,10) {
                values(arrayOf(IntFragmentElement(12)))
            }
        }
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

    private fun getFragmentRanges(complexRange: ComplexRange<IntFragmentElement>): Array<IntRange> {
        return complexRange.fragments().map { it.start.value..it.endInclusive.value }.toTypedArray()
    }

    private fun createComplexRange(limitRange: IntRange, ranges: Array<IntRange>): IntComplexRange {
        return BitIntComplexRange(limitRange.first, limitRange.last) {
            ranges.forEach { fragment(it) }
        }
    }
}