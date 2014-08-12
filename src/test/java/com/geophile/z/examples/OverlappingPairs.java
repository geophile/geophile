/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.examples;

import com.geophile.z.*;
import com.geophile.z.SpatialJoinFilter;
import com.geophile.z.spatialjoin.SpatialJoinImpl;
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
        SpatialIndex<Record> left = SpatialIndex.newSpatialIndex(SPACE, new TestIndex());
        SpatialIndex<Record> right = SpatialIndex.newSpatialIndex(SPACE, new TestIndex());
        for (int i = 0; i < N_BOXES; i++) {
            left.add(new Record(randomBox(), i));
            right.add(new Record(randomBox(), i));
        }
        // Find overlapping pairs
        Iterator<Pair<Record, Record>> iterator =
            SpatialJoin.newSpatialJoin(BOX_OVERLAP, SpatialJoinImpl.Duplicates.EXCLUDE).iterator(left, right);
        // Print points contained in box
        System.out.println("Overlapping pairs");
        while (iterator.hasNext()) {
            Pair overlappingPair = iterator.next();
            System.out.println(String.format("    %s", overlappingPair));
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
    private static final int N_BOXES = 1_000_000;
    private static final int BOX_WIDTH = 2;
    private static final int BOX_HEIGHT = 2;
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{X, Y},
                                                      new int[]{X_BITS, Y_BITS});
    private static final SpatialJoinFilter BOX_OVERLAP =
        new SpatialJoinFilter()
        {
            @Override
            public boolean overlap(com.geophile.z.Record r, com.geophile.z.Record s)
            {
                Box a = (Box) r.spatialObject();
                Box b = (Box) s.spatialObject();
                return
                    a.xLo() <= b.xHi() && b.xLo() <= a.xHi() &&
                    a.yLo() <= b.yHi() && b.yLo() <= a.yHi();
            }
        };

    private final Random random = new Random(System.currentTimeMillis());
}
