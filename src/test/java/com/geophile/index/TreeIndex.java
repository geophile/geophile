/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.index;

import com.geophile.Space;
import com.geophile.spatialobject.SpatialObject;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class TreeIndex<SPATIAL_OBJECT extends SpatialObject> implements Index<SPATIAL_OBJECT>
{
    // Index interface

    @Override
    public void add(long z, SPATIAL_OBJECT spatialObject)
    {
        tree.put(z, spatialObject);
    }

    @Override
    public void remove(long z, SPATIAL_OBJECT spatialObject)
    {
        Iterator<Map.Entry<Long, SPATIAL_OBJECT>> zScan = tree.tailMap(z).entrySet().iterator();
        Map.Entry<Long, SPATIAL_OBJECT> entry;
        while (zScan.hasNext() && (entry = zScan.next()) != null) {
            if (entry.getValue().equals(spatialObject)) {
                zScan.remove();
            }
        }
    }

    @Override
    public Cursor<SPATIAL_OBJECT> cursor(long z)
    {
        return new TreeIndexCursor<>(space, tree, z);
    }

    // TreeIndex

    public TreeIndex(Space space)
    {
        this.space = space;
    }

    // Object state

    private final Space space;
    private final TreeMap<Long, SPATIAL_OBJECT> tree = new TreeMap<>();
}
