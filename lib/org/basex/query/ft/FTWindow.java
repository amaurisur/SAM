package org.basex.query.ft;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTWindow expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTWindow extends FTFilter {
  /** Window. */
  private Expr win;

  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param w window
   * @param u unit
   */
  public FTWindow(final InputInfo ii, final FTExpr e, final Expr w, final FTUnit u) {
    super(ii, e, u);
    win = w;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(win);
    super.checkUp();
  }

  @Override
  public FTExpr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    win = win.compile(ctx, scp);
    return super.compile(ctx, scp);
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc, final FTLexer lex)
      throws QueryException {

    final int n = (int) checkItr(win, ctx) - 1;
    mtc.sort();

    FTStringMatch f = null;
    for(final FTStringMatch m : mtc) {
      if(m.exclude) continue;
      if(f == null) f = m;
      f.gaps |= m.end - f.end > 1;
      f.end = m.end;
      if(pos(f.end, lex) - pos(f.start, lex) > n) return false;
    }
    if(f == null) return false;

    final int w = n - pos(f.end, lex) + pos(f.start, lex);
    for(int s = pos(f.start, lex) - w; s <= pos(f.start, lex); ++s) {
      boolean h = false;
      for(final FTStringMatch m : mtc) {
        h = m.exclude && pos(m.start, lex) >= s && pos(m.end, lex) <= s + w;
        if(h) break;
      }
      if(!h) {
        mtc.reset();
        mtc.add(f);
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean has(final Flag flag) {
    return win.has(flag) || super.has(flag);
  }

  @Override
  public boolean removable(final Var v) {
    return win.removable(v) && super.removable(v);
  }

  @Override
  public VarUsage count(final Var v) {
    return win.count(v).plus(super.count(v));
  }

  @Override
  public FTExpr inline(final QueryContext ctx, final VarScope scp, final Var v, final Expr e)
      throws QueryException {
    final boolean ex = inlineAll(ctx, scp, expr, v, e);
    final Expr w = win.inline(ctx, scp, v, e);
    if(w != null) win = w;
    return ex || w != null ? optimize(ctx, scp) : null;
  }

  @Override
  public FTExpr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTWindow(info, expr[0].copy(ctx, scp, vs), win.copy(ctx, scp, vs), unit);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(QueryText.WINDOW, unit), win, expr);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return super.accept(visitor) && win.accept(visitor);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final FTExpr e : expr) sz += e.exprSize();
    return sz + win.exprSize();
  }

  @Override
  public String toString() {
    return super.toString() + QueryText.WINDOW + ' ' + win + ' ' + unit;
  }
}
