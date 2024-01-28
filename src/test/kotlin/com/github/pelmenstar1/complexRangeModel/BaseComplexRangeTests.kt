package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

abstract class BaseComplexRangeTests {
    abstract fun createComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): IntComplexRange

    private fun createComplexRange(ranges: Array<IntRange>): IntComplexRange {
        return createComplexRange {
            ranges.forEach { fragment(it) }
        }
    }

    @Test
    fun toStringTest() {
        fun testCase(fragmentRanges: Array<IntRange>, expectedResult: String) {
            val range = createComplexRange(fragmentRanges)

            val actualResult = range.toString()
            assertEquals(expectedResult, actualResult)
        }

        testCase(emptyArray(), "ComplexRange()")
        testCase(arrayOf(1..2), "ComplexRange([1, 2])")
        testCase(arrayOf(1..2, 4..5), "ComplexRange([1, 2], [4, 5])")
    }

    @Test
    fun modifySetTest() {
        val range = createComplexRange {
            fragment(0, 2)
        }

        val newRange = range.modify {
            set(4, 5)
        }

        val expectedFragments = arrayOf(
            IntRangeFragment(0, 2),
            IntRangeFragment(4, 5)
        )
        val actualFragments = newRange.fragments().toTypedArray()

        assertContentEquals(expectedFragments, actualFragments)
    }

    @Test
    fun modifyUnsetTest() {
        fun testCase(initialRanges: Array<IntRange>, unsetRange: IntRange, expectedRanges: Array<IntRange>) {
            val initial = createComplexRange(initialRanges)
            val rangeAfterUnset = initial.modify {
                unset(unsetRange)
            }

            val expectedFragments = expectedRanges.map { IntRangeFragment(it) }.toTypedArray()
            val actualFragments = rangeAfterUnset.fragments().toTypedArray()

            assertContentEquals(expectedFragments, actualFragments)
        }

        testCase(
            initialRanges = arrayOf(0..1),
            unsetRange = 1..1,
            expectedRanges = arrayOf(0..0)
        )

        testCase(
            initialRanges = arrayOf(0..1),
            unsetRange = 0..0,
            expectedRanges = arrayOf(1..1)
        )

        testCase(
            initialRanges = arrayOf(0..1),
            unsetRange = 0..1,
            expectedRanges = emptyArray()
        )

        testCase(
            initialRanges = arrayOf(1..2),
            unsetRange = 0..3,
            expectedRanges = emptyArray()
        )

        testCase(
            initialRanges = arrayOf(0..1, 3..4),
            unsetRange = 0..4,
            expectedRanges = emptyArray()
        )

        testCase(
            initialRanges = arrayOf(0..1, 3..4),
            unsetRange = (-1)..5,
            expectedRanges = emptyArray()
        )

        testCase(
            initialRanges = arrayOf(0..1, 3..4),
            unsetRange = 0..3,
            expectedRanges = arrayOf(4..4)
        )

        testCase(
            initialRanges = arrayOf(0..1, 3..4, 6..7),
            unsetRange = 0..3,
            expectedRanges = arrayOf(4..4, 6..7)
        )

        testCase(
            initialRanges = arrayOf(0..1, 3..4),
            unsetRange = 1..4,
            expectedRanges = arrayOf(0..0)
        )

        testCase(
            initialRanges = arrayOf((-3)..(-2), 0..1, 3..4),
            unsetRange = 1..4,
            expectedRanges = arrayOf((-3)..(-2), 0..0)
        )

        testCase(
            initialRanges = arrayOf(0..1, 3..4),
            unsetRange = 1..3,
            expectedRanges = arrayOf(0..0, 4..4)
        )

        testCase(
            initialRanges = arrayOf((-3)..(-2), 0..1, 3..4, 6..7),
            unsetRange = 1..3,
            expectedRanges = arrayOf((-3)..(-2), 0..0, 4..4, 6..7)
        )

        testCase(
            initialRanges = arrayOf(0..3),
            unsetRange = 1..2,
            expectedRanges = arrayOf(0..0, 3..3)
        )

        testCase(
            initialRanges = arrayOf(0..1, 3..4, 6..7),
            unsetRange = 1..6,
            expectedRanges = arrayOf(0..0, 7..7)
        )

        testCase(
            initialRanges = arrayOf(0..1, 3..4, 6..7, 9..10),
            unsetRange = 1..9,
            expectedRanges = arrayOf(0..0, 10..10)
        )
    }

    @Test
    fun iteratorTest() {
        fun testCase(ranges: Array<IntRange>) {
            val complexRange = createComplexRange(ranges)
            val iterator = complexRange.fragments().iterator()

            var index = 0
            while (iterator.hasNext()) {
                val fragment = iterator.next()
                val expectedFrag = IntRangeFragment(ranges[index++])

                assertEquals(fragment, expectedFrag)
            }

            assertEquals(ranges.size, index)
        }

        testCase(emptyArray())
        testCase(arrayOf(1..2))
        testCase(arrayOf(1..2, 4..5))
        testCase(arrayOf(1..2, 4..5, 7..9))
    }
}