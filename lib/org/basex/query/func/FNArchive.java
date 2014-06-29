package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.archive.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Functions on archives.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNArchive extends StandardFunc {
  /** Packer format: gzip. */
  public static final String GZIP = "gzip";
  /** Packer format: zip. */
  public static final String ZIP = "zip";

  /** Module prefix. */
  private static final String PREFIX = "archive";
  /** QName. */
  private static final QNm Q_ENTRY = QNm.get(PREFIX, "entry", ARCHIVEURI);
  /** QName. */
  private static final QNm Q_OPTIONS = QNm.get(PREFIX, "options", ARCHIVEURI);
  /** QName. */
  private static final QNm Q_FORMAT = QNm.get(PREFIX, "format", ARCHIVEURI);
  /** QName. */
  private static final QNm Q_ALGORITHM = QNm.get(PREFIX, "algorithm", ARCHIVEURI);
  /** Root node test. */
  private static final NodeTest TEST = new NodeTest(Q_ENTRY);

  /** Level. */
  private static final String LEVEL = "compression-level";
  /** Encoding. */
  private static final String ENCODING = "encoding";
  /** Last modified. */
  private static final String LAST_MOD = "last-modified";
  /** Compressed size. */
  private static final String COMP_SIZE = "compressed-size";
  /** Uncompressed size. */
  private static final String SIZE = "size";
  /** Value. */
  private static final String VALUE = "value";

  /** Option: algorithm: deflate. */
  private static final String DEFLATE = "deflate";
  /** Option: algorithm: stored. */
  private static final String STORED = "stored";
  /** Option: algorithm: unknown. */
  private static final String UNKNOWN = "unknown";

  /** Archive options. */
  public static class ArchiveOptions extends Options {
    /** Archiving format. */
    public static final StringOption FORMAT = new StringOption("format", ZIP);
    /** Archiving algorithm. */
    public static final StringOption ALGORITHM = new StringOption("algorithm", DEFLATE);
  }

  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNArchive(final StaticContext sctx, final InputInfo ii, final Function f,
      final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _ARCHIVE_ENTRIES:        return entries(ctx);
      case _ARCHIVE_EXTRACT_TEXT:   return extractText(ctx);
      case _ARCHIVE_EXTRACT_BINARY: return extractBinary(ctx);
      default:                      return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    checkCreate(ctx);
    switch(sig) {
      case _ARCHIVE_CREATE:  return create(ctx);
      case _ARCHIVE_UPDATE:  return update(ctx);
      case _ARCHIVE_DELETE:  return delete(ctx);
      case _ARCHIVE_OPTIONS: return options(ctx);
      case _ARCHIVE_WRITE:   return write(ctx);
      default:               return super.item(ctx, ii);
    }
  }

  /**
   * Creates a new archive.
   * @param ctx query context
   * @return archive
   * @throws QueryException query exception
   */
  private B64 create(final QueryContext ctx) throws QueryException {
    final Iter entr = ctx.iter(expr[0]);
    final Iter cont = ctx.iter(expr[1]);
    final Options opts = checkOptions(2, Q_OPTIONS, new ArchiveOptions(), ctx);

    final String format = opts.get(ArchiveOptions.FORMAT);
    final ArchiveOut out = ArchiveOut.get(format.toLowerCase(Locale.ENGLISH), info);
    // check algorithm
    final String alg = opts.get(ArchiveOptions.ALGORITHM);
    int level = ZipEntry.DEFLATED;
    if(alg != null) {
      if(format.equals(ZIP)  && !eq(alg, STORED, DEFLATE) ||
         format.equals(GZIP) && !eq(alg, DEFLATE)) {
        throw ARCH_SUPP.get(info, ArchiveOptions.ALGORITHM.name(), alg);
      }
      if(eq(alg, STORED)) level = ZipEntry.STORED;
      else if(eq(alg, DEFLATE)) level = ZipEntry.DEFLATED;
    }
    out.level(level);

    try {
      int e = 0;
      int c = 0;
      Item en, cn;
      while(true) {
        en = entr.next();
        cn = cont.next();
        if(en == null || cn == null) break;
        if(out instanceof GZIPOut && c > 0)
          throw ARCH_ONE.get(info, format.toUpperCase(Locale.ENGLISH));
        add(checkElmStr(en), cn, out, level, ctx);
        e++;
        c++;
      }
      // count remaining entries
      if(cn != null) do c++; while(cont.next() != null);
      if(en != null) do e++; while(entr.next() != null);
      if(e != c) throw ARCH_DIFF.get(info, e, c);
    } catch(final IOException ex) {
      throw ARCH_FAIL.get(info, ex);
    } finally {
      out.close();
    }
    return new B64(out.toArray());
  }

  /**
   * Returns the options of an archive.
   * @param ctx query context
   * @return entries
   * @throws QueryException query exception
   */
  private FElem options(final QueryContext ctx) throws QueryException {
    final B64 archive = (B64) checkType(checkItem(expr[0], ctx), AtomType.B64);
    String format = null;
    int level = -1;

    final ArchiveIn arch = ArchiveIn.get(archive.input(info), info);
    try {
      format = arch.format();
      while(arch.more()) {
        final ZipEntry ze = arch.entry();
        if(ze.isDirectory()) continue;
        level = ze.getMethod();
        break;
      }
    } catch(final IOException ex) {
      throw ARCH_FAIL.get(info, ex);
    } finally {
      arch.close();
    }

    // create result element
    final FElem e = new FElem(Q_OPTIONS).declareNS();
    if(format != null) e.add(new FElem(Q_FORMAT).add(VALUE, format));
    if(level >= 0) {
      final String lvl = level == 8 ? DEFLATE : level == 0 ? STORED : UNKNOWN;
      e.add(new FElem(Q_ALGORITHM).add(VALUE, lvl));
    }
    return e;
  }

  /**
   * Returns the entries of an archive.
   * @param ctx query context
   * @return entries
   * @throws QueryException query exception
   */
  private Iter entries(final QueryContext ctx) throws QueryException {
    final B64 archive = (B64) checkType(checkItem(expr[0], ctx), AtomType.B64);

    final ValueBuilder vb = new ValueBuilder();
    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    try {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        if(ze.isDirectory()) continue;
        final FElem e = new FElem(Q_ENTRY).declareNS();
        long s = ze.getSize();
        if(s != -1) e.add(SIZE, token(s));
        s = ze.getTime();
        if(s != -1) e.add(LAST_MOD, new Dtm(s, info).string(info));
        s = ze.getCompressedSize();
        if(s != -1) e.add(COMP_SIZE, token(s));
        e.add(ze.getName());
        vb.add(e);
      }
      return vb;
    } catch(final IOException ex) {
      throw ARCH_FAIL.get(info, ex);
    } finally {
      in.close();
    }
  }

  /**
   * Extracts text entries.
   * @param ctx query context
   * @return text entry
   * @throws QueryException query exception
   */
  private ValueBuilder extractText(final QueryContext ctx) throws QueryException {
    final String enc = encoding(2, ARCH_ENCODING, ctx);
    final ValueBuilder vb = new ValueBuilder();
    for(final byte[] b : extract(ctx)) vb.add(Str.get(encode(b, enc, ctx)));
    return vb;
  }

  /**
   * Extracts binary entries.
   * @param ctx query context
   * @return binary entry
   * @throws QueryException query exception
   */
  private ValueBuilder extractBinary(final QueryContext ctx) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final byte[] b : extract(ctx)) vb.add(new B64(b));
    return vb;
  }

  /**
   * Updates an archive.
   * @param ctx query context
   * @return updated archive
   * @throws QueryException query exception
   */
  private B64 update(final QueryContext ctx) throws QueryException {
    final B64 archive = (B64) checkType(checkItem(expr[0], ctx), AtomType.B64);
    // entries to be updated
    final TokenObjMap<Item[]> hm = new TokenObjMap<>();

    final Iter entr = ctx.iter(expr[1]);
    final Iter cont = ctx.iter(expr[2]);
    int e = 0;
    int c = 0;
    Item en, cn;
    while(true) {
      en = entr.next();
      cn = cont.next();
      if(en == null || cn == null) break;
      hm.put(checkElmStr(en).string(info), new Item[] { en, cn });
      e++;
      c++;
    }
    // count remaining entries
    if(cn != null) do c++; while(cont.next() != null);
    if(en != null) do e++; while(entr.next() != null);
    if(e != c) throw ARCH_DIFF.get(info, e, c);

    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    final ArchiveOut out = ArchiveOut.get(in.format(), info);
    try {
      if(in instanceof GZIPIn)
        throw ARCH_MODIFY.get(info, in.format().toUpperCase(Locale.ENGLISH));
      // delete entries to be updated
      while(in.more()) if(!hm.contains(token(in.entry().getName()))) out.write(in);
      // add new and updated entries
      for(final byte[] h : hm) {
        if(h == null) continue;
        final Item[] it = hm.get(h);
        add(it[0], it[1], out, ZipEntry.DEFLATED, ctx);
      }
    } catch(final IOException ex) {
      throw ARCH_FAIL.get(info, ex);
    } finally {
      in.close();
      out.close();
    }
    return new B64(out.toArray());
  }

  /**
   * Deletes files from an archive.
   * @param ctx query context
   * @return updated archive
   * @throws QueryException query exception
   */
  private B64 delete(final QueryContext ctx) throws QueryException {
    final B64 archive = (B64) checkType(checkItem(expr[0], ctx), AtomType.B64);
    // entries to be deleted
    final TokenObjMap<Item[]> hm = new TokenObjMap<>();
    final Iter names = ctx.iter(expr[1]);
    for(Item en; (en = names.next()) != null;) {
      hm.put(checkElmStr(en).string(info), null);
    }

    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    final ArchiveOut out = ArchiveOut.get(in.format(), info);
    try {
      if(in instanceof GZIPIn)
        throw ARCH_MODIFY.get(info, in.format().toUpperCase(Locale.ENGLISH));
      while(in.more()) if(!hm.contains(token(in.entry().getName()))) out.write(in);
    } catch(final IOException ex) {
      throw ARCH_FAIL.get(info, ex);
    } finally {
      in.close();
      out.close();
    }
    return new B64(out.toArray());
  }

  /**
   * Extracts entries from the archive.
   * @param ctx query context
   * @return text entries
   * @throws QueryException query exception
   */
  private TokenList extract(final QueryContext ctx) throws QueryException {
    final B64 archive = (B64) checkType(checkItem(expr[0], ctx), AtomType.B64);
    final TokenSet hs = entries(1, ctx);

    final TokenList tl = new TokenList();
    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    try {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        if(!ze.isDirectory() && (hs == null || hs.delete(token(ze.getName())) != 0))
          tl.add(in.read());
      }
    } catch(final IOException ex) {
      throw ARCH_FAIL.get(info, ex);
    } finally {
      in.close();
    }
    return tl;
  }

  /**
   * Writes entries from an archive to disk.
   * @param ctx query context
   * @return text entries
   * @throws QueryException query exception
   */
  private Item write(final QueryContext ctx) throws QueryException {
    final java.nio.file.Path path = checkPath(0, ctx);
    final B64 archive = (B64) checkType(checkItem(expr[1], ctx), AtomType.B64);
    final TokenSet hs = entries(2, ctx);

    final ArchiveIn in = ArchiveIn.get(archive.input(info), info);
    try {
      while(in.more()) {
        final ZipEntry ze = in.entry();
        final String name = ze.getName();
        if(hs == null || hs.delete(token(name)) != 0) {
          final java.nio.file.Path file = path.resolve(name);
          if(ze.isDirectory()) {
            Files.createDirectories(file);
          } else {
            Files.createDirectories(file.getParent());
            Files.write(file, in.read());
          }
        }
      }
    } catch(final IOException ex) {
      throw ARCH_FAIL.get(info, ex);
    } finally {
      in.close();
    }
    return null;
  }

  /**
   * Returns all archive entries from the specified argument.
   * A {@code null} reference is returned if no entries are specified.
   * @param e argument index
   * @param ctx query context
   * @return set with all entries
   * @throws QueryException query exception
   */
  private TokenSet entries(final int e, final QueryContext ctx) throws QueryException {
    TokenSet hs = null;
    if(e < expr.length) {
      // filter result to specified entries
      hs = new TokenSet();
      final Iter names = ctx.iter(expr[e]);
      for(Item en; (en = names.next()) != null;) {
        hs.add(checkElmStr(en).string(info));
      }
    }
    return hs;
  }

  /**
   * Adds the specified entry to the output stream.
   * @param entry entry descriptor
   * @param cont contents
   * @param out output archive
   * @param level default compression level
   * @param ctx query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void add(final Item entry, final Item cont, final ArchiveOut out, final int level,
      final QueryContext ctx) throws QueryException, IOException {

    // create new zip entry
    String name = string(entry.string(info));
    if(name.isEmpty()) throw ARCH_EMPTY.get(info);
    if(Prop.WIN) name = name.replace('\\', '/');

    final ZipEntry ze = new ZipEntry(name);
    String enc = null;

    // compression level
    byte[] lvl = null;
    if(entry instanceof ANode) {
      final ANode el = (ANode) entry;
      lvl = el.attribute(LEVEL);

      // last modified
      final byte[] mod = el.attribute(LAST_MOD);
      if(mod != null) {
        try {
          ze.setTime(dateTimeToMs(new Dtm(mod, info), ctx));
        } catch(final QueryException qe) {
          throw ARCH_DATETIME.get(info, mod);
        }
      }

      // encoding
      final byte[] ea = el.attribute(ENCODING);
      if(ea != null) {
        enc = string(ea);
        if(!Charset.isSupported(enc)) throw ARCH_ENCODING.get(info, ea);
      }
    }

    // data to be compressed
    byte[] val = checkStrBin(cont);
    if(cont instanceof AStr && enc != null && enc != UTF8) val = encode(val, enc, ctx);

    try {
      out.level(lvl == null ? level : toInt(lvl));
    } catch(final IllegalArgumentException ex) {
      throw ARCH_LEVEL.get(info, lvl);
    }
    out.write(ze, val);
  }

  /**
   * Encodes the specified string to another encoding.
   * @param val value to be encoded
   * @param enc encoding
   * @param ctx query context
   * @return encoded string
   * @throws QueryException query exception
   */
  private byte[] encode(final byte[] val, final String enc, final QueryContext ctx)
      throws QueryException {

    try {
      return FNConvert.toString(new ArrayInput(val), enc, ctx);
    } catch(final IOException ex) {
      throw ARCH_ENCODE.get(info, ex);
    }
  }

  /**
   * Checks if the specified item is a string or element.
   * @param it item to be checked
   * @return item
   * @throws QueryException query exception
   */
  private Item checkElmStr(final Item it) throws QueryException {
    if(it instanceof AStr || TEST.eq(it)) return it;
    throw ELMSTRTYPE.get(info, Q_ENTRY.string(), it.type);
  }
}
