package com.geophile.z.spatialjoin;

import com.geophile.z.Space;
import com.geophile.z.SpatialObject;

import java.util.Random;

public abstract class SpatialObjectGenerator
{
    public abstract SpatialObject newSpatialObject();

    public abstract String description();

    protected SpatialObjectGenerator(Space space, Random random)
    {
        this.space = space;
        this.random = random;
    }

    protected final Space space;
    protected final Random random;
}
