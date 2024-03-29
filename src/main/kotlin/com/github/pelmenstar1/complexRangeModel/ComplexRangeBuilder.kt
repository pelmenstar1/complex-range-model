package com.github.pelmenstar1.complexRangeModel

/**
 * An interface for builder-like objects that are used for constructing [ComplexRange].
 */
interface ComplexRangeBuilder<T : FragmentElement<T>> {
    /**
     * Adds given fragment to the [ComplexRange].
     *
     * This fragment might intersect with, or be equal to other fragments.
     * The implementation is expected to handle these cases in an appropriate way - some fragments might be united into one.
     */
    fun fragment(value: RangeFragment<T>)

    /**
     * Adds an array of fragments to the [ComplexRange].
     *
     * These fragments may intersect with, or be equal to other fragments or mutually.
     * The implementation is expected to handle these cases in an appropriate way - some fragments might be united into one.
     */
    fun fragments(fs: Array<out RangeFragment<T>>) {
        fs.forEach(::fragment)
    }

    /**
     * Adds iterable of fragments to the [ComplexRange].
     *
     * These fragments may intersect with, or be equal to other fragments or mutually.
     * The implementation is expected to handle these cases in an appropriate way - some fragments might be united into one.
     */
    fun fragments(fs: Iterable<RangeFragment<T>>) {
        fs.forEach(::fragment)
    }

    /**
     * Adds a single value to the [ComplexRange].
     * It's the same operation as adding a fragment whose `start` and `endInclusive` values equal to [v].
     * The implementation may handle this operation in more performant way.
     */
    fun value(v: T) {
        fragment(RangeFragment(v, v))
    }

    /**
     * Adds an array of values to the [ComplexRange].
     *
     * It's the same operation as adding array of fragments whose `start` and `endInclusive` values equal to elements of [vs].
     * The implementation may handle this operation in more performant way.
     */
    fun values(vs: Array<out T>) {
        vs.forEach(::value)
    }

    /**
     * Adds iterable values to the [ComplexRange].
     *
     * It's the same operation as adding array of fragments whose `start` and `endInclusive` values equal to elements of [vs].
     * The implementation may handle this operation in more performant way.
     */
    fun values(vs: Iterable<T>) {
        vs.forEach(::value)
    }
}

/**
 * Fast-path for adding a new fragment to the builder.
 *
 * @param range a range to create [IntRangeFragment] from.
 */
fun ComplexRangeBuilder<IntFragmentElement>.fragment(range: IntRange) {
    fragment(range.first, range.last)
}

/**
 * Fast-path for adding a new fragment to the builder.
 */
fun ComplexRangeBuilder<IntFragmentElement>.fragment(start: Int, endInclusive: Int) {
    fragment(IntRangeFragment(start, endInclusive))
}