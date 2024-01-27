package com.github.pelmenstar1.complexRangeModel

interface FragmentElementSupport<T> {
    val zero: T

    fun isPositive(value: T): Boolean
    fun difference(a: T, b: T): T

    fun compare(a: T, b: T): Int
    fun isNextToOther(a: T, b: T): Boolean
    fun previousValue(value: T): T
    fun nextValue(value: T): T
}

object IntFragmentElementSupport : FragmentElementSupport<Int> {
    override val zero: Int
        get() = 0

    override fun compare(a: Int, b: Int): Int = a.compareTo(b)
    override fun isNextToOther(a: Int, b: Int): Boolean = a + 1 == b

    override fun previousValue(value: Int): Int = value - 1
    override fun nextValue(value: Int): Int = value + 1

    override fun difference(a: Int, b: Int) = a - b

    override fun isPositive(value: Int): Boolean = value > 0
}