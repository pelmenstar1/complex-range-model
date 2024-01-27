package com.github.pelmenstar1.complexRangeModel

interface ComplexRangeBuilder<T : FragmentElement<T>> {
    fun fragment(value: RangeFragment<T>)
}

fun ComplexRangeBuilder<IntFragmentElement>.fragment(range: IntRange) {
    fragment(range.first, range.last)
}

fun ComplexRangeBuilder<IntFragmentElement>.fragment(start: Int, endInclusive: Int) {
    fragment(IntRangeFragment(start, endInclusive))
}