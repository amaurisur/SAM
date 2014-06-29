package org.basex.query.up.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Replace expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class Replace extends Update {
  /** 'Value of' flag. */
  private final boolean value;

  /**
   * Constructor.
   * @param sctx static context
   * @param info input info
   * @param trg target expression
   * @param src source expression
   * @param value replace value of
   */
  public Replace(final StaticContext sctx, final InputInfo info, final Expr trg, final Expr src,
      final boolean value) {
    super(sctx, info, trg, src);
    this.value = value;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Constr c = new Constr(ii, sc).add(ctx, expr[1]);
    if(c.errAtt) throw UPNOATTRPER.get(info);
    if(c.duplAtt != null) throw UPATTDUPL.get(info, new QNm(c.duplAtt));

    final Iter t = ctx.iter(expr[0]);
    final Item i = t.next();
    // check target constraints
    if(i == null) throw UPSEQEMP.get(info, Util.className(this));
    final Type tp = i.type;
    if(!(i instanceof ANode) || tp == NodeType.DOC || t.next() != null)
      throw UPTRGMULT.get(info);
    final ANode targ = (ANode) i;
    final Updates updates = ctx.resources.updates();
    final DBNode dbn = updates.determineDataRef(targ, ctx);

    // replace node
    final ANodeList aList = c.atts;
    ANodeList list = c.children;
    if(value) {
      // replace value of node
      final byte[] txt = list.size() < 1 ? aList.size() < 1 ? EMPTY :
        aList.get(0).string() : list.get(0).string();
      // check validity of future comments or PIs
      if(tp == NodeType.COM) FComm.parse(txt, info);
      if(tp == NodeType.PI) FPI.parse(txt, info);

      updates.add(new ReplaceValue(dbn.pre, dbn.data, info, txt), ctx);
    } else {
      final ANode par = targ.parent();
      if(par == null) throw UPNOPAR.get(info, i);
      if(tp == NodeType.ATT) {
        // replace attribute node
        if(!list.isEmpty()) throw UPWRATTR.get(info);
        list = checkNS(aList, par);
      } else {
        // replace non-attribute node
        if(!aList.isEmpty()) throw UPWRELM.get(info);
      }
      // conforms to specification: insertion sequence may be empty
      updates.add(new ReplaceNode(dbn.pre, dbn.data, info, list), ctx);
    }
    return null;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new Replace(sc, info, expr[0].copy(ctx, scp, vs),
        expr[1].copy(ctx, scp, vs), value);
  }

  @Override
  public String toString() {
    return REPLACE + (value ? ' ' + VALUEE + ' ' + OF : "") +
      ' ' + NODE + ' ' + expr[0] + ' ' + WITH + ' ' + expr[1];
  }
}
