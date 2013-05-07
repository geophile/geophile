/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.Pair;
import com.geophile.z.SpatialIndex;
import com.geophile.z.index.CursorIterator;
import com.geophile.z.index.Index;
import com.geophile.z.index.MultiCursor;
import com.geophile.z.spatialjoin.SpatialJoinIterator;
import com.geophile.z.spatialobject.SpatialObject;

import java.util.Iterator;

public class SpatialIndexImpl<SPATIAL_OBJECT extends SpatialObject> extends SpatialIndex<SPATIAL_OBJECT>
{
    // SpatialIndex interface

    public void add(SPATIAL_OBJECT spatialObject)
    {
        long[] zs = new long[space.dimensions() * 2];
        space.decompose(spatialObject, zs);
        int i = 0;
        while (zs[i] != -1L) {
            index.add(zs[i++], spatialObject);
        }
    }

    public void remove(SPATIAL_OBJECT spatialObject)
    {
        long[] zs = new long[space.dimensions() * 2];
        space.decompose(spatialObject, zs);
        int i = 0;
        while (zs[i] != -1L) {
            index.remove(zs[i++], spatialObject);
        }
    }

    public Iterator<SPATIAL_OBJECT> overlapping(SpatialObject query, Duplicates duplicates)
    {
        if (duplicates == Duplicates.EXCLUDE) {
            throw new UnsupportedOperationException();
        }
        long[] zs = new long[space.dimensions() * 2];
        space.decompose(query, zs);
        MultiCursor<SPATIAL_OBJECT> multiScan = new MultiCursor<>();
        int i = 0;
        while (i < zs.length && zs[i] != -1L) {
            multiScan.addInput(index.cursor(zs[i++]));
        }
        multiScan.start();
        return new CursorIterator<>(multiScan);
    }

    public <OTHER_SPATIAL_OBJECT extends SpatialObject>
        Iterator<Pair<SPATIAL_OBJECT, OTHER_SPATIAL_OBJECT>> join(SpatialIndex<OTHER_SPATIAL_OBJECT> that,
                                                                  Duplicates duplicates)
    {
        if (duplicates == Duplicates.EXCLUDE) {
            throw new UnsupportedOperationException();
        }
        return new SpatialJoinIterator<SPATIAL_OBJECT, OTHER_SPATIAL_OBJECT>(this, that, duplicates == Duplicates.EXCLUDE);
    }

    public Index<SPATIAL_OBJECT> index()
    {
        return index;
    }

    public SpatialIndexImpl(SpaceImpl space, Index<SPATIAL_OBJECT> index)
    {
        super(space, index);
    }
}
