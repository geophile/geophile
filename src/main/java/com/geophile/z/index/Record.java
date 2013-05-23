package com.geophile.z.index;

import com.geophile.z.SpatialObject;

public class Record<SPATIAL_OBJECT extends SpatialObject>
{
    // Object interface

    @Override
    public String toString()
    {
        return eof() ? "EOF" : key.toString();
    }

    // Record interface

    public SpatialObjectKey key()
    {
        return key;
    }

    public SPATIAL_OBJECT spatialObject()
    {
        return spatialObject;
    }

    public boolean eof()
    {
        return key == null && spatialObject == null;
    }

    public void copyTo(Record<SPATIAL_OBJECT> record)
    {
        if (eof()) {
            record.setEOF();
        } else {
            record.key = key;
            record.spatialObject = spatialObject;
        }
    }

    public void set(long z, SPATIAL_OBJECT spatialObject)
    {
        this.key = SpatialObjectKey.key(z, spatialObject.id());
        this.spatialObject = spatialObject;
    }

    public void setEOF()
    {
        key = null;
        spatialObject = null;
    }

    // Object state

    private SpatialObjectKey key;
    private SPATIAL_OBJECT spatialObject;
}
