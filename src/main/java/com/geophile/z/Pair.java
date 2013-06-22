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
            eq = this.left.equals(that.left) && this.right.equals(that.right);
        }
        return eq;
    }

    @Override
    public int hashCode()
    {
        return left.hashCode() ^ right.hashCode();
    }

    // Pair interface

    /**
     * The left side of the Pair.
     * @return The left side of the Pair.
     */
    public SpatialObject left()
    {
        return left;
    }

    /**
     * The right side of the Pair.
     * @return The right side of the Pair.
     */
    public SpatialObject right()
    {
        return right;
    }

    public Pair(SpatialObject left, SpatialObject right)
    {
        this.left = left;
        this.right = right;
    }

    // Object state

    private final SpatialObject left;
    private final SpatialObject right;
}
