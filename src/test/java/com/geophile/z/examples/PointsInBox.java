/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.examples;

import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
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
        // Load spatial index with points
        ExampleRecord.Factory recordFactory = new ExampleRecord.Factory();
        SpatialIndex<ExampleRecord> points = SpatialIndex.newSpatialIndex(SPACE, new ExampleIndex());
        for (int i = 0; i < N_POINTS; i++) {
            Point point = randomPoint();
            points.add(point, recordFactory.setup(point, i));
        }
        // Run queries
        for (int q = 0; q < N_QUERIES; q++) {
            // Create Iterator over spatial join output
            Box box = randomBox();
            Iterator<ExampleRecord> iterator =
                SpatialJoin.newSpatialJoin(SpatialJoin.Duplicates.EXCLUDE, BOX_CONTAINS_POINT).iterator(box, points);
            // Print points contained in box
            System.out.println(String.format("Points inside %s", box));
            while (iterator.hasNext()) {
                ExampleRecord record = iterator.next();
                System.out.println(String.format("    %s", record.spatialObject()));
            }
        }
    }

    private Point randomPoint()
    {
        int x = random.nextInt(X);
        int y = random.nextInt(Y);
        return new Point(x, y);
    }

    private Box randomBox()
    {
        int xLo = random.nextInt(X - BOX_WIDTH);
        int xHi = xLo + BOX_WIDTH - 1;
        int yLo = random.nextInt(Y - BOX_HEIGHT);
        int yHi = yLo + BOX_HEIGHT - 1;
        return new Box(xLo, xHi, yLo, yHi);
    }

    private static final int X = 1_000_000;
    private static final int Y = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final int N_POINTS = 100_000;
    private static final int BOX_WIDTH = 10_000;
    private static final int BOX_HEIGHT = 10_000;
    private static final int N_QUERIES = 5;
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{X, Y},
                                                      new int[]{X_BITS, Y_BITS});
    private static final SpatialJoin.Filter<SpatialObject, ExampleRecord> BOX_CONTAINS_POINT =
        new SpatialJoin.Filter<SpatialObject, ExampleRecord>()
        {
            @Override
            public boolean overlap(SpatialObject x, ExampleRecord y)
            {
                Box box = (Box) x;
                Point point = (Point) y.spatialObject();
                return
                    box.xLo() <= point.x() && point.x() <= box.xHi() &&
                    box.yLo() <= point.y() && point.y() <= box.yHi();
            }
        };

    private final Random random = new Random(System.currentTimeMillis());
}
