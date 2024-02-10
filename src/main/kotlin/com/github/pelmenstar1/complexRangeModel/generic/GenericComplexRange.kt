package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.*

internal class GenericComplexRange<T : FragmentElement<T>> internal constructor(
    private val fragments: RawLinkedList<RangeFragment<T>>
) : ComplexRange<T> {
    private val wrappedFragments = RawLinkedFragmentList(fragments)

    override fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T> {
        val copied = fragments.copyOf()
        GenericComplexRangeModify(copied).also(block)

        return GenericComplexRange(copied)
    }

    override fun fragments(): ComplexRangeFragmentList<T> = wrappedFragments

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is GenericComplexRange<*> -> fragments == other.fragments
            is ComplexRange<*> -> fragments.sequenceEquals(other.fragments())
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
            fragments.forEachForward { frag ->
                if (!isFirst) {
                    append(", ")
                }

                isFirst = false
                append(frag)
            }

            append(')')
        }
    }
}

private class RawLinkedFragmentList<T : FragmentElement<T>>(
    private val list: RawLinkedList<RangeFragment<T>>
) : ComplexRangeFragmentList<T> {
    override val size: Int
        get() = list.size

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun get(index: Int): RangeFragment<T> = list[index]
    override fun last(): RangeFragment<T> {
        return list.lastValue
    }

    override fun contains(element: RangeFragment<T>): Boolean = list.contains(element)
    override fun containsAll(elements: Collection<RangeFragment<T>>): Boolean = list.containsAll(elements)

    override fun indexOf(element: RangeFragment<T>): Int = list.indexOf(element)
    override fun lastIndexOf(element: RangeFragment<T>): Int = list.lastIndexOf(element)

    override fun subList(fromIndex: Int, toIndex: Int): List<RangeFragment<T>> {
        val newList = list.subList(fromIndex, toIndex)

        return RawLinkedFragmentList(newList)
    }

    override fun iterator(): Iterator<RangeFragment<T>> = list.iterator()
    override fun listIterator(): ListIterator<RangeFragment<T>> = list.listIterator()
    override fun listIterator(index: Int): ListIterator<RangeFragment<T>> = list.listIterator(index)

    override fun fragmentIterator(): ComplexRangeFragmentListIterator<T> {
        return IteratorImpl()
    }

    private inner class IteratorImpl : ComplexRangeFragmentListIterator<T> {
        private var currentNode: RawLinkedList.Node<RangeFragment<T>>? = null
        private var currentNodeIndex = -1

        private var markedNode: RawLinkedList.Node<RangeFragment<T>>? = null
        private var markedNodeIndex = -1

        override val current: RangeFragment<T>
            get() = currentNode?.value ?: throw IllegalStateException("No current element")

        override fun moveNext(): Boolean {
            var cn = currentNode
            if (cn == null) {
                cn = list.head ?: return false
                currentNode = cn
                currentNodeIndex = 0

                return true
            }

            val next = cn.next ?: return false

            currentNode = next
            currentNodeIndex++

            return true
        }

        override fun movePrevious(): Boolean {
            val cn = currentNode ?: return false
            val prev = cn.previous ?: return false

            currentNode = prev
            currentNodeIndex--

            return true
        }

        override fun mark() {
            markedNode = currentNode
            markedNodeIndex = currentNodeIndex
        }

        override fun subRange(): ComplexRange<T> {
            val startNode = markedNode ?: throw IllegalStateException("No marked element")
            val endNode = currentNode ?: throw IllegalStateException("Iteration is not started")
            val subSize = currentNodeIndex - markedNodeIndex + 1
            if (subSize < 0) {
                throw IllegalStateException("Current element's index is less than the marked node index")
            }

            val subList = RawLinkedList(startNode, endNode, subSize)
            return GenericComplexRange(subList)
        }
    }
}

@PublishedApi // For the builder method.
internal class GenericComplexRangeBuilder<T : FragmentElement<T>> : GenericComplexRangeBaseBuilder<T>(), ComplexRangeBuilder<T> {
    override fun fragment(value: RangeFragment<T>) {
        includeFragment(value)
    }

    fun build(): ComplexRange<T> {
        return GenericComplexRange(fragments)
    }
}

internal class GenericComplexRangeModify<T : FragmentElement<T>>(
    fragments: RawLinkedList<RangeFragment<T>>
) : GenericComplexRangeBaseBuilder<T>(fragments), ComplexRangeModify<T> {
    override fun set(fragment: RangeFragment<T>) {
        includeFragment(fragment)
    }

    override fun unset(fragment: RangeFragment<T>) {
        excludeFragment(fragment)
    }
}