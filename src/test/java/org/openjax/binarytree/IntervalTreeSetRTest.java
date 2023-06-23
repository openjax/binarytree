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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.libj.console.Ansi;
import org.libj.console.Ansi.Color;
import org.libj.console.Ansi.Intensity;
import org.libj.util.CollectionUtil;
import org.libj.util.CombinationIterator;
import org.libj.util.Interval;
import org.libj.util.PermutationIterator;

public class IntervalTreeSetRTest extends BinarySearchTreeTest<IntervalTreeSet<Integer>,Interval<Integer>> {
  private static final Interval<Integer> MIN_VALUE = new Interval<>(Integer.MIN_VALUE, Integer.MIN_VALUE + 1);
  private static final Interval<Integer> MAX_VALUE = new Interval<>(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);

  private static final int TEST_TREE_MIN_SIZE = 1;
  private static final int TEST_TREE_MAX_SIZE = 1000;

  @Override
  void assertHasKeysInGivenOrderAndAscending(final IntervalTreeSet<Integer> tree, final Collection<Interval<Integer>> keys, final Supplier<String> onError) {
    super.assertHasKeysInGivenOrderAndAscending(tree, new IntervalArraySet<>(keys), onError);
  }

  @Override
  BinaryTree<Interval<Integer>>.Node assertNodeWithKeyIsPresent(final IntervalTreeSet<Integer> tree, final Interval<Integer> key, final Supplier<String> onError) {
    final Integer keyMin = key.getMin();
    final Integer keyMax = key.getMax();
    final BinaryTree<Interval<Integer>>.Node node = tree.searchNode(key);
    assertNotNull(node, onError);
    assertTrue(node.getKey().contains(key), onError);
    assertTrue(tree.contains(key), onError);
    assertTrue(tree.contains(keyMin), onError);
    assertTrue(tree.contains(keyMin + (keyMax - keyMin) / 2), onError);
    if (keyMax.equals(tree.last().getMax()))
      assertFalse(tree.contains(keyMax), onError);
    else
      assertTrue(tree.contains(keyMax - 1), onError);

    return node;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<Interval<Integer>> type() {
    return (Class<Interval<Integer>>)(Class<?>)Interval.class;
  }

  @Override
  public Interval<Integer> minValue() {
    return MIN_VALUE;
  }

  @Override
  public Interval<Integer> maxValue() {
    return MAX_VALUE;
  }

  @Override
  public Interval<Integer> prevValue(final Interval<Integer> value) {
    return new Interval<>(value.getMin() - 1, value.getMax() - 1);
  }

  @Override
  public Interval<Integer> nextValue(final Interval<Integer> value) {
    final Integer min = value.getMax() + 1;
    return new Interval<>(min, min + value.getMax() - value.getMin());
  }

  @Override
  boolean supportsMerging() {
    return true;
  }

  @Override
  ArrayList<Interval<Integer>> createOrderedSequenceOfKeys() {
    final int count = random.nextInt(TEST_TREE_MIN_SIZE, TEST_TREE_MAX_SIZE);
    final ArrayList<Interval<Integer>> keys = new ArrayList<>(count);
    for (int i = 0, min = 0; i < count; ++i) // [RA]
      keys.add(new Interval<>(min += random.nextInt(100), random.nextInt(min + 1, min + 100)));

    return keys;
  }

  @Override
  IntervalTreeSet<Integer> createTree() {
    return new ConcurrentIntervalTreeSet<>();
  }

  @Override
  void assertSpecificTreeInvariants(final IntervalTreeSet<Integer> tree, final Supplier<String> onError) {
    final IntervalTreeSet<Integer>.IntervalNode root = tree.getRoot();
    AvlTreeTest.validateAVLInvariant(root, onError);
    assertMinMaxSetCorrectly(root, onError);
  }

  static Interval<Integer> assertMinMaxSetCorrectly(final IntervalTreeSet<Integer>.IntervalNode node, final Supplier<String> onError) {
    if (node == null)
      return null;

    final Interval<Integer> leftMinMax = assertMinMaxSetCorrectly(node.getLeft(), onError);
    final Interval<Integer> rightMinMax = assertMinMaxSetCorrectly(node.getRight(), onError);

    final Integer min = leftMinMax != null ? leftMinMax.getMin() : node.getKey().getMin();
    final Integer max = rightMinMax != null ? rightMinMax.getMax() : node.getKey().getMax();

    final Integer minNode = node.getMinNode().getKey().getMin();
    final Integer maxNode = node.getMaxNode().getKey().getMax();

    assertEquals(min, minNode, onError);
    assertEquals(max, maxNode, onError);

    return new Interval<>(min, max);
  }

  private static ArrayList<Interval<Integer>> createRandomIntervalList(final int count, final int intervalMax) {
    final ArrayList<Interval<Integer>> intervals = new ArrayList<>(count);
    for (int i = 0; i < count; ++i) { // [RA]
      final int min = random.nextInt(intervalMax - 1);
      final int max = random.nextInt(min + 1, intervalMax);
      intervals.add(new Interval<>(min, max));
    }

    return intervals;
  }

  private static void log(final String methodName, final int range, final long[] times) {
    final long time0 = times[0] / 1000000;
    final long time1 = times[1] / 1000000;
    final Color c;
    final String s;
    if (time0 <= time1) {
      c = Color.RED;
      s = "-";
    }
    else {
      c = Color.GREEN;
      s = "+";
    }

    System.err.println(String.format("%s(%6d) IntervalList: %4dms", methodName, range, time0));
    System.err.println(String.format("%s(%6d) IntervalTree: %4dms", methodName, range, time1) + " " + Ansi.apply(s, Intensity.BOLD, c));
  }

  @Test
  public final void testEmptyTreeEchoDifference() {
    final IntervalTreeSet<Integer> tree = createTree();
    test(onError -> {
      assertSpecificTreeInvariants(tree, onError);

      final Interval<Integer> i = new Interval<>(1, 2);
      final Interval<Integer>[] d = tree.difference(i);
      assertEquals(1, d.length, onError);
      assertEquals(i, d[0], onError);
    });
  }

  @Override
  void assertNextIterator(final IntervalTreeSet<Integer> tree, final Interval<Integer> prev, final Interval<Integer> next, final Iterator<Interval<Integer>> iterator, final Supplier<String> onError) {
    super.assertNextIterator(tree, prev, next, iterator, onError);

    if (prev != null) {
      assertEquals(next, tree.higher(prev), onError);
      assertEquals(next, tree.ceiling(next), onError);
      if (next != null) {
        final Interval<Integer> i = new Interval<>((prev.getMin() + next.getMin()) / 2, (prev.getMax() + next.getMax()) / 2);
        assertEquals(next, tree.ceiling(i), onError);
      }
    }
    else {
      assertThrows(NullPointerException.class, () -> tree.higher(prev), onError);
      assertThrows(NullPointerException.class, () -> tree.ceiling(prev), onError);
    }

    if (next != null) {
      assertEquals(prev, tree.lower(next), onError);
      assertEquals(next, tree.floor(next), onError);
      if (prev != null) {
        final Interval<Integer> i = new Interval<>((prev.getMin() + next.getMin()) / 2, (prev.getMax() + next.getMax()) / 2);
        assertEquals(prev, tree.floor(i), onError);
      }
    }
    else {
      assertThrows(NullPointerException.class, () -> tree.lower(next), onError);
      assertThrows(NullPointerException.class, () -> tree.floor(next), onError);
    }
  }

  @Test
  public void testDifferenceRemove() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final IntervalTreeSet<Integer> tree = createTree();
      tree.addAll(createOrderedSequenceOfKeys());
      final int size = tree.size();
      if (size < 4)
        continue;

      final Interval<Integer>[] datas = tree.toArray(new Interval[size]);

      final int fromIndex = random.nextInt(1, size - 2);
      final int toIndex = random.nextInt(fromIndex, size - 1);

      final Interval<Integer> from = datas[fromIndex];
      final Interval<Integer> to = datas[toIndex];

      final Integer fromMin = from.getMin();
      final Integer fromMax = from.getMax();
      final Integer toMin = to.getMin();
      final Integer toMax = to.getMax();

      final Integer beforeMinFrom = (datas[fromIndex - 1].getMax() + fromMin) / 2;
      final Integer onMinFrom = fromMin;
      final Integer insideFrom = (fromMin + fromMax) / 2;
      final Integer onMaxFrom = fromMax;
      final Integer afterMaxFrom = (fromMax + datas[fromIndex + 1].getMin()) / 2;

      final Integer beforeMinTo = (datas[toIndex - 1].getMax() + toMin) / 2;
      final Integer onMinTo = toMin;
      final Integer insideTo = (toMin + toMax) / 2;
      final Integer onMaxTo = toMax;
      final Integer afterMaxTo = (toMax + datas[toIndex + 1].getMin()) / 2;

      final CombinationIterator<Integer> combinations = new CombinationIterator<>(Arrays.asList(beforeMinFrom, onMinFrom, insideFrom, onMaxFrom, afterMaxFrom), Arrays.asList(beforeMinTo, onMinTo, insideTo, onMaxTo, afterMaxTo));
      while (combinations.hasNext()) {
        final Integer[] combination = combinations.next();
        if (combination[0] >= combination[1])
          continue;

        test(onError -> {
          final Interval<Integer> range = new Interval<>(combination[0], combination[1]);
          final Integer rangeMin = range.getMin();
          final Integer rangeMax = range.getMax();

          final Interval<Integer>[] diffs = tree.difference(range);
          final int len = diffs.length;
          if (len == 0) {
            final String str = tree.toString();
            final IntervalTreeSet<Integer> clone = tree.clone();
            assertTrue(clone.delete(range), onError);
            assertEquals(str, tree.toString(), onError);
            assertSpecificTreeInvariants(clone, onError);
          }
          else {
            int i = fromIndex, j = 0;
            final ArrayList<Interval<Integer>> seq = new ArrayList<>();
            if (rangeMin == insideFrom || rangeMin == onMinFrom || rangeMin == onMaxFrom || rangeMin == afterMaxFrom)
              seq.add(datas[i++]);

            Interval<Integer> diff, data;
            seq.add(diff = diffs[j++]);
            seq.add(data = datas[i++]);

            assertEquals(diff.getMin(), rangeMin.equals(insideFrom) || rangeMin.equals(onMinFrom) ? fromMax : rangeMin, onError);
            if (j < len) {
              assertEquals(diff.getMax(), data.getMin(), onError);
              do {
                seq.add(diff = diffs[j++]);
                assertEquals(diff.getMin(), data.getMax(), onError);
                seq.add(data = datas[i++]);

                if (j == len)
                  break;

                assertEquals(diff.getMax(), data.getMin(), onError);
              }
              while (true);
            }
            assertEquals(diff.getMax(), rangeMax.equals(insideTo) || rangeMax.equals(onMaxTo) || data.getMin() <= rangeMax && rangeMax <= data.getMax() ? data.getMin() : rangeMax, onError);

            // Assert that each value between range.min and range.max is contained in the `seq` list
            int cursor = range.getMin();
            for (int l = 0; cursor < rangeMax;) { // [N]
              final Interval<Integer> interval = seq.get(l);
              if (interval.contains(cursor))
                ++cursor;
              else if (++l == seq.size())
                fail(onError);
            }

            assertEquals(range.getMax(), cursor, onError);
          }
        });
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPollFirst() {
    for (int r = 0; r < repeat / 100; ++r) { // [N]
      for (int c = 2, m = 4, f = 8; c <= 65536; c += f += 1, m = c * 2) { // [N]
        final IntervalTreeSet<Integer> tree = createTree();
        tree.addAll(createRandomIntervalList(c, m));
        test(onError -> {
          final IntervalTreeSet<Integer> clone = tree.clone();
          int size = clone.size();
          final ArrayList<Interval<Integer>> keys = CollectionUtil.asCollection(new ArrayList<>(), tree.toArray(new Interval[size]));
          final Iterator<Interval<Integer>> keyIterator = keys.iterator();
          for (int i = 0; keyIterator.hasNext(); ++i) { //[I]
            final Interval<Integer> keyToDelete = keyIterator.next();
            final Interval<Integer> data = clone.pollFirst();
            assertEquals(keyToDelete, data, onError);
            keyIterator.remove();
            assertEquals(--size, clone.size(), onError);
            if (i % 10 == 0) {
              assertValid(clone, onError);
              assertHasKeysInGivenOrderAndAscending(clone, keys, onError);
            }
          }

          assertTrue(clone.isEmpty());
        });
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPollLast() {
    for (int r = 0; r < repeat / 100; ++r) { // [N]
      for (int c = 2, m = 4, f = 8; c <= 65536; c += f += 1, m = c * 2) { // [N]
        final IntervalTreeSet<Integer> tree = createTree();
        tree.addAll(createRandomIntervalList(c, m));
        test(onError -> {
          final IntervalTreeSet<Integer> clone = tree.clone();
          int size = clone.size();
          final ArrayList<Interval<Integer>> keys = CollectionUtil.asCollection(new ArrayList<>(), tree.toArray(new Interval[size]));
          final ListIterator<Interval<Integer>> keyIterator = keys.listIterator(size);
          for (int i = 0; keyIterator.hasPrevious(); ++i) { //[I]
            final Interval<Integer> keyToDelete = keyIterator.previous();
            final Interval<Integer> data = clone.pollLast();
            assertEquals(keyToDelete, data, onError);
            keyIterator.remove();
            assertEquals(--size, clone.size(), onError);
            if (i % 10 == 0) {
              assertValid(clone, onError);
              assertHasKeysInGivenOrderAndAscending(clone, keys, onError);
            }
          }

          assertTrue(clone.isEmpty());
        });
      }
    }
  }

  @Test
  public void testIntersects() {
    for (int r = 0; r < repeat / 1000; ++r) { // [N]
      for (int c = 2, m = 4, f = 8; c <= 65536; c += f += 1, m = c * 2) { // [N]
        final ArrayList<Interval<Integer>> keys = createRandomIntervalList(c, m);
        final IntervalSet<Integer> list = new IntervalArraySet<>(keys);
        final IntervalSet<Integer> tree = createTree();
        tree.addAll(keys);

        final int size = tree.size();
        final Interval<Integer>[] array = tree.toArray(new Interval[size]);

        test(onError -> {
          Interval<Integer> data = array[0];
          final Interval<Integer> beforeMin = new Interval<>(data.getMin() - 2, data.getMin() - 1);

          assertFalse(list.intersects(beforeMin), onError);
          assertFalse(tree.intersects(beforeMin), onError);

          data = array[array.length - 1];
          final Interval<Integer> afterMax = new Interval<>(data.getMax() + 1, data.getMax() + 2);

          assertFalse(list.intersects(afterMax), onError);
          assertFalse(tree.intersects(afterMax), onError);

          Interval<Integer> prev = null;
          Integer dataMin, dataMax;
          for (int i = 0; i < size; ++i, prev = data) { // [A]
            data = array[i];
            dataMin = data.getMin();
            dataMax = data.getMax();

            final Integer prevMin = dataMin - 1;
            final Integer prevMax = dataMax - 1;
            final Integer nextMin = dataMin + 1;
            final Integer nextMax = dataMax + 1;

            final Interval<Integer> onLeft = new Interval<>(prevMin, nextMin);
            final Interval<Integer> match = new Interval<>(dataMin, dataMax);
            final Interval<Integer> onRight = new Interval<>(prevMax, nextMax);

            final Interval<Integer> beforeLeft = new Interval<>(prevMin - 1, prevMin);
            // It may happen that the generated data has `beforeLeft` intersect with `prev`
            if (i == 0 || !beforeLeft.intersects(prev)) {
              assertFalse(list.intersects(beforeLeft), onError);
              assertFalse(tree.intersects(beforeLeft), onError);
            }

            assertTrue(list.intersects(onLeft), onError);
            assertTrue(tree.intersects(onLeft), onError);

            if (dataMin < prevMax) {
              final Interval<Integer> inLeft = new Interval<>(dataMin, prevMax);
              assertTrue(list.intersects(inLeft), onError);
              assertTrue(tree.intersects(inLeft), onError);
            }

            assertTrue(list.intersects(match), onError);
            assertTrue(tree.intersects(match), onError);

            if (nextMin < dataMax) {
              final Interval<Integer> inRight = new Interval<>(nextMin, dataMax);
              assertTrue(list.intersects(inRight), onError);
              assertTrue(tree.intersects(inRight), onError);
            }

            assertTrue(list.intersects(onRight), onError);
            assertTrue(tree.intersects(onRight), onError);

            final Interval<Integer> afterRight = new Interval<>(nextMax, nextMax + 1);
            // It may happen that the generated data has `afterRight` intersect with `array[i + 1]`
            if (i == size - 1 || !afterRight.intersects(array[i + 1])) {
              assertFalse(list.intersects(afterRight), onError);
              assertFalse(tree.intersects(afterRight), onError);
            }
          }
        });
      }
    }
  }

  @Test
  public void testRandom() {
    final int warmup = 10;
    for (int c = 16, m = 4, f = 32; c <= 2048; c += f += 1, m = c * 2) { // [N]
      final long[] times = {0, 0};
      for (int r = repeat, w = warmup; r > 0; --r, --w) { // [N]
        test(createRandomIntervalList(c, m), times);
        if (w > 0)
          times[0] = times[1] = 0;
      }

      log("testRandom", m, times);
    }
  }

  @Test
  public void testDeterministic() {
    final int range = 10;
    final long[] times = {0, 0};
    for (int m1 = 1; m1 < 5; m1 += 2) // [N]
      for (int m2 = 1; m2 < range; m2 += 2) // [N]
        permute(range, m1, m2, times);

    log("testDeterministic", range, times);
  }

  @SuppressWarnings("unchecked")
  private void permute(final int maxIntervals, final int m1, final int m2, final long[] times) {
    final Interval<Integer>[] intervals = new Interval[maxIntervals];
    for (int i = 0, j = 0; i < maxIntervals; j = i * 2 + i % m2) { // [N]
      intervals[i] = new Interval<>(j, j * (i % m1 + 1) + i % m2 + 1);
      for (final PermutationIterator<Interval<Integer>> iterator = new PermutationIterator<>(intervals, 0, ++i); iterator.hasNext(); test(iterator.next(), times)); // [X]
    }
  }

  private void test(final ArrayList<Interval<Integer>> keys, final long[] times) {
    test(onError -> {
      final boolean[] added = new boolean[keys.size()];
      final IntervalArraySet<Integer> array = test(keys, new IntervalArraySet<Integer>(), added, true, times, 0, onError);
      final IntervalTreeSet<Integer> tree = test(keys, createTree(), added, false, times, 1, onError);

      assertEquals(array.size(), tree.size(), onError);
      assertEquals(array.toString(), tree.toString(), onError(onError, () -> keys.toString()));
      assertSpecificTreeInvariants(tree, onError);
    });
  }

  private static <T extends IntervalSet<Integer>>T test(final ArrayList<Interval<Integer>> intervals, final T impl, final boolean[] added, final boolean setOrAssert, final long[] times, final int index, final Supplier<String> onError) {
    long time = 0;
    long ts;
    for (int i = 0, i$ = intervals.size(); i < i$; ++i) { // [RA]
      final Interval<Integer> interval = intervals.get(i);

      ts = System.nanoTime();
      boolean add = impl.add(interval);
      time += System.nanoTime() - ts;

      if (setOrAssert)
        added[i] = add;
      else
        assertEquals(added[i], add, onError);

      ts = System.nanoTime();
      add = impl.add(interval);
      time += System.nanoTime() - ts;

      assertFalse(add, onError);
    }

    times[index] += time;
    return impl;
  }
}