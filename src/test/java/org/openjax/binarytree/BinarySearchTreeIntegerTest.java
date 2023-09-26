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

abstract class BinarySearchTreeIntegerTest<BST extends BinarySearchTree<Integer>> extends BinarySearchTreeTest<BST,Integer> implements ValueSpecInteger {
  private static final int TEST_TREE_MIN_SIZE = 1;
  private static final int TEST_TREE_MAX_SIZE = 1000;

  @Override
  ArrayList<Integer> createOrderedSequenceOfKeys() {
    final int size = random.nextInt(TEST_TREE_MIN_SIZE, TEST_TREE_MAX_SIZE);
    final ArrayList<Integer> list = new ArrayList<>(size);
    int i = 0;
    do
      list.add(i);
    while (++i < size);
    return list;
  }

  @Override
  boolean supportsMerging() {
    return false;
  }
}