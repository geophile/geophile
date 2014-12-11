/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.util.MicroBenchmark;
import com.geophile.z.Record;
import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.TestIndex;
import com.geophile.z.TestRecord;
import com.geophile.z.spatialobject.d2.Point;

import java.io.IOException;

public class RemovePerformance extends MicroBenchmark
{
    public static void main(String[] args) throws Exception
    {
        for (copies = 1000; copies <= 10000; copies += 1000) {
            double nsec = new RemovePerformance().run();
            System.out.format("Removal of %d copies: %f msec\n", copies, nsec / 1_000_000);
        }
    }

    @Override
    public void beforeAction() throws IOException, InterruptedException
    {
        loadCopies();
    }

    @Override
    public Object action() throws Exception
    {
        TestFilter filter = new TestFilter();
        // Worst case: visit ids in reverse order
        for (int id = copies - 1; id >= 0; id--) {
            filter.soid = id;
            spatialIndex.remove(TEST_POINT, filter);
        }
        return null;
    }

    private RemovePerformance()
    {
        super(10, 0.10);
    }

    private void loadCopies() throws IOException, InterruptedException
    {
        spatialIndex = SpatialIndex.newSpatialIndex(SPACE, new TestIndex(), SpatialIndex.Options.SINGLE_CELL);
        TestRecord.Factory recordFactory = new TestRecord.Factory();
        for (int id = 0; id < copies; id++) {
            spatialIndex.add(TEST_POINT, recordFactory.setup(TEST_POINT, id));
        }
    }

    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{NX, NY},
                                                      new int[]{X_BITS, Y_BITS});
    private static final Point TEST_POINT = new Point(123, 456);
    private static int copies;

    private SpatialIndex<TestRecord> spatialIndex;

    private static class TestFilter implements Record.Filter<TestRecord>
    {
        @Override
        public boolean select(TestRecord record)
        {
            return record.soid() == soid;
        }

        public int soid;
    }
}