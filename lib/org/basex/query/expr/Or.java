package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Or expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Or extends Logical {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression list
   */
  public Or(final InputInfo ii, final Expr... e) {
    super(ii, e);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    // remove atomic values
    final Expr c = super.compile(ctx, scp);
    return c != this ? c : optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    // merge predicates if possible
    CmpG cmpg = null;
    final ExprList el = new ExprList(expr.length);
    for(final Expr e : expr) {
      boolean merged = false;
      if(e instanceof CmpG) {
        // merge general comparisons
        final CmpG g = (CmpG) e;
        if(cmpg == null) cmpg = g;
        else if(cmpg.union(g, ctx, scp)) merged = true;
      }
      // no optimization found; add original expression
      if(!(merged || e == Bln.FALSE)) {
        if(e == Bln.TRUE) return optPre(Bln.TRUE, ctx);
        el.add(e);
      }
    }

    // all arguments were false()
    if(el.isEmpty()) return optPre(Bln.FALSE, ctx);

    if(expr.length != el.size()) {
      ctx.compInfo(OPTWRITE, this);
      expr = el.finish();
    }
    compFlatten(ctx);

    boolean not = true;
    for(final Expr e : expr) {
      if(!e.isFunction(Function.NOT)) {
        not = false;
        break;
      }
    }

    if(not) {
      ctx.compInfo(OPTWRITE, this);
      final Expr[] inner = new Expr[expr.length];
      for(int i = 0; i < inner.length; i++) inner[i] = ((Arr) expr[i]).expr[0];
      final Expr and = new And(info, inner).optimize(ctx, scp);
      return Function.NOT.get(null, and).optimize(ctx, scp);
    }

    // return single expression if it yields a boolean
    return expr.length == 1 ? compBln(expr[0], info) : this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    for(int i = 0; i < expr.length - 1; i++)
      if(expr[i].ebv(ctx, info).bool(info)) return Bln.TRUE;
    final Expr last = expr[expr.length - 1];
    return tailCall ? last.item(ctx, ii) : last.ebv(ctx, ii).bool(ii) ? Bln.TRUE : Bln.FALSE;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new Or(info, copyAll(ctx, scp, vs, expr));
  }

  @Override
  public boolean indexAccessible(final IndexCosts ic) throws QueryException {
    int is = 0;
    Expr[] exprs = {};
    boolean ia = true;
    for(final Expr e : expr) {
      if(e.indexAccessible(ic) && !ic.seq) {
        // skip expressions without results
        if(ic.costs() == 0) continue;
        is += ic.costs();
      } else {
        ia = false;
      }
      exprs = Array.add(exprs, e);
    }
    ic.costs(is);
    expr = exprs;
    return ia;
  }

  @Override
  public Expr indexEquivalent(final IndexCosts ic) throws QueryException {
    super.indexEquivalent(ic);
    return new Union(info, expr);
  }

  @Override
  public String toString() {
    return toString(' ' + OR + ' ');
  }
}
