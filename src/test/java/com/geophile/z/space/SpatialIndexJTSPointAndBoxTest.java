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

import com.geophile.z.*;
import com.geophile.z.index.Record;
import com.geophile.z.index.tree.TreeIndex;
import com.geophile.z.spatialjoin.SpatialJoinFilter;
import com.geophile.z.spatialjoin.SpatialJoinImpl;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.jts.JTSPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

// Like SpatialIndexTest, but with JTS spatial objects

public class SpatialIndexJTSPointAndBoxTest
{
    @Test
    public void testRetrieval() throws IOException, InterruptedException
    {
        TreeIndex index = new TreeIndex();
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                spatialIndex.add(point(x, y));
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
        TreeIndex index = new TreeIndex();
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                spatialIndex.add(point(x, y));
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
        TreeIndex index = new TreeIndex();
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                spatialIndex.add(point(x, y));
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

    @Test
    public void spatialIdGeneratorRestore() throws IOException, InterruptedException
    {
        TreeIndex index = new TreeIndex();
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        // pX: expected id is X
        JTSPoint p0 = point(0, 0);
        spatialIndex.add(p0);
        assertEquals(0, p0.id());
        JTSPoint p1 = point(1, 1);
        spatialIndex.add(p1);
        assertEquals(1, p1.id());
        JTSPoint p2 = point(2, 2);
        spatialIndex.add(p2);
        assertEquals(2, p2.id());
        // Creating a new SpatialIndexImpl restores the id generator
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        JTSPoint q0 = point(0, 0);
        spatialIndex.add(q0);
        assertEquals(SpatialIndexImpl.soidReservationBlockSize() + 0, q0.id());
        JTSPoint q1 = point(1, 1);
        spatialIndex.add(q1);
        assertEquals(SpatialIndexImpl.soidReservationBlockSize() + 1, q1.id());
        JTSPoint q2 = point(2, 2);
        spatialIndex.add(q2);
        assertEquals(SpatialIndexImpl.soidReservationBlockSize() + 2, q2.id());
    }

    @Test
    public void spatialIdGeneratorTracking() throws IOException, InterruptedException
    {
        final int SOID_RESERVATION_BLOCK_SIZE = 3;
        System.setProperty(SpatialIndexImpl.SOID_RESERVALTION_BLOCK_SIZE_PROPERTY,
                           Integer.toString(SOID_RESERVATION_BLOCK_SIZE));
        TreeIndex index = new TreeIndex();
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        assertEquals(SOID_RESERVATION_BLOCK_SIZE, spatialIndex.firstUnreservedSoid());
        assertEquals(SOID_RESERVATION_BLOCK_SIZE, spatialIndex.firstUnreservedSoidStored());
        spatialIndex.add(point(0, 0));
        spatialIndex.add(point(1, 1));
        spatialIndex.add(point(2, 2));
        assertEquals(SOID_RESERVATION_BLOCK_SIZE, spatialIndex.firstUnreservedSoid());
        assertEquals(SOID_RESERVATION_BLOCK_SIZE, spatialIndex.firstUnreservedSoidStored());
        spatialIndex.add(point(3, 3));
        assertEquals(SOID_RESERVATION_BLOCK_SIZE * 2, spatialIndex.firstUnreservedSoid());
        assertEquals(SOID_RESERVATION_BLOCK_SIZE * 2, spatialIndex.firstUnreservedSoidStored());
    }

    private void test(int xLo, int xHi, int yLo, int yHi, Filter filter) throws IOException, InterruptedException
    {
        Box box = new Box(xLo, xHi, yLo, yHi);
        TreeIndex boxTreeIndex = new TreeIndex();
        SpatialIndex query = new SpatialIndexImpl(SPACE, boxTreeIndex, SpatialIndex.Options.DEFAULT);
        query.add(box);
        Iterator<Pair> iterator =
            SpatialJoin.newSpatialJoin(FILTER, SpatialJoinImpl.Duplicates.INCLUDE).iterator(query, spatialIndex);
        List<JTSPoint> actual = new ArrayList<>();
        JTSPoint point;
        while (iterator.hasNext()) {
            point = (JTSPoint) iterator.next().right();
            if (!actual.contains(point)) {
                actual.add(point);
            }
        }
        List<JTSPoint> expected = new ArrayList<>();
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

    private JTSPoint point(double x, double y)
    {
        return new JTSPoint(SPACE, factory.createPoint(new Coordinate(x, y)));
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

    private static final int SEED = 123456;
    private static final int X_MAX = 1000;
    private static final int Y_MAX = 1000;
    private static final Comparator<JTSPoint> POINT_RANKING =
        new Comparator<JTSPoint>()
        {
            @Override
            public int compare(JTSPoint p, JTSPoint q)
            {
                int c = p.point().getX() < q.point().getX() ? -1 : p.point().getX() > q.point().getX() ? 1 : 0;
                if (c == 0) {
                    c = p.point().getY() < q.point().getY() ? -1 : p.point().getY() > q.point().getY() ? 1 : 0;
                }
                return c;
            }
        };
    private static final SpaceImpl SPACE = new SpaceImpl(new double[]{0, 0}, new double[]{1000, 1000}, new int[]{10, 10}, null);
    private static final SpatialJoinFilter FILTER = new SpatialJoinFilter()
    {
        @Override
        public boolean overlap(SpatialObject x, SpatialObject y)
        {
            Box b = (Box) x;
            JTSPoint p = (JTSPoint) y;
            return
                b.xLo() <= p.point().getX() && p.point().getX() <= b.xHi() &&
                b.yLo() <= p.point().getY() && p.point().getY() <= b.yHi();
        }
    };
    private static final RecordFilter RECORD_FILTER = new RecordFilter()
    {
        @Override
        public boolean select(Record record)
        {
            return true;
        }
    };

    private SpatialIndexImpl spatialIndex;
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
