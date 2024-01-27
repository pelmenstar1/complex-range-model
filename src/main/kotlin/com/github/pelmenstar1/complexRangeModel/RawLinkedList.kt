package com.github.pelmenstar1.complexRangeModel

class RawLinkedList<T> : MutableCollection<T> {
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
    private constructor(head: Node<T>, tail: Node<T>, size: Int) {
        _head = head
        _tail = tail
        _size = size
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

    inline fun forEach(action: (value: T) -> Unit) {
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

    operator fun get(index: Int): T {
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

    override fun contains(element: T): Boolean {
        return findFirstNode(element) != null
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

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { contains(it) }
    }

    override fun addAll(elements: Collection<T>): Boolean {
        elements.forEach { add(it) }

        return elements.isNotEmpty()
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

    fun insertBeforeHead(element: T) {
        val node = Node(element)
        val h = head

        if (h == null) {
            _tail = node
        } else {
            node.next = h
            h.previous = node
        }

        _head = node
        _size++
    }

    fun insertAfterNode(element: T, node: Node<T>) {
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
        forEach { output[index++] = it }
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

    override fun iterator(): MutableIterator<T> {
        return IteratorImpl(this)
    }

    fun twoWayIterator(): TwoWayIterator<T> {
        val h = head

        return if (h == null) TwoWayIterator.empty() else TwoWayIteratorImpl(h, tail!!, _size)
    }

    companion object {
        private fun<T> countBetweenNodes(start: Node<T>, end: Node<T>): Int {
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

    private class IteratorImpl<T>(
        private val list: RawLinkedList<T>
    ) : MutableIterator<T> {
        private var previousNode: Node<T>? = null
        private var currentNode: Node<T>? = list.head

        override fun hasNext(): Boolean {
            return currentNode != null
        }

        override fun next(): T {
            val curNode = currentNode ?: throw IllegalStateException("Iterator is already empty")

            previousNode = curNode
            currentNode = curNode.next

            return curNode.value
        }

        override fun remove() {
            previousNode?.also { list.removeNode(it) }
        }
    }

    private class TwoWayIteratorImpl<T>(
        startNode: Node<T>,
        private val endNode: Node<T>,
        override val size: Int
    ) : TwoWayIterator<T> {
        private var lastReturnedNode: Node<T>? = null
        private var nextNode: Node<T>? = startNode
        private var nextIndex = 0

        private var markedNode: Node<T>? = null
        private var markedNodeIndex = -1

        override fun hasNext(): Boolean {
            return nextNode !== endNode.next
        }

        override fun hasPrevious(): Boolean {
            return nextIndex > 0
        }

        override fun next(): T {
            val nn = nextNode
            if (nn == null || nn === endNode.next) {
                throw NoSuchElementException()
            }

            lastReturnedNode = nn
            nextNode = nn.next
            nextIndex++

            return nn.value
        }

        override fun previous(): T {
            val nn = nextNode
            val l = if (nn == null) endNode else nn.previous

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

        override fun mark() {
            markedNode = nextNode
            markedNodeIndex = nextIndex
        }

        override fun fillArray(array: Array<in T>) {
            var currentNode = nextNode

            for (i in array.indices) {
                if (currentNode == null) {
                    throw NoSuchElementException()
                }

                array[i] = currentNode.value
                currentNode = currentNode.next
            }

            lastReturnedNode = currentNode
            nextNode = currentNode
        }

        override fun subIterator(): TwoWayIterator<T> {
            val startNode = markedNode ?: throw IllegalStateException("No element is marked")
            val endNode = lastReturnedNode ?: endNode
            val subSize = nextIndex - markedNodeIndex

            return TwoWayIteratorImpl(startNode, endNode, subSize)
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun<reified T> RawLinkedList<T>.toArray(): Array<T> {
    val arr = arrayOfNulls<T>(size)
    toArray(arr)

    return arr as Array<T>
}