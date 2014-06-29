package org.basex.query.func;

import static java.sql.DriverManager.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.Map.Entry;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Functions on relational databases.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class FNSql extends StandardFunc {
  /** Module prefix. */
  private static final String PREFIX = "sql";
  /** QName. */
  private static final QNm Q_ROW = QNm.get(PREFIX, "row", SQLURI);
  /** QName. */
  private static final QNm Q_COLUMN = QNm.get(PREFIX, "column", SQLURI);
  /** QName. */
  private static final QNm Q_OPTIONS = QNm.get(PREFIX, "options", SQLURI);
  /** QName. */
  private static final QNm Q_PARAMETERS = QNm.get(PREFIX, "parameters", SQLURI);
  /** QName. */
  private static final QNm Q_PARAMETER = QNm.get(PREFIX, "parameter", SQLURI);

  /** Type int. */
  private static final byte[] INT = AtomType.INT.string();
  /** Type string. */
  private static final byte[] STRING = AtomType.STR.string();
  /** Type boolean. */
  private static final byte[] BOOL = AtomType.BLN.string();
  /** Type date. */
  private static final byte[] DATE = AtomType.DAT.string();
  /** Type double. */
  private static final byte[] DOUBLE = AtomType.DBL.string();
  /** Type float. */
  private static final byte[] FLOAT = AtomType.FLT.string();
  /** Type short. */
  private static final byte[] SHORT = AtomType.SHR.string();
  /** Type time. */
  private static final byte[] TIME = AtomType.TIM.string();
  /** Type timestamp. */
  private static final byte[] TIMESTAMP = token("timestamp");

  /** Name. */
  private static final String NAME = "name";
  /** Auto-commit mode. */
  private static final String AUTO_COMM = "autocommit";
  /** User. */
  private static final String USER = "user";
  /** Password. */
  private static final String PASS = "password";

  /** Attribute "type" of <sql:parameter/>. */
  private static final byte[] TYPE = token("type");

  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNSql(final StaticContext sctx, final InputInfo ii, final Function f, final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    switch(sig) {
      case _SQL_EXECUTE:          return execute(ctx);
      case _SQL_EXECUTE_PREPARED: return executePrepared(ctx);
      default:                    return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkCreate(ctx);
    switch(sig) {
      case _SQL_INIT:     return init(ctx);
      case _SQL_CONNECT:  return connect(ctx);
      case _SQL_PREPARE:  return prepare(ctx);
      case _SQL_CLOSE:    return close(ctx);
      case _SQL_COMMIT:   return commit(ctx);
      case _SQL_ROLLBACK: return rollback(ctx);
      default:            return super.item(ctx, ii);
    }
  }

  /**
   * Initializes JDBC with the specified driver.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item init(final QueryContext ctx) throws QueryException {
    final String driver = string(checkStr(expr[0], ctx));
    if(Reflect.find(driver) == null) throw BXSQ_DRIVER.get(info, driver);
    return null;
  }

  /**
   * Establishes a connection to a relational database.
   * @param ctx query context
   * @return connection id
   * @throws QueryException query exception
   */
  private Int connect(final QueryContext ctx) throws QueryException {
    // URL to relational database
    final String url = string(checkStr(expr[0], ctx));
    final JDBCConnections jdbc = jdbc(ctx);
    try {
      if(expr.length > 2) {
        // credentials
        final String user = string(checkStr(expr[1], ctx));
        final String pass = string(checkStr(expr[2], ctx));
        if(expr.length == 4) {
          // connection options
          final Options opts = checkOptions(3, Q_OPTIONS, new Options(), ctx);
          // extract auto-commit mode from options
          boolean ac = true;
          final HashMap<String, String> options = opts.free();
          final String commit = options.get(AUTO_COMM);
          if(commit != null) {
            ac = Util.yes(commit);
            options.remove(AUTO_COMM);
          }
          // connection properties
          final Properties props = connProps(options);
          props.setProperty(USER, user);
          props.setProperty(PASS, pass);

          // open connection
          final Connection conn = getConnection(url, props);
          // set auto/commit mode
          conn.setAutoCommit(ac);
          return Int.get(jdbc.add(conn));
        }
        return Int.get(jdbc.add(getConnection(url, user, pass)));
      }
      return Int.get(jdbc.add(getConnection(url)));
    } catch(final SQLException ex) {
      throw BXSQ_ERROR.get(info, ex);
    }
  }

  /**
   * Parses connection options.
   * @param options options
   * @return connection properties
   */
  private static Properties connProps(final HashMap<String, String> options) {
    final Properties props = new Properties();
    for(final Entry<String, String> entry : options.entrySet()) {
      props.setProperty(entry.getKey(), entry.getValue());
    }
    return props;
  }

  /**
   * Prepares a statement and returns its id.
   * @param ctx query context
   * @return prepared statement id
   * @throws QueryException query exception
   */
  private Int prepare(final QueryContext ctx) throws QueryException {
    final Connection conn = connection(ctx, false);
    // Prepared statement
    final byte[] prepStmt = checkStr(expr[1], ctx);
    try {
      // Keep prepared statement
      final PreparedStatement prep = conn.prepareStatement(string(prepStmt));
      return Int.get(jdbc(ctx).add(prep));
    } catch(final SQLException ex) {
      throw BXSQ_ERROR.get(info, ex);
    }
  }

  /**
   * Executes a query, update or prepared statement.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private NodeSeqBuilder execute(final QueryContext ctx) throws QueryException {
    final int id = (int) checkItr(expr[0], ctx);
    final Object obj = jdbc(ctx).get(id);
    if(!(obj instanceof Connection)) throw BXSQ_CONN.get(info, id);

    final String query = string(checkStr(expr[1], ctx));
    try(final Statement stmt = ((Connection) obj).createStatement()) {
      final boolean result = stmt.execute(query);
      return result ? buildResult(stmt.getResultSet(), ctx) : new NodeSeqBuilder();
    } catch(final SQLException ex) {
      throw BXSQ_ERROR.get(info, ex);
    }
  }

  /**
   * Executes a query, update or prepared statement.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private NodeSeqBuilder executePrepared(final QueryContext ctx) throws QueryException {
    final int id = (int) checkItr(expr[0], ctx);
    final Object obj = jdbc(ctx).get(id);
    if(!(obj instanceof PreparedStatement)) throw BXSQ_STATE.get(info, id);

    // Get parameters for prepared statement
    long c = 0;
    ANode params = null;
    if(expr.length > 1) {
      params = (ANode) checkType(checkItem(expr[1], ctx), NodeType.ELM);
      if(!params.qname().eq(Q_PARAMETERS)) throw INVALIDOPTX.get(info, params.qname().local());
      c = countParams(params);
    }

    try {
      final PreparedStatement stmt = (PreparedStatement) obj;
      // Check if number of parameters equals number of place holders
      if(c != stmt.getParameterMetaData().getParameterCount()) throw BXSQ_PARAMS.get(info);
      if(params != null) setParameters(params.children(), stmt);
      return stmt.execute() ? buildResult(stmt.getResultSet(), ctx) : new NodeSeqBuilder();
    } catch(final SQLException ex) {
      throw BXSQ_ERROR.get(info, ex);
    }
  }

  /**
   * Counts the numbers of <sql:parameter/> elements.
   * @param params element <sql:parameter/>
   * @return number of parameters
   */
  private static long countParams(final ANode params) {
    final AxisIter ch = params.children();
    long c = ch.size();
    if(c == -1) do ++c;
    while(ch.next() != null);
    return c;
  }

  /**
   * Sets the parameters of a prepared statement.
   * @param params parameters
   * @param stmt prepared statement
   * @throws QueryException query exception
   */
  private void setParameters(final AxisMoreIter params, final PreparedStatement stmt)
      throws QueryException {

    int i = 0;
    for(ANode next; (next = params.next()) != null;) {
      // Check name
      if(!next.qname().eq(Q_PARAMETER)) throw INVALIDOPTX.get(info, next.qname().local());
      final AxisIter attrs = next.attributes();
      byte[] paramType = null;
      boolean isNull = false;
      for(ANode attr; (attr = attrs.next()) != null;) {
        // Attribute "type"
        if(eq(attr.name(), TYPE)) paramType = attr.string();
        // Attribute "null"
        else if(eq(attr.name(), NULL))
          isNull = attr.string() != null && Bln.parse(attr.string(), info);
        // Not expected attribute
        else throw BXSQ_ATTR.get(info, string(attr.name()));
      }
      if(paramType == null) throw BXSQ_TYPE.get(info);
      final byte[] v = next.string();
      setParam(++i, stmt, paramType, isNull ? null : string(v), isNull);
    }
  }

  /**
   * Sets the parameter with the given index in a prepared statement.
   * @param index parameter index
   * @param stmt prepared statement
   * @param paramType parameter type
   * @param value parameter value
   * @param isNull indicator if the parameter is null or not
   * @throws QueryException query exception
   */
  private void setParam(final int index, final PreparedStatement stmt,
      final byte[] paramType, final String value, final boolean isNull) throws QueryException {
    try {
      if(eq(BOOL, paramType)) {
        if(isNull) stmt.setNull(index, Types.BOOLEAN);
        else stmt.setBoolean(index, Boolean.parseBoolean(value));
      } else if(eq(DATE, paramType)) {
        if(isNull) stmt.setNull(index, Types.DATE);
        else stmt.setDate(index, Date.valueOf(value));
      } else if(eq(DOUBLE, paramType)) {
        if(isNull) stmt.setNull(index, Types.DOUBLE);
        else stmt.setDouble(index, Double.parseDouble(value));
      } else if(eq(FLOAT, paramType)) {
        if(isNull) stmt.setNull(index, Types.FLOAT);
        else stmt.setFloat(index, Float.parseFloat(value));
      } else if(eq(INT, paramType)) {
        if(isNull) stmt.setNull(index, Types.INTEGER);
        else stmt.setInt(index, Integer.parseInt(value));
      } else if(eq(SHORT, paramType)) {
        if(isNull) stmt.setNull(index, Types.SMALLINT);
        else stmt.setShort(index, Short.parseShort(value));
      } else if(eq(STRING, paramType)) {
        if(isNull) stmt.setNull(index, Types.VARCHAR);
        else stmt.setString(index, value);
      } else if(eq(TIME, paramType)) {
        if(isNull) stmt.setNull(index, Types.TIME);
        else stmt.setTime(index, Time.valueOf(value));
      } else if(eq(TIMESTAMP, paramType)) {
        if(isNull) stmt.setNull(index, Types.TIMESTAMP);
        else stmt.setTimestamp(index, Timestamp.valueOf(value));
      } else {
        throw BXSQ_ERROR.get(info, "unsupported type: " + string(paramType));
      }
    } catch(final SQLException ex) {
      throw BXSQ_ERROR.get(info, ex);
    } catch(final IllegalArgumentException ex) {
      throw BXSQ_FORMAT.get(info, string(paramType));
    }
  }

  /**
   * Builds a sequence of elements from a query's result set.
   * @param rs result set
   * @param ctx query context
   * @return sequence of elements <tuple/> each of which represents a row from
   *         the result set
   * @throws QueryException query exception
   */
  private NodeSeqBuilder buildResult(final ResultSet rs, final QueryContext ctx)
      throws QueryException {

    try {
      final ResultSetMetaData metadata = rs.getMetaData();
      final int cc = metadata.getColumnCount();
      final NodeSeqBuilder rows = new NodeSeqBuilder();
      while(rs.next()) {
        final FElem row = new FElem(Q_ROW);
        rows.add(row);
        for(int k = 1; k <= cc; k++) {
          // for each row add column values as children
          final String name = metadata.getColumnLabel(k);
          final Object value = rs.getObject(k);
          // null values are ignored
          if(value == null) continue;

          // element <sql:column name='...'>...</sql:column>
          final FElem col = new FElem(Q_COLUMN).add(NAME, name);
          row.add(col);

          if(value instanceof SQLXML) {
            // add XML value as child element
            final String xml = ((SQLXML) value).getString();
            try {
              col.add(new DBNode(new IOContent(xml), ctx.context.options).children().next());
            } catch(final IOException ex) {
              // fallback: add string representation
              Util.debug(ex);
              col.add(xml);
            }
          } else {
            // add string representation of other values
            col.add(value.toString());
          }
        }
      }
      return rows;
    } catch(final SQLException ex) {
      throw BXSQ_ERROR.get(info, ex);
    }
  }

  /**
   * Closes a connection to a relational database.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item close(final QueryContext ctx) throws QueryException {
    try {
      connection(ctx, true).close();
      return null;
    } catch(final SQLException ex) {
      throw BXSQ_ERROR.get(info, ex);
    }
  }

  /**
   * Commits all changes made during last transaction.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item commit(final QueryContext ctx) throws QueryException {
    try {
      connection(ctx, false).commit();
      return null;
    } catch(final SQLException ex) {
      throw BXSQ_ERROR.get(info, ex);
    }
  }

  /**
   * Rollbacks all changes made during last transaction.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item rollback(final QueryContext ctx) throws QueryException {
    try {
      connection(ctx, false).rollback();
      return null;
    } catch(final SQLException ex) {
      throw BXSQ_ERROR.get(info, ex);
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
  private Connection connection(final QueryContext ctx, final boolean del)
      throws QueryException {
    final int id = (int) checkItr(expr[0], ctx);
    final Object obj = jdbc(ctx).get(id);
    if(!(obj instanceof Connection)) throw BXSQ_CONN.get(info, id);
    if(del) jdbc(ctx).remove(id);
    return (Connection) obj;
  }

  /**
   * Returns the JDBC connection handler.
   * @param ctx query context
   * @return connection handler
   */
  private static JDBCConnections jdbc(final QueryContext ctx) {
    JDBCConnections res = ctx.resources.get(JDBCConnections.class);
    if(res == null) {
      res = new JDBCConnections();
      ctx.resources.add(res);
    }
    return res;
  }
}
