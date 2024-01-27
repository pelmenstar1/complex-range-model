package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.*

private typealias FragmentIterator<T> = TwoWayIterator<RangeFragment<T>>
private typealias FragmentLinkedList<T> = RawLinkedList<RangeFragment<T>>

class ComplexRangeTransitionManager<T>(
    private val fragmentFactory: RangeFragmentFactory<T>,
    private val proxDetector: FragmentProximityDetector<T>
) {
    fun createTransition(origin: ComplexRange<T>, dest: ComplexRange<T>): ComplexRangeTransition<T> {
        val groups = ArraySet<TransitionGroup<T>>()
        val originIter = origin.twoWayIterator()
        val destIter = dest.twoWayIterator()

        while(true) {
            val originHasNext = originIter.hasNext()
            val destHasNext = destIter.hasNext()

            if (!originHasNext || !destHasNext) {
                if (originHasNext) {
                    // Then destHasNext is false
                    addRemoveAllTransition(originIter, groups)
                } else if (destHasNext) {
                    addInsertAllTransition(destIter, groups)
                }

                break
            }

            val originFrag = originIter.next()
            val destFrag = destIter.next()

            if (originFrag != destFrag) {
                if (originFrag.overlapsWith(destFrag)) {
                    val originGroupFrags = RawLinkedList<RangeFragment<T>>()
                    val destGroupsFrags = RawLinkedList<RangeFragment<T>>()

                    originGroupFrags.add(originFrag)
                    destGroupsFrags.add(destFrag)

                    consumeElementsForTransformGroup(
                        originIter, destIter,
                        originGroupFrags, destGroupsFrags
                    )

                    groups.add(createTransformGroup(originGroupFrags, destGroupsFrags))
                } else {
                    if (proxDetector.canMove(originFrag, destFrag)) {
                        groups.add(TransitionGroup.create(TransitionOperation.Transform(originFrag, destFrag)))
                    } else {
                        groups.add(TransitionGroup.create(TransitionOperation.Remove(originFrag)))
                        groups.add(TransitionGroup.create(TransitionOperation.Insert(destFrag)))
                    }
                }
            }
        }

        return ComplexRangeTransition.create(groups)
    }

    private fun addInsertAllTransition(iter: FragmentIterator<T>, groups: ArraySet<TransitionGroup<T>>) {
        addOperationAllTransition(iter, groups) { TransitionOperation.Insert(it) }
    }

    private fun addRemoveAllTransition(iter: FragmentIterator<T>, groups: ArraySet<TransitionGroup<T>>) {
        addOperationAllTransition(iter, groups) { TransitionOperation.Remove(it) }
    }

    private inline fun addOperationAllTransition(
        iter: FragmentIterator<T>,
        groups: ArraySet<TransitionGroup<T>>,
        createOp: (RangeFragment<T>) -> TransitionOperation<T>
    ) {
        while (iter.hasNext()) {
            val fragment = iter.next()
            val op = createOp(fragment)

            groups.add(TransitionGroup.create(op))
        }
    }

    // Internal for tests
    internal fun consumeElementsForTransformGroup(
        originIter: FragmentIterator<T>,
        destIter: FragmentIterator<T>,
        originGroupFrags: FragmentLinkedList<T>,
        destGroupFrags: FragmentLinkedList<T>
    ) {
        while (true) {
            var lastOriginFrag = originGroupFrags.lastValue

            val nonOverlappingDestFrag = consumeLaneForTransform(lastOriginFrag, destIter, destGroupFrags)
            val lastDestFrag = destGroupFrags.lastValue

            if (nonOverlappingDestFrag == null) {
                consumeLaneForTransform(lastDestFrag, originIter, originGroupFrags)

                break
            }

            if (lastOriginFrag == nonOverlappingDestFrag) {
                break
            }

            if (originIter.hasNext()) {
                val nextOriginFrag = originIter.next()

                if (nextOriginFrag.overlapsWith(lastDestFrag)) {
                    originGroupFrags.add(nextOriginFrag)
                } else {
                    originIter.previous()
                    break
                }
            }

            val nonOverlappingOriginFrag = consumeLaneForTransform(lastDestFrag, originIter, originGroupFrags)
            lastOriginFrag = originGroupFrags.lastValue

            if (nonOverlappingOriginFrag == null) {
                consumeLaneForTransform(lastOriginFrag, destIter, destGroupFrags)

                break
            }

            if (lastDestFrag == nonOverlappingOriginFrag) {
                break
            }

            if (destIter.hasNext()) {
                val nextDestFrag = destIter.next()

                if (nextDestFrag.overlapsWith(lastOriginFrag)) {
                    destGroupFrags.add(nextDestFrag)
                } else {
                    destIter.previous()
                    break
                }
            }
        }
    }

    private fun consumeLaneForTransform(
        anchorFrag: RangeFragment<T>,
        iter: FragmentIterator<T>,
        output: FragmentLinkedList<T>
    ): RangeFragment<T>? {
        while (iter.hasNext()) {
            val frag = iter.next()

            if (frag == anchorFrag || !frag.overlapsWith(anchorFrag)) {
                iter.previous()
                return frag
            }

            output.add(frag)
        }

        return null
    }

    private fun createTransformGroup(
        originFrags: FragmentLinkedList<T>,
        destFrags: FragmentLinkedList<T>
    ): TransitionGroup<T> {
        val ops = ArrayList<TransitionOperation<T>>(3)

        val originSize = originFrags.size
        val destSize = destFrags.size

        if (originSize == 1) {
            val originFrag = originFrags.firstValue

            if (destSize == 1) {
                // Ops:
                // - Transform

                val destFrag = destFrags.firstValue
                ops.add(TransitionOperation.Transform(originFrag, destFrag))
            } else {
                // Ops:
                // - Transform
                // - Split

                val destFragsArray = destFrags.toArray()

                val minFrag = destFragsArray[0]
                val maxFrag = destFragsArray[destSize - 1]
                val destTransformFrag = fragmentFactory.create(minFrag.start, maxFrag.endInclusive)

                if (originFrag != destTransformFrag) {
                    ops.add(TransitionOperation.Transform(originFrag, destTransformFrag))
                }

                ops.add(TransitionOperation.Split(destTransformFrag, destFragsArray))
            }
        } else {
            // originSize > 1
            val originFragsArray = originFrags.toTypedArray()

            val minOriginFrag = originFragsArray[0]
            val maxOriginFrag = originFragsArray[originSize - 1]
            val originTransformFrag = fragmentFactory.create(minOriginFrag.start, maxOriginFrag.endInclusive)

            if (destSize == 1) {
                val destFrag = destFrags.firstValue

                // Ops:
                // - Join
                // - Transform? (if joined fragment is not destination fragment)
                ops.add(TransitionOperation.Join(originFragsArray, originTransformFrag))

                if (destFrag != originTransformFrag) {
                    ops.add(TransitionOperation.Transform(originTransformFrag, destFrag))
                }
            } else {
                // Ops:
                // - Join
                // - Transform
                // - Split

                val destFragsArray = destFrags.toTypedArray()

                val minDestFrag = destFragsArray[0]
                val maxDestFrag = destFragsArray[destSize - 1]

                val destTransformFrag = fragmentFactory.create(minDestFrag.start, maxDestFrag.endInclusive)

                ops.add(TransitionOperation.Join(originFragsArray, originTransformFrag))

                if (originTransformFrag != destTransformFrag) {
                    ops.add(TransitionOperation.Transform(originTransformFrag, destTransformFrag))
                }

                ops.add(TransitionOperation.Split(destTransformFrag, destFragsArray))
            }
        }

        return TransitionGroup.create(ops)
    }

    companion object {
        fun intNoMove(): ComplexRangeTransitionManager<Int> {
            return ComplexRangeTransitionManager(IntRangeFragmentFactory, FragmentProximityDetector.noMove())
        }

        fun intWithMoveDistance(maxMoveDist: Int): ComplexRangeTransitionManager<Int> {
            return ComplexRangeTransitionManager(
                IntRangeFragmentFactory,
                FragmentProximityDetector.withMoveDistance(IntFragmentElementSupport, maxMoveDist)
            )
        }
    }
}