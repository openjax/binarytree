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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.libj.util.Interval;

/**
 * An {@link IntervalSet} with the following features:
 * <ol>
 * <li>Augmented <a href="https://en.wikipedia.org/wiki/AVL_tree">AVL Tree</a> data structure, providing {@code O(log n)} time
 * complexity, and O(n) memory complexity for all operations that add, remove, or search for keys in the set.</li>
 * <li>Automatically merges intersecting intervals (i.e. no intersecting intervals can exist in the set).</li>
 * <li>Supports unbounded intervals (for example, the interval {@code [null,0)} means "from negative infinity to zero", and the
 * interval {@code [0,null)} means "from zero to infinity").</li>
 * </ol>
 *
 * @param <T> The type parameter of values defining the coordinates of the {@link Interval}s belonging to this set.
 */
public class IntervalTreeSet<T> extends AvlTree<Interval<T>> implements IntervalSet<T> {
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

    protected T getMinNodeMin() {
      return getMinNode().getData().getMin();
    }

    protected void setMinNode(final IntervalNode minNode) {
      this.minNode = minNode;
    }

    @Override
    protected IntervalNode getMaxNode() {
      return maxNode;
    }

    protected T getMaxNodeMax() {
      return getMaxNode().getData().getMax();
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
      return super.getText() + " <" + getMinNodeMin() + "|" + getMaxNodeMax() + ">";
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

  private boolean addAll(final BinaryTree<? extends Interval<T>>.Node n) {
    if (n == null)
      return false;

    boolean changed = false;
    changed |= addAll(n.getLeft());
    changed |= add(n.getData());
    changed |= addAll(n.getRight());
    return changed;
  }

  /**
   * Returns {@code true} if this set changed in lieu of the addition of the members of the provided {@link Collection} of
   * {@link Interval}s, otherwise {@code false}.
   *
   * @param c A of {@link Interval}s to add.
   * @return {@code true} if this set changed due to the addition of the members of the provided provided {@link Collection} of
   *         {@link Interval}s, otherwise {@code false}.
   * @throws NullPointerException If the provided array, or any member of the provided array is null.
   * @implNote {@link Interval} values that intersect are automatically merged.
   * @complexity O(log(n) * m)
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean addAll(final Collection<? extends Interval<T>> c) {
    if (c instanceof BinaryTree)
      return addAll(((BinaryTree<? extends Interval<T>>)c).getRoot());

    return super.addAll(c);
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

    if (dataMin == null) {
      if (keyMin != null && dataMax != null && key.compare(keyMin, dataMax) > 0)
        return node.setRight(add(key, node.getRight()));

      return node.setRight(mergeRight(key, node));
    }

    if (keyMin == null) {
      if (keyMax != null && key.compare(keyMax, dataMin) < 0)
        return node.setLeft(add(key, node.getLeft()));

      return node.setLeft(mergeLeft(key, node));
    }

    if (dataMax == null) {
      if (key.compare(keyMin, dataMin) < 0)
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
    if (key.compare(keyMin, dataMax) > 0)
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
    if (keyMax != null && key.compare(keyMax, dataMin) < 0)
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
    if (key.compare(keyMin, dataMin) < 0)
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
      final boolean updateMax = keyMax == null ? nodeMax != null : key.compare(keyMax, nodeMax) > 0;
      if (updateMax) {
        node.superSetRight(mergeRight(key, keyMax, keyMin, node, node.getRight()));
//        if (nodeData == node.getData()) { // Seems to not be needed, because it's guaranteed that `mergeRight` will call `node.setData()`.
//          node.setData(key.newInstance(keyMin, nodeMax));
//          changed = true;
//        }
      }
      else {
        node.setData(key.newInstance(keyMin, nodeMax));
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
    if (keyMin == null || childMin != null && key.compare(keyMin, childMin) <= 0) {
//      node.setMinNode(node); // FIXME: Is this needed?
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
    if (key.compare(keyMin, childData.getMax()) > 0) {
      // Keep the `child`, and merge to its right
      return child.setRight(mergeLeft(key, keyMin, node, child.getRight()));
    }

    final Interval<T> data = node.getData();
    final T dataMax = data.getMax();
    final T keyMax = key.getMax();
    final boolean updateMax = keyMax == null ? dataMax != null : key.compare(keyMax, dataMax) > 0;
    if (updateMax) {
      final boolean updateMin = childMin == null || key.compare(childMin, data.getMin()) < 0;
      node.superSetRight(mergeRight(key, keyMax, updateMin ? childMin : keyMin, node, node.getRight()));
//      if (nodeData == node.getData() && updateMin) { // Seems to not be needed, because it's guaranteed that `mergeRight` will call `node.setData()`.
//        node.setData(key.newInstance(childMin, nodeMax));
//        changed = true;
//      }
    }
    else { /* if (key.compare(childMin, nodeData.getMin()) < 0) { */ // Not needed, because mergeLeft is called for c = -1, which guarantees this exact condition.
      node.setData(key.newInstance(childMin, dataMax));
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
      if (dataMax != null && (keyMax == null || key.compare(keyMax, dataMax) > 0)) {
        node.setData(key.newInstance(dataMin, keyMax));
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
    final T dataMax;
    if (keyMax == null || (dataMax = data.getMax()) != null && key.compare(keyMax, dataMax) > 0) {
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
    if (key.compare(keyMax, data.getMin()) < 0) {
      // Keep the `child`, and merge to its left
      return child.setLeft(mergeRight(key, keyMax, dataMin, node, child.getLeft()));
    }

    keyMax = dataMax;

    { /* if (key.compare(keyMax, data.getMax()) > 0) { */ // Not needed, because mergeRight is called for c = 1, which guarantees this exact condition.
      node.setData(key.newInstance(dataMin, keyMax));
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
    final IntervalNode root = getRoot();
    if (root == null || !key.intersects(root.getMinNodeMin(), root.getMaxNodeMax()))
      return false;

    changed = false;
    final IntervalNode newRoot = deleteNodeUnsafe(key, root);
    if (newRoot != root) {
      if (newRoot != null)
        newRoot.setParent(null);

      setRoot(newRoot);
    }

    return changed;
  }

  private IntervalNode deleteNodeLeft(final Interval<T> key, final IntervalNode node) {
    final IntervalNode left = node.getLeft();
    final T keyMin;
    if (left == null || (keyMin = key.getMin()) != null && key.compare(keyMin, left.getMaxNodeMax()) >= 0)
      return node;

    return node.setLeft(deleteNodeUnsafe(key, left));
  }

  private IntervalNode deleteNodeRight(final Interval<T> key, final IntervalNode node) {
    final IntervalNode right = node.getRight();
    final T keyMax = key.getMax();
    final T minNodeMin = node.getMinNodeMin();
    if (right == null || keyMax != null && minNodeMin != null && key.compare(keyMax, minNodeMin) <= 0)
      return node;

    return node.setRight(deleteNodeUnsafe(key, right));
  }

  private IntervalNode deleteNodeUnsafe(final Interval<T> key, final IntervalNode node) {
    final Interval<T> data = node.getData();
    final T dataMin = data.getMin();
    final T dataMax = data.getMax();
    final T keyMax = key.getMax();
    final T keyMin = key.getMin();

    if (keyMin != null) {
      if (dataMax != null && key.compare(keyMin, dataMax) >= 0) // If key is to the right of the node, recurse right
        return deleteNodeRight(key, node);

      if (dataMin == null || key.compare(keyMin, dataMin) > 0) { // If key intersects node on the right of node.min
        changed = true;
        node.setData(key.newInstance(dataMin, keyMin));
        if (keyMax != null && (dataMax == null || key.compare(keyMax, dataMax) < 0)) // Split into two
          return node.setRight(deleteNodeRight(key, newNode(key.newInstance(keyMax, dataMax)).setRight(node.getRight())));

        return deleteNodeRight(key, node);
      }
    }

    if (keyMax != null) {
      if (dataMin != null && key.compare(keyMax, dataMin) <= 0) // If key is to the left of the node, recurse left
        return deleteNodeLeft(key, node);

      // If key intersects node on the left of node.min

      if (dataMax == null || key.compare(keyMax, dataMax) < 0) { // If key partially intersects node on the left
        changed = true;
        node.setData(key.newInstance(keyMax, dataMax));
        return deleteNodeLeft(key, node);
      }
    }

    changed = true;

    // Otherwise, key intersects node entirely, so return its child(ren)

    final IntervalNode right = node.getRight();
    if (right == null)
      return node.getLeft() == null ? null : deleteNodeUnsafe(key, node.getLeft());

    if (node.getLeft() == null)
      return deleteNodeUnsafe(key, right);

    final IntervalNode inOrderSuccessor = right.getMinNode();
    final Interval<T> inOrderSuccessorData = inOrderSuccessor.getData();
    node.setData(inOrderSuccessorData);

    // Recurse back onto node, because the data was replaced by successor's (the right bound of which has not yet been checked)
    return deleteNodeUnsafe(key, node.setRight(deleteNodeUnsafe(inOrderSuccessorData, right)));
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public Interval<T>[] difference(final Interval<T> key) {
    return difference(key, key.getMin(), key.getMax());
  }

  @SuppressWarnings("unchecked")
  private Interval<T>[] difference(final Interval<T> key, final T keyMin, final T keyMax) {
    Interval<T> data;
    T dataMin;
    T dataMax;
    Node minNode = null;

    final IntervalNode root = getRoot();
    if (keyMin == null) {
      if (root == null)
        return new Interval[] {key.newInstance(keyMin, keyMax)};

      minNode = root.getMinNode();
      data = minNode.getData();
      dataMax = data.getMax();
      if (keyMax != null && (dataMax == null || key.compare(keyMax, dataMax) <= 0))
        return emptyIntervals;

      dataMin = data.getMin();
      if (dataMin == null)
        return difference(minNode, key, dataMax, keyMax, 0);

      final Interval<T>[] diff = difference(minNode, key, dataMax, keyMax, 1);
      diff[0] = key.newInstance(keyMin, dataMin);
      return diff;
    }

    for (Node node = root; node != null;) { // [X]
      data = node.getData();
      dataMin = data.getMin();
      if (dataMin != null && key.compare(keyMin, dataMin) < 0) {
        minNode = node;
        node = node.getLeft();
      }
      else {
        dataMax = data.getMax();
        if (key.compare(keyMin, dataMax) > 0)
          node = node.getRight();
        else
          return keyMax == null || key.compare(keyMax, dataMax) > 0 ? difference(node, key, dataMax, keyMax, 0) : emptyIntervals;
      }
    }

    if (minNode == null)
      return new Interval[] {key.newInstance(keyMin, keyMax)};

    data = minNode.getData();
    dataMax = data.getMax();

    if (key == null && keyMax != null && data.compare(keyMin, keyMax) >= 0)
      throw new IllegalArgumentException("Illegal interval: " + data.toString(keyMin, keyMax));

    final Interval<T>[] diff = dataMax != null && (keyMax == null || data.compare(keyMax, dataMax) > 0) ? difference(minNode, key, dataMax, keyMax, 1) : new Interval[1];
    diff[0] = data.newInstance(keyMin, data.getMin());
    return diff;
  }

  @SuppressWarnings("unchecked")
  private Interval<T>[] newDiffArray(final Interval<T> key, final T min, final T max, final int depth) {
    final Interval<T>[] diff = new Interval[depth + 1];
    diff[depth] = key.newInstance(min, max);
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
        return newDiffArray(key, min, max, depth);
    }

    final Interval<T> data = node.getData();
    final T dataMin = data.getMin();
    if (max != null && key.compare(max, dataMin) < 0)
      return newDiffArray(key, min, max, depth);

    final T dataMax = data.getMax();
    final Interval<T>[] diff = dataMax != null && (max == null || key.compare(max, dataMax) > 0) ? difference(node, key, dataMax, max, depth + 1) : new Interval[depth + 1];
    diff[depth] = key.newInstance(min, dataMin);
    return diff;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided key is null.
   * @complexity O(log(n))
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean contains(final Object o) {
    final IntervalNode root = getRoot();
    if (root == null)
      return false;

    final T key = (T)o;
    final Interval<T> minNode = root.getMinNode().getData();
    final Interval<T> maxNode = root.getMaxNode().getData();
    return (minNode == null || minNode.compare(key, minNode.getMin()) >= 0) && (maxNode == null || maxNode.compare(key, maxNode.getMax()) < 0) && contains(key, root);
  }

  private boolean containsLeft(final T key, final IntervalNode node) {
    if (node == null)
      return false;

    final Interval<T> maxNode = node.getMaxNode().getData();
    return maxNode.compare(key, maxNode.getMax()) < 0 && contains(key, node);
  }

  private boolean containsRight(final T key, final IntervalNode node) {
    if (node == null)
      return false;

    final Interval<T> minNode = node.getMinNode().getData();
    return minNode.compare(key, minNode.getMin()) >= 0 && contains(key, node);
  }

  private boolean contains(final T key, final IntervalNode node) {
    final Interval<T> data = node.getData();
    return data.compare(key, data.getMin()) < 0 ? containsLeft(key, node.getLeft()) : data.compare(key, data.getMax()) > 0 ? containsRight(key, node.getRight()) : true;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public boolean contains(final Interval<T> key) {
    final Node node = searchNode(key);
    if (node == null)
      return false;

    final T keyMax = key.getMax();
    final T dataMax = node.getData().getMax();
    return keyMax == null ? dataMax == null : dataMax == null || key.compare(keyMax, dataMax) <= 0;
  }

  @Override
  protected Node searchNode(final Interval<T> key) {
    final IntervalNode root = getRoot();
    if (root == null)
      return null;

    final T keyMin = key.getMin();
    if (keyMin == null) {
      final IntervalNode minNode = root.getMinNode();
      return minNode.getData().getMin() == null ? minNode : null;
    }

    final T minNodeMin = root.getMinNodeMin();
    final T maxNodeMax = root.getMaxNodeMax();
    if (minNodeMin != null && key.compare(keyMin, minNodeMin) < 0 || maxNodeMax != null && key.compare(keyMin, maxNodeMax) >= 0)
      return null;

    return searchNode(keyMin, root);
  }

  private Node searchNodeLeft(final T keyMin, final IntervalNode node) {
    final Interval<T> data;
    return node == null || (data = node.getMaxNode().getData()).compare(keyMin, data.getMax()) >= 0 ? null : searchNode(keyMin, node);
  }

  private Node searchNodeRight(final T keyMin, final IntervalNode node) {
    final Interval<T> data;
    return node == null || (data = node.getMinNode().getData()).compare(keyMin, data.getMin()) < 0 ? null : searchNode(keyMin, node);
  }

  private Node searchNode(final T keyMin, final IntervalNode node) {
    final Interval<T> data = node.getData();
    final T dataMin = data.getMin();
    final T dataMax;
    return dataMin != null && data.compare(keyMin, dataMin) < 0 ? searchNodeLeft(keyMin, node.getLeft()) : (dataMax = data.getMax()) != null && data.compare(keyMin, dataMax) >= 0 ? searchNodeRight(keyMin, node.getRight()) : node;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public boolean intersects(final Interval<T> key) {
    final IntervalNode root = getRoot();
    if (root == null)
      return false;

    final T keyMin = key.getMin();
    final T keyMax = key.getMax();
    if (keyMin == null) {
      if (keyMax == null)
        return true;

      final T minNodeMin = root.getMinNodeMin();
      return minNodeMin == null || key.compare(keyMax, minNodeMin) > 0;
    }

    if (keyMax == null) {
      final T maxNodeMax = root.getMaxNodeMax();
      return maxNodeMax == null || key.compare(keyMin, maxNodeMax) < 0;
    }

    final T minNodeMin = root.getMinNodeMin();
    final T maxNodeMax;
    return (minNodeMin == null || key.compare(keyMax, minNodeMin) > 0) && ((maxNodeMax = root.getMaxNodeMax()) == null || key.compare(keyMin, maxNodeMax) < 0) && intersects(key, root);
  }

  private boolean intersectsLeft(final Interval<T> key, final IntervalNode node) {
    return node != null && key.compare(key.getMin(), node.getMaxNodeMax()) < 0 && intersects(key, node);
  }

  private boolean intersectsRight(final Interval<T> key, final IntervalNode node) {
    return node != null && key.compare(key.getMax(), node.getMinNodeMin()) > 0 && intersects(key, node);
  }

  private boolean intersects(final Interval<T> key, final IntervalNode node) {
    final Interval<T> data = node.getData();
    final T dataMin = data.getMin();
    final T dataMax;
    return dataMin != null && key.compare(key.getMax(), dataMin) <= 0 ? intersectsLeft(key, node.getLeft()) : (dataMax = data.getMax()) != null && key.compare(key.getMin(), dataMax) >= 0 ? intersectsRight(key, node.getRight()) : key.intersects(data);
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
    final IntervalNode root = getRoot();
    if (root == null)
      throw new NoSuchElementException();

    return root.getMinNode().getData();
  }

  /**
   * {@inheritDoc}
   *
   * @complexity O(1)
   */
  @Override
  public Interval<T> last() {
    final IntervalNode root = getRoot();
    if (root == null)
      throw new NoSuchElementException();

    return root.getMaxNode().getData();
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
      c = e.compare(min, data.getMin());
      if (c == 0)
        return data;
    }

    return floor == null ? null : floor.getData();
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
      c = e.compare(min, data.getMin());
      if (c == 0)
        return data;
    }

    return ceiling == null ? null : ceiling.getData();
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