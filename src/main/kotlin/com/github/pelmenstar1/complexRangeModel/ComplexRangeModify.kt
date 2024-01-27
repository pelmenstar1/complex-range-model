package com.github.pelmenstar1.complexRangeModel

interface ComplexRangeModify<T : FragmentElement<T>> {
    fun set(fragment: RangeFragment<T>)
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