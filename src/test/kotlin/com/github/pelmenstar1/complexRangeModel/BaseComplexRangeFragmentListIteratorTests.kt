package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private typealias FragmentIterator = ComplexRangeFragmentListIterator<IntFragmentElement>

abstract class BaseComplexRangeFragmentListIteratorTests {
    protected abstract fun createIterator(data: List<IntRangeFragment>): FragmentIterator

    class SubRangeTestCase(val ranges: Array<IntRange>, val subRangeIndices: IntRange)

    open fun iterateForwardBackwardTestData(): List<List<IntRange>> {
        return listOf(
            listOf(1..2),
            listOf(0..2),
            listOf(0..63),
            listOf(0..127),
            listOf(1..2, 4..5),
            listOf(0..2, 4..5),
            listOf(1..2, 4..5, 7..8),
            listOf(0..2, 4..5, 7..8, 40..63),
        )
    }

    @Test
    fun iterateForwardBackwardTest() {
        fun forwardPass(iter: FragmentIterator, expectedElements: List<IntRangeFragment>) {
            var index = 0

            do {
                val current = iter.current
                val expected = expectedElements[index++]

                assertEquals(expected, current)
            } while(iter.moveNext())

            assertEquals(expectedElements.size, index)
        }

        fun backwardPass(iter: FragmentIterator, expectedElements: List<IntRangeFragment>) {
            var index = expectedElements.size - 1

            do {
                val current = iter.current
                val expected = expectedElements[index--]

                assertEquals(expected, current)
            } while(iter.movePrevious())

            assertEquals(-1, index)
        }

        fun testCase(elements: List<IntRange>) {
            val expectedFragments = elements.map { IntRangeFragment(it) }

            val iter = createIterator(expectedFragments)
            iter.moveNext()

            forwardPass(iter, expectedFragments)
            backwardPass(iter, expectedFragments)
            forwardPass(iter, expectedFragments)
        }

        iterateForwardBackwardTestData().forEach { testCase(it) }
    }

    open fun iterateNextPreviousForwardTestData(): List<Array<IntRange>> {
        return listOf(
            arrayOf(1..1, 3..6),
            arrayOf(1..1, 3..3, 5..6),
            arrayOf(1..1, 3..3, 5..5, 7..9)
        )
    }

    @Test
    fun iterateNextPreviousForwardTest() {
        fun testCase(elements: Array<IntRange>) {
            val expectedFragments = elements.map { IntRangeFragment(it) }

            val iter = createIterator(expectedFragments)
            var index = 1

            iter.moveNext()

            while(true) {
                val moveNextRes = iter.moveNext()

                if(!moveNextRes) {
                    break
                }

                val currentElement1 = iter.current
                val expectedElement1 = IntRangeFragment(elements[index])
                assertEquals(expectedElement1, currentElement1)

                val movePrevRes = iter.movePrevious()
                assertTrue(movePrevRes)

                val currentElement2 = iter.current
                val expectedElement2 = IntRangeFragment(elements[index - 1])
                assertEquals(expectedElement2, currentElement2)

                val moveNextRes2 = iter.moveNext()
                assertTrue(moveNextRes2)

                val currentElement3 = iter.current
                assertEquals(expectedElement1, currentElement3)

                index++
            }

            assertEquals(elements.size, index)
        }

        iterateNextPreviousForwardTestData().forEach { testCase(it) }
    }

    open fun subRangeTestData(): List<SubRangeTestCase> {
        return listOf(
            SubRangeTestCase(ranges = arrayOf(1..2), subRangeIndices = 0..0),
            SubRangeTestCase(ranges = arrayOf(1..2, 4..5), subRangeIndices = 0..0),
            SubRangeTestCase(ranges = arrayOf(1..2, 4..5), subRangeIndices = 0..1),
            SubRangeTestCase(ranges = arrayOf(1..2, 4..5, 7..8), subRangeIndices = 0..0),
            SubRangeTestCase(ranges = arrayOf(1..2, 4..5, 7..8), subRangeIndices = 0..1),
            SubRangeTestCase(ranges = arrayOf(1..2, 4..5, 7..8), subRangeIndices = 1..1),
            SubRangeTestCase(ranges = arrayOf(1..2, 4..5, 7..8), subRangeIndices = 1..2),
            SubRangeTestCase(ranges = arrayOf(1..2, 4..5, 7..8), subRangeIndices = 0..2)
        )
    }

    @Test
    fun subRangeTest() {
        fun testCase(ranges: Array<IntRange>, subRangeIndices: IntRange) {
            val fragments = ranges.map { IntRangeFragment(it) }
            val iter = createIterator(fragments)

            repeat(subRangeIndices.first + 1) {
                iter.moveNext()
            }
            iter.mark()

            repeat(subRangeIndices.count() - 1) {
                iter.moveNext()
            }

            val subRange = iter.subRange()

            val expectedSubFragments = fragments.subList(subRangeIndices.first, subRangeIndices.last + 1)
            val actualSubFragments = subRange.fragments().toList()

            assertContentEquals(expectedSubFragments, actualSubFragments)
        }

        subRangeTestData().forEach { testCase(it.ranges, it.subRangeIndices) }
    }
}