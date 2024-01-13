package com.github.pelmenstar1.complexRangeModel

interface RangeFragmentSupport<T : Comparable<T>> {
    fun next(value: T): T
    fun previous(value: T): T

    fun createFragment(start: T, endInclusive: T): RangeFragment<T>
}

object IntRangeFragmentSupport : RangeFragmentSupport<Int> {
    override fun next(value: Int) = value + 1
    override fun previous(value: Int) = value - 1

    override fun createFragment(start: Int, endInclusive: Int): RangeFragment<Int> {
        return IntRangeFragment(start, endInclusive)
    }
}