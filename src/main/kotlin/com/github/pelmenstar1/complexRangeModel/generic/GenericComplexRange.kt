package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.*

class GenericComplexRange<T : Comparable<T>> internal constructor(
    private val fragments: RangeFragmentLinkedList<T>
) : ComplexRange<T> {
    override val size: Int
        get() = fragments.size

    override fun isEmpty() = fragments.isEmpty()

    override fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T> {
        val copied = fragments.copyOf()
        GenericComplexRangeModify(copied).also(block)

        return GenericComplexRange(copied)
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
        return when(other) {
            is GenericComplexRange<*> -> fragments == other.fragments
            is ComplexRange<*> -> sequenceEquals(other)
            else -> false
        }
    }

    override fun hashCode(): Int {
        return fragments.hashCode()
    }

    override fun toString(): String {
        return buildString {
            append("ComplexRange(")

            var isFirst = true
            fragments.forEach { frag ->
                if (!isFirst) {
                    append(", ")
                }

                isFirst = false
                append(frag)
            }

            append(')')
        }
    }

    override fun iterator(): Iterator<RangeFragment<T>> {
        return fragments.iterator()
    }

    companion object {
        fun <T : Comparable<T>> empty() = GenericComplexRange<T>(RangeFragmentLinkedList())
    }
}

class GenericComplexRangeBuilder<T : Comparable<T>> : GenericComplexRangeBaseBuilder<T>(), ComplexRangeBuilder<T> {
    override fun fragment(value: RangeFragment<T>) {
        includeFragment(value)
    }

    fun build(): ComplexRange<T> {
        return GenericComplexRange(fragments)
    }
}

class GenericComplexRangeModify<T : Comparable<T>>(
    fragments: RangeFragmentLinkedList<T>
) : GenericComplexRangeBaseBuilder<T>(fragments), ComplexRangeModify<T> {
    override fun set(fragment: RangeFragment<T>) {
        includeFragment(fragment)
    }

    override fun unset(fragment: RangeFragment<T>) {
        excludeFragment(fragment)
    }
}