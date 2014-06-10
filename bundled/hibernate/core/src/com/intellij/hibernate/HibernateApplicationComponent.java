/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate;

import com.intellij.facet.FacetTypeRegistry;
import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.hibernate.facet.HibernateFacetType;
import com.intellij.hibernate.model.enums.HibernateAttributeType;
import com.intellij.hibernate.model.manipulators.*;
import com.intellij.hibernate.model.xml.config.HibernateConfiguration;
import com.intellij.hibernate.model.xml.config.SessionFactory;
import com.intellij.hibernate.model.xml.impl.mapping.HbmEmbeddableImpl;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.hibernate.view.HibernateIcons;
import com.intellij.jam.view.JamDeleteHandler;
import com.intellij.javaee.CommonModelManager;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.jpa.util.JpaUtil;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.persistence.PersistenceHelper;
import com.intellij.persistence.model.manipulators.ManipulatorsRegistry;
import com.intellij.psi.PsiElement;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.impl.meta.MetaRegistry;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ReflectionCache;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * @author Gregory.Shrago
 */
public class HibernateApplicationComponent implements ApplicationComponent {


  @NonNls
  @NotNull
  public String getComponentName() {
    return "HibernateApplicationComponent";
  }

  public void initComponent() {
    FacetTypeRegistry.getInstance().registerFacetType(HibernateFacetType.INSTANCE);

    MetaRegistry.addMetadataBinding(new ElementFilter() {
      public boolean isAcceptable(Object element, PsiElement context) {
        if (!(element instanceof XmlTag)) return false;

        XmlTag tag = (XmlTag)element;
        final DomElement domElement = DomManager.getDomManager(tag.getProject()).getDomElement(tag);
        return domElement instanceof HbmClassBase && ((HbmClassBase)domElement).getEntityName().getXmlTag() == null;
      }

      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }, HbmClassMetaData.class);
    ElementPresentationManager.registerIcon(SessionFactory.class, HibernateIcons.SESSION_FACTORY_ICON);
    ElementPresentationManager.registerIcon(HbmHibernateMapping.class, HibernateIcons.HIBERNATE_MAPPING_ICON);
    ElementPresentationManager.registerIcon(HibernateConfiguration.class, HibernateIcons.HIBERNATE_CONFIG_ICON);
    ElementPresentationManager.registerIconProvider(new Function<Object, Icon>() {

      public Icon fun(final Object o) {
        if (o instanceof HbmClassBase) {
          return HibernateIcons.CLASS_ICON;
        }
        else if (o instanceof HbmComponent) {
          return HibernateIcons.COMPONENT_ICON;
        }
        return null;
      }
    });
    TypeNameManager.registerTypeName(SessionFactory.class, HibernateMessages.message("type.hibernate.session.factory"));
    TypeNameManager.registerTypeName(HbmClass.class, HibernateMessages.message("type.hibernate.class"));
    TypeNameManager.registerTypeName(HbmSubclass.class, HibernateMessages.message("type.hibernate.subclass"));
    TypeNameManager.registerTypeName(HbmJoinedSubclass.class, HibernateMessages.message("type.hibernate.joined.subclass"));
    TypeNameManager.registerTypeName(HbmUnionSubclass.class, HibernateMessages.message("type.hibernate.union.subclass"));
    TypeNameManager.registerTypeName(HbmEmbeddableImpl.class, HibernateMessages.message("type.hibernate.component"));
    TypeNameManager.registerTypeProvider(new Function<Class, String>() {
      @Nullable
      public String fun(final Class aClass) {
        if (ReflectionCache.isAssignable(HbmAttributeBase.class, aClass)
            || ReflectionCache.isAssignable(HbmContainer.class, aClass)) {
          return HibernateMessages.message("model.object.type.attribute");
        }
        return null;
      }
    });
    CommonModelManager.getInstance().registerDeleteHandler(new JamDeleteHandler() {
      public void addPsiElements(final CommonModelElement element, final Collection<PsiElement> result) {
        if (element instanceof HbmAttributeBase) {
          result.addAll(JpaUtil.getAttributePsiMembers((HbmAttributeBase)element));
        }
      }
    });

    final ManipulatorsRegistry manipulatorsRegistry = PersistenceHelper.getHelper().getManipulatorsRegistry();
    manipulatorsRegistry.registerManipulator(HibernateFacet.class, HibernateFacetManipulator.class);
    manipulatorsRegistry.registerManipulator(SessionFactory.class, SessionFactoryManipulator.class);
    manipulatorsRegistry.registerManipulator(HbmHibernateMapping.class, HbmMappingsManipulator.class);
    manipulatorsRegistry.registerManipulator(HbmPersistentObjectBase.class, HibernateObjectManipulator.class);
    manipulatorsRegistry.registerManipulator(HbmEmbeddableImpl.class, HibernateEmbeddableManipulator.class);
    manipulatorsRegistry.registerManipulator(HbmRelationAttributeBase.class, HibernateRelationshipAttributeManipulator.class);
    manipulatorsRegistry.registerManipulator(HbmAttributeBase.class, HibernateAttributeManipulator.class);

    HibernateAttributeType.initialize();
  }

  public void disposeComponent() {
  }

  public static class HbmClassMetaData extends DomMetaData<HbmClassBase> {

    protected GenericDomValue getNameElement(final HbmClassBase classBase) {
      return classBase.getClazz();
    }

    public void setName(final String name) throws IncorrectOperationException {
      getElement().getEntityName().setValue(name);
    }
  }

}
