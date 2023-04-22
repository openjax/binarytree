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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.libj.util.CollectionUtil;
import org.libj.util.Interval;

public class IntervalArraySet<T extends Comparable<? super T> & Serializable> implements IntervalSet<T> {
  @SuppressWarnings("rawtypes")
  private static final Comparator<Interval> minComparator = (final Interval o1, final Interval o2) -> o1.getMin().compareTo(o2.getMax());
  @SuppressWarnings("rawtypes")
  private static final Comparator<Interval> maxComparator = (final Interval o1, final Interval o2) -> o1.getMax().compareTo(o2.getMax());
  @SuppressWarnings("rawtypes")
  private static final Interval[] EMPTY = {};

  private class IntervalArrayIntList extends ArrayList<Interval<T>> {
    @Override
    protected void removeRange(final int fromIndex, final int toIndex) {
      super.removeRange(fromIndex, toIndex);
    }
  }

  private final IntervalArrayIntList data = new IntervalArrayIntList();

  public IntervalArraySet(final Collection<Interval<T>> c) {
    addAll(c);
  }

  @SafeVarargs
  public IntervalArraySet(final Interval<T> ... a) {
    this(a, 0, a.length);
  }

  public IntervalArraySet(final Interval<T>[] a, final int fromIndex, final int toIndex) {
    addAll(a, fromIndex, toIndex);
  }

  public IntervalArraySet() {
  }

  @Override
  public boolean addAll(final Collection<? extends Interval<T>> c) {
    if (c instanceof List) {
      final List<? extends Interval<T>> l = (List<? extends Interval<T>>)c;
      return addAll(l, 0, l.size());
    }

    boolean changed = false;
    for (final Interval<T> i : c) // [C]
      changed |= add(i);

    return changed;
  }

  public boolean addAll(final List<? extends Interval<T>> l, int fromIndex, final int toIndex) {
    boolean changed = false;
    if (CollectionUtil.isRandomAccess(l)) {
      for (int i = fromIndex; i < toIndex; ++i) // [RA]
        changed |= add(l.get(i));
    }
    else {
      for (final ListIterator<? extends Interval<T>> iterator = l.listIterator(fromIndex); fromIndex < toIndex; ++fromIndex) // [I]
        changed |= add(iterator.next());
    }

    return changed;
  }

  @SuppressWarnings("unchecked")
  public boolean addAll(final Interval<T> ... a) {
    return addAll(a, 0, a.length);
  }

  public boolean addAll(final Interval<T>[] a, int fromIndex, final int toIndex) {
    boolean changed = false;
    while (fromIndex < toIndex) // [A]
      changed |= add(a[fromIndex++]);

    return changed;
  }

  /**
   * Add the interval defined by the provided {@code (x,y)} values to this {@link IntervalArraySet}. If the provided interval
   * overlaps one or more existing interval, the overlapping intervals are merged.
   *
   * @param key The {@link Interval} to be added.
   * @return {@code true} if the addition of the provided interval resulted in a change to this {@link IntervalArraySet}, otherwise
   *         {@code false}.
   */
  @Override
  public boolean add(final Interval<T> key) {
    final int size = data.size();
    if (size == 0)
      return data.add(key);

    final T iMin = key.getMin();
    int fromIndex = CollectionUtil.binaryClosestSearch(data, 0, size, key, minComparator);
    if (fromIndex == size) {
      final Interval<T> f = data.get(--fromIndex);
      if (iMin.compareTo(f.getMax()) > 0)
        return data.add(key);
    }

    int c0 = key.compareTo(data.get(fromIndex));
    if (c0 < 0 && fromIndex > 0) {
      final int c1 = key.compareTo(data.get(--fromIndex));
      if (c1 != 2)
        c0 = c1;
      else
        ++fromIndex;
    }

    final T a, b;
    if (c0 < 0)
      a = iMin;
    else
      a = data.get(fromIndex).getMin();

    int toIndex = CollectionUtil.binaryClosestSearch(data, fromIndex, size, key, maxComparator);
    if (toIndex == size) {
      b = key.getMax();
    }
    else {
      c0 = key.compareTo(data.get(toIndex));
      if (c0 >= -1) {
        b = data.get(toIndex).getMax();
        ++toIndex;
      }
      else {
        b = key.getMax();
      }
    }

    if (toIndex - fromIndex != 1) {
      data.removeRange(fromIndex, toIndex);
      data.add(fromIndex, new Interval<>(a, b));
    }
    else if (data.get(fromIndex).equals(a, b)) {
      return false;
    }
    else {
      data.set(fromIndex, new Interval<>(a, b));
    }

    return true;
  }

  /**
   * Returns {@code true} if the provided {@code key} overlaps with keys in this {@link IntervalArraySet}, otherwise {@code false}.
   *
   * @param key The {@link Interval} to check for overlap.
   * @return {@code true} if the provided {@code key} overlaps with keys in this {@link IntervalArraySet}, otherwise {@code false}.
   */
  @Override
  public boolean intersects(final Interval<T> key) {
    final int size = data.size();
    int i = CollectionUtil.binaryClosestSearch(data, 0, size, key, minComparator);
    if (i >= size)
      return false;

    int j = CollectionUtil.binaryClosestSearch(data, i, size, key, maxComparator);
    if (j >= size)
      --j;

    while (i <= j)
      if (key.intersects(data.get(i++)))
        return true;

    return false;
  }

  /**
   * Returns an array of {@link Interval}s not present in this {@link IntervalArraySet} within the provided {@code key}
   * {@link Interval}.
   *
   * @param key The {@link Interval} to evaluate for difference.
   * @return An array of {@link Interval}s not present in this {@link IntervalArraySet} within the provided {@code key}
   *         {@link Interval}.
   */
  @Override
  @SuppressWarnings("unchecked")
  public Interval<T>[] difference(final Interval<T> key) {
    final int size = data.size();
    int fromIndex = CollectionUtil.binaryClosestSearch(data, 0, size, key, minComparator);

    if (fromIndex >= size)
      return EMPTY;

    final Interval<T> from = data.get(fromIndex);
    final T keyMax = key.getMax();
    final T fromMin = from.getMin();
    if (fromIndex >= size || fromMin.compareTo(keyMax) >= 0)
      return EMPTY;

    int toIndex = CollectionUtil.binaryClosestSearch(data, fromIndex, size, key, maxComparator);
    if (toIndex == size)
      --toIndex;

    final Interval<T> to = data.get(toIndex);
    final T keyMin = key.getMin();
    final T toMax = to.getMax();
    if (toMax.compareTo(keyMin) < 0)
      return EMPTY;

    if (keyMax.compareTo(fromMin) <= 0 || keyMin.compareTo(toMax) >= 0)
      return new Interval[] {key};

    final Interval<T>[] diff;
    if (keyMin.compareTo(fromMin) < 0) {
      if (keyMax.compareTo(toMax) >= 0) {
        diff = getGaps(fromIndex, toIndex, true, true);
        diff[0] = new Interval<>(keyMin, fromMin);
        diff[diff.length - 1] = new Interval<>(toMax, keyMax);
      }
      else {
        diff = getGaps(fromIndex, toIndex, true, false);
        if (keyMax.compareTo(toMax) <= 0 && diff.length > 1) {
          final int len1 = diff.length - 1;
          final Interval<T> last = diff[len1];
          if (last.getMax().compareTo(keyMax) > 0)
            diff[len1] = new Interval<>(last.getMin(), keyMax);
        }

        diff[0] = new Interval<>(keyMin, fromMin);
      }
    }
    else if (keyMax.compareTo(toMax) >= 0) {
      diff = getGaps(fromIndex, toIndex, false, true);
      diff[diff.length - 1] = new Interval<>(toMax, keyMax);
    }
    else {
      diff = getGaps(fromIndex, toIndex, false, false);
      if (keyMax.compareTo(toMax) <= 0 && diff.length > 0) {
        final int len1 = diff.length - 1;
        final Interval<T> last = diff[len1];
        if (last.getMax().compareTo(keyMax) > 0)
          diff[len1] = new Interval<>(last.getMin(), keyMax);
      }
    }

    return diff;
  }

  @SuppressWarnings("unchecked")
  private Interval<T>[] getGaps(int fromIndex, final int toIndex, final boolean withSpaceFront, final boolean withSpaceBack) {
    int len = toIndex - fromIndex;
    int i;
    if (withSpaceFront) {
      i = 1;
      ++len;
    }
    else {
      i = 0;
    }

    if (withSpaceBack)
      ++len;

    final Interval<T>[] gaps = new Interval[len];
    for (Interval<T> next, prev = data.get(fromIndex); ++fromIndex <= toIndex; prev = next) { // [RA]
      next = data.get(fromIndex);
      gaps[i++] = new Interval<>(prev.getMax(), next.getMin());
    }

    return gaps;
  }

  /**
   * Returns the number of intervals.
   *
   * @return The number of intervals.
   */
  @Override
  public int size() {
    return data.size();
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof IntervalArraySet && data.equals(((IntervalArraySet<T>)obj).data);
  }

  @Override
  public int hashCode() {
    return data.hashCode() * 31;
  }

  @Override
  public String toString() {
    final int i$ = data.size();
    if (i$ == 0)
      return "[]";

    final StringBuilder b = new StringBuilder();
    for (int i = 0; i < i$;) // [RA]
      b.append(',').append(data.get(i++));

    b.setCharAt(0, '[');
    b.append(']');
    return b.toString();
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean contains(final Object o) {
    return contains((Interval<T>)o);
  }

  @Override
  public boolean contains(final T key) {
    final int size = size();
    int index = CollectionUtil.binaryClosestSearch(data, 0, size, key, i -> i.getMin());
    if (index == size)
      return false;

    Interval<T> i = data.get(index);
    if (key.compareTo(i.getMin()) < 0) {
      if (++index == size)
        return false;

      i = data.get(index + 1);
    }

    return i.contains(key);
  }

  @Override
  public boolean contains(final Interval<T> key) {
    final int size = size();
    int index = CollectionUtil.binaryClosestSearch(data, 0, size, key, minComparator);
    if (index == size)
      return false;

    Interval<T> i = data.get(index);
    if (key.getMin().compareTo(i.getMin()) < 0) {
      if (++index == size)
        return false;

      i = data.get(index + 1);
    }

    return i.contains(key);
  }

  @Override
  public Iterator<Interval<T>> iterator() {
    return data.iterator();
  }

  @Override
  public Object[] toArray() {
    return data.toArray();
  }

  @Override
  public <E>E[] toArray(final E[] a) {
    return data.toArray(a);
  }

  @Override
  public boolean remove(final Interval<T> key) {
    return data.remove(key);
  }

  @Override
  public boolean remove(final Object o) {
    return data.remove(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return CollectionUtil.containsAll(data, c);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    return CollectionUtil.removeAll(data, c);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return CollectionUtil.retainAll(data, c);
  }

  @Override
  public void clear() {
    data.clear();
  }







  @Override
  public Interval<T> lower(final Interval<T> e) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public Interval<T> higher(final Interval<T> e) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public Interval<T> floor(final Interval<T> e) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public Interval<T> ceiling(final Interval<T> e) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public Interval<T> pollFirst() {
    if (isEmpty())
      return null;

    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public Interval<T> pollLast() {
    if (isEmpty())
      return null;

    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> descendingSet() {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<Interval<T>> descendingIterator() {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> subSet(final Interval<T> fromElement, final boolean fromInclusive, final Interval<T> toElement, final boolean toInclusive) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> headSet(final Interval<T> toElement, final boolean inclusive) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> tailSet(final Interval<T> fromElement, final boolean inclusive) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<Interval<T>> subSet(final Interval<T> fromElement, final Interval<T> toElement) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<Interval<T>> headSet(final Interval<T> toElement) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<Interval<T>> tailSet(final Interval<T> fromElement) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public Comparator<? super Interval<T>> comparator() {
    return Interval.COMPARATOR;
  }

  @Override
  public Interval<T> first() {
    if (isEmpty())
      throw new NoSuchElementException();

    return data.get(0);
  }

  @Override
  public Interval<T> last() {
    if (isEmpty())
      throw new NoSuchElementException();

    return data.get(size() - 1);
  }
}