package com.geophile;

import com.geophile.spatialobject.SpatialObject;

public class Pair<LEFT extends SpatialObject, RIGHT extends SpatialObject>
{
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
