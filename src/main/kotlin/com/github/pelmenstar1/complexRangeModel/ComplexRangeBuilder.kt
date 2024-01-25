package com.github.pelmenstar1.complexRangeModel

interface ComplexRangeBuilder<T> {
    fun fragment(value: RangeFragment<T>)
}

fun ComplexRangeBuilder<Int>.fragment(range: IntRange) {
    fragment(range.first, range.last)
}

fun ComplexRangeBuilder<Int>.fragment(start: Int, endInclusive: Int) {
    fragment(IntRangeFragment(start, endInclusive))
}