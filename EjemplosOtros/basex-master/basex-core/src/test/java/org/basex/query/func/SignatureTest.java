package org.basex.query.func;

import java.util.*;
import java.util.Set;

import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.query.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Tests all function signatures.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class SignatureTest extends AdvancedQueryTest {
  /**
   * Tests the validity of all function signatures.
   * @throws Exception exception
   */
  @Test
  public void signatures() throws Exception {
    context.openDB(CreateDB.mainMem(new IOContent("<a/>"), context));
    context.data().meta.name = "X";
    for(final Function f : Function.values()) check(f);
  }

  /**
   * Checks if the specified function correctly handles its argument types,
   * and returns the function name.
   * @param def function definition
   * types are supported.
   */
  private static void check(final Function def) {
    final String desc = def.toString();
    final String name = desc.replaceAll("\\(.*", "");

    // check that there are enough argument names
    final String[] names = def.names();
    assertTrue(def + Arrays.toString(names),
        names.length == (def.max == Integer.MAX_VALUE ? def.min : def.max));
    // all variable names must be distinct
    final Set<String> set = new HashSet<>(Arrays.asList(names));
    assertEquals("Duplicate argument names: " + def, names.length, set.size());
    // var-arg functions must have a number at the end
    if(def.max == Integer.MAX_VALUE) assertTrue(names[names.length - 1].matches(".*\\d+$"));

    // test too few, too many, and wrong argument types
    for(int al = Math.max(def.min - 1, 0); al <= def.max + 1; al++) {
      final boolean in = al >= def.min && al <= def.max;
      final StringBuilder qu = new StringBuilder(name + '(');
      int any = 0;
      for(int a = 0; a < al; a++) {
        if(a != 0) qu.append(", ");
        if(in) {
          // test arguments
          if(def.args[a].type == AtomType.STR) {
            qu.append('1');
          } else { // any type (skip test)
            qu.append("'X'");
            if(SeqType.STR.instanceOf(def.args[a])) any++;
          }
        } else {
          // test wrong number of arguments
          qu.append("'x'");
        }
      }
      // skip test if all types are arbitrary
      if((def.min > 0 || al != 0) && (any == 0 || any != al)) {
        final String query = qu.append(')').toString();
        // wrong types: XPTY0004, FORG0006, FODC0002, BXDB0001, BXDB0004
        if(in) error(query, Err.INVCAST, Err.NONUMBER, Err.INVFUNCITEM,
            Err.STRNODTYPE, Err.ELMMAPTYPE, Err.BINARYTYPE,
            Err.STRBINTYPE, Err.WHICHRES, Err.BXDB_NODB, Err.BXDB_INDEX);
        // wrong number of arguments: XPST0017
        else error(query, Err.FUNCARGSG, Err.FUNCARGPL);
      }
    }
  }
}
