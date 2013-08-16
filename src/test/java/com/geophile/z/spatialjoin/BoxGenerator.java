package com.geophile.z.spatialjoin;

import com.geophile.z.Space;
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

    public BoxGenerator(Space space, Random random, int maxX, int maxY)
    {
        super(space, random);
        this.nx = (int) (space.hi(0) - space.lo(0));
        this.ny = (int) (space.hi(1) - space.lo(1));
        this.maxX = maxX;
        this.maxY = maxY;
    }

    private final int nx;
    private final int ny;
    private final int maxX;
    private final int maxY;
}
