package com.github.pelmenstar1.complexRangeModel

import kotlin.math.abs

/**
 * Represents an element of [RangeFragment]. The element must satisfy certain rules:
 * - It can be compared to other element (of the same type).
 * - Each element has its previous and next elements. Their existence can also be determined.
 */
interface FragmentElement<T : FragmentElement<T>> : Comparable<T> {
    /**
     * Returns the next element relatively to the current one.
     * If there's no such value, it throws [NoSuchElementException].
     *
     * The [next] element must satisfy following rules:
     * - The next element is greater than the current one: `element.next() > element`.
     * - If the next element exists, then the following equality must hold: `element.next().previous() == element`.
     */
    fun next(): T

    /**
     * Returns the previous element relatively to the current one. If there's no such value, it throws [NoSuchElementException]
     *
     * The [previous] element must satisfy following rules:
     * - The previous element is lesser than the current one: `element.previous() < element`.
     * - If the next element exists, then the following equality must hold: `element.next().previous() == element`.
     */
    fun previous(): T

    /**
     * Determines whether the current element has the next element.
     * If the method returns `true`, [next] should not throw [NoSuchElementException]
     */
    fun hasNext(): Boolean

    /**
     * Determines whether the current element has the next element.
     * If the method returns `true`, [next] should not throw [NoSuchElementException]
     */
    fun hasPrevious(): Boolean

    /**
     * Returns amount of elements between the current element and given [other] one.
     * The current element should be not greater than [other].
     */
    fun countElementsTo(other: T): Int

    /**
     * Returns amount of elements between the current element and given [other] one.
     * This method does not require the current element to be not greater than [other].
     */
    @Suppress("UNCHECKED_CAST")
    fun countElementsToAbsolute(other: T): Int {
        return if (this <= other) {
            countElementsTo(other)
        } else {
            other.countElementsTo(this as T)
        }
    }
}

/**
 * A variation of [FragmentElement] that has a notion of distance between the elements.
 *
 * One definition of distance that has all the implementations of [FragmentElement] is
 * simply an amount of elements between two [FragmentElement]'s. Such distance is called "raw".
 * But there can be cases when there exists more robust definition of the distance that is expressed through given [D] type.
 * For example, if the element is a date, then distance may be a **duration** between two dates expressed as a dedicated class.
 */
interface DistanceFragmentElement<T : FragmentElement<T>, in D> : FragmentElement<T> {
    /**
     * Returns whether distance between the current element and given [other] one is less than or equal to given [maxDistance] value.
     *
     * The current element should be not greater than [other] one.
     */
    fun isDistanceLessThanOrEqual(other: T, maxDistance: D): Boolean
}

open class IntFragmentElement(val value: Int) : DistanceFragmentElement<IntFragmentElement, Int> {
    override fun hasPrevious(): Boolean {
        return value != Int.MIN_VALUE
    }

    override fun hasNext(): Boolean {
        return value != Int.MAX_VALUE
    }

    override fun previous(): IntFragmentElement {
        if (value == Int.MIN_VALUE) {
            throw NoSuchElementException()
        }

        return IntFragmentElement(value - 1)
    }

    override fun next(): IntFragmentElement {
        if (value == Int.MAX_VALUE) {
            throw NoSuchElementException()
        }

        return IntFragmentElement(value + 1)
    }

    override fun compareTo(other: IntFragmentElement): Int {
        return value.compareTo(other.value)
    }

    override fun isDistanceLessThanOrEqual(other: IntFragmentElement, maxDistance: Int): Boolean {
        return abs(value - other.value) <= maxDistance
    }

    override fun countElementsTo(other: IntFragmentElement): Int {
        return other.value - value
    }

    override fun countElementsToAbsolute(other: IntFragmentElement): Int {
        return abs(other.value - value)
    }

    override fun equals(other: Any?): Boolean {
        return other is IntFragmentElement && value == other.value
    }

    override fun hashCode(): Int = value

    override fun toString(): String = value.toString()
}