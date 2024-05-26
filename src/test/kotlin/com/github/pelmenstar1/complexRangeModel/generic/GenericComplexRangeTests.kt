package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class GenericComplexRangeTests : BaseComplexRangeTests() {
    class DifferentClassComplexRange<T : FragmentElement<T>>(
        private val fragments: List<RangeFragment<T>>
    ) : ComplexRange<T> {
        override fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T> {
            throw NotImplementedError()
        }

        override fun fragments(): ComplexRangeFragmentList<T> =
            FragmentListImpl(fragments)

        class FragmentListImpl<T : FragmentElement<T>>(
            private val fragments: List<RangeFragment<T>>
        ) : ComplexRangeFragmentList<T> {
            override val size: Int
                get() = fragments.size

            override fun isEmpty(): Boolean = fragments.isEmpty()

            override fun containsAll(elements: Collection<RangeFragment<T>>): Boolean =
                fragments.containsAll(elements)

            override fun contains(element: RangeFragment<T>): Boolean =
                fragments.contains(element)

            override fun get(index: Int): RangeFragment<T> =
                fragments[index]

            override fun indexOf(element: RangeFragment<T>): Int =
                fragments.indexOf(element)

            override fun lastIndexOf(element: RangeFragment<T>): Int =
                fragments.lastIndexOf(element)

            override fun subList(fromIndex: Int, toIndex: Int): List<RangeFragment<T>> =
                throw NotImplementedError()

            override fun iterator(): Iterator<RangeFragment<T>> =
                fragments.iterator()

            override fun listIterator(): ListIterator<RangeFragment<T>> =
                fragments.listIterator()

            override fun listIterator(index: Int): ListIterator<RangeFragment<T>> =
                fragments.listIterator(index)

            override fun fragmentIterator(): ComplexRangeFragmentListIterator<T> =
                throw NotImplementedError()
        }
    }

    // We're testing whether the equals() correctly handles the 'equals to null' case
    @Suppress("SENSELESS_COMPARISON")
    @Test
    fun equalsNullTest() {
        val complexRange = ComplexRange.empty<IntFragmentElement>()

        val actual = complexRange == null
        assertFalse(actual)
    }

    @Test
    fun equalsSameClassTest() {
        fun testCase(fragments: Array<IntRange>, otherFragments: Array<IntRange>, expected: Boolean) {
            val complexRange = IntComplexRange(fragments)
            val otherComplexRange = IntComplexRange(otherFragments)

            val actual = complexRange == otherComplexRange
            assertEquals(expected, actual)
        }

        val fragments0 = emptyArray<IntRange>()
        val fragments1 = arrayOf(1..2)
        val fragments2 = arrayOf(1..2, 5..7)
        val fragments3 = arrayOf(2..3)

        testCase(fragments0, fragments0, expected = true)
        testCase(fragments1, fragments1, expected = true)
        testCase(fragments1, fragments2, expected = false)
        testCase(fragments2, fragments1, expected = false)
        testCase(fragments2, fragments3, expected = false)
        testCase(fragments2, fragments2, expected = true)
    }

    @Test
    fun equalsDifferentClassTest() {
        fun testCase(fragments: Array<IntRange>, otherFragments: Array<IntRange>, expected: Boolean) {
            val complexRange = IntComplexRange(fragments)
            val otherComplexRange = DifferentClassComplexRange(otherFragments.map { IntRangeFragment(it) })

            // Order of operations is important.
            // GenericComplexRange must be compared to DifferentClassComplexRange and not vice versa.
            val actual = complexRange == otherComplexRange

            assertEquals(expected, actual)
        }

        val fragments0 = emptyArray<IntRange>()
        val fragments1 = arrayOf(1..2)
        val fragments2 = arrayOf(1..2, 5..7)
        val fragments3 = arrayOf(2..3)

        testCase(fragments0, fragments0, expected = true)
        testCase(fragments1, fragments1, expected = true)
        testCase(fragments1, fragments2, expected = false)
        testCase(fragments2, fragments1, expected = false)
        testCase(fragments2, fragments3, expected = false)
        testCase(fragments2, fragments2, expected = true)
    }

    @Test
    fun hashCodeSameDataTest() {
        val fragments = arrayOf(1..3, 6..9)

        val complexRange1 = IntComplexRange(fragments)
        val complexRange2 = IntComplexRange(fragments)

        val hash1 = complexRange1.hashCode()
        val hash2 = complexRange2.hashCode()

        assertEquals(hash1, hash2)
    }

    override fun createComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): IntComplexRange {
        return IntComplexRange(block)
    }
}