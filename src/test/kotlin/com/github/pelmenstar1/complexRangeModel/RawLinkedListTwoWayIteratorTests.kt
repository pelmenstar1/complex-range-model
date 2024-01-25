package com.github.pelmenstar1.complexRangeModel

class RawLinkedListTwoWayIteratorTests : BaseTwoWayIteratorTests<Int>() {
    override fun createIterator(elements: Array<Int>): TwoWayIterator<Int> {
        return RawLinkedList<Int>().apply {
            elements.forEach { add(it) }
        }.twoWayIterator()
    }

    override fun createArray(size: Int): Array<Int?> = arrayOfNulls(size)

    override fun iterateForwardBackwardDataset(): List<Array<Int>> {
        return listOf(
            emptyArray(),
            arrayOf(1),
            arrayOf(1, 2),
            arrayOf(1, 2, 3),
        )
    }

    override fun subIteratorDataset(): List<SubIteratorData<Int>> {
        return listOf(
            SubIteratorData(arrayOf(1), 0..0),
            SubIteratorData(arrayOf(1, 2), 0..1),
            SubIteratorData(arrayOf(1, 2, 3), 1..1),
            SubIteratorData(arrayOf(1, 2, 3), 1..2),
            SubIteratorData(arrayOf(1, 2, 3, 4), 1..2),
            SubIteratorData(arrayOf(1, 2, 3, 4), 1..3),
        )
    }

    override fun fillArrayDataset(): List<FillArrayData<Int>> {
        return listOf(
            FillArrayData(arrayOf(1), fillRange = 0..0),
            FillArrayData(arrayOf(1, 2), fillRange = 0..1),
            FillArrayData(arrayOf(1, 2, 3), fillRange = 0..2),
            FillArrayData(arrayOf(1, 2, 3, 4), fillRange = 0..2),
            FillArrayData(arrayOf(1, 2, 3, 4), fillRange = 1..2),
            FillArrayData(arrayOf(1, 2, 3, 4), fillRange = 1..3),
        )
    }
}