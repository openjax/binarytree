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

public class TestTree extends BinaryTree<Integer> {
  static final Integer[] PRE_ORDER_VALUES = {3, 1, 13, 5, 6, 10, 11, 16, 15, 9, 4, 2};
  static final Integer[] POST_ORDER_VALUES = {13, 6, 5, 1, 11, 9, 4, 15, 2, 16, 10, 3};
  static final Integer[] IN_ORDER_VALUES = {13, 1, 6, 5, 3, 11, 10, 9, 15, 4, 16, 2};
  static final Integer[] REVERSE_IN_ORDER_VALUES = {2, 16, 4, 15, 9, 10, 11, 3, 5, 6, 1, 13};
  static final Integer[] LEVEL_ORDER_VALUES = {3, 1, 10, 13, 5, 11, 16, 6, 15, 2, 9, 4};

  static TestTree emptyTree() {
    return new TestTree();
  }

  static TestTree withValues() {
    final TestTree tree = new TestTree();
    final Node root = tree.newNode(3);
    root.setLeft(tree.newNode(1));
    root.getLeft().setLeft(tree.newNode(13));
    root.getLeft().setRight(tree.newNode(5));
    root.getLeft().getRight().setLeft(tree.newNode(6));
    root.setRight(tree.newNode(10));
    root.getRight().setLeft(tree.newNode(11));
    root.getRight().setRight(tree.newNode(16));
    root.getRight().getRight().setLeft(tree.newNode(15));
    root.getRight().getRight().getLeft().setLeft(tree.newNode(9));
    root.getRight().getRight().getLeft().setRight(tree.newNode(4));
    root.getRight().getRight().setRight(tree.newNode(2));
    tree.setRoot(root);
    return tree;
  }
}