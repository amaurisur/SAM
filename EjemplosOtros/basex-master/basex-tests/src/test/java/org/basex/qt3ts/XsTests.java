package org.basex.qt3ts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.basex.qt3ts.xs.*;

/**
 * Test suite for the "xs" test group.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@RunWith(Suite.class)
@SuiteClasses({
  XsAnyURI.class,
  XsBase64Binary.class,
  XsDouble.class,
  XsFloat.class,
  XsHexBinary.class,
  XsNormalizedString.class,
  XsToken.class
})
public class XsTests { }
