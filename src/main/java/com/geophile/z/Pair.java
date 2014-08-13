package com.geophile.z;

/**
 * A pair of possibly overlapping spatial objects obtained by a many/many spatial join.
 * @param <LEFT_RECORD> Record type of spatial join's left input.
 * @param <RIGHT_RECORD> Record type of spatial join's right input.
 */

public class Pair<LEFT_RECORD extends Record, RIGHT_RECORD extends Record>
{
    // Object interface

    @Override
    public String toString()
    {
        return String.format("(%s, %s)", left, right);
    }

    @Override
    public boolean equals(Object o)
    {
        boolean eq = false;
        if (o != null && o instanceof Pair) {
            Pair that = (Pair) o;
            eq =
                this.left.hashCode() == that.left.hashCode() &&
                this.right.hashCode() == that.right.hashCode() &&
                this.left.equals(that.left) &&
                this.right.equals(that.right);
        }
        return eq;
    }

    @Override
    public int hashCode()
    {
        return left.hashCode() ^ right.hashCode();
    }

    public Pair(LEFT_RECORD left, RIGHT_RECORD right)
    {
        this.left = left;
        this.right = right;
    }

    // Pair interface

    /**
     * The left side of the Pair.
     * @return The left side of the Pair.
     */
    public LEFT_RECORD left()
    {
        return left;
    }

    /**
     * The right side of the Pair.
     * @return The right side of the Pair.
     */
    public RIGHT_RECORD right()
    {
        return right;
    }

    // Object state

    private final LEFT_RECORD left;
    private final RIGHT_RECORD right;
}
