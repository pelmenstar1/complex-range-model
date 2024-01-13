package com.github.pelmenstar1.complexRangeModel

class ComplexRangeBuilder<T : Comparable<T>>(
    support: RangeFragmentSupport<T>
) : ComplexRangeBaseBuilder<T>(support) {
    fun fragment(value: RangeFragment<T>) {
        includeFragment(value)
    }

    fun build(): ComplexRange<T> {
        return ComplexRange(fragments)
    }
}

fun ComplexRangeBuilder<Int>.fragment(range: IntRange) {
    fragment(IntRangeFragment(range))
}

fun ComplexRangeBuilder<Int>.fragment(start: Int, endInclusive: Int) {
    fragment(IntRangeFragment(start, endInclusive))
}