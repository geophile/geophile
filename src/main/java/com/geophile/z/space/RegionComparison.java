/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

/**
 * Represents the relationship of a {@link com.geophile.z.space.Region} to a {@link com.geophile.z.SpatialObject}.
 */

public enum RegionComparison
{
    /**
     * Represents a {@link Region} outside a {@link com.geophile.z.SpatialObject} to which it was compared.
     */
    REGION_OUTSIDE_OBJECT,

    /**
     * Represents a {@link Region} inside a {@link com.geophile.z.SpatialObject} to which it was compared.
     */
    REGION_INSIDE_OBJECT,

    /**
     * Represents a {@link Region} that overlaps (and is not inside) a {@link com.geophile.z.SpatialObject} to which it was compared.
     */
    REGION_OVERLAPS_OBJECT  // region overlaps but is not inside
}
