/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate;

import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.jpa.JpaProjectComponent;
import com.intellij.jpa.ORMHelperRegistry;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.NullableFunction;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gregory.Shrago
 */
public class HibernateProjectComponent extends AbstractProjectComponent {

  @SuppressWarnings({"UnusedDeclaration"})
  public HibernateProjectComponent(final JpaProjectComponent jpaComponent, // dependency
                                   final Project project,
                                   final ORMHelperRegistry ormRegistry) {
    super(project);

    ormRegistry.registerTableInfoProvider(new NullableFunction<DomElement, Object>() {
      public Object fun(final DomElement domElement) {
        final DomElement elementParent = domElement.getParent();
        final DomElement parent = elementParent instanceof HbmColumn ? elementParent.getParent() : elementParent;
        if (parent instanceof HbmKey && parent.getParent() instanceof HbmContainer) {
          final HbmContainer container = (HbmContainer)parent.getParent();
          final HbmCollectionAttributeBase attribute = container.getContainedAttribute();
          if (attribute instanceof HbmRelationAttributeBase && StringUtil.isEmpty(container.getTableName().getValue())) {
            return ((HbmRelationAttributeBase)attribute).getTargetEntityClass().getValue();
          }
        }
        return null;
      }
    });

    //EntityRefactoringSupport.INSTANCE.initFindUsages(project, FindUsagesHandler.createFindUsagesOptions(project));
    //AttributeRefactoringSupport.INSTANCE.initFindUsages(project, FindUsagesHandler.createFindUsagesOptions(project));
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "HibernateProjectComponent";
  }

}
