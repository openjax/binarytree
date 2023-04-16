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
public abstract class BinaryTree<T extends Comparable<T>> implements Cloneable {
  protected class Node {
    private T data;
    private Node parent;
    private Node left;
    private Node right;
    private int size = 1;

    protected Node(final T data) {
      setData(data);
    }

    protected T getData() {
      return data;
    }

    protected void setData(final T data) {
      this.data = data;
    }

    protected Node getParent() {
      return parent;
    }

    protected void setParent(final Node parent) {
      this.parent = parent;
    }

    protected Node getLeft() {
      return left;
    }

    protected Node setLeft(final Node node) {
      if (node != null)
        node.setParent(this);

      this.left = node;
      updateSize();
      return this;
    }

    protected Node getRight() {
      return right;
    }

    protected Node setRight(final Node node) {
      if (node != null)
        node.setParent(this);

      this.right = node;
      updateSize();
      return this;
    }

    protected int getSize() {
      return size;
    }

    protected void updateSize() {
      size = size(left) + size(right) + 1;
    }

    protected void updateNode() {
      updateSize();
    }

    protected Node getMinNode() {
      Node node = this;
      while (node.left != null)
        node = node.left;

      return node;
    }

    protected Node getMaxNode() {
      Node node = this;
      while (node.right != null)
        node = node.right;

      return node;
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

    protected void replaceInOrderSuccessor(final Node inOrderSuccessor, final Node right) {
      if (inOrderSuccessor == right)
        replaceRight(this, inOrderSuccessor.getRight());
      else
        replaceLeft(inOrderSuccessor.getParent(), inOrderSuccessor.getRight());
    }

    protected String getText() {
      return data.toString() + " {S=" + size + "}";
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
        int index = b.length();
        while ((index = Strings.lastIndexOf(b, '\n', index - spaces) + 1) > 0)
          b.delete(index, index + spaces);

        b.delete(index, index + spaces);
      }

      return b.toString();
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

    @Override
    public boolean equals(final Object obj) {
      if (obj == this)
        return true;

      if (!(obj instanceof BinaryTree.Node))
        return false;

      final BinaryTree<?>.Node that = (BinaryTree<?>.Node)obj;
      return Objects.equals(left, that.left) && Objects.equals(right, that.right);
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
  }

  protected static int size(final BinaryTree<?>.Node node) {
    return node != null ? node.size : 0;
  }

  private Node root;

  protected void setRoot(final Node root) {
    this.root = root;
  }

  protected Node getRoot() {
    return root;
  }

  protected Node newNode(final T key) {
    return new Node(key);
  }

  public Iterator<T> iterator() {
    return isEmpty() ? Iterators.empty() : new Iterator<T>() {
      private Node prevPrev = null, prev = null, next = getRoot().getMinNode();

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public T next() {
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
          next();
        }
        else {
          final Node root = getRoot();
          if (root != null)
            next = root.getMinNode();
        }
      }
    };
  }

  public boolean isEmpty() {
    return getRoot() == null;
  }

  public void clear() {
    setRoot(null);
  }

  public int size() {
    final Node root = getRoot();
    return root != null ? root.getSize() : 0;
  }

  private static int toArray(final BinaryTree<?>.Node n, final Object[] a, int index) {
    if (n == null)
      return index;

    index = toArray(n.getLeft(), a, index);
    a[index++] = n.getData();
    return toArray(n.getRight(), a, index);
  }

  public Object[] toArray() {
    final Node root = getRoot();
    if (root == null)
      return ArrayUtil.EMPTY_ARRAY;

    final int size = size();
    final Object[] a = new Object[size];
    toArray(root, a, 0);
    return a;
  }

  @SuppressWarnings("unchecked")
  public <E>E[] toArray(E[] a) {
    final Node root = getRoot();
    if (root == null) {
      if (a.length > 0)
        a[0] = null;

      return a;
    }

    final int size = size();
    if (a.length < size)
      a = (E[])Array.newInstance(a.getClass().getComponentType(), size);

    toArray(root, a, 0);

    if (a.length > size)
      a[size] = null;

    return a;
  }

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

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof BinaryTree && Objects.equals(root, ((BinaryTree<?>)obj).root);
  }

  @Override
  public int hashCode() {
    return root == null ? 0 : root.hashCode();
  }

  private static void toString(final StringBuilder b, final BinaryTree<?>.Node n) {
    if (n == null)
      return;

    toString(b, n.getLeft());
    b.append(',').append(n.getData());
    toString(b, n.getRight());
  }

  @Override
  public final String toString() {
    final Node root = getRoot();
    if (root == null)
      return "[]";

    final StringBuilder b = new StringBuilder();
    toString(b, root);
    b.setCharAt(0, '[');
    b.append(']');
    return b.toString();
  }
}