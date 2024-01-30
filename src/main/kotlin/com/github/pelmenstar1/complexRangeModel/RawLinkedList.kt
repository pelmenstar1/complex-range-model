package com.github.pelmenstar1.complexRangeModel

import java.util.*

class RawLinkedList<T> : MutableList<T> {
    class Node<T>(var value: T) {
        var previous: Node<T>? = null
        var next: Node<T>? = null
    }

    private var _size = 0
    private var _head: Node<T>? = null
    private var _tail: Node<T>? = null

    val head: Node<T>?
        get() = _head

    val tail: Node<T>?
        get() = _tail

    val firstValue: T
        get() = _head?.value ?: throw IllegalStateException("List is empty")

    val lastValue: T
        get() = _tail?.value ?: throw IllegalStateException("List is empty")

    override val size: Int
        get() = _size

    constructor()
    constructor(singleElement: T) {
        val node = Node(singleElement)

        _head = node
        _tail = node
        _size = 1
    }

    private constructor(head: Node<T>?, tail: Node<T>?, size: Int) {
        _head = head
        _tail = tail
        _size = size
    }

    override fun isEmpty(): Boolean {
        return _head == null
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

    inline fun forEachForward(action: (value: T) -> Unit) {
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

    fun getNode(index: Int): Node<T> {
        return getNodeOrNull(index) ?: throw IndexOutOfBoundsException()
    }

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

    inline fun findFirstNode(predicate: (value: T) -> Boolean): Node<T>? {
        forEachNode { node ->
            if (predicate(node.value)) {
                return node
            }
        }

        return null
    }

    inline fun findLastNode(predicate: (value: T) -> Boolean): Node<T>? {
        forEachNodeReversed { node ->
            if (predicate(node.value)) {
                return node
            }
        }

        return null
    }

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

    fun removeNode(node: Node<T>) {
        removeBetween(node, node)
    }

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

    fun copyOf(): RawLinkedList<T> {
        val currentHead = head ?: return RawLinkedList()

        val newHead = Node(currentHead.value)
        var newCurrent = newHead

        forEachNodeStartingWith(currentHead.next) { current ->
            val newNode = Node(current.value).apply {
                previous = newCurrent
            }

            newCurrent.next = newNode
            newCurrent = newNode
        }

        return RawLinkedList(newHead, newCurrent, _size)
    }

    fun toArray(output: Array<in T>) {
        var index = 0
        forEachForward { output[index++] = it }
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
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

@Suppress("UNCHECKED_CAST")
inline fun <reified T> RawLinkedList<T>.toArray(): Array<T> {
    val arr = arrayOfNulls<T>(size)
    toArray(arr)

    return arr as Array<T>
}