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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

    public IntervalNode(final Interval<T> key) {
      super(key);
    }

    @Override
    protected IntervalNode clone(final BinaryTree<Interval<T>> tree) {
      final IntervalNode clone = (IntervalNode)super.clone(tree);
      clone.updateMinMax();
      return clone;
    }

    @Override
    protected IntervalNode getLeft() {
      return (IntervalNode)super.getLeft();
    }

    @Override
    protected IntervalNode getMaxNode() {
      return maxNode;
    }

    protected T getMaxNodeMax() {
      return getMaxNode().getKey().getMax();
    }

    @Override
    protected IntervalNode getMinNode() {
      return minNode;
    }

    protected T getMinNodeMin() {
      return getMinNode().getKey().getMin();
    }

    @Override
    protected IntervalNode getParent() {
      return (IntervalNode)super.getParent();
    }

    @Override
    protected IntervalNode getRight() {
      return (IntervalNode)super.getRight();
    }

    @Override
    protected String getText() {
      return super.getText() + " <" + getMinNodeMin() + "|" + getMaxNodeMax() + ">";
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

    protected IntervalNode lower() {
      final IntervalNode left = getLeft();
      if (left != null)
        return left.getMaxNode();

      IntervalNode node = this, next = node;
      while ((next = next.getParent()) != null && next.getRight() != node)
        node = next;

      return next;
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
    protected void setKey(final Interval<T> key) {
      super.setKey(key);
      if (getLeft() == null)
        setMinNode(this);

      if (getRight() == null)
        setMaxNode(this);
    }

    @Override
    protected IntervalNode setLeft(final Node node) {
      return (IntervalNode)super.setLeft(node);
    }

    protected void setMaxNode(final IntervalNode maxNode) {
      this.maxNode = maxNode;
    }

    protected void setMinNode(final IntervalNode minNode) {
      this.minNode = minNode;
    }

    @Override
    protected IntervalNode setRight(final Node node) {
      return (IntervalNode)super.setRight(node);
    }

    @Override
    protected Node setLeft$(final Node node) {
      this.setMinNode(node != null ? ((IntervalNode)node).getMinNode() : this);
      return super.setLeft$(node);
    }

    @Override
    protected Node setRight$(final Node node) {
      this.setMaxNode(node != null ? ((IntervalNode)node).getMaxNode() : this);
      return super.setRight$(node);
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
  }

  @SuppressWarnings("rawtypes")
  private static final Interval[] emptyIntervals = {};

  /**
   * Creates a new {@link IntervalTreeSet} that is empty.
   */
  public IntervalTreeSet() {
  }

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
   * Creates a new {@link IntervalTreeSet} and calls {@link #add(Interval)} on the members of the provided array between the specified
   * {@code fromIndex} and {@code toIndex} values.
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
    return addFast(key);
  }

  private IntervalNode add(final Interval<T> key, final IntervalNode node) {
    if (node == null) {
      incModCount();
      changed = true;
      return newNode(key);
    }

    final T keyMin = key.getMin();
    final T keyMax = key.getMax();

    final Interval<T> nodeKey = node.getKey();
    final T nodeKeyMin = nodeKey.getMin();
    final T nodeKeyMax = nodeKey.getMax();

    if (nodeKeyMin == null) {
      if (keyMin != null && nodeKeyMax != null && key.compare(keyMin, nodeKeyMax) > 0)
        return node.setRight(add(key, node.getRight()));

      return node.setRight(mergeRight(key, node));
    }

    if (keyMin == null) {
      if (keyMax != null && key.compare(keyMax, nodeKeyMin) < 0)
        return node.setLeft(add(key, node.getLeft()));

      return node.setLeft(mergeLeft(key, node));
    }

    if (nodeKeyMax == null) {
      if (key.compare(keyMin, nodeKeyMin) < 0)
        return node.setLeft(mergeLeft(key, node));

      return node.setRight(mergeRight(key, node));
    }

    /** @formatter:off
     *                             ____________
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
     * @formatter:on */
    if (key.compare(keyMin, nodeKeyMax) > 0)
      return node.setRight(add(key, node.getRight()));

    /** @formatter:off
     *   ____________
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
     * @formatter:on */
    if (keyMax != null && key.compare(keyMax, nodeKeyMin) < 0)
      return node.setLeft(add(key, node.getLeft()));

    /** @formatter:off
     *  ____________
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
     * @formatter:on */
    if (key.compare(keyMin, nodeKeyMin) < 0)
      return node.setLeft(mergeLeft(key, node));

    /** @formatter:off
     *                             ____________
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
     * @formatter:on */
    return node.setRight(mergeRight(key, node));
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
  public boolean addAll(final Collection<? extends Interval<T>> c) {
    return super.addAll(c);
  }

  /**
   * Returns {@code true} if this set changed in lieu of the addition of the members of the provided array of {@link Interval}s,
   * otherwise {@code false}.
   *
   * @param a An array of {@link Interval}s to add.
   * @return {@code true} if this set changed due to the addition of the members of the provided array of {@link Interval}s, otherwise
   *         {@code false}.
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
    while (fromIndex < toIndex)
      addFast(a[fromIndex++]);

    return changed;
  }

  @Override
  protected boolean addFast(final Interval<T> key) {
    final IntervalNode root = getRoot();
    if (key.getMin() == null && key.getMax() == null) {
      if (root == null) {
        setRoot(add(key, newNode(key)));
        incModCount();
        return true;
      }

      final Interval<T> nodeKey = root.getKey();
      if (nodeKey.getMin() == null && nodeKey.getMax() == null)
        return false;

      root.setKey(key);
      root.setLeft(null);
      root.setRight(null);
      incModCount();
      return true;
    }

    changed = false;
    setRoot(add(key, root));
    return changed;
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
    Interval<T> key;
    IntervalNode node = getRoot();
    for (int c; node != null; node = c < 0 ? (ceiling = node).getLeft() : node.getRight()) { // [X]
      key = node.getKey();
      c = e.compare(min, key.getMin());
      if (c == 0)
        return key;
    }

    return ceiling == null ? null : ceiling.getKey();
  }

  @Override
  public IntervalTreeSet<T> clone() {
    return (IntervalTreeSet<T>)super.clone();
  }

  @Override
  public Comparator<? super Interval<T>> comparator() {
    return Interval.COMPARATOR;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public boolean contains(final Interval<T> key) {
    return containsFast(key);
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
    return o instanceof Interval ? containsFast((Interval<T>)o) : containsFast(o);
  }

  private boolean contains(final T key, final IntervalNode node) {
    final Interval<T> nodeKey = node.getKey();
    return nodeKey.compare(key, nodeKey.getMin()) < 0 ? containsLeft(key, node.getLeft()) : nodeKey.compare(key, nodeKey.getMax()) > 0 ? containsRight(key, node.getRight()) : true;
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return super.containsAll(c);
  }

  @Override
  protected boolean containsFast(final Interval<T> key) {
    final Node node = searchNodeFast(key);
    if (node == null)
      return false;

    final T nodeKeyMax = node.getKey().getMax();
    if (nodeKeyMax == null)
      return true;

    final T keyMax = key.getMax();
    return keyMax != null && key.compare(keyMax, nodeKeyMax) <= 0;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean containsFast(final Object o) {
    final IntervalNode root = getRoot();
    if (root == null)
      return false;

    final T key = (T)o;
    final Interval<T> minNode = root.getMinNode().getKey();
    final Interval<T> maxNode = root.getMaxNode().getKey();
    return (minNode == null || minNode.compare(key, minNode.getMin()) >= 0) && (maxNode == null || maxNode.compare(key, maxNode.getMax()) < 0) && contains(key, root);
  }

  private boolean containsLeft(final T key, final IntervalNode node) {
    if (node == null)
      return false;

    final Interval<T> maxNode = node.getMaxNode().getKey();
    return maxNode.compare(key, maxNode.getMax()) < 0 && contains(key, node);
  }

  private boolean containsRight(final T key, final IntervalNode node) {
    if (node == null)
      return false;

    final Interval<T> minNode = node.getMinNode().getKey();
    return minNode.compare(key, minNode.getMin()) >= 0 && contains(key, node);
  }

  @Override
  protected boolean deleteFast(final Interval<T> key) {
    final IntervalNode root = getRoot();
    return root != null && remove(root, key);
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
    final Interval<T> nodeKey = node.getKey();
    final T nodeKeyMin = nodeKey.getMin();
    final T nodeKeyMax = nodeKey.getMax();
    final T keyMax = key.getMax();
    final T keyMin = key.getMin();

    if (keyMin != null) {
      if (nodeKeyMax != null && key.compare(keyMin, nodeKeyMax) >= 0) // If key is to the right of the node, recurse right
        return deleteNodeRight(key, node);

      if (nodeKeyMin == null || key.compare(keyMin, nodeKeyMin) > 0) { // If key intersects node on the right of node.min
        incModCount();
        changed = true;
        node.setKey(key.newInstance(nodeKeyMin, keyMin));
        if (keyMax != null && (nodeKeyMax == null || key.compare(keyMax, nodeKeyMax) < 0)) // Split into two
          return node.setRight(deleteNodeRight(key, newNode(key.newInstance(keyMax, nodeKeyMax)).setRight(node.getRight())));

        return deleteNodeRight(key, node);
      }
    }

    if (keyMax != null) {
      if (nodeKeyMin != null && key.compare(keyMax, nodeKeyMin) <= 0) // If key is to the left of the node, recurse left
        return deleteNodeLeft(key, node);

      // If key intersects node on the left of node.min

      if (nodeKeyMax == null || key.compare(keyMax, nodeKeyMax) < 0) { // If key partially intersects node on the left
        incModCount();
        changed = true;
        node.setKey(key.newInstance(keyMax, nodeKeyMax));
        return deleteNodeLeft(key, node);
      }
    }

    incModCount();
    changed = true;

    // Otherwise, key intersects node entirely, so return its child(ren)

    final IntervalNode right = node.getRight();
    if (right == null)
      return node.getLeft() == null ? null : deleteNodeUnsafe(key, node.getLeft());

    if (node.getLeft() == null)
      return deleteNodeUnsafe(key, right);

    final IntervalNode inOrderSuccessor = right.getMinNode();
    final Interval<T> inOrderSuccessorKey = inOrderSuccessor.getKey();
    node.setKey(inOrderSuccessorKey);

    // Recurse back onto node, because the key was replaced by successor's (the right bound of which has not yet been checked)
    return deleteNodeUnsafe(key, node.setRight(deleteNodeUnsafe(inOrderSuccessorKey, right)));
  }

  @Override
  public Iterator<Interval<T>> descendingIterator() {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> descendingSet() {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
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
    Interval<T> nodeKey;
    T nodeKeyMin;
    T nodeKeyMax;
    Node minNode = null;

    final IntervalNode root = getRoot();
    if (keyMin == null) {
      if (root == null)
        return new Interval[] {key.newInstance(keyMin, keyMax)};

      minNode = root.getMinNode();
      nodeKey = minNode.getKey();
      nodeKeyMax = nodeKey.getMax();
      if (keyMax != null && (nodeKeyMax == null || key.compare(keyMax, nodeKeyMax) <= 0))
        return emptyIntervals;

      nodeKeyMin = nodeKey.getMin();
      if (nodeKeyMin == null)
        return difference(minNode, key, nodeKeyMax, keyMax, 0);

      final Interval<T>[] diff = difference(minNode, key, nodeKeyMax, keyMax, 1);
      diff[0] = key.newInstance(keyMin, nodeKeyMin);
      return diff;
    }

    for (Node node = root; node != null;) { // [X]
      nodeKey = node.getKey();
      nodeKeyMin = nodeKey.getMin();
      if (nodeKeyMin != null && key.compare(keyMin, nodeKeyMin) < 0) {
        minNode = node;
        node = node.getLeft();
      }
      else {
        nodeKeyMax = nodeKey.getMax();
        if (key.compare(keyMin, nodeKeyMax) > 0)
          node = node.getRight();
        else
          return keyMax == null || key.compare(keyMax, nodeKeyMax) > 0 ? difference(node, key, nodeKeyMax, keyMax, 0) : emptyIntervals;
      }
    }

    if (minNode == null)
      return new Interval[] {key.newInstance(keyMin, keyMax)};

    nodeKey = minNode.getKey();
    nodeKeyMax = nodeKey.getMax();

    if (key == null && keyMax != null && nodeKey.compare(keyMin, keyMax) >= 0)
      throw new IllegalArgumentException("Illegal interval: " + nodeKey.toString(keyMin, keyMax));

    final Interval<T>[] diff = nodeKeyMax != null && (keyMax == null || nodeKey.compare(keyMax, nodeKeyMax) > 0) ? difference(minNode, key, nodeKeyMax, keyMax, 1) : new Interval[1];
    diff[0] = nodeKey.newInstance(keyMin, nodeKey.getMin());
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

    final Interval<T> nodeKey = node.getKey();
    final T nodeKeyMin = nodeKey.getMin();
    if (max != null && key.compare(max, nodeKeyMin) < 0)
      return newDiffArray(key, min, max, depth);

    final T nodeKeyMax = nodeKey.getMax();
    final Interval<T>[] diff = nodeKeyMax != null && (max == null || key.compare(max, nodeKeyMax) > 0) ? difference(node, key, nodeKeyMax, max, depth + 1) : new Interval[depth + 1];
    diff[depth] = key.newInstance(min, nodeKeyMin);
    return diff;
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

    return root.getMinNode().getKey();
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
    Interval<T> key;
    IntervalNode node = getRoot();
    for (int c; node != null; node = c < 0 ? node.getLeft() : (floor = node).getRight()) { // [X]
      key = node.getKey();
      c = e.compare(min, key.getMin());
      if (c == 0)
        return key;
    }

    return floor == null ? null : floor.getKey();
  }

  @Override
  public void forEach(final Consumer<? super Interval<T>> action) {
    final IntervalNode root = getRoot();
    if (root == null)
      return;

    final int mc = modCount;
    final BinaryTreeIterator i = new BinaryTreeIterator(root);
    while (i.hasNext())
      action.accept(i.next());

    if (modCount != mc)
      throw new ConcurrentModificationException();
  }

  @Override
  protected IntervalNode getRoot() {
    return (IntervalNode)super.getRoot();
  }

  @Override
  public SortedSet<Interval<T>> headSet(final Interval<T> toElement) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> headSet(final Interval<T> toElement, final boolean inclusive) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public Interval<T> higher(final Interval<T> e) {
    Node node = searchNodeFast(e);
    return node == null ? null : higher(node);
  }

  protected Interval<T> higher(Node node) {
    node = ((IntervalNode)node).higher();
    return node == null ? null : node.getKey();
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
    return root != null && intersects(root, key);
  }

  private boolean intersects(final Interval<T> key, final IntervalNode node) {
    final Interval<T> nodeKey = node.getKey();
    final T nodeKeyMin = nodeKey.getMin();
    final T nodeKeyMax;
    return nodeKeyMin != null && key.compare(key.getMax(), nodeKeyMin) <= 0 ? intersectsLeft(key, node.getLeft()) : (nodeKeyMax = nodeKey.getMax()) != null && key.compare(key.getMin(), nodeKeyMax) >= 0 ? intersectsRight(key, node.getRight()) : key.intersects(nodeKey);
  }

  protected boolean intersects(final IntervalNode root, final Interval<T> key) {
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

    return root.getMaxNode().getKey();
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public Interval<T> lower(final Interval<T> e) {
    final Node node = searchNodeFast(e);
    return node == null ? null : lower(node);
  }

  protected Interval<T> lower(Node node) {
    node = ((IntervalNode)node).lower();
    return node == null ? null : node.getKey();
  }

  private IntervalNode mergeLeft(final Interval<T> key, final IntervalNode node) {
    final T keyMin = key.getMin();
    final IntervalNode left = mergeLeft(key, keyMin, node, node.getLeft());
    final IntervalNode right = node.getRight();
    if (right != null) {
      right.updateHeight();
      node.setRight$(right.rebalance());
    }

    return left;
  }

  /** @formatter:off
   *         __________
   *         |  root  |
   *         ----------
   * ___________
   * |   key   |
   * -----------
   * @formatter:on */
  private IntervalNode mergeLeft(final Interval<T> key, final T keyMin, final IntervalNode node, final IntervalNode child) {
    if (child == null) {
      final Interval<T> nodeKey = node.getKey();
      final T nodeMax = nodeKey.getMax();
      final T keyMax = key.getMax();
      final boolean updateMax = keyMax == null ? nodeMax != null : nodeMax != null && key.compare(keyMax, nodeMax) > 0;
      if (updateMax) {
        node.setRight$(mergeRight(key, keyMax, keyMin, node, node.getRight()));
        // if (nodeKey == node.getKey()) { // Seems to not be needed, because it's guaranteed that `mergeRight` will call `node.setKey()`.
        // node.setKey(key.newInstance(keyMin, nodeMax));
        // ppMod();
        // changed = true;
        // }
      }
      else {
        node.setKey(key.newInstance(keyMin, nodeMax));
        incModCount();
        changed = true;
      }

      return null;
    }

    final Interval<T> childKey = child.getKey();

    /** @formatter:off
     *       ___________
     *       |  child  |
     *       -----------
     *    ___________
     *    |   key   |
     *    -----------
     *    |
     *  keyMin
     * @formatter:on */
    final T childMin = childKey.getMin();
    if (keyMin == null || childMin != null && key.compare(keyMin, childMin) <= 0) {
      // node.setMinNode(node); // FIXME: Is this needed?
      // Skip the child, and merge to its left
      incModCount();
      changed = true;
      return mergeLeft(key, keyMin, node, child.getLeft());
    }

    /** @formatter:off
     *   ___________
     *   |  child  |
     *   -----------
     *                ___________
     *                |   key   |
     *                -----------
     *                |
     *              keyMin
     * @formatter:on */
    if (key.compare(keyMin, childKey.getMax()) > 0) {
      // Keep the `child`, and merge to its right
      return child.setRight(mergeLeft(key, keyMin, node, child.getRight()));
    }

    final Interval<T> nodeKey = node.getKey();
    final T nodeKeyMax = nodeKey.getMax();
    final T keyMax = key.getMax();
    final boolean updateMax = keyMax == null ? nodeKeyMax != null : key.compare(keyMax, nodeKeyMax) > 0;
    if (updateMax) {
      final boolean updateMin = childMin == null || key.compare(childMin, nodeKey.getMin()) < 0;
      node.setRight$(mergeRight(key, keyMax, updateMin ? childMin : keyMin, node, node.getRight()));
      // if (nodeKey == node.getKey() && updateMin) { // Seems to not be needed, because it's guaranteed that `mergeRight` will call
      // `node.setKey()`.
      // node.setKey(key.newInstance(childMin, nodeMax));
      // ppMod();
      // changed = true;
      // }
    }
    else { /* if (key.compare(childMin, nodeKey.getMin()) < 0) { */
       // Not needed, because mergeLeft is called for c = -1, which guarantees this exact condition.
      node.setKey(key.newInstance(childMin, nodeKeyMax));
      incModCount();
      changed = true;
    }

    return child.getLeft();
  }

  private IntervalNode mergeRight(final Interval<T> key, final IntervalNode node) {
    final T keyMax = key.getMax();
    return mergeRight(key, keyMax, node.getKey().getMin(), node, node.getRight());
  }

  /** @formatter:off
   *  __________
   *  |  root  |
   *  ----------
   *        ___________
   *        |   key   |
   *        -----------
   * @formatter:on */
  private IntervalNode mergeRight(final Interval<T> key, T keyMax, final T nodeKeyMin, final IntervalNode node, final IntervalNode child) {
    if (child == null) {
      final T nodeKeyMax = node.getKey().getMax();
      if (nodeKeyMax != null && (keyMax == null || key.compare(keyMax, nodeKeyMax) > 0)) {
        node.setKey(key.newInstance(nodeKeyMin, keyMax));
        incModCount();
        changed = true;
      }

      return null;
    }

    /** @formatter:off
     *   ___________
     *   |  child  |
     *   -----------
     *      ___________
     *      |   key   |
     *      -----------
     *                |
     *              keyMax
     * @formatter:on */
    final Interval<T> childKey = child.getKey();
    final T childKeyMax;
    if (keyMax == null || (childKeyMax = childKey.getMax()) != null && key.compare(keyMax, childKeyMax) > 0) {
      // Skip the child, and merge to its right
      incModCount();
      changed = true;
      return mergeRight(key, keyMax, nodeKeyMin, node, child.getRight());
    }

    /**@formatter:off
     *              ___________
     *              |  child  |
     *              -----------
     * ___________
     * |   key   |
     * -----------
     *           |
     *         keyMax
     * @formatter:on */
    if (key.compare(keyMax, childKey.getMin()) < 0) {
      // Keep the `child`, and merge to its left
      return child.setLeft(mergeRight(key, keyMax, nodeKeyMin, node, child.getLeft()));
    }

    keyMax = childKeyMax;

    { /* if (key.compare(keyMax, childKey.getMax()) > 0) { */
       // Not needed, because mergeRight is called for c = 1, which guarantees this exact condition.
      node.setKey(key.newInstance(nodeKeyMin, keyMax));
      incModCount();
      changed = true;
    }

    return child.getRight();
  }

  @SuppressWarnings("unchecked")
  private Interval<T>[] newDiffArray(final Interval<T> key, final T min, final T max, final int depth) {
    final Interval<T>[] diff = new Interval[depth + 1];
    diff[depth] = key.newInstance(min, max);
    return diff;
  }

  @Override
  protected IntervalNode newNode(final Interval<T> key) {
    return new IntervalNode(key);
  }

  /**
   * {@inheritDoc}
   *
   * @complexity O(log(n))
   */
  @Override
  public Interval<T> pollFirst() {
    final IntervalNode root = getRoot();
    return root == null ? null : pollFirst(root);
  }

  protected Interval<T> pollFirst(final IntervalNode root) {
    final IntervalNode node = root.getMinNode();
    final Interval<T> key = node.getKey();
    node.delete();
    return key;
  }

  /**
   * {@inheritDoc}
   *
   * @complexity O(log(n))
   */
  @Override
  public Interval<T> pollLast() {
    final IntervalNode root = getRoot();
    return root == null ? null : pollLast(root);
  }

  protected Interval<T> pollLast(final IntervalNode root) {
    final IntervalNode node = root.getMaxNode();
    final Interval<T> key = node.getKey();
    node.delete();
    return key;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException If the provided {@link Interval} is null.
   * @complexity O(log(n))
   */
  @Override
  public boolean remove(final Interval<T> key) {
    return deleteFast(key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean remove(final Object o) {
    return delete((Interval<T>)o);
  }

  protected boolean remove(final IntervalNode root, final Interval<T> key) {
    if (!key.intersects(root.getMinNodeMin(), root.getMaxNodeMax()))
      return false;

    changed = false;
    final IntervalNode newRoot = deleteNodeUnsafe(key, root);
    if (newRoot != root) {
      if (newRoot != null)
        newRoot.setParent(null);

      incModCount();
      setRoot(newRoot);
      return true;
    }

    return changed;
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    return super.removeAll(c);
  }

  @Override
  public boolean removeIf(final Predicate<? super Interval<T>> filter) {
    final IntervalNode root = getRoot();
    if (root == null)
      return false;

    int mc = modCount;
    boolean removed = false;
    final BinaryTreeIterator i = new BinaryTreeIterator(root);
    while (i.hasNext()) {
      if (filter.test(i.next())) {
        ++mc;
        i.remove();
        ++modCount;
        removed = true;
      }
    }

    if (modCount != mc)
      throw new ConcurrentModificationException();

    return removed;
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return super.retainAll(c);
  }

  @Override
  protected Node searchNodeFast(final Interval<T> key) {
    final IntervalNode root = getRoot();
    if (root == null)
      return null;

    final T keyMin = key.getMin();
    if (keyMin == null) {
      final IntervalNode minNode = root.getMinNode();
      return minNode.getKey().getMin() == null ? minNode : null;
    }

    final T minNodeMin = root.getMinNodeMin();
    final T maxNodeMax = root.getMaxNodeMax();
    if (minNodeMin != null && key.compare(keyMin, minNodeMin) < 0 || maxNodeMax != null && key.compare(keyMin, maxNodeMax) >= 0)
      return null;

    return searchNode(keyMin, root);
  }

  private Node searchNode(final T keyMin, final IntervalNode node) {
    final Interval<T> nodeKey = node.getKey();
    final T nodeKeyMin = nodeKey.getMin();
    final T nodeKeyMax;
    return nodeKeyMin != null && nodeKey.compare(keyMin, nodeKeyMin) < 0 ? searchNodeLeft(keyMin, node.getLeft()) : (nodeKeyMax = nodeKey.getMax()) != null && nodeKey.compare(keyMin, nodeKeyMax) >= 0 ? searchNodeRight(keyMin, node.getRight()) : node;
  }

  private Node searchNodeLeft(final T keyMin, final IntervalNode node) {
    final Interval<T> nodeKey;
    return node == null || (nodeKey = node.getMaxNode().getKey()).compare(keyMin, nodeKey.getMax()) >= 0 ? null : searchNode(keyMin, node);
  }

  private Node searchNodeRight(final T keyMin, final IntervalNode node) {
    final Interval<T> nodeKey;
    return node == null || (nodeKey = node.getMinNode().getKey()).compare(keyMin, nodeKey.getMin()) < 0 ? null : searchNode(keyMin, node);
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
  public SortedSet<Interval<T>> tailSet(final Interval<T> fromElement) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }

  @Override
  public NavigableSet<Interval<T>> tailSet(final Interval<T> fromElement, final boolean inclusive) {
    // FIXME: Implement this.
    throw new UnsupportedOperationException();
  }
}