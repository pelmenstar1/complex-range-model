package com.github.pelmenstar1.complexRangeModel.bits

import com.github.pelmenstar1.complexRangeModel.*

class BitIntComplexRangeFragmentListIteratorTests : BaseComplexRangeFragmentListIteratorTests() {
    override fun createIterator(data: Array<IntRangeFragment>): ComplexRangeFragmentListIterator<IntFragmentElement> {
        return BitIntComplexRange(0, 127) {
            data.forEach { fragment(it) }
        }.fragments().fragmentIterator()
    }
}