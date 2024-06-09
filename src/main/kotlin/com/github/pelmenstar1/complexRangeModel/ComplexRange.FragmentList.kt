package com.github.pelmenstar1.complexRangeModel

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

fun<T : FragmentElement<T>> ComplexRangeFragmentListIterator<T>.toListIterator(): ListIterator<RangeFragment<T>> {
    return FragmentListIteratorToListIterator(this)
}

internal fun ComplexRangeFragmentListIterator<*>.contentEquals(
    otherIterator: ComplexRangeFragmentListIterator<*>
): Boolean {
    while(true) {
        val moveRes = moveNext()
        val otherMoveRes = otherIterator.moveNext()

        if (!moveRes || !otherMoveRes) {
            return moveRes == otherMoveRes
        }

        if (current != otherIterator.current) {
            return false
        }
    }

    return true
}

private class FragmentListIteratorToListIterator<T : FragmentElement<T>>(
    private val iterator: ComplexRangeFragmentListIterator<T>
) : ListIterator<RangeFragment<T>> {
    private var lastMoveNextResult: Int = DIRTY
    private var index = 0

    private fun doMoveNext(): Boolean {
        return iterator.moveNext().also {
            lastMoveNextResult = if (it) HAS_ELEMENTS else NO_ELEMENTS
        }
    }

    private fun getLastMoveNextResult(): Boolean {
        val result = lastMoveNextResult
        if (result == DIRTY) {
            return doMoveNext()
        }

        return result == HAS_ELEMENTS
    }

    override fun hasNext(): Boolean {
        return getLastMoveNextResult()
    }

    override fun hasPrevious(): Boolean {
        return index > 0
    }

    override fun next(): RangeFragment<T> {
        if (getLastMoveNextResult()) {
            val current = iterator.current

            index++
            doMoveNext()

            return current
        }

        throw NoSuchElementException()
    }

    override fun previous(): RangeFragment<T> {
        val result = iterator.movePrevious()
        if (!result) {
            throw IndexOutOfBoundsException()
        }

        index--
        lastMoveNextResult = DIRTY

        return iterator.current
    }

    override fun nextIndex(): Int = index
    override fun previousIndex(): Int = index - 1

    companion object {
        private const val HAS_ELEMENTS = 0
        private const val NO_ELEMENTS = 1
        private const val DIRTY = 2
    }
}

private object EmptyComplexRangeFragmentListIterator : ComplexRangeFragmentListIterator<Nothing> {
    override val current: RangeFragment<Nothing>
        get() = throw NoSuchElementException()

    override fun moveNext(): Boolean = false
    override fun movePrevious(): Boolean = false

    override fun mark() {
    }

    override fun subRange(): ComplexRange<Nothing> = ComplexRange.empty()
}

/**
 * Represents a list of [RangeFragment] that is specialized for use in [ComplexRange].
 */
interface ComplexRangeFragmentList<T : FragmentElement<T>> : List<RangeFragment<T>> {
    /**
     * Determines whether the list contains all elements from given [fragment].
     *
     * It differs from `fragments().contains(fragment)` in that `contains` tries to find the exact match.
     * So that:
     * ```kotlin
     * var complexRange = ComplexRange(arrayOf(1..5))
     * complexRange.fragments().contains(IntRangeFragment(2..4)) // = false
     * complexRange.includes(IntRangeFragment(2..4)) // = true
     * ```
     */
    fun includes(fragment: RangeFragment<T>): Boolean {
        return any { it.containsCompletely(fragment) }
    }

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

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun<T : FragmentElement<T>> empty(): ComplexRangeFragmentList<T> {
            return EmptyComplexRangeFragmentList as ComplexRangeFragmentList<T>
        }
    }
}

private object EmptyComplexRangeFragmentList : ComplexRangeFragmentList<Nothing> {
    override val size: Int
        get() = 0

    override fun isEmpty(): Boolean = true

    override fun get(index: Int): RangeFragment<Nothing> = throw IndexOutOfBoundsException()

    override fun includes(fragment: RangeFragment<Nothing>): Boolean = false

    override fun last(): RangeFragment<Nothing> = throw IndexOutOfBoundsException()

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