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
import static org.openjax.binarytree.BinarySearchTreeValidator.*;

import org.junit.jupiter.api.Test;

public final class BinarySearchTreeValidatorTest implements ValueSpecInteger {
  @Test
  public void testShouldReturnTrueForEmptyTree() {
    final BinaryTree<Integer> tree = TestTree.emptyTree();
    assertTrue(isTreeWithoutDuplicates(this, tree));
  }

  @Test
  public void testShouldReturnTrueForTreeWithOneNode() {
    final TestTree tree = new TestTree();
    tree.setRoot(tree.newNode(100));
    assertTrue(isTreeWithoutDuplicates(this, tree));
  }

  @Test
  public void testShouldReturnTrueForTreeWithNodeAndSmallerLeftChild() {
    final TestTree tree = new TestTree();
    final BinaryTree<Integer>.Node root = tree.newNode(100);
    root.setLeft(tree.newNode(50));
    tree.setRoot(root);
    assertTrue(isTreeWithoutDuplicates(this, tree));
  }

  @Test
  public void testShouldReturnFalseForTreeWithNodeAndGreaterLeftChild() {
    final TestTree tree = new TestTree();
    final BinaryTree<Integer>.Node root = tree.newNode(100);
    root.setLeft(tree.newNode(110));
    tree.setRoot(root);
    assertFalse(isTreeWithoutDuplicates(this, tree));
  }

  @Test
  public void testShouldReturnTrueForTreeWithNodeAndGreaterRightChild() {
    final TestTree tree = new TestTree();
    final BinaryTree<Integer>.Node root = tree.newNode(100);
    root.setRight(tree.newNode(110));
    tree.setRoot(root);
    assertTrue(isTreeWithoutDuplicates(this, tree));
  }

  @Test
  public void testShouldReturnFalseForTreeWithNodeAndSmallerRightChild() {
    final TestTree tree = new TestTree();
    final BinaryTree<Integer>.Node root = tree.newNode(100);
    root.setRight(tree.newNode(90));
    tree.setRoot(root);
    assertFalse(isTreeWithoutDuplicates(this, tree));
  }

  @Test
  public void testShouldReturnTrueForComplexValidTree() {
    final BinaryTree<Integer> tree = generateComplexValidTree();
    assertTrue(isTreeWithoutDuplicates(this, tree));
  }

  @Test
  public void testWithoutDuplicatesShouldReturnFalseForComplexInvalidTree1() {
    final BinaryTree<Integer> tree = generateComplexInvalidTree1();
    assertFalse(isTreeWithoutDuplicates(this, tree));
  }

  @Test
  public void testWithDuplicatesShouldReturnFalseForComplexInvalidTree1() {
    final BinaryTree<Integer> tree = generateComplexInvalidTree1();
    assertFalse(isTreeWithDuplicates(this, tree));
  }

  @Test
  public void testShouldReturnFalseForComplexInvalidTree2() {
    final BinaryTree<Integer> tree = generateComplexInvalidTree2();
    assertFalse(isTreeWithoutDuplicates(this, tree));
  }

  @Test
  public void testShouldReturnTrueForDuplicatesOfRoot() {
    final TestTree tree = new TestTree();
    final BinaryTree<Integer>.Node root = tree.newNode(100);
    root.setLeft(tree.newNode(100));
    root.setRight(tree.newNode(100));
    tree.setRoot(root);

    assertTrue(isTreeWithDuplicates(this, tree));
  }

  @Test
  public void testShouldReturnTrueForDuplicateInValidComplexTree() {
    final BinaryTree<Integer> tree = generateComplexValidTree();

    tree.getRoot().getLeft().getLeft().setLeft(tree.newNode(1));
    tree.getRoot().getRight().getRight().getRight().setLeft(tree.newNode(16));

    assertTrue(isTreeWithDuplicates(this, tree));
  }

  @Test
  public void testShouldReturnFalseForDuplicatesWithErrorInValidComplexTree() {
    final BinaryTree<Integer> tree = generateComplexValidTree();

    tree.getRoot().getLeft().getLeft().setLeft(tree.newNode(1));
    tree.getRoot().getLeft().getLeft().getLeft().setRight(tree.newNode(2));

    assertFalse(isTreeWithDuplicates(this, tree));
  }

  @Test
  public void withDuplicates_shouldReturnFalseForComplexInvalidTree2() {
    final BinaryTree<Integer> tree = generateComplexInvalidTree2();
    assertFalse(isTreeWithDuplicates(this, tree));
  }

  private static BinaryTree<Integer> generateComplexValidTree() {
    final TestTree tree = new TestTree();
    final BinaryTree<Integer>.Node root = tree.newNode(5);

    root.setLeft(tree.newNode(2));
    root.getLeft().setLeft(tree.newNode(1));
    root.getLeft().setRight(tree.newNode(4));
    root.getLeft().getRight().setLeft(tree.newNode(3));

    root.setRight(tree.newNode(9));
    root.getRight().setLeft(tree.newNode(6));
    root.getRight().setRight(tree.newNode(15));
    root.getRight().getRight().setLeft(tree.newNode(11));
    root.getRight().getRight().getLeft().setLeft(tree.newNode(10));
    root.getRight().getRight().getLeft().setRight(tree.newNode(13));
    root.getRight().getRight().setRight(tree.newNode(16));

    tree.setRoot(root);
    return tree;
  }

  private static BinaryTree<Integer> generateComplexInvalidTree1() {
    final TestTree tree = new TestTree();
    final BinaryTree<Integer>.Node root = tree.newNode(5);

    root.setLeft(tree.newNode(2));
    root.getLeft().setLeft(tree.newNode(1));
    root.getLeft().setRight(tree.newNode(4));
    root.getLeft().getRight().setRight(tree.newNode(3)); // right instead of left

    root.setRight(tree.newNode(9));
    root.getRight().setLeft(tree.newNode(6));
    root.getRight().setRight(tree.newNode(15));
    root.getRight().getRight().setLeft(tree.newNode(11));
    root.getRight().getRight().getLeft().setLeft(tree.newNode(10));
    root.getRight().getRight().getLeft().setRight(tree.newNode(13));
    root.getRight().getRight().setRight(tree.newNode(16));

    tree.setRoot(root);
    return tree;
  }

  private static BinaryTree<Integer> generateComplexInvalidTree2() {
    final TestTree tree = new TestTree();
    final BinaryTree<Integer>.Node root = tree.newNode(5);

    root.setLeft(tree.newNode(2));
    root.getLeft().setLeft(tree.newNode(1));
    root.getLeft().setRight(tree.newNode(4));
    root.getLeft().getRight().setLeft(tree.newNode(3));

    root.setRight(tree.newNode(9));
    root.getRight().setLeft(tree.newNode(6));
    root.getRight().setRight(tree.newNode(15));
    root.getRight().getRight().setLeft(tree.newNode(11));
    root.getRight().getRight().getLeft().setLeft(tree.newNode(10));
    root.getRight().getRight().setRight(tree.newNode(16));
    root.getRight().getRight().getRight().setLeft(tree.newNode(13));

    tree.setRoot(root);
    return tree;
  }
}