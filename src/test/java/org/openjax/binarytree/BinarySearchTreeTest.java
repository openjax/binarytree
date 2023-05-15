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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.libj.test.TestAide;
import org.libj.util.CollectionUtil;

public abstract class BinarySearchTreeTest<BST extends BinarySearchTree<T>,T extends Comparable<? super T>> implements ValueSpec<T> {
  static final ThreadLocalRandom random = ThreadLocalRandom.current();

  static final int repeat = 1000;

  static void debug() {
    System.err.println("Put breakpoint here");
  }

  static void test(final Consumer<Supplier<String>> test) {
    test.accept(() -> {
      if (TestAide.isInDebug()) {
        debug();
        test(test);
      }

      return "";
    });
  }

  static Supplier<String> onError(final Supplier<String> onError, final Supplier<String> messageSupplier) {
    return () -> {
      final String supplied = onError.get();
      final String message = messageSupplier.get();
      return supplied != null && supplied.length() > 0 ? supplied + "\n" + message : message;
    };
  }

  void assertHasKeysInGivenOrderAndAscending(final BST tree, final Collection<T> keys, final Supplier<String> onError) {
    assertTrue(isTreeWithoutDuplicates(this, tree), onError(onError, () -> "Tree is not valid"));

    final Iterator<T> treeIterator = tree.iterator();
    final Iterator<T> keysIterator = keys.iterator();
    T key, next, prev = null;
    for (int i = 0, i$ = keys.size(); i < i$; ++i, prev = next) { // [I]
      assertTrue(keysIterator.hasNext(), onError);
      assertTrue(treeIterator.hasNext(), onError);

      key = keysIterator.next();
      next = treeIterator.next();
      assertEquals(key, next, onError);
      if (prev != null)
        assertTrue(prev.compareTo(next) < 0, onError);
    }

    assertFalse(keysIterator.hasNext(), onError);
    assertFalse(treeIterator.hasNext(), onError);
  }

  private ArrayList<T> shuffle(ArrayList<T> keys) {
    keys = new ArrayList<>(keys);
    Collections.shuffle(keys);
    return keys;
  }

  private T pickRandomKey(final ArrayList<T> keys) {
    return keys.get(random.nextInt(keys.size()));
  }

  @Test
  public final void testInsertingKeysShouldCreateValidTreeWithKeysInOrder() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final ArrayList<T> orderedKeys = createOrderedSequenceOfKeys();
      final ArrayList<T> shuffledKeys = shuffle(orderedKeys);
      test(onError -> addKeys(createTree(), shuffledKeys, orderedKeys, onError));
    }
  }

  @Test
  public final void testShouldReturnFalseWhenInsertingExistingKey() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final ArrayList<T> orderedKeys = createOrderedSequenceOfKeys();
      final ArrayList<T> shuffledKeys = shuffle(orderedKeys);
      final T randomKey = pickRandomKey(shuffledKeys);
      test(onError -> {
        final BST tree = createTree();
        addKeys(tree, shuffledKeys, orderedKeys, onError);
        assertFalse(tree.add(randomKey), onError);
      });
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public final void testToArrayIsCorrect() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final ArrayList<T> orderedKeys = createOrderedSequenceOfKeys();
      final ArrayList<T> shuffledKeys = shuffle(orderedKeys);
      test(onError -> {
        final BST tree = createTree();
        addKeys(tree, shuffledKeys, orderedKeys, onError);

        final Object[] array0 = tree.toArray();
        final T[] array1 = (T[])Array.newInstance(type(), tree.size());
        assertSame(array1, tree.toArray(array1), onError);
        assertArrayEquals(array0, array1, onError);

        final T[] array2 = (T[])Array.newInstance(type(), tree.size() - 1);
        final T[] array3 = tree.toArray(array2);
        assertNotSame(array2, array3, onError);
        assertArrayEquals(array0, array3, onError);

        final T[] array4 = (T[])Array.newInstance(type(), tree.size() + 2);
        Arrays.fill(array4, maxValue());
        assertSame(array4, tree.toArray(array4), onError);
        assertNull(array4[array4.length - 2], onError);
        assertEquals(maxValue(), array4[array4.length - 1], onError);
        for (int i = 0, i$ = array0.length; i < i$; ++i) // [A]
          assertEquals(array0[i], array4[i], onError);
      });
    }
  }

  @Test
  public final void testClearClearsTree() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final ArrayList<T> orderedKeys = createOrderedSequenceOfKeys();
      final ArrayList<T> shuffledKeys = shuffle(orderedKeys);
      final int size = orderedKeys.size();
      test(onError -> {
        final BST tree = createTree();
        addKeys(tree, shuffledKeys, orderedKeys, onError);

        assertFalse(tree.isEmpty(), onError);
        if (!supportsMerging())
          assertEquals(size, tree.size(), onError);

        tree.clear();

        assertEquals(0, tree.size(), onError);
        assertTrue(tree.isEmpty(), onError);
        assertEquals(0, tree.toArray().length, onError);
        assertFalse(tree.iterator().hasNext(), onError);
      });
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public final void testIteratorIsCorrect() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final ArrayList<T> orderedKeys = createOrderedSequenceOfKeys();
      final ArrayList<T> shuffledKeys = shuffle(orderedKeys);
      test(onError -> {
        final BST tree = createTree();
        addKeys(tree, shuffledKeys, orderedKeys, onError);

        final Object[] array = tree.toArray();
        final Iterator<T> iterator = tree.iterator();
        T prev = null;
        for (int i = 0, i$ = array.length; i < i$; ++i) // [A]
          assertNextIterator(tree, prev, prev = (T)array[i], iterator, onError);

        assertFalse(iterator.hasNext(), onError);
      });
    }
  }

  /**
   * Assert the next iteration.
   *
   * @param tree The tree.
   * @param prev The previous iterated value.
   * @param next The next iterated value.
   * @param iterator The {@link Iterator}.
   * @param onError {@link Supplier} to be called when an error occurs.
   */
  void assertNextIterator(final BST tree, final T prev, final T next, final Iterator<T> iterator, final Supplier<String> onError) {
    assertTrue(iterator.hasNext(), onError);
    assertTrue(iterator.hasNext(), onError);
    assertEquals(next, iterator.next(), onError);
  }

  @Test
  @SuppressWarnings("unchecked")
  public final void testSearchFindsAllKeys() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final ArrayList<T> orderedKeys = createOrderedSequenceOfKeys();
      final ArrayList<T> shuffledKeys = shuffle(orderedKeys);
      test(onError -> {
        final BST tree = createTree();
        addKeys(tree, shuffledKeys, orderedKeys, onError);

        final BST clone = (BST)tree.clone();
        for (int i = 0, i$ = shuffledKeys.size(); i < i$; ++i) { // [RA]
          final T key = shuffledKeys.get(i);
          final BinaryTree<T>.Node node1 = assertNodeWithKeyIsPresent(tree, key, onError);
          final BinaryTree<T>.Node node2 = assertNodeWithKeyIsPresent(clone, key, onError);

          assertEquals(node1, node2, onError);
          assertNotSame(node1, node2, onError);
        }
      });
    }
  }

  BinaryTree<T>.Node assertNodeWithKeyIsPresent(final BST tree, final T key, final Supplier<String> onError) {
    final BinaryTree<T>.Node node = tree.searchNode(key);
    assertNotNull(node, onError);
    assertEquals(key, node.getData(), onError);
    return node;
  }

  @Test
  public final void testSearchReturnsNullWhenKeyNotFound() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final ArrayList<T> orderedKeys = createOrderedSequenceOfKeys();
      final ArrayList<T> shuffledKeys = shuffle(orderedKeys);
      test(onError -> {
        final BST tree = createTree();
        addKeys(tree, shuffledKeys, orderedKeys, onError);
        final T highestKey = tree.getRoot().getMaxNode().getData();
        final T key = nextValue(highestKey);
        assertNull(tree.searchNode(key), onError);
      });
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public final void testSearchRemove() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final ArrayList<T> orderedKeys = createOrderedSequenceOfKeys();
      final ArrayList<T> shuffledKeys = shuffle(orderedKeys);
      test(onError1 -> {
        final BST tree = createTree();
        addKeys(tree, shuffledKeys, orderedKeys, onError1);
        final ArrayList<T> keys = CollectionUtil.asCollection(new ArrayList<>(), tree.toArray((T[])Array.newInstance(type(), tree.size())));
        final ArrayList<T> keysToDelete = shuffle(keys);

        test(onError2 -> {
          final BST tree1 = (BST)tree.clone();
          final BST tree2 = (BST)tree.clone();
          final ArrayList<T> keysRemaining = new ArrayList<>(keys);

          OUT:
          for (int i = 0, i$ = keysToDelete.size(); i < i$; ++i) { // [RA]
            final T keyToDelete = keysToDelete.get(i);
            assertTrue(tree1.remove(keyToDelete), onError2);
            assertTrue(keysRemaining.remove(keyToDelete), onError2);
            if (i % 10 == 0) {
              assertValid(tree1, onError2);
              assertHasKeysInGivenOrderAndAscending(tree1, keysRemaining, onError2);
            }

            final Iterator<T> iterator = tree2.iterator();
            while (iterator.hasNext()) {
              final T next = iterator.next();
              if (keyToDelete.equals(next)) {
                iterator.remove();
                assertEquals(tree1, tree2, onError2);
                assertValid(tree2, onError2);
                assertHasKeysInGivenOrderAndAscending(tree2, keysRemaining, onError2);
                break OUT;
              }
            }

            fail(onError2);
          }
        });
      });
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public final void testIteratorRemove() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final ArrayList<T> orderedKeys = createOrderedSequenceOfKeys();
      final ArrayList<T> shuffledKeys = shuffle(orderedKeys);
      test(onError1 -> {
        final BST tree = createTree();
        addKeys(tree, shuffledKeys, orderedKeys, onError1);

        test(onError2 -> {
          final BST clone = (BST)tree.clone();
          int size = clone.size();
          final ArrayList<T> keys = CollectionUtil.asCollection(new ArrayList<>(), clone.toArray((T[])Array.newInstance(type(), size)));
          final Iterator<T> treeIterator = clone.iterator();
          final Iterator<T> keyIterator = keys.iterator();
          for (int i = 0; keyIterator.hasNext(); ++i) { //[I]
            final T keyToDelete = keyIterator.next();
            final BinaryTree<T>.Node node = clone.getRoot().getMinNode();
            assertEquals(keyToDelete, node.getData(), onError2);
            assertTrue(treeIterator.hasNext(), onError2);
            assertEquals(keyToDelete, treeIterator.next(), onError2);
            keyIterator.remove();
            treeIterator.remove();
            assertEquals(--size, clone.size(), onError2);
            if (i % 10 == 0) {
              assertValid(clone, onError2);
              assertHasKeysInGivenOrderAndAscending(clone, keys, onError2);
            }
          }

          assertFalse(treeIterator.hasNext());
        });
      });
    }
  }

  @Test
  public final void testDeleteNotExistingKeyShouldNotChangeTree() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final ArrayList<T> orderedKeys = createOrderedSequenceOfKeys();
      final ArrayList<T> shuffledKeys = shuffle(orderedKeys);
      test(onError -> {
        final BST tree = createTree();
        addKeys(tree, shuffledKeys, orderedKeys, onError);

        final T highestKey = tree.getRoot().getMaxNode().getData();
        final T notExistingKey = nextValue(highestKey);

        assertFalse(tree.remove(notExistingKey), onError);
        assertEquals(tree, tree, onError);
        assertValid(tree, onError);
        assertHasKeysInGivenOrderAndAscending(tree, orderedKeys, onError);
      });
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public final void testClone() {
    for (int r = 0; r < repeat; ++r) { // [N]
      final ArrayList<T> orderedKeys = createOrderedSequenceOfKeys();
      final ArrayList<T> shuffledKeys = shuffle(orderedKeys);
      final BST tree = createTree();
      test(onError1 -> {
        addKeys(tree, shuffledKeys, orderedKeys, onError1);
        test(onError2 -> {
          final BST clone = (BST)tree.clone();
          assertNotSame(tree, clone, onError2);
          assertEquals(tree, clone, onError2);
          assertValid(clone, onError2);
          assertHasKeysInGivenOrderAndAscending(clone, orderedKeys, onError2);
        });
      });
    }
  }

  private void addKeys(final BST tree, final ArrayList<T> shuffledKeys, final ArrayList<T> orderedKeys, final Supplier<String> onError) {
    if (supportsMerging()) {
      for (int i = 0, i$ = shuffledKeys.size(); i < i$; ++i) // [RA]
        tree.add(shuffledKeys.get(i));
    }
    else {
      for (int i = 0, i$ = shuffledKeys.size(); i < i$;) { // [RA]
        final boolean changed = tree.add(shuffledKeys.get(i));
        assertTrue(changed, onError);
        assertEquals(++i, tree.size(), onError);
      }
    }

    assertValid(tree, onError);
    assertHasKeysInGivenOrderAndAscending(tree, orderedKeys, onError);
  }

  final void assertValid(final BST tree, final Supplier<String> onError) {
    final BinaryTree<T>.Node root = tree.getRoot();
    assertSizeSetCorrectly(root, onError);
    assertParentsSetCorrectly(root, null, root, onError);
    assertSpecificTreeInvariants(tree, onError);
  }

  private static int assertSizeSetCorrectly(final BinaryTree<?>.Node node, final Supplier<String> onError) {
    if (node == null)
      return 0;

    final int expectedSize = assertSizeSetCorrectly(node.getLeft(), onError) + assertSizeSetCorrectly(node.getRight(), onError) + 1;
    assertEquals(expectedSize, node.getSize(), onError);
    return expectedSize;
  }

  private static void assertParentsSetCorrectly(final BinaryTree<?>.Node root, final BinaryTree<?>.Node parent, final BinaryTree<?>.Node node, final Supplier<String> onError) {
    if (node == null)
      return;

    if (node == root) {
      assertNull(node.getParent(), onError(onError, () -> "Root must not have a parent"));
    }
    else {
      assertNotNull(node.getParent(), onError(onError, () -> "Node " + node.getData() + " has no parent"));
      assertEquals(parent, node.getParent(), onError(onError, () -> "Parent " + node.getParent().getData() + " of node " + node.getData() + " isn't the expected parent " + parent.getData()));
    }

    assertParentsSetCorrectly(root, node, node.getLeft(), onError);
    assertParentsSetCorrectly(root, node, node.getRight(), onError);
  }

  final ArrayList<T> createShuffledSequenceOfKeys() {
    final ArrayList<T> keys = createOrderedSequenceOfKeys();
    Collections.shuffle(keys);
    return keys;
  }

  abstract BST createTree();
  abstract ArrayList<T> createOrderedSequenceOfKeys();
  abstract boolean supportsMerging();
  abstract void assertSpecificTreeInvariants(BST tree, Supplier<String> onError);
}