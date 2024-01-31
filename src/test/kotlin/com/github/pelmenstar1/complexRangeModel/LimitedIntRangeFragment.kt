package com.github.pelmenstar1.complexRangeModel

import kotlin.math.abs

class LimitedIntFragmentElement(
    private val limitedRange: IntRange,
    private val value: Int
): FragmentElement<LimitedIntFragmentElement> {
    init {
        require(value in limitedRange) { "Value is out of its limit range" }
    }

    override fun compareTo(other: LimitedIntFragmentElement): Int {
        return value.compareTo(other.value)
    }

    override fun hasPrevious(): Boolean {
        return value != limitedRange.first
    }

    override fun hasNext(): Boolean {
        return value != limitedRange.last
    }

    override fun previous(): LimitedIntFragmentElement {
        if (value == limitedRange.first) {
            throw NoSuchElementException()
        }

        return LimitedIntFragmentElement(limitedRange, value - 1)
    }

    override fun next(): LimitedIntFragmentElement {
        if (value == limitedRange.last) {
            throw NoSuchElementException()
        }

        return LimitedIntFragmentElement(limitedRange, value + 1)
    }

    override fun countElementsTo(other: LimitedIntFragmentElement): Int {
        return other.value - value
    }

    override fun countElementsToAbsolute(other: LimitedIntFragmentElement): Int {
        return abs(other.value - value)
    }

    override fun equals(other: Any?): Boolean {
        return other is LimitedIntFragmentElement && value == other.value
    }

    override fun hashCode(): Int = value
    override fun toString(): String = value.toString()
}

typealias LimitedIntRangeFragment = RangeFragment<LimitedIntFragmentElement>

fun LimitedIntRangeFragment(limit: IntRange, valueRange: IntRange): LimitedIntRangeFragment {
    return LimitedIntRangeFragment(
        LimitedIntFragmentElement(limit, valueRange.first),
        LimitedIntFragmentElement(limit, valueRange.last)
    )
}