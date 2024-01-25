package com.github.pelmenstar1.complexRangeModel

abstract class RangeFragment<T>(val start: T, val endInclusive: T) {
    abstract val support: FragmentElementSupport<T>

    val endExclusive: T
        get() = support.nextValue(endInclusive)

    init {
        require(compare(start, endInclusive) <= 0) {
            "Invalid range parameters"
        }
    }

    fun withStart(newStart: T): RangeFragment<T> {
        return if (newStart == start) this else createSelf(newStart, endInclusive)
    }

    fun withEnd(newEnd: T): RangeFragment<T> {
        return if (newEnd == endInclusive) this else createSelf(start, newEnd)
    }

    fun withEndExclusive(newEnd: T): RangeFragment<T> {
        return withEnd(support.previousValue(newEnd))
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
        return compare(value, start) >= 0 && compare(value, endInclusive) <= 0
    }

    fun containsExclusive(other: RangeFragment<T>): Boolean {
        return compare(other.start, start) > 0 && compare(other.endInclusive, endInclusive) < 0
    }

    fun containsCompletely(other: RangeFragment<T>): Boolean {
        return compare(other.start, start) >= 0 && compare(other.endInclusive, endInclusive) <= 0
    }

    fun leftContains(other: RangeFragment<T>): Boolean {
        return other.endInclusive in this && compare(other.start, start) <= 0
    }

    fun overlapsWith(other: RangeFragment<T>): Boolean {
        return compare(start, other.endInclusive) <= 0 && compare(other.start, endInclusive) <= 0
    }

    fun canUniteWith(other: RangeFragment<T>): Boolean {
        // Fragments can be united if they either:
        // 1. Overlap
        // 2. Next to each other, e.g [1; 2] and [3; 4] can be united to [1; 4]
        return overlapsWith(other) ||
                support.isNextToOther(endInclusive, other.start) ||
                support.isNextToOther(other.endInclusive, start)
    }

    fun uniteWith(other: RangeFragment<T>): RangeFragment<T>? {
        if (canUniteWith(other)) {
            return createSelf(minOf(start, other.start), maxOf(endInclusive, other.endInclusive))
        }

        return null
    }

    private fun minOf(a: T, b: T): T = if(compare(a, b) <= 0) a else b
    private fun maxOf(a: T, b: T): T = if(compare(a, b) >= 0) a else b

    private fun compare(a: T, b: T): Int = support.compare(a, b)

    protected abstract fun createSelf(start: T, endInclusive: T): RangeFragment<T>
}

fun interface RangeFragmentFactory<T> {
    fun create(start: T, endInclusive: T): RangeFragment<T>
}

object IntRangeFragmentFactory : RangeFragmentFactory<Int> {
    override fun create(start: Int, endInclusive: Int): RangeFragment<Int> {
        return IntRangeFragment(start, endInclusive)
    }
}

private class IntRangeFragment(
    start: Int,
    endInclusive: Int,
    @Suppress("UNUSED_PARAMETER") marker: Boolean
) : RangeFragment<Int>(start, endInclusive) {
    override val support: FragmentElementSupport<Int>
        get() = IntFragmentElementSupport

    override fun createSelf(start: Int, endInclusive: Int): RangeFragment<Int> {
        return IntRangeFragment(start, endInclusive)
    }
}

fun IntRangeFragment(start: Int, endInclusive: Int): RangeFragment<Int> {
    return IntRangeFragment(start, endInclusive, false)
}

fun IntRangeFragment(range: IntRange): RangeFragment<Int> {
    return IntRangeFragment(range.first, range.last)
}