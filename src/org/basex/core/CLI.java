package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.cmd.*;
import org.basex.core.parse.*;
import org.basex.query.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * This is the abstract main class for the starter classes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class CLI extends Main {
  /** Database context. */
  public final Context context;

  /** Output file for queries. */
  protected OutputStream out = System.out;
  /** Console mode. May be set to {@code false} during execution. */
  protected boolean console;
  /** Session. */
  protected Session session;
  /** Verbose mode. */
  protected boolean verbose;
  /** Separate serialized items with newlines. */
  protected boolean newline;
  /** Password reader. */
  private final PasswordReader pwReader = new PasswordReader() {
    @Override
    public String password() {
      Util.out(PASSWORD + COLS);
      return md5(Util.password());
    }
  };

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  protected CLI(final String[] args) throws IOException {
    this(args, null);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @param ctx database context, or {@code null}
   * @throws IOException I/O exception
   */
  protected CLI(final String[] args, final Context ctx) throws IOException {
    super(args);
    context = ctx != null ? ctx : new Context();
    parseArgs();

    // console: turn on verbose mode
    verbose |= console;

    // guarantee correct shutdown of database context
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public synchronized void run() {
        context.close();
      }
    });
  }

  /**
   * Launches the console mode, which reads and executes user input.
   */
  protected final void console() {
    // create console reader
    final ConsoleReader cr = ConsoleReader.get();
    // loop until console is set to false (may happen in server mode)
    while(console) {
      // get next line
      final String in = cr.readLine();
      // end of input: break loop
      if(in == null) break;
      // skip empty lines
      if(in.isEmpty()) continue;
      try {
        if(!execute(new CommandParser(in, context).pwReader(cr.pwReader()))) {
          // show goodbye message if method returns false
          Util.outln(BYE[new Random().nextInt(4)]);
          return;
        }
      } catch(final IOException ex) {
        // output error messages
        Util.errln(ex);
      }
    }
  }

  /**
   * Quits the console mode.
   * @throws IOException I/O exception
   */
  protected void quit() throws IOException {
    if(out == System.out || out == System.err) out.flush();
    else out.close();
  }

  /**
   * Parses and executes the input string.
   * @param in input commands
   * @throws IOException database exception
   */
  protected final void execute(final String in) throws IOException {
    execute(new CommandParser(in, context).pwReader(pwReader));
  }

  /**
   * Execute the commands from the given command parser.
   * @param parser command parser
   * @return {@code false} if the exit command was sent
   * @throws IOException database exception
   */
  private boolean execute(final CommandParser parser) throws IOException {
    try {
      for(final Command cmd : parser.parse()) {
        if(cmd instanceof Exit) return false;
        execute(cmd, verbose);
      }
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
    return true;
  }

  /**
   * Executes the specified command and optionally prints some information.
   * @param cmd command to be run
   * @param info verbose flag
   * @throws IOException I/O exception
   */
  protected final void execute(final Command cmd, final boolean info) throws IOException {
    final Session ss = session();
    ss.execute(cmd);
    if(newline && cmd instanceof XQuery) out.write(token(NL));
    if(info) Util.out(ss.info());
  }

  /**
   * Returns the session.
   * @return session
   * @throws IOException I/O exception
   */
  protected abstract Session session() throws IOException;
}
