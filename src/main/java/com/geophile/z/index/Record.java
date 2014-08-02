package com.geophile.z.index;

import com.geophile.z.SpatialObject;

public class Record
{
    // Object interface

    @Override
    public String toString()
    {
        return key.toString();
    }

    // Record interface

    public SpatialObjectKey key()
    {
        return key;
    }

    public SpatialObject spatialObject()
    {
        return spatialObject;
    }

    public void copyTo(Record record)
    {
        if (record == this) {
            throw new IllegalArgumentException();
        }
        record.key = key;
        record.spatialObject = spatialObject;
    }

    public void set(long z, SpatialObject spatialObject)
    {
        this.key = SpatialObjectKey.key(z, spatialObject.id());
        this.spatialObject = spatialObject;
    }

    // For creating a Record used just for its key, e.g. in a binary search of an array of Records.
    public void set(long z, long soid)
    {
        this.key = SpatialObjectKey.key(z, soid);
        this.spatialObject = null;
    }

    public Record()
    {
    }

    // Object state

    private SpatialObjectKey key;
    private SpatialObject spatialObject;
}
