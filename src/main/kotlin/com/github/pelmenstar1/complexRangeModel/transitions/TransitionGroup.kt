package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.emptyIterator
import com.github.pelmenstar1.complexRangeModel.sequenceEquals
import com.github.pelmenstar1.complexRangeModel.singleValueIterator

interface TransitionGroup<T> : Collection<TransitionOperation<T>> {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T> empty(): TransitionGroup<T> = EmptyTransitionGroup as TransitionGroup<T>

        fun <T> create(op: TransitionOperation<T>): TransitionGroup<T> {
            return SingleOpTransitionGroup(op)
        }

        fun <T> create(list: Collection<TransitionOperation<T>>): TransitionGroup<T> {
            return CollectionTransitionGroup(list)
        }
    }
}

private object EmptyTransitionGroup : TransitionGroup<Nothing> {
    override val size: Int
        get() = 0

    override fun isEmpty() = true

    override fun contains(element: TransitionOperation<Nothing>) = false
    override fun containsAll(elements: Collection<TransitionOperation<Nothing>>) = false

    override fun iterator(): Iterator<TransitionOperation<Nothing>> = emptyIterator()

    override fun equals(other: Any?): Boolean {
        return if (other is TransitionGroup<*>) other.isEmpty() else false
    }

    override fun hashCode(): Int = 1

    override fun toString(): String = "TransitionGroup()"
}

private class SingleOpTransitionGroup<T>(
    val singleValue: TransitionOperation<T>
) : TransitionGroup<T> {
    override val size: Int
        get() = 1

    override fun isEmpty(): Boolean = false

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

private class CollectionTransitionGroup<T>(
    private val ops: Collection<TransitionOperation<T>>
) : TransitionGroup<T> {
    override val size: Int
        get() = ops.size

    override fun isEmpty() = ops.isEmpty()

    override fun containsAll(elements: Collection<TransitionOperation<T>>) = ops.containsAll(elements)
    override fun contains(element: TransitionOperation<T>) = ops.contains(element)

    override fun iterator() = ops.iterator()

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is CollectionTransitionGroup<*> -> ops == other.ops
            is SingleOpTransitionGroup<*> -> ops.size == 1 && ops.first() == other.singleValue
            is EmptyTransitionGroup -> ops.isEmpty()
            is TransitionGroup<*> -> sequenceEquals(other)
            else -> false
        }
    }

    override fun hashCode(): Int {
        return ops.hashCode()
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
