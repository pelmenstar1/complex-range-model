package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.ComplexRange
import com.github.pelmenstar1.complexRangeModel.FragmentElement
import com.github.pelmenstar1.complexRangeModel.RangeFragment

sealed interface TransitionOperation<T : FragmentElement<T>> {
    fun reversed(): TransitionOperation<T>
    fun efficiencyLevel(): Int

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

    data class Split<T : FragmentElement<T>>(
        val origin: RangeFragment<T>,
        val destination: ComplexRange<T>
    ) : TransitionOperation<T> {
        override fun reversed(): TransitionOperation<T> = Join(destination, origin)

        override fun efficiencyLevel(): Int {
            val destElementCount = destination.fragments().sumOf { it.elementCount }

            // destElementCount must be lesser than origin.elementCount
            return origin.elementCount - destElementCount
        }
    }

    data class Join<T : FragmentElement<T>>(
        val origin: ComplexRange<T>,
        val destination: RangeFragment<T>
    ) : TransitionOperation<T> {
        override fun reversed(): TransitionOperation<T> = Split(destination, origin)

        override fun efficiencyLevel(): Int {
            val originElementCount = origin.fragments().sumOf { it.elementCount }

            // originElementCount must be lesser than destination.elementCount
            return destination.elementCount - originElementCount
        }
    }
}