package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.IntComplexRange

class GenericComplexRangeTransitionManagerTests : BaseComplexRangeTransitionManagerTests() {
    override fun createComplexRange(ranges: List<IntRange>): IntComplexRange {
        return IntComplexRange(ranges.toTypedArray())
    }
}