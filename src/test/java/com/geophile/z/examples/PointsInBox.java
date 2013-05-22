/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.examples;

import java.util.Random;

public class PointsInBox
{
    public static void main(String[] args)
    {
        new PointsInBox().run();
    }

    private void run()
    {

    }

    private static final int SPACE_X = 1_000_000_000;
    private static final int SPACE_Y = 1_000_000_000;
    private static final int N_POINTS = 1_000_000;
    private static final int BOX_WIDTH = 300;
    private static final int BOX_HEIGHT = 300;

    private final Random random = new Random(System.currentTimeMillis());
}
