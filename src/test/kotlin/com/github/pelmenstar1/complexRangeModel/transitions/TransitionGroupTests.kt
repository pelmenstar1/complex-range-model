package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.IntFragmentElement
import com.github.pelmenstar1.complexRangeModel.IntRangeFragment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransitionGroupTests {
    @Test
    fun emptyTest() {
        val group = TransitionGroup.empty<IntFragmentElement>()

        assertTrue(group.operations().isEmpty())
        assertEquals(0, group.operations().size)

        val groupToString = group.toString()
        assertEquals("TransitionGroup()", groupToString)
    }

    @Test
    fun emptyEqualsTest() {
        val emptyGroup = TransitionGroup.empty<IntFragmentElement>()
        val emptyListGroup = TransitionGroup.create(emptyList<TransitionOperation<IntFragmentElement>>())

        assertEquals(emptyGroup, emptyGroup)
        assertEquals(emptyGroup, emptyListGroup)
        assertEquals(emptyListGroup, emptyGroup)
    }

    @Test
    fun singleValueTest() {
        val frag = IntRangeFragment(1..2)
        val otherFrag = IntRangeFragment(2..3)

        val group = TransitionGroup.create(TransitionOperation.Insert(frag))

        assertFalse(group.operations().isEmpty())
        assertEquals(1, group.operations().size)
        assertTrue(group.operations().contains(TransitionOperation.Insert(frag)))
        assertFalse(group.operations().contains(TransitionOperation.Insert(otherFrag)))

        val groupToString = group.toString()
        assertEquals("TransitionGroup(TransitionOperation.Insert(fragment=[1, 2]))", groupToString)
    }

    @Test
    fun singleValueEqualsTest() {
        val op = TransitionOperation.Insert(IntRangeFragment(1..2))
        val group = TransitionGroup.create(op)
        val listGroup = TransitionGroup.create(listOf(op))

        assertEquals(group, group)
        assertEquals(group, listGroup)
        assertEquals(listGroup, group)
    }

    @Test
    fun listTest() {
        val op1 = TransitionOperation.Insert(IntRangeFragment(1..2))
        val op2 = TransitionOperation.Insert(IntRangeFragment(2..3))
        val op3 = TransitionOperation.Insert(IntRangeFragment(3..4))
        val ops = listOf(op1, op2)

        val group = TransitionGroup.create(ops)
        assertFalse(group.operations().isEmpty())
        assertEquals(2, group.operations().size)
        assertTrue(group.operations().contains(op1))
        assertTrue(group.operations().contains(op2))
        assertFalse(group.operations().contains(op3))

        val groupToString = group.toString()
        assertEquals("TransitionGroup(TransitionOperation.Insert(fragment=[1, 2]), TransitionOperation.Insert(fragment=[2, 3]))", groupToString)
    }

    @Test
    fun listEqualsTest() {
        val op1 = TransitionOperation.Insert(IntRangeFragment(1..2))
        val op2 = TransitionOperation.Insert(IntRangeFragment(2..3))
        val ops = listOf(op1, op2)

        val group1 = TransitionGroup.create(ops)
        val group2 = TransitionGroup.create(ops)

        assertEquals(group1, group2)
    }

    @Test
    fun hashCode_emptyToEmptyCollectionTest() {
        val empty = TransitionGroup.empty<IntFragmentElement>()
        val emptyCollection = TransitionGroup.create(emptyList<TransitionOperation<IntFragmentElement>>())

        val emptyHash = empty.hashCode()
        val emptyColHash = emptyCollection.hashCode()

        assertEquals(emptyHash, emptyColHash)
    }

    @Test
    fun hashCode_singleToSingleCollectionTest() {
        val op = TransitionOperation.Insert(IntRangeFragment(0, 1))
        val single = TransitionGroup.create(op)
        val singleCol = TransitionGroup.create(listOf(op))

        val singleHash = single.hashCode()
        val singleColHash = singleCol.hashCode()

        assertEquals(singleHash, singleColHash)
    }

    @Test
    fun reversedTest() {
        fun testCase(
            initialOps: TransitionGroupBuilder<IntFragmentElement>.() -> Unit,
            reversedOps: TransitionGroupBuilder<IntFragmentElement>.() -> Unit
        ) {
            val initialGroup = TransitionGroup(initialOps)

            val expectedReversedGroup = TransitionGroup(reversedOps)
            val actualReversedGroup = initialGroup.reversed()

            assertEquals(expectedReversedGroup, actualReversedGroup)
        }

        testCase(
            initialOps = {
                insert(1..2)
                remove(3..4)
            },
            reversedOps = {
                insert(3..4)
                remove(1..2)
            }
        )

        testCase(
            initialOps = {
                insert(1..2)
            },
            reversedOps = {
                remove(1..2)
            }
        )
    }
}