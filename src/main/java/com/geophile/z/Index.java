/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import java.io.IOException;

/**
 * An index containing {@link com.geophile.z.Record}s must extend this class.
 * Access to Index contents is accomplished using a {@link Cursor}, obtained by
 * {@link #cursor()}.
 * @param <RECORD> The type of {@link com.geophile.z.Record} contained by this Index.
 */

public abstract class Index<RECORD extends Record>
{
    /**
     * Adds a {@link com.geophile.z.Record} to this index.
     * An Index implementation must not assume that it owns the record, and that the record is immutable.
     * This means that the state of the record must be copied into the index before this method returns.
     * Inserting the given record directly into the index is dangerous because the state of the record
     * may be changed by the caller.
     * @param record The record being added to this Index.
     * @throws DuplicateRecordException if the record is already present. This exception cannot
     *         be thrown by an index that does blind updates.
     */
    public abstract void add(RECORD record)
        throws IOException, InterruptedException, DuplicateRecordException;

    /**
     * Removal of an indexed spatial object requires removal of the {@link com.geophile.z.Record}s
     * containing each of the spatial object's z-values. Z-values are not unique -- the same z-value
     * may be part of the decomposition of any number of {@link com.geophile.z.SpatialObject}s.
     * A {@link com.geophile.z.Record.Filter} is used to determine which {@link com.geophile.z.Record}s
     * are to be removed. Geophile (via {@link com.geophile.z.SpatialIndex#remove(SpatialObject, com.geophile.z.Record.Filter)})
     * orchestrates the calls to this method, and does not ensure that recordFilter.select returns true
     * for at most one record. Instead, it proceeds until one such record is located and removes that record.
     * @param z z-value representing a region that overlaps the spatial object being removed.
     * @param filter identifies which {@link com.geophile.z.Record} associated with the given
     *                     z-value should be removed.
     * @return false if this index does blind updates. Otherwise, the return value is true
     *               iff a {@link com.geophile.z.Record} is found such that it has given z-value, and causes
     *               recordFilter.select to return true.
     */
    public abstract boolean remove(long z, Record.Filter<RECORD> filter) throws IOException, InterruptedException;

    /**
     * Returns a {@link com.geophile.z.Cursor} that can visit this Index's records.
     * @return A {@link com.geophile.z.Cursor} that can visit this Index's records.
     */
    public abstract Cursor<RECORD> cursor() throws IOException, InterruptedException;

    /**
     * Returns a {@link com.geophile.z.Record} that can be added to this Index.
     * @return A {@link com.geophile.z.Record} that can be added to this Index.
     */
    public abstract RECORD newRecord();

    /**
     * Returns a {@link com.geophile.z.Record} that can be used as a key to search this Index.
     * @return A {@link com.geophile.z.Record} that can be used as a key to search this Index.
     */
    public RECORD newKeyRecord()
    {
        return newRecord();
    }

    /**
     * Indicates whether this index does blind updates.
     * @return true if this index does blind updates, false otherwise.
     */
    public abstract boolean blindUpdates();

    /**
     * Indicates whether records retrieved from this index are stable. A stable record doesn't change
     * as the cursor that produces it advances. A record that is not stable has state tied to the cursor,
     * and may change as the cursor moves.
     * @return true iff records retrieved from this index are stable.
     */
    public abstract boolean stableRecords();
}
