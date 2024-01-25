package com.github.pelmenstar1.complexRangeModel

fun <T> Iterable<T>.sequenceEquals(other: Iterable<T>): Boolean {
    return iterator().contentEquals(other.iterator())
}

fun <T> Iterator<T>.contentEquals(otherIterator: Iterator<T>): Boolean {
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

fun <T> Iterator<T>.skip(n: Int) {
    var r = n

    while (r > 0) {
        next()
        r--
    }
}

inline fun<T, reified R> Array<T>.mapToArray(mapping: (T) -> R): Array<R> {
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

private class SingleValueIterator<T>(private val value: T) : ListIterator<T> {
    private var isEnded = false

    override fun hasNext(): Boolean = !isEnded
    override fun hasPrevious(): Boolean = isEnded

    override fun next(): T {
        if (isEnded) {
            throwNoElements()
        }

        isEnded = true
        return value
    }

    override fun previous(): T {
        if (!isEnded) {
            throwNoElements()
        }

        isEnded = false
        return value
    }

    override fun nextIndex(): Int = 1
    override fun previousIndex(): Int = if (isEnded) 0 else -1

    private fun throwNoElements(): Nothing {
        throw IllegalStateException("No elements")
    }
}

fun <T> emptyIterator(): ListIterator<T> = EmptyIterator
fun <T> singleValueIterator(value: T): ListIterator<T> = SingleValueIterator(value)