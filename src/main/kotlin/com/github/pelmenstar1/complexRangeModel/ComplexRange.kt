package com.github.pelmenstar1.complexRangeModel

import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRange
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRangeModify

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

    override fun fragments(): ComplexRangeFragmentList<Nothing> = ComplexRangeFragmentList.empty()
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




