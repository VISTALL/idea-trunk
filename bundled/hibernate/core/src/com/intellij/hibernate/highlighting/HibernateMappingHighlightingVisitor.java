/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.highlighting;

import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.model.xml.HibernateMappingVisitor;
import com.intellij.hibernate.model.xml.mapping.HbmClassBase;
import com.intellij.hibernate.model.xml.mapping.HbmJoinedSubclass;
import com.intellij.hibernate.model.xml.mapping.HbmSubclass;
import com.intellij.hibernate.model.xml.mapping.HbmUnionSubclass;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.TypeNameManager;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;

/**
 * @author Gregory.Shrago
 */
public class HibernateMappingHighlightingVisitor extends HibernateMappingVisitor {
  private final DomElementAnnotationHolder myAnnotator;

  public HibernateMappingHighlightingVisitor(final DomElementAnnotationHolder annotator) {
    myAnnotator = annotator;
  }

  private void checkHierarchy(final HbmClassBase o, final GenericDomValue<PsiClass> extendsValue) {
    if (o.getParent() instanceof HbmClassBase) {
      checkClassExtends(o, ((HbmClassBase)o.getParent()).getClazz());
      if (DomUtil.hasXml(extendsValue)) {
        myAnnotator.createProblem(extendsValue, HibernateMessages.message("message.0.should.not.be.specified.for.nested.1", extendsValue.getXmlElementName(),
                                                                          TypeNameManager.getTypeName(o.getClass())));
      }
    }
    else {
      checkClassExtends(o, extendsValue);
    }
  }

  private void checkClassExtends(final HbmClassBase subclass, final GenericDomValue<PsiClass> superclass) {
    final PsiClass psiSubclass = subclass.getClazz().getValue();
    final PsiClass psiSuperclass = superclass.getValue();
    if (psiSubclass != null && psiSuperclass != null && !psiSubclass.isInheritor(psiSuperclass, true)) {
      myAnnotator.createProblem(subclass.getClazz(), HibernateMessages.message("message.0.should.be.sublclass.of.1", psiSubclass.getQualifiedName(),
                                                                        psiSuperclass.getQualifiedName()));
    }
  }

  @Override public void visitHbmSubclass(final HbmSubclass o) {
    super.visitHbmSubclass(o);
    checkHierarchy(o, o.getExtends());
  }

  @Override public void visitHbmUnionSubclass(final HbmUnionSubclass o) {
    super.visitHbmUnionSubclass(o);
    checkHierarchy(o, o.getExtends());
  }

  @Override public void visitHbmJoinedSubclass(final HbmJoinedSubclass o) {
    super.visitHbmJoinedSubclass(o);
    checkHierarchy(o, o.getExtends());
  }
}
