package com.github.pelmenstar1.complexRangeModel

import com.github.pelmenstar1.complexRangeModel.bits.BitArrayComplexRangeBuilder
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRangeBuilder

inline fun <T : Comparable<T>> ComplexRange(block: ComplexRangeBuilder<T>.() -> Unit): ComplexRange<T> {
    return GenericComplexRangeBuilder<T>().also(block).build()
}

inline fun BitIntComplexRange(
    limitStart: Int,
    limitEnd: Int,
    block: ComplexRangeBuilder<Int>.() -> Unit
): ComplexRange<Int> {
    return BitArrayComplexRangeBuilder(limitStart, limitEnd).also(block).build()
}

inline fun IntComplexRange(block: ComplexRangeBuilder<Int>.() -> Unit): ComplexRange<Int> {
    return ComplexRange(block)
}

fun IntComplexRange(ranges: Array<out IntRange>): ComplexRange<Int> {
    return IntComplexRange {
        ranges.forEach { fragment(it) }
    }
}