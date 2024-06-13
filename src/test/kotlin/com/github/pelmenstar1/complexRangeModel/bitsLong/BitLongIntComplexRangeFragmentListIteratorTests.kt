package com.github.pelmenstar1.complexRangeModel.bitsLong

import com.github.pelmenstar1.complexRangeModel.*

class BitLongIntComplexRangeFragmentListIteratorTests : BaseComplexRangeFragmentListIteratorTests() {
    override fun createIterator(data: Array<IntRangeFragment>): ComplexRangeFragmentListIterator<IntFragmentElement> {
        return BitLongIntComplexRange(limitStart = -1) {
            data.forEach { fragment(it) }
        }.fragments().fragmentIterator()
    }

    override fun iterateForwardBackwardTestData(): List<Array<IntRange>> {
        return listOf(
            arrayOf(1..2),
            arrayOf(0..2),
            arrayOf(0..62),
            arrayOf(1..2, 4..5),
            arrayOf(0..2, 4..5),
            arrayOf(1..2, 4..5, 7..8),
            arrayOf(0..2, 4..5, 7..8, 40..62),
        )
    }
}