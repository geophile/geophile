/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.Index;
import com.geophile.z.SpatialObjectSerializer;
import com.geophile.z.TestRecord;
import com.geophile.z.index.tree.TreeWithSerialization;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;

public class PersistentSpatialIndexTest extends SpatialIndexTestBase
{
    @Override
    public Index<TestRecord> newIndex()
    {
        SpatialObjectSerializer serializer = SpatialObjectSerializer.newSerializer();
        serializer.register(1, Point.class);
        serializer.register(2, Box.class);
        return new TreeWithSerialization(serializer);
    }
}
