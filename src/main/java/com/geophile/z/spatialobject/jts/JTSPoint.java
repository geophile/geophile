package com.geophile.z.spatialobject.jts;

import com.geophile.z.Space;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class JTSPoint extends JTSSpatialObject
{
    // SpatialObject interface (not implemented by JTSBase)

    @Override
    public int maxZ()
    {
        return 1;
    }

    @Override
    public boolean containedBy(Space space)
    {
        Coordinate coordinate = geometry.getCoordinate();
        return
            space == this.space &&
            space.lo(0) <= coordinate.x && coordinate.x <= space.hi(0) &&
            space.lo(1) <= coordinate.y && coordinate.y <= space.hi(1);
    }

    @Override
    public boolean containedBy(Region region)
    {
        Point point = point();
        return
            region.loLE(0, point.getX()) && region.hiGE(0, point.getX()) &&
            region.loLE(1, point.getY()) && region.hiGE(1, point.getY());
    }

    @Override
    public RegionComparison compare(Region region)
    {
        return
            containedBy(region)
            ? RegionComparison.REGION_OVERLAPS_OBJECT
            : RegionComparison.REGION_OUTSIDE_OBJECT;
    }

    // JTSPoint interface

    public Point point()
    {
        ensureGeometry();
        return (Point) geometry;
    }

    public JTSPoint()
    {}

    // For use by this package

    JTSPoint(Space space, Point point)
    {
        super(space, point);
    }
}
