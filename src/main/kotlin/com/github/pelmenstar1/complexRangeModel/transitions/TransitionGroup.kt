package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.FragmentElement
import com.github.pelmenstar1.complexRangeModel.sequenceEquals

interface TransitionGroup<T : FragmentElement<T>> {
    fun operations(): Collection<TransitionOperation<T>>
    fun efficiencyLevel(): Int
    fun reversed(): TransitionGroup<T>

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : FragmentElement<T>> empty() = EmptyTransitionGroup as TransitionGroup<T>

        fun <T : FragmentElement<T>> create(op: TransitionOperation<T>): TransitionGroup<T> {
            return create(listOf(op))
        }

        fun <T : FragmentElement<T>> create(ops: List<TransitionOperation<T>>): TransitionGroup<T> {
            return CollectionTransitionGroup(ops)
        }
    }
}

inline fun<T : FragmentElement<T>> TransitionGroup(block: TransitionGroupBuilder<T>.() -> Unit): TransitionGroup<T> {
    return TransitionGroupBuilder<T>().also(block).build()
}

private object EmptyTransitionGroup : TransitionGroup<Nothing> {
    override fun reversed(): TransitionGroup<Nothing> = this

    override fun operations(): Collection<TransitionOperation<Nothing>> = emptyList()

    override fun efficiencyLevel(): Int = 0

    override fun equals(other: Any?): Boolean {
        return other is TransitionGroup<*> && other.operations().isEmpty()
    }

    // Return 0 to make CollectionTransitionGroup's hash (when empty) and EmptyTransitionGroup's hashes equal
    override fun hashCode(): Int = 0

    override fun toString(): String = "TransitionGroup()"
}

private class CollectionTransitionGroup<T : FragmentElement<T>>(
    private val ops: List<TransitionOperation<T>>
) : TransitionGroup<T> {
    override fun reversed(): TransitionGroup<T> {
        val size = ops.size
        val revOps = ArrayList<TransitionOperation<T>>(size)

        for (i in 0 until size) {
            val op = ops[size - i - 1]

            revOps.add(op.reversed())
        }

        return CollectionTransitionGroup(revOps)
    }

    override fun efficiencyLevel(): Int {
        return ops.sumOf { it.efficiencyLevel() }
    }

    override fun operations(): Collection<TransitionOperation<T>> = ops

    override fun equals(other: Any?): Boolean {
        if (other === EmptyTransitionGroup) {
            return ops.isEmpty()
        }

        if (other is TransitionGroup<*>) {
            if (other is CollectionTransitionGroup<*>) {
                return ops == other.ops
            }

            return ops.sequenceEquals(other.operations())
        }

        return false
    }

    override fun hashCode(): Int {
        // Initial value is important:
        // It's zero and not, for example, one, to make EmptyTransitionGroup's and
        // CollectionTransitionGroup's (when empty) equal.
        //
        // Re-implement the hash computation, because different implementations of ops may use
        // different hash computation approaches. For instance, ArrayList's initial hash value is 1, which
        // breaks contract between equals and hashCode
        var result = 0
        ops.forEach {
            result = result * 31 + it.hashCode()
        }

        return result
    }

    override fun toString(): String {
        return buildString {
            append("TransitionGroup(")
            ops.forEachIndexed { i, op ->
                append(op)

                if (i < ops.size - 1) {
                    append(", ")
                }
            }
            append(')')
        }
    }
}
