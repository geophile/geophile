package com.geophile.z.spatialobject.jts;

import com.geophile.z.SpatialObject;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.geophile.z.space.SpaceImpl;
import com.vividsolutions.jts.geom.Point;

public class JTSPoint extends JTSBase
{
    // Object interface

    @Override
    public String toString()
    {
        return geometry.toString();
    }

    @Override
    public int hashCode()
    {
        return geometry.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj != null && obj instanceof JTSPoint && geometry.equals(((JTSPoint)obj).geometry);
    }

    // SpatialObject interface (not implemented by JTSBase)

    @Override
    public double[] arbitraryPoint()
    {
        double[] point = new double[2];
        Point jtsPoint = point();
        point[0] = jtsPoint.getX();
        point[1] = jtsPoint.getY();
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
        Point point = point();
        return
            region.lo(0) <= point.getX() && point.getX() <= region.hi(0) &&
            region.lo(1) <= point.getY() && point.getY() <= region.hi(1);
    }

    @Override
    public RegionComparison compare(Region region)
    {
        Point point = point();
        SpaceImpl space = (SpaceImpl) region.space();
        long pX = space.appToZ(0, point.getX());
        long pY = space.appToZ(1, point.getY());
        long rXLo = region.lo(0);
        long rYLo = region.lo(1);
        return
            region.isPoint() && rXLo == pX && rYLo == pY
            ? RegionComparison.REGION_INSIDE_OBJECT
            : rXLo <= pX && pX <= region.hi(0) && rYLo <= pY && pY <= region.hi(1)
              ? RegionComparison.REGION_OVERLAPS_OBJECT
              : RegionComparison.REGION_OUTSIDE_OBJECT;
    }

    // JTSPoint interface

    public JTSPoint(Point point)
    {
        super(point);
    }

    public Point point()
    {
        ensureGeometry();
        return (Point) geometry;
    }
}
