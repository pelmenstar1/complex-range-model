package com.github.pelmenstar1.complexRangeModel

interface FragmentElementSupport<T> {
    fun compare(a: T, b: T): Int
    fun isNextToOther(a: T, b: T): Boolean
    fun previousValue(value: T): T
    fun nextValue(value: T): T
}

object IntFragmentElementSupport : FragmentElementSupport<Int> {
    override fun compare(a: Int, b: Int): Int = a.compareTo(b)
    override fun isNextToOther(a: Int, b: Int): Boolean = a + 1 == b

    override fun previousValue(value: Int): Int = value - 1
    override fun nextValue(value: Int): Int = value + 1
}