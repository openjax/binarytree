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

public class IntervalArraySet<T> implements IntervalSet<T>, Cloneable {
  private class IntervalArrayIntList extends ArrayList<Interval<T>> {
    @Override
    protected void removeRange(final int fromIndex, final int toIndex) {
      super.removeRange(fromIndex, toIndex);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final Comparator<Interval> minComparator = (final Interval o1, final Interval o2) -> o2.getMax() == null ? -1 : o1.compare(o1.getMin(), o2.getMax());
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static final Comparator<Interval> maxComparator = (final Interval o1, final Interval o2) -> o1.getMax() == null ? o2.getMax() == null ? 0 : 1 : o2.getMax() == null ? -1 : o1.compare(o1.getMax(), o2.getMax());

  @SuppressWarnings("rawtypes")
  private static final Interval[] EMPTY = {};

  private final IntervalArrayIntList data;

  public IntervalArraySet() {
    this.data = new IntervalArrayIntList();
  }

  public IntervalArraySet(final Collection<Interval<T>> c) {
    this();
    addAll(c);
  }

  @SafeVarargs
  public IntervalArraySet(final Interval<T> ... a) {
    this(a, 0, a.length);
  }

  public IntervalArraySet(final Interval<T>[] a, final int fromIndex, final int toIndex) {
    this();
    addAll(a, fromIndex, toIndex);
  }

  private IntervalArraySet(final IntervalArrayIntList data) {
    this.data = data;
  }

  /**
   * Add the interval defined by the provided {@code (x,y)} values to this {@link IntervalArraySet}. If the provided interval overlaps
   * one or more existing interval, the overlapping intervals are merged.
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

    final T keyMin = key.getMin();
    int fromIndex = 0;
    if (keyMin != null) {
      fromIndex = CollectionUtil.binaryClosestSearch(data, 0, size, key, minComparator);
      if (fromIndex == size) {
        final Interval<T> f = data.get(--fromIndex);
        if (key.compare(keyMin, f.getMax()) > 0)
          return data.add(key);
      }
    }

    int c0 = key.compareTo(data.get(fromIndex));
    if (c0 < 0 && fromIndex > 0) {
      final int c1 = key.compareTo(data.get(--fromIndex));
      if (c1 != 2)
        c0 = c1;
      else
        ++fromIndex;
    }

    T min, max;
    if (c0 < 0)
      min = keyMin;
    else
      min = data.get(fromIndex).getMin();

    int toIndex = size;
    max = key.getMax();
    if (max != null) {
      toIndex = CollectionUtil.binaryClosestSearch(data, fromIndex, size, key, maxComparator);
      if (toIndex < size) {
        c0 = key.compareTo(data.get(toIndex));
        if (c0 >= -1) {
          max = data.get(toIndex).getMax();
          ++toIndex;
        }
        else {
          max = key.getMax();
        }
      }
    }

    if (toIndex - fromIndex != 1) {
      data.removeRange(fromIndex, toIndex);
      data.add(fromIndex, key.newInstance(min, max));
    }
    else if (data.get(fromIndex).equals(min, max)) {
      return false;
    }
    else {
      data.set(fromIndex, key.newInstance(min, max));
    }

    return true;
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

  @SuppressWarnings("unchecked")
  public boolean addAll(final Interval<T> ... a) {
    return addAll(a, 0, a.length);
  }

  public boolean addAll(final Interval<T>[] a, int fromIndex, final int toIndex) {
    boolean changed = false;
    while (fromIndex < toIndex)
      changed |= add(a[fromIndex++]);

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

  @Override
  public Interval<T> ceiling(final Interval<T> e) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    data.clear();
  }

  @Override
  @SuppressWarnings("unchecked")
  public IntervalArraySet<T> clone() {
    return new IntervalArraySet<>((IntervalArrayIntList)data.clone());
  }

  @Override
  public Comparator<? super Interval<T>> comparator() {
    return Interval.COMPARATOR;
  }

  @Override
  public boolean contains(final Interval<T> key) {
    final int size = size();
    int index = CollectionUtil.binaryClosestSearch(data, 0, size, key, minComparator);
    if (index == size)
      return false;

    Interval<T> i = data.get(index);
    if (key.compare(key.getMin(), i.getMin()) < 0) {
      if (++index == size)
        return false;

      i = data.get(index + 1);
    }

    return i.contains(key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean contains(final Object o) {
    if (o instanceof Interval)
      return contains((Interval<T>)o);

    final T key = (T)o;
    final int size = size();
    int index;
    if (size == 0 || size == (index = CollectionUtil.binaryClosestSearch(data, 0, size, key, (final Interval<T> i) -> i.getMin(), data.get(0))))
      return false;

    Interval<T> i = data.get(index);
    if (i.compare(key, i.getMin()) < 0) {
      if (++index == size)
        return false;

      i = data.get(index + 1);
    }

    return i.contains(key);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return CollectionUtil.containsAll(data, c);
  }

  @Override
  public Iterator<Interval<T>> descendingIterator() {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> descendingSet() {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

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
    if (fromIndex >= size || key.compare(fromMin, keyMax) >= 0)
      return EMPTY;

    int toIndex = CollectionUtil.binaryClosestSearch(data, fromIndex, size, key, maxComparator);
    if (toIndex == size)
      --toIndex;

    final Interval<T> to = data.get(toIndex);
    final T keyMin = key.getMin();
    final T toMax = to.getMax();
    if (key.compare(toMax, keyMin) < 0)
      return EMPTY;

    if (key.compare(keyMax, fromMin) <= 0 || key.compare(keyMin, toMax) >= 0)
      return new Interval[] {key};

    final Interval<T>[] diff;
    if (key.compare(keyMin, fromMin) < 0) {
      if (key.compare(keyMax, toMax) >= 0) {
        diff = getGaps(key, fromIndex, toIndex, true, true);
        diff[0] = key.newInstance(keyMin, fromMin);
        diff[diff.length - 1] = key.newInstance(toMax, keyMax);
      }
      else {
        diff = getGaps(key, fromIndex, toIndex, true, false);
        if (key.compare(keyMax, toMax) <= 0 && diff.length > 1) {
          final int len1 = diff.length - 1;
          final Interval<T> last = diff[len1];
          if (key.compare(last.getMax(), keyMax) > 0)
            diff[len1] = key.newInstance(last.getMin(), keyMax);
        }

        diff[0] = key.newInstance(keyMin, fromMin);
      }
    }
    else if (key.compare(keyMax, toMax) >= 0) {
      diff = getGaps(key, fromIndex, toIndex, false, true);
      diff[diff.length - 1] = key.newInstance(toMax, keyMax);
    }
    else {
      diff = getGaps(key, fromIndex, toIndex, false, false);
      if (key.compare(keyMax, toMax) <= 0 && diff.length > 0) {
        final int len1 = diff.length - 1;
        final Interval<T> last = diff[len1];
        if (key.compare(last.getMax(), keyMax) > 0)
          diff[len1] = key.newInstance(last.getMin(), keyMax);
      }
    }

    return diff;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof IntervalArraySet && data.equals(((IntervalArraySet<T>)obj).data);
  }

  @Override
  public Interval<T> first() {
    if (isEmpty())
      throw new NoSuchElementException();

    return data.get(0);
  }

  @Override
  public Interval<T> floor(final Interval<T> e) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  private Interval<T>[] getGaps(final Interval<T> key, int fromIndex, final int toIndex, final boolean withSpaceFront, final boolean withSpaceBack) {
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
      gaps[i++] = key.newInstance(prev.getMax(), next.getMin());
    }

    return gaps;
  }

  @Override
  public int hashCode() {
    return data.hashCode() * 31;
  }

  @Override
  public SortedSet<Interval<T>> headSet(final Interval<T> toElement) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> headSet(final Interval<T> toElement, final boolean inclusive) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public Interval<T> higher(final Interval<T> e) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

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

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public Iterator<Interval<T>> iterator() {
    return data.iterator();
  }

  @Override
  public Interval<T> last() {
    if (isEmpty())
      throw new NoSuchElementException();

    return data.get(size() - 1);
  }

  @Override
  public Interval<T> lower(final Interval<T> e) {
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
  public boolean remove(final Interval<T> key) {
    return data.remove(key);
  }

  @Override
  public boolean remove(final Object o) {
    return data.remove(o);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    return CollectionUtil.removeAll(data, c);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return CollectionUtil.retainAll(data, c);
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
  public NavigableSet<Interval<T>> subSet(final Interval<T> fromElement, final boolean fromInclusive, final Interval<T> toElement, final boolean toInclusive) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<Interval<T>> subSet(final Interval<T> fromElement, final Interval<T> toElement) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<Interval<T>> tailSet(final Interval<T> fromElement) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> tailSet(final Interval<T> fromElement, final boolean inclusive) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray() {
    return data.toArray();
  }

  @Override
  public <E> E[] toArray(final E[] a) {
    return data.toArray(a);
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
}