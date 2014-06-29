package org.basex.core;

import org.basex.util.list.*;

/**
 * Result object for databases function.
 * @see Proc#databases(LockResult)
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Jens Erat
 */
public class LockResult {
  /** List of databases to read lock. */
  public final StringList read = new StringList(1);
  /** List of databases to write lock. */
  public final StringList write = new StringList(1);
  /** Flag if global read lock is required. */
  public boolean readAll;
  /** Flag if global write lock is required. */
  public boolean writeAll;

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("LockResult: ");
    sb.append("Read ").append(read);
    sb.append(", Write ").append(write);
    return sb.toString();
  }
}
