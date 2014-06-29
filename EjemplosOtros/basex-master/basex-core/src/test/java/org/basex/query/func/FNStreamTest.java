package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Streaming Module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNStreamTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/corrupt.xml";

  /** Test method. */
  @Test
  public void materialize() {
    query(_FILE_READ_TEXT.args(FILE), "&lt;");
    query(_STREAM_MATERIALIZE.args(_FILE_READ_BINARY.args(FILE)), "PA==");
    query(_STREAM_MATERIALIZE.args(_FILE_READ_TEXT.args(FILE)), "&lt;");
  }

  /** Test method. */
  @Test
  public void isStreamable() {
    query(_STREAM_IS_STREAMABLE.args(_FILE_READ_BINARY.args(FILE)), "true");

    query(_STREAM_IS_STREAMABLE.args("A"), "false");
    query(_STREAM_IS_STREAMABLE.args(_STREAM_MATERIALIZE.args(
        _FILE_READ_TEXT.args(FILE))), "false");
  }
}
