package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.BitIntComplexRange
import com.github.pelmenstar1.complexRangeModel.IntComplexRange
import com.github.pelmenstar1.complexRangeModel.fragment

class BitIntComplexRangeTransitionManagerTests : BaseComplexRangeTransitionManagerTests() {
    override fun createComplexRange(ranges: List<IntRange>): IntComplexRange {
        return BitIntComplexRange(0, 100) {
            ranges.forEach { fragment(it) }
        }
    }
}