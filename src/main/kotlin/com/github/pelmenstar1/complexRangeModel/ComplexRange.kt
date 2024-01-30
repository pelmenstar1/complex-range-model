package com.github.pelmenstar1.complexRangeModel

import com.github.pelmenstar1.complexRangeModel.bits.BitArrayComplexRangeBuilder
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRange
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRangeBuilder
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRangeModify

interface ComplexRangeFragmentListIterator<T : FragmentElement<T>> {
    val current: RangeFragment<T>

    fun moveNext(): Boolean
    fun movePrevious(): Boolean

    fun mark()
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

interface ComplexRangeFragmentList<T : FragmentElement<T>> : List<RangeFragment<T>> {
    fun last(): RangeFragment<T> {
        return this[lastIndex]
    }

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

interface ComplexRange<T : FragmentElement<T>> {
    fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T>

    fun fragments(): ComplexRangeFragmentList<T>

    fun elements(): Collection<T> {
        return ComplexRangeElementCollection(fragments())
    }

    companion object {
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

inline fun <T : FragmentElement<T>> ComplexRange(block: ComplexRangeBuilder<T>.() -> Unit): ComplexRange<T> {
    return GenericComplexRangeBuilder<T>().also(block).build()
}

fun <T : FragmentElement<T>> ComplexRange(fragments: Array<out RangeFragment<T>>): ComplexRange<T> {
    return ComplexRange {
        fragments.forEach { fragment(it) }
    }
}

fun<T : FragmentElement<T>> ComplexRange(fragments: Iterable<RangeFragment<T>>): ComplexRange<T> {
    return ComplexRange {
        fragments.forEach { fragment(it) }
    }
}

inline fun BitIntComplexRange(
    limitStart: Int,
    limitEnd: Int,
    block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit
): IntComplexRange {
    return BitArrayComplexRangeBuilder(limitStart, limitEnd).also(block).build()
}

inline fun IntComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): IntComplexRange {
    return ComplexRange(block)
}

fun IntComplexRange(ranges: Array<out IntRange>): IntComplexRange {
    return IntComplexRange {
        ranges.forEach { fragment(it) }
    }
}




