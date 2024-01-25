package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertSame

abstract class BaseTwoWayIteratorTests<T> {
    class SubIteratorData<T>(val elements: Array<T>, val subRange: IntRange)
    class FillArrayData<T>(val elements: Array<T>, val fillRange: IntRange)

    abstract fun createIterator(elements: Array<T>): TwoWayIterator<T>
    abstract fun iterateForwardBackwardDataset(): List<Array<T>>
    abstract fun subIteratorDataset(): List<SubIteratorData<T>>
    abstract fun fillArrayDataset(): List<FillArrayData<T>>
    abstract fun createArray(size: Int): Array<T?>

    private fun forwardIteratorTest(iter: TwoWayIterator<T>, expectedValues: Array<T>) {
        var index = 0
        while (iter.hasNext()) {
            val nextIndex = iter.nextIndex()
            assertEquals(index, nextIndex, "next index")

            val iterValue = iter.next()
            val expectedValue = expectedValues[index++]

            assertSame(expectedValue, iterValue, "next value")
        }

        assertEquals(expectedValues.size, index)
    }

    private fun backwardIteratorTest(iter: TwoWayIterator<T>, expectedValues: Array<T>) {
        var index = expectedValues.size - 1

        while (iter.hasPrevious()) {
            val prevIndex = iter.previousIndex()
            assertEquals(index, prevIndex, "previous index")

            val iterValue = iter.previous()
            val expectedValue = expectedValues[index--]

            assertSame(expectedValue, iterValue, "previous value")
        }

        assertEquals(-1, index)
    }

    private fun iteratorTestHelper(iter: TwoWayIterator<T>, expectedValues: Array<T>) {
        assertEquals(expectedValues.size, iter.size)

        forwardIteratorTest(iter, expectedValues)
        backwardIteratorTest(iter, expectedValues)
        forwardIteratorTest(iter, expectedValues)
    }

    @Test
    fun iterateForwardBackwardTest() {
        for (data in iterateForwardBackwardDataset()) {
            val iter = createIterator(data)

            iteratorTestHelper(iter, data)
        }
    }

    @Test
    fun subIteratorTest() {
        fun testCase(data: SubIteratorData<T>) {
            val iter = createIterator(data.elements)
            val subRange = data.subRange

            iter.skip(subRange.first)
            iter.mark()
            iter.skip(subRange.last - subRange.first + 1)

            val subIter = iter.subIterator()
            val expectedSubRange = data.elements.copyOfRange(subRange.first, subRange.last + 1)

            iteratorTestHelper(subIter, expectedSubRange)
        }

        for (data in subIteratorDataset()) {
            testCase(data)
        }
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun fillArrayTest() {
        fun testCase(data: FillArrayData<T>) {
            val elements = data.elements
            val range = data.fillRange

            val expectedValues = elements.copyOfRange(range.first, range.last + 1)
            val length = range.last - range.first + 1

            val iter = createIterator(elements)
            iter.skip(range.first)

            val actualValues = createArray(length)
            iter.fillArray(actualValues)

            assertContentEquals(expectedValues, actualValues as Array<T>)
        }

        fillArrayDataset().forEach { testCase(it) }
    }
}