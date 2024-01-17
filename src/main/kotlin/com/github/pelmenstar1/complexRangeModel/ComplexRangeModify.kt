package com.github.pelmenstar1.complexRangeModel

interface ComplexRangeModify<T : Comparable<T>> {
    fun set(fragment: RangeFragment<T>)
    fun unset(fragment: RangeFragment<T>)
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