/* Copyright (c) 2023 OpenJAX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.openjax.binarytree;

import java.io.Serializable;
import java.util.NavigableSet;

import org.libj.util.Interval;

/**
 * Interface defining the abstract methods for a {@link NavigableSet} containing {@link Interval} elements.
 *
 * @param <T> The type parameter of values defining the coordinates of the {@link Interval}s belonging to this set.
 */
public interface IntervalSet<T extends Comparable<T> & Serializable> extends NavigableSet<Interval<T>>, Serializable {
  boolean contains(T key);
  boolean contains(Interval<T> key);
  boolean intersects(Interval<T> key);
  Interval<T>[] difference(Interval<T> key);
  boolean remove(Interval<T> key);
}