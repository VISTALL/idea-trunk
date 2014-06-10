package com.intellij.spring.impl.model.beans;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.ListOrSet;
import com.intellij.spring.model.xml.beans.TypeHolder;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Collections;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class ListOrSetImpl extends TypedCollectionImpl implements ListOrSet {

  @NotNull
  public List<? extends PsiType> getRequiredTypes() {
    final List<? extends PsiType> list = super.getRequiredTypes();
    if (!list.isEmpty()) {
      return list;
    }
    final PsiType fromGenerics = getRequiredTypeFromGenerics();
    final Project project = getManager().getProject();

    final PsiType type = fromGenerics != null
                         ? fromGenerics
                         : PsiType.getJavaLangObject(PsiManager.getInstance(project), GlobalSearchScope.allScope(project));
    return Collections.singletonList(type);  
  }

  @Nullable
  private PsiType getRequiredTypeFromGenerics() {
    final DomElement parent = getParent();
    if (parent instanceof TypeHolder) {
      final List<? extends PsiType> types = ((TypeHolder)parent).getRequiredTypes();
      if (types.isEmpty()) return null;
      final PsiType type = types.get(0);
      if (type instanceof PsiClassType) {
        final List<PsiType> list = SpringUtils.resolveGenerics((PsiClassType)type);

        return list.size() == 1 ? list.get(0) : null;
      } else if (type instanceof PsiArrayType) {
        return ((PsiArrayType)type).getComponentType();
      }
    }
    return null;
  }
}
