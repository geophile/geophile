package com.geophile.z.spatialobject.jts;

import com.geophile.z.SpatialObject;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.geophile.z.spatialobject.SpatialObjectIdGenerator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

public class JTSPoint extends JTSBase
{
    // SpatialObject interface (not implemented by JTSBase)

    @Override
    public double[] arbitraryPoint()
    {
        double[] point = new double[2];
        Coordinate coord = point().getCoordinate();
/*
        point[0] = coord.x;
        point[1] = coord.y;
*/
        return point;
    }

    @Override
    public int maxZ()
    {
        return 1;
    }

    @Override
    public boolean equalTo(SpatialObject that)
    {
        boolean eq = false;
        if (that != null && that instanceof JTSPoint) {
            Point thatPoint = ((JTSPoint) that).point();
            eq = point().equalsExact(thatPoint);
        }
        return eq;
    }

    @Override
    public boolean containedBy(Region region)
    {
        ensureBoundingBox();
        Point point = point();
        return
            region.lo(0) <= point.getX() && point.getX() <= region.hi(0) &&
            region.lo(1) <= point.getY() && point.getY() <= region.hi(1);
    }

    @Override
    public RegionComparison compare(Region region)
    {
        Point point = point();
        return null; // region.isPoint() && region.lo(0) == point.getX() && region.lo(1) == point.getY();
    }

    // JTSPoint interface

    public JTSPoint(Point point)
    {
        super(point);
    }

    // For use by this class

    private Point point()
    {
        ensureGeometry();
        return (Point) geometry;
    }
}
