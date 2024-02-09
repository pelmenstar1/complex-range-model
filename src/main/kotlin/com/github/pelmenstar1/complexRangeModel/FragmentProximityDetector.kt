package com.github.pelmenstar1.complexRangeModel

/**
 * Responsible for detecting a relative proximity between fragments.
 */
interface FragmentProximityDetector<T : FragmentElement<T>> {
    /**
     * Determines whether [first] fragment can be moved to [second] one.
     */
    fun canMove(first: RangeFragment<T>, second: RangeFragment<T>): Boolean

    companion object {
        private val NO_MOVE = object : FragmentProximityDetector<Nothing> {
            override fun canMove(first: RangeFragment<Nothing>, second: RangeFragment<Nothing>) = false
        }

        /**
         * Returns an instance of [FragmentProximityDetector] that forbids all the movement.
         */
        @Suppress("UNCHECKED_CAST")
        fun<T : FragmentElement<T>> noMove() = NO_MOVE as FragmentProximityDetector<T>

        /**
         * Returns an instance of [FragmentProximityDetector] that enables movement
         * only if the "raw" distance between two fragments is less than or equal to [maxDist] value.
         */
        fun<T : FragmentElement<T>> withRawMoveDistance(maxDist: Int): FragmentProximityDetector<T> {
            return object : FragmentProximityDetector<T> {
                override fun canMove(first: RangeFragment<T>, second: RangeFragment<T>): Boolean {
                    return first.getRawDistanceTo(second) <= maxDist
                }
            }
        }

        /**
         * Returns an instance of [FragmentProximityDetector] that enables movement
         * only if the distance between two fragments is less than or equal to [maxDist] value.
         */
        fun<T : DistanceFragmentElement<T, D>, D> withMoveDistance(maxDist: D): FragmentProximityDetector<T> {
            return object : FragmentProximityDetector<T> {
                override fun canMove(first: RangeFragment<T>, second: RangeFragment<T>): Boolean {
                    return first.isDistanceLessThanOrEqual(second, maxDist)
                }
            }
        }
    }
}