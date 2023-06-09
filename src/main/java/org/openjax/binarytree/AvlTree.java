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

/**
 * An <a href="https://en.wikipedia.org/wiki/AVL_tree">AVL Tree</a> {@link BinarySearchTree} implemented with recursive logic.
 *
 * @param <T> The type parameter of values belonging to this tree.
 */
public class AvlTree<T extends Comparable<? super T>> extends BinarySearchTree.Recursive<T> {
  protected class AvlNode extends Node {
    private int height;

    protected AvlNode(final T key) {
      super(key);
    }

    protected int balanceFactor() {
      return height(getRight()) - height(getLeft());
    }

    @Override
    protected AvlNode clone(final BinaryTree<T> tree) {
      final AvlNode clone = (AvlNode)super.clone(tree);
      clone.height = height;
      return clone;
    }

    @Override
    protected AvlNode getLeft() {
      return (AvlNode)super.getLeft();
    }

    @Override
    protected AvlNode getParent() {
      return (AvlNode)super.getParent();
    }

    @Override
    protected AvlNode getRight() {
      return (AvlNode)super.getRight();
    }

    @Override
    protected String getText() {
      return getKey() + " {H=" + height + ",BF=" + balanceFactor() + ",S=" + getSize() + "}";
    }

    protected AvlNode rebalance() {
      final int balanceFactor = balanceFactor();

      if (balanceFactor < -1) {
        final AvlNode left = getLeft();
        if (left.balanceFactor() > 0)
          setLeft$(left.rotateLeft());

        return rotateRight();
      }

      if (balanceFactor > 1) {
        final AvlNode right = getRight();
        if (right.balanceFactor() < 0)
          setRight$(right.rotateRight());

        return rotateLeft();
      }

      return this;
    }

    @Override
    protected void replaceInOrderSuccessor(final Node inOrderSuccessor, final Node right) {
      replaceRight(this, deleteNode(inOrderSuccessor.getKey(), right));
    }

    @Override
    protected Node replaceLeft(final Node node, final Node child) {
      final Node parent = node.getParent();

      final AvlNode avlNode = (AvlNode)node.setLeft(child);
      node.updateNode();

      if (parent == null)
        setRoot(avlNode);
      else if (parent.getLeft() == node)
        replaceLeft(parent, avlNode);
      else
        replaceRight(parent, avlNode);

      return avlNode;
    }

    @Override
    protected Node replaceRight(final Node node, final Node child) {
      final Node parent = node.getParent();

      final AvlNode avlNode = (AvlNode)node.setRight(child);
      node.updateNode();

      if (parent == null)
        setRoot(avlNode);
      else if (parent.getLeft() == node)
        replaceLeft(parent, avlNode);
      else
        replaceRight(parent, avlNode);

      return avlNode;
    }

    protected AvlNode rotateLeft() {
      final AvlNode rightChild = getRight();
      rightChild.setParent(null);
      rightChild.setLeft$(setRight(rightChild.getLeft()));
      rightChild.updateHeight();
      return rightChild;
    }

    protected AvlNode rotateRight() {
      final AvlNode leftChild = getLeft();
      leftChild.setParent(null);
      leftChild.setRight$(setLeft(leftChild.getRight()));
      leftChild.updateHeight();
      return leftChild;
    }

    @Override
    protected AvlNode setLeft(final Node node) {
      setLeft$(node);
      updateHeight();
      return rebalance();
    }

    @Override
    protected AvlNode setRight(final Node node) {
      setRight$(node);
      updateHeight();
      return rebalance();
    }

    protected Node setLeft$(final Node node) {
      return super.setLeft(node);
    }

    protected Node setRight$(final Node node) {
      return super.setRight(node);
    }

    protected void updateHeight() {
      this.height = Math.max(height(getLeft()), height(getRight())) + 1;
    }

    @Override
    protected void updateNode() {
      updateHeight();
      super.updateNode();
    }
  }

  protected static int height(final AvlTree<?>.AvlNode node) {
    return node != null ? node.height : -1;
  }

  @Override
  public AvlTree<T> clone() {
    return (AvlTree<T>)super.clone();
  }

  @Override
  protected AvlNode getRoot() {
    return (AvlNode)super.getRoot();
  }

  @Override
  protected AvlNode newNode(final T key) {
    return new AvlNode(key);
  }
}