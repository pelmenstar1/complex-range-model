package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.FragmentElement
import com.github.pelmenstar1.complexRangeModel.RangeFragment

sealed interface TransitionOperation<T : FragmentElement<T>> {
    fun reversed(): TransitionOperation<T>

    sealed class StructuralOperation<T : FragmentElement<T>>(val fragment: RangeFragment<T>) : TransitionOperation<T> {
        override fun equals(other: Any?): Boolean {
            if (other === this) return true
            if (other == null || javaClass != other.javaClass) return false

            return fragment == (other as StructuralOperation<*>).fragment
        }

        override fun hashCode(): Int {
            return fragment.hashCode()
        }

        override fun toString(): String {
            return "TransitionOperation.${javaClass.simpleName}(fragment=${fragment})"
        }
    }

    class Insert<T : FragmentElement<T>>(fragment: RangeFragment<T>) : StructuralOperation<T>(fragment) {
        override fun reversed(): TransitionOperation<T> = Remove(fragment)
    }

    class Remove<T : FragmentElement<T>>(fragment: RangeFragment<T>) : StructuralOperation<T>(fragment) {
        override fun reversed(): TransitionOperation<T> = Insert(fragment)
    }

    data class Transform<T : FragmentElement<T>>(
        val origin: RangeFragment<T>,
        val destination: RangeFragment<T>
    ) : TransitionOperation<T> {
        override fun reversed(): TransitionOperation<T> = Transform(destination, origin)
    }

    class Split<T : FragmentElement<T>>(
        val origin: RangeFragment<T>,
        val destinations: Array<RangeFragment<T>>
    ) : TransitionOperation<T> {
        override fun reversed(): TransitionOperation<T> = Join(destinations, origin)

        override fun equals(other: Any?): Boolean {
            return other is Split<*> && origin == other.origin && destinations.contentEquals(other.destinations)
        }

        override fun hashCode(): Int {
            return origin.hashCode() * 31 + destinations.contentHashCode()
        }

        override fun toString(): String {
            return "TransitionOperation.Split(origin=$origin, destinations=${destinations.contentToString()})"
        }
    }

    class Join<T : FragmentElement<T>>(
        val origins: Array<RangeFragment<T>>,
        val destination: RangeFragment<T>
    ) : TransitionOperation<T> {
        override fun reversed(): TransitionOperation<T> = Split(destination, origins)

        override fun equals(other: Any?): Boolean {
            return other is Join<*> && origins.contentEquals(other.origins) && destination == other.destination
        }

        override fun hashCode(): Int {
            return origins.contentHashCode() * 31 + destination.hashCode()
        }

        override fun toString(): String {
            return "TransitionOperation.Split(origins=${origins.contentToString()}, destination=${destination})"
        }
    }
}