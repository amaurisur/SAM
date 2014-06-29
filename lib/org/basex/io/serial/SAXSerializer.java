package org.basex.io.serial;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.query.value.item.*;
import org.basex.util.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

/**
 * Bridge to translate XQuery items to SAX events.
 * The {@link #parse} methods do the following:
 * <ol>
 *   <li>notify startDocument()</li>
 *   <li>serialize the item</li>
 *   <li>notify endDocument()</li>
 * </ol>
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Michael Hedenus
 */
public final class SAXSerializer extends Serializer implements XMLReader {
  /** Item to be serialized. */
  private final Item item;

  /** Content handler. */
  private ContentHandler contentHandler;
  /** Entity resolver. */
  private EntityResolver entityResolver;
  /** DTD handler. */
  private DTDHandler dtdHandler;
  /** Error handler. */
  private ErrorHandler errorHandler;
  /** Lexical handler. */
  private LexicalHandler lexicalHandler;

  /**
   * Constructor.
   * @param it item to be serialized
   */
  public SAXSerializer(final Item it) {
    item = it;
  }

  // XMLReader ==========================================================================

  @Override
  public ContentHandler getContentHandler() {
    return contentHandler;
  }

  @Override
  public boolean getFeature(final String name) {
    return false;
  }

  @Override
  public Object getProperty(final String name) {
    return null;
  }

  @Override
  public void parse(final InputSource input) throws SAXException {
    parse("");
  }

  @Override
  public void parse(final String id) throws SAXException {
    try {
      contentHandler.startDocument();
      serialize(item);
      contentHandler.endDocument();
    } catch(final Exception ex) {
      throw new SAXException(ex);
    }
  }

  @Override
  public void setContentHandler(final ContentHandler ch) {
    contentHandler = ch;
  }

  /**
   * Sets a lexical handler.
   * @param lh handler reference
   */
  public void setLexicalHandler(final LexicalHandler lh) {
    lexicalHandler = lh;
  }

  @Override
  public void setEntityResolver(final EntityResolver er) {
    entityResolver = er;
  }

  @Override
  public EntityResolver getEntityResolver() {
    return entityResolver;
  }

  @Override
  public void setDTDHandler(final DTDHandler dtd) {
    dtdHandler = dtd;
  }

  @Override
  public DTDHandler getDTDHandler() {
    return dtdHandler;
  }

  @Override
  public void setErrorHandler(final ErrorHandler eh) {
    errorHandler = eh;
  }

  @Override
  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  @Override
  public void setFeature(final String name, final boolean value)
      throws SAXNotRecognizedException {
    throw new SAXNotRecognizedException();
  }

  @Override
  public void setProperty(final String name, final Object value)
      throws SAXNotRecognizedException {
    throw new SAXNotRecognizedException();
  }

  // Serializer =========================================================================

  /** Map containing all attributes. */
  private final Atts attributes = new Atts();
  /** Map containing all attributes. */
  private NSDecl namespaces;

  @Override
  protected void startOpen(final byte[] n) {
    namespaces = new NSDecl(namespaces);
    attributes.clear();
  }

  @Override
  protected void attribute(final byte[] n, final byte[] v) {
    byte[] prefix = null;
    if(startsWith(n, XMLNS)) {
      if(n.length == 5) {
        prefix = EMPTY;
      } else if(n[5] == ':') {
        prefix = substring(n, 6);
      }
    }

    if(prefix != null) {
      namespaces.put(prefix, v);
    } else {
      attributes.add(n, v);
    }
  }

  @Override
  protected void finishOpen() throws IOException {
    try {
      final AttributesImpl attrs = new AttributesImpl();
      final int as = attributes.size();
      for(int a = 0; a < as; a++) {
        final byte[] name = attributes.name(a);
        final String uri = string(namespaces.get(prefix(name)));
        final String lname = string(local(name));
        final String rname = string(name);
        final String value = string(attributes.value(a));
        attrs.addAttribute(uri, lname, rname, null, value);
      }

      final String uri = string(namespaces.get(prefix(tag)));
      final String lname = string(local(tag));
      final String rname = string(tag);
      contentHandler.startElement(uri, lname, rname, attrs);

    } catch(final SAXException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    try {
      final String name = string(tag);
      contentHandler.endElement("", name, name);
      namespaces = namespaces.getParent();
    } catch(final SAXException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  protected void finishText(final byte[] text) throws IOException {
    try {
      final String s = string(text);
      final char[] c = s.toCharArray();
      contentHandler.characters(c, 0, c.length);
    } catch(final SAXException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  protected void finishComment(final byte[] comment) throws IOException {
    if(lexicalHandler != null) {
      try {
        final String s = string(comment);
        final char[] c = s.toCharArray();
        lexicalHandler.comment(c, 0, c.length);
      } catch(final SAXException ex) {
        throw new IOException(ex);
      }
    }
  }

  @Override
  protected void finishPi(final byte[] n, final byte[] v) throws IOException {
    try {
      contentHandler.processingInstruction(string(n), string(v));
    } catch(final SAXException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  protected void atomic(final Item i, final boolean iter) {
    // ignored
  }

  /**
   * Namespace declaration.
   */
  static class NSDecl {
    /** Parent namespace declarations. */
    private final NSDecl parent;
    /** Namespace declarations. */
    private Atts decls;

    /**
     * Constructor.
     * @param par parent declarations
     */
    NSDecl(final NSDecl par) {
      parent = par;
    }

    /**
     * Returns the parent declarations.
     * @return parent declarations
     */
    NSDecl getParent() {
      return parent;
    }

    /**
     * Stores a new prefix and namespace.
     * @param prefix prefix
     * @param uri namespace uri
     */
    void put(final byte[] prefix, final byte[] uri) {
      if(decls == null) decls = new Atts();
      decls.add(prefix, uri);
    }

    /**
     * Retrieves the namespace uri for the given prefix.
     * @param prefix prefix to be found
     * @return namespace uri
     */
    byte[] get(final byte[] prefix) {
      for(NSDecl c = this; c != null; c = c.parent) {
        if(c.decls != null) {
          final byte[] ns = c.decls.value(prefix);
          if(ns != null) return ns;
        }
      }
      return EMPTY;
    }
  }
}
