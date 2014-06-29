package org.basex.query.up.expr;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract update expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class Update extends Arr {
  /** Static context. */
  final StaticContext sc;

  /**
   * Constructor.
   * @param sctx static context
   * @param info input info
   * @param expr expressions
   */
  Update(final StaticContext sctx, final InputInfo info, final Expr... expr) {
    super(info, expr);
    sc = sctx;
    type = SeqType.EMP;
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.UPD || super.has(flag);
  }

  /**
   * Checks if the new namespaces have conflicting namespaces.
   * @param list node list
   * @param targ target node
   * @throws QueryException query exception
   * @return specified node list
   */
  final ANodeList checkNS(final ANodeList list, final ANode targ) throws QueryException {
    for(final ANode n : list) {
      final QNm name = n.qname();
      final byte[] pref = name.prefix();
      // attributes without prefix have no namespace
      if(pref.length == 0) continue;
      // check if attribute and target have the same namespace
      final byte[] uri = targ.uri(pref);
      if(uri != null && !eq(name.uri(), uri)) throw UPNSCONFL.get(info);
    }
    return list;
  }
}
