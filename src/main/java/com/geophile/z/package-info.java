/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/**
 * Geophile, implemented by this package, is a framework for spatial querying.
 *
 * All Geophile operations take place within an {@link com.geophile.z.ApplicationSpace}, which
 * represents a multi-dimensional space, and defines the coordinates for each dimension.
 * A {@link com.geophile.z.SpatialIndex} organizes a set of {@link com.geophile.z.SpatialObject}s,
 * and provides for
 * efficient filtering and joining based on spatial predicates.
 *
 * <p>
 * Implementations of two abstractions are needed to use Geophile:
 *
 * <ol>
 * <li> {@link com.geophile.z.SpatialObject}: A {@link com.geophile.z.SpatialObject}
 * represents a set of points.
 * For example, in a 2d-space,
 * there might be a Box spatial object, representing the set of points (x, y) such that 100 <= x <= 110 and
 * 560 <= y <= 567.
 *
 * <li> {@link com.geophile.z.Index}: An index is a data structure that efficiently supports random and
 * sequential access in key order.
 * Examples of suitable data structures include sorted arrays, balanced binary trees, b-trees, and skiplists.
 * Hash tables do not qualify because they do not allow for efficient access in key order.
 * </ol>
 *
 * Geophile includes some implementations of these interfaces:
 *
 * <ul>
 * <li> {@link com.geophile.z.spatialobject.d2.Point}: A point in 2d space.
 * <li> {@link com.geophile.z.spatialobject.d2.Box}: A box in 2d space.
 * <li> {@link com.geophile.z.spatialobject.jts}: This package contains an adapter to
 *      the <a href="http://www.vividsolutions.com/jts/JTSHome.htm">JTS Topology suite</a> classes,
 *      which implements
 *      the <a href="http://www.opengis.org/">Open GIS Consortium</a>'s
 *      <a href="http://www.opengeospatial.org/standards/sfa">Simple Feature Specification</a>.
 * <li> {@link com.geophile.z.index.treeindex.TreeIndex}: An index based on {@link java.util.TreeMap}.
 * </ul>
 */

package com.geophile.z;
