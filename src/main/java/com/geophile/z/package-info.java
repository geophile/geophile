/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/**
 * 
 * Geophile is a Java implementation of <i>spatial join</i>. Given two
 * sets of spatial objects, <i>R</i> and <i>S</i>, the spatial join is
 * the set of pairs, (<i>x</i>, <i>y</i>), such that <i>x</i> belongs to
 * <i>R</i>, <i>y</i> belongs to <i>S</i>, and <i>x</i> and <i>y</i>
 * overlap.
 *
 * This implementation is built on two abstractions:
 *
 * <ul>
 *    <li><b>Index:</b> Any key/value data structure supporting both random and
 * sequential access. Binary trees, skiplists, and b-trees are examples
 * of indexes that are compatible with Geophile. Hash tables are not,
 * because they do not provide for sequential access in key order.
 *    <li><b>Spatial Object:</b> Any kind of spatial object, in any number of
 * dimensions. The implementation must provide a method that determines the
 * relationship of the object to a box: the box is outside the spatial
 * object; the box is contained by the spatial object; or the box
 * overlaps the spatial object.
 *</ul>
 *
 * These implementations can be mixed arbitrarily, as long as all the
 * spatial objects involved are located in the same space.  For example,
 * you could compute the spatial join of a b-tree containing millions of
 * polygons with an in-memory sorted array containing a small number of
 * points. The output would contain the (polygon, point) pairs for which
 * the point is inside the polygon.
 *
 * <h2>Concepts</h2>
 *
 * In addition to Index and Spatial Object, described above, Geophile
 * relies on the following concepts.
 *
 *<h3>Space</h3>
 *
 * In order to use Geophile, you must describe the <i>space</i> in which spatial
 * objects reside by giving the number of cells in each dimension, e.g.
 *
 *<pre>
 *    Space space = Space.newSpace(new double[]{0.0, 1_000_000.0},
 *    new double[]{0.0, 1_000_000.0},
 *    new int[]{10, 10});
 *</pre>
 *
 * A {@link com.geophile.z.Space} describes the space from the application's point of view.
 * In this case, the space has two dimensions, whose coordinates go from 0 to 1,000,000 in each
 * dimension. The last argument says that Geophile represents the space internally
 * using a grid with 10 bits of resolution in each dimension.
 *
 * The total resolution must never exceed 57. In this case, the total resolution is 20 (10 + 10).
 *
 *
 *<h3>Spatial Index</h3>
 *
 * A <i>spatial index</i> is layered over a {@link com.geophile.z.Space} and an {@link com.geophile.z.Index}.
 * This is the
 * interface for adding and removing spatial objects.
 *
 * Geophile works by mapping spatial objects into <i>z-values</i>, 64-bit
 * integers which encode spatial information. This encoding of spatial
 * objects as z-values, and the use of z-values as index keys is managed
 * by the {@link com.geophile.z.SpatialIndex} implementation.
 *
 * <h3>Spatial Join</h3>
 *
 * <i>Spatial join</i>  is computed by the {@link com.geophile.z.SpatialJoin} class, given two
 * spatial indexes. Output from spatial join comprises a set of
 * overlapping pairs of spatial objects, which can be accessed through an
 * Iterator.
 *
 * <h2>Included with Geophile</h2>
 *
 * Geophile includes some implementations of {@link com.geophile.z.SpatialObject}:
 *
 * <ul>
 * <li> {@link com.geophile.z.spatialobject.d2.Point}: A point in 2d space.
 * <li> {@link com.geophile.z.spatialobject.d2.Box}: A box in 2d space.
 * <li> {@link com.geophile.z.spatialobject.jts}: This package contains an adapter to
 *      the <a href="http://www.vividsolutions.com/jts/JTSHome.htm">JTS Topology suite</a> classes,
 *      which implements
 *      the <a href="http://www.opengis.org/">Open GIS Consortium</a>'s
 *      <a href="http://www.opengeospatial.org/standards/sfa">Simple Feature Specification</a>.
 * </ul>
 */

package com.geophile.z;
