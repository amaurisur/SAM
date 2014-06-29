package org.basex.build;

import java.io.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class defines a parser, which is used to create new databases instances.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Parser extends Proc {
  /** Source document, or {@code null}. */
  public IO src;
  /** Attributes of currently parsed element. */
  protected final Atts atts = new Atts();
  /** Namespaces of currently parsed element. */
  protected final Atts nsp = new Atts();
  /** Database options. */
  protected final MainOptions options;
  /** Target path (empty, or suffixed with a single slash). */
  String target = "";

  /**
   * Constructor.
   * @param source document source, or {@code null}
   * @param opts database options
   */
  protected Parser(final String source, final MainOptions opts) {
    this(source == null ? null : IO.get(source), opts);
  }

  /**
   * Constructor.
   * @param source document source, or {@code null}
   * @param opts database options
   */
  protected Parser(final IO source, final MainOptions opts) {
    src = source;
    options = opts;
  }

  /**
   * Parses all nodes and sends events to the specified builder.
   * @param build database builder
   * @throws IOException I/O exception
   */
  public abstract void parse(final Builder build) throws IOException;

  /**
   * Closes the parser.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void close() throws IOException { }

  /**
   * Returns parser information.
   * @return info string
   */
  public String info() {
    return "";
  }

  /**
   * Sets the target path.
   * @param path target path
   * @return self reference
   */
  public Parser target(final String path) {
    target = path.isEmpty() ? "" : (path + '/').replaceAll("//+", "/");
    return this;
  }

  // STATIC METHODS ===========================================================

  /**
   * Returns a parser instance for creating empty databases.
   * @param options database options
   * @return parser
   */
  public static Parser emptyParser(final MainOptions options) {
    return new Parser((IO) null, options) {
      @Override
      public void parse(final Builder build) { /* empty */ }
    };
  }

  /**
   * Returns an XML parser instance.
   * @param in input source
   * @param options database options
   * @return xml parser
   * @throws IOException I/O exception
   */
  public static SingleParser xmlParser(final IO in, final MainOptions options)
      throws IOException {
    // use internal or default XML parser
    return options.get(MainOptions.INTPARSE) ? new XMLParser(in, options) :
      new SAXWrapper(in, options);
  }

  /**
   * Returns a parser instance, based on the current options.
   * @param in input source
   * @param options database options
   * @param target relative path reference
   * @return parser
   * @throws IOException I/O exception
   */
  public static SingleParser singleParser(final IO in, final MainOptions options,
      final String target) throws IOException {

    // use file specific parser
    final SingleParser p;
    final MainParser mp = options.get(MainOptions.PARSER);
    switch(mp) {
      case HTML: p = new HtmlParser(in, options); break;
      case TEXT: p = new TextParser(in, options); break;
      case JSON: p = new JsonParser(in, options); break;
      case CSV:  p = new CsvParser(in, options); break;
      default:   p = xmlParser(in, options); break;
    }
    p.target(target);
    return p;
  }
}
