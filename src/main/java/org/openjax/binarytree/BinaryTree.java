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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.libj.lang.Strings;
import org.libj.util.ArrayUtil;
import org.libj.util.Iterators;

/**
 * Abstract class defining base methods and structures of a <a href="https://en.wikipedia.org/wiki/Binary_tree">Binary Tree</a>.
 *
 * @param <T> The type parameter of values belonging to this tree.
 */
public abstract class BinaryTree<T extends Comparable<? super T>> implements Cloneable {
  protected class BinaryTreeIterator implements Iterator<T> {
    private Node prevPrev = null, prev = null, next;

    BinaryTreeIterator(final Node root) {
      next = root.getMinNode();
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public T next() {
      return _next();
    }

    protected T _next() {
      if (next == null)
        throw new NoSuchElementException();

      final T data = next.getData();
      prevPrev = prev;
      prev = next;

      final Node right = next.getRight();
      if (right != null) {
        next = right.getMinNode();
      }
      else {
        Node next = this.next;
        while ((next = next.getParent()) != null && next.getLeft() != this.next)
          this.next = next;

        this.next = next;
      }

      return data;
    }

    @Override
    public void remove() {
      if (prev == null)
        throw new IllegalStateException();

      prev.delete();
      next = prev = prevPrev;
      if (next != null) {
        _next();
      }
      else {
        final Node root = getRoot();
        if (root != null)
          next = root.getMinNode();
      }
    }
  }

  void ppMod() {
    ++modCount;
  }

  protected class Node {
    private T data;
    private Node parent;
    private Node left;
    private Node right;
    private int size = 1;

    protected Node(final T data) {
      setData(data);
    }

    protected Node clone(final BinaryTree<T> tree) {
      final Node clone = tree.newNode(data);
      clone.size = size;

      if (left != null) {
        final Node leftClone = clone.left = left.clone(tree);
        leftClone.parent = clone;
      }

      if (right != null) {
        final Node rightClone = clone.right = right.clone(tree);
        rightClone.parent = clone;
      }

      return clone;
    }

    protected void delete() {
      final Node parent = getParent();
      final Node left = getLeft();
      final Node right = getRight();
      if (left == null) {
        if (right == null) {
          if (parent == null)
            setRoot(null);
          else if (parent.getLeft() == this)
            replaceLeft(parent, null);
          else
            replaceRight(parent, null);
        }
        else {
          setData(right.getData());
          replaceLeft(this, right.getLeft());
          replaceRight(this, right.getRight());
        }
      }
      else if (right == null) {
        setData(left.getData());
        replaceLeft(this, left.getLeft());
        replaceRight(this, left.getRight());
      }
      else {
        final Node inOrderSuccessor = right.getMinNode();
        setData(inOrderSuccessor.getData());
        replaceInOrderSuccessor(inOrderSuccessor, right);
      }
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this)
        return true;

      if (!(obj instanceof BinaryTree.Node))
        return false;

      final BinaryTree<?>.Node that = (BinaryTree<?>.Node)obj;
      return Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    protected T getData() {
      return data;
    }

    protected Node getLeft() {
      return left;
    }

    protected Node getMaxNode() {
      Node node = this;
      while (node.right != null)
        node = node.right;

      return node;
    }

    protected Node getMinNode() {
      Node node = this;
      while (node.left != null)
        node = node.left;

      return node;
    }

    protected Node getParent() {
      return parent;
    }

    protected Node getRight() {
      return right;
    }

    protected int getSize() {
      return size;
    }

    protected String getText() {
      return data.toString() + " {S=" + size + "}";
    }

    @Override
    public int hashCode() {
      int hashCode = 0;
      if (left != null)
        hashCode = hashCode * 31 + left.hashCode();

      if (right != null)
        hashCode = hashCode * 31 + right.hashCode();

      return hashCode;
    }

    protected void replaceInOrderSuccessor(final Node inOrderSuccessor, final Node right) {
      if (inOrderSuccessor == right)
        replaceRight(this, inOrderSuccessor.getRight());
      else
        replaceLeft(inOrderSuccessor.getParent(), inOrderSuccessor.getRight());
    }

    protected Node replaceLeft(final Node node, final Node child) {
      final Node rebalanced = node.setLeft(child);
      Node parent = node;
      do
        parent.updateNode();
      while ((parent = parent.getParent()) != null);
      return rebalanced;
    }

    protected Node replaceRight(final Node node, final Node child) {
      final Node rebalanced = node.setRight(child);
      Node parent = node;
      do
        parent.updateNode();
      while ((parent = parent.getParent()) != null);
      return rebalanced;
    }

    protected void setData(final T data) {
      this.data = data;
    }

    protected Node setLeft(final Node node) {
      if (node != null)
        node.setParent(this);

      this.left = node;
      updateSize();
      return this;
    }

    protected void setParent(final Node parent) {
      this.parent = parent;
    }

    protected Node setRight(final Node node) {
      if (node != null)
        node.setParent(this);

      this.right = node;
      updateSize();
      return this;
    }

    @Override
    public String toString() {
      final StringBuilder b = new StringBuilder();
      final ArrayList<ArrayList<String>> lines = new ArrayList<>();

      ArrayList<Node> levels = new ArrayList<>();
      ArrayList<Node> next = new ArrayList<>();

      levels.add(this);
      int nn = 1;

      int widest = 0;

      while (nn != 0) {
        final ArrayList<String> line = new ArrayList<>();

        nn = 0;

        for (int i = 0, i$ = levels.size(); i < i$; ++i) { // [RA]
          final Node node = levels.get(i);
          if (node == null) {
            line.add(null);

            next.add(null);
            next.add(null);
          }
          else {
            final String aa = node.getText();
            line.add(aa);
            if (aa.length() > widest)
              widest = aa.length();

            next.add(node.getLeft());
            next.add(node.getRight());

            if (node.getLeft() != null)
              ++nn;

            if (node.getRight() != null)
              ++nn;
          }
        }

        if (widest % 2 == 1)
          ++widest;

        lines.add(line);

        final ArrayList<Node> tmp = levels;
        levels = next;
        next = tmp;
        next.clear();
      }

      final int noLines = lines.size();
      int perpiece = lines.get(noLines - 1).size() * (widest + 4);
      for (int i = 0; i < noLines; ++i) { // [RA]
        final ArrayList<String> line = lines.get(i);
        final int hpw = (int)Math.floor(perpiece / 2d) - 1;

        final int length = line.size();
        if (i > 0) {
          for (int j = 0; j < length; ++j) { // [RA]
            final boolean even = j % 2 == 0;
            // split Node
            char c = ' ';
            if (!even) {
              if (line.get(j - 1) != null) {
                c = (line.get(j) != null) ? '┴' : '┘';
              }
              else {
                if (j < length && line.get(j) != null)
                  c = '└';
              }
            }

            b.append(c);

            // lines and spaces
            if (line.get(j) == null) {
              for (int k = 0; k < perpiece - 1; ++k) // [N]
                b.append(' ');
            }
            else {
              for (int k = 0; k < hpw; ++k) // [N]
                b.append(even ? ' ' : '─');

              b.append(even ? '┌' : '┐');
              for (int k = 0; k < hpw; ++k) // [N]
                b.append(even ? '─' : ' ');
            }
          }

          b.append('\n');
        }

        // print line of numbers
        for (int j = 0; j < length; ++j) { // [RA]
          String str = line.get(j);
          if (str == null)
            str = "";

          final int len = str.length();
          final int gap1 = (int)Math.ceil(perpiece / 2d - len / 2d);
          final int gap2 = (int)Math.floor(perpiece / 2d - len / 2d);

          // a number
          for (int k = 0; k < gap1; ++k) // [N]
            b.append(' ');

          b.append(str);
          for (int k = 0; k < gap2; ++k) // [N]
            b.append(' ');
        }

        b.append('\n');
        perpiece /= 2;
      }

      int spaces = Integer.MAX_VALUE;
      for (int i = 0, j = 0, ch = '\0', i$ = b.length(), end = i$ - 1; i < i$; ++i, ++j) { // [N]
        ch = b.charAt(i);
        if (ch != ' ' || i == end) {
          spaces = Math.min(spaces, j);
          if (i == end)
            break;

          i = Strings.indexOf(b, '\n', i) + 1;
          j = -1;
        }
      }

      if (spaces > 1) {
        int pos = b.length();
        while ((pos = Strings.lastIndexOf(b, '\n', pos - spaces) + 1) > 0)
          b.delete(pos, pos + spaces);

        b.delete(pos, pos + spaces);
      }

      return b.toString();
    }

    protected void updateNode() {
      updateSize();
    }

    protected void updateSize() {
      size = size(left) + size(right) + 1;
    }
  }

  /**
   * Returns the size of the provided {@link BinaryTree.Node}, or {@code 0} if the provided {@link BinaryTree.Node} is null. A
   * node's size defined as the the count of the node itself (i.e. {@code 1}), plus the count of all of the node's children.
   *
   * @param node The {@link BinaryTree.Node} for which to return the size.
   * @return The size of the provided {@link BinaryTree.Node}, or {@code 0} if the provided {@link BinaryTree.Node} is null.
   * @complexity O(1)
   */
  protected static int size(final BinaryTree<?>.Node node) {
    return node != null ? node.size : 0;
  }

  private static int toArray(final BinaryTree<?>.Node n, final Object[] a, int index) {
    if (n == null)
      return index;

    index = toArray(n.getLeft(), a, index);
    a[index++] = n.getData();
    return toArray(n.getRight(), a, index);
  }

  private static void toString(final StringBuilder b, final BinaryTree<?>.Node n) {
    if (n == null)
      return;

    toString(b, n.getLeft());
    b.append(',').append(n.getData());
    toString(b, n.getRight());
  }

  /**
   * The number of times this {@link IntervalTreeSet} has been structurally modified. Structural modifications are those that change
   * the number of mappings in the {@link IntervalTreeSet} or otherwise modify its internal structure (e.g., rotate). This field is
   * used to make iterators on Collection-views of the {@link IntervalTreeSet} fail-fast. (See {@link ConcurrentModificationException}).
   */
  protected transient int modCount;
  private Node root;

  /**
   * Removes all of the elements from this set (i.e. the set will be empty after this method returns).
   *
   * @complexity O(1)
   */
  public void clear() {
    ppMod();
    setRoot(null);
  }

  /**
   * Returns a copy of this set, with a guarantee for any object {@code x}:
   * <ol>
   * <li>{@code x.clone() != x}</li>
   * <li>{@code x.clone().getClass() == x.getClass()}</li>
   * <li>{@code x.clone().equals(x)}</li>
   * </ol>
   *
   * @return A copy of this set.
   * @complexity O(n)
   */
  @Override
  @SuppressWarnings("unchecked")
  public BinaryTree<T> clone() {
    try {
      final BinaryTree<T> clone = (BinaryTree<T>)super.clone();
      if (root != null)
        clone.root = root.clone(clone);

      return clone;
    }
    catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  protected boolean equals(final BinaryTree<?> tree) {
    return Objects.equals(getRoot(), tree.getRoot());
  }

  /**
   * {@inheritDoc}
   *
   * @complexity O(n)
   */
  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof BinaryTree && equals((BinaryTree<?>)obj);
  }

  protected Node getRoot() {
    return root;
  }

  /**
   * {@inheritDoc}
   *
   * @complexity O(n)
   */
  @Override
  public int hashCode() {
    final Node root = getRoot();
    return root == null ? 0 : hashCode(root);
  }

  protected int hashCode(final Node root) {
    return root.hashCode();
  }

  /**
   * Returns {@code true} if this set has zero elements, otherwise {@code false}.
   *
   * @return {@code true} if this set has zero elements, otherwise {@code false}.
   * @complexity O(1)
   */
  public boolean isEmpty() {
    return getRoot() == null;
  }

  /**
   * Returns an {@link Iterator} over the elements in this set in ascending order.
   *
   * @return An {@link Iterator} over the elements in this set in ascending order.
   * @complexity O(1)
   */
  public Iterator<T> iterator() {
    final Node root = getRoot();
    return root == null ? Iterators.empty() : new BinaryTreeIterator(root) {
      private int modCount = BinaryTree.this.modCount;

      @Override
      public T next() {
        final T next = super.next();
        if (modCount != BinaryTree.this.modCount)
          throw new ConcurrentModificationException();

        return next;
      }

      @Override
      public void remove() {
        ++modCount;
        super.remove();
        ++BinaryTree.this.modCount;
      }
    };
  }

  /**
   * Returns a new instance of this class's specific {@link Node} subclass with the provided key.
   *
   * @param key The key.
   * @return A new instance of this class's specific {@link Node} subclass with the provided key.
   * @complexity O(1)
   */
  protected Node newNode(final T key) {
    return new Node(key);
  }

  protected void setRoot(final Node root) {
    this.root = root;
  }

  /**
   * Returns the number of elements in this set.
   *
   * @return The number of elements in this set.
   * @complexity O(1)
   */
  public int size() {
    final Node root = getRoot();
    return root != null ? root.getSize() : 0;
  }

  /**
   * Returns an array containing all of the elements in this set in ascending order. The returned array's
   * {@linkplain Class#getComponentType runtime component type} is {@code Object}.
   * <p>
   * The returned array will be "safe" in that no references to it are maintained by this set. (In other words, this method
   * allocates a new array). The caller is thus free to modify the returned array.
   *
   * @return An array containing all of the elements in this set in ascending order.
   * @complexity O(n)
   */
  public Object[] toArray() {
    final Node root = getRoot();
    return root != null ? toArray(root) : ArrayUtil.EMPTY_ARRAY;
  }

  /**
   * Returns an array containing all of the elements in this set in ascending order; the runtime type of the returned array is that
   * of the specified array. If the set fits in the specified array, it is returned therein. Otherwise, a new array is allocated
   * with the runtime type of the specified array and the size of this set.
   * <p>
   * If this set fits in the specified array with room to spare (i.e., the array has more elements than this set), the element in
   * the array immediately following the end of the set is set to {@code null}.
   * <p>
   * The returned array will be "safe" in that no references to it are maintained by this set. (In other words, this method
   * allocates a new array). The caller is thus free to modify the returned array.
   *
   * @param <E> The component type of the array to contain the set.
   * @param a The array into which the elements of this set are to be stored, if it is big enough; otherwise, a new array of the
   *          same runtime type is allocated for this purpose.
   * @return An array containing all of the elements in this set.
   * @throws ArrayStoreException If the runtime type of any element in this set is not assignable to the
   *           {@linkplain Class#getComponentType runtime component type} of the specified array.
   * @throws NullPointerException If the specified array is null.
   * @complexity O(n)
   */
  public <E>E[] toArray(E[] a) {
    final Node root = getRoot();
    if (root != null)
      return toArray(root, a);

    if (a.length > 0)
      a[0] = null;

    return a;
  }

  protected Object[] toArray(final Node node) {
    final int size = size();
    final Object[] a = new Object[size];
    toArray(node, a, 0);
    return a;
  }

  @SuppressWarnings("unchecked")
  protected <E>E[] toArray(final Node node, E[] a) {
    final int size = size();
    if (a.length < size)
      a = (E[])Array.newInstance(a.getClass().getComponentType(), size);

    toArray(node, a, 0);

    if (a.length > size)
      a[size] = null;

    return a;
  }

  /**
   * {@inheritDoc}
   *
   * @complexity O(n)
   */
  @Override
  public String toString() {
    final Node root = getRoot();
    return root == null ? "[]" : toString(root).toString();
  }

  protected StringBuilder toString(final Node root) {
    final StringBuilder b = new StringBuilder();
    toString(b, root);
    b.setCharAt(0, '[');
    b.append(']');
    return b;
  }
}