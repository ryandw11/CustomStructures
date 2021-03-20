package com.ryandw11.structure.utils;

/**
 * A utility class to represent a Pair.
 *
 * @param <L> The type of the left item.
 * @param <R> The type of the right item.
 */
public class Pair<L, R> {
    private final L left;
    private final R right;

    /**
     * Create a Pair.
     *
     * @param left  The left value.
     * @param right The right value.
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Get the left value.
     *
     * @return The left value.
     */
    public L getLeft() {
        return left;
    }

    /**
     * Get the right value.
     *
     * @return The right value.
     */
    public R getRight() {
        return right;
    }

    /**
     * Quick way of creating a pair.
     *
     * @param left  The left value.
     * @param right The right value.
     * @param <L>   The left type.
     * @param <R>   The right type.
     * @return A new pair.
     */
    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }
}
