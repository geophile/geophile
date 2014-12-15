/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.space.SpaceImpl;
import com.geophile.z.space.SpatialIndexImpl;

import java.io.IOException;

/**
 * A SpatialIndex organizes a set of {@link SpatialObject}s for the efficient execution of spatial joins.
 * There are two overloadings for add and for remove. The overloadings for each method differ in whether
 * the maximum number of z-values is specified for the spatial object's decomposition. Remove will work only
 * if the maximum number of z-values is the same as was specified when the object was added. To minimize
 * the possibility of getting this wrong, use the default values, (i.e., use the overloadings without the
 * maxZ arguments).
 */

public abstract class SpatialIndex<RECORD extends Record>
{
    /**
     * Returns the {@link com.geophile.z.Space} associated with this SpatialIndex.
     * @return The {@link com.geophile.z.Space} associated with this SpatialIndex.
     */
    public final Space space()
    {
        return space;
    }

    /**
     * Adds up to spatialObject.maxZ() records to the index, associated with the given
     * {@link com.geophile.z.SpatialObject}. The records are intended to differ only by the z-values,
     * which are obtained by decomposing spatialObject. The recordFactory allocates the records to be added.
     * After creating each record, Geophile will set the record's z-value by calling Record.z(long).
     * @param spatialObject The {@link com.geophile.z.SpatialObject} being indexed.
     * @param recordFactory Creates the record to be added.
     */
    public final void add(SpatialObject spatialObject, Record.Factory<RECORD> recordFactory)
        throws IOException, InterruptedException
    {
        add(spatialObject, recordFactory, USE_SPATIAL_OBJECT_MAX_Z);
    }

    /**
     * Adds up to maxZ records to the index, associated with the given
     * {@link com.geophile.z.SpatialObject}. The records are intended to differ only by the z-values,
     * which are obtained by decomposing spatialObject. The recordFactory allocates the records to be added.
     * After creating each record, Geophile will set the record's z-value by calling Record.z(long).
     * @param spatialObject The {@link com.geophile.z.SpatialObject} being indexed.
     * @param recordFactory Creates the record to be added.
     * @param maxZ The maximum number of z-values to be generated for the given {@link com.geophile.z.SpatialObject}.
     */
    public abstract void add(SpatialObject spatialObject, Record.Factory<RECORD> recordFactory, int maxZ)
        throws IOException, InterruptedException;

    /**
     * Removes from this index the record associated with the given {@link com.geophile.z.SpatialObject}.
     * A number of records may be located during the removal. The given {@link com.geophile.z.Record.Filter}
     * will identify the records to be removed.
     * @param spatialObject Key of the records to be removed.
     * @param recordFilter Identifies the exact records to be removed, causing false positives to be ignored.
     * @return true if spatialObject was found and removed, false otherwise
     */
    public final boolean remove(SpatialObject spatialObject,
                                Record.Filter<RECORD> recordFilter) throws IOException, InterruptedException
    {
        return remove(spatialObject, recordFilter, USE_SPATIAL_OBJECT_MAX_Z);
    }

    /**
     * Removes from this index the record associated with the given {@link com.geophile.z.SpatialObject}.
     * A number of records may be located during the removal. The given {@link com.geophile.z.Record.Filter}
     * will identify the records to be removed.
     * @param spatialObject Key of the records to be removed.
     * @param recordFilter Identifies the exact records to be removed, causing false positives to be ignored.
     * @param maxZ The maximum number of z-values to be generated for the given {@link com.geophile.z.SpatialObject}.
     * @return true if spatialObject was found and removed, false otherwise
     */
    public abstract boolean remove(SpatialObject spatialObject,
                                   Record.Filter<RECORD> recordFilter,
                                   int maxZ) throws IOException, InterruptedException;

    /**
     * Creates a SpatialIndex. The index
     * should never be manipulated directly at any time. It is intended to be maintained and searched only
     * through the interface of this class.
     * @param space The {@link Space} containing the {@link SpatialObject}s to be indexed.
     * @param index The {@link Index} that will store the indexed {@link SpatialObject}s.
     * @return A new SpatialIndex
     */
    public static <RECORD extends Record> SpatialIndex<RECORD> newSpatialIndex(Space space,
                                                                               Index<RECORD> index)
        throws IOException, InterruptedException
    {
        return newSpatialIndex(space, index, Options.DEFAULT);
    }

    /**
     * Creates a SpatialIndex. The index
     * should never be manipulated directly at any time. It is intended to be maintained and searched only
     * through the interface of this class.
     * @param space The {@link Space} containing the {@link SpatialObject}s to be indexed.
     * @param index The {@link Index} that will store the indexed {@link SpatialObject}s.
     * @return A new SpatialIndex.
     */
    public static <RECORD extends Record> SpatialIndex<RECORD> newSpatialIndex(Space space,
                                                                               Index<RECORD> index,
                                                                               Options options)
        throws IOException, InterruptedException
    {
        return new SpatialIndexImpl<>((SpaceImpl) space, index, options);
    }

    // For use by subclasses

    protected SpatialIndex(SpaceImpl space, Index<RECORD> index, Options options)
    {
        this.space = space;
        this.index = index;
        this.options = options;
    }

    // Class state

    protected static final int USE_SPATIAL_OBJECT_MAX_Z = -1;

    // Object state

    protected final SpaceImpl space;
    protected final Index<RECORD> index;
    protected final Options options;

    // Inner classes

    public enum Options {DEFAULT, SINGLE_CELL}

    public static class Exception extends RuntimeException
    {
        public Exception(String message)
        {
            super(message);
        }
    }
}
