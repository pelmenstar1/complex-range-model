package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.AbstractBitComplexRangeElementCollectionTests
import com.github.pelmenstar1.complexRangeModel.BitIntComplexRange
import com.github.pelmenstar1.complexRangeModel.IntComplexRange
import com.github.pelmenstar1.complexRangeModel.fragment

class BitIntComplexRangeElementCollectionTests : AbstractBitComplexRangeElementCollectionTests() {
    override fun createComplexRange(ranges: Array<IntRange>, limitStart: Int): IntComplexRange {
        return BitIntComplexRange(limitStart, limitStart + 128) {
            ranges.forEach { fragment(it) }
        }
    }

    override fun maxBits(): Int {
        return 128
    }
}