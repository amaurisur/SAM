package org.basex.query.up.primitives;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Insert into primitive.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public class InsertInto extends NodeCopy {
  /**
   * Constructor for an insertInto.
   * @param p target pre value
   * @param d target data instance
   * @param i input info
   * @param n node copy insertion sequence
   */
  public InsertInto(final int p, final Data d, final InputInfo i, final ANodeList n) {
    super(UpdateType.INSERTINTO, p, d, i, n);
  }

  @Override
  public final void merge(final Update up) {
    final ANodeList newInsert = ((NodeCopy) up).insert;
    for(final ANode n : newInsert) insert.add(n);
  }

  @Override
  public final void addAtomics(final AtomicUpdateCache l) {
    final int s = data.size(pre, data.kind(pre));
    l.addInsert(pre + s, pre, insseq, false);
  }

  @Override
  public final NodeUpdate[] substitute(final MemData tmp) {
    return new NodeUpdate[] { this };
  }

  @Override
  public final void update(final NamePool pool) { }
}
