package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.BitLongIntComplexRange
import com.github.pelmenstar1.complexRangeModel.IntComplexRange
import com.github.pelmenstar1.complexRangeModel.fragment

class BitLongIntComplexRangeTransitionManagerTests : BaseComplexRangeTransitionManagerTests() {
    override fun createComplexRange(ranges: List<IntRange>): IntComplexRange {
        return BitLongIntComplexRange(-1) {
            ranges.forEach { fragment(it) }
        }
    }
}