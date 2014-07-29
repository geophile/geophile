/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject.jts;

import com.geophile.z.Space;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public abstract class JTSBaseWithBoundingBox extends JTSBase
{
    // SpatialObject interface

    // Region comparisons use bounding box. Override to get more precise.

    @Override
    public boolean containedBy(Region region)
    {
        assert region.space() == space;
        ensureBoundingBox();
        return
            region.lo(0) <= xLo && xHi <= region.hi(0) &&
            region.lo(1) <= yLo && yHi <= region.hi(1);
    }

    @Override
    public boolean containedBy(Space space)
    {
        assert space == this.space;
        Envelope envelope = geometry.getEnvelopeInternal();
        return
            space.lo(0) <= envelope.getMinX() && envelope.getMaxX() <= space.hi(0) &&
            space.lo(1) <= envelope.getMinY() && envelope.getMaxY() <= space.hi(1);
    }

    @Override
    public RegionComparison compare(Region region)
    {
        assert region.space() == space;
        ensureBoundingBox();
        long rxLo = region.lo(0);
        long rxHi = region.hi(0);
        long ryLo = region.lo(1);
        long ryHi = region.hi(1);
        return
            xLo <= rxLo && rxHi <= xHi && yLo <= ryLo && ryHi <= yHi
            ? RegionComparison.REGION_INSIDE_OBJECT
            : rxHi < xLo || rxLo > xHi || ryHi < yLo || ryLo > yHi
              ? RegionComparison.REGION_OUTSIDE_OBJECT
              : RegionComparison.REGION_OVERLAPS_OBJECT;
    }

    // For use by subclasses

    protected JTSBaseWithBoundingBox(Space space, Geometry geometry)
    {
        super(space, geometry);
    }

    protected JTSBaseWithBoundingBox()
    {}

    // For use by this class

    private boolean boundingBoxAvailable()
    {
        return xLo <= xHi;
    }

    protected void ensureBoundingBox()
    {
        if (!boundingBoxAvailable()) {
            assert geometry != null;
            assert space != null;
            Envelope envelope = geometry.getEnvelopeInternal();
            xLo = space.appToZ(0, envelope.getMinX());
            xHi = space.appToZ(0, envelope.getMaxX());
            yLo = space.appToZ(1, envelope.getMinY());
            yHi = space.appToZ(1, envelope.getMaxY());
            assert boundingBoxAvailable();
        }
    }

    // Object state

    // Bounding box is in geophile space, not app space
    protected long xLo = 0L;
    protected long xHi = -1L;
    protected long yLo;
    protected long yHi;
}
