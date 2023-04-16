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
import org.libj.util.Interval;

abstract class IntervalSetTest {
  static class IntervalArraySetTest extends IntervalSetTest {
    @Override
    IntervalSet<Integer> newTree() {
      return new IntervalArraySet<>();
    }
  }

  static class IntervalTreeSetTest extends IntervalSetTest {
    @Override
    IntervalSet<Integer> newTree() {
      return new IntervalTreeSet<>();
    }
  }

  abstract IntervalSet<Integer> newTree();

  private IntervalSet<Integer> test2() {
    final IntervalSet<Integer> s = newTree();

    s.add(new Interval<Integer>(6, 8));
    assertEquals("[[6,8]]", s.toString());
    s.add(new Interval<Integer>(6, 7));
    assertEquals("[[6,8]]", s.toString());
    s.add(new Interval<Integer>(7, 8));
    assertEquals("[[6,8]]", s.toString());

    s.add(new Interval<Integer>(18, 19));
    assertEquals("[[6,8],[18,19]]", s.toString());

    s.add(new Interval<Integer>(9, 10));
    assertEquals("[[6,10],[18,19]]", s.toString());

    s.add(new Interval<Integer>(16, 17));
    assertEquals("[[6,10],[16,19]]", s.toString());

    s.add(new Interval<Integer>(3, 5));
    assertEquals("[[3,10],[16,19]]", s.toString());

    s.add(new Interval<Integer>(20, 22));
    assertEquals("[[3,10],[16,22]]", s.toString());

    s.add(new Interval<Integer>(1, 3));
    assertEquals("[[1,10],[16,22]]", s.toString());

    s.add(new Interval<Integer>(10, 12));
    assertEquals("[[1,12],[16,22]]", s.toString());

    s.add(new Interval<Integer>(14, 16));
    assertEquals("[[1,12],[14,22]]", s.toString());

    s.add(new Interval<Integer>(22, 24));
    assertEquals("[[1,12],[14,24]]", s.toString());

    return s;
  }

  @Test
  public void test2x() {
    final IntervalSet<Integer> s0 = test2();
    s0.add(new Interval<Integer>(12, 13));
    assertEquals("[[1,24]]", s0.toString());

    final IntervalSet<Integer> s1 = test2();
    s1.add(new Interval<Integer>(13, 14));
    assertEquals("[[1,24]]", s1.toString());

    final IntervalSet<Integer> s2 = test2();
    s2.add(new Interval<Integer>(12, 14));
    assertEquals("[[1,24]]", s2.toString());
  }

  private IntervalSet<Integer> test1() {
    final IntervalSet<Integer> s = newTree();

    s.add(new Interval<Integer>(6, 7));
    assertEquals("[[6,7]]", s.toString());
    s.add(new Interval<Integer>(6, 6));
    assertEquals("[[6,7]]", s.toString());

    s.add(new Interval<Integer>(15, 16));
    assertEquals("[[6,7],[15,16]]", s.toString());

    s.add(new Interval<Integer>(8, 9));
    assertEquals("[[6,9],[15,16]]", s.toString());

    s.add(new Interval<Integer>(13, 14));
    assertEquals("[[6,9],[13,16]]", s.toString());

    s.add(new Interval<Integer>(4, 5));
    assertEquals("[[4,9],[13,16]]", s.toString());

    s.add(new Interval<Integer>(17, 18));
    assertEquals("[[4,9],[13,18]]", s.toString());

    s.add(new Interval<Integer>(3, 4));
    assertEquals("[[3,9],[13,18]]", s.toString());

    s.add(new Interval<Integer>(9, 10));
    assertEquals("[[3,10],[13,18]]", s.toString());

    s.add(new Interval<Integer>(12, 13));
    assertEquals("[[3,10],[12,18]]", s.toString());

    s.add(new Interval<Integer>(18, 19));
    assertEquals("[[3,10],[12,19]]", s.toString());

    return s;
  }

  @Test
  public void test1x() {
    final IntervalSet<Integer> s0 = test1();
    s0.add(new Interval<Integer>(10, 11));
    assertEquals("[[3,19]]", s0.toString());

    final IntervalSet<Integer> s1 = test1();
    s1.add(new Interval<Integer>(11, 12));
    assertEquals("[[3,19]]", s1.toString());
  }

  @Test
  public void test0() {
    final IntervalSet<Integer> s = newTree();

    try {
      s.add(new Interval<Integer>(1, 0));
      fail("Expected IllegalArgumentException");
    }
    catch (final IllegalArgumentException e) {
    }

    try {
      s.intersects(new Interval<Integer>(1, 0));
      fail("Expected IllegalArgumentException");
    }
    catch (final IllegalArgumentException e) {
    }

    s.add(new Interval<Integer>(7, 7));
    assertEquals("[[7,7]]", s.toString());
    assertTrue(s.intersects(new Interval<Integer>(7, 7)));
    assertTrue(s.intersects(new Interval<Integer>(6, 7)));
    assertTrue(s.intersects(new Interval<Integer>(7, 8)));
    assertFalse(s.intersects(new Interval<Integer>(5, 6)));
    assertFalse(s.intersects(new Interval<Integer>(8, 9)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(5, 6)}, s.difference(new Interval<Integer>(5, 7)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(8, 9)}, s.difference(new Interval<Integer>(7, 9)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(5, 6), new Interval<Integer>(8, 9)}, s.difference(new Interval<Integer>(5, 9)));

    s.add(new Interval<Integer>(11, 11));
    assertEquals("[[7,7],[11,11]]", s.toString());
    assertTrue(s.intersects(new Interval<Integer>(7, 7)));
    assertTrue(s.intersects(new Interval<Integer>(6, 7)));
    assertTrue(s.intersects(new Interval<Integer>(7, 8)));
    assertFalse(s.intersects(new Interval<Integer>(8, 10)));
    assertFalse(s.intersects(new Interval<Integer>(5, 6)));
    assertFalse(s.intersects(new Interval<Integer>(8, 9)));
    assertTrue(s.intersects(new Interval<Integer>(10, 11)));
    assertTrue(s.intersects(new Interval<Integer>(11, 12)));
    assertFalse(s.intersects(new Interval<Integer>(12, 13)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(8, 10)}, s.difference(new Interval<Integer>(7, 11)));

    s.add(new Interval<Integer>(8, 8));
    assertEquals("[[7,8],[11,11]]", s.toString());
    assertArrayEquals(new Interval[] {new Interval<Integer>(9, 10)}, s.difference(new Interval<Integer>(7, 11)));

    s.add(new Interval<Integer>(10, 10));
    assertEquals("[[7,8],[10,11]]", s.toString());
    assertArrayEquals(new Interval[] {new Interval<Integer>(9, 9), new Interval<Integer>(12, 111)}, s.difference(new Interval<Integer>(7, 111)));

    s.add(new Interval<Integer>(6, 6));
    assertEquals("[[6,8],[10,11]]", s.toString());
    assertArrayEquals(new Interval[] {new Interval<Integer>(0, 5), new Interval<Integer>(9, 9), new Interval<Integer>(12, 100)}, s.difference(new Interval<Integer>(0, 100)));

    s.add(new Interval<Integer>(12, 12));
    assertEquals("[[6,8],[10,12]]", s.toString());
    assertArrayEquals(new Interval[] {new Interval<Integer>(0, 5), new Interval<Integer>(9, 9)}, s.difference(new Interval<Integer>(0, 9)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(0, 5), new Interval<Integer>(9, 9)}, s.difference(new Interval<Integer>(0, 10)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(0, 5), new Interval<Integer>(9, 9)}, s.difference(new Interval<Integer>(0, 11)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(9, 9)}, s.difference(new Interval<Integer>(6, 11)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(9, 9)}, s.difference(new Interval<Integer>(7, 11)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(9, 9)}, s.difference(new Interval<Integer>(9, 11)));
    assertArrayEquals(new Interval[] {}, s.difference(new Interval<Integer>(10, 11)));
    assertArrayEquals(new Interval[] {}, s.difference(new Interval<Integer>(6, 8)));
    assertArrayEquals(new Interval[] {}, s.difference(new Interval<Integer>(6, 7)));
    assertArrayEquals(new Interval[] {}, s.difference(new Interval<Integer>(6, 6)));

    s.add(new Interval<Integer>(6, 6));
    assertEquals("[[6,8],[10,12]]", s.toString());
    assertArrayEquals(new Interval[] {new Interval<Integer>(9, 9), new Interval<Integer>(13, 15)}, s.difference(new Interval<Integer>(9, 15)));

    s.add(new Interval<Integer>(8, 8));
    assertEquals("[[6,8],[10,12]]", s.toString());
    assertArrayEquals(new Interval[] {new Interval<Integer>(9, 9)}, s.difference(new Interval<Integer>(9, 10)));

    s.add(new Interval<Integer>(10, 10));
    assertEquals("[[6,8],[10,12]]", s.toString());
    assertArrayEquals(new Interval[] {new Interval<Integer>(9, 9)}, s.difference(new Interval<Integer>(8, 9)));

    s.add(new Interval<Integer>(12, 12));
    assertEquals("[[6,8],[10,12]]", s.toString());
    assertArrayEquals(new Interval[] {new Interval<Integer>(5, 5)}, s.difference(new Interval<Integer>(5, 6)));

    s.add(new Interval<Integer>(9, 9));
    assertEquals("[[6,12]]", s.toString());
    assertArrayEquals(new Interval[] {new Interval<Integer>(13, 13)}, s.difference(new Interval<Integer>(12, 13)));
  }

  private IntervalSet<Integer> testX() {
    final IntervalSet<Integer> s = newTree();
    s.add(new Interval<Integer>(9, 11));
    s.add(new Interval<Integer>(1, 3));
    s.add(new Interval<Integer>(5, 7));
    return s;
  }

  @Test
  public void testX2() {
    final IntervalSet<Integer> s = newTree();
    s.add(new Interval<Integer>(1, 3));
    s.add(new Interval<Integer>(9, 11));
    s.add(new Interval<Integer>(5, 7));
  }

  @Test
  public void testXL() {
    final IntervalSet<Integer> s = testX();
    s.add(new Interval<Integer>(0, 9));
    assertEquals("[[0,11]]", s.toString());

    final IntervalSet<Integer> s0 = testX();
    s0.add(new Interval<Integer>(1, 9));
    assertEquals("[[1,11]]", s0.toString());

    final IntervalSet<Integer> s1 = testX();
    s1.add(new Interval<Integer>(2, 9));
    assertEquals("[[1,11]]", s1.toString());

    final IntervalSet<Integer> s2 = testX();
    s2.add(new Interval<Integer>(3, 9));
    assertEquals("[[1,11]]", s2.toString());

    final IntervalSet<Integer> s3 = testX();
    s3.add(new Interval<Integer>(4, 9));
    assertEquals("[[1,11]]", s3.toString());

    final IntervalSet<Integer> s4 = testX();
    s4.add(new Interval<Integer>(5, 9));
    assertEquals("[[1,3],[5,11]]", s4.toString());

    final IntervalSet<Integer> s5 = testX();
    s5.add(new Interval<Integer>(6, 9));
    assertEquals("[[1,3],[5,11]]", s5.toString());

    final IntervalSet<Integer> s6 = testX();
    s6.add(new Interval<Integer>(7, 9));
    assertEquals("[[1,3],[5,11]]", s6.toString());

    final IntervalSet<Integer> s7 = testX();
    s7.add(new Interval<Integer>(8, 9));
    assertEquals("[[1,3],[5,11]]", s7.toString());

    final IntervalSet<Integer> s8 = testX();
    s8.add(new Interval<Integer>(9, 9));
    assertEquals("[[1,3],[5,7],[9,11]]", s8.toString());
    assertArrayEquals(new Interval[] {new Interval<Integer>(0, 0), new Interval<Integer>(4, 4), new Interval<Integer>(8, 8), new Interval<Integer>(12, 20)}, s8.difference(new Interval<Integer>(0, 20)));
  }

  @Test
  public void testXR() {
    final IntervalSet<Integer> s = testX();
    s.add(new Interval<Integer>(3, 12));
    assertEquals("[[1,12]]", s.toString());

    final IntervalSet<Integer> s0 = testX();
    s0.add(new Interval<Integer>(3, 11));
    assertEquals("[[1,11]]", s0.toString());

    final IntervalSet<Integer> s1 = testX();
    s1.add(new Interval<Integer>(3, 10));
    assertEquals("[[1,11]]", s1.toString());

    final IntervalSet<Integer> s2 = testX();
    s2.add(new Interval<Integer>(3, 9));
    assertEquals("[[1,11]]", s2.toString());

    final IntervalSet<Integer> s3 = testX();
    s3.add(new Interval<Integer>(3, 8));
    assertEquals("[[1,11]]", s3.toString());

    final IntervalSet<Integer> s4 = testX();
    s4.add(new Interval<Integer>(3, 7));
    assertEquals("[[1,7],[9,11]]", s4.toString());

    final IntervalSet<Integer> s5 = testX();
    s5.add(new Interval<Integer>(3, 6));
    assertEquals("[[1,7],[9,11]]", s5.toString());

    final IntervalSet<Integer> s6 = testX();
    s6.add(new Interval<Integer>(3, 5));
    assertEquals("[[1,7],[9,11]]", s6.toString());

    final IntervalSet<Integer> s7 = testX();
    s7.add(new Interval<Integer>(3, 4));
    assertEquals("[[1,7],[9,11]]", s7.toString());

    final IntervalSet<Integer> s8 = testX();
    s8.add(new Interval<Integer>(3, 3));
    assertEquals("[[1,3],[5,7],[9,11]]", s8.toString());
    assertArrayEquals(new Interval[] {new Interval<Integer>(4, 4), new Interval<Integer>(8, 8)}, s8.difference(new Interval<Integer>(1, 9)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(4, 4), new Interval<Integer>(8, 8)}, s8.difference(new Interval<Integer>(2, 10)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(4, 4)}, s8.difference(new Interval<Integer>(2, 6)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(8, 8)}, s8.difference(new Interval<Integer>(6, 10)));
    assertArrayEquals(new Interval[] {new Interval<Integer>(8, 8), new Interval<Integer>(12, 15)}, s8.difference(new Interval<Integer>(6, 15)));
  }

  @Test
  public void testXX() {
    final IntervalSet<Integer> s0 = testX();
    s0.add(new Interval<Integer>(0, 12));
    assertEquals("[[0,12]]", s0.toString());

    final IntervalSet<Integer> s1 = testX();
    s1.add(new Interval<Integer>(-1, 13));
    assertEquals("[[-1,13]]", s1.toString());
  }

  @Test
  public void testCompareTo() {
    final Interval<Integer> a = new Interval<Integer>(0, 2);
    final Interval<Integer> b = new Interval<Integer>(2, 4);
    assertEquals(-1, IntervalArraySet.compareTo(a, b));
    assertEquals(1, IntervalArraySet.compareTo(b, a));

    final Interval<Integer> c = new Interval<Integer>(4, 6);
    assertEquals(-1, IntervalArraySet.compareTo(b, c));
    assertEquals(1, IntervalArraySet.compareTo(c, b));
    assertEquals(-2, IntervalArraySet.compareTo(a, c));
    assertEquals(2, IntervalArraySet.compareTo(c, a));

    final Interval<Integer> d = new Interval<Integer>(1, 2);
    assertEquals(-1, IntervalArraySet.compareTo(a, d));
    assertEquals(1, IntervalArraySet.compareTo(d, a));

    final Interval<Integer> e = new Interval<Integer>(3, 5);
    assertEquals(-1, IntervalArraySet.compareTo(b, e));
    assertEquals(1, IntervalArraySet.compareTo(e, b));
    assertEquals(1, IntervalArraySet.compareTo(c, e));
    assertEquals(-1, IntervalArraySet.compareTo(e, c));

    assertEquals(-1, IntervalArraySet.compareTo(d, e));
    assertEquals(1, IntervalArraySet.compareTo(e, d));

    final Interval<Integer> f = new Interval<Integer>(3, 4);
    assertEquals(0, IntervalArraySet.compareTo(e, f));
    assertEquals(0, IntervalArraySet.compareTo(f, e));
  }
}