package com.github.pelmenstar1.complexRangeModel

internal fun <T> Iterable<T>.sequenceEquals(other: Iterable<T>): Boolean {
    return iterator().contentEquals(other.iterator())
}

internal fun <T> Iterator<T>.contentEquals(otherIterator: Iterator<T>): Boolean {
    while (hasNext()) {
        if (!otherIterator.hasNext()) {
            return false
        }

        val thisValue = next()
        val otherValue = otherIterator.next()

        if (thisValue != otherValue) {
            return false
        }
    }

    return !otherIterator.hasNext()
}

private object EmptyIterator : ListIterator<Nothing> {
    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun next(): Nothing = throwIteratorEmpty()
    override fun previous(): Nothing = throwIteratorEmpty()

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1

    private fun throwIteratorEmpty(): Nothing {
        throw IllegalStateException("Iterator is empty")
    }
}

internal fun <T> emptyIterator(): ListIterator<T> = EmptyIterator

internal class LimitingListIterator<T>(
    private val baseIterator: ListIterator<T>,
    private val maxSize: Int
) : ListIterator<T> {
    private var index = 0

    override fun hasNext(): Boolean {
        return index < maxSize && baseIterator.hasNext()
    }

    override fun hasPrevious(): Boolean {
        return baseIterator.hasPrevious()
    }

    override fun next(): T {
        if (index >= maxSize) {
            throw NoSuchElementException()
        }
        index++

        return baseIterator.next()
    }

    override fun previous(): T {
        index--
        return baseIterator.previous()
    }

    override fun nextIndex(): Int = baseIterator.nextIndex()
    override fun previousIndex(): Int = baseIterator.previousIndex()
}

internal fun<T> ListIterator<T>.limitTo(size: Int): ListIterator<T> {
    return LimitingListIterator(this, size)
}