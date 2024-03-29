package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.ComplexRange
import com.github.pelmenstar1.complexRangeModel.FragmentElement
import com.github.pelmenstar1.complexRangeModel.RangeFragment

/**
 * Represents a primitive operation on [RangeFragment]
 * that transitions the initial fragment(s) to another(s).
 * Let the initial fragment(s) be 'origin state'; the result fragments after the transition be 'destination state'
 * The operation itself contains only the information about the transform, not the way to apply it.
 */
sealed interface TransitionOperation<T : FragmentElement<T>> {
    /**
     * Returns such operation that transitions current destination state to the origin.
     */
    fun reversed(): TransitionOperation<T>

    /**
     * Returns the efficiency level of the operation, i.e. how many elements are changed during the transition.
     */
    fun efficiencyLevel(): Int

    /**
     * Represents an abstract class for structural operation, such that either insert or removes a fragment.
     */
    sealed class StructuralOperation<T : FragmentElement<T>>(val fragment: RangeFragment<T>) : TransitionOperation<T> {
        override fun efficiencyLevel(): Int {
            return fragment.elementCount
        }

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

    /**
     * Represents an operation that insert a fragment.
     */
    class Insert<T : FragmentElement<T>>(fragment: RangeFragment<T>) : StructuralOperation<T>(fragment) {
        override fun reversed(): TransitionOperation<T> = Remove(fragment)
    }

    /**
     * Represents an operation that removes a fragment.
     */
    class Remove<T : FragmentElement<T>>(fragment: RangeFragment<T>) : StructuralOperation<T>(fragment) {
        override fun reversed(): TransitionOperation<T> = Insert(fragment)
    }

    /**
     * Represents an operation that transforms a single fragment to another.
     *
     * [origin] and [destination] cannot be equal. Also, they are required to overlap.
     *
     * It differs from [Move] in that [Transform] requires fragments to overlap, while [Move] does not.
     * But [Move] requires the fragment to have same [RangeFragment.elementCount].
     * If two fragments have the same [RangeFragment.elementCount] and overlap,
     * [Transform] and [Move] are interchangeable.
     * They are represented as different classes because the client code may handle these two cases differently.
     */
    data class Transform<T : FragmentElement<T>>(
        val origin: RangeFragment<T>,
        val destination: RangeFragment<T>
    ) : TransitionOperation<T> {
        init {
            require(origin != destination) {
                "Origin fragment should not be the same as destination fragment"
            }
            require(origin.overlapsWith(destination)) {
                "Origin fragment should overlap with destination fragment"
            }
        }

        override fun efficiencyLevel(): Int {
            return origin.start.countElementsToAbsolute(destination.start) +
                    origin.endInclusive.countElementsToAbsolute(destination.endInclusive)
        }

        override fun reversed(): TransitionOperation<T> = Transform(destination, origin)
    }

    /**
     * Represents an operation that moves a single fragment to another.
     * Moving means that the total element count of a fragment being in a state of transition doesn't change during the transition.
     * It requires the [origin] and [destination] to have the same [RangeFragment.elementCount].
     */
    data class Move<T : FragmentElement<T>>(
        val origin: RangeFragment<T>,
        val destination: RangeFragment<T>
    ) : TransitionOperation<T> {
        init {
            require(origin != destination) {
                "Origin fragment should not be the same as destination fragment"
            }
            require(origin.elementCount == destination.elementCount) {
                "Origin fragment should have same length as destination fragment"
            }
        }

        override fun efficiencyLevel(): Int {
            return origin.start.countElementsToAbsolute(destination.start)
        }

        override fun reversed(): TransitionOperation<T> = Move(destination, origin)
    }

    /**
     * Represents an operation that splits [origin] fragment into multiple other [destination] fragments
     * that are completely in the [origin] fragment.
     *
     * A set of the [destination] fragments is represented as [ComplexRange] to preserve
     * useful properties of fragments being mutually non-intersecting and ordered in ascending order.
     * These properties may greatly assist in processing of this operation.
     * [ComplexRange] is the most natural representation that has these properties.
     */
    data class Split<T : FragmentElement<T>>(
        val origin: RangeFragment<T>,
        val destination: ComplexRange<T>
    ) : TransitionOperation<T> {
        init {
            require(isFragmentCompletelyContainsComplexRange(origin, destination)) {
                "Origin fragment should completely contains destination complex range"
            }
        }

        override fun reversed(): TransitionOperation<T> = Join(destination, origin)

        override fun efficiencyLevel(): Int {
            val destElementCount = destination.fragments().sumOf { it.elementCount }

            // destElementCount must be lesser than origin.elementCount
            return origin.elementCount - destElementCount
        }
    }

    /**
     * Represents an operation that joins multiple [origin] fragments into single [destination] fragment.
     * [origin] fragments are inside the [destination] fragment.
     *
     * A set of the [origin] fragments is represented as [ComplexRange] to preserve
     * useful properties of fragments being mutually non-intersecting and ordered in ascending order.
     * These properties may greatly assist in processing of this operation.
     * [ComplexRange] is the most natural representation that has these properties.
     */
    data class Join<T : FragmentElement<T>>(
        val origin: ComplexRange<T>,
        val destination: RangeFragment<T>
    ) : TransitionOperation<T> {
        init {
            require(isFragmentCompletelyContainsComplexRange(destination, origin)) {
                "Destination fragment should completely contains origin complex range"
            }
        }

        override fun reversed(): TransitionOperation<T> = Split(destination, origin)

        override fun efficiencyLevel(): Int {
            val originElementCount = origin.fragments().sumOf { it.elementCount }

            // originElementCount must be lesser than destination.elementCount
            return destination.elementCount - originElementCount
        }
    }

    companion object {
        private fun<T : FragmentElement<T>> isFragmentCompletelyContainsComplexRange(
            outerFrag: RangeFragment<T>,
            complexRange: ComplexRange<T>
        ): Boolean {
            val frags = complexRange.fragments()

            // Check only first and last of fragments of the ComplexRange because ranges in the ComplexRange is ordered in
            // ascending order.
            return frags.first().start == outerFrag.start && frags.last().endInclusive == outerFrag.endInclusive
        }
    }
}