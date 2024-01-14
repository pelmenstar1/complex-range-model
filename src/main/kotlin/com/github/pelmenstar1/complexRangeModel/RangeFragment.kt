package com.github.pelmenstar1.complexRangeModel

abstract class RangeFragment<T : Comparable<T>>(val start: T, val endInclusive: T) {
    val endExclusive: T
        get() = getNextValue(endInclusive)

    init {
        require(start <= endInclusive) { "Invalid range parameters" }
    }

    fun withStart(newStart: T): RangeFragment<T> {
        return if (newStart == start) this else createSelf(newStart, endInclusive)
    }

    fun withEnd(newEnd: T): RangeFragment<T> {
        return if (newEnd == endInclusive) this else createSelf(start, newEnd)
    }

    fun withEndExclusive(newEnd: T): RangeFragment<T> {
        return withEnd(getPreviousValue(newEnd))
    }

    override fun equals(other: Any?): Boolean {
        return other is RangeFragment<*> && start == other.start && endInclusive == other.endInclusive
    }

    override fun hashCode(): Int {
        return start.hashCode() * 31 + endInclusive.hashCode()
    }

    override fun toString(): String {
        return "[$start, $endInclusive]"
    }

    operator fun contains(value: T): Boolean {
        return value in start..endInclusive
    }

    fun containsExclusive(other: RangeFragment<T>): Boolean {
        return other.start > start && other.endInclusive < endInclusive
    }

    fun containsCompletely(other: RangeFragment<T>): Boolean {
        return other.start >= start && other.endInclusive <= endInclusive
    }

    fun leftContains(other: RangeFragment<T>): Boolean {
        return other.endInclusive in this && other.start <= start
    }

    fun overlapsWith(other: RangeFragment<T>): Boolean {
        return start <= other.endInclusive && other.start <= endInclusive
    }

    fun canUniteWith(other: RangeFragment<T>): Boolean {
        // Fragments can be united if they either:
        // 1. Overlap
        // 2. Next to each other, e.g [1; 2] and [3; 4] can be united to [1; 4]
        return overlapsWith(other) ||
                isNextToOther(endInclusive, other.start) ||
                isNextToOther(other.endInclusive, start)
    }

    fun uniteWith(other: RangeFragment<T>): RangeFragment<T>? {
        if (canUniteWith(other)) {
            return createSelf(minOf(start, other.start), maxOf(endInclusive, other.endInclusive))
        }

        return null
    }

    protected abstract fun isNextToOther(a: T, b: T): Boolean
    protected abstract fun createSelf(start: T, endInclusive: T): RangeFragment<T>
    protected abstract fun getPreviousValue(value: T): T
    protected abstract fun getNextValue(value: T): T
}

private class IntRangeFragment(
    start: Int,
    endInclusive: Int,
    @Suppress("UNUSED_PARAMETER") marker: Boolean
) : RangeFragment<Int>(start, endInclusive) {
    override fun isNextToOther(a: Int, b: Int): Boolean {
        return a + 1 == b
    }

    override fun createSelf(start: Int, endInclusive: Int): RangeFragment<Int> {
        return IntRangeFragment(start, endInclusive)
    }

    override fun getPreviousValue(value: Int): Int = value - 1
    override fun getNextValue(value: Int): Int = value + 1
}

fun IntRangeFragment(start: Int, endInclusive: Int): RangeFragment<Int> {
    return IntRangeFragment(start, endInclusive, false)
}

fun IntRangeFragment(range: IntRange): RangeFragment<Int> {
    return IntRangeFragment(range.first, range.last)
}