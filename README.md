# Proof-of-concept of modeling complex ranges and creating transitions between them

Implements the concept of complex ranges in more or less performant way. The project tests what API surface better fits the concept.

## Basic

In this context, the complex range relates not to complex numbers but to its **complex** structure.

- **Complex range** consists of mutually non-intersecting simple ranges (called _fragments_). This sequence of fragments should be sorted in ascending order. The ordering makes it easier to create transitions between fragments.
- **Fragment** is a closed range between two _elements_ (fragment elements). Formally it can be represented as a set:
  > Let $\mathbb{X}$ be a set of the fragment elements; $a, b \in X$, then:
  >
  > $$F(a, b) = \\{ x \in \mathbb{X} \mid a <= x <= b \\}\text{, where } a <= b$$

- **Fragment element** is any object that satisfies the following rules:
    - Two elements can be compared, i.e. there is a $<=$ relation.
    - Has a notion of the _next element_ (notated as $next(x)$). If element $next(x)$ exists, then it must satisfy the following relation: $x < next(x)$

      The next element might not always exist. For example, if $\mathbb{X}$ is a set of unsigned 32-bit integers and $next(x)$ is defined as $next(x) = x + 1$, then $next(2^{32} - 1) = 2^{32} - 1 + 1 = (overflows) = 0 < 2^{32} - 1$ breaks the definition of the next element, so formally $next(2^{32} - 1)$ does not exist.

  From the definition follows:
    - A notion of the previous element: the $prev(x)$ function returns such $y \in \mathbb{X}$ that $next(y) = x$.

      Like the next element, the previous element also might not exist. For example, if $\mathbb{X}$ is a set of unsigned 32-bit integers and $prev(x)$ is defined as $prev(x) = x - 1$, then $prev(0)$ does not exist due to an underflow.

A distance between two elements can be expressed in not a single way. For instance, if $\mathbb{X}\$ is a set of unsigned 32-bit integers and $next(x) = x + 1$, then distance can be naturally defined as an absolute difference between elements or _amount of elements between elements_. Such distance is called **raw**. But if $\mathbb{X}\$ is a set of dates and $next(x) = tommorow$, then raw distance (i.e. amount of days between two dates) is not always useful. A more robust definition of the distance would be a timespan. Summarizing: all elements have a notion of _raw_ distances, elements that have another definition of distance are called **distance elements**.

## Transitions

- The transition between two complex ranges is a **set** of transition groups. Set means that the operation of the groups can be executed in any order. In other words, the result of a transition group doesn't affect the results of another transition groups. It enables the implementation to execute them in parallel.
- A transition group is a **sequence** of transition operations. Sequence means that the operations must be executed sequentially.
- A transition operation is a change simple enough to be visually animated. Such operation are defined:
    - `Insert` - inserts specified fragment to the complex range
    - `Remove` - removes the existing fragment from the complex range
    - `Transform` - transforms existing (old) fragment to a new one. Old and new fragments should not be the same and they should overlap.
    - `Move` - moves the existing (old) fragment to a new location. Old and new fragments should not be the same and they should have the same length (element count). `Move` and `Transform` are similiar but they might be handled by the code that animates transition operations. If old and new fragments are not the same and they have **different** lengths, then a sequence of `Remove` and `Insert` should be used.
    - `Split` - splits existing (origin) fragment to other (destination) fragments that lie completely in the existing fragment and satisfy the following relation (pseudocode):

      `origin[0].firstElement == destination[0].firstElement && origin[origin.fragmentCount - 1].lastElement == destination[length(destination) - 1].lastElement `
    - `Join` - joins multiple (origin) fragments into a single (destination) fragment. Input fragments should lie comptetely in the destination fragment and satisfy the following relation (pseudocode):

      `origin[0].firstElement == destination[0].firstElement && origin[origin.fragmentCount - 1].lastElement == destination[length(destination) - 1].lastElement`.

## Implementation

The implementation is written in Kotlin. Java imports are not used, so technically the code can be used in Multiplatform, though it hasn't been tested. 

In general, there's nothing Kotlin specific, so the current implementation can be rewritten in any other language.