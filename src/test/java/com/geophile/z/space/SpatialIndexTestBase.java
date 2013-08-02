/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.*;
import com.geophile.z.spatialjoin.SpatialJoinFilter;
import com.geophile.z.spatialjoin.SpatialJoinImpl;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

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
    public void testRetrieval() throws IOException, InterruptedException
    {
        Index index = newIndex();
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                spatialIndex.add(new Point(x, y));
            }
        }
        Random random = new Random(SEED);
        int xLo;
        int xHi;
        int yLo;
        int yHi;
        for (int i = 0; i < 1000; i++) {
            do {
                xLo = random.nextInt(X_MAX);
                xHi = xLo + random.nextInt(X_MAX - xLo);
            } while (xHi < xLo);
            do {
                yLo = random.nextInt(Y_MAX);
                yHi = random.nextInt(Y_MAX - yLo);
            } while (yHi < yLo);
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
        Index index = newIndex();
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                spatialIndex.add(new Point(x, y));
            }
        }
        // Remove everything
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                spatialIndex.remove(new Point(x, y));
            }
        }
        Random random = new Random(SEED);
        int xLo;
        int xHi;
        int yLo;
        int yHi;
        for (int i = 0; i < 1000; i++) {
            do {
                xLo = random.nextInt(X_MAX);
                xHi = xLo + random.nextInt(X_MAX - xLo);
            } while (xHi < xLo);
            do {
                yLo = random.nextInt(Y_MAX);
                yHi = random.nextInt(Y_MAX - yLo);
            } while (yHi < yLo);
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
        Index index = newIndex();
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                spatialIndex.add(new Point(x, y));
            }
        }
        // Remove (x, y), for odd x/10 and even y/10
        for (long x = 0; x < X_MAX; x += 10) {
            if ((x / 10) % 2 == 1) {
                for (long y = 0; y < Y_MAX; y += 10) {
                    if ((y / 10) % 2 == 0) {
                        spatialIndex.remove(new Point(x, y));
                    }
                }
            }
        }
        Random random = new Random(SEED);
        int xLo;
        int xHi;
        int yLo;
        int yHi;
        for (int i = 0; i < 1000; i++) {
            do {
                xLo = random.nextInt(X_MAX);
                xHi = xLo + random.nextInt(X_MAX - xLo);
            } while (xHi < xLo);
            do {
                yLo = random.nextInt(Y_MAX);
                yHi = random.nextInt(Y_MAX - yLo);
            } while (yHi < yLo);
            test(xLo, xHi, yLo, yHi,
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
    public void spatialIdGeneratorRestore() throws IOException, InterruptedException
    {
        Index index = newIndex();
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        // pX: expected id is X
        Point p0 = new Point(0, 0);
        spatialIndex.add(p0);
        assertEquals(0, p0.id());
        Point p1 = new Point(1, 1);
        spatialIndex.add(p1);
        assertEquals(1, p1.id());
        Point p2 = new Point(2, 2);
        spatialIndex.add(p2);
        assertEquals(2, p2.id());
        // Creating a new SpatialIndexImpl restores the id generator
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        Point q0 = new Point(0, 0);
        spatialIndex.add(q0);
        assertEquals(SpatialIndexImpl.soidReservationBlockSize() + 0, q0.id());
        Point q1 = new Point(1, 1);
        spatialIndex.add(q1);
        assertEquals(SpatialIndexImpl.soidReservationBlockSize() + 1, q1.id());
        Point q2 = new Point(2, 2);
        spatialIndex.add(q2);
        assertEquals(SpatialIndexImpl.soidReservationBlockSize() + 2, q2.id());
    }

    @Test
    public void spatialIdGeneratorTracking() throws IOException, InterruptedException
    {
        final int SOID_RESERVATION_BLOCK_SIZE = 3;
        System.setProperty(SpatialIndexImpl.SOID_RESERVALTION_BLOCK_SIZE_PROPERTY,
                           Integer.toString(SOID_RESERVATION_BLOCK_SIZE));
        Index index = newIndex();
        spatialIndex = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        assertEquals(SOID_RESERVATION_BLOCK_SIZE, spatialIndex.firstUnreservedSoid());
        assertEquals(SOID_RESERVATION_BLOCK_SIZE, spatialIndex.firstUnreservedSoidStored());
        spatialIndex.add(new Point(0, 0));
        spatialIndex.add(new Point(1, 1));
        spatialIndex.add(new Point(2, 2));
        assertEquals(SOID_RESERVATION_BLOCK_SIZE, spatialIndex.firstUnreservedSoid());
        assertEquals(SOID_RESERVATION_BLOCK_SIZE, spatialIndex.firstUnreservedSoidStored());
        spatialIndex.add(new Point(3, 3));
        assertEquals(SOID_RESERVATION_BLOCK_SIZE * 2, spatialIndex.firstUnreservedSoid());
        assertEquals(SOID_RESERVATION_BLOCK_SIZE * 2, spatialIndex.firstUnreservedSoidStored());
    }

    private void test(int xLo, int xHi, int yLo, int yHi, Filter filter) throws IOException, InterruptedException
    {
        Box box = new Box(xLo, xHi, yLo, yHi);
        Index index = newIndex();
        SpatialIndex query = new SpatialIndexImpl(SPACE, index, SpatialIndex.Options.DEFAULT);
        query.add(box);
        Iterator<Pair> iterator =
            SpatialJoin.newSpatialJoin(FILTER, SpatialJoinImpl.Duplicates.INCLUDE).iterator(query, spatialIndex);
        List<Point> actual = new ArrayList<>();
        Point point;
        while (iterator.hasNext()) {
            point = (Point) iterator.next().right();
            if (!actual.contains(point)) {
                actual.add(point);
            }
        }
        List<Point> expected = new ArrayList<>();
        for (long x = 10 * ((xLo + 9) / 10); x <= 10 * (xHi / 10); x += 10) {
            for (long y = 10 * ((yLo + 9) / 10); y <= 10 * (yHi / 10); y += 10) {
                point = new Point(x, y);
                if (filter.keep(point)) {
                    expected.add(point);
                }
            }
        }
        Collections.sort(actual, POINT_RANKING);
        Collections.sort(expected, POINT_RANKING);
        assertEquals(expected, actual);
    }

    public abstract Index newIndex() throws IOException, InterruptedException;

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
    private static final ApplicationSpace APP_SPACE =
        ApplicationSpace.newApplicationSpace(new double[]{0, 0}, new double[]{1000, 1000});
    private static final SpaceImpl SPACE = new SpaceImpl(APP_SPACE, new int[]{10, 10}, null);
    private static final SpatialJoinFilter FILTER = new SpatialJoinFilter()
    {
        @Override
        public boolean overlap(SpatialObject x, SpatialObject y)
        {
            Box b = (Box) x;
            Point p = (Point) y;
            return
                b.xLo() <= p.x() && p.x() <= b.xHi() &&
                b.yLo() <= p.y() && p.y() <= b.yHi();
        }
    };
    protected static final Serializer SERIALIZER = Serializer.newSerializer();

    private SpatialIndexImpl spatialIndex;

    private static interface Filter
    {
        boolean keep(SpatialObject spatialObject);
    }
}
