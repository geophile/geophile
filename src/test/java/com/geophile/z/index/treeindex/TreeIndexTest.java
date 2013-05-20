/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.treeindex;

import com.geophile.z.index.Cursor;
import com.geophile.z.index.Index;
import com.geophile.z.index.Record;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;
import com.geophile.z.space.SpaceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class TreeIndexTest
{
    @Before
    public void before()
    {
        space = new SpaceImpl(new int[]{10, 10}, null);
        index = new TreeIndex<>();
        for (long x = 0; x < X_MAX; x += 10) {
            for (long y = 0; y < Y_MAX; y += 10) {
                index.add(space.shuffle(new long[]{x, y}), new Point(x, y));
            }
        }
    }

    @Test
    public void testRetrieval()
    {
        Random random = new Random(419);
        int xLo;
        int xHi;
        int yLo;
        int yHi;
        for (int i = 0; i < 10000; i++) {
            do {
                xLo = random.nextInt(X_MAX);
                xHi = xLo + random.nextInt(X_MAX - xLo);
            } while (xHi < xLo);
            do {
                yLo = random.nextInt(Y_MAX);
                yHi = random.nextInt(Y_MAX - yLo);
            } while (yHi < yLo);
            testRetrieval(xLo, xHi, yLo, yHi);
        }
    }

    private void testRetrieval(int xLo, int xHi, int yLo, int yHi)
    {
        // debug("(%s: %s, %s: %s)", xLo, xHi, yLo, yHi);
        Box box = new Box(xLo, xHi, yLo, yHi);
        long[] zs = new long[4];
        space.decompose(box, zs);
        List<Point> actual = new ArrayList<>();
        for (long z : zs) {
            if (z != -1L) {
                // debug("%x: %s - %s", space, space.zLo(space), space.zHi(space));
                Cursor<Point> cursor = index.cursor(SpaceImpl.zLo(z));
                Point point;
                Record<Point> entry;
                while ((entry = currentIfInside(cursor, z)) != null) {
                    point = entry.spatialObject();
                    if (xLo <= point.x() && point.x() <= xHi && yLo <= point.y() && point.y() <= yHi) {
                        actual.add(point);
                    }
                }
            }
        }
        List<Point> expected = new ArrayList<>();
        for (long x = 10 * ((xLo + 9) / 10); x <= 10 * (xHi / 10); x += 10) {
            for (long y = 10 * ((yLo + 9) / 10); y <= 10 * (yHi / 10); y += 10) {
                expected.add(new Point(x, y));
            }
        }
        Collections.sort(actual, POINT_RANKING);
        Collections.sort(expected, POINT_RANKING);
        assertEquals(expected, actual);
    }

    private Record<Point> currentIfInside(Cursor<Point> cursor, long z)
    {
        Record<Point> entry = cursor.next();
        if (entry != null && entry.key().z() > SpaceImpl.zHi(z)) {
            entry = null;
        }
        return entry;
    }

    private void debug(String template, Object ... args)
    {
        System.out.println(String.format(template, args));
    }

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

    private SpaceImpl space;
    private Index<Point> index;
}
