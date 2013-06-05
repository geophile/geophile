/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.Pair;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.geophile.z.space.SpatialIndexImpl;

import java.io.IOException;
import java.util.Iterator;

public class SpatialJoinImpl<LEFT extends SpatialObject, RIGHT extends SpatialObject> extends SpatialJoin<LEFT, RIGHT>
{
    public SpatialJoinImpl(SpatialJoinFilter<LEFT, RIGHT> filter, Duplicates duplicates)
    {
        this.filter = filter;
        this.duplicates = duplicates;
    }

    public Iterator<Pair<LEFT, RIGHT>> iterator(SpatialIndex<LEFT> leftSpatialIndex,
                                                SpatialIndex<RIGHT> rightSpatialIndex)
        throws IOException, InterruptedException
    {
        Iterator<Pair<LEFT, RIGHT>> iterator =
            new SpatialJoinIterator<>((SpatialIndexImpl<LEFT>) leftSpatialIndex,
                                      (SpatialIndexImpl<RIGHT>) rightSpatialIndex,
                                      filter);
        if (duplicates == Duplicates.EXCLUDE) {
            iterator = new DuplicateEliminatingIterator<>(iterator);
        }
        return iterator;
    }

    public static boolean singleCellOptimization()
    {
        return Boolean.valueOf(System.getProperty(SINGLE_CELL_OPTIMIZATION_PROPERTY, "true"));
    }

    public static final String SINGLE_CELL_OPTIMIZATION_PROPERTY = "singlecellopt";

    private final SpatialJoinFilter<LEFT, RIGHT> filter;
    private final Duplicates duplicates;
}
