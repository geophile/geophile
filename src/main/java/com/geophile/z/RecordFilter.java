/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

/**
 * Used to filter {@link com.geophile.z.Record}s in conjunction
 * with {@link com.geophile.z.SpatialIndex#remove(SpatialObject, RecordFilter)}.
 * @param <RECORD> Type of the Records to be filtered.
 */

public interface RecordFilter<RECORD extends Record>
{
    /**
     * Returns true iff the given Record is of interest, (i.e., should be removed).
     * @param record A candidate for removal.
     * @return true iff the given Record is of interest, (i.e., should be removed).
     */
    boolean select(RECORD record);
}
