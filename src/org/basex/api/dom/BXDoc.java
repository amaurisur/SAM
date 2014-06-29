package org.basex.api.dom;

import static org.basex.util.Token.*;

import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.w3c.dom.*;

/**
 * DOM - Document implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BXDoc extends BXNode implements Document {
  /**
   * Constructor.
   * @param n node reference
   */
  public BXDoc(final ANode n) {
    super(n);
  }

  @Override
  public BXDoc getOwnerDocument() {
    return null;
  }

  @Override
  public BXNode adoptNode(final Node source) {
    throw readOnly();
  }

  @Override
  public BXAttr createAttribute(final String nm) {
    return new BXAttr(new FAttr(new QNm(nm), EMPTY));
  }

  @Override
  public BXAttr createAttributeNS(final String uri, final String qn) {
    return new BXAttr(new FAttr(new QNm(qn, uri), EMPTY));
  }

  @Override
  public BXCData createCDATASection(final String dat) {
    return new BXCData(new FTxt(dat));
  }

  @Override
  public BXComm createComment(final String dat) {
    return new BXComm(new FComm(dat));
  }

  @Override
  public BXDocFrag createDocumentFragment() {
    return new BXDocFrag(new FDoc(node.baseURI()));
  }

  @Override
  public BXElem createElement(final String nm) {
    return new BXElem(new FElem(new QNm(nm)));
  }

  @Override
  public BXElem createElementNS(final String uri, final String qn) {
    return new BXElem(new FElem(new QNm(qn, uri)));
  }

  @Override
  public EntityReference createEntityReference(final String name) {
    throw readOnly();
  }

  @Override
  public BXPI createProcessingInstruction(final String t, final String dat) {
    return new BXPI(new FPI(t, dat));
  }

  @Override
  public BXText createTextNode(final String dat) {
    return new BXText(new FTxt(dat));
  }

  @Override
  public DocumentType getDoctype() {
    return null;
  }

  @Override
  public BXElem getDocumentElement() {
    final BXNList list = getChildNodes();
    for(int l = 0; l < list.getLength(); ++l) {
      final BXNode n = list.item(l);
      if(n.getNodeType() == Node.ELEMENT_NODE) return (BXElem) n;
    }
    throw Util.notExpected();
  }

  @Override
  public String getDocumentURI() {
    return getBaseURI();
  }

  @Override
  public DOMConfiguration getDomConfig() {
    throw Util.notImplemented();
  }

  @Override
  public BXElem getElementById(final String elementId) {
    throw Util.notImplemented();
  }

  @Override
  public BXNList getElementsByTagName(final String name) {
    return getElements(name);
  }

  @Override
  public BXNList getElementsByTagNameNS(final String namespaceURI, final String localName) {
    throw Util.notImplemented();
  }

  @Override
  public DOMImplementation getImplementation() {
    return BXDomImpl.get();
  }

  @Override
  public String getInputEncoding() {
    return UTF8;
  }

  @Override
  public boolean getStrictErrorChecking() {
    throw Util.notImplemented();
  }

  @Override
  public String getXmlEncoding() {
    return UTF8;
  }

  @Override
  public boolean getXmlStandalone() {
    return false;
  }

  @Override
  public String getXmlVersion() {
    return "1.0";
  }

  @Override
  public BXNode importNode(final Node importedNode, final boolean deep) {
    throw Util.notImplemented();
  }

  @Override
  public void normalizeDocument() {
    throw readOnly();
  }

  @Override
  public BXNode renameNode(final Node n, final String namespaceURI,
      final String qualifiedName) {
    throw readOnly();
  }

  @Override
  public void setDocumentURI(final String documentURI) {
    throw readOnly();
  }

  @Override
  public void setStrictErrorChecking(final boolean strictErrorChecking) {
    throw Util.notImplemented();
  }

  @Override
  public void setXmlStandalone(final boolean xmlStandalone) {
    throw Util.notImplemented();
  }

  @Override
  public void setXmlVersion(final String xmlVersion) {
    throw Util.notImplemented();
  }
}
