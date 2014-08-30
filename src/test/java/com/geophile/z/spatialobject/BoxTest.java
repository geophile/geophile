/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject;

import com.geophile.z.spatialobject.d2.Box;
import org.junit.Test;

import static org.junit.Assert.fail;

public class BoxTest
{
    @Test
    public void testZeroWidth()
    {
        try {
            new Box(5, 5, 0, 10);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void testNegativeWidth()
    {
        try {
            new Box(5, 4, 0, 10);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testZeroHeight()
    {
        try {
            new Box(5, 6, 10, 10);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void testNegativeHeight()
    {
        try {
            new Box(5, 6, 10, 9);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testOK()
    {
        new Box(1, 2, 3, 4);
    }
}
