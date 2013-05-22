/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile;

import com.geophile.z.Pair;
import com.geophile.z.SpatialIndex;
import com.geophile.z.spatialjoin.SpatialJoinIterator;
import com.geophile.z.spatialobject.SpatialObject;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;
import com.geophile.z.space.SpaceImpl;
import com.geophile.z.space.SpatialIndexImpl;
import com.geophile.z.index.treeindex.TreeIndex;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class SpatialIndexTest
{
    // Like TreeIndexTest.testRetrieval, but written in terms of SpatialIndex
    @Test
    public void testRetrieval()
    {
        TreeIndex<Point> index = new TreeIndex<>();
        spatialIndex = new SpatialIndexImpl<>(SPACE, index);
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
    public void testRemoveAll()
    {
        TreeIndex<Point> index = new TreeIndex<>();
        spatialIndex = new SpatialIndexImpl<>(SPACE, index);
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
    public void testRemoveSome()
    {
        TreeIndex<Point> index = new TreeIndex<>();
        spatialIndex = new SpatialIndexImpl<>(SPACE, index);
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

    private void test(int xLo, int xHi, int yLo, int yHi, Filter filter)
    {
        Box box = new Box(xLo, xHi, yLo, yHi);
        TreeIndex<Box> boxTreeIndex = new TreeIndex<>();
        SpatialIndex<Box> query = new SpatialIndexImpl<>(SPACE, boxTreeIndex);
        query.add(box);
        Iterator<Pair<Box, Point>> iterator = new SpatialJoinIterator<>(query, spatialIndex);
        List<Point> actual = new ArrayList<>();
        Point point;
        while (iterator.hasNext()) {
            point = iterator.next().right();
            if (!actual.contains(point) && boxContainsPoint(box, point)) {
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

    private boolean boxContainsPoint(Box box, Point point)
    {
        return
            box.xLo() <= point.x() && point.x() <= box.xHi() &&
            box.yLo() <= point.y() && point.y() <= box.yHi();
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
    private static final SpaceImpl SPACE = new SpaceImpl(new int[]{10, 10}, null);

    private SpatialIndex<Point> spatialIndex;

    private static interface Filter
    {
        boolean keep(SpatialObject spatialObject);
    }
}
