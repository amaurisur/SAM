package org.basex.core;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.index.*;
import org.basex.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests update conflicts caused by multiple database contexts.
 * Note that the use of multiple contexts is bad practice: all databases should be
 * opened and managed by a single database context. As the standalone and GUI mode
 * of BaseX are not synchronized, and as users prefer to visualize databases that are
 * opened in other instances, this pinning concept prevents users from performing
 * updates in one instance that will not be reflected in another instance.
 *
 * Currently, all tests are disabled, as the concurrent process instances would have
 * to run in different JVMs.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
@Ignore("OverlappingLocking")
public final class PinTest extends SandboxTest {
  /** Second database context. */
  private static final Context CONTEXT2 = new Context();

  /** Test file name. */
  private static final String FN = "input.xml";
  /** Test folder. */
  private static final String FLDR = "src/test/resources";
  /** Test file. */
  private static final String FILE = FLDR + '/' + FN;
  /** Test name. */
  private static final String NAME2 = NAME + '2';

  /** Creates new database contexts. */
  @BeforeClass
  public static void start() {
    initContext(CONTEXT2);
  }

  /** Closes the database contexts. */
  @AfterClass
  public static void finish() {
    CONTEXT2.close();
  }

  /** Closes the contexts. */
  @Before
  public void before() {
    cleanUp();
  }

  /** Closes the contexts. */
  @After
  public void after() {
    cleanUp();
  }

  /** Test ADD, DELETE, RENAME, REPLACE and STORE. */
  @Test
  public void update() {
    // create database and perform update
    ok(new CreateDB(NAME), context);
    // open database by second process
    ok(new Check(NAME), CONTEXT2);
    // fail, close database and succeed
    noCloseOk(new Add(FN, FILE));
    // fail, close database and succeed
    ok(new Check(NAME), CONTEXT2);
    noCloseOk(new Replace(FN, "<x/>"));
    // fail, close database and succeed
    ok(new Check(NAME), CONTEXT2);
    noCloseOk(new Rename(FN, FN));
    // fail, close database and succeed
    ok(new Check(NAME), CONTEXT2);
    noCloseOk(new Store(NAME2, "<x/>"));
    // fail, close database and succeed
    ok(new Check(NAME), CONTEXT2);
    noCloseOk(new Delete(FN));
  }

  /** Test COPY. */
  @Test
  public void copy() {
    // create databases and open by second context
    ok(new CreateDB(NAME), context);
    ok(new Check(NAME), CONTEXT2);
    // copy database (may be opened by multiple databases)
    ok(new Copy(NAME, NAME2), context);
    // open second database and run update operation
    ok(new Open(NAME2), context);
    ok(new Add(FN, FILE), context);
    // drop first database and copy back
    ok(new Close(), CONTEXT2);
    ok(new DropDB(NAME), context);
    ok(new Copy(NAME2, NAME), context);
  }

  /** Test CREATE BACKUP and RESTORE. */
  @Test
  public void backupRestore() {
    // create databases and open by second context
    ok(new CreateDB(NAME), context);
    ok(new Check(NAME), CONTEXT2);
    // copy database (may be opened by multiple databases)
    ok(new CreateBackup(NAME), context);
    // fail, close database and succeed
    noCloseOk(new Restore(NAME));
    // run update operation to ensure that no pin files were zipped
    ok(new Add(FN, FILE), context);
  }

  /** Test CREATE DB, DROP DB and ALTER DB. */
  @Test
  public void createDropAlterDB() {
    // create database
    ok(new CreateDB(NAME), context);
    // create database with same name
    ok(new CreateDB(NAME), context);
    // block second process
    no(new CreateDB(NAME), CONTEXT2);
    no(new CreateDB(NAME), CONTEXT2);
    // create database with different name
    ok(new CreateDB(NAME2), context);
    // allow second process
    ok(new CreateDB(NAME), CONTEXT2);
    ok(new CreateDB(NAME), CONTEXT2);
    // fail, close database and succeed
    noCloseOk(new CreateDB(NAME));
    // allow main-memory instances with same name
    ok(new Set(MainOptions.MAINMEM, true), CONTEXT2);
    ok(new CreateDB(NAME), CONTEXT2);
    ok(new Set(MainOptions.MAINMEM, false), CONTEXT2);
    // fail, close database and succeed
    ok(new Check(NAME), CONTEXT2);
    noCloseOk(new DropDB(NAME));
    // fail, close database and succeed
    ok(new CreateDB(NAME), CONTEXT2);
    ok(new DropDB(NAME), CONTEXT2);
    // create databases and open by second context
    ok(new CreateDB(NAME), context);
    ok(new Check(NAME), CONTEXT2);
    // fail, close database and succeed
    ok(new DropDB(NAME2), context);
    noCloseOk(new AlterDB(NAME, NAME2));
  }

  /** Test CREATE USER, DROP USER and ALTER USER. */
  @Test
  public void createDropAlterUser() {
    // create and alter users (open issue: allow this if other instances are opened?)
    ok(new CreateUser(NAME, Token.md5(Text.S_ADMIN)), context);
    ok(new AlterUser(NAME, Token.md5("abc")), context);
    // create databases and open by second context
    ok(new CreateDB(NAME), context);
    ok(new Check(NAME), CONTEXT2);
    // create databases and open by second context
    ok(new AlterUser(NAME, Token.md5("abc")), context);
  }

  /** Test CREATE INDEX and DROP INDEX. */
  @Test
  public void createDropIndex() {
    // create databases and open by second context
    ok(new CreateDB(NAME), context);
    ok(new Check(NAME), CONTEXT2);
    // fail, close database and succeed
    noCloseOk(new CreateIndex(IndexType.TEXT));
    ok(new Check(NAME), CONTEXT2);
    // fail, close database and succeed
    noCloseOk(new DropIndex(IndexType.TEXT));
  }

  /** Test XQUERY. */
  @Test
  public void xquery() {
    // create databases and open by second context
    ok(new CreateDB(NAME, FILE), context);
    ok(new Check(NAME), CONTEXT2);
    // perform read-only queries
    ok(new XQuery("."), context);
    ok(new XQuery("."), CONTEXT2);
    // perform updating query: fail, close database and succeed
    noCloseOk(new XQuery("delete node /*"));
  }

  /**
   * Runs a command twice: the first time, it is supposed to fail, because the database
   * is opened by two contexts; after closing the database, it is supposed to succeed.
   * @param cmd command to test
   */
  private static void noCloseOk(final Command cmd) {
    // command is supposed to fail, because database is opened by two contexts
    no(cmd, context);
    // close database in second context
    ok(new Close(), CONTEXT2);
    // command should now succeed
    ok(cmd, context);
  }

  /**
   * Assumes that this command is successful.
   *
   * @param cmd command reference
   * @param ctx database context
   */
  private static void ok(final Command cmd, final Context ctx) {
    try {
      cmd.execute(ctx);
    } catch(final IOException ex) {
      fail(Util.message(ex));
    }
  }

  /**
   * Assumes that this command fails.
   * @param cmd command reference
   * @param ctx database context
   */
  private static void no(final Command cmd, final Context ctx) {
    try {
      cmd.execute(ctx);
      fail("\"" + cmd + "\" was supposed to fail.");
    } catch(final IOException ignored) {
    }
  }

  /**
   * Deletes the potentially already existing DBs.
   * DBs & User {@link #NAME} and {@link #NAME2}
   */
  private static void cleanUp() {
    ok(new Close(), context);
    ok(new Close(), CONTEXT2);
    ok(new DropDB(NAME), context);
    ok(new DropDB(NAME2), context);
    ok(new DropUser(NAME), context);
    ok(new DropUser(NAME2), context);
  }
}
