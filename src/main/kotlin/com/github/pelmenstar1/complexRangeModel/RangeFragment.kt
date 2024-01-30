package com.github.pelmenstar1.complexRangeModel

class RangeFragment<T : FragmentElement<T>>(
    override val start: T,
    override val endInclusive: T
) : ClosedRange<T>, Iterable<T>  {
    val endExclusive: T
        get() = endInclusive.next()

    val elementCount: Int
        get() = start.countElementsTo(endInclusive) + 1

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

    override fun contains(value: T): Boolean {
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
        return overlapsWith(other.start, other.endInclusive)
    }

    fun overlapsWith(otherStart: T, otherEndInclusive: T): Boolean {
        return start <= otherEndInclusive && otherStart <= endInclusive
    }

    fun canUniteWith(other: RangeFragment<T>): Boolean {
        // Fragments can be united if they either:
        // 1. Overlap
        // 2. Next to each other, e.g [1; 2] and [3; 4] can be united to [1; 4]
        return overlapsWith(other) || isAdjacentTo(other)
    }

    fun isAdjacentTo(other: RangeFragment<T>) = isAdjacentTo(other.start, other.endInclusive)

    fun isAdjacentTo(otherStart: T, otherEndInclusive: T): Boolean {
        return isAdjacentLeft(otherStart) || isAdjacentRight(otherEndInclusive)
    }

    fun isAdjacentLeft(otherStart: T): Boolean {
        return endInclusive.next() == otherStart
    }

    fun isAdjacentRight(otherEndInclusive: T): Boolean {
        return start == otherEndInclusive.next()
    }

    fun isBefore(other: RangeFragment<T>): Boolean {
        return start <= other.start
    }

    fun isAfter(other: RangeFragment<T>): Boolean {
        return endInclusive >= other.endInclusive
    }

    fun getRawDistanceTo(other: RangeFragment<T>): Int {
        return if (overlapsWith(other)) {
            0
        } else {
            val thisStart = start
            val otherStart = other.start

            if (thisStart >= otherStart) {
                other.endInclusive.countElementsTo(thisStart)
            } else {
                endInclusive.countElementsTo(otherStart)
            }
        }
    }

    override fun iterator(): Iterator<T> = IteratorImpl()

    private inner class IteratorImpl : Iterator<T> {
        private var lastReturned: T? = null

        override fun hasNext(): Boolean {
            return lastReturned != endInclusive
        }

        override fun next(): T {
            var lr = lastReturned
            lr = lr?.next() ?: start

            lastReturned = lr
            return lr
        }
    }
}

typealias IntRangeFragment = RangeFragment<IntFragmentElement>

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
    override fun create(start: IntFragmentElement, endInclusive: IntFragmentElement): IntRangeFragment {
        return IntRangeFragment(start, endInclusive)
    }
}

fun IntRangeFragment(start: Int, endInclusive: Int): IntRangeFragment {
    return IntRangeFragment(IntFragmentElement(start), IntFragmentElement(endInclusive))
}

fun IntRangeFragment(range: IntRange): IntRangeFragment {
    return IntRangeFragment(range.first, range.last)
}