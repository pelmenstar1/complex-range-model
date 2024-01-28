package com.github.pelmenstar1.complexRangeModel

import com.github.pelmenstar1.complexRangeModel.bits.BitArrayComplexRangeBuilder
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRange
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRangeBuilder
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRangeModify

interface ComplexRange<T : FragmentElement<T>> {
    fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T>

    fun fragments(): List<RangeFragment<T>>

    fun elements(): Collection<T> {
        return ComplexRangeElementCollection(fragments())
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : FragmentElement<T>> empty() = EmptyComplexRange as ComplexRange<T>
    }
}

private object EmptyComplexRange : ComplexRange<Nothing> {
    override fun modify(block: ComplexRangeModify<Nothing>.() -> Unit): ComplexRange<Nothing> {
        val fragments = RawLinkedList<RangeFragment<Nothing>>()
        GenericComplexRangeModify(fragments).also(block)

        return GenericComplexRange(fragments)
    }

    override fun fragments(): List<RangeFragment<Nothing>> = emptyList()
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

inline fun BitIntComplexRange(
    limitStart: Int,
    limitEnd: Int,
    block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit
): ComplexRange<IntFragmentElement> {
    return BitArrayComplexRangeBuilder(limitStart, limitEnd).also(block).build()
}

inline fun IntComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): ComplexRange<IntFragmentElement> {
    return ComplexRange(block)
}

fun IntComplexRange(ranges: Array<out IntRange>): ComplexRange<IntFragmentElement> {
    return IntComplexRange {
        ranges.forEach { fragment(it) }
    }
}




