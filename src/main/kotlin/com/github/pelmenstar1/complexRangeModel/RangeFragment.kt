package com.github.pelmenstar1.complexRangeModel

/**
 * Represents a range of values of type [T].
 */
class RangeFragment<T : FragmentElement<T>>(
    override val start: T,
    override val endInclusive: T
) : ClosedRange<T>, Iterable<T> {
    val endExclusive: T
        get() = endInclusive.next()

    val elementCount: Int
        get() = start.countElementsTo(endInclusive) + 1

    init {
        require(start <= endInclusive) {
            "Invalid range parameters"
        }
    }

    /**
     * Returns an instance of [RangeFragment] with given new start [value] and the same [endInclusive] value.
     */
    fun withStart(value: T): RangeFragment<T> {
        return if (value == start) this else RangeFragment(value, endInclusive)
    }

    /**
     * Returns an instance of [RangeFragment] with given new end (inclusive) [value] and the same [start] value.
     */
    fun withEnd(value: T): RangeFragment<T> {
        return if (value == endInclusive) this else RangeFragment(start, value)
    }

    /**
     * Returns an instance of [RangeFragment] with given new end (exclusive) [value] and the same [start] value.
     */
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

    /**
     * Determines whether the current fragment contains [other] fragment exclusively, i.e.
     * `other.start > start && other.endInclusive < endInclusive`
     */
    fun containsExclusive(other: RangeFragment<T>): Boolean {
        return other.start > start && other.endInclusive < endInclusive
    }

    /**
     * Determines whether the current fragment contains [other] fragments (inclusively), i.e.
     * `other.start >= start && other.endInclusive <= endInclusive`
     */
    fun containsCompletely(other: RangeFragment<T>) = containsCompletely(other.start, other.endInclusive)

    /**
     * Determines whether the current fragment contains a fragment defined by [otherStart] and [otherEndInclusive]
     * (inclusively), i.e. `otherStart >= start && otherEndInclusive <= endInclusive`
     */
    fun containsCompletely(otherStart: T, otherEndInclusive: T): Boolean {
        return otherStart >= start && otherEndInclusive <= endInclusive
    }

    /**
     * Determines whether the current fragment contains [other] fragment only on the left side.
     * For example, if `a = 1..3` and `b = 2..5`, then `a` is on the left side of `b` range.
     *
     * In other words, the method returns `true`, if this fragment contains [other]'s [endInclusive] and [other]'s [start] is less than or equal to this fragment's [start].
     */
    fun leftContains(other: RangeFragment<T>): Boolean {
        return other.endInclusive in this && other.start <= start
    }

    /**
     * Determines whether the current fragment overlaps with [other] fragment.
     * The method has the following properties:
     * - Reflexive: `a.overlapsWith(a)`
     * - Commutative: if `a.overlapsWith(b)`, then `b.overlapsWith(a)`.
     */
    fun overlapsWith(other: RangeFragment<T>): Boolean {
        return overlapsWith(other.start, other.endInclusive)
    }

    /**
     * Determines whether the current fragment overlaps with a fragment defined by [otherStart] and [otherEndInclusive].
     */
    fun overlapsWith(otherStart: T, otherEndInclusive: T): Boolean {
        return start <= otherEndInclusive && otherStart <= endInclusive
    }

    /**
     * Determines whether the current fragment can be united with [other] fragment.
     *
     * A fragment can be united with another fragment if the two fragments overlap or one of the fragments is adjacent to another.
     */
    fun canUniteWith(other: RangeFragment<T>): Boolean {
        return canUniteWith(other.start, other.endInclusive)
    }

    /**
     * Determines whether the current fragment can be united with a fragment defined by [otherStart] and [otherEndInclusive].
     */
    fun canUniteWith(otherStart: T, otherEndInclusive: T): Boolean {
        return overlapsWith(otherStart, otherEndInclusive) || isAdjacentTo(otherStart, otherEndInclusive)
    }

    /**
     * Determines whether the current fragment is adjacent to [other] fragment.
     * Adjacency means that either:
     * - the next element of [endInclusive] of this fragment is the [start] element of [other] fragment, or
     * - the previous element of [start] of this fragment is the [endInclusive] element of [other] fragment.
     *
     * The method has the following properties:
     * - Commutative: if `a.isAdjacentTo(b)`, then `b.isAdjacent(a)`
     */
    fun isAdjacentTo(other: RangeFragment<T>) = isAdjacentTo(other.start, other.endInclusive)

    /**
     * Determines whether the current fragment is adjacent to a fragment defined by [otherStart] and [otherEndInclusive]
     */
    fun isAdjacentTo(otherStart: T, otherEndInclusive: T): Boolean {
        return isAdjacentLeft(otherStart) || isAdjacentRight(otherEndInclusive)
    }

    /**
     * Determines whether the current fragment is adjacent on the left side to other fragment.
     * Adjacency on the left side means that the next element of [endInclusive] of this fragment is equal to [otherStart].
     *
     * @param otherStart start value of the other fragment.
     * End value is not needed to determine adjacency ont the left side.
     */
    fun isAdjacentLeft(otherStart: T): Boolean {
        return endInclusive.hasNext() && endInclusive.next() == otherStart
    }

    /**
     * Determines whether the current fragment on the right side is adjacent to other fragment.
     * Adjacency on the right side means that the previous element of [start] of this fragment is equal to [otherEndInclusive].
     *
     * @param otherEndInclusive end (inclusive) value of the other fragment.
     * Start value is not needed to determine adjacency on the right side.
     */
    fun isAdjacentRight(otherEndInclusive: T): Boolean {
        return start.hasPrevious() && start.previous() == otherEndInclusive
    }

    /**
     * Determines whether the current fragment lies before the other fragment if these fragments don't overlap.
     */
    fun isBefore(other: RangeFragment<T>): Boolean {
        return start <= other.start
    }

    /**
     * Determines whether the current fragment lies after the other fragment if these fragments don't overlap.
     */
    fun isAfter(other: RangeFragment<T>): Boolean {
        return endInclusive >= other.endInclusive
    }

    /**
     * Returns raw distance to the other fragment.
     *
     * Raw distance (between ranges) is zero if the fragments overlap,
     * otherwise it is the least element count between start and end elements of this and [other] fragments.
     * In other words, the raw distance between fragments `a` and `b` is
     * ```
     * if (a.overlapsWith(b)) 0 else min(abs(a.start - b.endInclusive), abs(b.start - a.endInclusive))
     * ```
     */
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
            if (lr == endInclusive) {
                throw NoSuchElementException()
            }

            lr = lr?.next() ?: start

            lastReturned = lr
            return lr
        }
    }
}

typealias IntRangeFragment = RangeFragment<IntFragmentElement>

/**
 * Determines whether the distance between this and [other] fragments is less than or equal to [maxDist].
 *
 * The definition of the distance between the fragments is the same as the definition of the raw distance between them,
 * except distance between element is defined by [D] type.
 */
fun <T : DistanceFragmentElement<T, D>, D> RangeFragment<T>.isDistanceLessThanOrEqual(
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

fun IntRangeFragment(start: Int, endInclusive: Int): IntRangeFragment {
    return IntRangeFragment(IntFragmentElement(start), IntFragmentElement(endInclusive))
}

fun IntRangeFragment(range: IntRange): IntRangeFragment {
    return IntRangeFragment(range.first, range.last)
}