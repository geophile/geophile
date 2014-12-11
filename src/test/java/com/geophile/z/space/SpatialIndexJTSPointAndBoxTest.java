/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
import com.geophile.z.TestIndex;
import com.geophile.z.TestRecord;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.jts.JTS;
import com.geophile.z.spatialobject.jts.JTSPoint;
import com.geophile.z.spatialobject.jts.JTSSpatialObject;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

// Like SpatialIndexTest, but with JTS spatial objects

public class SpatialIndexJTSPointAndBoxTest
{
    @Test
    public void testRetrieval() throws IOException, InterruptedException
    {
        Index<TestRecord> index = newIndex();
        spatialIndex = new SpatialIndexImpl<>(SPACE, index, SpatialIndex.Options.DEFAULT);
        int id = 0;
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                JTSSpatialObject point = point(x, y);
                spatialIndex.add(point, RECORD_FACTORY.setup(point, id++));
            }
        }
        Random random = new Random(SEED);
        for (int i = 0; i < 1000; i++) {
            generateRandomBox(random);
            test(xLo, xHi, yLo, yHi,
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
    public void testRemoveAll() throws IOException, InterruptedException
    {
        Index<TestRecord> index = newIndex();
        spatialIndex = new SpatialIndexImpl<>(SPACE, index, SpatialIndex.Options.DEFAULT);
        int id = 0;
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                JTSSpatialObject point = point(x, y);
                spatialIndex.add(point, RECORD_FACTORY.setup(point, id++));
            }
        }
        // Remove everything
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                spatialIndex.remove(point(x, y), RECORD_FILTER);
            }
        }
        Random random = new Random(SEED);
        for (int i = 0; i < 1000; i++) {
            generateRandomBox(random);
            test(xLo, xHi, yLo, yHi,
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
    public void testRemoveSome() throws IOException, InterruptedException
    {
        Index<TestRecord> index = newIndex();
        spatialIndex = new SpatialIndexImpl<>(SPACE, index, SpatialIndex.Options.DEFAULT);
        int id = 0;
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                JTSSpatialObject point = point(x, y);
                spatialIndex.add(point, RECORD_FACTORY.setup(point, id++));
            }
        }
        // Remove (x, y), for odd x/10 and even y/10
        for (long x = 0; x < X_MAX; x += 10) {
            if ((x / 10) % 2 == 1) {
                for (long y = 0; y < Y_MAX; y += 10) {
                    if ((y / 10) % 2 == 0) {
                        spatialIndex.remove(point(x, y), RECORD_FILTER);
                    }
                }
            }
        }
        Random random = new Random(SEED);
        for (int i = 0; i < 1000; i++) {
            generateRandomBox(random);
            test(xLo, xHi, yLo, yHi,
                 new Filter()
                 {
                     @Override
                     public boolean keep(SpatialObject spatialObject)
                     {
                         JTSPoint point = (JTSPoint) spatialObject;
                         return !((point.point().getX() / 10) % 2 == 1 && (point.point().getY() / 10) % 2 == 0);
                     }
                 });
        }
    }

    private void test(int xLo, int xHi, int yLo, int yHi, Filter filter) throws IOException, InterruptedException
    {
        Box box = new Box(xLo, xHi, yLo, yHi);
        TestIndex boxTestIndex = new TestIndex();
        SpatialIndex<TestRecord> query = new SpatialIndexImpl<>(SPACE, boxTestIndex, SpatialIndex.Options.DEFAULT);
        query.add(box, RECORD_FACTORY.setup(box, 0));
        Iterator<Pair<TestRecord, TestRecord>> iterator =
            SpatialJoin.newSpatialJoin(SpatialJoin.Duplicates.INCLUDE, FILTER).iterator(query, spatialIndex);
        List<JTSSpatialObject> actual = new ArrayList<>();
        JTSSpatialObject point;
        while (iterator.hasNext()) {
            point = (JTSPoint) iterator.next().right().spatialObject();
            if (!actual.contains(point)) {
                actual.add(point);
            }
        }
        List<JTSSpatialObject> expected = new ArrayList<>();
        for (long x = 10 * ((xLo + 9) / 10); x <= 10 * (xHi / 10); x += 10) {
            for (long y = 10 * ((yLo + 9) / 10); y <= 10 * (yHi / 10); y += 10) {
                point = point(x, y);
                if (filter.keep(point)) {
                    expected.add(point);
                }
            }
        }
        Collections.sort(actual, POINT_RANKING);
        Collections.sort(expected, POINT_RANKING);
        assertEquals(expected, actual);
    }

    private JTSSpatialObject point(double x, double y)
    {
        return JTS.spatialObject(SPACE, factory.createPoint(new Coordinate(x, y)));
    }

    private void generateRandomBox(Random random)
    {
        do {
            xLo = random.nextInt(X_MAX);
            xHi = xLo + random.nextInt(X_MAX - xLo);
        } while (xHi <= xLo);
        do {
            yLo = random.nextInt(Y_MAX);
            yHi = random.nextInt(Y_MAX - yLo);
        } while (yHi <= yLo);
    }

    private static Index<TestRecord> newIndex()
    {
        return new TestIndex();
    }

    private static final int SEED = 123456;
    private static final int X_MAX = 1000;
    private static final int Y_MAX = 1000;
    private static final Comparator<JTSSpatialObject> POINT_RANKING =
        new Comparator<JTSSpatialObject>()
        {
            @Override
            public int compare(JTSSpatialObject x, JTSSpatialObject y)
            {
                JTSPoint p = (JTSPoint) x;
                JTSPoint q = (JTSPoint) y;
                int c = p.point().getX() < q.point().getX() ? -1 : p.point().getX() > q.point().getX() ? 1 : 0;
                if (c == 0) {
                    c = p.point().getY() < q.point().getY() ? -1 : p.point().getY() > q.point().getY() ? 1 : 0;
                }
                return c;
            }
        };
    private static final SpaceImpl SPACE = new SpaceImpl(new double[]{0, 0}, new double[]{1000, 1000}, new int[]{10, 10}, null);
    private static final SpatialJoin.Filter<TestRecord, TestRecord> FILTER =
        new SpatialJoin.Filter<TestRecord, TestRecord>()
        {
            @Override
            public boolean overlap(TestRecord r, TestRecord s)
            {
                Box b = (Box) r.spatialObject();
                JTSPoint p = (JTSPoint) s.spatialObject();
                return
                    b.xLo() <= p.point().getX() && p.point().getX() <= b.xHi() &&
                    b.yLo() <= p.point().getY() && p.point().getY() <= b.yHi();
            }
        };
    private static final Record.Filter<TestRecord> RECORD_FILTER = new Record.Filter<TestRecord>()
    {
        @Override
        public boolean select(TestRecord record)
        {
            return true;
        }
    };
    private static final TestRecord.Factory RECORD_FACTORY = new TestRecord.Factory();

    private SpatialIndexImpl<TestRecord> spatialIndex;
    private final GeometryFactory factory = new GeometryFactory();

    private static interface Filter
    {
        boolean keep(SpatialObject spatialObject);
    }

    private int xLo;
    private int xHi;
    private int yLo;
    private int yHi;
}
