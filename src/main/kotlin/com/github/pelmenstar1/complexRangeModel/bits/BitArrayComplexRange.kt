package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.*
import com.github.pelmenstar1.complexRangeModel.IntRangeFragment

class BitArrayComplexRange internal constructor(private val ranges: BitArrayRangeSet) : ComplexRange<Int> {
    override val size: Int
        get() = ranges.size

    override fun isEmpty(): Boolean {
        return ranges.isEmpty()
    }

    operator fun get(index: Int): RangeFragment<Int> {
        return IntRangeFragment(ranges[index])
    }

    override fun modify(block: ComplexRangeModify<Int>.() -> Unit): ComplexRange<Int> {
        val newFragments = ranges.copy()
        BitArrayComplexRangeModify(newFragments).also(block)

        return BitArrayComplexRange(newFragments)
    }

    override fun contains(element: RangeFragment<Int>): Boolean {
        return ranges.contains(element.start, element.endInclusive)
    }

    override fun containsAll(elements: Collection<RangeFragment<Int>>): Boolean {
        return elements.all { contains(it) }
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is BitArrayComplexRange -> ranges == other.ranges
            is ComplexRange<*> -> sequenceEquals(other)
            else -> false
        }
    }

    override fun hashCode(): Int {
        return ranges.hashCode()
    }

    override fun toString(): String {
        return buildString {
            append("ComplexRange(")

            var isFirst = true
            ranges.forEachRange { start, endInclusive ->
                if (!isFirst) {
                    append(", ")
                }

                isFirst = false

                append('[')
                append(start)
                append(", ")
                append(endInclusive)
                append(']')
            }

            append(')')
        }
    }

    override fun iterator(): Iterator<RangeFragment<Int>> {
        val rangeIter = ranges.iterator()

        return object : Iterator<RangeFragment<Int>> {
            override fun hasNext() = rangeIter.hasNext()

            override fun next(): RangeFragment<Int> {
                return IntRangeFragment(rangeIter.next())
            }
        }
    }
}

class BitArrayComplexRangeBuilder(limitStart: Int, limitEnd: Int) : ComplexRangeBuilder<Int> {
    private val fragments = BitArrayRangeSet(limitStart, limitEnd)

    override fun fragment(value: RangeFragment<Int>) {
        fragments.add(value.start, value.endInclusive)
    }

    fun build(): BitArrayComplexRange {
        return BitArrayComplexRange(fragments)
    }
}

private class BitArrayComplexRangeModify(private val fragments: BitArrayRangeSet) : ComplexRangeModify<Int> {
    override fun set(fragment: RangeFragment<Int>) {
        fragments.add(fragment.start, fragment.endInclusive)
    }

    override fun unset(fragment: RangeFragment<Int>) {
        fragments.remove(fragment.start, fragment.endInclusive)
    }
}