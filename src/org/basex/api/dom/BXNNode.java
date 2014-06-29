package org.basex.api.dom;

import org.basex.query.util.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Named node map implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BXNNode extends BXNList implements NamedNodeMap {
  /**
   * Constructor.
   * @param nb nodes
   */
  public BXNNode(final ANodeList nb) {
    super(nb);
  }

  @Override
  public BXNode getNamedItem(final String name) {
    final byte[] nm = Token.token(name);
    final int s = getLength();
    for(int i = 0; i < s; ++i) {
      if(Token.eq(nl.get(i).name(), nm)) return item(i);
    }
    return null;
  }

  @Override
  public BXNode getNamedItemNS(final String uri, final String ln) {
    throw Util.notImplemented();
  }

  @Override
  public BXNode setNamedItem(final Node node) {
    throw Util.notImplemented();
  }

  @Override
  public BXNode removeNamedItem(final String name) {
    throw Util.notImplemented();
  }

  @Override
  public BXNode setNamedItemNS(final Node node) {
    throw Util.notImplemented();
  }

  @Override
  public BXNode removeNamedItemNS(final String uri, final String ln) {
    throw Util.notImplemented();
  }
}
