package com.github.pelmenstar1.complexRangeModel.transitions

import com.github.pelmenstar1.complexRangeModel.IntRangeFragment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransitionGroupTests {
    @Test
    fun emptyTest() {
        val group = TransitionGroup.empty<Int>()

        assertTrue(group.isEmpty())
        assertEquals(0, group.size)

        val groupToString = group.toString()
        assertEquals("TransitionGroup()", groupToString)
    }

    @Test
    fun emptyEqualsTest() {
        val emptyGroup = TransitionGroup.empty<Int>()
        val emptyListGroup = TransitionGroup.create(emptyList<TransitionOperation<Int>>())

        assertEquals(emptyGroup, emptyGroup)
        assertEquals(emptyGroup, emptyListGroup)
        assertEquals(emptyListGroup, emptyGroup)
    }

    @Test
    fun singleValueTest() {
        val frag = IntRangeFragment(1..2)
        val otherFrag = IntRangeFragment(2..3)

        val group = TransitionGroup.create(TransitionOperation.Insert(frag))

        assertFalse(group.isEmpty())
        assertEquals(1, group.size)
        assertTrue(group.contains(TransitionOperation.Insert(frag)))
        assertFalse(group.contains(TransitionOperation.Insert(otherFrag)))

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
        assertFalse(group.isEmpty())
        assertEquals(2, group.size)
        assertTrue(group.contains(op1))
        assertTrue(group.contains(op2))
        assertFalse(group.contains(op3))

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
}