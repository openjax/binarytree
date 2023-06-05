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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.libj.util.CollectionUtil;

/**
 * Abstract subclass of {@link BinaryTree} defining methods for the {@linkplain #add(Comparable) addition},
 * {@linkplain #remove(Comparable) removal}, and {@linkplain #contains(Object) search} of keys.
 *
 * @param <T> The type parameter of values belonging to this tree.
 */
public abstract class BinarySearchTree<T extends Comparable<? super T>> extends BinaryTree<T> implements Collection<T> {
  /**
   * Implementation of {@link BinarySearchTree} utilizing recursive logic.
   *
   * @param <T> The type parameter of values belonging to this tree.
   */
  protected static class Iterative<T extends Comparable<? super T>> extends BinarySearchTree<T> {
    protected class IterativeNode extends Node {
      protected IterativeNode(final T data) {
        super(data);
      }

      @Override
      protected void updateSize() {
        super.updateSize();
        final Node parent = getParent();
        if (parent != null)
          parent.updateSize();
      }
    }

    @Override
    public boolean add(final T key) {
      return addFast(key);
    }

    @Override
    protected boolean addFast(final T key) {
      return insertNode(key) != null;
    }

    @Override
    protected Node deleteNode(final T key, final Node node) {
      final Node left = node.getLeft();
      final Node right = node.getRight();

      if (left == null) {
        set(node, right, key);
      }
      else if (right == null) {
        set(node, left, key);
      }
      else {
        final Node inOrderSuccessor = right.getMinNode();
        node.setData(inOrderSuccessor.getData());
        if (inOrderSuccessor == right)
          node.setRight(inOrderSuccessor.getRight());
        else
          inOrderSuccessor.getParent().setLeft(inOrderSuccessor.getRight());
      }

      ++modCount;
      return node;
    }

    protected Node insertNode(final T key) {
      Node node = getRoot();
      if (node == null) {
        ++modCount;
        setRoot(node = newNode(key));
        return node;
      }

      do {
        final int c = key.compareTo(node.getData());
        if (c < 0) {
          final Node left = node.getLeft();
          if (left != null) {
            node = left;
          }
          else {
            node.setLeft(node = newNode(key));
            return node;
          }
        }
        else if (c > 0) {
          final Node right = node.getRight();
          if (right != null) {
            node = right;
          }
          else {
            node.setRight(node = newNode(key));
            return node;
          }
        }
        else {
          return null;
        }
      }
      while (true);
    }

    @Override
    protected Node newNode(final T key) {
      return new IterativeNode(key);
    }

    @Override
    public boolean remove(final T key) {
      return removeFast(key);
    }

    @Override
    protected boolean removeFast(final T key) {
      final Node root = getRoot();
      Node node = root;

      T data;
      while (node != null && !key.equals((data = node.getData())))
        node = key.compareTo(data) < 0 ? node.getLeft() : node.getRight();

      if (node == null)
        return false;

      deleteNode(key, node);
      return true;
    }

    @Override
    protected Node searchNode(final T key) {
      Node node = getRoot();
      while (node != null) {
        if (Objects.equals(node.getData(), key))
          return node;

        node = key.compareTo(node.getData()) < 0 ? node.getLeft() : node.getRight();
      }

      return null;
    }

    private void set(final Node node, final Node child, final T key) {
      final Node parent = node.getParent();
      if (node == getRoot()) {
        setRoot(child);
      }
      else {
        if (key.compareTo(parent.getData()) < 0)
          parent.setLeft(child);
        else
          parent.setRight(child);
      }

      if (child != null)
        child.setParent(parent);
    }
  }

  /**
   * Implementation of {@link BinarySearchTree} utilizing iterative logic.
   *
   * @param <T> The type parameter of values belonging to this tree.
   */
  protected static class Recursive<T extends Comparable<? super T>> extends BinarySearchTree<T> {
    protected transient boolean changed;

    @Override
    public boolean add(final T key) {
      return addFast(key);
    }

    @Override
    protected boolean addFast(T key) {
      changed = false;
      setRoot(insertNode(key, getRoot()));
      return changed;
    }

    @Override
    protected Node deleteNode(final T key, final Node node) {
      if (node == null)
        return null;

      final Node left = node.getLeft();
      final Node right = node.getRight();
      final int c = key.compareTo(node.getData());
      if (c < 0)
        return node.setLeft(deleteNode(key, left));

      if (c > 0)
        return node.setRight(deleteNode(key, right));

      ++modCount;
      changed = true;

      if (left == null) {
        if (right == null)
          return null;

        right.setParent(node.getParent());
        return right;
      }
      else if (right == null) {
        left.setParent(node.getParent());
        return left;
      }
      else {
        final Node inOrderSuccessor = right.getMinNode();
        final T data = inOrderSuccessor.getData();
        node.setData(data);
        return node.setRight(deleteNode(data, right));
      }
    }

    protected Node insertNode(final T key, final Node node) {
      if (node == null) {
        ++modCount;
        changed = true;
        return newNode(key);
      }

      final int c = key.compareTo(node.getData());
      return c < 0 ? node.setLeft(insertNode(key, node.getLeft())) : c > 0 ? node.setRight(insertNode(key, node.getRight())) : node;
    }

    @Override
    public boolean remove(final T key) {
      return removeFast(key);
    }

    @Override
    protected boolean removeFast(final T key) {
      changed = false;
      setRoot(deleteNode(key, getRoot()));
      return changed;
    }

    @Override
    protected Node searchNode(final T key) {
      return searchNode(key, getRoot());
    }

    private Node searchNode(final T key, final Node node) {
      final T data;
      return node == null ? null : Objects.equals((data = node.getData()), key) ? node : searchNode(key, key.compareTo(data) < 0 ? node.getLeft() : node.getRight());
    }
  }

  /**
   * Ensures that this tree contains the specified key. Returns {@code true} if this tree changed as a result of the call, and
   * {@code false} if this tree already contains the specified element.
   *
   * @param key The key to be added.
   * @return {@code true} if this tree changed as a result of the call, and {@code false} if this tree already contains the
   *         specified element.
   * @throws NullPointerException If the specified key is null.
   */
  @Override
  public abstract boolean add(T key);

  @Override
  @SuppressWarnings("unchecked")
  public boolean addAll(final Collection<? extends T> c) {
    if (c instanceof BinaryTree)
      return addAll(((BinaryTree<T>)c).getRoot());

    final int i$ = c.size();
    if (i$ == 0)
      return false;

    boolean changed = false;
    final List<? extends T> l;
    if (c instanceof List && CollectionUtil.isRandomAccess(l = (List<? extends T>)c)) {
      int i = 0; do // [RA]
        changed |= addFast(l.get(i));
      while (++i < i$);
    }
    else {
      final Iterator<? extends T> it = c.iterator(); do // [I]
        changed |= addFast(it.next());
      while (it.hasNext());
    }

    return changed;
  }

  private boolean addAll(final Node n) {
    if (n == null)
      return false;

    boolean changed = false;
    changed |= addAll(n.getLeft());
    changed |= addFast(n.getData());
    changed |= addAll(n.getRight());
    return changed;
  }

  protected abstract boolean addFast(T key);

  @Override
  public BinarySearchTree<T> clone() {
    return (BinarySearchTree<T>)super.clone();
  }

  /**
   * Returns {@code true} if this tree contains the specified element, otherwise {@code false}.
   *
   * @param o The element whose presence in this tree is to be tested.
   * @return {@code true} if this tree contains the specified key, otherwise {@code false}.
   * @throws ClassCastException If the type of the specified element is incompatible with this tree.
   * @throws NullPointerException If the specified key is null.
   */
  @Override
  public boolean contains(final Object o) {
    return containsFast(o);
  }

  /**
   * Returns {@code true} if this tree contains the specified element, otherwise {@code false}.
   *
   * @param o The element whose presence in this tree is to be tested.
   * @return {@code true} if this tree contains the specified key, otherwise {@code false}.
   * @throws ClassCastException If the type of the specified element is incompatible with this tree.
   * @throws NullPointerException If the specified key is null.
   */
  public boolean contains(final T o) {
    return containsFast(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    final int size = c.size();
    if (size == 0)
      return true;

    final List<?> l;
    if (c instanceof List && CollectionUtil.isRandomAccess(l = (List<?>)c)) {
      int i = 0; do // [RA]
        if (!containsFast(l.get(i)))
          return false;
      while (++i < size);
    }
    else {
      final Iterator<?> it = c.iterator(); do // [I]
        if (!containsFast(it.next()))
          return false;
      while (it.hasNext());
    }

    return true;
  }

  protected boolean containsFast(final T o) {
    return searchNode(o) != null;
  }

  @SuppressWarnings("unchecked")
  protected boolean containsFast(final Object o) {
    return searchNode((T)o) != null;
  }

  protected abstract Node deleteNode(T key, Node node);

  @Override
  @SuppressWarnings("unchecked")
  public boolean remove(final Object o) {
    return remove((T)o);
  }

  /**
   * Removes the specified key from this tree. Returns {@code true} if this collection changed as a result of the call, and
   * {@code false} other if this tree did not contain the specified element.
   *
   * @param key The key to be deleted.
   * @return {@code true} if this collection changed as a result of the call, and {@code false} other if this tree did not contain
   *         the specified element.
   * @throws NullPointerException If the specified key is null.
   */
  public abstract boolean remove(T key);

  @Override
  @SuppressWarnings("unchecked")
  public boolean removeAll(final Collection<?> c) {
    final int i$ = c.size();
    if (i$ == 0)
      return false;

    final int size = size();
    final List<? extends T> l;
    if (c instanceof List && CollectionUtil.isRandomAccess(l = (List<? extends T>)c)) {
      int i = 0; do // [RA]
        removeFast(l.get(i));
      while (++i < i$);
    }
    else {
      final Iterator<? extends T> it = (Iterator<? extends T>)c.iterator(); do // [I]
        removeFast(it.next());
      while (it.hasNext());
    }

    return size != size();
  }

  protected abstract boolean removeFast(T key);

  @Override
  public boolean retainAll(final Collection<?> c) {
    final int size = size();
    final int cSize = c.size();
    if (cSize == 0) {
      if (size == 0)
        return false;

      clear();
      return true;
    }

    final Iterator<T> i = iterator(); do //[I]
      if (!c.contains(i.next()))
        i.remove();
    while (i.hasNext());

    return size != size();
  }

  /**
   * Searches for a node with the given search key.
   *
   * @param key The search key.
   * @return The node, or {@code null} if no node with the given search key exists.
   * @throws NullPointerException If the specified key is null.
   */
  protected abstract Node searchNode(T key);
}