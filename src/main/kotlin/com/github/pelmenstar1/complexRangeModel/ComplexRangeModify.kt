package com.github.pelmenstar1.complexRangeModel

/**
 * An interface that is used for modifying previously constructed [ComplexRange].
 */
interface ComplexRangeModify<T : FragmentElement<T>> {
    /**
     * Adds values of given [fragment] to the complex range.
     *
     * This fragment might intersect with, or be equal to other (more than one) fragments.
     * The implementation is expected to handle these cases in an appropriate way - some fragments might be united into one.
     */
    fun set(fragment: RangeFragment<T>)

    /**
     * Removes values of given [fragment] from the complex range.
     *
     * This fragment might intersect with, or be equal to other (more than one) fragments.
     * The implementation is expected to handle these cases in an appropriate way - some fragments might be removed or split into multiple fragments.
     */
    fun unset(fragment: RangeFragment<T>)
}

fun ComplexRangeModify<IntFragmentElement>.set(start: Int, endInclusive: Int) {
    set(IntRangeFragment(start, endInclusive))
}

fun ComplexRangeModify<IntFragmentElement>.set(range: IntRange) {
    set(range.first, range.last)
}

fun ComplexRangeModify<IntFragmentElement>.unset(start: Int, endInclusive: Int) {
    unset(IntRangeFragment(start, endInclusive))
}

fun ComplexRangeModify<IntFragmentElement>.unset(range: IntRange) {
    unset(range.first, range.last)
}