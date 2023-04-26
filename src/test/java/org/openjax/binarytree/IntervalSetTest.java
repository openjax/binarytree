/* Copyright (c) 2022 OpenJAX
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

import org.junit.jupiter.api.Test;
import org.libj.util.ArrayUtil;
import org.libj.util.Interval;

abstract class IntervalSetTest {
  static class IntervalArraySetTest extends IntervalSetTest {
    @Override
    IntervalArraySet<Integer> newTree() {
      return new IntervalArraySet<>();
    }

    @Override
    IntervalArraySet<Integer> clone(final IntervalSet<Integer> s) {
      return ((IntervalArraySet<Integer>)s).clone();
    }
  }

  static class IntervalTreeSetTest extends IntervalSetTest {
    @Override
    IntervalTreeSet<Integer> newTree() {
      return new IntervalTreeSet<>();
    }

    @Override
    IntervalTreeSet<Integer> clone(final IntervalSet<Integer> s) {
      return ((IntervalTreeSet<Integer>)s).clone();
    }
  }

  @SuppressWarnings("rawtypes")
  private static final Interval[] is = {i(1, 3), i(5, 7), i(9, 11)};
  private static final int[][] y = {{0, 1, 2}, {0, 2, 1}, {1, 0, 2}, {1, 2, 0}, {2, 1, 0}, {2, 0, 1}};

  @SuppressWarnings("unchecked")
  private IntervalSet<Integer> testX(final int x) {
    final IntervalSet<Integer> s = newTree();
    for (final int i : y[x % y.length])
      s.add(is[i]);

    assertEquals("[[1,3),[5,7),[9,11)]", s.toString());
    return s;
  }

  private static Interval<Integer> i(final Integer min, final Integer max) {
    return new Interval<>(min, max);
  }

  abstract IntervalSet<Integer> newTree();
  abstract IntervalSet<Integer> clone(IntervalSet<Integer> s);

  @Test
  public void testXL() {
    int x = 0;

    final IntervalSet<Integer> s = testX(x++);
    assertTrue(s.add(i(0, 9)));
    assertEquals("[[0,11)]", s.toString());

    final IntervalSet<Integer> s0 = testX(x++);
    assertTrue(s0.add(i(1, 9)));
    assertEquals("[[1,11)]", s0.toString());

    final IntervalSet<Integer> s1 = testX(x++);
    assertTrue(s1.add(i(2, 9)));
    assertEquals("[[1,11)]", s1.toString());

    final IntervalSet<Integer> s2 = testX(x++);
    assertTrue(s2.add(i(3, 9)));
    assertEquals("[[1,11)]", s2.toString());

    final IntervalSet<Integer> s3 = testX(x++);
    assertTrue(s3.add(i(4, 9)));
    assertEquals("[[1,3),[4,11)]", s3.toString());

    final IntervalSet<Integer> s4 = testX(x++);
    assertTrue(s4.add(i(5, 9)));
    assertEquals("[[1,3),[5,11)]", s4.toString());

    final IntervalSet<Integer> s5 = testX(x++);
    assertTrue(s5.add(i(6, 9)));
    assertEquals("[[1,3),[5,11)]", s5.toString());

    final IntervalSet<Integer> s6 = testX(x++);
    assertTrue(s6.add(i(7, 9)));
    assertEquals("[[1,3),[5,11)]", s6.toString());

    final IntervalSet<Integer> s7 = testX(x++);
    assertTrue(s7.add(i(8, 9)));
    assertEquals("[[1,3),[5,7),[8,11)]", s7.toString());

    assertArrayEquals(new Interval[] {i(0, 1), i(3, 5), i(7, 8), i(11, 20)}, s7.difference(i(0, 20)));
  }

  @Test
  public void testXR() {
    int x = 0;

    final IntervalSet<Integer> s = testX(x++);
    assertTrue(s.add(i(3, 12)));
    assertEquals("[[1,12)]", s.toString());

    final IntervalSet<Integer> s0 = testX(x++);
    assertTrue(s0.add(i(3, 11)));
    assertEquals("[[1,11)]", s0.toString());

    final IntervalSet<Integer> s1 = testX(x++);
    assertTrue(s1.add(i(3, 10)));
    assertEquals("[[1,11)]", s1.toString());

    final IntervalSet<Integer> s2 = testX(x++);
    assertTrue(s2.add(i(3, 9)));
    assertEquals("[[1,11)]", s2.toString());

    final IntervalSet<Integer> s3 = testX(x++);
    assertTrue(s3.add(i(3, 8)));
    assertEquals("[[1,8),[9,11)]", s3.toString());

    final IntervalSet<Integer> s4 = testX(x++);
    assertTrue(s4.add(i(3, 7)));
    assertEquals("[[1,7),[9,11)]", s4.toString());

    final IntervalSet<Integer> s5 = testX(x++);
    assertTrue(s5.add(i(3, 6)));
    assertEquals("[[1,7),[9,11)]", s5.toString());

    final IntervalSet<Integer> s6 = testX(x++);
    assertTrue(s6.add(i(3, 5)));
    assertEquals("[[1,7),[9,11)]", s6.toString());

    final IntervalSet<Integer> s7 = testX(x++);
    assertTrue(s7.add(i(3, 4)));
    assertEquals("[[1,4),[5,7),[9,11)]", s7.toString());

    assertArrayEquals(new Interval[] {i(4, 5), i(7, 9)}, s7.difference(i(1, 9)));
    assertArrayEquals(new Interval[] {i(4, 5), i(7, 9)}, s7.difference(i(2, 10)));
    assertArrayEquals(new Interval[] {i(4, 5)}, s7.difference(i(2, 6)));
    assertArrayEquals(new Interval[] {i(7, 9)}, s7.difference(i(6, 10)));
    assertArrayEquals(new Interval[] {i(7, 9), i(11, 15)}, s7.difference(i(6, 15)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUnbounded() {
    final IntervalSet<Integer> s = newTree();
    for (int i = 0; i < 20; i += 6)
      s.add(new Interval<>(i, i + 4));

    for (int i = 0, i$ = s.size(); i < i$; ++i) {
      final Object[] array = s.toArray();
      final Interval<Integer> interval = (Interval<Integer>)array[i];
      for (int v = interval.getMin(); v <= interval.getMax() + 1; ++v) {
        Object[] result = ArrayUtil.splice(array, 0, i);
        result[0] = new Interval<>(null, Math.max(v, interval.getMax()));

        final IntervalSet<Integer> c = clone(s);
        assertTrue(c.add(new Interval<>(null, v)));
        assertFalse(c.add(new Interval<>(null, v)));
        assertFalse(c.add(new Interval<>(v - 1, v)));
        assertArrayEquals(result, c.toArray());

//        if (i == 1 && v == 11)
//          System.out.println();

        final Interval<Integer> a = new Interval<>(null, c.first().getMax() + 1);
        assertTrue(c.add(a));
        final Interval<Integer> b;
        if (result.length > 1 && a.getMax() == (b = (Interval<Integer>)result[1]).getMin()) {
          result = ArrayUtil.splice(result, 0, 1);
          result[0] = new Interval<>(null, b.getMax());
        }
        else {
          result[0] = a;
        }

//        System.err.println(i + " " + v + " " + Arrays.toString(result));
//        System.err.println(i + " " + v + " " + Arrays.toString(c.toArray()));
        assertArrayEquals(result, c.toArray());
      }
    }

    for (int i = s.size() - 1; i >= 0; --i) {
      final Object[] array = s.toArray();
      final Interval<Integer> interval = (Interval<Integer>)array[i];
      for (int v = interval.getMax(); v >= interval.getMin() - 1; --v) {
        Object[] result = ArrayUtil.splice(array, i + 1);
        result[result.length - 1] = new Interval<>(Math.min(interval.getMin(), v), null);

        final IntervalSet<Integer> c = clone(s);
        assertTrue(c.add(new Interval<>(v, null)));
        assertFalse(c.add(new Interval<>(v, null)));
        assertFalse(c.add(new Interval<>(v, v + 1)));
        assertArrayEquals(result, c.toArray());

//        if (i == 3 && v == 22)
//          System.out.println();

//        System.err.println(i + " " + v + " " + Arrays.toString(result));
//        System.err.println(i + " " + v + " " + Arrays.toString(c.toArray()));

        final Interval<Integer> a = new Interval<>(c.last().getMin() - 1, null);
        assertTrue(c.add(a));
        final Interval<Integer> b;
        if (result.length > 1 && a.getMin() == (b = (Interval<Integer>)result[result.length - 2]).getMax()) {
          result = ArrayUtil.splice(result, result.length - 1);
          result[result.length - 1] = new Interval<>(b.getMin(), null);
        }
        else {
          result[result.length - 1] = a;
        }

        assertArrayEquals(result, c.toArray());
      }
    }
  }

  @Test
  public void testXX() {
    int x = 0;

    final IntervalSet<Integer> s0 = testX(x++);
    assertTrue(s0.add(i(0, 12)));
    assertEquals("[[0,12)]", s0.toString());

    final IntervalSet<Integer> s1 = testX(x++);
    assertTrue(s1.add(i(-1, 13)));
    assertEquals("[[-1,13)]", s1.toString());
  }

  private IntervalSet<Integer> test2() {
    final IntervalSet<Integer> s = newTree();

    assertTrue(s.add(i(6, 8)));
    assertEquals("[[6,8)]", s.toString());
    assertFalse(s.add(i(6, 7)));
    assertEquals("[[6,8)]", s.toString());
    assertFalse(s.add(i(7, 8)));
    assertEquals("[[6,8)]", s.toString());

    assertTrue(s.add(i(18, 19)));
    assertEquals("[[6,8),[18,19)]", s.toString());

    assertTrue(s.add(i(8, 10)));
    assertEquals("[[6,10),[18,19)]", s.toString());

    assertTrue(s.add(i(16, 17)));
    assertEquals("[[6,10),[16,17),[18,19)]", s.toString());

    assertTrue(s.add(i(3, 5)));
    assertEquals("[[3,5),[6,10),[16,17),[18,19)]", s.toString());

    assertTrue(s.add(i(5, 6)));
    assertEquals("[[3,10),[16,17),[18,19)]", s.toString());

    assertTrue(s.add(i(16, 19)));
    assertEquals("[[3,10),[16,19)]", s.toString());

    assertTrue(s.add(i(20, 22)));
    assertEquals("[[3,10),[16,19),[20,22)]", s.toString());

    assertTrue(s.add(i(17, 21)));
    assertEquals("[[3,10),[16,22)]", s.toString());

    assertTrue(s.add(i(1, 3)));
    assertEquals("[[1,10),[16,22)]", s.toString());

    assertFalse(s.add(i(2, 3)));
    assertEquals("[[1,10),[16,22)]", s.toString());

    assertTrue(s.add(i(10, 12)));
    assertEquals("[[1,12),[16,22)]", s.toString());

    assertTrue(s.add(i(14, 16)));
    assertEquals("[[1,12),[14,22)]", s.toString());

    assertFalse(s.add(i(15, 17)));
    assertEquals("[[1,12),[14,22)]", s.toString());

    assertTrue(s.add(i(22, 24)));
    assertEquals("[[1,12),[14,24)]", s.toString());

    return s;
  }

  @Test
  public void test2x() {
    final IntervalSet<Integer> s0 = test2();
    assertTrue(s0.add(i(12, 13)));
    assertEquals("[[1,13),[14,24)]", s0.toString());

    final IntervalSet<Integer> s1 = test2();
    assertTrue(s1.add(i(13, 14)));
    assertEquals("[[1,12),[13,24)]", s1.toString());

    final IntervalSet<Integer> s2 = test2();
    assertTrue(s2.add(i(12, 14)));
    assertEquals("[[1,24)]", s2.toString());
  }

  private IntervalSet<Integer> test1() {
    final IntervalSet<Integer> s = newTree();

    assertTrue(s.add(i(6, 7)));
    assertEquals("[[6,7)]", s.toString());
    assertFalse(s.add(i(6, 7)));
    assertEquals("[[6,7)]", s.toString());

    assertTrue(s.add(i(15, 16)));
    assertEquals("[[6,7),[15,16)]", s.toString());

    assertTrue(s.add(i(8, 9)));
    assertEquals("[[6,7),[8,9),[15,16)]", s.toString());

    assertTrue(s.add(i(13, 14)));
    assertEquals("[[6,7),[8,9),[13,14),[15,16)]", s.toString());

    assertTrue(s.add(i(4, 5)));
    assertEquals("[[4,5),[6,7),[8,9),[13,14),[15,16)]", s.toString());

    assertTrue(s.add(i(17, 18)));
    assertEquals("[[4,5),[6,7),[8,9),[13,14),[15,16),[17,18)]", s.toString());

    assertTrue(s.add(i(3, 4)));
    assertEquals("[[3,5),[6,7),[8,9),[13,14),[15,16),[17,18)]", s.toString());

    assertTrue(s.add(i(9, 10)));
    assertEquals("[[3,5),[6,7),[8,10),[13,14),[15,16),[17,18)]", s.toString());

    assertTrue(s.add(i(12, 13)));
    assertEquals("[[3,5),[6,7),[8,10),[12,14),[15,16),[17,18)]", s.toString());

    assertTrue(s.add(i(14, 15)));
    assertEquals("[[3,5),[6,7),[8,10),[12,16),[17,18)]", s.toString());

    assertTrue(s.add(i(2, 7)));
    assertEquals("[[2,7),[8,10),[12,16),[17,18)]", s.toString());

    assertTrue(s.add(i(1, 8)));
    assertEquals("[[1,10),[12,16),[17,18)]", s.toString());

    for (int i = 1; i < 10; ++i) {
      assertFalse(s.add(i(i, ++i)));
      assertEquals("[[1,10),[12,16),[17,18)]", s.toString());
    }

    assertTrue(s.add(i(16, 19)));
    assertEquals("[[1,10),[12,19)]", s.toString());

    return s;
  }

  @Test
  public void test1x() {
    final IntervalSet<Integer> s0 = test1();
    assertTrue(s0.add(i(10, 11)));
    assertEquals("[[1,11),[12,19)]", s0.toString());

    final IntervalSet<Integer> s1 = test1();
    assertTrue(s1.add(i(11, 12)));
    assertEquals("[[1,10),[11,19)]", s1.toString());
  }

  @Test
  public void simpleTest() {
    final IntervalSet<Integer> s = newTree();

    assertTrue(s.add(i(6, 7)));
    assertEquals("[[6,7)]", s.toString());
    assertFalse(s.add(i(6, 7)));
    assertEquals("[[6,7)]", s.toString());

    assertTrue(s.add(i(15, 16)));
    assertEquals("[[6,7),[15,16)]", s.toString());

    assertTrue(s.add(i(8, 9)));
    assertEquals("[[6,7),[8,9),[15,16)]", s.toString());

    assertTrue(s.add(i(13, 14)));
    assertEquals("[[6,7),[8,9),[13,14),[15,16)]", s.toString());

    assertTrue(s.add(i(4, 5)));
    assertEquals("[[4,5),[6,7),[8,9),[13,14),[15,16)]", s.toString());

    assertTrue(s.add(i(17, 18)));
    assertEquals("[[4,5),[6,7),[8,9),[13,14),[15,16),[17,18)]", s.toString());

    assertTrue(s.add(i(3, 4)));
    assertEquals("[[3,5),[6,7),[8,9),[13,14),[15,16),[17,18)]", s.toString());

    assertTrue(s.add(i(9, 10)));
    assertEquals("[[3,5),[6,7),[8,10),[13,14),[15,16),[17,18)]", s.toString());

    assertTrue(s.add(i(12, 13)));
    assertEquals("[[3,5),[6,7),[8,10),[12,14),[15,16),[17,18)]", s.toString());

    assertTrue(s.add(i(18, 19)));
    assertEquals("[[3,5),[6,7),[8,10),[12,14),[15,16),[17,19)]", s.toString());

    assertTrue(s.add(i(10, 11)));
    assertEquals("[[3,5),[6,7),[8,11),[12,14),[15,16),[17,19)]", s.toString());

    assertTrue(s.add(i(11, 12)));
    assertEquals("[[3,5),[6,7),[8,14),[15,16),[17,19)]", s.toString());

    assertTrue(s.add(i(5, 6)));
    assertEquals("[[3,7),[8,14),[15,16),[17,19)]", s.toString());

    assertTrue(s.add(i(16, 17)));
    assertEquals("[[3,7),[8,14),[15,19)]", s.toString());

    assertTrue(s.add(i(14, 15)));
    assertEquals("[[3,7),[8,19)]", s.toString());

    assertFalse(s.add(i(8, 11)));
    assertEquals("[[3,7),[8,19)]", s.toString());

    assertFalse(s.add(i(6, 7)));
    assertEquals("[[3,7),[8,19)]", s.toString());

    assertTrue(s.add(i(7, 17)));
    assertEquals("[[3,19)]", s.toString());
  }
}