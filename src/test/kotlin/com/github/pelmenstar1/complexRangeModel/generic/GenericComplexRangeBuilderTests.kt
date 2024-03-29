package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class GenericComplexRangeBuilderTests {
    @Test
    fun createEmptyTest() {
        val emptyRange = IntComplexRange { }

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
    fun createWithValuesOneShotTest() {
        fun testHelperBase(values: Array<out Int>, expected: Array<IntRange>) {
            val actual = IntComplexRange {
                values.forEach { value(IntFragmentElement(it)) }
            }

            val expectedFragments = expected.mapToArray { IntRangeFragment(it) }
            val actualFragments = actual.fragments().toTypedArray()

            assertContentEquals(expectedFragments, actualFragments, "values: ${values.contentToString()}")
        }

        fun testHelper(values: Array<out Int>, expected: Array<IntRange>) {
            values.allPermutations().forEach { permValues ->
                testHelperBase(permValues, expected)
            }
        }

        testHelper(values = arrayOf(0), expected = arrayOf(0..0))
        testHelper(values = arrayOf(1, 2), expected = arrayOf(1..2))
        testHelper(values = arrayOf(1, 2, 3, 2), expected = arrayOf(1..3))
        testHelper(values = arrayOf(1, 2, 3, 5), expected = arrayOf(1..3, 5..5))
        testHelper(values = arrayOf(1, 2, 3, 5, 6), expected = arrayOf(1..3, 5..6))
        testHelper(values = arrayOf(1, 2, 3, 5, 6, 8), expected = arrayOf(1..3, 5..6, 8..8))
    }

    @Test
    fun createWithValuesTest() {
        fun testHelperBase(values: Array<out Int>, expected: Array<IntRange>) {
            val actual = IntComplexRange {
                values(values.mapToArray { IntFragmentElement(it) })
            }

            val expectedFragments = expected.mapToArray { IntRangeFragment(it) }
            val actualFragments = actual.fragments().toTypedArray()

            assertContentEquals(expectedFragments, actualFragments, "values: ${values.contentToString()}")
        }

        fun testHelper(values: Array<out Int>, expected: Array<IntRange>) {
            values.allPermutations().forEach { permValues ->
                testHelperBase(permValues, expected)
            }
        }

        testHelper(values = arrayOf(0), expected = arrayOf(0..0))
        testHelper(values = arrayOf(1, 2), expected = arrayOf(1..2))
        testHelper(values = arrayOf(1, 2, 3, 2), expected = arrayOf(1..3))
        testHelper(values = arrayOf(1, 2, 3, 5), expected = arrayOf(1..3, 5..5))
        testHelper(values = arrayOf(1, 2, 3, 5, 6), expected = arrayOf(1..3, 5..6))
        testHelper(values = arrayOf(1, 2, 3, 5, 6, 8), expected = arrayOf(1..3, 5..6, 8..8))
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
        // Regardless of the order of addition, the result must be the same.
        for (permRanges in fragmentRanges.allPermutations()) {
            createRangeTestHelperBase(permRanges, expected)
        }
    }

    private fun createRangeTestHelperBase(
        fragmentRanges: Array<out IntRange>,
        expected: Array<out IntRange> = fragmentRanges
    ) {
        val actual = IntComplexRange {
            fragmentRanges.forEach { fragment(it) }
        }

        val expectedFragments = expected.mapToArray { IntRangeFragment(it) }
        val actualFragments = actual.fragments().toTypedArray()

        assertContentEquals(expectedFragments, actualFragments)
    }
}