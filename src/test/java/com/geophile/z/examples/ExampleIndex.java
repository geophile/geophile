/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.examples;

import com.geophile.z.index.tree.TreeIndex;

public class ExampleIndex extends TreeIndex<ExampleRecord>
{
    @Override
    public ExampleRecord newRecord()
    {
        return new ExampleRecord();
    }

    public ExampleIndex()
    {
        super(ExampleRecord.COMPARATOR, true);
    }
}
