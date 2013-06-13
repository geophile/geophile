/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.examples;

import com.geophile.z.*;
import com.geophile.z.index.treeindex.TreeIndex;
import com.geophile.z.spatialjoin.SpatialJoinFilter;
import com.geophile.z.spatialjoin.SpatialJoinImpl;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

public class PointsInBox
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        new PointsInBox().run();
    }

    private void run() throws IOException, InterruptedException
    {
        Space space = Space.newSpace(APPLICATION_SPACE, SPACE_X, SPACE_Y);
        // Load spatial index with points
        SpatialIndex<Point> points = SpatialIndex.newSpatialIndex(space, new TreeIndex<Point>());
        for (int i = 0; i < N_POINTS; i++) {
            points.add(randomPoint());
        }
        // Run queries
        for (int q = 0; q < N_QUERIES; q++) {
            // Load spatial index with query box
            SpatialIndex<Box> box = SpatialIndex.newSpatialIndex(space, new TreeIndex<Box>());
            Box query = randomBox();
            box.add(query);
            // Create Iterator over spatial join output
            Iterator<Pair<Box, Point>> iterator =
                SpatialJoin.newSpatialJoin(BOX_CONTAINS_POINT, SpatialJoinImpl.Duplicates.EXCLUDE)
                           .iterator(box, points);
            // Print points contained in box
            System.out.println(String.format("Points inside %s", query));
            while (iterator.hasNext()) {
                Pair<Box, Point> pointInBox = iterator.next();
                System.out.println(String.format("    %s", pointInBox.right()));
            }
        }
    }

    private Point randomPoint()
    {
        int x = random.nextInt(SPACE_X);
        int y = random.nextInt(SPACE_Y);
        return new Point(x, y);
    }
    
    private Box randomBox()
    {
        int xLo = random.nextInt(SPACE_X - BOX_WIDTH);
        int xHi = xLo + BOX_WIDTH - 1;
        int yLo = random.nextInt(SPACE_Y - BOX_HEIGHT);
        int yHi = yLo + BOX_HEIGHT - 1;
        return new Box(xLo, xHi, yLo, yHi);
    }

    private static final int SPACE_X = 1_000_000;
    private static final int SPACE_Y = 1_000_000;
    private static final int N_POINTS = 1_000_000;
    private static final int BOX_WIDTH = 2_000;
    private static final int BOX_HEIGHT = 2_000;
    private static final int N_QUERIES = 5;
    private static final ApplicationSpace APPLICATION_SPACE =
        new ApplicationSpace()
        {
            @Override
            public int dimensions()
            {
                return 2;
            }

            @Override
            public double lo(int d)
            {
                return 0;
            }

            @Override
            public double hi(int d)
            {
                switch (d) {
                    case 0: return SPACE_X;
                    case 1: return SPACE_Y;
                }
                assert false;
                return Double.NaN;
            }
        };
    private static final SpatialJoinFilter<Box, Point> BOX_CONTAINS_POINT =
        new SpatialJoinFilter<Box, Point>()
        {
            @Override
            public boolean overlap(Box box, Point point)
            {
                return
                    box.xLo() <= point.x() && point.x() <= box.xHi() &&
                    box.yLo() <= point.y() && point.y() <= box.yHi();
            }
        };

    private final Random random = new Random(System.currentTimeMillis());
}
