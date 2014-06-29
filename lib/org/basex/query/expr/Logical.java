package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Logical expression, extended by {@link And} and {@link Or}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Logical extends Arr {
  /** Tail-call flag. */
  boolean tailCall;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  Logical(final InputInfo ii, final Expr[] e) {
    super(ii, e);
    type = SeqType.BLN;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    final boolean and = this instanceof And;
    final int es = expr.length;
    final ExprList el = new ExprList(es);
    for(final Expr e : expr) {
      final Expr ex = e.compEbv(ctx);
      if(ex.isValue()) {
        // atomic items can be pre-evaluated
        ctx.compInfo(OPTREMOVE, this, e);
        if(ex.ebv(ctx, info).bool(info) ^ and) return Bln.get(!and);
      } else {
        el.add(ex);
      }
    }
    if(el.isEmpty()) return Bln.get(and);
    expr = el.finish();
    return this;
  }

  @Override
  public final void markTailCalls(final QueryContext ctx) {
    // if the last expression surely returns a boolean, we can jump to it
    final Expr last = expr[expr.length - 1];
    if(last.type().eq(SeqType.BLN)) {
      tailCall = true;
      last.markTailCalls(ctx);
    }
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem();
    if(tailCall) el.add(planAttr(TCL, true));
    plan.add(el);
    for(final ExprInfo e : expr) {
      if(e != null) e.plan(el);
    }
  }

  /**
   * Flattens nested logical expressions.
   * @param ctx query context
   */
  final void compFlatten(final QueryContext ctx) {
    // flatten nested expressions
    final int es = expr.length;
    final ExprList tmp = new ExprList(es);
    final boolean and = this instanceof And;
    final boolean or = this instanceof Or;
    for(final Expr ex : expr) {
      if(and && ex instanceof And || or && ex instanceof Or) {
        for(final Expr e : ((Arr) ex).expr) tmp.add(e);
        ctx.compInfo(OPTFLAT, ex);
      } else {
        tmp.add(ex);
      }
    }
    if(es != tmp.size()) expr = tmp.finish();
  }
}
