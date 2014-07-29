/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.Space;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.SpatialObjectKey;

import java.nio.ByteBuffer;

// A SpatialObject that is stored in an index to track the maximum reserved spatial object id. A spatial index record
// is (z, soid, spatial object). z = SpatialIndexMetadata.Z_MAX_RESERVED. soid is the maximum soid reserved so far,
// for the index containing this record. SpatialIndexMetadata is the spatial object, serializing to a zero-length
// byte array.

public class SpatialIndexMetadata implements SpatialObject
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
        return ID;
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
        return that != null && that instanceof SpatialIndexMetadata;
    }

    @Override
    public boolean containedBy(Region region)
    {
        assert false;
        return false;
    }

    @Override
    public boolean containedBy(Space space)
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
        firstUnreservedSoid = buffer.getLong();
    }

    @Override
    public void writeTo(ByteBuffer buffer)
    {
        buffer.putLong(firstUnreservedSoid);
    }

    // SpatialIndexMetadata interface

    public long firstUnreservedSoid()
    {
        return firstUnreservedSoid;
    }

    public SpatialIndexMetadata(long firstUnreservedSoid)
    {
        this.firstUnreservedSoid = firstUnreservedSoid;
    }

    public SpatialIndexMetadata()
    {}

    // Class state

    private static final long ID = -1L;
    public static final SpatialObjectKey SPATIAL_INDEX_METADATA_KEY = SpatialObjectKey.key(-1L, ID);
    public static final int SPATIAL_INDEX_METADATA_TYPE_ID = -1;

    // Object state

    // Spatial object id generation
    private long firstUnreservedSoid;
}
