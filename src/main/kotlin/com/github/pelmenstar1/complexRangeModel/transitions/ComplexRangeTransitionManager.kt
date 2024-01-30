package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.*

private typealias FragmentIterator<T> = ComplexRangeFragmentListIterator<T>

class ComplexRangeTransitionManager<T : FragmentElement<T>>(
    private val proxDetector: FragmentProximityDetector<T>
) {
    fun createTransition(origin: ComplexRange<T>, dest: ComplexRange<T>): ComplexRangeTransition<T> {
        val groups = HashSet<TransitionGroup<T>>()

        val originIter = origin.fragments().fragmentIterator()
        val destIter = dest.fragments().fragmentIterator()

        while(true) {
            val originHasNext = originIter.moveNext()
            val destHasNext = destIter.moveNext()

            if (!originHasNext || !destHasNext) {
                if (originHasNext) {
                    // Then destHasNext is false
                    addRemoveAllTransition(originIter, groups)
                } else if (destHasNext) {
                    addInsertAllTransition(destIter, groups)
                }

                break
            }

            originIter.mark()
            destIter.mark()

            val originFrag = originIter.current
            val destFrag = destIter.current

            if (originFrag != destFrag) {
                if (originFrag.overlapsWith(destFrag)) {
                    consumeElementsForTransformGroup(originFrag, originIter, destIter)

                    val originGroupRange = originIter.subRange()
                    val destGroupRange = destIter.subRange()

                    groups.add(createTransformGroup(originGroupRange, destGroupRange))
                } else {
                    if (proxDetector.canMove(originFrag, destFrag)) {
                        groups.add(TransitionGroup.create(TransitionOperation.Move(originFrag, destFrag)))
                    } else {
                        groups.add(TransitionGroup.create(TransitionOperation.Remove(originFrag)))
                        groups.add(TransitionGroup.create(TransitionOperation.Insert(destFrag)))
                    }
                }
            }
        }

        return ComplexRangeTransition.create(groups)
    }

    private fun addInsertAllTransition(iter: FragmentIterator<T>, groups: MutableSet<TransitionGroup<T>>) {
        addOperationAllTransition(iter, groups) { TransitionOperation.Insert(it) }
    }

    private fun addRemoveAllTransition(iter: FragmentIterator<T>, groups: MutableSet<TransitionGroup<T>>) {
        addOperationAllTransition(iter, groups) { TransitionOperation.Remove(it) }
    }

    private inline fun addOperationAllTransition(
        iter: FragmentIterator<T>,
        groups: MutableSet<TransitionGroup<T>>,
        createOp: (RangeFragment<T>) -> TransitionOperation<T>
    ) {
        do {
            val fragment = iter.current
            val op = createOp(fragment)

            groups.add(TransitionGroup.create(op))
        } while(iter.moveNext())
    }

    // Internal for tests
    internal fun consumeElementsForTransformGroup(
        firstOriginGroupFrag: RangeFragment<T>,
        originIter: FragmentIterator<T>,
        destIter: FragmentIterator<T>
    ) {
        var lastOriginFrag = firstOriginGroupFrag
        var lastDestFrag: RangeFragment<T>

        while (true) {
            val nonOverlappingDestFrag = consumeLaneForTransform(lastOriginFrag, destIter)
            lastDestFrag = destIter.current

            if (nonOverlappingDestFrag == null) {
                consumeLaneForTransform(lastDestFrag, originIter)
                break
            }

            if (lastOriginFrag == nonOverlappingDestFrag) {
                break
            }

            if (originIter.moveNext()) {
                val nextOriginFrag = originIter.current

                if (!nextOriginFrag.overlapsWith(lastDestFrag)) {
                    originIter.movePrevious()
                    break
                }
            }

            val nonOverlappingOriginFrag = consumeLaneForTransform(lastDestFrag, originIter)
            lastOriginFrag = originIter.current

            if (nonOverlappingOriginFrag == null) {
                consumeLaneForTransform(lastOriginFrag, destIter)

                break
            }

            if (lastDestFrag == nonOverlappingOriginFrag) {
                break
            }

            if (destIter.moveNext()) {
                val nextDestFrag = destIter.current

                if (!nextDestFrag.overlapsWith(lastOriginFrag)) {
                    destIter.movePrevious()
                    break
                }
            }
        }
    }

    // Returns the first fragment that doesn't overlap with anchorFrag or equal to anchorFrag.
    // If there's no such fragment, returns null
    private fun consumeLaneForTransform(anchorFrag: RangeFragment<T>, iter: FragmentIterator<T>): RangeFragment<T>? {
        while(iter.moveNext()) {
            val frag = iter.current

            if (frag == anchorFrag || !frag.overlapsWith(anchorFrag)) {
                iter.movePrevious()
                return frag
            }
        }

        return null
    }

    private fun createTransformGroup(
        originGroupRange: ComplexRange<T>,
        destGroupRange: ComplexRange<T>
    ): TransitionGroup<T> {
        val ops = ArrayList<TransitionOperation<T>>(3)

        val originFrags = originGroupRange.fragments()
        val destFrags = destGroupRange.fragments()

        val originSize = originFrags.size
        val destSize = destFrags.size

        if (originSize == 1) {
            val originFrag = originFrags[0]

            if (destSize == 1) {
                // Ops:
                // - Transform
                val destFrag = destFrags[0]

                ops.add(TransitionOperation.Transform(originFrag, destFrag))
            } else {
                // Ops:
                // - Transform
                // - Split

                val minFrag = destFrags[0]
                val maxFrag = destFrags.last()

                val destTransformFrag = RangeFragment(minFrag.start, maxFrag.endInclusive)

                if (originFrag != destTransformFrag) {
                    ops.add(TransitionOperation.Transform(originFrag, destTransformFrag))
                }

                ops.add(TransitionOperation.Split(destTransformFrag, destGroupRange))
            }
        } else {
            // originSize > 1
            val minOriginFrag = originFrags[0]
            val maxOriginFrag = originFrags.last()
            val originTransformFrag = RangeFragment(minOriginFrag.start, maxOriginFrag.endInclusive)

            if (destSize == 1) {
                val destFrag = destFrags.first()

                // Ops:
                // - Join
                // - Transform? (if joined fragment is not destination fragment)
                ops.add(TransitionOperation.Join(originGroupRange, originTransformFrag))

                if (destFrag != originTransformFrag) {
                    ops.add(TransitionOperation.Transform(originTransformFrag, destFrag))
                }
            } else {
                // Ops:
                // - Join
                // - Transform
                // - Split

                val minDestFrag = destFrags[0]
                val maxDestFrag = destFrags.last()

                val destTransformFrag = RangeFragment(minDestFrag.start, maxDestFrag.endInclusive)

                ops.add(TransitionOperation.Join(originGroupRange, originTransformFrag))

                if (originTransformFrag != destTransformFrag) {
                    ops.add(TransitionOperation.Transform(originTransformFrag, destTransformFrag))
                }

                ops.add(TransitionOperation.Split(destTransformFrag, destGroupRange))
            }
        }

        return TransitionGroup.create(ops)
    }

    companion object {
        fun noMove(): ComplexRangeTransitionManager<IntFragmentElement> {
            return ComplexRangeTransitionManager(FragmentProximityDetector.noMove())
        }

        fun withMoveDistance(maxMoveDist: Int): ComplexRangeTransitionManager<IntFragmentElement> {
            return ComplexRangeTransitionManager(FragmentProximityDetector.withMoveDistance(maxMoveDist))
        }
    }
}