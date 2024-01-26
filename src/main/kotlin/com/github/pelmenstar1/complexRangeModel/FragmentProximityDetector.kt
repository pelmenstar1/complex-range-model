package com.github.pelmenstar1.complexRangeModel

interface FragmentProximityDetector<T> {
    fun canMove(first: RangeFragment<T>, second: RangeFragment<T>): Boolean

    companion object {
        private val NO_MOVE = object : FragmentProximityDetector<Nothing> {
            override fun canMove(first: RangeFragment<Nothing>, second: RangeFragment<Nothing>) = false
        }

        @Suppress("UNCHECKED_CAST")
        fun<T> noMove() = NO_MOVE as FragmentProximityDetector<T>

        fun<T> withMoveDistance(support: FragmentElementSupport<T>, maxDist: T): FragmentProximityDetector<T> {
            return object : FragmentProximityDetector<T> {
                override fun canMove(first: RangeFragment<T>, second: RangeFragment<T>): Boolean {
                    return support.compare(first.distanceTo(second), maxDist) <= 0
                }
            }
        }
    }
}