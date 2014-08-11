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
 * A SpatialIndex organizes a set of {@link SpatialObject}s for the efficient execution of spatial searches.
 */

public abstract class SpatialIndex
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
     * Adds the given spatial object to the index.
     * @param record The record to be added.
     */
    public abstract void add(Record record) throws IOException, InterruptedException;

    /**
     * Removes the given spatial object from the index.
     * @param spatialObject The object to be removed.
     * @param filterTODO
     * @return true if spatialObject was found and removed, false otherwise
     */
    public abstract boolean remove(SpatialObject spatialObject,
                                   RecordFilter recordFilter) throws IOException, InterruptedException;

    /**
     * Creates a SpatialIndex. The index
     * should never be manipulated directly at any time. It is intended to be maintained and searched only
     * through the interface of this class.
     * @param space The {@link Space} containing the {@link SpatialObject}s to be indexed.
     * @param index The {@link Index} that will store the indexed {@link SpatialObject}s.
     */
    public static SpatialIndex newSpatialIndex(Space space, Index index) throws IOException, InterruptedException
    {
        return newSpatialIndex(space, index, Options.DEFAULT);
    }

    /**
     * Creates a SpatialIndex. The index
     * should never be manipulated directly at any time. It is intended to be maintained and searched only
     * through the interface of this class.
     * @param space The {@link Space} containing the {@link SpatialObject}s to be indexed.
     * @param index The {@link Index} that will store the indexed {@link SpatialObject}s.
     */
    public static SpatialIndex newSpatialIndex(Space space, Index index, Options options)
        throws IOException, InterruptedException
    {
        return new SpatialIndexImpl((SpaceImpl) space, index, options);
    }

    // For use by subclasses

    protected SpatialIndex(SpaceImpl space, Index index, Options options)
    {
        this.space = space;
        this.index = index;
        this.options = options;
    }

    // Object state

    protected final SpaceImpl space;
    protected final Index index;
    protected final Options options;

    // Inner classes

    public enum Options {DEFAULT, SINGLE_CELL}

    public class Exception extends RuntimeException
    {
        public Exception(String message)
        {
            super(message);
        }
    }
}
