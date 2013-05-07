package com.geophile.index;

import java.util.Map;

public class Record<SPATIAL_OBJECT> implements Map.Entry<Long, SPATIAL_OBJECT>
{
    // Record interface

    public long z()
    {
        return z;
    }

    public SPATIAL_OBJECT spatialObject()
    {
        return spatialObject;
    }

    // Map.Entry interface

    @Override
    public Long getKey()
    {
        return z;
    }

    @Override
    public SPATIAL_OBJECT getValue()
    {
        return spatialObject;
    }

    @Override
    public SPATIAL_OBJECT setValue(SPATIAL_OBJECT value)
    {
        throw new UnsupportedOperationException();
    }

    // For use by this package

    void z(long z)
    {
        this.z = z;
    }

    void spatialObject(SPATIAL_OBJECT spatialObject)
    {
        this.spatialObject = spatialObject;
    }

    // Object state

    private long z;
    private SPATIAL_OBJECT spatialObject;
}
