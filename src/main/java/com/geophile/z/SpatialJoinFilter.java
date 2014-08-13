/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

/**
 * Geophile's spatial join algorithm produces false positives. A SpatialJoinFilter can be used to eliminate false
 * positives.
 * @param <LEFT> Type of the left filter argument.
 * @param <RIGHT> Type of the right filter argument.
 *
 * For a many/many spatial join, LEFT and RIGHT are {@link com.geophile.z.Record} types.
 * For a one/many spatial join, LEFT is {@link com.geophile.z.SpatialObject}
 * and RIGHT is a {@link com.geophile.z.Record} type.
 */

public interface SpatialJoinFilter<LEFT, RIGHT>
{
    /**
     * Indicates whether the left and right objects overlap.
     * @param left Object from the left side of the spatial join.
     * @param right Object from the right side of the spatial join.
     * @return true if the objects overlap, false otherwise.
     */
    boolean overlap(LEFT left, RIGHT right);
}
