/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.examples;

import com.geophile.z.Pair;
import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.spatialobject.d2.Box;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

public class OverlappingPairs
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        new OverlappingPairs().run();
    }

    private void run() throws IOException, InterruptedException
    {
        // Load spatial indexes with boxes
        ExampleRecord.Factory recordFactory = new ExampleRecord.Factory();
        SpatialIndex<ExampleRecord> left = SpatialIndex.newSpatialIndex(SPACE, new ExampleIndex());
        SpatialIndex<ExampleRecord> right = SpatialIndex.newSpatialIndex(SPACE, new ExampleIndex());
        for (int i = 0; i < N_BOXES; i++) {
            Box leftBox = randomBox();
            Box rightBox = randomBox();
            left.add(leftBox, recordFactory.setup(leftBox, i));
            right.add(rightBox, recordFactory.setup(rightBox, i));
        }
        // Find overlapping pairs
        Iterator<Pair<ExampleRecord, ExampleRecord>> iterator =
            SpatialJoin.newSpatialJoin(SpatialJoin.Duplicates.EXCLUDE, BOX_OVERLAP).iterator(left, right);
        // Print points contained in box
        System.out.println("Overlapping pairs");
        while (iterator.hasNext()) {
            Pair<ExampleRecord, ExampleRecord> overlappingPair = iterator.next();
            System.out.format("    %s\t%s\n",
                              overlappingPair.left().spatialObject(),
                              overlappingPair.right().spatialObject());
        }
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
    private static final int N_BOXES = 100_000;
    private static final int BOX_WIDTH = 10;
    private static final int BOX_HEIGHT = 10;
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{X, Y},
                                                      new int[]{X_BITS, Y_BITS});
    private static final SpatialJoin.Filter<ExampleRecord, ExampleRecord> BOX_OVERLAP =
        new SpatialJoin.Filter<ExampleRecord, ExampleRecord>()
        {
            @Override
            public boolean overlap(ExampleRecord r, ExampleRecord s)
            {
                Box a = (Box) r.spatialObject();
                Box b = (Box) s.spatialObject();
                return
                    a.xLo() < b.xHi() && b.xLo() < a.xHi() &&
                    a.yLo() < b.yHi() && b.yLo() < a.yHi();
            }
        };

    private final Random random = new Random(System.currentTimeMillis());
}
