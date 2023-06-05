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

import java.util.NavigableSet;

import org.libj.util.Interval;

/**
 * Interface defining the abstract methods for a {@link NavigableSet} containing {@link Interval} elements.
 *
 * @param <T> The type parameter of values defining the coordinates of the {@link Interval}s belonging to this set.
 */
public interface IntervalSet<T> extends NavigableSet<Interval<T>> {
  /**
   * Returns {@code true} if the full span (i.e. from the {@linkplain Interval#getMin() min} to the {@linkplain Interval#getMax()
   * max} coordinates, which includes the min and max values themselves) of the provided {@code key} is present in (i.e. contained
   * by) at least one {@link Interval} in this set, otherwise {@code false}.
   *
   * @param key The key whose presence in this set is to be tested.
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @return {@code true} if this set contains the provided {@code key}, otherwise {@code false}.
   */
  boolean contains(Interval<T> key);

  /**
   * Returns {@code true} if the provided {@code key} is present in (i.e. contained by) at least one {@link Interval} in this set,
   * otherwise {@code false}.
   *
   * @param key The key whose presence in this set is to be tested.
   * @return {@code true} if the provided {@code key} is present in (i.e. contained by) at least one {@link Interval} in this set,
   *         otherwise {@code false}.
   */
  @Override
  boolean contains(Object key);

  /**
   * Returns an array of {@link Interval} objects representing the intervals that are not present in (i.e. contained by) this set
   * within the range of the provided {@link Interval}.
   * <p>
   * If the full range of the provided {@link Interval} is present (i.e. contained by) this set, then this method returns an empty
   * array.
   * <p>
   * If the full range of the provided {@link Interval} is absent (i.e. not contained by) this set, then this method returns an
   * array containing a reference to the provided {@link Interval}.
   *
   * @param key The {@link Interval} representing the range within which to determine the intervals that are not present in this
   *          set.
   * @return An array of {@link Interval} objects representing the intervals that are not present in this set within the range of
   *         the provided {@link Interval}.
   */
  Interval<T>[] difference(Interval<T> key);

  /**
   * Returns {@code true} if part of the span (i.e. from the {@linkplain Interval#getMin() min} to the {@linkplain Interval#getMax()
   * max} coordinates, which includes the min and max values themselves) of the provided {@code key} is present in (i.e. contained
   * by) at least one {@link Interval} in this set, otherwise {@code false}.
   *
   * @param key The {@link Interval} to test for intersection with this set.
   * @return {@code true} if part of the span (i.e. from the {@linkplain Interval#getMin() min} to the {@linkplain Interval#getMax()
   *         max} coordinates, which includes the min and max values themselves) of the provided {@link Interval} is present in
   *         (i.e. contained by) at least one {@link Interval} in this set, otherwise {@code false}.
   */
  boolean intersects(Interval<T> key);

  /**
   * Returns {@code true} if an {@link Interval} of the provided {@code key} was removed from this set, otherwise {@code false}.
   *
   * @param key The {@link Interval} to remove from this set.
   * @return {@code true} if an {@link Interval} of the provided {@code key} was removed from this set, otherwise {@code false}.
   */
  boolean remove(Interval<T> key);

  // TODO: Implement retain()...
//  /**
//   * Retains only the {@link Interval}s that intersect with the provided {@code key}, and returns {@code true} if the set was
//   * modified, otherwise {@code false}.
//   *
//   * @param key The {@link Interval} to retain in this set.
//   * @return {@code true} if the set was modified retaining the provided {@code key}, otherwise {@code false}.
//   */
//  boolean retain(Interval<T> key);
}