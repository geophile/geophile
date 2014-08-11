package com.geophile.z;


public interface Record
{
    // Object interface
    /*
     * hashCode and equals must be implemented by Record subclasses:
     * - To permit the use of hash tables keyed by Records.
     * - To support duplicate elimination from spatial join output.
     * Duplicate elimination requires that Record.z() NOT be included in the computation of either hashCode or equals.
     */
    int hashCode();
    boolean equals(Object object);

    // Record interface
    long z();
    void z(long z);
    SpatialObject spatialObject();
    void copyTo(Record record);
    int keyCompare(Record record);
    int keyHash();
}
