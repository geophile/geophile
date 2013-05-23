package com.geophile.z;

/**
 * A pair of overlapping {@link SpatialObject}s, obtained via spatial join.
 * @param <LEFT> The {@link SpatialObject} on the left side of a Pair.
 * @param <RIGHT> The {@link SpatialObject} on the right side of a Pair.
 */

public class Pair<LEFT extends SpatialObject, RIGHT extends SpatialObject>
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
            Pair<LEFT, RIGHT> that = (Pair<LEFT, RIGHT>) o;
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
    public LEFT left()
    {
        return left;
    }

    /**
     * The right side of the Pair.
     * @return The right side of the Pair.
     */
    public RIGHT right()
    {
        return right;
    }

    public Pair(LEFT left, RIGHT right)
    {
        this.left = left;
        this.right = right;
    }

    // Object state

    private final LEFT left;
    private final RIGHT right;
}
