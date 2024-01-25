package com.github.pelmenstar1.complexRangeModel.transitions

class ComplexRangeTransition<T>(val groups: List<TransitionGroup<T>>) {
    override fun equals(other: Any?): Boolean {
        return other is ComplexRangeTransition<*> && groups == other.groups
    }

    override fun hashCode(): Int {
        return groups.hashCode()
    }

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

    companion object {
        private val EMPTY = ComplexRangeTransition<Nothing>(emptyList())

        @Suppress("UNCHECKED_CAST")
        fun<T> empty() = EMPTY as ComplexRangeTransition<T>
    }
}

inline fun<T> ComplexRangeTransition(block: TransitionBuilder<T>.() -> Unit): ComplexRangeTransition<T> {
    return TransitionBuilder<T>().also(block).build()
}