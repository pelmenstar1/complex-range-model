# Proof-of-concept of modeling complex ranges and creating transitions between them

In this context, the complex range does not relate to complex numbers but to its **complex** structure. A complex range is a union of fragments, i.e. $a <= x <= b$. These fragments should not mutually intersect. More formally:

```math
F(a, b) = \\{ x \in \mathbb{Z} \mid a <= x <= b \\}\text{, where } a <= b
```

$$
C(a_1 \dots a_n, b_1 \dots b_n) = \bigcup\limits_{i=1}^{n} F(a_{i}, b_{i})\text {, where } F(a_i, b_i) \cap F(a_j, b_j) = \emptyset \quad \forall i, j \quad i \ne j
$$

## Transitions