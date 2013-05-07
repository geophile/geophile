/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.spatialobject;

import com.geophile.z.Region;
import com.geophile.z.RegionComparison;

/**
 * Spatial objects, organized by a {@link com.geophile.SpatialIndex}, must implement the {@link SpatialObject} interface.
 */

public interface SpatialObject
{
    /**
     * Returns the coordinates of an arbitrary point inside this spatial object.
     * @return The coordinates of an arbitrary point inside this spatial object.
     */
    long[] arbitraryPoint();

    /**
     * Returns true iff this spatial object is contained by the given region.
     * @param region The region to compare to.
     * @return true iff this spatial object is contained by the region.
     */
    boolean containedBy(Region region);

    /**
     * Returns the relationship of this spatial object to the given Region.
     * @param region region to compare.
     * @return The relationship of this spatial object to the given Region.
     */
    RegionComparison compare(Region region);
}
