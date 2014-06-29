package org.basex.query.util.http;

import java.util.*;

import org.basex.query.iter.*;
import org.basex.util.hash.*;

/**
 * Container for parsed data from {@code <http:request/>}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 */
public final class HTTPRequest {
  /** Request attributes. */
  public final TokenMap attrs = new TokenMap();
  /** Request headers. */
  public final TokenMap headers = new TokenMap();
  /** Body or multipart attributes. */
  public final TokenMap payloadAttrs = new TokenMap();
  /** Body content. */
  public final ValueBuilder bodyContent = new ValueBuilder();
  /** Parts in case of multipart request. */
  public final ArrayList<Part> parts = new ArrayList<>();
  /** Indicator for multipart request. */
  public boolean isMultipart;

  /**
   * Container for parsed data from <part/> element.
   * @author BaseX Team 2005-14, BSD License
   * @author Rositsa Shadura
   */
  public static class Part {
    /** Part headers. */
    public final TokenMap headers = new TokenMap();
    /** Attributes of part body. */
    public final TokenMap bodyAttrs = new TokenMap();
    /** Content of part body. */
    public final ValueBuilder bodyContent = new ValueBuilder();
  }
}
