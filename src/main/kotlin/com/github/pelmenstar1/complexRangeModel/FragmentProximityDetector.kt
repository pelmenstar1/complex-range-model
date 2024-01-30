package com.github.pelmenstar1.complexRangeModel

interface FragmentProximityDetector<T : FragmentElement<T>> {
    fun canMove(first: RangeFragment<T>, second: RangeFragment<T>): Boolean

    companion object {
        private val NO_MOVE = object : FragmentProximityDetector<Nothing> {
            override fun canMove(first: RangeFragment<Nothing>, second: RangeFragment<Nothing>) = false
        }

        @Suppress("UNCHECKED_CAST")
        fun<T : FragmentElement<T>> noMove() = NO_MOVE as FragmentProximityDetector<T>

        fun<T : FragmentElement<T>> withRawMoveDistance(maxDist: Int): FragmentProximityDetector<T> {
            return object : FragmentProximityDetector<T> {
                override fun canMove(first: RangeFragment<T>, second: RangeFragment<T>): Boolean {
                    return first.getRawDistanceTo(second) <= maxDist
                }
            }
        }

        fun<T : DistanceFragmentElement<T, D>, D> withMoveDistance(maxDist: D): FragmentProximityDetector<T> {
            return object : FragmentProximityDetector<T> {
                override fun canMove(first: RangeFragment<T>, second: RangeFragment<T>): Boolean {
                    return first.isDistanceLessThanOrEqual(second, maxDist)
                }
            }
        }
    }
}