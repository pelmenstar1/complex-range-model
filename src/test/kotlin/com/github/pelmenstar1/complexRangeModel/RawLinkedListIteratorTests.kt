package com.github.pelmenstar1.complexRangeModel

class RawLinkedListIteratorTests : BaseListIteratorTests<Int>() {
    override fun createIterator(elements: Array<Int>): MutableListIterator<Int> {
        return RawLinkedList<Int>().apply {
            elements.forEach { add(it) }
        }.listIterator()
    }

    override fun iterateForwardBackwardDataset(): List<Array<Int>> {
        return listOf(
            emptyArray(),
            arrayOf(1),
            arrayOf(1, 2),
            arrayOf(1, 2, 3),
        )
    }
}