package com.intellij.hibernate.model.xml.impl.mapping;

import com.intellij.util.xml.DomElementVisitor;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.hibernate.util.HibernateUtil;

/**
 * @author Gregory.Shrago
 */
public class HbmAttributeDomElementVisitor implements DomElementVisitor {
  private final HbmAttributeVisitor myVisitor;

  public HbmAttributeDomElementVisitor(final HbmAttributeVisitor visitor) {
    myVisitor = visitor;
  }

  public void visitDomElement(DomElement element) {
  }

  public void visitHbmId(final HbmId id) {
    myVisitor.visitId(id);
  }

  public void visitHbmVersion(final HbmVersion version) {
    myVisitor.visitVersion(version);
  }

  public void visitHbmTimestamp(final HbmTimestamp timestamp) {
    myVisitor.visitTimestamp(timestamp);
  }

  public void visitHbmProperty(final HbmProperty property) {
    myVisitor.visitProperty(property);
  }

  public void visitHbmManyToOne(final HbmManyToOne manyToOne) {
    myVisitor.visitManyToOne(manyToOne);
  }

  public void visitHbmKeyProperty(final HbmKeyProperty property) {
    myVisitor.visitKeyProperty(property);
  }

  public void visitHbmKeyManyToOne(final HbmKeyManyToOne manyToOne) {
    myVisitor.visitKeyManyToOne(manyToOne);
  }

  public void visitHbmOneToOne(final HbmOneToOne oneToOne) {
    myVisitor.visitOneToOne(oneToOne);
  }

  public void visitHbmComponent(final HbmComponent component) {
    myVisitor.visitComponent(component);
  }

  public void visitHbmDynamicComponent(final HbmDynamicComponent dynamicComponent) {
    myVisitor.visitDynamicComponent(dynamicComponent);
  }

  public void visitHbmAny(final HbmAny any) {
    myVisitor.visitAny(any);
  }

  public void visitHbmElement(final HbmElement element) {
    myVisitor.visitElement(element);
  }

  public void visitHbmCompositeElement(final HbmCompositeElement element) {
    myVisitor.visitCompositeElement(element);
  }

  public void visitHbmNestedCompositeElement(final HbmNestedCompositeElement element) {
    myVisitor.visitNestedCompositeElement(element);
  }

  public void visitHbmManyToMany(final HbmManyToMany manyToMany) {
    myVisitor.visitManyToMany(manyToMany);
  }

  public void visitHbmOneToMany(final HbmOneToMany oneToMany) {
    myVisitor.visitOneToMany(oneToMany);
  }

  // complex elements
  public void visitHbmCompositeId(final HbmCompositeId compositeId) {
    if (HibernateUtil.isEmbedded(compositeId)) {
      myVisitor.visitCompositeId(compositeId);
    }
    else {
      DomUtil.acceptAvailableChildren(compositeId, this);
    }
  }

  public void visitHbmJoin(final HbmJoin join) {
    DomUtil.acceptAvailableChildren(join, this);
  }

  public void visitHbmProperties(final HbmProperties properties) {
    DomUtil.acceptAvailableChildren(properties, this);
  }

  public void visitHbmNaturalId(final HbmNaturalId naturalId) {
    DomUtil.acceptAvailableChildren(naturalId, this);
  }

  // holders
  public void visitHbmArray(final HbmArray array) {
    DomUtil.acceptAvailableChildren(array, this);
  }

  public void visitHbmBag(final HbmBag bag) {
    DomUtil.acceptAvailableChildren(bag, this);
  }

  public void visitHbmIdbag(final HbmIdbag idbag) {
    DomUtil.acceptAvailableChildren(idbag, this);
  }

  public void visitHbmList(final HbmList list) {
    DomUtil.acceptAvailableChildren(list, this);
  }

  public void visitHbmSet(final HbmSet set) {
    DomUtil.acceptAvailableChildren(set, this);
  }

  public void visitHbmMap(final HbmMap map) {
    DomUtil.acceptAvailableChildren(map, this);
  }

  public void visitHbmPrimitiveArray(final HbmPrimitiveArray primitiveArray) {
    DomUtil.acceptAvailableChildren(primitiveArray, this);
  }

}
