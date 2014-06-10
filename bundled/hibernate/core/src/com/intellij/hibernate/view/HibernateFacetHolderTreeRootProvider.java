/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.view;

import com.intellij.jpa.view.PersistenceFacetHolderTreeRootProvider;
import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.hibernate.facet.HibernateFacetType;
import com.intellij.facet.FacetType;

/**
 * @author nik
 */
public class HibernateFacetHolderTreeRootProvider extends PersistenceFacetHolderTreeRootProvider<HibernateFacet> {
  public HibernateFacetHolderTreeRootProvider() {
    super(HibernateFacetType.INSTANCE);
  }
}
