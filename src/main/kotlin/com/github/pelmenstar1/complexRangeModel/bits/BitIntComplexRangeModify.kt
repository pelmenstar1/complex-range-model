package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.ComplexRangeModify
import com.github.pelmenstar1.complexRangeModel.IntFragmentElement
import com.github.pelmenstar1.complexRangeModel.RangeFragment

internal class BitIntComplexRangeModify(
    private val bitSet: FixedBitSet,
    private val limitStart: Int,
    private val limitEnd: Int
) : ComplexRangeModify<IntFragmentElement> {
    override fun set(fragment: RangeFragment<IntFragmentElement>) {
        operation(fragment, FixedBitSet::set)
    }

    override fun unset(fragment: RangeFragment<IntFragmentElement>) {
        operation(fragment, FixedBitSet::unset)
    }

    private inline fun operation(fragment: RangeFragment<IntFragmentElement>, method: FixedBitSet.(Int, Int) -> Unit) {
        val start = fragment.start.value
        val end = fragment.endInclusive.value

        ensureValidRange(start, end)

        bitSet.method(start - limitStart, end - limitStart)
    }

    private fun ensureValidRange(start: Int, endInclusive: Int) {
        if (!isValidRange(start, endInclusive)) {
            throw IllegalArgumentException("Given fragment is out of bounds")
        }
    }

    private fun isValidRange(start: Int, endInclusive: Int): Boolean {
        return start >= limitStart && endInclusive <= limitEnd
    }
}