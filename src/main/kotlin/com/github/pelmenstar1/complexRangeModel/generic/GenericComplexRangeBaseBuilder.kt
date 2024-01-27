package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.FragmentElement
import com.github.pelmenstar1.complexRangeModel.RangeFragment
import com.github.pelmenstar1.complexRangeModel.RawLinkedList

abstract class GenericComplexRangeBaseBuilder<T : FragmentElement<T>> {
    protected val fragments: RawLinkedList<RangeFragment<T>>

    protected constructor() {
        fragments = RawLinkedList()
    }

    protected constructor(fragments: RawLinkedList<RangeFragment<T>>) {
        this.fragments = fragments
    }

    protected fun includeFragment(fragment: RangeFragment<T>) {
        if (fragments.isEmpty()) {
            fragments.add(fragment)
            return
        }

        fragments.forEachNode { node ->
            val thisFragment = node.value

            if (thisFragment == fragment) {
                // No need for creating new array if we're adding the same fragment to the array
                return
            }

            val unitedFragment = thisFragment.uniteWith(fragment)
            if (unitedFragment != null) {
                includeFragmentWithUniting(unitedFragment, node)
                return
            }
        }

        // Given fragment cannot be united with any of current fragments.
        // Then find its index (for the ranges to be sorted) and insert it
        includeFragmentWithoutUniting(fragment)
    }

    private fun includeFragmentWithUniting(
        fragmentUnitedWithStart: RangeFragment<T>,
        startNode: RawLinkedList.Node<RangeFragment<T>>
    ) {
        var totalUnitedFragment = fragmentUnitedWithStart

        // Last node cannot be null because the list is not empty.
        var endNode = fragments.tail

        fragments.forEachNodeStartingWith(startNode.next) { node ->
            val currentFragment = node.value

            val unitedFragment = totalUnitedFragment.uniteWith(currentFragment)
            if (unitedFragment != null) {
                totalUnitedFragment = unitedFragment
            } else {
                endNode = node.previous

                // Bail out from the loop
                return@forEachNodeStartingWith
            }
        }

        fragments.replaceBetweenWith(totalUnitedFragment, startNode, endNode!!)
    }

    private fun includeFragmentWithoutUniting(fragment: RangeFragment<T>) {
        val insertNode = findNewFragmentInsertAfterNode(fragment)

        if (insertNode == null) {
            fragments.insertBeforeHead(fragment)
        } else {
            fragments.insertAfterNode(fragment, insertNode)
        }
    }

    // Tries to find the node after which the fragment should be inserted. It returns null, when
    // the fragment should be inserted before the head.
    private fun findNewFragmentInsertAfterNode(fragment: RangeFragment<T>): RawLinkedList.Node<RangeFragment<T>>? {
        // It assumes that given fragment doesn't overlap with any of current fragments
        fragments.forEachNode { node ->
            if (fragment.isBefore(node.value)) {
                return node.previous
            }
        }

        return fragments.tail
    }

    protected fun excludeFragment(fragment: RangeFragment<T>) {
        val affectedStartNode = findFirstOverlapFragmentNode(fragment)
        val affectedEndNode = findLastOverlapFragmentNode(fragment)

        if (affectedStartNode == null || affectedEndNode == null) {
            return
        }

        if (affectedStartNode === affectedEndNode) {
            excludeFragmentWithOneAffected(fragment, affectedStartNode)
        } else {
            excludeFragmentWithRangeAffected(fragment, affectedStartNode, affectedEndNode)
        }
    }

    private fun excludeFragmentWithOneAffected(
        excludeFragment: RangeFragment<T>,
        affectedNode: RawLinkedList.Node<RangeFragment<T>>
    ) {
        val affectedFragment = affectedNode.value

        if (excludeFragment.containsCompletely(affectedFragment)) {
            // Simply remove the whole fragment
            fragments.removeNode(affectedNode)
        } else if (affectedFragment.containsExclusive(excludeFragment)) {
            splitFragmentWithExcludingOtherFragment(affectedNode, excludeFragment)
        } else if (affectedFragment.leftContains(excludeFragment)) {
            // Something like:
            // affectedFragment:    [] [] | [] []
            // excludeFragment:  [] [] [] |
            // result:                    | [] []
            // So we need to remove the part where these fragments intersect
            affectedNode.value = affectedFragment.withStart(excludeFragment.endExclusive)
        } else {
            // affectedFragment is not fragmentToRemove, affectedFragment doesn't contain the fragmentToRemove (exclusively)
            // and the affectedFragment doesn't left-contains the fragmentToRemove,
            // then the affectedFragment right-contains the fragmentToRemove
            //
            // Something like:
            // affectedFragment: [] [] | [] []
            // excludeFragment:        | [] [] []
            // result:           [] [] |
            affectedNode.value = affectedFragment.withEndExclusive(excludeFragment.start)
        }
    }

    private fun excludeFragmentWithRangeAffected(
        excludeFragment: RangeFragment<T>,
        affectedStartNode: RawLinkedList.Node<RangeFragment<T>>,
        affectedEndNode: RawLinkedList.Node<RangeFragment<T>>
    ) {
        val affectedStartFrag = affectedStartNode.value
        val affectedEndFrag = affectedEndNode.value

        val removalStartNode: RawLinkedList.Node<RangeFragment<T>>?
        val removalEndNode: RawLinkedList.Node<RangeFragment<T>>?

        if (excludeFragment.isBefore(affectedStartFrag)) {
            // Something like:
            // affectedStartFrag:    [] [] []
            // excludeFragment:   [] [] [] [] ...
            // So we need to remove the whole affectedStartFrag
            removalStartNode = affectedStartNode
        } else {
            // Something like:
            // affectedStartFrag: [] [] | [] [] []
            // excludeFragment:         | [] [] [] ...
            // result:            [] []
            // So we need to narrow the affectedStartFrag and remove starting from the next fragment
            removalStartNode = affectedStartNode.next
            affectedStartNode.value = affectedStartFrag.withEndExclusive(excludeFragment.start)
        }

        if (excludeFragment.isAfter(affectedEndFrag)) {
            // Something like:
            // affectedEndFrag:     [] [] []
            // excludeFragment: ... [] [] [] []
            // So we need to remove whole affectedEndFrag
            removalEndNode = affectedEndNode
        } else {
            // Something like:
            // affectedEndFrag:     [] [] | [] []
            // excludeFragment: ... [] [] |
            // result:                      [] []
            // So we need no narrow affectedEndFrag and remove ending with the previous fragment
            removalEndNode = affectedEndNode.previous
            affectedEndNode.value = affectedEndFrag.withStart(excludeFragment.endExclusive)
        }

        if (removalStartNode != null && removalEndNode != null) {
            if (removalStartNode.value.isBefore(removalEndNode.value)) {
                fragments.removeBetween(removalStartNode, removalEndNode)
            }
        }
    }

    private fun splitFragmentWithExcludingOtherFragment(
        splitNode: RawLinkedList.Node<RangeFragment<T>>,
        excludeFragment: RangeFragment<T>
    ) {
        // Range in splitNode: [] [] [] [] [] []
        // excludeFragment:           [] []
        // result:              [] []       [] []
        val splitFragment = splitNode.value

        // Change range at splitIndex to the left part of the split.
        splitNode.value = splitFragment.withEndExclusive(excludeFragment.start)

        // Insert the right part of the split after splitIndex
        fragments.insertAfterNode(splitFragment.withStart(excludeFragment.endExclusive), splitNode)
    }

    private fun findFirstOverlapFragmentNode(fragment: RangeFragment<T>): RawLinkedList.Node<RangeFragment<T>>? {
        return fragments.findFirstNode { fragment.overlapsWith(it) }
    }

    private fun findLastOverlapFragmentNode(fragment: RangeFragment<T>): RawLinkedList.Node<RangeFragment<T>>? {
        return fragments.findLastNode { fragment.overlapsWith(it) }
    }
}