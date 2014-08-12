/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.Index;
import com.geophile.z.TestIndex;
import com.geophile.z.TestRecord;

public class SpatialIndexTest extends SpatialIndexTestBase
{
    @Override
    public Index<TestRecord> newIndex()
    {
        return new TestIndex();
    }
}
