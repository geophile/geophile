/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.*;

import java.io.IOException;

public class KeyTemplateIndex extends Index
{
    // Index interface

    @Override
    public void add(Record record)
        throws IOException, InterruptedException, DuplicateRecordException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(long z, RecordFilter recordFilter) throws IOException, InterruptedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cursor cursor() throws IOException, InterruptedException
    {
        return index.cursor();
    }

    @Override
    public Record newRecord()
    {
        return index.newRecord();
    }

    @Override
    public Record newKeyRecord()
    {
        Record keyRecord = index.newRecord();
        keyTemplate.copyTo(keyRecord);
        return keyRecord;
    }

    @Override
    public boolean blindUpdates()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Index restrict(Record keyTemplate)
    {
        throw new UnsupportedOperationException();
    }

    // KeyTemplateIndex interface

    public KeyTemplateIndex(Index index, Record keyTemplate)
    {
        this.index = index;
        this.keyTemplate = keyTemplate;
    }

    // Object state

    private final Index index;
    private final Record keyTemplate;
}
