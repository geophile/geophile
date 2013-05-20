/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.index.treeindex.TreeIndex;
import com.geophile.z.spatialobject.d2.Point;
import com.geophile.z.space.SpaceImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MultiCursorTest
{
    @Test
    public void testNoScans()
    {
        MultiCursor<Point> scan = new MultiCursor<>();
        scan.start();
        assertNull(scan.next());
    }

    @Test
    public void testOneEmptyScan()
    {
        MultiCursor<Point> scan = new MultiCursor<>();
        scan.addInput(scan());
        scan.start();
        assertNull(scan.next());
    }
    
    @Test
    public void testMultipleEmptyScans()
    {
        MultiCursor<Point> scan = new MultiCursor<>();
        for (int n = 1; n <= 10; n++) {
            scan.addInput(scan());
        }
        scan.start();
        assertNull(scan.next());
    }

    @Test
    public void testOneNonEmptyScan()
    {
        for (int n = 1; n <= 10; n++) {
            MultiCursor<Point> cursor = new MultiCursor<>();
            for (int i = 0; i < n; i++) {
                cursor.addInput(scan(point(i)));
            }
            cursor.start();
            Point point;
            int expected = 0;
            Record<Point> entry;
            while ((entry = cursor.next()) != null) {
                point = entry.spatialObject();
                assertEquals(expected, point.x());
                assertEquals(expected, point.y());
                expected++;
            }
            assertEquals(n, expected);
        }
    }

    @Test
    public void testMultipleNonEmptyScans()
    {
        for (int n = 1; n <= 10; n++) {
            long xy = 0;
            MultiCursor<Point> scan = new MultiCursor<>();
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= i; j++) {
                    scan.addInput(scan(point(xy++)));
                }
            }
            long xyLimit = xy;
            scan.start();
            Point point;
            int expected = 0;
            Record<Point> entry;
            while ((entry = scan.next()) != null) {
                point = entry.spatialObject();
                assertEquals(expected, point.x());
                assertEquals(expected, point.y());
                expected++;
            }
            assertEquals(xyLimit, expected);
        }
    }
    
    private Cursor<Point> scan(Point... points)
    {
        TreeIndex<Point> treeIndex = new TreeIndex<>();
        long z = 0;
        for (Point point : points) {
            treeIndex.add(zCounter++,  point);
        }
        return treeIndex.cursor(SpaceImpl.z(0, 0)); // cursor the entire space
    }

    private Point point(long xy)
    {
        return new Point(xy, xy);
    }

    private static final SpaceImpl SPACE = new SpaceImpl(new int[]{10, 10}, null);

    private long zCounter = 0;
}
