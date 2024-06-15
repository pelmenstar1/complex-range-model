package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertEquals

class IteratorsTests {
    @Test
    fun sequenceEqualsTest() {
        fun testCase(first: Array<Int>, second: Array<Int>, expected: Boolean) {
            val actual = first.asIterable().sequenceEquals(second.asIterable())

            assertEquals(expected, actual)
        }

        testCase(first = emptyArray(), second = emptyArray(), expected = true)
        testCase(first = arrayOf(0), second = emptyArray(), expected = false)
        testCase(first = emptyArray(), second = arrayOf(0), expected = false)
        testCase(first = arrayOf(0), second = arrayOf(0), expected = true)
        testCase(first = arrayOf(0), second = arrayOf(1), expected = false)
        testCase(first = arrayOf(0, 1), second = arrayOf(0, 1), expected = true)
        testCase(first = arrayOf(0, 1), second = arrayOf(0), expected = false)
        testCase(first = arrayOf(0), second = arrayOf(0, 1), expected = false)
    }

    @Test
    fun limitToForwardBackwardTest() {
        fun forwardTest(iterator: ListIterator<Int>, expectedElements: List<Int>) {
            var index = 0
            while(iterator.hasNext()) {
                val expectedNextIndex = index
                val actualNextIndex = iterator.nextIndex()

                assertEquals(expectedNextIndex, actualNextIndex)

                val actualElement = iterator.next()
                val expectedElement = expectedElements[index]

                assertEquals(expectedElement, actualElement)

                index++
            }

            assertEquals(expectedElements.size, index)
        }

        fun backwardTest(iterator: ListIterator<Int>, expectedElements: List<Int>) {
            var index = expectedElements.size - 1

            while(iterator.hasPrevious()) {
                val actualPrevIndex = iterator.previousIndex()

                assertEquals(index, actualPrevIndex)

                val actualElement = iterator.previous()
                val expectedElement = expectedElements[index]

                assertEquals(expectedElement, actualElement)

                index--
            }

            assertEquals(-1, index)
        }

        fun testCase(elements: List<Int>, limitingSize: Int) {
            val expectedElements = elements.subList(0, limitingSize)
            val iterator = elements.listIterator().limitTo(limitingSize)

            forwardTest(iterator, expectedElements)
            backwardTest(iterator, expectedElements)
            forwardTest(iterator, expectedElements)
        }

        testCase(elements = listOf(1), limitingSize = 1)
        testCase(elements = listOf(1, 2), limitingSize = 1)
        testCase(elements = listOf(1, 2), limitingSize = 2)
        testCase(elements = listOf(1, 2, 3), limitingSize = 1)
        testCase(elements = listOf(1, 2, 3), limitingSize = 2)
        testCase(elements = listOf(1, 2, 3), limitingSize = 3)
    }
}