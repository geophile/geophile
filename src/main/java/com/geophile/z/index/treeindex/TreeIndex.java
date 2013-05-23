/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.treeindex;

import com.geophile.z.DuplicateSpatialObjectException;
import com.geophile.z.Index;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.Cursor;
import com.geophile.z.index.SpatialObjectKey;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TreeIndex implements the {@link com.geophile.z.Index} interface in terms of a {@link java.util.TreeMap}.
 * @param <SPATIAL_OBJECT>
 */

public class TreeIndex<SPATIAL_OBJECT extends SpatialObject> implements Index<SPATIAL_OBJECT>
{
    // Object interface

    @Override
    public String toString()
    {
        return name;
    }

    // Index interface

    @Override
    public void add(long z, SPATIAL_OBJECT spatialObject)
    {
        SPATIAL_OBJECT replaced = tree.put(key(z, spatialObject.id()), spatialObject);
        // Checking replaced is not a foolproof way to catch duplicates. If two threads concurrently
        // add spatial objects that have a common z-value and the same id, neither may see the other's insert.
        // (The exact behavior depends on how the underlying index does concurrency control.)
        if (replaced != null) {
            throw new DuplicateSpatialObjectException();
        }
    }

    @Override
    public long remove(long z, SPATIAL_OBJECT spatialObject)
    {
        long removedId = -1L;
        Iterator<Map.Entry<SpatialObjectKey, SPATIAL_OBJECT>> zScan =
            tree.tailMap(key(z)).entrySet().iterator();
        boolean done = false;
        while (!done) {
            if (zScan.hasNext()) {
                Map.Entry<SpatialObjectKey, SPATIAL_OBJECT> entry = zScan.next();
                SpatialObjectKey key = entry.getKey();
                if (key.z() == z) {
                    if (entry.getValue().equalTo(spatialObject)) {
                        removedId = entry.getValue().id();
                        zScan.remove();
                        done = true;
                    }
                } else {
                    done = true;
                }
            } else {
                done = true;
            }
        }
        return removedId;
    }

    @Override
    public boolean remove(long z, long soid)
    {
        boolean removed = false;
        Iterator<Map.Entry<SpatialObjectKey, SPATIAL_OBJECT>> zScan =
            tree.tailMap(key(z, soid)).entrySet().iterator();
        if (zScan.hasNext()) {
            Map.Entry<SpatialObjectKey, SPATIAL_OBJECT> entry = zScan.next();
            if (entry.getKey().z() == z) {
                SpatialObjectKey key = entry.getKey();
                assert key.z() == z : key;
                if (key.soid() == soid) {
                    zScan.remove();
                    removed = true;
                }
            }
        }
        return removed;
    }

    @Override
    public Cursor<SPATIAL_OBJECT> cursor(long z)
    {
        return new TreeIndexCursor<>(tree, key(z));
    }

    @Override
    public SpatialObjectKey key(long z)
    {
        return SpatialObjectKey.keyLowerBound(z);
    }

    @Override
    public SpatialObjectKey key(long z, long soid)
    {
        return SpatialObjectKey.key(z, soid);
    }

    // TreeIndex

    public TreeIndex()
    {}

    // Class state

    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    // Object state

    private final String name = String.format("TreeIndex(%s)", idGenerator.getAndIncrement());
    private final TreeMap<SpatialObjectKey, SPATIAL_OBJECT> tree = new TreeMap<>();
}
