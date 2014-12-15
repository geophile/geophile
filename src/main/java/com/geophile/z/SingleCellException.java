/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

/**
 * Indicates an attempt to use a {@link com.geophile.z.SpatialObject} with maxZ &gt; 1 with a
 * {@link com.geophile.z.SpatialIndex} created as {@link com.geophile.z.SpatialIndex.Options#SINGLE_CELL}.
 */

public class SingleCellException extends RuntimeException
{
    public SingleCellException(SpatialObject spatialObject)
    {
        super(spatialObject.toString());
    }
}
