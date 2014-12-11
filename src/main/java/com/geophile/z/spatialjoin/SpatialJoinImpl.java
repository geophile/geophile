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

import com.geophile.z.Pair;
import com.geophile.z.Record;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialJoinException;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.RecordWithSpatialObject;
import com.geophile.z.index.sortedarray.SortedArray;
import com.geophile.z.space.SpatialIndexImpl;

import java.io.IOException;
import java.util.Iterator;

public class SpatialJoinImpl extends SpatialJoin
{
    public SpatialJoinImpl(Duplicates duplicates,
                           Filter filter,
                           InputObserver leftObserver,
                           InputObserver rightObserver)
    {
        if (duplicates == null) {
            throw new IllegalArgumentException();
        }
        this.duplicates = duplicates;
        this.filter = filter == null ? DEFAULT_FILTER : filter;
        this.leftObserver = leftObserver;
        this.rightObserver = rightObserver;
    }

    @Override
    public <LEFT_RECORD extends Record, RIGHT_RECORD extends Record>
    Iterator<Pair<LEFT_RECORD, RIGHT_RECORD>> iterator(SpatialIndex<LEFT_RECORD> leftSpatialIndex,
                                                       SpatialIndex<RIGHT_RECORD> rightSpatialIndex)
        throws IOException, InterruptedException
    {
        if (!leftSpatialIndex.space().equals(rightSpatialIndex.space())) {
            throw new SpatialJoinException("Attempt to join spatial indexes with incompatible spaces");
        }
        Iterator iterator =
            SpatialJoinIterator.pairIterator((SpatialIndexImpl) leftSpatialIndex,
                                             (SpatialIndexImpl) rightSpatialIndex,
                                             filter,
                                             leftObserver,
                                             rightObserver);
        if (duplicates == SpatialJoin.Duplicates.EXCLUDE) {
            iterator = new DuplicateEliminatingIterator<Pair<LEFT_RECORD, RIGHT_RECORD>>(iterator);
        }
        return iterator;
    }

    @Override
    public <RECORD extends Record>
    Iterator<RECORD> iterator(final SpatialObject query,
                              SpatialIndex<RECORD> data)
        throws IOException, InterruptedException
    {
        final SortedArray<RecordWithSpatialObject> queryIndex = new SortedArray.OfBaseRecord();
        SpatialIndex<RecordWithSpatialObject> querySpatialIndex =
            SpatialIndex.newSpatialIndex(data.space(),
                                         queryIndex,
                                         query.maxZ() == 1
                                         ? SpatialIndex.Options.SINGLE_CELL
                                         : SpatialIndex.Options.DEFAULT);
        querySpatialIndex.add(query,
                              new Record.Factory<RecordWithSpatialObject>()
                              {
                                  @Override
                                  public RecordWithSpatialObject newRecord()
                                  {
                                      RecordWithSpatialObject queryRecord = queryIndex.newRecord();
                                      queryRecord.spatialObject(query);
                                      return queryRecord;
                                  }
                              });
        Iterator<RECORD> iterator =
            (Iterator<RECORD>) SpatialJoinIterator.spatialObjectIterator(query,
                                                                         (SpatialIndexImpl) data,
                                                                         filter,
                                                                         leftObserver,
                                                                         rightObserver);
        if (duplicates == SpatialJoin.Duplicates.EXCLUDE) {
            iterator = new DuplicateEliminatingIterator<>(iterator);
        }
        return iterator;
    }

    public static boolean singleCellOptimization()
    {
        return Boolean.valueOf(System.getProperty(SINGLE_CELL_OPTIMIZATION_PROPERTY, "true"));
    }

    private static SpatialJoin.Filter DEFAULT_FILTER =
        new SpatialJoin.Filter()
        {
            @Override
            public boolean overlap(Object r, Object s)
            {
                return true;
            }
        };

    private static final String SINGLE_CELL_OPTIMIZATION_PROPERTY = "singlecellopt";

    private final Duplicates duplicates;
    private final Filter filter;
    private final InputObserver leftObserver;
    private final InputObserver rightObserver;
}
