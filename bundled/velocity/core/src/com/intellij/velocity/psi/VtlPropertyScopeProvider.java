/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.search.CustomPropertyScopeProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.velocity.psi.files.VtlFileType;

/**
 * @author peter
 */
public class VtlPropertyScopeProvider implements CustomPropertyScopeProvider{
  public SearchScope getScope(final Project project) {
    return GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), VtlFileType.INSTANCE);
  }
}
