/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;

import java.nio.ByteBuffer;

/**
 * A SpatialObject represents a set of points in a {@link com.geophile.z.Space}.
 */

public interface SpatialObject
{
    /**
     * Returns the coordinates of an arbitrary point inside this spatial object.
     * @return The coordinates of an arbitrary point inside this spatial object.
     */
    double[] arbitraryPoint();

    /**
     * Returns the maximum number of z-values to be used in approximating this object.
     * @return The maximum number of z-values to be used in approximating this object.
     */
    int maxZ();

    /**
     * Returns true iff this spatial object is contained by the given Space.
     * @param space The Space to compare to.
     * @return true iff this spatial object is contained by the Space.
     */
    boolean containedBy(Space space);

    /**
     * Returns true iff this spatial object is contained by the given Region.
     * @param region The Region to compare to.
     * @return true iff this spatial object is contained by the Region.
     */
    boolean containedBy(Region region);

    /**
     * Returns the relationship of this spatial object to the given Region.
     * @param region region to compare.
     * @return The relationship of this spatial object to the given Region.
     */
    RegionComparison compare(Region region);

    /**
     * Reconstitute this SpatialObject from its serialized form.
     * @param buffer Contains the serialized bytes of a spatial object.
     */
    void readFrom(ByteBuffer buffer);

    /**
     * Write the serialized form of this SpatialObject to the given buffer.
     * @param buffer Receives the serialized bytes.
     */
    void writeTo(ByteBuffer buffer);
}
