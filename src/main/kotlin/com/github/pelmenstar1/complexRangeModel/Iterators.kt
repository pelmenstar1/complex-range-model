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

internal inline fun<T, reified R> Array<T>.mapToArray(mapping: (T) -> R): Array<R> {
    return Array(size) { i -> mapping(get(i)) }
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