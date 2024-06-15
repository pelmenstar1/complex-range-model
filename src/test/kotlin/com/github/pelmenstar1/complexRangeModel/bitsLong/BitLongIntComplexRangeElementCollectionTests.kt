package com.github.pelmenstar1.complexRangeModel.bitsLong

import com.github.pelmenstar1.complexRangeModel.AbstractBitComplexRangeElementCollectionTests
import com.github.pelmenstar1.complexRangeModel.BitLongIntComplexRange
import com.github.pelmenstar1.complexRangeModel.IntComplexRange
import com.github.pelmenstar1.complexRangeModel.fragment

class BitLongIntComplexRangeElementCollectionTests : AbstractBitComplexRangeElementCollectionTests() {
    override fun createComplexRange(ranges: Array<IntRange>, limitStart: Int): IntComplexRange {
        return BitLongIntComplexRange(limitStart) {
            ranges.forEach { fragment(it) }
        }
    }

    override fun maxBits(): Int {
        return 64
    }
}