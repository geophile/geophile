package com.geophile.z;

/**
 * A Record represents a record in an {@link com.geophile.z.Index}. A Geophile application must provide
 * an implementation of this interface. A Record contains the following:
 *
 * - A {@link com.geophile.z.SpatialObject}.
 * - A z-value, resulting from the decomposition of the {@link com.geophile.z.SpatialObject}.
 * - Any other state provided by the application.
 *
 * A Record implementation must have sufficient state to uniquely identify the Record within the
 * {@link com.geophile.z.Index} containing it. This <i>key</i> state is compared by {@link #keyCompare(Record)},
 * and is hashed by {@link #keyHash()}. The key must include the z-value, but the z-value by itself is not sufficient
 * to uniquely identify the Record.
 *
 * {@link #hashCode()} and {@link #equals(Object)} must be implemented both to permit the use of hash tables keyed by
 * Records, and to support duplicate elimination from spatial join output. The latter purpose requires that
 * Record.z() NOT be included in the computation of {@link #hashCode()} and {@link #equals(Object)}.
 */

public interface Record
{
    // Object
    int hashCode();
    boolean equals(Object o);

    // Record
    long z();
    void z(long z);
    void copyTo(Record record);
}
