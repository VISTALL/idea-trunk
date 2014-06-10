package com.intellij.hibernate.model.xml.mapping;

/**
 * @author Gregory.Shrago
 */
public interface HbmAttributeVisitor {

  void visitId(final HbmId id);

  void visitCompositeId(final HbmCompositeId compositeId);

  void visitVersion(final HbmVersion version);

  void visitTimestamp(final HbmTimestamp timestamp);

  void visitProperty(final HbmProperty property);

  void visitManyToOne(final HbmManyToOne manyToOne);

  void visitKeyProperty(final HbmKeyProperty property);

  void visitKeyManyToOne(final HbmKeyManyToOne manyToOne);

  void visitOneToOne(final HbmOneToOne oneToOne);

  void visitComponent(final HbmComponent component);

  void visitDynamicComponent(final HbmDynamicComponent dynamicComponent);

  void visitAny(final HbmAny any);

  void visitElement(final HbmElement element);

  void visitCompositeElement(final HbmCompositeElement element);

  void visitNestedCompositeElement(final HbmNestedCompositeElement element);

  void visitManyToMany(final HbmManyToMany manyToMany);

  void visitOneToMany(final HbmOneToMany oneToMany);
}
