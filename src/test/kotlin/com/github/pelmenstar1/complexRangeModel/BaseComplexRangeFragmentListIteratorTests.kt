package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertEquals

private typealias FragmentIterator = ComplexRangeFragmentListIterator<IntFragmentElement>

abstract class BaseComplexRangeFragmentListIteratorTests {
    protected abstract fun createIterator(data: Array<IntRangeFragment>): FragmentIterator

    @Test
    fun iterateForwardBackwardTest() {
        fun forwardPass(iter: FragmentIterator, expectedElements: Array<IntRangeFragment>) {
            var index = 0

            do {
                val current = iter.current
                val expected = expectedElements[index++]

                assertEquals(expected, current)
            } while(iter.moveNext())

            assertEquals(expectedElements.size, index)
        }

        fun backwardPass(iter: FragmentIterator, expectedElements: Array<IntRangeFragment>) {
            var index = expectedElements.size - 1

            do {
                val current = iter.current
                val expected = expectedElements[index--]

                assertEquals(expected, current)
            } while(iter.movePrevious())

            assertEquals(-1, index)
        }

        fun testCase(elements: Array<IntRange>) {
            val expectedFragments = elements.mapToArray { IntRangeFragment(it) }

            val iter = createIterator(expectedFragments)
            iter.moveNext()

            forwardPass(iter, expectedFragments)
            backwardPass(iter, expectedFragments)
            forwardPass(iter, expectedFragments)
        }

        testCase(arrayOf(1..2))
        testCase(arrayOf(1..2, 4..5))
        testCase(arrayOf(1..2, 4..5, 7..8))
    }
}