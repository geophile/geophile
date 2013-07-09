package com.geophile.z.spatialjoin;

import com.geophile.z.ApplicationSpace;
import com.geophile.z.SpatialObject;

import java.util.Random;

public abstract class SpatialObjectGenerator
{
    public abstract SpatialObject newSpatialObject();

    public abstract String description();

    protected SpatialObjectGenerator(ApplicationSpace appSpace, Random random)
    {
        this.appSpace = appSpace;
        this.random = random;
    }

    protected final ApplicationSpace appSpace;
    protected final Random random;
}
