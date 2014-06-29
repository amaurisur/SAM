package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * This class serializes data as XML.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class XMLSerializer extends OutputSerializer {
  /** Root elements. */
  private boolean root;

  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  XMLSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts, V10, V11);
  }

  @Override
  protected void startOpen(final byte[] t) throws IOException {
    if(tags.isEmpty()) {
      if(root) check();
      root = true;
    }
    super.startOpen(t);
  }

  @Override
  protected void finishText(final byte[] v) throws IOException {
    if(tags.isEmpty()) check();
    super.finishText(v);
  }

  @Override
  protected void atomic(final Item i, final boolean iter) throws IOException {
    if(tags.isEmpty()) check();
    super.atomic(i, iter);
  }

  /**
   * Checks if document serialization is valid.
   * @throws QueryIOException query I/O exception
   */
  private void check() throws QueryIOException {
    if(!saomit) throw SERSA.getIO();
    if(docsys != null) throw SERDT.getIO();
  }
}
