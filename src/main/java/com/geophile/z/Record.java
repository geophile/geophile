package com.geophile.z;

/**
 * A Record represents a record in an {@link com.geophile.z.Index}. A Geophile application must provide
 * an implementation of this interface. A Record contains a z-value, resulting from the decomposition of a
 * {@link com.geophile.z.SpatialObject}, as well as other state provided by the application. The records
 * representing the decomposition of a {@link com.geophile.z.SpatialObject} will typically contain some
 * identifier of that {@link com.geophile.z.SpatialObject}.
 *
 * {@link #hashCode()} and {@link #equals(Object)} must be implemented both to permit the use of hash tables keyed by
 * Records, and to support duplicate elimination from spatial join output. The latter purpose requires that
 * Record.z() NOT be included in the computation of {@link #hashCode()} and {@link #equals(Object)}.
 */

public interface Record
{
    // Object

    /**
     * The hash value for this Record, which must not depend on z().
     * @return The hash value for this Record.
     */
    int hashCode();

    /**
     * Test equality against o, which must be a {@link com.geophile.z.Record} of the same type.
     * The comparison must not depend on z().
     * @param o {@link com.geophile.z.Record} to compare to.
     * @return true if this and o are equal (ignoring z()), false otherwise.
     */
    boolean equals(Object o);

    // Record

    /**
     * This Record's z-value.
     * @return this Record's z-value.
     */
    long z();

    /**
     * Sets this Record's z-value.
     * @param z The z-value to assign.
     */
    void z(long z);

    /**
     * Copy the state of this Record into the given Record.
     * @param record The Record to be modified.
     */
    void copyTo(Record record);
}
