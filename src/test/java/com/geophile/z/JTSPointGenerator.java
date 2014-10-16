package com.geophile.z;

import com.geophile.z.spatialjoin.SpatialObjectGenerator;
import com.geophile.z.spatialobject.jts.JTS;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.Random;

public class JTSPointGenerator extends SpatialObjectGenerator
{
    @Override
    public SpatialObject newSpatialObject()
    {
        long x = random.nextInt(nx);
        long y = random.nextInt(ny);
        return JTS.spatialObject(space, factory.createPoint(new Coordinate(x, y)));
    }

    @Override
    public String description()
    {
        return "JTSPoint";
    }

    public JTSPointGenerator(Space space, GeometryFactory factory, Random random)
    {
        super(space, random);
        this.factory = factory;
        this.nx = (int) (space.hi(0) - space.lo(0));
        this.ny = (int) (space.hi(1) - space.lo(1));
    }

    private final GeometryFactory factory;
    private final int nx;
    private final int ny;
}
