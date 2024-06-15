package com.github.pelmenstar1.complexRangeModel.bitsLong

import com.github.pelmenstar1.complexRangeModel.*

class BitLongIntComplexRangeFragmentListIteratorTests : BaseComplexRangeFragmentListIteratorTests() {
    override fun createIterator(data: List<IntRangeFragment>): ComplexRangeFragmentListIterator<IntFragmentElement> {
        return BitLongIntComplexRange(limitStart = -1) {
            data.forEach { fragment(it) }
        }.fragments().fragmentIterator()
    }

    override fun iterateForwardBackwardTestData(): List<List<IntRange>> {
        return listOf(
            listOf(1..2),
            listOf(0..2),
            listOf(0..62),
            listOf(1..2, 4..5),
            listOf(0..2, 4..5),
            listOf(1..2, 4..5, 7..8),
            listOf(0..2, 4..5, 7..8, 40..62),
        )
    }
}