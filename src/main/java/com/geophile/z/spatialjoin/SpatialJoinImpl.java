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

public class SpatialJoinImpl extends SpatialJoin
{
    public SpatialJoinImpl(SpatialJoinFilter filter, Duplicates duplicates)
    {
        super(filter, duplicates);
    }

    public Iterator<Pair> iterator(SpatialIndex leftSpatialIndex, SpatialIndex rightSpatialIndex)
        throws IOException, InterruptedException
    {
        Iterator<Pair> iterator =
            new SpatialJoinIterator((SpatialIndexImpl) leftSpatialIndex, (SpatialIndexImpl) rightSpatialIndex,
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
}
