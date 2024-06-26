package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertEquals

class ComplexRangeFragmentListIteratorTests {
    class TestFragmentListIterator(
        private val ranges: List<IntRange>
    ) : ComplexRangeFragmentListIterator<IntFragmentElement> {
        private var _current: IntRangeFragment? = null
        override val current: IntRangeFragment
            get() = _current ?: throw NoSuchElementException()

        private var index = 0

        override fun moveNext(): Boolean {
            if (index >= ranges.size) {
                return false
            }

            _current = IntRangeFragment(ranges[index])
            index++

            return true
        }

        override fun movePrevious(): Boolean {
            if (index <= 0) {
                return false
            }

            index--
            _current = IntRangeFragment(ranges[index])

            return true
        }

        override fun mark() = throw NotImplementedError()
        override fun subRange() = throw NotImplementedError()
    }

    private fun<T> forwardPass(iterator: ListIterator<T>, expected: List<T>) {
        var index = 0

        while(iterator.hasNext()) {
            val nextIndex = iterator.nextIndex()
            assertEquals(index, nextIndex)

            val actualValue = iterator.next()
            val expectedValue = expected[index]

            assertEquals(expectedValue, actualValue)
            index++
        }

        assertEquals(expected.size, index)
    }

    private fun<T> backwardPass(iterator: ListIterator<T>, expected: List<T>) {
        var index = expected.size - 1

        while(iterator.hasPrevious()) {
            val prevIndex = iterator.previousIndex()
            assertEquals(index, prevIndex)

            val actualValue = iterator.previous()
            val expectedValue = expected[index]

            assertEquals(expectedValue, actualValue)
            index--
        }

        assertEquals(-1, index)
    }

    @Test
    fun toListIteratorTest() {
        fun testCase(values: List<IntRange>) {
            val fragments = values.map { IntRangeFragment(it) }
            val iterator = TestFragmentListIterator(values).toListIterator()

            forwardPass(iterator, fragments)
            backwardPass(iterator, fragments)
            forwardPass(iterator, fragments)
        }

        testCase(emptyList())
        testCase(listOf(1..1))
        testCase(listOf(1..1, 3..3))
        testCase(listOf(1..1, 3..3, 5..5))
    }
}