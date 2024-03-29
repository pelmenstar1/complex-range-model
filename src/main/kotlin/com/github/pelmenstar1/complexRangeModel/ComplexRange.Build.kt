package com.github.pelmenstar1.complexRangeModel

import com.github.pelmenstar1.complexRangeModel.bits.BitArrayComplexRangeBuilder
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRangeBuilder

/**
 * Returns a new [ComplexRange] created using given [block] lambda.
 */
inline fun <T : FragmentElement<T>> ComplexRange(block: ComplexRangeBuilder<T>.() -> Unit): ComplexRange<T> {
    return GenericComplexRangeBuilder<T>().also(block).build()
}

/**
 * Returns a new [ComplexRange] using given [fs] array.
 *
 * @param fs the array of [RangeFragment] to build [ComplexRange] from.
 * The array is **not** required to contain mutually non-intersecting fragments and the array can be in any order.
 */
fun <T : FragmentElement<T>> ComplexRange(fs: Array<out RangeFragment<T>>): ComplexRange<T> {
    return ComplexRange { fragments(fs) }
}

/**
 * Returns a new [ComplexRange] using given fragments.
 *
 * @param fs the iterable of [RangeFragment] to build [ComplexRange] from.
 * The elements are **not** required to be mutually non-intersecting and the elements can be in any order.
 */
fun<T : FragmentElement<T>> ComplexRange(fs: Iterable<RangeFragment<T>>): ComplexRange<T> {
    return ComplexRange { fragments(fs) }
}

fun <T : FragmentElement<T>> ComplexRange(vs: Array<out T>): ComplexRange<T> {
    return ComplexRange { values(vs) }
}

/**
 * Returns a new [ComplexRange] created by given [block] lambda.
 *
 * A bit-array is used during the construction,
 * so this builder-method is more effective default [ComplexRange] one that uses linked list internally.
 * Note that more absolute difference between [limitStart] and [limitEnd], more memory is allocated,
 * regardless of the actual amount of fragments.
 * So this method of the building might not be suitable in all situations.
 */
inline fun BitIntComplexRange(
    limitStart: Int,
    limitEnd: Int,
    block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit
): IntComplexRange {
    return BitArrayComplexRangeBuilder(limitStart, limitEnd).also(block).build()
}

/**
 * Same of [ComplexRange] builder method with lambda argument. It's used for easier reading.
 */
inline fun IntComplexRange(block: ComplexRangeBuilder<IntFragmentElement>.() -> Unit): IntComplexRange {
    return ComplexRange(block)
}

/**
 * Creates a new [ComplexRange] instance using given [ranges] array.
 *
 * @param ranges an array of [IntRange] to create [IntRangeFragment] from.
 * The array is **not** required to contain mutually non-intersecting fragments and the array can be in any order.
 */
fun IntComplexRange(ranges: Array<out IntRange>): IntComplexRange {
    return IntComplexRange {
        ranges.forEach { fragment(it) }
    }
}