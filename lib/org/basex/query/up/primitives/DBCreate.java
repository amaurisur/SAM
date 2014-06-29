package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.List;

import org.basex.build.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Update primitive for the {@link Function#_DB_CREATE} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class DBCreate extends NameUpdate {
  /** Container for new database documents. */
  private final DBNew add;
  /** Database update options. */
  private final DBOptions updates;

  /**
   * Constructor.
   * @param info input info
   * @param name name for created database
   * @param input input (ANode and QueryInput references)
   * @param opts database options
   * @param qc query context
   * @throws QueryException query exception
   */
  public DBCreate(final InputInfo info, final String name, final List<NewInput> input,
      final Options opts, final QueryContext qc) throws QueryException {

    super(UpdateType.DBCREATE, name, info, qc);
    updates = new DBOptions(qc, opts.free(), info);
    add = new DBNew(qc, input, info);
    updates.check(true);
  }

  @Override
  public void prepare() throws QueryException {
    if(add.inputs != null && !add.inputs.isEmpty())
      add.addDocs(new MemData(updates.qc.context.options), name);
  }

  @Override
  public void apply() throws QueryException {
    close();

    updates.initOptions();
    updates.assignOptions();
    try {
      final Data data = CreateDB.create(name, Parser.emptyParser(updates.qc.context.options),
          updates.qc.context);

      // add initial documents and optimize database
      if(add.md != null) {
        if(!data.startUpdate()) throw BXDB_OPENED.get(null, data.meta.name);
        try {
          data.insert(data.meta.size, -1, new DataClip(add.md));
          Optimize.optimize(data, null);
        } finally {
          data.finishUpdate();
        }
      }
      Close.close(data, qc.context);

    } catch(final IOException ex) {
      throw UPDBOPTERR.get(info, ex);
    } finally {
      updates.resetOptions();
    }
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + add.inputs + ']';
  }

  @Override
  public String operation() { return "created"; }
}
