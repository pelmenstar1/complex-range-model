package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.ArraySet

class TransitionBuilder<T> {
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