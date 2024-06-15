package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertContentEquals

class ComplexRangeFragmentListTests {
    @Test
    fun subListImplTest() {
        fun testCase(ranges: Array<IntRange>, subListRange: IntRange) {
            val complexRange = IntComplexRange(ranges)
            val actual = complexRange.fragments().subListImpl(subListRange.first, subListRange.last + 1)
            val expected = ranges.sliceArray(subListRange).map { IntRangeFragment(it) }

            assertContentEquals(expected, actual)
        }

        testCase(ranges = arrayOf(1..2), subListRange = IntRange.EMPTY)
        testCase(ranges = arrayOf(1..2, 4..5), subListRange = 0..0)
        testCase(ranges = arrayOf(1..2, 4..5), subListRange = 0..1)
        testCase(ranges = arrayOf(1..2, 4..5), subListRange = 1..1)
        testCase(ranges = arrayOf(1..2, 4..5, 7..8), subListRange = 0..1)
        testCase(ranges = arrayOf(1..2, 4..5, 7..8), subListRange = 0..2)
        testCase(ranges = arrayOf(1..2, 4..5, 7..8), subListRange = 1..2)
        testCase(ranges = arrayOf(1..2, 4..5, 7..8), subListRange = 2..2)
    }
}