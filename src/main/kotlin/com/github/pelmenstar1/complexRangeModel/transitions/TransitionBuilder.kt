package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.FragmentElement

/**
 * A builder for [TransitionGroup].
 */
class TransitionBuilder<T : FragmentElement<T>> {
    private val groups = HashSet<TransitionGroup<T>>()

    /**
     * Adds a group built by given [block] lambda to the transition.
     */
    inline fun group(block: TransitionGroupBuilder<T>.() -> Unit) {
        val g = TransitionGroupBuilder<T>().also(block).build()

        group(g)
    }

    /**
     * Adds given group to the transition.
     */
    fun group(g: TransitionGroup<T>) {
        groups.add(g)
    }

    /**
     * Builds a new [ComplexRangeTransition] object using given transition groups.
     */
    fun build(): ComplexRangeTransition<T> {
        return ComplexRangeTransition.create(groups)
    }
}