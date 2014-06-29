package org.basex.query.func;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Functions to connect remote database instances.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNClient extends StandardFunc {
  /** Query pattern. */
  private static final Pattern QUERYPAT = Pattern.compile("\\[(.*?)\\] (.*)", Pattern.MULTILINE);

  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNClient(final StaticContext sctx, final InputInfo ii, final Function f, final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    switch(sig) {
      case _CLIENT_QUERY: return query(ctx).iter(ctx);
      default:            return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _CLIENT_QUERY: return query(ctx);
      default:            return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkCreate(ctx);
    switch(sig) {
      case _CLIENT_CONNECT: return connect(ctx);
      case _CLIENT_EXECUTE: return execute(ctx);
      case _CLIENT_INFO:    return info(ctx);
      case _CLIENT_CLOSE:   return close(ctx);
      default:              return super.item(ctx, ii);
    }
  }

  /**
   * Establishes a connection to a remote database instance.
   * @param ctx query context
   * @return connection id
   * @throws QueryException query exception
   */
  private Uri connect(final QueryContext ctx) throws QueryException {
    final String host = Token.string(checkStr(expr[0], ctx));
    final String user = Token.string(checkStr(expr[2], ctx));
    final String pass = Token.string(checkStr(expr[3], ctx));
    final int port = (int) checkItr(expr[1], ctx);
    try {
      return sessions(ctx).add(new ClientSession(host, port, user, pass));
    } catch(final IOException ex) {
      throw BXCL_CONN.get(info, ex);
    }
  }

  /**
   * Executes a command and returns the result as string.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str execute(final QueryContext ctx) throws QueryException {
    final ClientSession cs = session(ctx, false);
    final String cmd = Token.string(checkStr(expr[1], ctx));

    try {
      final ArrayOutput ao = new ArrayOutput();
      cs.setOutputStream(ao);
      cs.execute(cmd);
      cs.setOutputStream(null);
      return Str.get(ao.toArray());
    } catch(final BaseXException ex) {
      throw BXCL_COMMAND.get(info, ex);
    } catch(final IOException ex) {
      throw BXCL_COMM.get(info, ex);
    }
  }

  /**
   * Executes a command and returns the result as string.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str info(final QueryContext ctx) throws QueryException {
    return Str.get(session(ctx, false).info().replaceAll("\r\n?", "\n").trim());
  }

  /**
   * Executes a query and returns the result as sequence.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Value query(final QueryContext ctx) throws QueryException {
    final ClientSession cs = session(ctx, false);
    final String query = Token.string(checkStr(expr[1], ctx));
    final ValueBuilder vb = new ValueBuilder();
    ClientQuery cq = null;
    try {
      cq = cs.query(query);
      // bind variables and context item
      for(final Map.Entry<String, Value> binding : bindings(2, ctx).entrySet()) {
        final String k = binding.getKey();
        final Value v = binding.getValue();
        if(!v.isItem()) throw BXCL_ITEM.get(info, v);
        final Item it = (Item) v;
        final Type t = v.type;
        if(it instanceof FuncItem) throw FIVALUE.get(info, t);

        final Object value = t instanceof NodeType ? v.serialize() : Token.string(it.string(info));
        if(k.isEmpty()) cq.context(value, t.toString());
        else cq.bind(k, value, t.toString());
      }
      // evaluate query
      while(cq.more()) {
        final String result = cq.next();
        vb.add(cq.type().castString(result, ctx, sc, info));
      }
      return vb.value();
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    } catch(final BaseXException ex) {
      final Matcher m = QUERYPAT.matcher(ex.getMessage());
      if(m.find()) {
        final QueryException exc = get(m.group(1), info, m.group(2));
        throw exc == null ? new QueryException(info, new QNm(m.group(1)), m.group(2)) : exc;
      }
      throw BXCL_QUERY.get(info, ex);
    } catch(final IOException ex) {
      throw BXCL_COMM.get(info, ex);
    } finally {
      if(cq != null) try { cq.close(); } catch(final IOException ignored) { }
    }
  }

  /**
   * Establishes a connection to a remote database instance.
   * @param ctx query context
   * @return connection id
   * @throws QueryException query exception
   */
  private Item close(final QueryContext ctx) throws QueryException {
    try {
      session(ctx, true).close();
      return null;
    } catch(final IOException ex) {
      throw BXCL_COMMAND.get(info, ex);
    }
  }

  /**
   * Returns a connection and removes it from list with opened connections if
   * requested.
   * @param ctx query context
   * @param del flag indicating if connection has to be removed
   * @return connection
   * @throws QueryException query exception
   */
  private ClientSession session(final QueryContext ctx, final boolean del) throws QueryException {
    final Uri id = (Uri) checkType(expr[0].item(ctx, info), AtomType.URI);
    final ClientSession cs = sessions(ctx).get(id);
    if(cs == null) throw BXCL_NOTAVL.get(info, id);
    if(del) sessions(ctx).remove(id);
    return cs;
  }

  /**
   * Returns the sessions handler.
   * @param ctx query context
   * @return connection handler
   */
  private static ClientSessions sessions(final QueryContext ctx) {
    ClientSessions res = ctx.resources.get(ClientSessions.class);
    if(res == null) {
      res = new ClientSessions();
      ctx.resources.add(res);
    }
    return res;
  }
}
