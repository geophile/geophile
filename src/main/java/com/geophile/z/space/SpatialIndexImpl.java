/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.Pair;
import com.geophile.z.SpatialIndex;
import com.geophile.z.index.Index;
import com.geophile.z.spatialjoin.SpatialJoinIterator;
import com.geophile.z.spatialobject.SpatialObject;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.geophile.z.space.SpaceImpl.formatZ;

public class SpatialIndexImpl<SPATIAL_OBJECT extends SpatialObject> extends SpatialIndex<SPATIAL_OBJECT>
{
    // Object interface

    @Override
    public String toString()
    {
        return index.toString();
    }

    // SpatialIndex interface

    public void add(SPATIAL_OBJECT spatialObject)
    {
        long[] zs = decompose(spatialObject);
        for (int i = 0; i < zs.length && zs[i] != -1L; i++) {
            index.add(zs[i], spatialObject);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "add {0}", spatialObject);
            for (int i = 0;i < zs.length && zs[i] != -1L; i++) {
                LOG.log(Level.FINE, "    {0}", formatZ(zs[i]));
            }
        }
    }

    public boolean remove(SPATIAL_OBJECT spatialObject)
    {
        boolean found;
        long[] zs = decompose(spatialObject);
        // Find smallest z and remove it first. The first removal, which has to search for a spatial object id,
        // should be the slowest step, and searching in the smallest z should minimize the number of spatial objects
        // that need to be examined.
        long zSmallest = zs[0];
        for (int i = 1; i < zs.length && zs[i] != -1L; i++) {
            long z = zs[i];
            if (SpaceImpl.length(z) > SpaceImpl.length(zSmallest)) {
                zSmallest = z;
            }
        }
        // Do the first removal, getting the spatial object id.
        long soid = index.remove(zSmallest, spatialObject);
        if (found = soid != -1L) {
            // Remove everything else with the same soid.
            for (int i = 1; i < zs.length && zs[i] != -1L; i++) {
                long z = zs[i];
                if (z != zSmallest) {
                    boolean removed = index.remove(z, soid);
                    assert removed;
                }
            }
        }
        return found;
    }

    public <OTHER_SPATIAL_OBJECT extends SpatialObject>
        Iterator<Pair<SPATIAL_OBJECT, OTHER_SPATIAL_OBJECT>> join(SpatialIndex<OTHER_SPATIAL_OBJECT> that,
                                                                  Duplicates duplicates)
    {
        if (duplicates == Duplicates.EXCLUDE) {
            throw new UnsupportedOperationException();
        }
        return new SpatialJoinIterator<>(this, that, duplicates == Duplicates.EXCLUDE);
    }

    public Index<SPATIAL_OBJECT> index()
    {
        return index;
    }

    public SpatialIndexImpl(SpaceImpl space, Index<SPATIAL_OBJECT> index)
    {
        super(space, index);
    }

    // For use by this class

    private long[] decompose(SpatialObject spatialObject)
    {
        int maxZs = space.dimensions() * 2;
        long[] zs = new long[maxZs];
        space.decompose(spatialObject, zs);
        return zs;
    }

    // Class state

    private static final Logger LOG = Logger.getLogger(SpatialIndexImpl.class.getName());
}
