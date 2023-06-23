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

import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.libj.util.Interval;
import org.libj.util.Iterators;

/**
 * Concurrent implementation of {@link IntervalTreeSet}.
 *
 * @param <T> The type parameter of values defining the coordinates of the {@link Interval}s belonging to this set.
 */
public class ConcurrentIntervalTreeSet<T> extends IntervalTreeSet<T> {
  protected class ConcurrentBinaryTreeIterator extends BinaryTreeIterator {
    ConcurrentBinaryTreeIterator(final IntervalNode root) {
      super(root);
    }

    @Override
    public Interval<T> next() {
      reading.lock();
      try {
        return super.next();
      }
      finally {
        reading.unlock();
      }
    }

    @Override
    public void remove() {
      writing.lock();
      try {
        super.remove();
      }
      finally {
        writing.unlock();
      }
    }
  }

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock reading = lock.readLock();
  private final Lock writing = lock.writeLock();

  /**
   * Creates a new {@link ConcurrentIntervalTreeSet} that is empty.
   */
  public ConcurrentIntervalTreeSet() {
  }

  /**
   * Creates a new {@link ConcurrentIntervalTreeSet} and calls {@link #add(Interval)} on each member of the provided
   * {@link Collection}.
   *
   * @param c The {@link Collection} with the {@link Interval}s to {@linkplain #add(Interval) add}.
   * @throws NullPointerException If the provided {@link Collection}, or any member of the provided {@link Collection} is null.
   */
  public ConcurrentIntervalTreeSet(final Collection<Interval<T>> c) {
    super(c);
  }

  /**
   * Creates a new {@link ConcurrentIntervalTreeSet} and calls {@link #add(Interval)} on each member of the provided array.
   *
   * @param a The array of {@link Interval}s to {@linkplain #add(Interval) add}.
   * @throws NullPointerException If the provided array, or any member of the provided array is null.
   */
  @SafeVarargs
  public ConcurrentIntervalTreeSet(final Interval<T> ... a) {
    super(a);
  }

  /**
   * Creates a new {@link ConcurrentIntervalTreeSet} and calls {@link #add(Interval)} on the members of the provided array between
   * the specified {@code fromIndex} and {@code toIndex} values.
   *
   * @param a The array of {@link Interval}s to {@linkplain #add(Interval) add}.
   * @param fromIndex The index of the first {@link Interval} (inclusive) to be added.
   * @param toIndex The index of the last {@link Interval} (exclusive) to be added.
   * @throws ArrayIndexOutOfBoundsException If the a value between {@code fromIndex} (inclusive) to {@code toIndex} (exclusive) is
   *           greater than the length of the provided array.
   * @throws NullPointerException If the provided array, or any member of the provided array is null.
   */
  public ConcurrentIntervalTreeSet(final Interval<T>[] a, final int fromIndex, final int toIndex) {
    super(a, fromIndex, toIndex);
  }

  @Override
  public boolean add(final Interval<T> key) {
    writing.lock();
    try {
      return super.add(key);
    }
    finally {
      writing.unlock();
    }
  }

  @Override
  public boolean addAll(final Collection<? extends Interval<T>> c) {
    writing.lock();
    try {
      return super.addAll(c);
    }
    finally {
      writing.unlock();
    }
  }

  @Override
  public boolean addAll(final Interval<T>[] a, final int fromIndex, final int toIndex) {
    writing.lock();
    try {
      return super.addAll(a, fromIndex, toIndex);
    }
    finally {
      writing.unlock();
    }
  }

  @Override
  public Interval<T> ceiling(final Interval<T> e) {
    reading.lock();
    try {
      return super.ceiling(e);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  public void clear() {
    writing.lock();
    try {
      super.clear();
    }
    finally {
      writing.unlock();
    }
  }

  @Override
  public IntervalTreeSet<T> clone() {
    reading.lock();
    try {
      return super.clone();
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  protected Node searchNode(final Interval<T> key) {
    reading.lock();
    try {
      return super.searchNodeFast(key);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    final int size = c.size();
    if (size == 0)
      return true;

    reading.lock();
    try {
      return super.containsAll(c, size);
    }
    finally {
      reading.unlock();
    }
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
  public Interval<T>[] difference(final Interval<T> key) {
    reading.lock();
    try {
      return super.difference(key);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  protected boolean equals(final BinaryTree<?> tree) {
    reading.lock();
    try {
      return super.equals(tree);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  public Interval<T> floor(final Interval<T> e) {
    reading.lock();
    try {
      return super.floor(e);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  public void forEach(final Consumer<? super Interval<T>> action) {
    final IntervalNode root = getRoot();
    if (root == null)
      return;

    final BinaryTreeIterator i = new ConcurrentBinaryTreeIterator(root);
    while (i.hasNext())
      action.accept(i.next());
  }

  @Override
  protected int hashCode(final Node node) {
    reading.lock();
    try {
      return super.hashCode(node);
    }
    finally {
      reading.unlock();
    }
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
  protected Interval<T> higher(final Node node) {
    reading.lock();
    try {
      return super.higher(node);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  protected boolean intersects(final IntervalNode root, final Interval<T> key) {
    reading.lock();
    try {
      return super.intersects(root, key);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  public Iterator<Interval<T>> iterator() {
    final IntervalNode root = getRoot();
    return root == null ? Iterators.empty() : new ConcurrentBinaryTreeIterator(root);
  }

  @Override
  protected Interval<T> lower(final Node node) {
    reading.lock();
    try {
      return super.lower(node);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  protected Interval<T> pollFirst(final IntervalNode root) {
    reading.lock();
    try {
      return super.pollFirst(root);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  protected Interval<T> pollLast(final IntervalNode root) {
    reading.lock();
    try {
      return super.pollLast(root);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  protected boolean remove(final IntervalNode root, final Interval<T> key) {
    writing.lock();
    try {
      return super.remove(root, key);
    }
    finally {
      writing.unlock();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean remove(final Object o) {
    writing.lock();
    try {
      return super.delete((Interval<T>)o);
    }
    finally {
      writing.unlock();
    }
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    writing.lock();
    try {
      return super.removeAll(c);
    }
    finally {
      writing.unlock();
    }
  }

  @Override
  public boolean removeIf(final Predicate<? super Interval<T>> filter) {
    reading.lock();
    try {
      final IntervalNode root = getRoot();
      if (root == null)
        return false;

      boolean removed = false;
      final BinaryTreeIterator i = new BinaryTreeIterator(root);
      while (i.hasNext()) {
        if (filter.test(i.next())) {
          reading.unlock();
          writing.lock();
          try {
            i.remove();
          }
          finally {
            writing.unlock();
            reading.lock();
          }

          removed = true;
        }
      }

      return removed;
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    writing.lock();
    try {
      return super.retainAll(c);
    }
    finally {
      writing.unlock();
    }
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
  protected Object[] toArray(final Node node) {
    reading.lock();
    try {
      return super.toArray(node);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  protected <E>E[] toArray(final Node node, final E[] a) {
    reading.lock();
    try {
      return super.toArray(node, a);
    }
    finally {
      reading.unlock();
    }
  }

  @Override
  protected StringBuilder toString(final Node root) {
    reading.lock();
    try {
      return super.toString(root);
    }
    finally {
      reading.unlock();
    }
  }
}