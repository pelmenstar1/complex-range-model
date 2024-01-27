package com.github.pelmenstar1.complexRangeModel

import com.github.pelmenstar1.complexRangeModel.bits.BitArrayComplexRangeBuilder
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRangeBuilder

inline fun <T : FragmentElement<T>> ComplexRange(block: ComplexRangeBuilder<T>.() -> Unit): ComplexRange<T> {
    return GenericComplexRangeBuilder<T>().also(block).build()
}

inline fun BitIntComplexRange(
    limitStart: Int,
    limitEnd: Int,
    block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit
): ComplexRange<IntFragmentElement> {
    return BitArrayComplexRangeBuilder(limitStart, limitEnd).also(block).build()
}

inline fun IntComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): ComplexRange<IntFragmentElement> {
    return ComplexRange(block)
}

fun IntComplexRange(ranges: Array<out IntRange>): ComplexRange<IntFragmentElement> {
    return IntComplexRange {
        ranges.forEach { fragment(it) }
    }
}