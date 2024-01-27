package com.github.pelmenstar1.complexRangeModel

class RangeFragment<T : FragmentElement<T>>(val start: T, val endInclusive: T)  {
    val endExclusive: T
        get() = endInclusive.next()

    init {
        require(start <= endInclusive) { "Invalid range parameters" }
    }

    fun withStart(value: T): RangeFragment<T> {
        return if (value == start) this else RangeFragment(value, endInclusive)
    }

    fun withEnd(value: T): RangeFragment<T> {
        return if (value == endInclusive) this else RangeFragment(start, value)
    }

    fun withEndExclusive(value: T): RangeFragment<T> {
        return withEnd(value.previous())
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
                endInclusive.next() == other.start ||
                other.endInclusive.next() == start
    }

    fun uniteWith(other: RangeFragment<T>): RangeFragment<T>? {
        if (canUniteWith(other)) {
            return RangeFragment(minOf(start, other.start), maxOf(endInclusive, other.endInclusive))
        }

        return null
    }

    fun isBefore(other: RangeFragment<T>): Boolean {
        return start <= other.start
    }

    fun isAfter(other: RangeFragment<T>): Boolean {
        return endInclusive >= other.endInclusive
    }
}

fun<T : DistanceFragmentElement<T, D>, D> RangeFragment<T>.isDistanceLessThanOrEqual(
    other: RangeFragment<T>,
    maxDist: D
): Boolean {
    return if (overlapsWith(other)) {
        return true
    } else {
        val thisStart = start
        val otherStart = other.start

        if (thisStart >= otherStart) {
            other.endInclusive.isDistanceLessThanOrEqual(thisStart, maxDist)
        } else {
            endInclusive.isDistanceLessThanOrEqual(otherStart, maxDist)
        }
    }
}

fun interface RangeFragmentFactory<T : FragmentElement<T>> {
    fun create(start: T, endInclusive: T): RangeFragment<T>
}

object IntRangeFragmentFactory : RangeFragmentFactory<IntFragmentElement> {
    override fun create(start: IntFragmentElement, endInclusive: IntFragmentElement): RangeFragment<IntFragmentElement> {
        return IntRangeFragment(start, endInclusive)
    }
}

fun IntRangeFragment(start: IntFragmentElement, endInclusive: IntFragmentElement): RangeFragment<IntFragmentElement> {
    return RangeFragment(start, endInclusive)
}

fun IntRangeFragment(start: Int, endInclusive: Int): RangeFragment<IntFragmentElement> {
    return IntRangeFragment(IntFragmentElement(start), IntFragmentElement(endInclusive))
}

fun IntRangeFragment(range: IntRange): RangeFragment<IntFragmentElement> {
    return IntRangeFragment(range.first, range.last)
}