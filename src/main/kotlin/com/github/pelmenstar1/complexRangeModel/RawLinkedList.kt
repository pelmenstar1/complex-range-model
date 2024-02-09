package com.github.pelmenstar1.complexRangeModel

import kotlin.NoSuchElementException
import java.util.LinkedList

/**
 * Represents a linked list that exposes its underlying [RawLinkedList.Node] class
 * (unlike standard [LinkedList] class) that allows some operations to be more efficient.
 */
class RawLinkedList<T> : MutableList<T> {
    /**
     * A node of the linked list.
     */
    class Node<T>(var value: T) {
        var previous: Node<T>? = null
        var next: Node<T>? = null
    }

    // This property should be changed manually whenever the list size actually changes
    private var _size = 0
    private var _head: Node<T>? = null
    private var _tail: Node<T>? = null

    /**
     * The head (first node) of the linked list. Might be null, if the list is empty.
     */
    val head: Node<T>?
        get() = _head

    /**
     * The tail (last node) of the linked list. Might null, if the list is empty.
     */
    val tail: Node<T>?
        get() = _tail

    /**
     * Gets the first value of the list. It throws [IllegalStateException] if the list is empty.
     */
    val firstValue: T
        get() = _head?.value ?: throw IllegalStateException("List is empty")

    /**
     * Gets the last value of the list. It throws [IllegalStateException] if the list is empty.
     */
    val lastValue: T
        get() = _tail?.value ?: throw IllegalStateException("List is empty")

    override val size: Int
        get() = _size

    constructor()

    internal constructor(head: Node<T>?, tail: Node<T>?, size: Int) {
        _head = head
        _tail = tail
        _size = size
    }

    override fun isEmpty(): Boolean {
        return _head == null
    }

    /**
     * Iterates through each element's node of the list starting with given [startNode].
     */
    inline fun forEachNodeStartingWith(startNode: Node<T>?, action: (node: Node<T>) -> Unit) {
        var current = startNode
        while (current != null) {
            action(current)
            current = current.next
        }
    }

    /**
     * Iterates through each element's node of the list.
     */
    inline fun forEachNode(action: (node: Node<T>) -> Unit) {
        forEachNodeStartingWith(head, action)
    }

    /**
     * Iterates through each element of the list.
     */
    inline fun forEachForward(action: (value: T) -> Unit) {
        forEachNode { action(it.value) }
    }

    /**
     * Iterates through each element of the list in reversed direction.
     */
    inline fun forEachNodeReversed(action: (value: Node<T>) -> Unit) {
        var current = tail
        while (current != null) {
            action(current)
            current = current.previous
        }
    }

    override fun clear() {
        _head = null
        _tail = null
        _size = 0
    }

    override fun get(index: Int): T {
        return getNode(index).value
    }

    override fun set(index: Int, element: T): T {
        val node = getNode(index)
        val previousValue = node.value
        node.value = element

        return previousValue
    }

    /**
     * Returns a node at given [index].
     *
     * @throws IndexOutOfBoundsException if given [index] is negative or greater than the list's size.
     */
    fun getNode(index: Int): Node<T> {
        return getNodeOrNull(index) ?: throw IndexOutOfBoundsException()
    }

    /**
     * Returns a node at given [index]. If given index is out of bounds, returns null.
     */
    fun getNodeOrNull(index: Int): Node<T>? {
        var count = 0
        var current = head

        while (current != null) {
            if (count == index) {
                return current
            }

            count++
            current = current.next
        }

        return null
    }

    /**
     * Returns a first element's node matching given [predicate], or `null` if the list does not contain such element
     */
    inline fun findFirstNode(predicate: (value: T) -> Boolean): Node<T>? {
        forEachNode { node ->
            if (predicate(node.value)) {
                return node
            }
        }

        return null
    }

    /**
     * Returns a last element's node matching given [predicate], or `null` if the list does not contain such element
     */
    inline fun findLastNode(predicate: (value: T) -> Boolean): Node<T>? {
        forEachNodeReversed { node ->
            if (predicate(node.value)) {
                return node
            }
        }

        return null
    }

    /**
     * Returns a first element's node that is equal to the given [value].
     */
    fun findFirstNode(value: T): Node<T>? {
        return findFirstNode { it == value }
    }

    override fun contains(element: T): Boolean {
        return findFirstNode(element) != null
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { contains(it) }
    }

    override fun indexOf(element: T): Int {
        var index = 0
        forEachForward {
            if (it == element) {
                return index
            }

            index++
        }

        return -1
    }

    override fun lastIndexOf(element: T): Int {
        var index = _size - 1

        forEachNodeReversed { node ->
            if (node.value == element) {
                return index
            }

            index--
        }

        return -1
    }

    override fun add(element: T): Boolean {
        val newNode = Node(element)
        val t = tail

        if (t == null) {
            _head = newNode
            _tail = newNode
        } else {
            newNode.previous = t
            t.next = newNode

            _tail = newNode
        }

        _size++

        return true
    }

    override fun add(index: Int, element: T) {
        val node = getNode(index)

        insertAfterNode(element, node)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        var lastNode = getNode(index)

        elements.forEach { value ->
            lastNode = insertAfterNode(value, lastNode)
        }

        return elements.isNotEmpty()
    }

    override fun addAll(elements: Collection<T>): Boolean {
        elements.forEach { add(it) }

        return elements.isNotEmpty()
    }

    /**
     * Inserts given [element] before specified [node]. The [node] must be in the list.
     */
    fun insertBeforeNode(element: T, node: Node<T>): Node<T> {
        val newNode = Node(element)

        val prevNode = node.previous

        newNode.next = node
        node.previous = newNode

        newNode.previous = prevNode
        prevNode?.next = newNode

        if (node === _head) {
            _head = newNode
        }

        _size++

        return newNode
    }

    /**
     * Inserts given [element] after specified [node]. The [node] must be in the list.
     */
    fun insertAfterNode(element: T, node: Node<T>): Node<T> {
        val nextNode = node.next
        val newNode = Node(element)

        newNode.next = nextNode
        nextNode?.previous = newNode

        newNode.previous = node
        node.next = newNode

        if (node === tail) {
            _tail = newNode
        }

        _size++

        return newNode
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var modified = false
        forEachNode {
            if (it.value !in elements) {
                modified = true
                removeNode(it)
            }
        }

        return modified
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var modified = false

        elements.forEach {
            modified = modified or remove(it)
        }

        return modified
    }

    override fun remove(element: T): Boolean {
        return findFirstNode(element)?.let { node ->
            removeNode(node)
            true
        } ?: false
    }

    /**
     * Removes given [node] from the list. This node must be in the list.
     */
    fun removeNode(node: Node<T>) {
        removeBetween(node, node)
    }

    /**
     * Removes elements starting from [startNode], ending with [endNode].
     *
     * Both nodes must be in the list. [startNode] must precede [endNode] or be equal to it.
     */
    fun removeBetween(startNode: Node<T>, endNode: Node<T>) {
        _size -= countBetweenNodes(startNode, endNode)

        val startNodePrev = startNode.previous
        val endNodeNext = endNode.next

        if (startNodePrev == null) {
            _head = endNode.next
        } else {
            startNodePrev.next = endNode.next
        }

        if (endNodeNext == null) {
            _tail = startNodePrev
        } else {
            endNodeNext.previous = startNodePrev
        }
    }

    /**
     * Replaces elements between [startNode] and [endNode] with specified [value].
     *
     * In other words, it removes elements between [startNode] and [endNode],
     * and inserts a new node with [value] after [startNode]'s previous node.
     * If [startNode] does not have a previous node, a new node is set to be head.
     */
    fun replaceBetweenWith(value: T, startNode: Node<T>, endNode: Node<T>) {
        _size -= countBetweenNodes(startNode, endNode) - 1

        val endNodeNext = endNode.next

        startNode.value = value
        startNode.next = endNodeNext

        if (endNodeNext == null) {
            _tail = startNode
        } else {
            endNodeNext.previous = startNode
        }
    }

    override fun removeAt(index: Int): T {
        val node = getNode(index)
        removeNode(node)

        return node.value
    }

    /**
     * Returns a shallow copy of the list: it copies only nodes, but references the same values.
     */
    fun copyOf(): RawLinkedList<T> {
        val h = head ?: return RawLinkedList()

        return copyOfBetween(h, _tail!!)
    }

    /**
     * Returns a shallow of the list between given two nodes: it copies only nodes, but references the same values.
     *
     * [startNode] and [endNode] must be in the list. [startNode] must precede [endNode] or be equal to it.
     */
    fun copyOfBetween(startNode: Node<T>, endNode: Node<T>): RawLinkedList<T> {
        if (startNode === endNode) {
            val n = Node(startNode.value)

            return RawLinkedList(n, n, size = 1)
        }

        var index = 1 // We already added the head.
        val newHead = Node(startNode.value)
        var newCurrent = newHead

        var current: Node<T>? = startNode.next

        while (current != null) {
            val newNode = Node(current.value).apply {
                previous = newCurrent
            }

            newCurrent.next = newNode
            newCurrent = newNode
            index++

            if (current === endNode) {
                return RawLinkedList(newHead, newCurrent, size = index)
            }

            current = current.next
        }

        throw IllegalStateException("Start and end nodes are not linked")
    }

    override fun equals(other: Any?): Boolean {
        if (other !is RawLinkedList<*>) {
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
        forEachForward { result = result * 31 + it.hashCode() }

        return result
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("RangeFragmentList(")
        var isFirst = true

        forEachForward {
            if (!isFirst) {
                sb.append(", ")
            }

            sb.append(it)
            isFirst = false
        }

        sb.append(')')

        return sb.toString()
    }

    override fun subList(fromIndex: Int, toIndex: Int): RawLinkedList<T> {
        val startNode = getNode(fromIndex)
        val endNode = getNode(toIndex - 1)
        val newSize = toIndex - fromIndex

        return RawLinkedList(startNode, endNode, newSize)
    }

    override fun iterator(): MutableIterator<T> = listIterator()

    override fun listIterator(): MutableListIterator<T> {
        return ListIteratorImpl(_head)
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        return ListIteratorImpl(getNode(index))
    }

    companion object {
        /**
         * Returns amount of elements between given two nodes.
         */
        private fun <T> countBetweenNodes(start: Node<T>, end: Node<T>): Int {
            var count = 0
            var current: Node<T>? = start

            while (current != null) {
                if (current == end) {
                    return count + 1
                }

                count++
                current = current.next
            }

            throw IllegalArgumentException("Start and end nodes are not connected")
        }
    }

    private inner class ListIteratorImpl(
        startNode: Node<T>?
    ) : MutableListIterator<T> {
        private var lastReturnedNode: Node<T>? = null
        private var nextNode: Node<T>? = startNode
        private var nextIndex = 0

        override fun hasNext(): Boolean {
            return nextNode != null
        }

        override fun hasPrevious(): Boolean {
            return nextIndex > 0
        }

        override fun next(): T {
            val nn = nextNode ?: throw NoSuchElementException()

            lastReturnedNode = nn
            nextNode = nn.next
            nextIndex++

            return nn.value
        }

        override fun previous(): T {
            val nn = nextNode
            val l = if (nn == null) _tail else nn.previous

            if (l == null) {
                throw NoSuchElementException()
            }

            nextNode = l
            lastReturnedNode = l
            nextIndex--

            return l.value
        }

        override fun nextIndex(): Int = nextIndex
        override fun previousIndex(): Int = nextIndex - 1

        override fun add(element: T) {
            lastReturnedNode = null

            val next = nextNode
            if (next == null) {
                this@RawLinkedList.add(element)
            } else {
                insertBeforeNode(element, next)
            }

            nextIndex++
        }

        override fun remove() {
            val lastReturned = lastReturnedNode

            checkNotNull(lastReturned)

            val lastNext = lastReturned.next
            removeNode(lastReturned)

            if (nextNode === lastReturned) {
                nextNode = lastNext
            } else {
                nextIndex--
            }

            lastReturnedNode = null
        }

        override fun set(element: T) {
            val lastReturned = lastReturnedNode
            checkNotNull(lastReturned)

            lastReturned.value = element
        }
    }
}