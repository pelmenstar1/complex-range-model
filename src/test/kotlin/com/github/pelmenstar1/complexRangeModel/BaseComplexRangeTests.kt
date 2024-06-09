package com.github.pelmenstar1.complexRangeModel

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse

abstract class BaseComplexRangeTests {
    class DifferentClassComplexRange<T : FragmentElement<T>>(
        private val fragments: List<RangeFragment<T>>
    ) : ComplexRange<T> {
        override fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T> {
            throw NotImplementedError()
        }

        override fun fragments(): ComplexRangeFragmentList<T> = FragmentListImpl()

        inner class FragmentListImpl : ComplexRangeFragmentList<T> {
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
                FragmentIterator()
        }

        private inner class FragmentIterator : ComplexRangeFragmentListIterator<T> {
            private var _current: RangeFragment<T>? = null
            override val current: RangeFragment<T>
                get() = _current ?: throw NoSuchElementException()

            private var index = 0
            override fun moveNext(): Boolean {
                if (index < fragments.size) {
                    _current = fragments[index++]
                    return true
                }

                return false
            }

            override fun movePrevious(): Boolean {
                if (index > 0) {
                    _current = fragments[index--]
                    return true
                }

                return false
            }

            override fun mark() = throw NotImplementedError()
            override fun subRange() = throw NotImplementedError()
        }
    }

    abstract fun createComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): IntComplexRange

    private fun createComplexRange(ranges: Array<IntRange>): IntComplexRange {
        return createComplexRange {
            ranges.forEach { fragment(it) }
        }
    }

    @Test
    fun includesTest() {
        fun testCase(fragments: Array<IntRange>, targetRange: IntRange, expected: Boolean) {
            val complexRange = createComplexRange(fragments)
            val actual = complexRange.fragments().includes(IntRangeFragment(targetRange))

            assertEquals(expected, actual)
        }

        testCase(fragments = arrayOf(1..3), targetRange = 1..3, expected = true)
        testCase(fragments = arrayOf(1..5), targetRange = 2..4, expected = true)
        testCase(fragments = arrayOf(1..3, 5..6), targetRange = 8..9, expected = false)
        testCase(fragments = arrayOf(3..5), targetRange = 2..3, expected = false)
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
            initialRanges = arrayOf(1..2, 4..5),
            unsetRange = 0..6,
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

    // We're testing whether the equals() correctly handles the 'equals to null' case
    @Suppress("SENSELESS_COMPARISON")
    @Test
    fun equalsNullTest() {
        val complexRange = createComplexRange(emptyArray())

        val actual = complexRange == null
        assertFalse(actual)
    }

    @Test
    fun equalsSameClassTest() {
        fun testCase(fragments: Array<IntRange>, otherFragments: Array<IntRange>, expected: Boolean) {
            val complexRange = createComplexRange(fragments)
            val otherComplexRange = createComplexRange(otherFragments)

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
            val complexRange = createComplexRange(fragments)
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

        val complexRange1 = createComplexRange(fragments)
        val complexRange2 = createComplexRange(fragments)

        val hash1 = complexRange1.hashCode()
        val hash2 = complexRange2.hashCode()

        assertEquals(hash1, hash2)
    }

    @Test
    fun subRangeHashCodeTest() {
        val complexRange0 = createComplexRange(arrayOf(1..3, 6..9, 11..12))
        val frIterator0 = complexRange0.fragments().fragmentIterator()
        frIterator0.moveNext()
        frIterator0.moveNext()
        frIterator0.mark()
        val complexRange1 = frIterator0.subRange()
        val complexRange2 = createComplexRange(arrayOf(6..9))

        val hash1 = complexRange1.hashCode()
        val hash2 = complexRange2.hashCode()

        assertEquals(hash1, hash2)
    }
}