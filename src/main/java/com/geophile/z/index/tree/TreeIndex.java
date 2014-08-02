/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.tree;

import com.geophile.z.DuplicateSpatialObjectException;
import com.geophile.z.Index;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.Cursor;
import com.geophile.z.index.Record;
import com.geophile.z.index.RecordImpl;
import com.geophile.z.index.SpatialObjectKey;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TreeIndex implements the {@link com.geophile.z.Index} interface in terms of a {@link java.util.TreeMap}.
 * A TreeIndex is not safe for use for simultaneous use by multiple threads.
 */

public class TreeIndex implements Index
{
    // Object interface

    @Override
    public String toString()
    {
        return name;
    }

    // Index interface

    @Override
    public boolean blindUpdates()
    {
        return false;
    }

    @Override
    public void add(long z, SpatialObject spatialObject)
    {
        SpatialObject replaced = tree.put(key(z, spatialObject.id()), spatialObject);
        if (replaced != null) {
            throw new DuplicateSpatialObjectException(replaced);
        }
    }

    @Override
    public boolean remove(long z, long soid)
    {
        boolean removed = false;
        Iterator<Map.Entry<SpatialObjectKey, SpatialObject>> zScan =
            tree.tailMap(key(z, soid)).entrySet().iterator();
        if (zScan.hasNext()) {
            Map.Entry<SpatialObjectKey, SpatialObject> entry = zScan.next();
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
    public Cursor cursor(long z)
    {
        return new TreeIndexCursor(this, key(z));
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

    @Override
    public Record newRecord()
    {
        return new RecordImpl();
    }

    // TreeIndex

    public TreeIndex()
    {}

    // For use by this package

    TreeMap<SpatialObjectKey, SpatialObject> tree()
    {
        return tree;
    }

    // Class state

    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    // Object state

    private final String name = String.format("TreeIndex(%s)", idGenerator.getAndIncrement());
    private final TreeMap<SpatialObjectKey, SpatialObject> tree = new TreeMap<>();
}
