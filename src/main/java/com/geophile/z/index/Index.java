/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.spatialobject.SpatialObject;

/**
 * An index used to contain {@link com.geophile.z.spatialobject.SpatialObject}s must implement this interface.
 * @param <SPATIAL_OBJECT> The type of {@link com.geophile.z.spatialobject.SpatialObject} contained by this index.
 */

public interface Index<SPATIAL_OBJECT extends SpatialObject>
{
    /**
     * Adds a spatial object to this index, keyed by a z-value.
     * @param z z-value representing a Region that overlaps the spatial object.
     * @param spatialObject The spatial object being added to the index.
     */
    void add(long z, SPATIAL_OBJECT spatialObject);

    /**
     * Remove the association between the given z-value and the given spatial object. Complete removal
     * of a spatial object will require removal for each of the object's z-values.
     * @param z z-value representing a Region that overlaps the spatial object.
     * @param spatialObject The spatial object being removed from the index.
     */
    void remove(long z, SPATIAL_OBJECT spatialObject);

    /**
     * Returns a {@link Cursor} positioned at the given z-value.
     * @param z A z-value
     * @return A {@link Cursor} postioned at the given z-value.
     */
    Cursor<SPATIAL_OBJECT> cursor(long z);
}
