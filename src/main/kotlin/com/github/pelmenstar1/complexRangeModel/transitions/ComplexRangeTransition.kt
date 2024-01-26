package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.ArraySet

interface ComplexRangeTransition<T> : Set<TransitionGroup<T>> {
    operator fun get(index: Int): TransitionGroup<T>

    companion object {
        fun<T> create(groups: List<TransitionGroup<T>>): ComplexRangeTransition<T> {
            val set = ArraySet(groups)

            return create(set)
        }

        fun<T> create(groups: ArraySet<TransitionGroup<T>>): ComplexRangeTransition<T> {
            return ArraySetComplexRangeTransition(groups)
        }
    }
}

private class ArraySetComplexRangeTransition<T>(
    private val groups: ArraySet<TransitionGroup<T>>
): ComplexRangeTransition<T> {
    override val size: Int
        get() = groups.size

    override fun isEmpty() = groups.isEmpty()

    override fun get(index: Int): TransitionGroup<T> = groups[index]

    override fun contains(element: TransitionGroup<T>) = groups.contains(element)
    override fun containsAll(elements: Collection<TransitionGroup<T>>) = groups.containsAll(elements)

    @Suppress("UNCHECKED_CAST")
    override fun equals(other: Any?): Boolean {
       if (other === this) return true

        if (other is ComplexRangeTransition<*>) {
            other as ComplexRangeTransition<T>

            val s = size
            if (s != other.size) {
                return false
            }

            for (i in 0 until s) {
                val group = groups[i]

                if (!other.contains(group)) {
                    return false
                }
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int = groups.hashCode()

    override fun iterator(): Iterator<TransitionGroup<T>> = groups.iterator()

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

inline fun<T> ComplexRangeTransition(block: TransitionBuilder<T>.() -> Unit): ComplexRangeTransition<T> {
    return TransitionBuilder<T>().also(block).build()
}