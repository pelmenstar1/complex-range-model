package com.github.pelmenstar1.complexRangeModel

import com.github.pelmenstar1.complexRangeModel.generic.GenericComplexRange

interface ComplexRange<T : Comparable<T>> : Collection<RangeFragment<T>> {
    fun modify(block: ComplexRangeModify<T>.() -> Unit): ComplexRange<T>

    companion object {
        fun<T : Comparable<T>> empty(): ComplexRange<T> = GenericComplexRange.empty()
    }
}




