package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.FragmentElement
import com.github.pelmenstar1.complexRangeModel.emptyIterator
import com.github.pelmenstar1.complexRangeModel.sequenceEquals
import com.github.pelmenstar1.complexRangeModel.singleValueIterator

interface TransitionGroup<T : FragmentElement<T>> : Collection<TransitionOperation<T>> {
    fun reversed(): TransitionGroup<T>

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : FragmentElement<T>> empty(): TransitionGroup<T> = EmptyTransitionGroup as TransitionGroup<T>

        fun <T : FragmentElement<T>> create(op: TransitionOperation<T>): TransitionGroup<T> {
            return SingleOpTransitionGroup(op)
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
    override val size: Int
        get() = 0

    override fun reversed(): TransitionGroup<Nothing> = EmptyTransitionGroup

    override fun isEmpty() = true

    override fun contains(element: TransitionOperation<Nothing>) = false
    override fun containsAll(elements: Collection<TransitionOperation<Nothing>>) = false

    override fun iterator(): Iterator<TransitionOperation<Nothing>> = emptyIterator()

    override fun equals(other: Any?): Boolean {
        return if (other is TransitionGroup<*>) other.isEmpty() else false
    }

    // Return 0 to make CollectionTransitionGroup's hash (when empty) and EmptyTransitionGroup's hashes equal
    override fun hashCode(): Int = 0

    override fun toString(): String = "TransitionGroup()"
}

private class SingleOpTransitionGroup<T : FragmentElement<T>>(
    val singleValue: TransitionOperation<T>
) : TransitionGroup<T> {
    override val size: Int
        get() = 1

    override fun isEmpty(): Boolean = false

    override fun reversed(): TransitionGroup<T> = this

    override fun iterator(): Iterator<TransitionOperation<T>> {
        return singleValueIterator(singleValue)
    }

    override fun containsAll(elements: Collection<TransitionOperation<T>>): Boolean {
        return elements.all { singleValue == it }
    }

    override fun contains(element: TransitionOperation<T>): Boolean {
        return singleValue == element
    }

    override fun equals(other: Any?): Boolean {
        if (other is SingleOpTransitionGroup<*>) {
            return other.singleValue == singleValue
        } else if (other is TransitionGroup<*>) {
            val otherIter = other.iterator()

            if (otherIter.hasNext()) {
                val otherFirst = otherIter.next()

                if (!otherIter.hasNext()) {
                    return otherFirst == singleValue
                }
            }
        }

        return false
    }

    override fun hashCode(): Int {
        return singleValue.hashCode()
    }

    override fun toString(): String {
        return "TransitionGroup($singleValue)"
    }
}

private class CollectionTransitionGroup<T : FragmentElement<T>>(
    private val ops: List<TransitionOperation<T>>
) : TransitionGroup<T> {
    override val size: Int
        get() = ops.size

    override fun isEmpty() = ops.isEmpty()

    override fun reversed(): TransitionGroup<T> {
        val size = ops.size
        if (size == 1) {
            return SingleOpTransitionGroup(ops[0].reversed())
        }

        val revOps = ArrayList<TransitionOperation<T>>(size)

        for (i in 0 until size) {
            val op = ops[size - i - 1]

            revOps.add(op.reversed())
        }

        return CollectionTransitionGroup(revOps)
    }

    override fun contains(element: TransitionOperation<T>) = ops.contains(element)
    override fun containsAll(elements: Collection<TransitionOperation<T>>) = ops.containsAll(elements)

    override fun iterator() = ops.iterator()

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is CollectionTransitionGroup<*> -> ops == other.ops
            is SingleOpTransitionGroup<*> -> ops.size == 1 && ops[0] == other.singleValue
            is EmptyTransitionGroup -> ops.isEmpty()
            is TransitionGroup<*> -> sequenceEquals(other)
            else -> false
        }
    }

    override fun hashCode(): Int {
        // Initial value is important:
        // It's zero and not, for example, one, to make SingleOpTransitionGroup's and
        // CollectionTransitionGroup's (when single and same element) equal.
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
