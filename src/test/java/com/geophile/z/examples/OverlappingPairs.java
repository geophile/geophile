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
import com.geophile.z.index.treeindex.TreeIndex;
import com.geophile.z.spatialjoin.SpatialJoinFilter;
import com.geophile.z.spatialjoin.SpatialJoinImpl;
import com.geophile.z.spatialobject.d2.Box;

import java.util.Iterator;
import java.util.Random;

public class OverlappingPairs
{
    public static void main(String[] args)
    {
        new OverlappingPairs().run();
    }

    private void run()
    {
        Space space = Space.newSpace(SPACE_X, SPACE_Y);
        // Load spatial indexes with boxes
        SpatialIndex<Box> left = SpatialIndex.newSpatialIndex(space, new TreeIndex<Box>());
        SpatialIndex<Box> right = SpatialIndex.newSpatialIndex(space, new TreeIndex<Box>());
        for (int i = 0; i < N_BOXES; i++) {
            left.add(randomBox());
            right.add(randomBox());
        }
        // Find overlapping pairs
        Iterator<Pair<Box, Box>> iterator =
            SpatialJoin.newSpatialJoin(BOX_OVERLAP, SpatialJoinImpl.Duplicates.EXCLUDE).iterator(left, right);
        // Print points contained in box
        System.out.println("Overlapping pairs");
        while (iterator.hasNext()) {
            Pair<Box, Box> overlappingPair = iterator.next();
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
    private static final SpatialJoinFilter<Box, Box> BOX_OVERLAP =
        new SpatialJoinFilter<Box, Box>()
        {
            @Override
            public boolean overlap(Box a, Box b)
            {
                return a.overlap(b);
            }
        };

    private final Random random = new Random(System.currentTimeMillis());
}
