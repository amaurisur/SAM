package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Format parser for integers and dates.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class FormatParser extends FormatUtil {
  /** Input information. */
  final InputInfo info;

  /** Case. */
  Case cs;
  /** Primary format token. */
  byte[] primary;
  /** First character of format token, or mandatory digit. */
  int first = -1;
  /** Ordinal suffix; {@code null} if not specified. */
  byte[] ordinal;
  /** Traditional modifier. */
  boolean trad;
  /** Minimum width. */
  int min;
  /** Maximum width. */
  int max = Integer.MAX_VALUE;

  /**
   * Constructor for formatting integers.
   * @param ii input info
   */
  FormatParser(final InputInfo ii) {
    info = ii;
  }

  /**
   * Parses and returns the presentation modifier.
   * @param pic picture
   * @param def default token
   * @param date date flag
   * @return presentation modifier
   * @throws QueryException query exception
   */
  byte[] presentation(final byte[] pic, final byte[] def, final boolean date)
      throws QueryException {

    // find primary format
    final TokenParser tp = new TokenParser(pic);
    int ch = tp.next();
    // check single character
    if(tp.more()) {
      // Word output (title case)
      if(ch == 'W' && tp.consume('w')) return pic;
      // Textual output (title case)
      if(date && ch == 'N' && tp.consume('n')) return pic;
    } else {
      // Latin, Greek and other alphabets
      if(sequence(ch) != null ||
        // Roman sequences (lower/upper case)
        ch == 'i' || ch == 'I' ||
        // Word output (lower/upper case)
        ch == 'w' || ch == 'W' ||
        // Textual output
        date && (ch == 'n' || ch == 'N') ||
        // circled, parenthesized or full stop digits
        ch == '\u2460' || ch == '\u2474' || ch == '\u2488' ||
        // Japanese numbering
        ch == KANJI[1]) return pic;
    }

    // find digit of decimal-digit-pattern
    tp.reset();
    while(first == -1 && tp.more()) first = zeroes(tp.next());
    // no digit found: return default primary token
    if(first == -1) return def;

    // flags for mandatory-digit-sign and group-separator-sign
    tp.reset();
    boolean gss = true;
    boolean mds = false;
    while(tp.more()) {
      ch = tp.next();
      final int d = zeroes(ch);
      if(d != -1) {
        // mandatory-digit-sign
        if(first != d) throw DIFFMAND.get(info, pic);
        mds = true;
        gss = false;
      } else if(ch == '#') {
        // optional-digit-sign
        if(mds) throw OPTAFTER.get(info, pic);
        gss = false;
      } else if(!Character.isLetter(ch)) {
        // grouping-separator-sign
        if(gss) throw INVGROUP.get(info, pic);
        gss = true;
      } else {
        // any other letter: return default primary token
        throw INVDDPATTERN.get(info, pic);
      }
    }
    if(gss) throw INVGROUP.get(info, pic);
    return pic;
  }

  /**
   * Finishes format parsing.
   * @param pres presentation string
   */
  void finish(final byte[] pres) {
    // skip correction of case if modifier has more than one codepoint (Ww)
    final int cp = ch(pres, 0);
    cs = cl(pres, 0) < pres.length || digit(cp) ? Case.STANDARD :
      (cp & ' ') == 0 ? Case.UPPER : Case.LOWER;
    primary = lc(pres);
    if(first == -1) first = ch(primary, 0);
  }
}
