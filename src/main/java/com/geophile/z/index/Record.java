package com.geophile.z.index;

import com.geophile.z.spatialobject.SpatialObject;

public class Record<SPATIAL_OBJECT extends SpatialObject>
{
    // Record interface

    public SpatialObjectKey key()
    {
        return key;
    }

    public SPATIAL_OBJECT spatialObject()
    {
        return spatialObject;
    }

    // For use by this package

    void set(long z, SPATIAL_OBJECT spatialObject)
    {
        this.key = SpatialObjectKey.key(z, spatialObject.id());
        this.spatialObject = spatialObject;
    }

    // Object state

    private SpatialObjectKey key;
    private SPATIAL_OBJECT spatialObject;
}
