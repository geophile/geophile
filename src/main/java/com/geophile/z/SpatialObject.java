/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A SpatialObject represents a set of points in a {@link com.geophile.z.Space}.
 * Each SpatialObject has an {@link #id()}, assigned by the application, which must be unique within the containing
 * {@link com.geophile.z.SpatialIndex}.
 */

public interface SpatialObject
{
    /**
     * Returns the identifier, which must be unique within the containing {@link com.geophile.z.SpatialIndex}.
     * @return This object's unique identifier.
     */
    long id();

    /**
     * Returns the coordinates of an arbitrary point inside this spatial object.
     * @return The coordinates of an arbitrary point inside this spatial object.
     */
    long[] arbitraryPoint();

    /**
     * Returns the maximum number of z-values to be used in approximating this object.
     * @return The maximum number of z-values to be used in approximating this object.
     */
    int maxZ();

    /**
     * Returns true if this and that describe the same points in space, false otherwise.
     * Two spatial objects, x and y, are considered to be equal iff for every point p in the space,
     * x.contains(p) = y.contains(p). Spatial object identity, returned by {@link #id()}, has no relevance for
     * determining equality. x.id() = y.id() implies x.equalTo(y), but the converse is not true.
     * @param that Spatial object to compare.
     * @return true if this and that describe the same spatial object, false otherwise.
     */
    boolean equalTo(SpatialObject that);

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

    void readFrom(ByteBuffer buffer) throws IOException;

    void writeTo(ByteBuffer buffer) throws IOException;
}
