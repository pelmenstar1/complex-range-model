package com.github.pelmenstar1.complexRangeModel

fun<T> Iterable<T>.sequenceEquals(other: Iterable<T>): Boolean {
    val thisIterator = iterator()
    val otherIterator = other.iterator()

    while(thisIterator.hasNext()) {
        if (!otherIterator.hasNext()) {
            return false
        }

        val thisValue = thisIterator.next()
        val otherValue = otherIterator.next()

        if (thisValue != otherValue) {
            return false
        }
    }

    return !otherIterator.hasNext()
}