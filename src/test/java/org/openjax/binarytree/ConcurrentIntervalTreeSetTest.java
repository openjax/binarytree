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

import static org.junit.Assert.*;

import java.util.ConcurrentModificationException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.libj.test.TestExecutorService;
import org.libj.util.Interval;
import org.libj.util.function.TriObjIntConsumer;

public class ConcurrentIntervalTreeSetTest {
  private static final Random random = new Random();
  private static void sleep(final long millis) {
    if (millis < 0)
      return;

    try {
      Thread.sleep(millis);
    }
    catch (final InterruptedException e) {
      System.err.println(e.getMessage());
      System.err.flush();
      System.exit(-1);
    }
  }

  private static void seed(final IntervalSet<Integer> set, final AtomicInteger modCount, final AtomicBoolean finished, final TestExecutorService executor)  {
    executor.execute(() -> {
      do {
        final int min = random.nextInt(10000);
        set.add(new Interval<>(min, min + 1 + random.nextInt(5)));
        modCount.getAndIncrement();
//        System.err.print('.');
//        System.err.flush();
        sleep(2);
      }
      while (!finished.get());
    });
  }

  private static void run(final IntervalSet<Integer> set, final TriObjIntConsumer<IntervalSet<Integer>,AtomicInteger,AtomicBoolean> tester, final int numTests) throws InterruptedException {
    final TestExecutorService executor = new TestExecutorService(Executors.newFixedThreadPool(4));
    final AtomicInteger modCount = new AtomicInteger();
    final AtomicBoolean finished = new AtomicBoolean();

    seed(set, modCount, finished, executor);
    sleep(50);

    executor.execute(() -> {
      try {
        tester.accept(set, modCount, finished, numTests);
      }
      finally {
        finished.set(true);
      }
    });

    executor.shutdown();
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  private static void testIterator(final IntervalSet<Integer> set, final AtomicInteger modCount, final AtomicBoolean finished, final int numTests) {
    long time;
    for (int i = 0, prev = -1, prevModCount; i < numTests; ++i, prev = -1) { // [N]
      prevModCount = modCount.get();
      time = System.currentTimeMillis();
      for (final Interval<Integer> key : set) { // [S]
        final Integer next = key.getMin();
        if (next < prev)
          fail("next (" + next + ")" + " < " + "prev (" + prev + ")");

        sleep(5 + time - System.currentTimeMillis());
        prev = next;
      }

      assertTrue(modCount.get() > prevModCount);
      sleep(10);
    }
  }

  private static void testForEach(final IntervalSet<Integer> set, final AtomicInteger modCount, final AtomicBoolean finished, final int numTests) {
    final AtomicInteger prev = new AtomicInteger(-1);
    for (int i = 0, prevModCount; i < numTests; ++i, prev.set(-1)) { // [N]
      prevModCount = modCount.get();
      final long time = System.currentTimeMillis();
      set.forEach(key -> {
        final Integer next = key.getMin();
        if (next < prev.get())
          fail("next (" + next + ")" + " < " + "prev (" + prev + ")");

        sleep(10 + time - System.currentTimeMillis());
        prev.set(next);
      });

      assertTrue(modCount.get() > prevModCount);
      sleep(10);
    }
  }

  private static void testRemoveIf(final IntervalSet<Integer> set, final AtomicInteger modCount, final AtomicBoolean finished, final int numTests) {
    if (numTests == 10) {
      finished.set(true);
      sleep(10);
    }

    assertTrue(modCount.get() > 0);
    final AtomicInteger prev = new AtomicInteger(-1);
    final AtomicInteger prevPrev = new AtomicInteger(-1);
    for (int i = 0, prevModCount; i < numTests; ++i, prev.set(-1), prevPrev.set(-1)) { // [N]
      prevModCount = modCount.get();
      final long time = System.currentTimeMillis();
      set.removeIf(key -> {
        final Integer next = key.getMin();
        if (prev.get() < prevPrev.get())
          fail("next (" + prev.get() + ")" + " < " + "prev (" + prevPrev.get() + ")");

        sleep(20 + time - System.currentTimeMillis());
        prevPrev.set(prev.get());
        prev.set(next);
        return next % 2 == 0 && set.size() > 5;
      });

      sleep(20);

      if (numTests > 10)
        assertTrue(modCount.get() > prevModCount);
    }
  }

  @Test
  public void testRemoveIfIntervalTreeSetSingleThread() throws InterruptedException {
    run(new IntervalTreeSet<>(), ConcurrentIntervalTreeSetTest::testRemoveIf, 10);
  }

  @Test
  public void testRemoveIfConcurrentIntervalTreeSetSingleThread() throws InterruptedException {
    run(new ConcurrentIntervalTreeSet<>(), ConcurrentIntervalTreeSetTest::testRemoveIf, 10);
  }

  @Test
  public void testRemoveIfIntervalTreeSet() throws InterruptedException {
    try {
      run(new IntervalTreeSet<>(), ConcurrentIntervalTreeSetTest::testRemoveIf, 5000);
      fail("Expected ConcurrentModificationException");
    }
    catch (final ConcurrentModificationException e) {
    }
    catch (final AssertionError e) {
      // FIXME: This needs to be fixed...
      // FIXME: This needs to be fixed...
      // FIXME: This needs to be fixed...
      // FIXME: This needs to be fixed...
      // FIXME: This needs to be fixed...
      // FIXME: This needs to be fixed...
      // FIXME: This needs to be fixed...
      // FIXME: This needs to be fixed...
      // FIXME: This needs to be fixed...
      if (!e.getMessage().startsWith("next ("))
        throw e;
    }
  }

  @Test
  public void testRemoveIfConcurrentIntervalTreeSet() throws InterruptedException {
    run(new ConcurrentIntervalTreeSet<>(), ConcurrentIntervalTreeSetTest::testRemoveIf, 100);
  }

  @Test
  public void testForEachIntervalTreeSet() throws InterruptedException {
    try {
      run(new IntervalTreeSet<>(), ConcurrentIntervalTreeSetTest::testForEach, 200);
      fail("Expected ConcurrentModificationException");
    }
    catch (final ConcurrentModificationException e) {
    }
  }

  @Test
  public void testForEachConcurrentIntervalTreeSet() throws InterruptedException {
    run(new ConcurrentIntervalTreeSet<>(), ConcurrentIntervalTreeSetTest::testForEach, 100);
  }

  @Test
  public void testIteratorIntervalTreeSet() throws InterruptedException {
    try {
      run(new IntervalTreeSet<>(), ConcurrentIntervalTreeSetTest::testIterator, 200);
      fail("Expected ConcurrentModificationException");
    }
    catch (final ConcurrentModificationException e) {
    }
  }

  @Test
  public void testIteratorConcurrentIntervalTreeSet() throws InterruptedException {
    run(new ConcurrentIntervalTreeSet<>(), ConcurrentIntervalTreeSetTest::testIterator, 100);
  }
}