package org.basex.query.util;

import java.util.*;

import org.basex.query.value.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is a simple container for values.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class ValueList extends ElementList {
  /** List entries. */
  private Value[] list;

  /**
   * Default constructor.
   */
  ValueList() {
    this(Array.CAPACITY);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param c array capacity
   */
  private ValueList(final int c) {
    list = new Value[c];
  }

  /**
   * Adds an element to the list.
   * @param e element be added
   */
  public void add(final Value e) {
    if(size == list.length) list = Array.copy(list, new Value[newSize()]);
    list[size++] = e;
  }

  /**
   * Returns the element at the specified index.
   * @param i index
   * @return element
   */
  public Value get(final int i) {
    return list[i];
  }

  @Override
  public String toString() {
    return Util.className(this) + Arrays.toString(Arrays.copyOf(list, size));
  }
}
