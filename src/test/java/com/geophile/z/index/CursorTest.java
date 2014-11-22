/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.Cursor;
import com.geophile.z.Index;
import com.geophile.z.Record;
import com.geophile.z.TestIndex;
import com.geophile.z.TestRecord;
import com.geophile.z.spatialobject.d2.Point;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class CursorTest
{
    @Test
    public void testCursor() throws IOException, InterruptedException
    {
        for (int n = 0; n <= N_MAX; n++) {
            testCursor(testIndex(n), n);
        }
    }

    private void testCursor(Index<TestRecord> index, int n) throws IOException, InterruptedException
    {
        Cursor<TestRecord> cursor;
        int expectedKey;
        int expectedLastKey;
        boolean expectedEmpty;
        TestRecord record;
        // Full cursor
        // debug("Full cursor %s\n", n);
        {
            cursor = newCursor(index, 0);
            expectedKey = 0;
            while ((record = cursor.next()) != null) {
                assertEquals(expectedKey, key(record));
                expectedKey += GAP;
            }
            assertEquals(n * GAP, expectedKey);
        }
        // Try scans starting at, before, and after each key and ending at, before and after each key.
        // debug("Scan from key %s\n", n);
        {
            for (int i = 0; i < n; i++) {
                int startBase = GAP * i;
                int endBase = GAP * (n - 1 - i);
                for (long start = startBase - 1; start <= startBase + 1; start++) {
                    for (long end = endBase - 1; end <= endBase + 1; end++) {
                        if (start <= end) {
                            // debug("start: %s, end: %s", start, end);
                            cursor = newCursor(index, start);
                            expectedKey = start <= startBase ? startBase : startBase + GAP;
                            expectedLastKey = end >= endBase ? endBase : endBase - GAP;
                            expectedEmpty = start > end || start <= end && (end >= startBase || start <= endBase);
                            boolean empty = true;
                            while ((record = cursor.next()) != null &&
                                   record.z() <= end) {
                                // debug("    %s", entry.getKey());
                                assertEquals(expectedKey, key(record));
                                expectedKey += GAP;
                                empty = false;
                            }
                            if (empty) {
                                assertTrue(expectedEmpty);
                            } else {
                                assertEquals(expectedLastKey + GAP, expectedKey);
                            }
                        }
                    }
                }
            }
        }
        // goTo
        // debug("goTo %s\n", n);
        if (n > 0) {
            cursor = newCursor(index, 0);
            int match;
            int before;
            for (int i = 0; i <= n; i++) {
                // debug("n: %s, i: %s", n, i);
                match = i * GAP;
                if (i < n) {
                    // Match, next
                    cursor.goTo(key(index, match));
                    assertEquals(match, key(cursor.next()));
                }
                // Before, next
                before = match - GAP / 2;
                cursor.goTo(key(index, before));
                if (i == n) {
                    assertTrue(cursor.next() == null);
                } else {
                    assertEquals(match, key(cursor.next()));
                }
            }
        }
    }

    private Index<TestRecord> testIndex(int n) throws IOException, InterruptedException
    {
        Index<TestRecord> index = new TestIndex();
        assertTrue(GAP > 1);
        // Populate map with keys 0, GAP, ..., GAP * (n - 1)
        for (int i = 0; i < n; i++) {
            long key = GAP * i;
            TestRecord record = index.newRecord();
            record.z(key);
            record.spatialObject(new Point(key, key));
            index.add(record);
        }
        return index;
    }

    private Cursor<TestRecord> newCursor(Index<TestRecord> index, long z) throws IOException, InterruptedException
    {
        Cursor<TestRecord> cursor = index.cursor();
        TestRecord key= index.newKeyRecord();
        key.z(z);
        cursor.goTo(key);
        return cursor;
    }

    private long key(Record record)
    {
        return record.z();
    }

    private TestRecord key(Index<TestRecord> index, long z)
    {
        TestRecord key = index.newKeyRecord();
        key.z(z);
        return key;
    }

    private void debug(String template, Object ... args)
    {
        System.out.println(String.format(template, args));
    }

    private static final int N_MAX = 100;
    private static final int GAP = 10;
}
