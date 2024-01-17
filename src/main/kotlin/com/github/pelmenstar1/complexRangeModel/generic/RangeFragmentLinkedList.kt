package com.github.pelmenstar1.complexRangeModel.generic

import com.github.pelmenstar1.complexRangeModel.RangeFragment

class RangeFragmentLinkedList<T : Comparable<T>> : MutableCollection<RangeFragment<T>> {
    class Node<T : Comparable<T>>(var value: RangeFragment<T>) {
        var previous: Node<T>? = null
        var next: Node<T>? = null
    }

    var head: Node<T>? = null
    var tail: Node<T>? = null

    override val size: Int
        get() {
            var result = 0
            forEach { _ -> result++ }

            return result
        }

    constructor()
    constructor(head: Node<T>, tail: Node<T>) {
        this.head = head
        this.tail = tail
    }

    override fun isEmpty(): Boolean {
        return head == null
    }

    inline fun forEachNodeStartingWith(startNode: Node<T>?, action: (node: Node<T>) -> Unit) {
        var current = startNode
        while (current != null) {
            action(current)
            current = current.next
        }
    }

    inline fun forEachNode(action: (node: Node<T>) -> Unit) {
        forEachNodeStartingWith(head, action)
    }

    inline fun forEach(action: (value: RangeFragment<T>) -> Unit) {
        forEachNode { action(it.value) }
    }

    inline fun forEachNodeReversed(action: (value: Node<T>) -> Unit) {
        var current = tail
        while (current != null) {
            action(current)
            current = current.previous
        }
    }

    override fun clear() {
        head = null
    }

    operator fun get(index: Int): RangeFragment<T> {
        return getNode(index).value
    }

    fun getNode(index: Int): Node<T> {
        var count = 0
        var current = head

        while (current != null) {
            if (count == index) {
                return current
            }

            count++
            current = current.next
        }

        throw IndexOutOfBoundsException()
    }

    override fun contains(element: RangeFragment<T>): Boolean {
        return findFirstNode(element) != null
    }

    inline fun findFirstNode(predicate: (value: RangeFragment<T>) -> Boolean): Node<T>? {
        forEachNode { node ->
            if (predicate(node.value)) {
                return node
            }
        }

        return null
    }

    inline fun findLastNode(predicate: (value: RangeFragment<T>) -> Boolean): Node<T>? {
        forEachNodeReversed { node ->
            if (predicate(node.value)) {
                return node
            }
        }

        return null
    }

    fun findFirstNode(value: RangeFragment<T>): Node<T>? {
        return findFirstNode { it == value }
    }

    override fun containsAll(elements: Collection<RangeFragment<T>>): Boolean {
        return elements.all { contains(it) }
    }

    override fun addAll(elements: Collection<RangeFragment<T>>): Boolean {
        elements.forEach { add(it) }

        return elements.isNotEmpty()
    }

    override fun add(element: RangeFragment<T>): Boolean {
        val newNode = Node(element)
        val t = tail

        if (t == null) {
            head = newNode
            tail = newNode
        } else {
            newNode.previous = t
            t.next = newNode

            tail = newNode
        }

        return true
    }

    fun insertBeforeHead(element: RangeFragment<T>) {
        val node = Node(element)
        val h = head

        if (h == null) {
            tail = node
        } else {
            node.next = h
            h.previous = node
        }

        head = node
    }

    fun insertAfterNode(element: RangeFragment<T>, node: Node<T>) {
        val nextNode = node.next
        val newNode = Node(element)

        newNode.next = nextNode
        nextNode?.previous = newNode

        newNode.previous = node
        node.next = newNode

        if (node === tail) {
            tail = newNode
        }
    }

    override fun retainAll(elements: Collection<RangeFragment<T>>): Boolean {
        var modified = false
        forEachNode {
            if (it.value !in elements) {
                modified = true
                removeNode(it)
            }
        }

        return modified
    }

    override fun removeAll(elements: Collection<RangeFragment<T>>): Boolean {
        var modified = false

        elements.forEach {
            modified = modified or remove(it)
        }

        return modified
    }

    override fun remove(element: RangeFragment<T>): Boolean {
        return findFirstNode(element)?.let { node ->
            removeNode(node)
            true
        } ?: false
    }

    fun removeNode(node: Node<T>) {
        removeBetween(node, node)
    }

    fun removeBetween(startNode: Node<T>, endNode: Node<T>) {
        val startNodePrev = startNode.previous
        val endNodeNext = endNode.next

        if (startNodePrev == null) {
            head = endNode.next
        } else {
            startNodePrev.next = endNode.next
        }

        if (endNodeNext == null) {
            tail = startNodePrev
        } else {
            endNodeNext.previous = startNodePrev
        }
    }

    fun replaceBetweenWith(value: RangeFragment<T>, startNode: Node<T>, endNode: Node<T>) {
        val endNodeNext = endNode.next

        startNode.value = value
        startNode.next = endNodeNext

        if (endNodeNext == null) {
            tail = startNode
        } else {
            endNodeNext.previous = startNode
        }
    }

    fun copyOf(): RangeFragmentLinkedList<T> {
        val currentHead = head ?: return RangeFragmentLinkedList()

        val newHead = Node(currentHead.value)
        var newCurrent = newHead

        forEachNodeStartingWith(currentHead.next) { current ->
            val newNode = Node(current.value).apply {
                previous = newCurrent
            }

            newCurrent.next = newNode
            newCurrent = newNode
        }

        return RangeFragmentLinkedList(newHead, newCurrent)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is RangeFragmentLinkedList<*>) {
            return false
        }

        var current = head
        var otherCurrent = other.head

        while (true) {
            if (current == null && otherCurrent == null) {
                return true
            } else if (current == null || otherCurrent == null) {
                return false
            }

            if (current.value != otherCurrent.value) {
                return false
            }

            current = current.next
            otherCurrent = otherCurrent.next
        }
    }

    override fun hashCode(): Int {
        var result = 1
        forEach { result = result * 31 + it.hashCode() }

        return result
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("RangeFragmentList(")
        var isFirst = true

        forEach {
            if (!isFirst) {
                sb.append(", ")
            }

            sb.append(it)
            isFirst = false
        }

        sb.append(')')

        return sb.toString()
    }

    override fun iterator(): MutableIterator<RangeFragment<T>> {
        return IteratorImpl(this)
    }

    private class IteratorImpl<T : Comparable<T>>(
        private val list: RangeFragmentLinkedList<T>
    ) : MutableIterator<RangeFragment<T>> {
        private var previousNode: Node<T>? = null
        private var currentNode: Node<T>? = list.head

        override fun hasNext(): Boolean {
            return currentNode != null
        }

        override fun next(): RangeFragment<T> {
            val curNode = currentNode ?: throw IllegalStateException("Iterator is already empty")

            previousNode = curNode
            currentNode = curNode.next

            return curNode.value
        }

        override fun remove() {
            previousNode?.also { list.removeNode(it) }
        }
    }
}