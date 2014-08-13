/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.examples;

import com.geophile.z.index.tree.TreeIndex;

public class TestIndex extends TreeIndex<Record>
{
    @Override
    public Record newRecord()
    {
        return new Record();
    }

    public TestIndex()
    {
        super(Record.COMPARATOR);
    }
}
