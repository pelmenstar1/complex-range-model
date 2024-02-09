package com.github.pelmenstar1.complexRangeModel

import com.github.pelmenstar1.complexRangeModel.bits.BitArrayComplexRangeBuilder
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRange
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRangeBuilder
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRangeModify

/**
 * An iterator over certain fragments.
 *
 * This iterator is defined in different way than standard [Iterator] interface.
 * This definition gives a notion of the "current" element.
 * It greatly simplifies the usage of [mark] and [subRange],
 * because it is more intuitive than the standard [Iterator]'s "last element returned by next() or previous()"
 */
interface ComplexRangeFragmentListIterator<T : FragmentElement<T>> {
    /**
     * Gets a current fragment of the iterator.
     *
     * On the initial construction, there's no "current" element before the first call of [moveNext].
     * The implementation should throw an exception in that case.
     */
    val current: RangeFragment<T>

    /**
     * Moves to the next element.
     * Returns true if there's such an element.
     * If the method returns false, the [current] should still return the same element.
     */
    fun moveNext(): Boolean

    /**
     * Moves to the previous element.
     * Returns true if there's such an element.
     * If the method returns false, the [current] should still return the same element.
     */
    fun movePrevious(): Boolean

    /**
     * Marks [current] fragment of the iterator. If there's no such element, the method does nothing.
     */
    fun mark()

    /**
     * Returns a sub-range of the [ComplexRange] instance that is iterated by this iterator.
     * The start fragment is the marked one (by [mark] method), the end fragment is the [current] fragment.
     */
    fun subRange(): ComplexRange<T>
}

private object EmptyComplexRangeFragmentListIterator : ComplexRangeFragmentListIterator<Nothing> {
    override val current: RangeFragment<Nothing>
        get() = throw NoSuchElementException()

    override fun moveNext(): Boolean = false
    override fun movePrevious(): Boolean = false

    override fun mark() {
    }

    override fun subRange(): ComplexRange<Nothing> = EmptyComplexRange
}

/**
 * Represents a list of [RangeFragment] that is specialized for use in [ComplexRange].
 */
interface ComplexRangeFragmentList<T : FragmentElement<T>> : List<RangeFragment<T>> {
    /**
     * Returns the last fragment of the list, if the list is not empty.
     *
     * Implementations may implement the method in more effective way.
     */
    fun last(): RangeFragment<T> {
        return this[lastIndex]
    }

    /**
     * Returns an iterator over the fragments of this list.
     */
    fun fragmentIterator(): ComplexRangeFragmentListIterator<T>
}

private object EmptyComplexRangeFragmentList : ComplexRangeFragmentList<Nothing> {
    override val size: Int
        get() = 0

    override fun isEmpty(): Boolean = true

    override fun get(index: Int): RangeFragment<Nothing> = throw IndexOutOfBoundsException()

    override fun subList(fromIndex: Int, toIndex: Int): List<RangeFragment<Nothing>> {
        return if (fromIndex == 0 && toIndex == 0) this else throw IndexOutOfBoundsException()
    }

    override fun contains(element: RangeFragment<Nothing>): Boolean = false

    override fun containsAll(elements: Collection<RangeFragment<Nothing>>): Boolean = elements.isEmpty()

    override fun indexOf(element: RangeFragment<Nothing>): Int = -1
    override fun lastIndexOf(element: RangeFragment<Nothing>): Int = -1

    override fun iterator(): Iterator<Nothing> = emptyIterator()
    override fun listIterator(): ListIterator<Nothing> = emptyIterator()
    override fun listIterator(index: Int): ListIterator<Nothing> = emptyIterator()

    override fun fragmentIterator(): ComplexRangeFragmentListIterator<Nothing> = EmptyComplexRangeFragmentListIterator
}

/**
 * Represents a union of simple ranges (fragments), i.e `[a, b] = { x in T | a <= x <= b }`.
 *
 * The implementations of the interface is expected to be immutable. To change a range, use [ComplexRange.modify].
 */
interface ComplexRange<T : FragmentElement<T>> {
    /**
     * Modifies the current range using given [block] lambda and returns a new instance [ComplexRange] with changes.
     * The method doesn't change current instance of [ComplexRange].
     */
    fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T>

    /**
     * Returns a list of fragments. The list must satisfy certain rules:
     * - The fragments must not overlap.
     * - The fragments must be in ascending order, i.e. `fragments[i].start < fragments[i + 1].start`,
     *   where `i` is element of `[0; n - 2]` and `n` is the total amount of the fragments
     */
    fun fragments(): ComplexRangeFragmentList<T>

    /**
     * Returns a collection of all elements of the [fragments].
     *
     * The method is implemented by default with the implementation that returns a new instance of [Collection] each time the method is called.
     * So
     */
    fun elements(): Collection<T> {
        return ComplexRangeElementCollection(fragments())
    }

    companion object {
        /**
         * Returns an instance of [ComplexRange] that contains no fragments.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : FragmentElement<T>> empty() = EmptyComplexRange as ComplexRange<T>
    }
}

typealias IntComplexRange = ComplexRange<IntFragmentElement>

private object EmptyComplexRange : ComplexRange<Nothing> {
    override fun modify(block: ComplexRangeModify<Nothing>.() -> Unit): ComplexRange<Nothing> {
        val fragments = RawLinkedList<RangeFragment<Nothing>>()
        GenericComplexRangeModify(fragments).also(block)

        return GenericComplexRange(fragments)
    }

    override fun fragments(): ComplexRangeFragmentList<Nothing> = EmptyComplexRangeFragmentList
    override fun elements(): Collection<Nothing> = emptyList()

    override fun equals(other: Any?): Boolean {
        if (other === EmptyComplexRange) {
            return true
        }

        return other is ComplexRange<*> && other.fragments().isEmpty()
    }

    override fun hashCode(): Int = 1

    override fun toString(): String = "ComplexRange()"
}

/**
 * Returns a new [ComplexRange] created using given [block] lambda.
 */
inline fun <T : FragmentElement<T>> ComplexRange(block: ComplexRangeBuilder<T>.() -> Unit): ComplexRange<T> {
    return GenericComplexRangeBuilder<T>().also(block).build()
}

/**
 * Returns a new [ComplexRange] using given [fragments] array.
 *
 * @param fragments the array of [RangeFragment] to build [ComplexRange] from.
 * The array is **not** required to contain mutually non-intersecting fragments and the array can be in any order.
 */
fun <T : FragmentElement<T>> ComplexRange(fragments: Array<out RangeFragment<T>>): ComplexRange<T> {
    return ComplexRange {
        fragments.forEach { fragment(it) }
    }
}

/**
 * Returns a new [ComplexRange] using given [fragments].
 *
 * @param fragments the iterable of [RangeFragment] to build [ComplexRange] from.
 * The elements are **not** required to be mutually non-intersecting and the elements can be in any order.
 */
fun<T : FragmentElement<T>> ComplexRange(fragments: Iterable<RangeFragment<T>>): ComplexRange<T> {
    return ComplexRange {
        fragments.forEach { fragment(it) }
    }
}

/**
 * Returns a new [ComplexRange] created by given [block] lambda.
 *
 * A bit-array is used during the construction,
 * so this builder-method is more effective default [ComplexRange] one that uses linked list internally.
 * Note that more absolute difference between [limitStart] and [limitEnd], more memory is allocated,
 * regardless of the actual amount of fragments.
 * So this method of the building might not be suitable in all situations.
 */
inline fun BitIntComplexRange(
    limitStart: Int,
    limitEnd: Int,
    block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit
): IntComplexRange {
    return BitArrayComplexRangeBuilder(limitStart, limitEnd).also(block).build()
}

/**
 * Same of [ComplexRange] builder method with lambda argument. It's used for easier reading.
 */
inline fun IntComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): IntComplexRange {
    return ComplexRange(block)
}

/**
 * Creates a new [ComplexRange] instance using given [ranges] array.
 *
 * @param ranges an array of [IntRange] to create [IntRangeFragment] from.
 * The array is **not** required to contain mutually non-intersecting fragments and the array can be in any order.
 */
fun IntComplexRange(ranges: Array<out IntRange>): IntComplexRange {
    return IntComplexRange {
        ranges.forEach { fragment(it) }
    }
}




