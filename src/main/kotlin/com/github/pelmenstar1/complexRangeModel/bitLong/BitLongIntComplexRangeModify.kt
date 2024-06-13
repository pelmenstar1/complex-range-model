package com.github.pelmenstar1.complexRangeModel.bitLong

import com.github.pelmenstar1.complexRangeModel.ComplexRangeModify
import com.github.pelmenstar1.complexRangeModel.IntFragmentElement
import com.github.pelmenstar1.complexRangeModel.RangeFragment
import com.github.pelmenstar1.complexRangeModel.bits.FixedBitSet
import com.github.pelmenstar1.complexRangeModel.bits.endMask
import com.github.pelmenstar1.complexRangeModel.bits.startMask

internal class BitLongIntComplexRangeModify(
    var bits: Long,
    private val limitStart: Int
) : ComplexRangeModify<IntFragmentElement> {
    override fun set(fragment: RangeFragment<IntFragmentElement>) {
        operation(fragment) { bits, mask -> bits or mask }
    }

    override fun unset(fragment: RangeFragment<IntFragmentElement>) {
        operation(fragment) { bits, mask -> bits and mask.inv() }
    }

    private inline fun operation(
        fragment: RangeFragment<IntFragmentElement>,
        aggregateMask: (bits: Long, mask: Long) -> Long
    ) {
        val bitStart = fragment.start.value - limitStart
        val bitEnd = fragment.endInclusive.value - limitStart

        if (!(bitStart >= 0 && bitEnd < 64)) {
            throw IllegalArgumentException("Given fragment is out of bounds")
        }

        val mask = startMask(bitStart) and endMask(bitEnd)

        bits = aggregateMask(bits, mask)
    }
}