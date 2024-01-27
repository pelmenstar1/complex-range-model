package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.*
import com.github.pelmenstar1.complexRangeModel.IntRangeFragment
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRange

class BitArrayComplexRangeBuilder(private val limitStart: Int, private val limitEnd: Int) : ComplexRangeBuilder<IntFragmentElement> {
    private val bitSet = FixedBitSet(limitEnd - limitStart)

    override fun fragment(value: RangeFragment<IntFragmentElement>) {
        val fragStart = value.start.value
        val fragEnd = value.endInclusive.value

        ensureValidRange(fragStart, fragEnd)

        bitSet.set(fragStart - limitStart, fragEnd - limitStart)
    }

    private fun isValidRange(start: Int, endInclusive: Int): Boolean {
        return start >= limitStart && endInclusive <= limitEnd
    }

    private fun ensureValidRange(start: Int, endInclusive: Int) {
        if (!isValidRange(start, endInclusive)) {
            throw IllegalArgumentException("Specified range is out of the limiting range")
        }
    }

    private inline fun forEachRange(block: (start: Int, endInclusive: Int) -> Unit) {
        var start = 0

        while(true) {
            val (rangeStart, rangeEnd) = bitSet.findNextSetBitRange(start)
            if (rangeStart > rangeEnd) {
                break
            }

            start = rangeEnd + 1
            block(rangeStart + limitStart, rangeEnd + limitStart)
        }
    }

    fun build(): ComplexRange<IntFragmentElement> {
        val list = RawLinkedList<RangeFragment<IntFragmentElement>>()
        forEachRange { start, endInclusive ->
            list.add(IntRangeFragment(start, endInclusive))
        }

        return GenericComplexRange(list)
    }
}