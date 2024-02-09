package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.FragmentElement
import com.github.pelmenstar1.complexRangeModel.ComplexRange

/**
 * Represents a transition between one [ComplexRange] instance to another.
 * The same as [TransitionOperation] and [TransitionGroup],
 * this class cannot be used to execute the transition, it contains only the information about how to do it.
 *
 * A transition consists of transition groups.
 * They are expected to be independent, which means that a result of one transition group does not affect other groups.
 * In other words, there is no different between executing transition groups in sequential order or in parallel.
 */
interface ComplexRangeTransition<T : FragmentElement<T>> {
    /**
     * Returns a set of transition groups of the transition.
     * This collection is represented as a set because the order of executing them should not matter.
     */
    fun groups(): Set<TransitionGroup<T>>

    /**
     * Returns a transition that transforms current destination complex range to the origin one.
     */
    fun reversed(): ComplexRangeTransition<T>

    /**
     * Returns total efficiency level of this transition. It equals to the sum of efficiency levels of transition groups.
     */
    fun efficiencyLevel(): Int

    companion object {
        /**
         * Creates an instance of [ComplexRangeTransition] using given [groups] list.
         *
         * This list is converted to a set - the groups are allowed to repeat.
         */
        fun <T : FragmentElement<T>> create(groups: List<TransitionGroup<T>>): ComplexRangeTransition<T> {
            return create(HashSet(groups))
        }

        /**
         * Creates an instance of [ComplexRangeTransition] using given [groups] set.
         *
         * The groups are expected to be independent.
         */
        fun <T : FragmentElement<T>> create(groups: Set<TransitionGroup<T>>): ComplexRangeTransition<T> {
            return SetComplexRangeTransition(groups)
        }
    }
}

private class SetComplexRangeTransition<T : FragmentElement<T>>(
    private val groups: Set<TransitionGroup<T>>
) : ComplexRangeTransition<T> {
    override fun groups(): Set<TransitionGroup<T>> = groups

    override fun reversed(): ComplexRangeTransition<T> {
        val revGroups = HashSet<TransitionGroup<T>>(groups.size)
        for (g in groups) {
            revGroups.add(g.reversed())
        }

        return SetComplexRangeTransition(revGroups)
    }

    override fun efficiencyLevel(): Int {
        return groups.sumOf { it.efficiencyLevel() }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true

        return other is ComplexRangeTransition<*> && groups == other.groups()
    }

    override fun hashCode(): Int = groups.hashCode()

    override fun toString(): String {
        return buildString {
            append("ComplexRangeTransition(groups=[")
            groups.forEachIndexed { i, group ->
                append(group)

                if (i < groups.size - 1) {
                    append(", ")
                }
            }

            append("])")
        }
    }
}

inline fun <T : FragmentElement<T>> ComplexRangeTransition(block: TransitionBuilder<T>.() -> Unit): ComplexRangeTransition<T> {
    return TransitionBuilder<T>().also(block).build()
}