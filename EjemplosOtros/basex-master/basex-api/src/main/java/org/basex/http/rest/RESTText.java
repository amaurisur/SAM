package org.basex.http.rest;

import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
interface RESTText {
  /** REST string.  */
  String REST = "rest";
  /** REST URI. */
  String RESTURI = Prop.URL + '/' + REST;

  /** Name. */
  QNm Q_DATABASES = QNm.get(REST, "databases", RESTURI);
  /** Name. */
  QNm Q_DATABASE = QNm.get(REST, "databases", RESTURI);
  /** Name. */
  QNm Q_RESOURCE = QNm.get(REST, "resource", RESTURI);

  /** Attribute. */
  String RESOURCES = "resources";
  /** Attribute. */
  String NAME = "name";

  /** Command operation. */
  String COMMAND = "command";
  /** Run operation. */
  String RUN = "run";
  /** Query operation. */
  String QUERY = "query";

  /** Wrap parameter. */
  String WRAP = "wrap";
  /** Initial context. */
  String CONTEXT = "context";
}
