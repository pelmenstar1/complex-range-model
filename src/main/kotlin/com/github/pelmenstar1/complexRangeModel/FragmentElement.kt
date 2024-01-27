package com.github.pelmenstar1.complexRangeModel

import kotlin.math.abs

interface FragmentElement<T : FragmentElement<T>> : Comparable<T> {
    fun previous(): T
    fun next(): T
}

interface DistanceFragmentElement<T : FragmentElement<T>, in D> : FragmentElement<T> {
    fun isDistanceLessThanOrEqual(other: T, maxDistance: D): Boolean
}

data class IntFragmentElement(val value: Int) : DistanceFragmentElement<IntFragmentElement, Int> {
    override fun compareTo(other: IntFragmentElement): Int {
        return value.compareTo(other.value)
    }

    override fun previous() = IntFragmentElement(value - 1)
    override fun next() = IntFragmentElement(value + 1)

    override fun isDistanceLessThanOrEqual(other: IntFragmentElement, maxDistance: Int): Boolean {
        return abs(value - other.value) <= maxDistance
    }

    override fun toString(): String = value.toString()
}