/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.Index;
import com.geophile.z.SingleCellException;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.Cursor;
import com.geophile.z.index.Record;
import com.geophile.z.index.SpatialObjectKey;
import com.geophile.z.spatialobject.SpatialObjectIdState;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.geophile.z.space.SpaceImpl.formatZ;

public class SpatialIndexImpl extends SpatialIndex
{
    // Object interface

    @Override
    public String toString()
    {
        return index.toString();
    }

    // SpatialIndex interface

    public void add(SpatialObject spatialObject) throws IOException, InterruptedException
    {
        spatialObject.id(nextSoid());
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

    public boolean remove(SpatialObject spatialObject) throws IOException, InterruptedException
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
        long soid = soid(zSmallest, spatialObject);
        if (found = soid != MISSING) {
            // Remove everything else with the same soid.
            for (int i = 0; i < zs.length && zs[i] != -1L; i++) {
                long z = zs[i];
                boolean removed = index.remove(z, soid);
                assert removed;
            }
        }
        return found;
    }

    public boolean singleCell()
    {
        return singleCell;
    }

    public Index index()
    {
        return index;
    }

    public SpatialIndexImpl(SpaceImpl space, Index index, Options options)
        throws IOException, InterruptedException
    {
        super(space, index, options);
        singleCell = options == Options.SINGLE_CELL;
        restoreIdGenerator();
    }

    // For use by this class

    private long[] decompose(SpatialObject spatialObject)
    {
        int maxZ = spatialObject.maxZ();
        if (singleCell && maxZ > 1) {
            throw new SingleCellException(spatialObject);
        }
        long[] zs = new long[maxZ];
        space.decompose(spatialObject, zs);
        return zs;
    }

    private long soid(long z, SpatialObject spatialObject) throws IOException, InterruptedException
    {
        long soid = UNKNOWN;
        Cursor cursor = index.cursor(z);
        while (soid == UNKNOWN) {
            Record record = cursor.next();
            if (record != null) {
                SpatialObjectKey key = record.key();
                if (key.z() == z) {
                    if (record.spatialObject().equalTo(spatialObject)) {
                        soid = record.spatialObject().id();
                    }
                } else {
                    soid = MISSING;
                }
            } else {
                soid = MISSING;
            }
        }
        return soid;
    }

    private long nextSoid() throws IOException, InterruptedException
    {
        long soid = idGenerator.getAndIncrement();
        if (soid == maxReservedSoid) {
            updateMaxReservedSoid();
        }
        return soid;
    }

    private void restoreIdGenerator() throws IOException, InterruptedException
    {
        Cursor cursor = index.cursor(SpatialObjectIdState.Z_MAX_RESERVED);
        try {
            Record record = cursor.next();
            maxReservedSoid = record.eof() ? 0 : record.key().soid();
            idGenerator.set(maxReservedSoid + 1);
            updateMaxReservedSoid();
        } finally {
            cursor.close();
        }
    }

    private void updateMaxReservedSoid() throws IOException, InterruptedException
    {
        index.remove(SpatialObjectIdState.Z_MAX_RESERVED, maxReservedSoid);
        maxReservedSoid += SOID_RESERVATION_BLOCK_SIZE;
        index.add(SpatialObjectIdState.Z_MAX_RESERVED, new SpatialObjectIdState(maxReservedSoid));
    }

    // Class state

    private static final Logger LOG = Logger.getLogger(SpatialIndexImpl.class.getName());
    private static final long UNKNOWN = -2L;
    private static final long MISSING = -1L;
    static final long SOID_RESERVATION_BLOCK_SIZE = 1_000_000L;

    // Object state

    private final boolean singleCell;
    private final AtomicLong idGenerator = new AtomicLong();
    private volatile long maxReservedSoid;
}
