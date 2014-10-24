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

// Region comparisons use bounding box. Override to get more precise.

public class JTSSpatialObjectWithBoundingBox extends JTSSpatialObject
{
    // SpatialObject interface

    @Override
    public boolean containedBy(Space space)
    {
        Envelope envelope = geometry.getEnvelopeInternal();
        return
            space.lo(0) <= envelope.getMinX() && envelope.getMaxX() <= space.hi(0) &&
            space.lo(1) <= envelope.getMinY() && envelope.getMaxY() <= space.hi(1);
    }

    @Override
    public boolean containedBy(Region region)
    {
        ensureBoundingBox();
        return
            region.loLE(0, xLo) && region.hiGE(0, xHi) &&
            region.loLE(1, yLo) && region.hiGE(1, yHi);
    }

    @Override
    public RegionComparison compare(Region region)
    {
        ensureBoundingBox();
        if (region.loGE(0, xLo) && region.hiLT(0, xHi) &&
            region.loGE(1, yLo) && region.hiLT(1, yHi)) {
            return RegionComparison.REGION_INSIDE_OBJECT;
        } else if (region.hiLT(0, xLo) || region.loGT(0, xHi) ||
                   region.hiLT(1, yLo) || region.loGT(1, yHi)) {
            return RegionComparison.REGION_OUTSIDE_OBJECT;
        } else {
            return RegionComparison.REGION_OVERLAPS_OBJECT;
        }
    }

    // JTSSpatialObjectWithBoundingBox interface

    public JTSSpatialObjectWithBoundingBox()
    {}

    // For use by this package

    JTSSpatialObjectWithBoundingBox(Space space, Geometry geometry)
    {
        super(space, geometry);
    }

    // For use by this class

    private void ensureBoundingBox()
    {
        if (!boundingBoxAvailable()) {
            assert geometry != null;
            assert space != null;
            Envelope envelope = geometry.getEnvelopeInternal();
            xLo = envelope.getMinX();
            xHi = envelope.getMaxX();
            yLo = envelope.getMinY();
            yHi = envelope.getMaxY();
            assert boundingBoxAvailable();
        }
    }
    private boolean boundingBoxAvailable()
    {
        return xLo <= xHi;
    }

    // Object state

    protected double xLo = 0L;
    protected double xHi = -1L;
    protected double yLo;
    protected double yHi;
}
