package com.github.pelmenstar1.complexRangeModel

import kotlin.math.abs

interface FragmentElement<T : FragmentElement<T>> : Comparable<T> {
    fun next(): T
    fun previous(): T

    fun hasNext(): Boolean
    fun hasPrevious(): Boolean

    fun countElementsTo(other: T): Int

    @Suppress("UNCHECKED_CAST")
    fun countElementsToAbsolute(other: T): Int {
        return if (this <= other) {
            countElementsTo(other)
        } else {
            other.countElementsTo(this as T)
        }
    }
}

interface DistanceFragmentElement<T : FragmentElement<T>, in D> : FragmentElement<T> {
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