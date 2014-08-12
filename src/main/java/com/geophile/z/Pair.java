package com.geophile.z;

/**
 * A pair of overlapping {@link SpatialObject}s, obtained via spatial join.
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
                // Avoid Record.equals check if possible
                this.left.keyHash() == that.left.keyHash() &&
                this.right.keyHash() == that.right.keyHash() &&
                // Hashes match, have to check Record.equals
                this.left.equals(that.left) && this.right.equals(that.right);
        }
        return eq;
    }

    @Override
    public int hashCode()
    {
        return left.keyHash() ^ right.keyHash();
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

    public Pair(LEFT_RECORD left, RIGHT_RECORD right)
    {
        this.left = left;
        this.right = right;
    }

    // Object state

    private final LEFT_RECORD left;
    private final RIGHT_RECORD right;
}
