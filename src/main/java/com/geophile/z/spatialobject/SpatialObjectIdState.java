/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject;

import com.geophile.z.SpatialObject;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;

import java.nio.ByteBuffer;

// A SpatialObject that is stored in an index to track the maximum reserved spatial object id. A spatial index record
// is (z, soid, spatial object). z = SpatialObjectIdState.Z_MAX_RESERVED. soid is the maximum soid reserved so far,
// for the index containing this record. SpatialObjectIdState is the spatial object, serializing to a zero-length
// byte array.

public class SpatialObjectIdState implements SpatialObject
{
    // SpatialObject interface

    @Override
    public void id(long id)
    {
        assert false;
    }

    @Override
    public long id()
    {
        return firstUnreservedSoid;
    }

    @Override
    public double[] arbitraryPoint()
    {
        assert false;
        return null;
    }

    @Override
    public int maxZ()
    {
        assert false;
        return -1;
    }

    @Override
    public boolean equalTo(SpatialObject that)
    {
        return that != null && that instanceof SpatialObjectIdState;
    }

    @Override
    public boolean containedBy(Region region)
    {
        assert false;
        return false;
    }

    @Override
    public RegionComparison compare(Region region)
    {
        assert false;
        return null;
    }

    @Override
    public void readFrom(ByteBuffer buffer)
    {
    }

    @Override
    public void writeTo(ByteBuffer buffer)
    {
    }

    // SpatialObjectIdState interface

    public SpatialObjectIdState(long firstUnreservedSoid)
    {
        this.firstUnreservedSoid = firstUnreservedSoid;
    }

    // Class state

    public static final long Z_MAX_RESERVED = -1L;

    // Object state

    private final long firstUnreservedSoid;
}
