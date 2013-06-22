/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.spatialjoin.SpatialJoinFilter;
import com.geophile.z.spatialjoin.SpatialJoinImpl;

import java.io.IOException;
import java.util.Iterator;

/**
 * A SpatialJoin object carries information associated with the computation of a spatial join.
 * A spatial join is performed in two steps:
 * <ol>
 * <li> Create a SpatialJoin object
 * <li> Invoke {@link SpatialJoin#iterator(SpatialIndex, SpatialIndex)}.
 * </ol>
 * The resulting {@link java.util.Iterator} provides access to spatial join results.
 */

public abstract class SpatialJoin
{
    /**
     * Create a new SpatialJoin object.
     * @param filter The spatial join algorithm returns some false positives, pairs of objects that don't actually
     *               overlap. The filter object is used to remove the false positives.
     * @param duplicates When both spatial indexes contain non-point objects, the spatial join algorithm
     *                   may return duplicate {@link com.geophile.z.Pair}s. Duplicates.INCLUDE keeps these duplicates.
     *                   Duplicates.EXCLUDE suppresses duplicates, which is more convenient for applications, but
     *                   requires the storing of all returned results, and checking each
     *                   {@link com.geophile.z.Pair} to see whether it has already been returned.
     *                   Duplicates are guaranteed not to occur in the following situations:
     *                   <ul>
     *                   <li> One or both of the spatial indexes being joined is empty or contains a single
     *                      spatial object.
     *                   <li> One or both of the spatial indexes being joined contains point objects, (occupying
     *                      a single grid cell of the Space).
     *                   </ul>
     * @return A SpatialJoin object that can be used for any number of possibly concurrent spatial joins, all
     * of which will use the given filter, and with the specified handling of duplicates.
     */
    public static SpatialJoin newSpatialJoin(SpatialJoinFilter filter, Duplicates duplicates)
    {
        return new SpatialJoinImpl(filter, duplicates);
    }

    /**
     * Return an {@link java.util.Iterator} that will provide access to spatial join results.
     * The objects accessed through the {@link java.util.Iterator} are {@link com.geophile.z.Pair}s,
     * such that the left object comes from the leftSpatialIndex, and the right object comes from the rightSpatialIndex.
     * @param leftSpatialIndex One spatial join input.
     * @param rightSpatialIndex The other spatial join input.
     * @return An {@link java.util.Iterator} providing access to spatial join results.
     */
    public abstract Iterator<Pair> iterator(SpatialIndex leftSpatialIndex, SpatialIndex rightSpatialIndex)
        throws IOException, InterruptedException;

    /**
     * Specifies duplicate-handling behavior for spatial joins.
     */
    public enum Duplicates
    {
        /**
         * Return duplicate {@link com.geophile.z.Pair}s found by the spatial join algorithm. This option is somewhat
         * faster and has a lower memory requirement, but may be less convenient for the application.
         */
        INCLUDE,

        /**
         * Suppress duplicate {@link com.geophile.z.Pair}s found by the spatial join algorithm. This option
         * is somewhat slower, and has a higher memory requirement, proportional to the number of
         * {@link com.geophile.z.Pair}s retrieved, but should be more convenient for the application.
         */
        EXCLUDE
    }
}
