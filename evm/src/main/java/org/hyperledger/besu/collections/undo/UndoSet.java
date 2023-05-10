package org.hyperledger.besu.collections.undo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A set that supports rolling back the set to a prior state.
 *
 * <p>To register a prior state you want to roll back to call `mark()`. Then use that value in a
 * subsequent call to `undo(mark)`. Every mutation operation across all undoable collections
 * increases the global mark, so a mark set in once collection is usable across all
 * UndoableCollection instances.
 *
 * @param <V> The type of the collection.
 */
public class UndoSet<V> implements Set<V>, UndoableCollection {

  record UndoEntry<V>(V value, boolean add, long level) {
    static <V> UndoSet.UndoEntry<V> add(final V value) {
      return new UndoEntry<>(value, true, UndoableCollection.incrementMarkStatic());
    }

    static <V> UndoSet.UndoEntry<V> remove(final V value) {
      return new UndoEntry<>(value, false, UndoableCollection.incrementMarkStatic());
    }
  }

  Set<V> delegate;
  List<UndoEntry<V>> undoLog;

  /**
   * Create an UndoSet backed by another Set instance.
   *
   * @param delegate The Set instance to use for backing storage
   */
  public UndoSet(final Set<V> delegate) {
    this.delegate = delegate;
    undoLog = new ArrayList<>();
  }

  @Override
  public void undo(final long mark) {
    int pos = undoLog.size() - 1;
    while (pos >= 0 && undoLog.get(pos).level > mark) {
      final var entry = undoLog.get(pos);
      if (entry.add) {
        delegate.remove(entry.value());
      } else {
        delegate.add(entry.value());
      }
      undoLog.remove(pos);
      pos--;
    }
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean contains(final Object key) {
    return delegate.contains(key);
  }

  @Override
  public boolean add(final V key) {
    final boolean added = delegate.add(key);
    if (added) {
      undoLog.add(UndoEntry.add(key));
    }
    return added;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean remove(final Object key) {
    final boolean removed = delegate.remove(key);
    if (removed) {
      undoLog.add(UndoEntry.remove((V) key));
    }
    return removed;
  }

  @Override
  public boolean addAll(@Nonnull final Collection<? extends V> m) {
    boolean added = false;
    for (V v : m) {
      // don't use short circuit, we need to evaluate all entries
      // we also need undo entries for each added entry
      added &= add(v);
    }
    return added;
  }

  @Override
  public boolean removeAll(@Nonnull final Collection<?> c) {
    boolean removed = false;
    for (Object v : c) {
      // don't use short circuit, we need to evaluate all entries
      // we also need undo entries for each removed entry
      removed &= remove(v);
    }
    return removed;
  }

  @Override
  public boolean retainAll(@Nonnull final Collection<?> c) {
    boolean removed = false;
    HashSet<?> hashed = new HashSet<>(c);
    Iterator<V> iter = delegate.iterator();
    while (iter.hasNext()) {
      V v = iter.next();
      if (!hashed.contains(v)) {
        removed = true;
        undoLog.add(UndoEntry.remove(v));
        iter.remove();
      }
    }
    return removed;
  }

  @Override
  public void clear() {
    delegate.forEach(v -> undoLog.add(UndoEntry.remove(v)));
    delegate.clear();
  }

  @Nonnull
  @Override
  public Iterator<V> iterator() {
    return new ReadOnlyIterator<>(delegate.iterator());
  }

  @Nonnull
  @Override
  public Object[] toArray() {
    return delegate.toArray();
  }

  @Nonnull
  @Override
  public <T> T[] toArray(@Nonnull final T[] a) {
    return delegate.toArray(a);
  }

  @Override
  public boolean containsAll(@Nonnull final Collection<?> c) {
    return delegate.containsAll(c);
  }

  @Override
  public boolean equals(final Object o) {
    return o instanceof UndoSet && delegate.equals(o);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode() ^ 0xde1e647e;
  }
}
