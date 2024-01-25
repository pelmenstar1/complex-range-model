package com.github.pelmenstar1.complexRangeModel.transitions

class TransitionBuilder<T> {
    private val groups = ArrayList<TransitionGroup<T>>()

    inline fun group(block: TransitionGroupBuilder<T>.() -> Unit) {
        val g = TransitionGroupBuilder<T>().also(block).build()

        group(g)
    }

    fun group(g: TransitionGroup<T>) {
        groups.add(g)
    }

    fun build(): ComplexRangeTransition<T> {
        return ComplexRangeTransition(groups)
    }
}