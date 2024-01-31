package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.*
import com.github.pelmenstar1.complexRangeModel.IntRangeFragment
import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRange

@PublishedApi // For the builder method.
internal class BitArrayComplexRangeBuilder(private val limitStart: Int, private val limitEnd: Int) : ComplexRangeBuilder<IntFragmentElement> {
    private val bitSet = FixedBitSet(limitEnd - limitStart + 1)

    override fun fragment(value: IntRangeFragment) {
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

    fun build(): IntComplexRange {
        val list = RawLinkedList<IntRangeFragment>()
        bitSet.forEachRange { start, endInclusive ->
            list.add(IntRangeFragment(start + limitStart, endInclusive + limitStart))
        }

        return GenericComplexRange(list)
    }
}