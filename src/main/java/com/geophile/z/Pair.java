package com.geophile.z;

/**
 * A pair of overlapping {@link SpatialObject}s, obtained via spatial join.
 */

public class Pair
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
    public Record left()
    {
        return left;
    }

    /**
     * The right side of the Pair.
     * @return The right side of the Pair.
     */
    public Record right()
    {
        return right;
    }

    public Pair(Record left, Record right)
    {
        this.left = left;
        this.right = right;
    }

    // Object state

    private final Record left;
    private final Record right;
}
