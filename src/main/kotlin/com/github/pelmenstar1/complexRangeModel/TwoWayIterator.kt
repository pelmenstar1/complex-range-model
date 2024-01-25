package com.github.pelmenstar1.complexRangeModel

interface TwoWayIterator<T> : ListIterator<T> {
    val size: Int

    fun mark()
    fun subIterator(): TwoWayIterator<T>

    fun fillArray(array: Array<in T>) {
        for (i in array.indices) {
            array[i] = next()
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun<T> empty(): TwoWayIterator<T> = EmptyTwoWayIterator as TwoWayIterator<T>
    }
}

@Suppress("UNCHECKED_CAST")
inline fun<reified T> TwoWayIterator<T>.toTypedArray(count: Int): Array<T> {
    val array = arrayOfNulls<T>(count)
    fillArray(array)

    return array as Array<T>
}

private object EmptyTwoWayIterator : TwoWayIterator<Nothing> {
    override val size: Int
        get() = 0

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun next(): Nothing = throwEmptyIterator()
    override fun previous(): Nothing = throwEmptyIterator()

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1

    override fun mark() {
    }

    override fun subIterator(): TwoWayIterator<Nothing> = this

    override fun fillArray(array: Array<*>) {
    }

    private fun throwEmptyIterator(): Nothing {
        throw IllegalStateException("Empty iterator")
    }
}