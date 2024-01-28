package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.FragmentElement

interface ComplexRangeTransition<T : FragmentElement<T>> {
    fun groups(): Set<TransitionGroup<T>>

    fun reversed(): ComplexRangeTransition<T>

    companion object {
        fun <T : FragmentElement<T>> create(groups: List<TransitionGroup<T>>): ComplexRangeTransition<T> {
            return create(HashSet(groups))
        }

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