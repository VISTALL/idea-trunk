package com.intellij.hibernate.model.xml.mapping;

import com.intellij.javaee.model.xml.CommonDomModelElement;

/**
 * @author Gregory.Shrago
 */
public interface HbmAttributeContainerBase extends CommonDomModelElement {

  void visitAttributes(final HbmAttributeVisitor visitor);

}