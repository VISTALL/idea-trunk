/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.xml.mapping;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Gregory.Shrago
 */
public interface HbmPersistentObjectBaseEx extends HbmPersistentObjectBase {
  @NotNull
  List<HbmMeta> getMetas();
  HbmMeta addMeta();

  @NotNull
  List<HbmTuplizer> getTuplizers();
  HbmTuplizer addTuplizer();

  @NotNull
  List<HbmProperty> getProperties();
  HbmProperty addProperty();

  @NotNull
  List<HbmManyToOne> getManyToOnes();
  HbmManyToOne addManyToOne();

  @NotNull
  List<HbmOneToOne> getOneToOnes();
  HbmOneToOne addOneToOne();

  @NotNull
  List<HbmComponent> getComponents();
  HbmComponent addComponent();

  @NotNull
  List<HbmDynamicComponent> getDynamicComponents();
  HbmDynamicComponent addDynamicComponent();

  @NotNull
  List<HbmAny> getAnies();
  HbmAny addAny();

  @NotNull
  List<HbmMap> getMaps();
  HbmMap addMap();

  @NotNull
  List<HbmSet> getSets();
  HbmSet addSet();

  @NotNull
  List<HbmList> getLists();
  HbmList addList();

  @NotNull
  List<HbmBag> getBags();
  HbmBag addBag();

  @NotNull
  List<HbmArray> getArrays();
  HbmArray addArray();

  @NotNull
  List<HbmPrimitiveArray> getPrimitiveArrays();
  HbmPrimitiveArray addPrimitiveArray();

}