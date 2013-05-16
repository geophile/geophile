/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.spatialobject.SpatialObject;

public interface SpatialJoinOutput<S extends SpatialObject, T extends SpatialObject>
{
    void add(S s, T t);
}
