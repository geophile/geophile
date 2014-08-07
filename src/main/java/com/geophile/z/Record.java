package com.geophile.z;

public interface Record
{
    SpatialObjectKey key();
    SpatialObject spatialObject();
    void copyTo(Record record);
    void set(long z, SpatialObject spatialObject);
    // For creating a Record used just for its key, e.g. in a binary search of an array of Records.
    void set(long z, long soid);
}
