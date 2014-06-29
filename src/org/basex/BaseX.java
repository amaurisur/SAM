package org.basex;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is the starter class for the stand-alone console mode.
 * It executes all commands locally.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class BaseX extends CLI {
  /** Commands to be executed. */
  private IntList ops;
  /** Command arguments. */
  private StringList vals;
  /** Flag for writing options to disk. */
  private boolean writeOptions;

  /**
   * Main method, launching the standalone mode.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
    try {
      new BaseX(args);
    } catch(final IOException ex) {
      Util.errln(ex);
      System.exit(1);
    }
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public BaseX(final String... args) throws IOException {
    super(args);

    // create session to show optional login request
    session();

    console = true;
    try {
      // loop through all commands
      final StringBuilder bind = new StringBuilder();
      SerializerOptions sopts = null;
      boolean v = false, qi = false, qp = false;
      for(int i = 0; i < ops.size(); i++) {
        final int c = ops.get(i);
        String val = vals.get(i);

        if(c == 'b') {
          // set/add variable binding
          if(bind.length() != 0) bind.append(',');
          // commas are escaped by a second comma
          val = bind.append(val.replaceAll(",", ",,")).toString();
          execute(new Set(MainOptions.BINDINGS, val), false);
        } else if(c == 'c') {
          // evaluate commands
          final IO io = IO.get(val);
          String base = ".";
          if(io.exists() && !io.isDir()) {
            val = io.string();
            base = io.path();
          }
          execute(new Set(MainOptions.QUERYPATH, base), false);
          execute(val);
          execute(new Set(MainOptions.QUERYPATH, ""), false);
          console = false;
        } else if(c == 'd') {
          // toggle debug mode
          Prop.debug ^= true;
        } else if(c == 'D') {
          // hidden option: show/hide dot query graph
          execute(new Set(MainOptions.DOTPLAN, null), false);
        } else if(c == 'i') {
          // open database or create main memory representation
          execute(new Set(MainOptions.MAINMEM, true), false);
          execute(new Check(val), verbose);
          execute(new Set(MainOptions.MAINMEM, false), false);
        } else if(c == 'L') {
          // toggle newline separators
          newline ^= true;
          if(sopts == null) sopts = new SerializerOptions();
          sopts.set(SerializerOptions.ITEM_SEPARATOR, "\\n");
          execute(new Set(MainOptions.SERIALIZER, sopts), false);
        } else if(c == 'o') {
          // change output stream
          if(out != System.out) out.close();
          out = new PrintOutput(val);
          session().setOutputStream(out);
        } else if(c == 'q') {
          // evaluate query
          execute(new XQuery(val), verbose);
          console = false;
        } else if(c == 'Q') {
          // evaluate file contents or string as query
          final IO io = IO.get(val);
          String base = ".";
          if(io.exists() && !io.isDir()) {
            val = io.string();
            base = io.path();
          }
          execute(new Set(MainOptions.QUERYPATH, base), false);
          execute(new XQuery(val), verbose);
          execute(new Set(MainOptions.QUERYPATH, ""), false);
          console = false;
        } else if(c == 'r') {
          // parse number of runs
          execute(new Set(MainOptions.RUNS, Token.toInt(val)), false);
        } else if(c == 'R') {
          // toggle query evaluation
          execute(new Set(MainOptions.RUNQUERY, null), false);
        } else if(c == 's') {
          // set/add serialization parameter
          if(sopts == null) sopts = new SerializerOptions();
          final String[] kv = val.split("=", 2);
          sopts.assign(kv[0], kv.length > 1  ? kv[1] : "");
          execute(new Set(MainOptions.SERIALIZER, sopts), false);
        } else if(c == 'u') {
          // (de)activate write-back for updates
          execute(new Set(MainOptions.WRITEBACK, null), false);
        } else if(c == 'v') {
          // show/hide verbose mode
          v ^= true;
        } else if(c == 'V') {
          // show/hide query info
          qi ^= true;
          execute(new Set(MainOptions.QUERYINFO, null), false);
        } else if(c == 'w') {
          // toggle chopping of whitespaces
          execute(new Set(MainOptions.CHOP, null), false);
        } else if(c == 'W') {
          // hidden option: toggle writing of options before exit
          writeOptions ^= true;
        } else if(c == 'x') {
          // show/hide xml query plan
          execute(new Set(MainOptions.XMLPLAN, null), false);
          qp ^= true;
        } else if(c == 'X') {
          // show query plan before/after query compilation
          execute(new Set(MainOptions.COMPPLAN, null), false);
        } else if(c == 'z') {
          // toggle result serialization
          execute(new Set(MainOptions.SERIALIZE, null), false);
        }
        verbose = qi || qp || v;
      }

      if(console) {
        verbose = true;
        // enter interactive mode
        Util.outln(S_CONSOLE + TRY_MORE_X, sa() ? S_STANDALONE : S_CLIENT);
        console();
      }

      if(writeOptions) context.globalopts.write();
    } finally {
      quit();
    }
  }

  /**
   * Tests if this client is stand-alone.
   * @return stand-alone flag
   */
  protected boolean sa() {
    return true;
  }

  @Override
  protected Session session() throws IOException {
    if(session == null) session = new LocalSession(context, out);
    session.setOutputStream(out);
    return session;
  }

  @Override
  protected final void parseArgs() throws IOException {
    ops = new IntList();
    vals = new StringList();

    final MainParser arg = new MainParser(this);
    while(arg.more()) {
      final char c;
      String v = null;
      if(arg.dash()) {
        c = arg.next();
        if(c == 'b' || c == 'c' || c == 'C' || c == 'i' || c == 'o' || c == 'q' ||
           c == 'r' || c == 's') {
          // options followed by a string
          v = arg.string();
        } else if(c == 'd' || c == 'D' && sa() || c == 'L' || c == 'u' || c == 'R' || c == 'v' ||
           c == 'V' || c == 'w' || c == 'W' || c == 'x' || c == 'X' || c == 'z') {
          // options to be toggled
          v = "";
        } else if(!sa()) {
          // client options: need to be set before other options
          if(c == 'n') {
            // set server name
            context.globalopts.set(GlobalOptions.HOST, arg.string());
          } else if(c == 'p') {
            // set server port
            context.globalopts.set(GlobalOptions.PORT, arg.number());
          } else if(c == 'P') {
            // specify password
            context.globalopts.set(GlobalOptions.PASSWORD, arg.string());
          } else if(c == 'U') {
            // specify user name
            context.globalopts.set(GlobalOptions.USER, arg.string());
          } else {
            throw arg.usage();
          }
        } else {
          throw arg.usage();
        }
      } else {
        v = arg.string().trim();
        // interpret as command file if input string ends with command script suffix
        c = v.endsWith(IO.BXSSUFFIX) ? 'c' : 'Q';
      }
      if(v != null) {
        ops.add(c);
        vals.add(v);
      }
    }
  }

  @Override
  public String header() {
    return Util.info(S_CONSOLE, sa() ? S_STANDALONE : S_CLIENT);
  }

  @Override
  public String usage() {
    return sa() ? S_LOCALINFO : S_CLIENTINFO;
  }
}
