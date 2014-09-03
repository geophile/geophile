/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

public class SpatialObjectException extends RuntimeException
{
    public static SpatialObjectException notContainedBySpace(SpatialObject spatialObject, Space space)
    {
        throw new SpatialObjectException(String.format("%s not contained by %s", spatialObject, space));
    }

    public SpatialObjectException(String message)
    {
        super(message);
    }

    public SpatialObjectException(Throwable throwable)
    {
        super(throwable);
    }
}
