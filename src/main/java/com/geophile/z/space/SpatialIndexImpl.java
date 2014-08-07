/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.*;
import com.geophile.z.index.Cursor;
import com.geophile.z.index.Record;
import com.geophile.z.index.SpatialObjectKey;

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
            for (int i = 0; i < zs.length && zs[i] != -1L; i++) {
                LOG.log(Level.FINE, "    {0}", formatZ(zs[i]));
            }
        }
    }

    public boolean remove(SpatialObject spatialObject,
                          RecordFilter recordFilter) throws IOException, InterruptedException
    {
        long[] zs = decompose(spatialObject);
        int recordsDeleted = 0;
        Cursor cursor = null;
        for (int i = 0; i < zs.length; i++) {
            long z = zs[i];
            if (cursor == null) {
                cursor = index.cursor(z);
            } else {
                cursor.goTo(SpatialObjectKey.keyLowerBound(z));
            }
            boolean more = true;
            boolean found = false;
            while (more && !found) {
                Record record = cursor.next();
                if (record == null) {
                    more = false;
                } else {
                    if (record.key().z() == z) {
                        if (recordFilter.select(record)) {
                            found = true;
                        }
                    } else {
                        more = false;
                    }
                }
                if (found) {
                    cursor.deleteCurrent();
                    recordsDeleted++;
                }
            }
        }
        if (recordsDeleted > 0 && recordsDeleted < zs.length) {
            throw new SpatialIndex.Exception(String.format("Incomplete deletion of spatial object %s", spatialObject));
        }
        return recordsDeleted == zs.length;
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

    // For use by this package (testing)

    long firstUnreservedSoid()
    {
        return firstUnreservedSoid;
    }

    long firstUnreservedSoidStored() throws IOException, InterruptedException
    {
        Cursor cursor = index.cursor(SpatialIndexMetadata.SPATIAL_INDEX_METADATA_KEY.z());
        try {
            return firstUnreservedSoid(cursor.next());
        } finally {
            cursor.close();
        }
    }

    static long soidReservationBlockSize()
    {
        return Long.getLong(SOID_RESERVALTION_BLOCK_SIZE_PROPERTY, SOID_RESERVATION_BLOCK_SIZE);
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
        if (soid == firstUnreservedSoid) {
            reserveMoreSoids();
        }
        return soid;
    }

    private void restoreIdGenerator() throws IOException, InterruptedException
    {
        Cursor cursor = index.cursor(SpatialIndexMetadata.SPATIAL_INDEX_METADATA_KEY.z());
        try {
            firstUnreservedSoid = firstUnreservedSoid(cursor.next());
            idGenerator.set(firstUnreservedSoid);
            reserveMoreSoids();
        } finally {
            cursor.close();
        }
    }

    private void reserveMoreSoids() throws IOException, InterruptedException
    {
        long metadataZ = SpatialIndexMetadata.SPATIAL_INDEX_METADATA_KEY.z();
        long metadataSoid = SpatialIndexMetadata.SPATIAL_INDEX_METADATA_KEY.soid();
        index.remove(metadataZ, metadataSoid);
        firstUnreservedSoid += soidReservationBlockSize();
        index.add(metadataZ, new SpatialIndexMetadata(firstUnreservedSoid));
    }

    private long firstUnreservedSoid(Record metadataRecord)
    {
        long firstUnreservedSoid = 0;
        if (metadataRecord != null) {
            SpatialIndexMetadata metadata = (SpatialIndexMetadata) metadataRecord.spatialObject();
            firstUnreservedSoid = metadata.firstUnreservedSoid();
        }
        return firstUnreservedSoid;
    }

    // Class state

    private static final Logger LOG = Logger.getLogger(SpatialIndexImpl.class.getName());
    private static final long UNKNOWN = -2L;
    private static final long MISSING = -1L;
    private static final long SOID_RESERVATION_BLOCK_SIZE = 1_000_000L;
    static final String SOID_RESERVALTION_BLOCK_SIZE_PROPERTY = "soidReservationBlockSize";

    // Object state

    private final boolean singleCell;
    private final AtomicLong idGenerator = new AtomicLong();
    private volatile long firstUnreservedSoid;
}
