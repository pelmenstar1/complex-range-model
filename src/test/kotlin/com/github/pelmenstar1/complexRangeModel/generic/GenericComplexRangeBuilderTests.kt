package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.ComplexRange
import com.github.pelmenstar1.complexRangeModel.IntComplexRange
import com.github.pelmenstar1.complexRangeModel.IntRangeFragment
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class GenericComplexRangeBuilderTests {
    @Test
    fun createEmptyTest() {
        val emptyRange = IntComplexRange {  }

        assertEquals(ComplexRange.empty(), emptyRange)
    }

    @Test
    fun createNonIntersectingTest() {
        createRangeTestHelper(arrayOf(1..2))
        createRangeTestHelper(arrayOf(1..2, 4..7))
        createRangeTestHelper(
            arrayOf(4..7, 1..2),
            expected = arrayOf(1..2, 4..7)
        )

        createRangeTestHelper(
            arrayOf(4..7, 1..2, 9..10),
            expected = arrayOf(1..2, 4..7, 9..10)
        )

        createRangeTestHelper(
            arrayOf(4..7, 9..10, 1..2),
            expected = arrayOf(1..2, 4..7, 9..10)
        )
    }

    @Test
    fun createIntersectingTest() {
        createRangeTestHelper(
            arrayOf(1..2, 1..2),
            expected = arrayOf(1..2)
        )

        createRangeTestHelper(
            arrayOf(1..2, 3..4),
            expected = arrayOf(1..4)
        )

        createRangeTestHelper(
            arrayOf(1..5, 3..4),
            expected = arrayOf(1..5)
        )

        createRangeTestHelper(
            arrayOf(3..4, 1..5),
            expected = arrayOf(1..5)
        )

        createRangeTestHelper(
            arrayOf(1..2, 4..6, 1..4),
            expected = arrayOf(1..6)
        )

        createRangeTestHelper(
            arrayOf(1..2, 4..6, 8..9, 4..7),
            expected = arrayOf(1..2, 4..9)
        )

        createRangeTestHelper(
            arrayOf(1..2, 4..6, 8..9, 11..12, 4..9),
            expected = arrayOf(1..2, 4..9, 11..12)
        )
    }

    private fun createRangeTestHelper(fragmentRanges: Array<IntRange>, expected: Array<IntRange> = fragmentRanges) {
        val actual = IntComplexRange(fragmentRanges)

        val expectedFragments = expected.map { IntRangeFragment(it) }.toTypedArray()
        val actualFragments = actual.toTypedArray()

        assertContentEquals(expectedFragments, actualFragments)
    }
}