package com.geophile.z.spatialjoin2;

import com.geophile.z.ApplicationSpace;
import com.geophile.z.SpatialObject;
import com.geophile.z.spatialobject.d2.Box;

import java.util.Random;

public class BoxGenerator extends SpatialObjectGenerator
{
    @Override
    public SpatialObject newSpatialObject()
    {
        long xLo = random.nextInt(nx - maxX);
        long xHi = xLo + (maxX == 1 ? 0 : random.nextInt(maxX));
        long yLo = random.nextInt(ny - maxY);
        long yHi = yLo + (maxY == 1 ? 0 : random.nextInt(maxY));
        return new Box(xLo, xHi, yLo, yHi);
    }

    @Override
    public String description()
    {
        return String.format("max sizes (%s, %s)", maxX, maxY);
    }

    public int maxX()
    {
        return maxX;
    }

    public int maxY()
    {
        return maxY;
    }

    public BoxGenerator(ApplicationSpace appSpace, Random random, int maxX, int maxY)
    {
        super(appSpace, random);
        this.nx = (int) (appSpace.hi(0) - appSpace.lo(0));
        this.ny = (int) (appSpace.hi(1) - appSpace.lo(1));
        this.maxX = maxX;
        this.maxY = maxY;
    }

    private final int nx;
    private final int ny;
    private final int maxX;
    private final int maxY;
}
