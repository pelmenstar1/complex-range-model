package com.github.pelmenstar1.complexRangeModel

class ComplexRangeModify<T : Comparable<T>>(fragments: RangeFragmentList<T>) : ComplexRangeBaseBuilder<T>(fragments) {
    fun set(fragment: RangeFragment<T>) {
        includeFragment(fragment)
    }

    fun unset(fragment: RangeFragment<T>) {
        excludeFragment(fragment)
    }
}

fun ComplexRangeModify<Int>.set(start: Int, endInclusive: Int) {
    set(IntRangeFragment(start, endInclusive))
}

fun ComplexRangeModify<Int>.set(range: IntRange) {
    set(range.first, range.last)
}

fun ComplexRangeModify<Int>.unset(start: Int, endInclusive: Int) {
    unset(IntRangeFragment(start, endInclusive))
}

fun ComplexRangeModify<Int>.unset(range: IntRange) {
    unset(range.first, range.last)
}