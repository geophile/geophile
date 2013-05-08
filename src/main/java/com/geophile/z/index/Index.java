/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.spatialobject.SpatialObject;

/**
 * An index used to contain {@link com.geophile.z.spatialobject.SpatialObject}s must implement this interface.
 *
 * Removal of a spatial object must be done using both
 * {@link #remove(long, com.geophile.z.spatialobject.SpatialObject)}
 * and {@link #remove(long, long)}. {@link #remove(long, com.geophile.z.spatialobject.SpatialObject)}
 * is called when the first z-value of a spatial object is
 * removed. This returns the id of the removed spatial object. Subsequent removal of z-values uses
 * {@link #remove(long, long)}, passing in the id returned on the first call.
 * This procedure for removal ensures that, in case of duplicate objects, or objects with common z-values,
 * all records of a single spatial object are removed. The first call may be
 * slower than the subsequent ones, because it may be necessary to search several z-values for a record
 * whose spatial object matches the one being removed. Subsequent calls can go directly to the record in question
 * because index records are keyed by (z, id).
 *
 * @param <SPATIAL_OBJECT> The type of {@link com.geophile.z.spatialobject.SpatialObject} contained by this index.
 *
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
     * Removes the association between the given z-value and the given spatial object. Complete
     * removal of a spatial object will require removal for each of the object's z-values.
     * @param z z-value representing a Region that overlaps the spatial object.
     * @param spatialObject The spatial object being removed from the index.
     * @return The id of the spatial object whose z-value was removed, or -1 if the object was not present.
     */
    long remove(long z, SPATIAL_OBJECT spatialObject);

    /**
     * Removes the association between the given z-value and the spatial object with the given id.
     * Complete removal of a spatial object will require removal for each of the object's z-values.
     * @param z z-value representing a Region that overlaps the spatial object.
     * @param soid Identifies the spatial object being removed from the index.
     * @return true if (z, soid) was found and removed, false otherwise.
     */
    boolean remove(long z, long soid);

    /**
     * Returns a {@link Cursor} positioned at the given z-value.
     * @param z A z-value
     * @return A {@link Cursor} postioned at the given z-value.
     */
    Cursor<SPATIAL_OBJECT> cursor(long z);

    SpatialObjectKey key(long z);

    SpatialObjectKey key(long z, long soid);
}
