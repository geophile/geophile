package com.geophile.z.spatialobject.jts;

import com.geophile.z.Space;
import com.geophile.z.SpatialObject;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.geophile.z.space.SpaceImpl;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public class JTSPoint extends JTSBase
{
    // SpatialObject interface (not implemented by JTSBase)

    @Override
    public double[] arbitraryPoint()
    {
        double[] point = new double[2];
        Coordinate coordinate = point().getCoordinate();
        point[0] = coordinate.x;
        point[1] = coordinate.y;
        return point;
    }

    @Override
    public int maxZ()
    {
        return 1;
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
    public boolean containedBy(Space space)
    {
        assert space == this.space;
        Coordinate coordinate = point().getCoordinate();
        return
            space.lo(0) <= coordinate.x && coordinate.x <= space.hi(0) &&
            space.lo(1) <= coordinate.y && coordinate.y <= space.hi(1);
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

    public Point point()
    {
        ensureGeometry();
        return (Point) geometry;
    }

    public JTSPoint(Space space, Point point)
    {
        super(space, point);
    }

    public JTSPoint()
    {}
}
