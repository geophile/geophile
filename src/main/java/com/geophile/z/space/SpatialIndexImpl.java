/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.geophile.z.space.SpaceImpl.formatZ;

public class SpatialIndexImpl<RECORD extends Record> extends SpatialIndex<RECORD>
{
    // Object interface

    @Override
    public String toString()
    {
        return index.toString();
    }

    // SpatialIndex interface

    public void add(RECORD record) throws IOException, InterruptedException
    {
        SpatialObject spatialObject = record.spatialObject();
        long[] zs = decompose(spatialObject);
        for (int i = 0; i < zs.length && zs[i] != -1L; i++) {
            record.z(zs[i]);
            index.add(record);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "add {0}", spatialObject);
            for (int i = 0; i < zs.length && zs[i] != -1L; i++) {
                LOG.log(Level.FINE, "    {0}", formatZ(zs[i]));
            }
        }
    }

    public boolean remove(SpatialObject spatialObject,
                          RecordFilter<RECORD> recordFilter) throws IOException, InterruptedException
    {
        long[] zs = decompose(spatialObject);
        int recordsDeleted = 0;
        int zCount = 0;
        Cursor<RECORD> cursor = index.cursor();
        RECORD key = index.newKeyRecord();
        for (int i = 0; i < zs.length; i++) {
            long z = zs[i];
            if (z != Space.Z_NULL) {
                key.z(z);
                cursor.goTo(key);
                boolean more = true;
                boolean found = false;
                while (more && !found) {
                    RECORD record = cursor.next();
                    if (record == null) {
                        more = false;
                    } else {
                        if (record.z() == z) {
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
                zCount++;
            }
        }
        if (recordsDeleted > 0 && recordsDeleted < zCount) {
            throw new SpatialIndex.Exception(String.format("Incomplete deletion of spatial object %s", spatialObject));
        }
        return recordsDeleted == zCount;
    }

    public boolean singleCell()
    {
        return singleCell;
    }

    public Index index()
    {
        return index;
    }

    public SpatialIndexImpl(SpaceImpl space, Index<RECORD> index, Options options)
        throws IOException, InterruptedException
    {
        super(space, index, options);
        singleCell = options == Options.SINGLE_CELL;
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

    // Class state

    private static final Logger LOG = Logger.getLogger(SpatialIndexImpl.class.getName());

    // Object state

    private final boolean singleCell;
}
