package com.intellij.hibernate.model.xml.mapping;

/**
 * @author Gregory.Shrago
 */
public class HbmAttributeVisitorAdapter implements HbmAttributeVisitor {
  public void visitAttributeBase(final HbmAttributeBase attributeBase) {
  }

  public void visitRelationAttributeBase(final HbmRelationAttributeBase relationAttributeBase) {
    visitAttributeBase(relationAttributeBase);
  }

  public void visitEmbeddedAttributeBase(final HbmEmbeddedAttributeBase embeddedAttributeBase) {
    visitAttributeBase(embeddedAttributeBase);
  }

  public void visitId(final HbmId id) {
    visitAttributeBase(id);
  }

  public void visitCompositeId(final HbmCompositeId compositeId) {
    visitEmbeddedAttributeBase(compositeId);
  }

  public void visitVersion(final HbmVersion version) {
    visitAttributeBase(version);
  }

  public void visitTimestamp(final HbmTimestamp timestamp) {
    visitAttributeBase(timestamp);
  }

  public void visitProperty(final HbmProperty property) {
    visitAttributeBase(property);
  }

  public void visitManyToOne(final HbmManyToOne manyToOne) {
    visitRelationAttributeBase(manyToOne);
  }

  public void visitKeyProperty(final HbmKeyProperty property) {
    visitAttributeBase(property);
  }

  public void visitKeyManyToOne(final HbmKeyManyToOne manyToOne) {
    visitRelationAttributeBase(manyToOne);
  }

  public void visitOneToOne(final HbmOneToOne oneToOne) {
    visitRelationAttributeBase(oneToOne);
  }

  public void visitComponent(final HbmComponent component) {
    visitEmbeddedAttributeBase(component);
  }

  public void visitDynamicComponent(final HbmDynamicComponent dynamicComponent) {
    visitAttributeBase(dynamicComponent);
  }

  public void visitAny(final HbmAny any) {
    visitAttributeBase(any);
  }

  public void visitElement(final HbmElement element) {
    visitAttributeBase(element);
  }

  public void visitCompositeElement(final HbmCompositeElement element) {
    visitEmbeddedAttributeBase(element);
  }

  public void visitNestedCompositeElement(final HbmNestedCompositeElement element) {
    visitEmbeddedAttributeBase(element);
  }

  public void visitManyToMany(final HbmManyToMany manyToMany) {
    visitRelationAttributeBase(manyToMany);
  }

  public void visitOneToMany(final HbmOneToMany oneToMany) {
    visitRelationAttributeBase(oneToMany);
  }

}
