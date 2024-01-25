package com.github.pelmenstar1.complexRangeModel

fun interface FragmentProximityPredicate<T> {
    fun test(first: RangeFragment<T>, second: RangeFragment<T>): Boolean

    companion object {
        private val UNITING = FragmentProximityPredicate<Nothing> { first, second -> first.canUniteWith(second) }
        private val INTERSECTING = FragmentProximityPredicate<Nothing> { first, second -> first.canUniteWith(second) }

        @Suppress("UNCHECKED_CAST")
        fun<T> uniting() = UNITING as FragmentProximityPredicate<T>

        @Suppress("UNCHECKED_CAST")
        fun<T> intersecting() = INTERSECTING as FragmentProximityPredicate<T>
    }
}