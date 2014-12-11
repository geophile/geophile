/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.Index;
import com.geophile.z.Pair;
import com.geophile.z.Record;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.geophile.z.SpatialObjectSerializer;
import com.geophile.z.TestRecord;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class SpatialIndexTestBase
{
    @BeforeClass
    public static void beforeClass() throws Exception
    {
        SERIALIZER.register(1, Point.class);
        SERIALIZER.register(2, Box.class);
    }

    // Like TreeIndexTest.testRetrieval, but written in terms of SpatialIndex
    @Test
    public void testRetrieval() throws Exception
    {
        Index<TestRecord> index = newIndex();
        SpatialIndexImpl<TestRecord> spatialIndex =
            new SpatialIndexImpl<>(SPACE, index, SpatialIndex.Options.DEFAULT);
        int id = 0;
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                Point point = new Point(x, y);
                spatialIndex.add(point, RECORD_FACTORY.setup(point, id++));
            }
        }
        commitTransaction();
/*
        new IndexDumper<TestRecord>(index)
        {
            @Override
            public String describe(TestRecord record)
            {
                return String.format("(0x%016x, %s, %s)", record.z(), record.soid(), record.spatialObject());
            }
        }.dump(System.out);
*/
        Random random = new Random(SEED);
        for (int i = 0; i < 1000; i++) {
            generateRandomBox(random);
            test(spatialIndex,
                 xLo, xHi, yLo, yHi,
                 new Filter()
                 {
                     @Override
                     public boolean keep(SpatialObject spatialObject)
                     {
                         return true;
                     }
                 });
        }
    }

    @Test
    public void testRemoveAll() throws Exception
    {
        Index<TestRecord> index = newIndex();
        SpatialIndexImpl<TestRecord> spatialIndex = new SpatialIndexImpl<>(SPACE, index, SpatialIndex.Options.DEFAULT);
        int id = 0;
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                Point point = new Point(x, y);
                spatialIndex.add(point, RECORD_FACTORY.setup(point, id++));
            }
        }
        commitTransaction();
        // Remove everything
        RemovalFilter removalFilter = new RemovalFilter();
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                Point point = new Point(x, y);
                removalFilter.spatialObject(point);
                spatialIndex.remove(point, removalFilter);
            }
        }
        commitTransaction();
        Random random = new Random(SEED);
        for (int i = 0; i < 1000; i++) {
            generateRandomBox(random);
            test(spatialIndex,
                 xLo, xHi, yLo, yHi,
                 new Filter()
                 {
                     @Override
                     public boolean keep(SpatialObject spatialObject)
                     {
                         return false;
                     }
                 });
        }
    }

    @Test
    public void testRemoveSome() throws Exception
    {
        Index<TestRecord> index = newIndex();
        SpatialIndexImpl<TestRecord> spatialIndex = new SpatialIndexImpl<>(SPACE, index, SpatialIndex.Options.DEFAULT);
        int id = 0;
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                Point point = new Point(x, y);
                spatialIndex.add(point, RECORD_FACTORY.setup(point, id++));
            }
        }
        commitTransaction();
        // Remove (x, y), for odd x/10 and even y/10
        RemovalFilter removalFilter = new RemovalFilter();
        for (long x = 0; x < X_MAX; x += 10) {
            if ((x / 10) % 2 == 1) {
                for (long y = 0; y < Y_MAX; y += 10) {
                    if ((y / 10) % 2 == 0) {
                        Point point = new Point(x, y);
                        removalFilter.spatialObject(point);
                        boolean removed = spatialIndex.remove(point, removalFilter);
                        assertTrue(removed);
                    }
                }
            }
        }
        commitTransaction();
        Random random = new Random(SEED);
        int xLo;
        int xHi;
        int yLo;
        int yHi;
        for (int i = 0; i < 1000; i++) {
            do {
                xLo = random.nextInt(X_MAX);
                xHi = xLo + random.nextInt(X_MAX - xLo);
            } while (xHi <= xLo);
            do {
                yLo = random.nextInt(Y_MAX);
                yHi = random.nextInt(Y_MAX - yLo);
            } while (yHi <= yLo);
            test(spatialIndex,
                 xLo, xHi, yLo, yHi,
                 new Filter()
                 {
                     @Override
                     public boolean keep(SpatialObject spatialObject)
                     {
                         Point point = (Point) spatialObject;
                         return !((point.x() / 10) % 2 == 1 && (point.y() / 10) % 2 == 0);
                     }
                 });
        }
    }

    @Test
    public void testRemovalVsDuplicates() throws Exception
    {
        final int COPIES = 10;
        TestRecord[] records = new TestRecord[COPIES];
        Index<TestRecord> index = newIndex();
        SpatialIndexImpl<TestRecord> spatialIndex = new SpatialIndexImpl<>(SPACE, index, SpatialIndex.Options.DEFAULT);
        Box box = new Box(250, 750, 250, 750);
        for (int c = 0; c < COPIES; c++) {
            spatialIndex.add(box, RECORD_FACTORY.setup(box, c));
            records[c] = RECORD_FACTORY.newRecord();
        }
        // Generate a permutation of records
        Random random = new Random(123456);
        if (COPIES > 1) {
            for (int i = 0; i < 100; i++) {
                int a = random.nextInt(COPIES);
                int b;
                do {
                    b = random.nextInt(COPIES);
                } while (a == b);
                TestRecord recordA = records[a];
                records[a] = records[b];
                records[b] = recordA;
            }
        }
        for (int c = 0; c < COPIES; c++) {
            final TestRecord victim = records[c];
            Record.Filter<TestRecord> filter = new Record.Filter<TestRecord>()
            {
                @Override
                public boolean select(TestRecord record)
                {
                    return record.soid() == victim.soid();
                }
            };
            assertTrue(spatialIndex.remove(box, filter));
            // Try it again, to make sure the removal doesn't happen
            assertTrue(!spatialIndex.remove(box, filter));
        }
        commitTransaction();
    }

    public abstract Index<TestRecord> newIndex() throws Exception;

    public void commitTransaction() throws Exception
    {
    }

    private void test(SpatialIndexImpl<TestRecord> spatialIndex,
                      int xLo, int xHi, int yLo, int yHi,
                      Filter filter) throws Exception
    {
        Box box = new Box(xLo, xHi, yLo, yHi);
        Index<TestRecord> index = newIndex();
        SpatialIndex<TestRecord> query = new SpatialIndexImpl<>(SPACE, index, SpatialIndex.Options.DEFAULT);
        query.add(box, RECORD_FACTORY.setup(box, 0));
        Iterator<Pair<TestRecord, TestRecord>> iterator =
            SpatialJoin.newSpatialJoin(SpatialJoin.Duplicates.INCLUDE, FILTER).iterator(query, spatialIndex);
        List<Point> actual = new ArrayList<>();
        while (iterator.hasNext()) {
            Point point = (Point) iterator.next().right().spatialObject();
            if (!actual.contains(point)) {
                actual.add(point);
            }
        }
        List<Point> expected = new ArrayList<>();
        for (long x = 10 * ((xLo + 9) / 10); x <= 10 * (xHi / 10); x += 10) {
            for (long y = 10 * ((yLo + 9) / 10); y <= 10 * (yHi / 10); y += 10) {
                Point point = new Point(x, y);
                if (filter.keep(point)) {
                    expected.add(point);
                }
            }
        }
        Collections.sort(actual, POINT_RANKING);
        Collections.sort(expected, POINT_RANKING);
        assertEquals(expected, actual);
    }

    private void generateRandomBox(Random random)
    {
        do {
            xLo = random.nextInt(X_MAX);
            xHi = xLo + random.nextInt(X_MAX - xLo);
        } while (xHi <= xLo);
        do {
            yLo = random.nextInt(Y_MAX);
            yHi = yLo + random.nextInt(Y_MAX - yLo);
        } while (yHi <= yLo);
    }

    private static final int SEED = 123456;
    private static final int X_MAX = 1000;
    private static final int Y_MAX = 1000;
    private static final Comparator<Point> POINT_RANKING =
        new Comparator<Point>()
        {
            @Override
            public int compare(Point p, Point q)
            {
                int c = p.x() < q.x() ? -1 : p.x() > q.x() ? 1 : 0;
                if (c == 0) {
                    c = p.y() < q.y() ? -1 : p.y() > q.y() ? 1 : 0;
                }
                return c;
            }
        };
    private static final SpaceImpl SPACE = new SpaceImpl(new double[]{0, 0}, new double[]{1000, 1000}, new int[]{10, 10}, null);
    private static final TestRecord.Factory RECORD_FACTORY = new TestRecord.Factory();
    private static final SpatialJoin.Filter<TestRecord, TestRecord> FILTER =
        new SpatialJoin.Filter<TestRecord, TestRecord>()
        {
            @Override
            public boolean overlap(TestRecord r, TestRecord s)
            {
                Box b = (Box) r.spatialObject();
                Point p = (Point) s.spatialObject();
                return
                    b.xLo() <= p.x() && p.x() <= b.xHi() &&
                    b.yLo() <= p.y() && p.y() <= b.yHi();
            }
        };
    protected static final SpatialObjectSerializer SERIALIZER = SpatialObjectSerializer.newSerializer();

    private static interface Filter
    {
        boolean keep(SpatialObject spatialObject);
    }

    private int xLo;
    private int xHi;
    private int yLo;
    private int yHi;
}
