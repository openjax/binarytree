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
import org.libj.util.function.BiObjIntConsumer;

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
        sleep(2);
      }
      while (!finished.get());
    });
  }

  private static void run(final IntervalSet<Integer> set, final BiObjIntConsumer<IntervalSet<Integer>,AtomicInteger> tester, final int numTests) throws InterruptedException {
    final TestExecutorService executor = new TestExecutorService(Executors.newFixedThreadPool(4));
    final AtomicInteger modCount = new AtomicInteger();
    final AtomicBoolean finished = new AtomicBoolean();

    seed(set, modCount, finished, executor);

    sleep(50);

    executor.execute(() -> {
      try {
        tester.accept(set, modCount, numTests);
      }
      finally {
        finished.set(true);
      }
    });

    executor.shutdown();
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  private static void testIterator(final IntervalSet<Integer> set, final AtomicInteger modCount, final int numTests) {
    long time;
    for (int i = 0, prev = -1, size; i < numTests; ++i, prev = -1) { // [N]
      size = modCount.get();
      time = System.currentTimeMillis();
      for (final Interval<Integer> key : set) { // [S]
        final Integer next = key.getMin();
        if (next < prev)
          fail("next (" + next + ")" + " < " + "prev (" + prev + ")");

        sleep(5 + time - System.currentTimeMillis());
        prev = next;
      }

      assertTrue(modCount.get() > size);
      sleep(10);
    }
  }

  private static void testForEach(final IntervalSet<Integer> set, final AtomicInteger modCount, final int numTests) {
    final AtomicInteger prev = new AtomicInteger(-1);
    for (int i = 0, size; i < numTests; ++i, prev.set(-1)) { // [N]
      size = modCount.get();
      final long time = System.currentTimeMillis();
      set.forEach(key -> {
        final Integer next = key.getMin();
        if (next < prev.get())
          fail("next (" + next + ")" + " < " + "prev (" + prev + ")");

        sleep(10 + time - System.currentTimeMillis());
        prev.set(next);
      });

      assertTrue(modCount.get() > size);
      sleep(10);
    }
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