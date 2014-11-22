/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.index.tree.TreeIndex;

public class TestIndex extends TreeIndex<TestRecord>
{
    @Override
    public TestRecord newRecord()
    {
        return new TestRecord();
    }

    public TestIndex()
    {
        super(TestRecord.COMPARATOR, true);
    }

    public TestIndex(boolean stableRecords)
    {
        super(TestRecord.COMPARATOR, stableRecords);
    }

    @Override
    public boolean blindUpdates()
    {
        return false;
    }

    @Override
    public boolean stableRecords()
    {
        return true;
    }
}
