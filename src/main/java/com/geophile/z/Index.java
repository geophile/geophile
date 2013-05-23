/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.index.Cursor;
import com.geophile.z.index.SpatialObjectKey;

/**
 * An index used to contain {@link com.geophile.z.SpatialObject}s must implement this interface.
 *
 * When a {@link com.geophile.z.SpatialObject} is added to a {@link com.geophile.z.SpatialIndex},
 * records of the form ((z, id), s) are added to an Index. s is the spatial object.
 * The key, (z, id), comprises a z-value z, and the spatial object's id.
 * ({@link com.geophile.z.SpatialIndex} is responsible for generating z-values and calling
 * {@link Index#add(long, com.geophile.z.SpatialObject)}.)
 *
 * Removal of a {@link com.geophile.z.SpatialObject} from a {@link com.geophile.z.SpatialIndex} is accomplished
 * in two steps. First, {@link #remove(long, com.geophile.z.SpatialObject)} is called. This removes one
 * (z, s) record for the given {@link com.geophile.z.SpatialObject}, and returns its id. Second, remaining
 * index records are removed using
 * {@link #remove(long, long)}, passing in the id returned on the first call.
 * This procedure for removal ensures that, in case of duplicate objects, or objects with common z-values,
 * all records of a single spatial object are removed. The first call may be
 * slower than the subsequent ones, because it may be necessary to search several z-values for a record
 * whose spatial object matches the one being removed. Subsequent calls can go directly to the record in question
 * because index records are keyed by (z, id).
 *
 * Access to Index contents is accomplished using a {@link com.geophile.z.index.Cursor}, obtained by
 * {@link Index#cursor(long)}.
 *
 * @param <SPATIAL_OBJECT> The type of {@link com.geophile.z.SpatialObject} contained by this index.
 *
 */

public interface Index<SPATIAL_OBJECT extends SpatialObject>
{
    /**
     * Adds a spatial object to this index, associated with the given z-value.
     * @param z z-value representing a region that overlaps the spatial object.
     * @param spatialObject The spatial object being added to the index.
     */
    void add(long z, SPATIAL_OBJECT spatialObject);

    /**
     * Removes the association between the given z-value and the given spatial object. Complete
     * removal of a spatial object will require removal for each of the object's z-values.
     * @param z z-value representing a region that overlaps the spatial object.
     * @param spatialObject The spatial object being removed from the index.
     * @return The id of the spatial object whose z-value was removed, or -1 if the object was not present.
     */
    long remove(long z, SPATIAL_OBJECT spatialObject);

    /**
     * Removes the association between the given z-value and the spatial object with the given id.
     * Complete removal of a spatial object will require removal for each of the object's z-values.
     * @param z z-value representing a region that overlaps the spatial object.
     * @param soid Identifies the spatial object being removed from the index.
     * @return true if (z, soid) was found and removed, false otherwise.
     */
    boolean remove(long z, long soid);

    /**
     * Returns a {@link com.geophile.z.index.Cursor} positioned at the given z-value.
     * @param z A z-value
     * @return A {@link com.geophile.z.index.Cursor} postioned at the given z-value.
     */
    Cursor<SPATIAL_OBJECT> cursor(long z);

    /**
     * Returns an index key comprising the given z-value, and -1 in place of the spatial object's id.
     * (Spatial object ids are non-negative.)
     * @param z A z-value.
     * @return An index key (z, -1).
     */
    SpatialObjectKey key(long z);

    /**
     * Returns an index key comprising the given z-value, and the given spatial object id.
     * @param z A z-value.
     * @return An index key (z, soid).
     */
    SpatialObjectKey key(long z, long soid);
}
