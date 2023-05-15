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

import java.util.function.Supplier;

public class AvlTreeTest extends BinarySearchTreeIntegerTest<AvlTree<Integer>> {
  @Override
  protected AvlTree<Integer> createTree() {
    return new AvlTree<>();
  }

  @Override
  void assertSpecificTreeInvariants(final AvlTree<Integer> tree, final Supplier<String> onError) {
    validateAVLInvariant(tree.getRoot(), onError);
  }

  static <BST extends AvlTree<T>,T extends Comparable<? super T>>void validateAVLInvariant(final BST.AvlNode node, final Supplier<String> onError) {
    if (node == null)
      return;

    final AvlTree<T>.AvlNode left = node.getLeft();
    final AvlTree<T>.AvlNode right = node.getRight();

    final int leftHeight = AvlTree.height(left);
    final int rightHeight = AvlTree.height(right);

    validateHeight(node, leftHeight, rightHeight, onError);
    validateBalanceFactor(node, leftHeight, rightHeight, onError);

    validateAVLInvariant(left, onError);
    validateAVLInvariant(right, onError);
  }

  static void validateHeight(final AvlTree<?>.AvlNode node, final int leftHeight, final int rightHeight, final Supplier<String> onError) {
    final int expectedHeight = 1 + Math.max(leftHeight, rightHeight);
    final int actualHeight = AvlTree.height(node);
    assertEquals(expectedHeight, actualHeight, onError(onError, () -> "Height of node " + node.getData() + " is " + actualHeight + " (expected: " + expectedHeight + ")"));
  }

  static void validateBalanceFactor(final AvlTree<?>.AvlNode node, final int leftHeight, final int rightHeight, final Supplier<String> onError) {
    final int bf = rightHeight - leftHeight;
    assertTrue(-1 <= bf && bf <= 1, onError(onError, () -> "Balance factor (bf) of node " + node.getData() + " is " + bf + " (expected: -1 <= bf <= 1)"));
  }
}