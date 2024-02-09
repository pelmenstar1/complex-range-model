package com.github.pelmenstar1.complexRangeModel

/**
 * An interface for builder-like objects that are used for constructing [ComplexRange].
 */
interface ComplexRangeBuilder<T : FragmentElement<T>> {
    /**
     * Adds given fragment to the [ComplexRange].
     *
     * This fragment might intersect with, or be equal to other fragments.
     * The implementation is expected to handle these cases in an appropriate way - some fragments might be united into one.
     */
    fun fragment(value: RangeFragment<T>)
}

/**
 * Fast-path for adding a new fragment to the builder.
 *
 * @param range a range to create [IntRangeFragment] from.
 */
fun ComplexRangeBuilder<IntFragmentElement>.fragment(range: IntRange) {
    fragment(range.first, range.last)
}

/**
 * Fast-path for adding a new fragment to the builder.
 */
fun ComplexRangeBuilder<IntFragmentElement>.fragment(start: Int, endInclusive: Int) {
    fragment(IntRangeFragment(start, endInclusive))
}