package org.basex.http;

import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.http.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Bundles parameters of an HTTP request.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class HTTPParams {
  /** HTTP Context. */
  private final HTTPContext http;
  /** Parameter map. */
  private Map<String, String[]> map;
  /** Query parameters. */
  private Map<String, Value> query;
  /** Form parameters. */
  private Map<String, Value> form;
  /** Content body. */
  private IOContent content;

  /**
   * Returns an immutable map with all query parameters.
   * @param http HTTP context
   */
  public HTTPParams(final HTTPContext http) {
    this.http = http;
  }

  /**
   * Returns the query parameters as map.
   * @return map
   * @throws IOException I/O exception
   */
  public Map<String, String[]> map() throws IOException {
    try {
      if(map == null) map = http.req.getParameterMap();
      return map;
    } catch(final IllegalStateException ex) {
      // may be caused by too large input (#884)
      throw new IOException(ex);
    }
  }

  /**
   * Returns the request body as value.
   * @return value
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public Value content() throws QueryException, IOException {
    return HTTPPayload.value(body(), http.context().options, http.contentType(),
        http.contentTypeExt());
  }

  /**
   * Binds form parameters.
   * @return form parameters
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public Map<String, Value> form() throws QueryException, IOException {
    if(form == null) {
      form = new HashMap<>();
      final String ct = http.contentType();
      if(MimeTypes.MULTIPART_FORM_DATA.equals(ct)) {
        // convert multipart parameters encoded in a form
        addMultipart(form, http.contentTypeExt());
      } else if(MimeTypes.APP_FORM_URLENCODED.equals(ct)) {
        // convert URL-encoded parameters
        addURLEncoded(form);
      }
    }
    return form;
  }

  /**
   * Returns query parameters.
   * @return query parameters
   * @throws IOException I/O exception
   */
  public Map<String, Value> query() throws IOException {
    if(query == null) {
      query = new HashMap<>();
      for(final Map.Entry<String, String[]> entry : map().entrySet()) {
        final String key = entry.getKey();
        final String[] values = entry.getValue();
        final ValueBuilder vb = new ValueBuilder(values.length);
        for(final String v : values) vb.add(new Atm(v));
        query.put(key, vb.value());
      }
    }
    return query;
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Adds multipart form-data from the passed on request body.
   * @param params map parameters
   * @param ext content type extension (may be {@code null})
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void addMultipart(final Map<String, Value> params, final String ext)
      throws QueryException, IOException {

    final MainOptions opts = http.context().options;
    final HTTPPayload hp = new HTTPPayload(body().inputStream(), true, null, opts);
    final HashMap<String, Value> mp = hp.multiForm(ext);
    for(final Map.Entry<String, Value> entry : mp.entrySet()) {
      params.put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Adds URL-encoded parameters from the passed on request body.
   * @param params map parameters
   * @throws IOException I/O exception
   */
  private void addURLEncoded(final Map<String, Value> params) throws IOException {
    for(final String nv : body().toString().split("&")) {
      final String[] parts = nv.split("=", 2);
      if(parts.length == 2) params.put(parts[0], Str.get(URLDecoder.decode(parts[1], UTF8)));
    }
  }

  /**
   * Returns the cached body.
   * @return value
   * @throws IOException I/O exception
   */
  private IOContent body() throws IOException {
    if(content == null) {
      content = new IOContent(new BufferInput(http.req.getInputStream()).content());
      content.name(http.method + IO.XMLSUFFIX);
    }
    return content;
  }
}
