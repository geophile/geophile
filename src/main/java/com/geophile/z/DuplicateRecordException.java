/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

/**
 * Indicates an attempt to add a duplicate {@link com.geophile.z.Record} to a
 * {@link com.geophile.z.SpatialIndex}.
 */

public class DuplicateRecordException extends SpatialObjectException
{
    public DuplicateRecordException(Record record)
    {
        super(record.toString());
    }
}
