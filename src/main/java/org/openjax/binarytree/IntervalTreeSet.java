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

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.libj.util.Interval;

/**
 * An {@link IntervalSet} implemented with an augmented <a href="https://en.wikipedia.org/wiki/AVL_tree">AVL Tree</a> that
 * automatically merges intersecting intervals (i.e. no intersecting intervals can exist in this set).
 * <p>
 * Each operation concerning the {@linkplain #add(Interval) search} for, and the {@linkplain #add(Interval) addition} and
 * {@linkplain #remove(Interval) deletion} of an {@link Interval} have {@code O(log n)} time complexity, and O(n) memory complexity.
 *
 * @param <T> The type parameter of values defining the coordinates of the {@link Interval}s belonging to this set.
 * @implNote {@link Interval} values are treated as closed intervals (i.e. the {@linkplain Interval#getMin() min} and
 *           {@linkplain Interval#getMax() max} values are included in the interval).
 */
public class IntervalTreeSet<T extends Comparable<? super T> & Serializable> extends AvlTree<Interval<T>> implements IntervalSet<T> {
  protected class IntervalNode extends AvlNode {
    protected IntervalNode minNode;
    protected IntervalNode maxNode;

    public IntervalNode(final Interval<T> interval) {
      super(interval);
    }

    @Override
    protected IntervalNode getMinNode() {
      return minNode;
    }

    protected void setMinNode(final IntervalNode minNode) {
      this.minNode = minNode;
    }

    @Override
    protected IntervalNode getMaxNode() {
      return maxNode;
    }

    protected void setMaxNode(final IntervalNode maxNode) {
      this.maxNode = maxNode;
    }

    protected IntervalNode lower() {
      final IntervalNode left = getLeft();
      if (left != null)
        return left.getMaxNode();

      IntervalNode node = this, next = node;
      while ((next = next.getParent()) != null && next.getRight() != node)
        node = next;

      return next;
    }

    protected IntervalNode higher() {
      final IntervalNode right = getRight();
      if (right != null)
        return right.getMinNode();

      IntervalNode node = this, next = node;
      while ((next = next.getParent()) != null && next.getLeft() != node)
        node = next;

      return next;
    }

    @Override
    protected void setData(final Interval<T> data) {
      super.setData(data);
      if (getLeft() == null)
        setMinNode(this);

      if (getRight() == null)
        setMaxNode(this);
    }

    @Override
    protected IntervalNode rotateLeft() {
      final IntervalNode node = (IntervalNode)super.rotateLeft();

      final IntervalNode left = node.getLeft();
      node.setMinNode(left.getMinNode());

      final IntervalNode leftRight = left.getRight();
      left.setMaxNode(leftRight != null ? leftRight.getMaxNode() : left);

      return node;
    }

    @Override
    protected IntervalNode rotateRight() {
      final IntervalNode node = (IntervalNode)super.rotateRight();

      final IntervalNode right = node.getRight();
      node.setMaxNode(right.getMaxNode());

      final IntervalNode rightLeft = right.getLeft();
      right.setMinNode(rightLeft != null ? rightLeft.getMinNode() : right);

      return node;
    }

    @Override
    protected IntervalNode getParent() {
      return (IntervalNode)super.getParent();
    }

    @Override
    protected Node superSetLeft(final Node node) {
      this.setMinNode(node != null ? ((IntervalNode)node).getMinNode() : this);
      return super.superSetLeft(node);
    }

    @Override
    protected IntervalNode setLeft(final Node node) {
      return (IntervalNode)super.setLeft(node);
    }

    @Override
    protected IntervalNode getLeft() {
      return (IntervalNode)super.getLeft();
    }

    @Override
    protected Node superSetRight(final Node node) {
      this.setMaxNode(node != null ? ((IntervalNode)node).getMaxNode() : this);
      return super.superSetRight(node);
    }

    @Override
    protected IntervalNode setRight(final Node node) {
      return (IntervalNode)super.setRight(node);
    }

    @Override
    protected IntervalNode getRight() {
      return (IntervalNode)super.getRight();
    }

    private void updateMinMax() {
      final IntervalNode left = getLeft();
      minNode = left != null ? left.getMinNode() : this;
      final IntervalNode right = getRight();
      maxNode = right != null ? right.getMaxNode() : this;
    }

    @Override
    protected void updateNode() {
      updateMinMax();
      super.updateNode();
    }

    @Override
    protected String getText() {
      return super.getText() + " <" + getMinNode().getData().getMin() + "|" + getMaxNode().getData().getMax() + ">";
    }

    @Override
    protected IntervalNode clone(final BinaryTree<Interval<T>> tree) {
      final IntervalNode clone = (IntervalNode)super.clone(tree);
      clone.updateMinMax();
      return clone;
    }
  }

  @SuppressWarnings("rawtypes")
  private static final Interval[] emptyIntervals = {};

  /**
   * Creates a new {@link IntervalTreeSet} and calls {@link #add(Interval)} on each member of the provided {@link Collection}.
   *
   * @param c The {@link Collection} with the {@link Interval}s to {@linkplain #add(Interval) add}.
   * @throws NullPointerException If the provided {@link Collection}, or any member of the provided {@link Collection} is null.
   */
  public IntervalTreeSet(final Collection<Interval<T>> c) {
    addAll(c);
  }

  /**
   * Creates a new {@link IntervalTreeSet} and calls {@link #add(Interval)} on each member of the provided array.
   *
   * @param a The array of {@link Interval}s to {@linkplain #add(Interval) add}.
   * @throws NullPointerException If the provided array, or any member of the provided array is null.
   */
  @SafeVarargs
  public IntervalTreeSet(final Interval<T> ... a) {
    this(a, 0, a.length);
  }

  /**
   * Creates a new {@link IntervalTreeSet} and calls {@link #add(Interval)} on the members of the provided array between the
   * specified {@code fromIndex} and {@code toIndex} values.
   *
   * @param a The array of {@link Interval}s to {@linkplain #add(Interval) add}.
   * @param fromIndex The index of the first {@link Interval} (inclusive) to be added.
   * @param toIndex The index of the last {@link Interval} (exclusive) to be added.
   * @throws ArrayIndexOutOfBoundsException If the a value between {@code fromIndex} (inclusive) to {@code toIndex} (exclusive) is
   *           greater than the length of the provided array.
   * @throws NullPointerException If the provided array, or any member of the provided array is null.
   */
  public IntervalTreeSet(final Interval<T>[] a, final int fromIndex, final int toIndex) {
    addAll(a, fromIndex, toIndex);
  }

  /**
   * Creates a new {@link IntervalTreeSet} that is empty.
   */
  public IntervalTreeSet() {
  }

  @Override
  protected IntervalNode getRoot() {
    return (IntervalNode)super.getRoot();
  }

  @Override
  protected IntervalNode newNode(final Interval<T> key) {
    return new IntervalNode(key);
  }

  /**
   * Returns {@code true} if this set changed in lieu of the addition of the members of the provided array of {@link Interval}s,
   * otherwise {@code false}.
   *
   * @param a An array of {@link Interval}s to add.
   * @return {@code true} if this set changed due to the addition of the members of the provided array of {@link Interval}s,
   *         otherwise {@code false}.
   * @throws NullPointerException If the provided array, or any member of the provided array is null.
   * @implNote {@link Interval} values that intersect are automatically merged.
   * @complexity O(log(n) * m)
   */
  @SuppressWarnings("unchecked")
  public boolean addAll(final Interval<T> ... a) {
    return addAll(a, 0, a.length);
  }

  /**
   * Returns {@code true} if this set changed in lieu of the addition of the members of the provided array of {@link Interval}s
   * between the specified {@code fromIndex} and {@code toIndex} values, otherwise {@code false}.
   *
   * @param a The array of {@link Interval}s to {@linkplain #add(Interval) add}.
   * @param fromIndex The index of the first {@link Interval} (inclusive) to be added.
   * @param toIndex The index of the last {@link Interval} (exclusive) to be added.
   * @return {@code true} if this set changed in lieu of the addition of the members of the provided array of {@link Interval}s
   *         between the specified {@code fromIndex} and {@code toIndex} values, otherwise {@code false}.
   * @throws ArrayIndexOutOfBoundsException If the a value between {@code fromIndex} (inclusive) to {@code toIndex} (exclusive) is
   *           greater than the length of the provided array.
   * @throws NullPointerException If the provided array, or any member of the provided array is null.
   * @implNote {@link Interval} values that intersect are automatically merged.
   * @complexity O(log(n) * m)
   */
  public boolean addAll(final Interval<T>[] a, int fromIndex, final int toIndex) {
    while (fromIndex < toIndex) // [A]
      add(a[fromIndex++]);

    return changed;
  }

  /**
   * Returns {@code true} if this set changed in lieu of the addition of the provided {@link Interval}, otherwise {@code false}.
   *
   * @param key The {@link Interval}s to add.
   * @return {@code true} if this set changed in lieu of the addition of the provided {@link Interval}, otherwise {@code false}.
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @implNote {@link Interval} values that intersect are automatically merged.
   * @complexity O(log(n))
   */
  @Override
  public boolean add(final Interval<T> key) {
    final IntervalNode root = getRoot();
    if (key.getMin() == null && key.getMax() == null) {
      if (root == null) {
        setRoot(add(key, newNode(key)));
        return true;
      }

      final Interval<T> data = root.getData();
      if (data.getMin() == null && data.getMax() == null)
        return false;

      root.setData(key);
      root.setLeft(null);
      root.setRight(null);
      return true;
    }

    changed = false;
    setRoot(add(key, root));
    return changed;
  }

  private IntervalNode add(final Interval<T> key, final IntervalNode node) {
    if (node == null) {
      changed = true;
      return newNode(key);
    }

    final T keyMin = key.getMin();
    final T keyMax = key.getMax();

    final Interval<T> data = node.getData();
    final T dataMin = data.getMin();
    final T dataMax = data.getMax();

    if (dataMin == null)
      return node.setRight(mergeRight(key, node));

    if (keyMin == null) {
      if (keyMax.compareTo(dataMin) < 0)
        return node.setLeft(add(key, node.getLeft()));

      return node.setLeft(mergeLeft(key, node));
    }

    if (dataMax == null) {
      if (keyMin.compareTo(dataMin) < 0)
        return node.setLeft(mergeLeft(key, node));

      return node.setRight(mergeRight(key, node));
    }

    /**                            ____________
     *                             |  parent  |
     *                         .   ------------
     *                     .
     *                 .
     *             .
     * ___________
     * |  child  |
     * -----------
     *               ___________
     *               |   key   |
     *               -----------
     */
    /** ____________
     *  |  parent  |
     *  ------------
     *               .
     *                 .
     *                   .
     *                     ___________
     *                     |  child  |
     *                     -----------
     *                                    ___________
     *                                    |   key   |
     *                                    -----------
     */
    if (keyMin.compareTo(dataMax) > 0)
      return node.setRight(add(key, node.getRight()));

    /** ____________
     *  |  parent  |
     *  ------------
     *               .
     *                   .
     *                       .
     *                           .  ___________
     *                              |  child  |
     *                              -----------
     *                 ___________
     *                 |   key   |
     *                 -----------
     */
    /**                            ____________
     *                             |  parent  |
     *                             ------------
     *                            .
     *                           .
     *                          .
     *               ___________
     *               |  child  |
     *               -----------
     * ___________
     * |   key   |
     * -----------
     */
    if (keyMax != null && keyMax.compareTo(dataMin) < 0)
      return node.setLeft(add(key, node.getLeft()));

    /** ____________
     *  |  parent  |
     *  ------------
     *               .
     *                 .
     *                   .
     *                     ___________
     *                     |  child  |
     *                     -----------
     *                 ___________
     *                 |   key   |
     *                 -----------
     */
    /**                           ____________
     *                            |  parent  |
     *                            ------------
     *                          .
     *                        .
     *                      .
     *          ___________
     *          |  child  |
     *          -----------
     * ___________
     * |   key   |
     * -----------
     */
    if (keyMin.compareTo(dataMin) < 0)
      return node.setLeft(mergeLeft(key, node));

    /**                            ____________
     *                             |  parent  |
     *                         .   ------------
     *                     .
     *                 .
     *             .
     * ___________
     * |  child  |
     * -----------
     *  ___________________
     *  |       key       |
     *  -------------------
     */
    /** ____________
     *  |  parent  |
     *  ------------
     *               .
     *                 .
     *                   .
     *                     ___________
     *                     |  child  |
     *                     -----------
     *                      ___________
     *                      |   key   |
     *                      -----------
     */
    return node.setRight(mergeRight(key, node));
  }

  private IntervalNode mergeLeft(final Interval<T> key, final IntervalNode node) {
    final T keyMin = key.getMin();
    final IntervalNode left = mergeLeft(key, keyMin, node, node.getLeft());
    final IntervalNode right = node.getRight();
    if (right != null) {
      right.updateHeight();
      node.superSetRight(right.rebalance());
    }

    return left;
  }

  private IntervalNode mergeRight(final Interval<T> key, final IntervalNode node) {
    final T keyMax = key.getMax();
    return mergeRight(key, keyMax, node.getData().getMin(), node, node.getRight());
  }

  /**        __________
   *         |  root  |
   *         ----------
   * ___________
   * |   key   |
   * -----------
   */
  private IntervalNode mergeLeft(final Interval<T> key, final T keyMin, final IntervalNode node, final IntervalNode child) {
    if (child == null) {
      final Interval<T> nodeData = node.getData();
      final T nodeMax = nodeData.getMax();
      final T keyMax = key.getMax();
      final boolean updateMax = keyMax == null ? nodeMax != null : keyMax.compareTo(nodeMax) > 0;
      if (updateMax) {
        node.superSetRight(mergeRight(key, keyMax, keyMin, node, node.getRight()));
//        if (nodeData == node.getData()) { // Seems to not be needed, because it's guaranteed that `mergeRight` will call `node.setData()`.
//          node.setData(new Interval<>(keyMin, nodeMax));
//          changed = true;
//        }
      }
      else {
        node.setData(new Interval<>(keyMin, nodeMax));
        changed = true;
      }

      return null;
    }

    final Interval<T> childData = child.getData();

    /**      ___________
     *       |  child  |
     *       -----------
     *    ___________
     *    |   key   |
     *    -----------
     *    |
     *  keyMin
     */
    final T childMin = childData.getMin();
    if (keyMin == null || keyMin.compareTo(childMin) <= 0) {
      node.setMinNode(node); // FIXME: Is this needed?
      // Skip the child, and merge to its left
      changed = true;
      return mergeLeft(key, keyMin, node, child.getLeft());
    }

    /**  ___________
     *   |  child  |
     *   -----------
     *                ___________
     *                |   key   |
     *                -----------
     *                |
     *              keyMin
     */
    if (keyMin.compareTo(childData.getMax()) > 0) {
      // Keep the `child`, and merge to its right
      return child.setRight(mergeLeft(key, keyMin, node, child.getRight()));
    }

    final Interval<T> data = node.getData();
    final T dataMax = data.getMax();
    final T keyMax = key.getMax();
    final boolean updateMax = keyMax == null ? dataMax != null : keyMax.compareTo(dataMax) > 0;
    if (updateMax) {
      final boolean updateMin = childMin.compareTo(data.getMin()) < 0;
      node.superSetRight(mergeRight(key, keyMax, updateMin ? childMin : keyMin, node, node.getRight()));
//      if (nodeData == node.getData() && updateMin) { // Seems to not be needed, because it's guaranteed that `mergeRight` will call `node.setData()`.
//        node.setData(new Interval<>(childMin, nodeMax));
//        changed = true;
//      }
    }
    else { /* if (childMin.compareTo(nodeData.getMin()) < 0) { */ // Not needed, because mergeLeft is called for c = -1, which guarantees this exact condition.
      node.setData(new Interval<>(childMin, dataMax));
      changed = true;
    }

    return child.getLeft();
  }

  /** __________
   *  |  root  |
   *  ----------
   *        ___________
   *        |   key   |
   *        -----------
   */
  private IntervalNode mergeRight(final Interval<T> key, T keyMax, final T dataMin, final IntervalNode node, final IntervalNode child) {
    if (child == null) {
      final T dataMax = node.getData().getMax();
      if (keyMax == null ? dataMax != null : dataMax != null && keyMax.compareTo(dataMax) > 0) {
        node.setData(new Interval<>(dataMin, keyMax));
        changed = true;
      }

      return null;
    }

    /**  ___________
     *   |  child  |
     *   -----------
     *      ___________
     *      |   key   |
     *      -----------
     *                |
     *              keyMax
     */
    final Interval<T> data = child.getData();
    if (keyMax == null || keyMax.compareTo(data.getMax()) > 0) {
      // Skip the child, and merge to its right
      changed = true;
      return mergeRight(key, keyMax, dataMin, node, child.getRight());
    }

    /**             ___________
     *              |  child  |
     *              -----------
     * ___________
     * |   key   |
     * -----------
     *           |
     *         keyMax
     */
    if (keyMax.compareTo(data.getMin()) < 0) {
      // Keep the `child`, and merge to its left
      return child.setLeft(mergeRight(key, keyMax, dataMin, node, child.getLeft()));
    }

    keyMax = data.getMax();

    { /* if (keyMax.compareTo(data.getMax()) > 0) { */ // Not needed, because mergeRight is called for c = 1, which guarantees this exact condition.
      node.setData(new Interval<>(dataMin, keyMax));
      changed = true;
    }

    return child.getRight();
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public boolean remove(final Interval<T> key) {
    // FIXME: Unbounded
    changed = false;
    final IntervalNode root = getRoot();
    final IntervalNode newRoot = deleteNode(key, root);
    if (newRoot != root) {
      if (newRoot != null)
        newRoot.setParent(null);

      setRoot(newRoot);
    }

    return changed;
  }

  private IntervalNode deleteNode(final Interval<T> key, final IntervalNode node) {
    if (node == null)
      return null;

    // First check if the range of this node's subtree overlaps with the key
    final T nodeMax = node.getMaxNode().getData().getMax();
    final T nodeMin;

    final T keyMin = key.getMin();
    if (keyMin.compareTo(nodeMax) > 0 || key.getMax().compareTo(nodeMin = node.getMinNode().getData().getMin()) < 0) // Does not overlap
      return node;

    if (keyMin.compareTo(nodeMin) == 1) // Overlaps on right of node
      return deleteNodeUnsafe(key, node);

    // Overlaps node.min

    if (key.getMax().compareTo(nodeMax) < 0) // But comes short of node.max
      return deleteNodeUnsafe(key, node);

    // Overlaps node entirely
    changed = true;
    return null;
  }

  private IntervalNode deleteNodeUnsafe(final Interval<T> key, final IntervalNode node) {
    final Interval<T> data = node.getData();
    final T dataMin = data.getMin();

    final T keyMax = key.getMax();
    if (keyMax.compareTo(dataMin) < 0) // If key is to the left of the node, recurse left
      return node.setLeft(deleteNode(key, node.getLeft()));

    final T dataMax = data.getMax();
    final T keyMin = key.getMin();
    if (keyMin.compareTo(dataMax) > 0) // If key is to the right of the node, recurse right
      return node.setRight(deleteNode(key, node.getRight()));

    changed = true;

    if (keyMin.compareTo(dataMin) == 1) { // If key overlaps node from the right
      node.setData(new Interval<>(dataMin, keyMin));
      if (keyMax.compareTo(dataMax) < 0) // Split into two
        return node.setRight(newNode(new Interval<>(keyMax, dataMax)).setRight(deleteNode(key, node.getRight())));

      return node;
    }

    // If key overlaps node from the left

    if (keyMax.compareTo(dataMax) < 0) { // If key partially overlaps node on the left
      node.setData(new Interval<>(keyMax, dataMax));
      final IntervalNode left = node.getLeft();
      if (left != null)
        return node.setLeft(deleteNode(key, left));

      return node;
    }

    // Otherwise, key overlaps node entirely, so return its child(ren)

    final IntervalNode right = node.getRight();
    if (right == null)
      return deleteNode(key, node.getLeft());

    if (node.getLeft() == null)
      return deleteNode(key, right);

    final IntervalNode inOrderSuccessor = right.getMinNode();
    node.setData(inOrderSuccessor.getData());

    // Recurse back onto node, because the data was replaced by successor's (the right bound of which has not yet been checked)
    return deleteNodeUnsafe(key, node.setRight(deleteNode(inOrderSuccessor.getData(), right)));
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  @SuppressWarnings("unchecked")
  public Interval<T>[] difference(final Interval<T> key) {
    // FIXME: Unbounded
    final T keyMin = key.getMin();
    final T keyMax = key.getMax();

    Interval<T> data;
    Node minNode = null;
    for (Node node = getRoot(); node != null;) { // [X]
      data = node.getData();
      if (keyMin.compareTo(data.getMin()) < 0) {
        minNode = node;
        node = node.getLeft();
      }
      else {
        final T dataMax = data.getMax();
        if (keyMin.compareTo(dataMax) > 0)
          node = node.getRight();
        else
          return keyMax.compareTo(dataMax) > 0 ? difference(node, key, dataMax, keyMax, 0) : emptyIntervals;
      }
    }

    if (minNode == null)
      return new Interval[] {key};

    data = minNode.getData();
    final T dataMax = data.getMax();
    final Interval<T>[] diff = keyMax.compareTo(dataMax) > 0 ? difference(minNode, key, dataMax, keyMax, 1) : new Interval[1];
    diff[0] = new Interval<>(keyMin, data.getMin());
    return diff;
  }

  @SuppressWarnings("unchecked")
  private Interval<T>[] difference(final Interval<T> key, final T min, final T max, final int depth) {
    final Interval<T>[] diff = new Interval[depth + 1];
    diff[depth] = new Interval<>(min, max);
    return diff;
  }

  @SuppressWarnings("unchecked")
  private Interval<T>[] difference(Node node, final Interval<T> key, final T min, final T max, final int depth) {
    final Node right = node.getRight();
    if (right != null) {
      node = right.getMinNode();
    }
    else {
      Node next = node;
      while ((next = next.getParent()) != null && next.getLeft() != node)
        node = next;

      node = next;
      if (node == null)
        return difference(key, min, max, depth);
    }

    final Interval<T> data = node.getData();
    final T dataMin = data.getMin();
    if (max.compareTo(dataMin) < 0)
      return difference(key, min, max, depth);

    final T dataMax = data.getMax();
    final Interval<T>[] diff = max.compareTo(dataMax) > 0 ? difference(node, key, dataMax, max, depth + 1) : new Interval[depth + 1];
    diff[depth] = new Interval<>(min, dataMin);
    return diff;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided key is null.
   * @complexity O(log(n))
   */
  @Override
  public boolean contains(final T key) {
    return contains(key, getRoot());
  }

  private boolean contains(final T key, final Node node) {
    if (node == null)
      return false;

    final Interval<T> data = node.getData();
    return key.compareTo(data.getMin()) < 0 ? contains(key, node.getLeft()) : key.compareTo(data.getMax()) > 0 ? contains(key, node.getRight()) : true;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public boolean contains(final Interval<T> key) {
    // FIXME: Unbounded
    final Node node = searchNode(key);
    return node != null && key.getMax().compareTo(node.getData().getMax()) <= 0;
  }

  @Override
  protected Node searchNode(final Interval<T> key) {
    return searchNode(key.getMin(), getRoot());
  }

  private Node searchNode(final T keyMin, final IntervalNode node) {
    if (node == null || keyMin.compareTo(node.getMinNode().getData().getMin()) < 0 || keyMin.compareTo(node.getMaxNode().getData().getMax()) > 0)
      return null;

    final Interval<T> data = node.getData();
    return keyMin.compareTo(data.getMin()) < 0 ? searchNode(keyMin, node.getLeft()) : keyMin.compareTo(data.getMax()) > 0 ? searchNode(keyMin, node.getRight()) : node;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public boolean intersects(final Interval<T> key) {
    // FIXME: Unbounded
    return intersects(key, getRoot());
  }

  private boolean intersects(final Interval<T> key, final IntervalNode node) {
    if (node == null)
      return false;

    final T keyMax = key.getMax();
    final T keyMin;
    if (keyMax.compareTo(node.getMinNode().getData().getMin()) < 0 || (keyMin = key.getMin()).compareTo(node.getMaxNode().getData().getMax()) > 0)
      return false;

    final Interval<T> data = node.getData();
    final T dataMin = data.getMin();
    final T dataMax = data.getMax();
    return keyMax.compareTo(dataMin) <= 0 ? intersects(key, node.getLeft()) : keyMin.compareTo(dataMax) >= 0 ? intersects(key, node.getRight()) : key.intersects(data);
  }

  @Override
  public Comparator<? super Interval<T>> comparator() {
    return Interval.COMPARATOR;
  }

  /**
   * {@inheritDoc}
   *
   * @complexity O(1)
   */
  @Override
  public Interval<T> first() {
    if (isEmpty())
      throw new NoSuchElementException();

    return getRoot().getMinNode().getData();
  }

  /**
   * {@inheritDoc}
   *
   * @complexity O(1)
   */
  @Override
  public Interval<T> last() {
    if (isEmpty())
      throw new NoSuchElementException();

    return getRoot().getMaxNode().getData();
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public Interval<T> lower(final Interval<T> e) {
    Node node = searchNode(e);
    if (node == null)
      return null;

    node = ((IntervalNode)node).lower();
    return node == null ? null : node.getData();
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public Interval<T> higher(final Interval<T> e) {
    Node node = searchNode(e);
    if (node == null)
      return null;

    node = ((IntervalNode)node).higher();
    return node == null ? null : node.getData();
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public Interval<T> floor(final Interval<T> e) {
    final T min = e.getMin();
    IntervalNode floor = null;
    Interval<T> data;
    int c;
    for (IntervalNode node = getRoot(); node != null; node = c < 0 ? node.getLeft() : (floor = node).getRight()) { // [X]
      data = node.getData();
      c = min.compareTo(data.getMin());
      if (c == 0)
        return data;
    }

    return floor != null ? floor.getData() : null;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public Interval<T> ceiling(final Interval<T> e) {
    final T min = e.getMin();
    IntervalNode ceiling = null;
    Interval<T> data;
    int c;
    for (IntervalNode node = getRoot(); node != null; node = c < 0 ? (ceiling = node).getLeft() : node.getRight()) { // [X]
      data = node.getData();
      c = min.compareTo(data.getMin());
      if (c == 0)
        return data;
    }

    return ceiling != null ? ceiling.getData() : null;
  }

  /**
   * {@inheritDoc}
   *
   * @complexity O(log(n))
   */
  @Override
  public Interval<T> pollFirst() {
    final IntervalNode root = getRoot();
    if (root == null)
      return null;

    final IntervalNode node = root.getMinNode();
    final Interval<T> data = node.getData();
    node.delete();
    return data;
  }

  /**
   * {@inheritDoc}
   *
   * @complexity O(log(n))
   */
  @Override
  public Interval<T> pollLast() {
    final IntervalNode root = getRoot();
    if (root == null)
      return null;

    final IntervalNode node = root.getMaxNode();
    final Interval<T> data = node.getData();
    node.delete();
    return data;
  }

  @Override
  public NavigableSet<Interval<T>> descendingSet() {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<Interval<T>> descendingIterator() {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> subSet(final Interval<T> fromElement, final boolean fromInclusive, final Interval<T> toElement, final boolean toInclusive) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<Interval<T>> subSet(final Interval<T> fromElement, final Interval<T> toElement) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> headSet(final Interval<T> toElement, final boolean inclusive) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<Interval<T>> headSet(final Interval<T> toElement) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> tailSet(final Interval<T> fromElement, final boolean inclusive) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<Interval<T>> tailSet(final Interval<T> fromElement) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public IntervalTreeSet<T> clone() {
    return (IntervalTreeSet<T>)super.clone();
  }
}