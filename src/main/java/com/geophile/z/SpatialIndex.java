/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.space.SpaceImpl;
import com.geophile.z.space.SpatialIndexImpl;

/**
 * A SpatialIndex organizes a set of {@link SpatialObject}s for the efficient execution of spatial searches.
 * @param <SPATIAL_OBJECT> The type of {@link SpatialObject} contained by this spatial index.
 */

public abstract class SpatialIndex<SPATIAL_OBJECT extends SpatialObject>
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
     * @param spatialObject The object to be added.
     */
    public abstract void add(SPATIAL_OBJECT spatialObject);

    /**
     * Removes the given spatial object from the index.
     * @param spatialObject The object to be removed.
     * @return true if spatialObject was found and removed, false otherwise
     */
    public abstract boolean remove(SPATIAL_OBJECT spatialObject);

    /**
     * Creates a SpatialIndex. The index
     * should never be manipulated directly at any time. It is intended to be maintained and searched only
     * through the interface of this class.
     * @param space The {@link Space} containing the {@link SpatialObject}s to be indexed.
     * @param index The {@link Index} that will store the indexed {@link SpatialObject}s.
     */
    public static <SPATIAL_OBJECT extends SpatialObject> SpatialIndex<SPATIAL_OBJECT> newSpatialIndex
        (Space space, Index<SPATIAL_OBJECT> index)
    {
        return new SpatialIndexImpl<>((SpaceImpl) space, index);
    }

    // For use by subclasses

    protected SpatialIndex(SpaceImpl space, Index<SPATIAL_OBJECT> index)
    {
        this.space = space;
        this.index = index;
    }

    // Object state

    protected final SpaceImpl space;
    protected final Index<SPATIAL_OBJECT> index;
}
