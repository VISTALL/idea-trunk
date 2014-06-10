/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.xml;

import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementVisitor;

/**
 * @author Gregory.Shrago
 */
public abstract class HibernateMappingVisitor implements DomElementVisitor {

  public void visitDomElement                  (final DomElement o) { }
  public void visitModelElement                (final CommonDomModelElement o) { visitDomElement(o); }

  public void visitHbmAny                      (final HbmAny                        o) { visitHbmAttributeBase(o); }
  public void visitHbmArray                    (final HbmArray                      o) { visitHbmContainer(o);}
  public void visitHbmAttributeBase            (final HbmAttributeBase              o) { visitModelElement(o);}
  public void visitHbmAttributeContainerBase   (final HbmAttributeContainerBase     o) { visitModelElement(o); }
  public void visitHbmBag                      (final HbmBag                        o) { visitHbmContainer(o); }
  public void visitHbmCache                    (final HbmCache                      o) { visitModelElement(o); }
  public void visitHbmClass                    (final HbmClass                      o) { visitHbmClassBase(o); }
  public void visitHbmClassBase                (final HbmClassBase                  o) { visitHbmPersistentObjectBase(o); }
  public void visitHbmCollectionAttributeBase  (final HbmCollectionAttributeBase    o) { visitHbmAttributeBase(o); }
  public void visitHbmContainer                (final HbmContainer                  o) { visitModelElement(o); }
  public void visitHbmCollectionId             (final HbmCollectionId               o) { visitModelElement(o); }
  public void visitHbmColumn                   (final HbmColumn                     o) { visitModelElement(o); }
  public void visitHbmComponent                (final HbmComponent                  o) { visitHbmEmbeddedAttributeBase(o); }
  public void visitHbmCompositeElement         (final HbmCompositeElement           o) { visitHbmCollectionAttributeBase(o); }
  public void visitHbmCompositeId              (final HbmCompositeId                o) { visitHbmEmbeddedAttributeBase(o); }
  public void visitHbmCompositeIndex           (final HbmCompositeIndex             o) { visitModelElement(o); }
  public void visitHbmCompositeMapKey          (final HbmCompositeMapKey            o) { visitHbmPersistentObjectBase(o); }
  public void visitHbmDatabaseObject           (final HbmDatabaseObject             o) { visitModelElement(o); }
  public void visitHbmDefinition               (final HbmDefinition                 o) { visitModelElement(o); }
  public void visitHbmDialectScope             (final HbmDialectScope               o) { visitModelElement(o); }
  public void visitHbmDiscriminator            (final HbmDiscriminator              o) { visitModelElement(o); }
  public void visitHbmDynamicComponent         (final HbmDynamicComponent           o) { visitHbmAttributeBase(o); }
  public void visitHbmElement                  (final HbmElement                    o) { visitHbmCollectionAttributeBase(o); }
  public void visitHbmEmbeddedAttributeBase    (final HbmEmbeddedAttributeBase      o) { visitHbmAttributeBase(o); }
  public void visitHbmFilter                   (final HbmFilter                     o) { visitModelElement(o); }
  public void visitHbmFilterDef                (final HbmFilterDef                  o) { visitModelElement(o); }
  public void visitHbmFilterParam              (final HbmFilterParam                o) { visitModelElement(o); }
  public void visitHbmGenerator                (final HbmGenerator                  o) { visitModelElement(o); }
  public void visitHbmHibernateMapping         (final HbmHibernateMapping           o) { visitModelElement(o); }
  public void visitHbmId                       (final HbmId                         o) { visitHbmAttributeBase(o); }
  public void visitHbmIdbag                    (final HbmIdbag                      o) { visitHbmContainer(o); }
  public void visitHbmImport                   (final HbmImport                     o) { visitModelElement(o); }
  public void visitHbmIndex                    (final HbmIndex                      o) { visitModelElement(o); }
  public void visitHbmIndexManyToAny           (final HbmIndexManyToAny             o) { visitModelElement(o); }
  public void visitHbmIndexManyToMany          (final HbmIndexManyToMany            o) { visitModelElement(o); }
  public void visitHbmJoin                     (final HbmJoin                       o) { visitHbmAttributeContainerBase(o); }
  public void visitHbmJoinedSubclass           (final HbmJoinedSubclass             o) { visitHbmClassBase(o); }
  public void visitHbmKey                      (final HbmKey                        o) { visitModelElement(o); }
  public void visitHbmKeyManyToOne             (final HbmKeyManyToOne               o) { visitModelElement(o); }
  public void visitHbmKeyProperty              (final HbmKeyProperty                o) { visitModelElement(o); }
  public void visitHbmList                     (final HbmList                       o) { visitHbmContainer(o); }
  public void visitHbmListIndex                (final HbmListIndex                  o) { visitModelElement(o); }
  public void visitHbmLoadCollection           (final HbmLoadCollection             o) { visitModelElement(o); }
  public void visitHbmLoader                   (final HbmLoader                     o) { visitModelElement(o); }
  public void visitHbmManyToAny                (final HbmManyToAny                  o) { visitModelElement(o); }
  public void visitHbmManyToMany               (final HbmManyToMany                 o) { visitHbmRelationAttributeBase(o); }
  public void visitHbmManyToOne                (final HbmManyToOne                  o) { visitHbmRelationAttributeBase(o); }
  public void visitHbmMap                      (final HbmMap                        o) { visitHbmContainer(o); }
  public void visitHbmMapKey                   (final HbmMapKey                     o) { visitModelElement(o); }
  public void visitHbmMapKeyManyToMany         (final HbmMapKeyManyToMany           o) { visitModelElement(o); }
  public void visitHbmMeta                     (final HbmMeta                       o) { visitModelElement(o); }
  public void visitHbmMetaValue                (final HbmMetaValue                  o) { visitModelElement(o); }
  public void visitHbmNaturalId                (final HbmNaturalId                  o) { visitModelElement(o); }
  public void visitHbmNestedCompositeElement   (final HbmNestedCompositeElement     o) { visitModelElement(o); }
  public void visitHbmOneToMany                (final HbmOneToMany                  o) { visitHbmRelationAttributeBase(o); }
  public void visitHbmOneToOne                 (final HbmOneToOne                   o) { visitHbmRelationAttributeBase(o); }
  public void visitHbmParam                    (final HbmParam                      o) { visitModelElement(o); }
  public void visitHbmParent                   (final HbmParent                     o) { visitModelElement(o); }
  public void visitHbmPersistentObjectBase     (final HbmPersistentObjectBase       o) { visitHbmAttributeContainerBase(o); }
  public void visitHbmPrimitiveArray           (final HbmPrimitiveArray             o) { visitHbmContainer(o); }
  public void visitHbmProperties               (final HbmProperties                 o) { visitHbmAttributeContainerBase(o); }
  public void visitHbmProperty                 (final HbmProperty                   o) { visitHbmAttributeBase(o); }
  public void visitHbmQuery                    (final HbmQuery                      o) { visitModelElement(o); }
  public void visitHbmQueryParam               (final HbmQueryParam                 o) { visitModelElement(o); }
  public void visitHbmRelationAttributeBase    (final HbmRelationAttributeBase      o) { visitHbmAttributeBase(o); }
  public void visitHbmResultset                (final HbmResultset                  o) { visitModelElement(o); }
  public void visitHbmReturn                   (final HbmReturn                     o) { visitModelElement(o); }
  public void visitHbmReturnColumn             (final HbmReturnColumn               o) { visitModelElement(o); }
  public void visitHbmReturnDiscriminator      (final HbmReturnDiscriminator        o) { visitModelElement(o); }
  public void visitHbmReturnJoin               (final HbmReturnJoin                 o) { visitModelElement(o); }
  public void visitHbmReturnProperty           (final HbmReturnProperty             o) { visitModelElement(o); }
  public void visitHbmReturnScalar             (final HbmReturnScalar               o) { visitModelElement(o); }
  public void visitHbmSet                      (final HbmSet                        o) { visitHbmContainer(o); }
  public void visitHbmSqlQuery                 (final HbmSqlQuery                   o) { visitModelElement(o); }
  public void visitHbmSqlStatement             (final HbmSqlStatement               o) { visitModelElement(o); }
  public void visitHbmSubclass                 (final HbmSubclass                   o) { visitHbmClassBase(o); }
  public void visitHbmSynchronize              (final HbmSynchronize                o) { visitModelElement(o); }
  public void visitHbmTableInfoProvider        (final HbmTableInfoProvider          o) { visitModelElement(o); }
  public void visitHbmTimestamp                (final HbmTimestamp                  o) { visitHbmAttributeBase(o); }
  public void visitHbmTuplizer                 (final HbmTuplizer                   o) { visitModelElement(o); }
  public void visitHbmType                     (final HbmType                       o) { visitModelElement(o); }
  public void visitHbmTypedef                  (final HbmTypedef                    o) { visitModelElement(o); }
  public void visitHbmUnionSubclass            (final HbmUnionSubclass              o) { visitHbmClassBase(o); }
  public void visitHbmVersion                  (final HbmVersion                    o) { visitHbmAttributeBase(o); }
}
