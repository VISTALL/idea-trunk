/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.beanValidation.toolWindow.tree.nodes;

import com.intellij.beanValidation.BVIcons;
import com.intellij.beanValidation.constants.BvAnnoConstants;
import com.intellij.beanValidation.utils.BVUtils;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedMembersSearch;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.Icons;
import com.intellij.util.Processor;
import com.intellij.util.containers.CollectionFactory;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.SortedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Konstantin Bulenkov
 */
public class AnnotationTypeNode extends PsiMemberSimpleNode<PsiClass> {
  protected Module myModule;
  protected PsiClass myAnnoClass;

  public AnnotationTypeNode(@NotNull Module module, @NotNull PsiClass annoClass, SimpleNode parent) {
    super(parent, annoClass);

    myModule = module;
    myAnnoClass = annoClass;

    setPlainText(annoClass.getName());
    setUniformIcon(BVUtils.isInLibrary(annoClass) ? BVIcons.LIBRARY_CONSTRAINT : Icons.ANNOTATION_TYPE_ICON);
  }

  @Override
  protected void doUpdate() {
    super.doUpdate();
    clearColoredText();
    addColoredFragment(myAnnoClass.getName(), myAnnoClass.getQualifiedName(), getPlainAttributes());
  }

  private static final Comparator<PsiMemberSimpleNode> CONSTRAINT_OWNERS_COMPARATOR = new Comparator<PsiMemberSimpleNode>() {
    public int compare(PsiMemberSimpleNode o1, PsiMemberSimpleNode o2) {
      if (o1.getClass() != o2.getClass()) {
        return o1 instanceof AnnotatedMembersNode ? -1 : 1;
      }

      final String name = o1.getMember().getName();
      if (name == null) return -1;
      return name.compareTo(o2.getMember().getName());
    }
  };
  public SimpleNode[] getChildren() {
    Collection<PsiMemberSimpleNode> children = new SortedList<PsiMemberSimpleNode>(CONSTRAINT_OWNERS_COMPARATOR);
    
    for (PsiMember psiMember : AnnotatedMembersSearch.search(myAnnoClass, GlobalSearchScope.moduleWithLibrariesScope(myModule)).findAll()) {
      children.add(new AnnotatedMembersNode<PsiMember>(psiMember, this));
    }

    for (PsiClass validator : getValidators(myAnnoClass)) {
      children.add(new ValidatorNode(this, validator));
    }

    return children.toArray(new SimpleNode[children.size()]);
  }

  @Override
  public Object[] getEqualityObjects() {
    return new Object[]{myAnnoClass};
  }

  protected List<PsiClass> findAnnotatedClasses() {
    return findAnnotatedMembers(PsiClass.class);
  }

  protected List<PsiMethod> findAnnotatedMethods() {
    return findAnnotatedMembers(PsiMethod.class);
  }

  protected <T extends PsiModifierListOwner> List<T> findAnnotatedMembers(Class<T> clazz) {
    final List<T> result = CollectionFactory.arrayList();
    Processor<T> processor = new Processor<T>() {
      public boolean process(final T t) {
        ContainerUtil.addIfNotNull(t, result);
        return true;
      }
    };
    AnnotationModelUtil.findAnnotatedElements(clazz,
                                              myAnnoClass.getQualifiedName(),
                                              PsiManager.getInstance(myModule.getProject()),
                                              GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule),
                                              processor);
    return result;
  }

  private static List<PsiClass> getValidators(PsiClass constraint) {
    final PsiModifierList modifierList = constraint.getModifierList();

    if (modifierList != null) {
      final PsiAnnotation annotation = modifierList.findAnnotation(BvAnnoConstants.CONSTRAINT);
      if (annotation != null) {
        final PsiAnnotationMemberValue memberValue = annotation.findAttributeValue(BvAnnoConstants.VALIDATED_BY);
        if (memberValue instanceof PsiArrayInitializerMemberValue) {
          ArrayList<PsiClass> result = new ArrayList<PsiClass>();
          final PsiArrayInitializerMemberValue arrayValue = (PsiArrayInitializerMemberValue)memberValue;
          for (PsiAnnotationMemberValue value : arrayValue.getInitializers()) {
            final PsiClass psiClass = getPsiClass(value);
            if (psiClass != null) {
              result.add(psiClass);
            }
          }
          return result;
        }
        else if (memberValue != null) {
          final PsiClass psiClass = getPsiClass(memberValue);
          if (psiClass != null) {
            return Collections.singletonList(psiClass);
          }
        }
      }
    }
    return Collections.emptyList();
  }

  @Nullable
  public static PsiClass getPsiClass(final PsiAnnotationMemberValue psiAnnotationMemberValue) {
    PsiClass psiClass = null;
    if (psiAnnotationMemberValue instanceof PsiClassObjectAccessExpression) {
      final PsiType type = ((PsiClassObjectAccessExpression)psiAnnotationMemberValue).getOperand().getType();
      if (type instanceof PsiClassType) {
        psiClass = ((PsiClassType)type).resolve();
      }
    }
    else if (psiAnnotationMemberValue instanceof PsiExpression) {
      final Object value = JamCommonUtil.computeMemberValue(psiAnnotationMemberValue);
      if (value instanceof String) {
        String className = StringUtil.stripQuotesAroundValue(((String)value));
        psiClass =
          JavaPsiFacade.getInstance(psiAnnotationMemberValue.getProject()).findClass(className, psiAnnotationMemberValue.getResolveScope());
      }
    }
    if (psiClass != null && CommonClassNames.JAVA_LANG_OBJECT.equals(psiClass.getQualifiedName())) {
      return null;
    }
    return psiClass;
  }

}
