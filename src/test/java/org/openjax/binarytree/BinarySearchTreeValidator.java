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

final class BinarySearchTreeValidator {
  private BinarySearchTreeValidator() {
  }

  /**
   * Validates if the given binary tree is a binary search tree (with no duplicates allowed).
   *
   * @param valueSpec The {@link ValueSpec}.
   * @param <BST> The type parameter of the {@link BinaryTree} subclass to validate.
   * @param <T> The type parameter of the values contained in the nodes of the {@link BinaryTree} subclass.
   * @param tree The binary tree to validate.
   * @return Whether the given binary tree is a binary search tree (with no duplicates allowed).
   */
  static <BST extends BinaryTree<T>,T extends Comparable<? super T>>boolean isTreeWithoutDuplicates(final ValueSpec<T> valueSpec, final BST tree) {
    return isTreeWithoutDuplicates(valueSpec, tree.getRoot(), valueSpec.minValue(), valueSpec.maxValue());
  }

  @SuppressWarnings("unchecked")
  private static <BST extends BinaryTree<T>,T extends Comparable<? super T>>boolean isTreeWithoutDuplicates(final ValueSpec<T> test, final BinaryTree<?>.Node node, final T minAllowedKey, final T maxAllowedKey) {
    if (node == null)
      return true;

    final T key = (T)node.getKey();
    if (key.compareTo(minAllowedKey) < 0 || key.compareTo(maxAllowedKey) > 0)
      return false;

    return isTreeWithoutDuplicates(test, node.getLeft(), minAllowedKey, test.prevValue(key)) && isTreeWithoutDuplicates(test, node.getRight(), test.nextValue(key), maxAllowedKey);
  }

  /**
   * Validates if the given binary tree is a binary search tree (with duplicates allowed).
   *
   * @param valueSpec The {@link ValueSpec}.
   * @param <BST> The type parameter of the {@link BinaryTree} subclass to validate.
   * @param <T> The type parameter of the values contained in the nodes of the {@link BinaryTree} subclass.
   * @param tree The binary tree to validate.
   * @return Whether the given binary tree is a binary search tree (with duplicates allowed).
   */
  static <BST extends BinaryTree<T>,T extends Comparable<? super T>>boolean isTreeWithDuplicates(final ValueSpec<T> valueSpec, final BST tree) {
    return isTreeWithDuplicates(tree.getRoot(), valueSpec.minValue(), valueSpec.maxValue());
  }

  @SuppressWarnings("unchecked")
  private static <BST extends BinaryTree<T>,T extends Comparable<? super T>>boolean isTreeWithDuplicates(final BinaryTree<?>.Node node, final T minAllowedKey, final T maxAllowedKey) {
    if (node == null)
      return true;

    final T key = (T)node.getKey();
    if (key.compareTo(minAllowedKey) < 0 || key.compareTo(maxAllowedKey) > 0)
      return false;

    return isTreeWithDuplicates(node.getLeft(), minAllowedKey, key) && isTreeWithDuplicates(node.getRight(), key, maxAllowedKey);
  }
}