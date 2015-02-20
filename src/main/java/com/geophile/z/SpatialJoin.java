/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.spatialjoin.SpatialJoinImpl;

import java.io.IOException;
import java.util.Iterator;

/**
 * <p>Provides the API for specifying a spatial join.
 * Spatial join results are accessed through an {@link java.util.Iterator}. For a many/many join,
 * the Iterator yields {@link com.geophile.z.Pair} objects, in which {@link com.geophile.z.Pair#left()}  and
 * {@link com.geophile.z.Pair#right()} provide access to the (possibly) overlapping objects. For a one/many join,
 * the Iterator returns {@link com.geophile.z.SpatialObject}s.
 * </p><p>
 * The spatial join algorithm
 * may return duplicate {@link com.geophile.z.Pair}s or {@link com.geophile.z.SpatialObject}s.
 * Duplicates.INCLUDE keeps these duplicates.
 * Duplicates.EXCLUDE suppresses duplicates, which is more convenient for applications, but
 * requires the storing of all returned results, and checking each
 * {@link com.geophile.z.Pair} to see whether it has already been returned.
 * </p><p>
 * A spatial join may yield false positives, which a Filter can remove.
 * </p>
 */

public abstract class SpatialJoin
{
    /**
     * Creates and configures a new SpatialJoin object.
     *
     * @param duplicates    Indicates whether the spatial join will suppress duplicates.
     * @param filter        Used to eliminate false positives from the spatial join output.
     * @param leftObserver  Used to monitor operations on the left input to the spatial join.
     * @param rightObserver Used to monitor operations on the right input to the spatial join.
     * @param <LEFT>        Type of object passed to the left filter argument.
     * @param <RIGHT>       TYpe of object passed to the right filter argument.
     * @return A configured SpatialJoin object. All spatial joins computed using it will use the configuration
     * specified by the above arguments.
     */
    public static <LEFT, RIGHT>
    SpatialJoin newSpatialJoin(Duplicates duplicates,
                               Filter<LEFT, RIGHT> filter,
                               InputObserver leftObserver,
                               InputObserver rightObserver)
    {
        return new SpatialJoinImpl(duplicates, filter, leftObserver, rightObserver);
    }

    /**
     * Creates and configures a new SpatialJoin object.
     *
     * @param duplicates Indicates whether the spatial join will suppress duplicates.
     * @param filter     Used to eliminate false positives from the spatial join output.
     * @param <LEFT>     Type of object passed to the left filter argument.
     * @param <RIGHT>    TYpe of object passed to the right filter argument.
     * @return A configured SpatialJoin object. All spatial joins computed using it will use the configuration
     * specified by the above arguments.
     */
    public static <LEFT, RIGHT>
    SpatialJoin newSpatialJoin(Duplicates duplicates,
                               Filter<LEFT, RIGHT> filter)
    {
        return new SpatialJoinImpl(duplicates, filter, null, null);
    }

    /**
     * Creates and configures a new SpatialJoin object.
     *
     * @param duplicates Indicates whether the spatial join will suppress duplicates.
     * @return A configured SpatialJoin object. All spatial joins computed using it will use the configuration
     * specified by the above arguments. False positives will be included in spatial join output.
     */
    public static SpatialJoin newSpatialJoin(Duplicates duplicates)
    {
        return new SpatialJoinImpl(duplicates, null, null, null);
    }

    /**
     * Returns an {@link java.util.Iterator} that will provide access to spatial join results.
     * The objects accessed through the {@link java.util.Iterator} are {@link com.geophile.z.Pair}s,
     * such that the left object comes from the leftSpatialIndex, and the right object comes from the rightSpatialIndex.
     * The results are not filtered, and so may contain false positives.
     *
     * @param <LEFT_RECORD>     Type of {@link com.geophile.z.Record} in leftSpatialIndex.
     * @param <RIGHT_RECORD>    Type of {@link com.geophile.z.Record} in rightSpatialIndex.
     * @param leftSpatialIndex  One spatial join input.
     * @param rightSpatialIndex The other spatial join input.
     * @return An {@link java.util.Iterator} providing access to spatial join results.
     */
    public abstract <LEFT_RECORD extends Record, RIGHT_RECORD extends Record>
    Iterator<Pair<LEFT_RECORD, RIGHT_RECORD>> iterator(SpatialIndex<LEFT_RECORD> leftSpatialIndex,
                                                       SpatialIndex<RIGHT_RECORD> rightSpatialIndex)
        throws IOException, InterruptedException;

    /**
     * Returns an {@link java.util.Iterator} that will provide access to spatial join results.
     * The objects accessed through the {@link java.util.Iterator} are {@link com.geophile.z.SpatialObject}s
     * from the data argument that overlap the given query object.
     * The results are filtered using the given filter, and should not contain false positives.
     *
     * @param <RECORD> Type of {@link com.geophile.z.Record} in data.
     * @param query    Used to locate data elements of interest.
     * @param data     The set of {@link com.geophile.z.SpatialObject}s to be searched.
     * @return An {@link java.util.Iterator} providing access to spatial join results.
     */
    public abstract <RECORD extends Record>
    Iterator<RECORD> iterator(SpatialObject query,
                              SpatialIndex<RECORD> data)
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

    /**
     * Used to monitor operations on a spatial join input.
     */
    public static class InputObserver
    {
        /**
         * Called when the input enters the given z-value.
         * @param z Z-value being entered.
         */
        public void enter(long z)
        {}

        /**
         * Called when the input exits the given z-value.
         * @param z Z-value being exited.
         */
        public void exit(long z)
        {}

        /**
         * Called when a random access to z has occurred on the given cursor. This method must not
         * cause the cursor state to be modified in any way.
         * @param cursor Cursor used to implement the random access.
         * @param z The z-value located by the random access.
         */
        public void randomAccess(Cursor cursor, long z)
        {}

        /**
         * Called when a sequential access has occurred on the given cursor.
         * This method must not cause the cursor state to be modified in any way.
         * @param cursor Cursor used to implement the sequential access.
         * @param zRandomAccess The z-value located by the random access that preceded the current
         *                      sequential access.
         * @param record The record located by this sequential access.
         */
        public void sequentialAccess(Cursor cursor, long zRandomAccess, Record record)
        {}

        /**
         * Called when an ancestor search is done (using SpatialJoinInput.findAncestorToResume).
         * @param cursor Cursor used for the ancestor search.
         * @param zStart The starting point of the ancestor search.
         * @param zAncestor The ancestor found, or SpaceImpl.Z_NULL if none found.
         */
        public void ancestorSearch(Cursor cursor, long zStart, long zAncestor)
        {}
    }

    /**
     * Used to remove false positives from spatial join output.
     *
     * @param <LEFT>  Type of object passed to the left filter argument.
     * @param <RIGHT> TYpe of object passed to the right filter argument.
     */
    public interface Filter<LEFT, RIGHT>
    {
        /**
         * Indicates whether the left and right objects overlap.
         *
         * @param left  Object from the left side of the spatial join.
         * @param right Object from the right side of the spatial join.
         * @return true if the objects overlap, false otherwise.
         */
        boolean overlap(LEFT left, RIGHT right);
    }
}
