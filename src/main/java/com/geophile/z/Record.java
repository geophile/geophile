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
     * Copy the state of this Record into the given Record. This method is only called if this Record is used in
     * conjunction with an Index with stableRecords() = false.
     * @param record The Record to be modified.
     */
    void copyTo(Record record);

    /**
     * Used to filter {@link Record}s in conjunction
     * with {@link SpatialIndex#remove(com.geophile.z.SpatialObject, com.geophile.z.Record.Filter)}.
     * @param <RECORD> Type of the Records to be filtered.
     */

    /**
     * Used in conjuction with {@link com.geophile.z.SpatialIndex#remove(SpatialObject, com.geophile.z.Record.Filter)},
     * a Filter identifies a record to be removed from a spatial index.
     * @param <RECORD> An implementation of Record.
     */
    interface Filter<RECORD extends Record>
    {
        /**
         * Returns true iff the given Record is of interest, (i.e., should be removed).
         * @param record A candidate for removal.
         * @return true iff the given Record is of interest, (i.e., should be removed).
         */
        boolean select(RECORD record);
    }

    /**
     * Used in conjuction with {@link com.geophile.z.SpatialIndex#add(SpatialObject, com.geophile.z.Record.Factory)},
     * a Factory creates a RECORD to be inserted into a spatial index. The RECORD will be owned by the spatial index,
     * and if made available to the application, the application should not modify it.
     * @param <RECORD> An implementation of Record.
     */
    interface Factory<RECORD extends Record>
    {
        /**
         * Returns a new RECORD.
         * @return a new RECORD
         */
        RECORD newRecord();
    }
}
