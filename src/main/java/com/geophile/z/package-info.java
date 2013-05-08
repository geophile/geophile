/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/**
 * Geophile, implemented by this package, is a framework for spatial indexing.
 * You create a {@link com.geophile.z.Space}, which
 * represents a multi-dimensional space, and defines the coordinates for each dimension. You can then add
 * {@link com.geophile.z.spatialobject.SpatialObject}s to and remove them from a {@link com.geophile.z.SpatialIndex} associated
 * with the space. The
 * {@link com.geophile.z.SpatialIndex} can be searched using
 * {@link com.geophile.z.SpatialIndex#overlapping(com.geophile.z.spatialobject.SpatialObject, com.geophile.z.SpatialIndex.Duplicates)},
 * which finds the {@link com.geophile.z.spatialobject.SpatialObject}s in the {@link com.geophile.z.SpatialIndex}
 * that overlap the given query object.
 *
 * To use geophile, you need to implement two abstractions, provided in this package as java interfaces:
 *
 * 1. {@link com.geophile.z.spatialobject.SpatialObject}: A {@link com.geophile.z.spatialobject.SpatialObject} represents a set of points.
 *    For example, in a 2d-space,
 *    there might be a Box spatial object, representing the set of points (x, y) such that 100 <= x <= 110 and
 *    560 <= y <= 567.
 *
 * 2. {@link com.geophile.z.index.Index}: An index is a data structure that efficiently supports random and sequential access
 *    in key order.
 *    Examples of suitable data structures include sorted arrays, balanced binary trees, b-trees, and skiplists.
 *    Hash tables do not qualify because they do not allow for efficient access in key order.
 */

package com.geophile.z;
