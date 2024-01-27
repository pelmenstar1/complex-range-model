package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.ArraySet
import com.github.pelmenstar1.complexRangeModel.FragmentElement

class TransitionBuilder<T : FragmentElement<T>> {
    private val groups = ArraySet<TransitionGroup<T>>()

    inline fun group(block: TransitionGroupBuilder<T>.() -> Unit) {
        val g = TransitionGroupBuilder<T>().also(block).build()

        group(g)
    }

    fun group(g: TransitionGroup<T>) {
        groups.add(g)
    }

    fun build(): ComplexRangeTransition<T> {
        return ComplexRangeTransition.create(groups)
    }
}