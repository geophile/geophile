package com.geophile.z;

import com.geophile.z.spatialobject.SpatialObject;

public class Pair<LEFT extends SpatialObject, RIGHT extends SpatialObject>
{
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

    public LEFT left()
    {
        return left;
    }

    public RIGHT right()
    {
        return right;
    }

    public Pair(LEFT left, RIGHT right)
    {
        this.left = left;
        this.right = right;
    }

    private final LEFT left;
    private final RIGHT right;
}
