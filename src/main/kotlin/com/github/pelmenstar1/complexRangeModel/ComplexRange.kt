package com.github.pelmenstar1.complexRangeModel

class ComplexRange<T : Comparable<T>> internal constructor(
    private val fragments: RangeFragmentList<T>
) : Collection<RangeFragment<T>> {
    override val size: Int
        get() = fragments.size

    override fun isEmpty() = fragments.isEmpty()

    fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T> {
        val copied = fragments.copyOf()
        ComplexRangeModify(copied).also(block)

        return ComplexRange(copied)
    }

    operator fun get(index: Int): RangeFragment<T> {
        return fragments[index]
    }

    override fun containsAll(elements: Collection<RangeFragment<T>>): Boolean {
        return elements.all { contains(it) }
    }

    override fun contains(element: RangeFragment<T>): Boolean {
        return fragments.contains(element)
    }

    override fun equals(other: Any?): Boolean {
        return other is ComplexRange<*> && fragments == other.fragments
    }

    override fun hashCode(): Int {
        return fragments.hashCode()
    }

    override fun toString(): String {
        return buildString {
            append("ComplexRange(")

            fragments.forEachIndexed { i, range ->
                append(range)

                if (i < fragments.size - 1) {
                    append(", ")
                }
            }

            append(')')
        }
    }

    override fun iterator(): Iterator<RangeFragment<T>> {
        return fragments.iterator()
    }

    companion object {
        fun <T : Comparable<T>> empty() = ComplexRange<T>(RangeFragmentList())
    }
}

inline fun <T : Comparable<T>> ComplexRange(block: ComplexRangeBuilder<T>.() -> Unit): ComplexRange<T> {
    return ComplexRangeBuilder<T>().also(block).build()
}

inline fun IntComplexRange(block: ComplexRangeBuilder<Int>.() -> Unit): ComplexRange<Int> {
    return ComplexRange(block)
}

fun IntComplexRange(ranges: Array<out IntRange>): ComplexRange<Int> {
    return IntComplexRange {
        ranges.forEach { fragment(it) }
    }
}


