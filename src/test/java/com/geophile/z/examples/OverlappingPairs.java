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
        Space space = Space.newSpace(APPLICATION_SPACE, SPACE_X, SPACE_Y);
        // Load spatial indexes with boxes
        SpatialIndex left = SpatialIndex.newSpatialIndex(space, new TreeIndex());
        SpatialIndex right = SpatialIndex.newSpatialIndex(space, new TreeIndex());
        for (int i = 0; i < N_BOXES; i++) {
            left.add(randomBox());
            right.add(randomBox());
        }
        // Find overlapping pairs
        Iterator<Pair> iterator =
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
        int xLo = random.nextInt(SPACE_X - BOX_WIDTH);
        int xHi = xLo + BOX_WIDTH - 1;
        int yLo = random.nextInt(SPACE_Y - BOX_HEIGHT);
        int yHi = yLo + BOX_HEIGHT - 1;
        return new Box(xLo, xHi, yLo, yHi);
    }

    private static final int SPACE_X = 1_000_000;
    private static final int SPACE_Y = 1_000_000;
    private static final int N_BOXES = 1_000_000;
    private static final int BOX_WIDTH = 2;
    private static final int BOX_HEIGHT = 2;
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
    private static final SpatialJoinFilter BOX_OVERLAP =
        new SpatialJoinFilter()
        {
            @Override
            public boolean overlap(SpatialObject a, SpatialObject b)
            {
                assert false;
                return false; // ((Box)a).overlap(((Box)b));
            }
        };

    private final Random random = new Random(System.currentTimeMillis());
}
