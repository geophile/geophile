/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.*;
import com.geophile.z.index.RecordWithSpatialObject;
import com.geophile.z.index.sortedarray.SortedArray;
import com.geophile.z.space.SpatialIndexImpl;

import java.io.IOException;
import java.util.Iterator;

public class SpatialJoinImpl
{
    public static <LEFT_RECORD extends Record, RIGHT_RECORD extends Record>
    Iterator<Pair<LEFT_RECORD, RIGHT_RECORD>> iterator(SpatialIndex<LEFT_RECORD> leftSpatialIndex,
                                                       SpatialIndex<RIGHT_RECORD> rightSpatialIndex,
                                                       SpatialJoin.Duplicates duplicates)
        throws IOException, InterruptedException
    {
        return iterator(leftSpatialIndex, rightSpatialIndex, DEFAULT_FILTER, duplicates);
    }

    public static <LEFT_RECORD extends Record, RIGHT_RECORD extends Record>
    Iterator<Pair<LEFT_RECORD, RIGHT_RECORD>> iterator(SpatialIndex<LEFT_RECORD> leftSpatialIndex,
                                                       SpatialIndex<RIGHT_RECORD> rightSpatialIndex,
                                                       SpatialJoinFilter<LEFT_RECORD, RIGHT_RECORD> filter,
                                                       SpatialJoin.Duplicates duplicates)
        throws IOException, InterruptedException
    {
        if (!leftSpatialIndex.space().equals(rightSpatialIndex.space())) {
            throw new SpatialJoinException("Attempt to join spatial indexes with incompatible spaces");
        }
        Iterator iterator =
            SpatialJoinIterator.pairIterator((SpatialIndexImpl) leftSpatialIndex,
                                             (SpatialIndexImpl) rightSpatialIndex, filter);
        if (duplicates == SpatialJoin.Duplicates.EXCLUDE) {
            iterator = new DuplicateEliminatingIterator<Pair<LEFT_RECORD, RIGHT_RECORD>>(iterator);
        }
        return iterator;
    }

    public static <RECORD extends Record> Iterator<RECORD> iterator(SpatialObject query,
                                                                    SpatialIndex<RECORD> data,
                                                                    SpatialJoin.Duplicates duplicates)
        throws IOException, InterruptedException
    {
        return iterator(query, data, DEFAULT_FILTER, duplicates);
    }

    public static <RECORD extends Record> Iterator<RECORD> iterator(SpatialObject query,
                                                                    SpatialIndex<RECORD> data,
                                                                    SpatialJoinFilter<SpatialObject, RECORD> filter,
                                                                    SpatialJoin.Duplicates duplicates)
        throws IOException, InterruptedException
    {
        if (!query.containedBy(data.space())) {
            throw new SpatialJoinException("Query object not contained by data's space");
        }
        SortedArray<RecordWithSpatialObject> queryIndex = new SortedArray.OfBaseRecord();
        SpatialIndex<RecordWithSpatialObject> querySpatialIndex =
            SpatialIndex.newSpatialIndex(data.space(),
                                         queryIndex,
                                         query.maxZ() == 1
                                         ? SpatialIndex.Options.SINGLE_CELL
                                         : SpatialIndex.Options.DEFAULT);
        RecordWithSpatialObject queryRecord = queryIndex.newRecord();
        queryRecord.spatialObject(query);
        querySpatialIndex.add(query, queryRecord);
        Iterator iterator =
            SpatialJoinIterator.spatialObjectIterator(query,
                                                      (SpatialIndexImpl) data,
                                                      filter);
        if (duplicates == SpatialJoin.Duplicates.EXCLUDE) {
            iterator = new DuplicateEliminatingIterator<RECORD>(iterator);
        }
        return iterator;
    }

    private static SpatialJoinFilter DEFAULT_FILTER =
        new SpatialJoinFilter()
        {
            @Override
            public boolean overlap(Object r, Object s)
            {
                return true;
            }
        };
}
