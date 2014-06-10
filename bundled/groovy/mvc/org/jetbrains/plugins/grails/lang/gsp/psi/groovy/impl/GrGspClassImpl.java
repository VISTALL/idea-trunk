/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrGspClass;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrGspDeclarationHolder;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrGspRunMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiElementImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.PsiImplUtil;
import org.jetbrains.plugins.groovy.lang.resolve.CollectClassMembersUtil;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;

import java.util.*;

/**
 * @author ilyas
 */
public class GrGspClassImpl extends GroovyPsiElementImpl implements GrGspClass {

  private final String GSPCLASS_SYNTHETIC_NAME = "GspClass";

  public GrGspClassImpl(ASTNode node) {
    super(node);
  }

  public String toString() {
    return GSPCLASS_SYNTHETIC_NAME;
  }

  @Nullable
  @NonNls
  public String getQualifiedName() {
    return null;
  }

  public boolean isInterface() {
    return false;
  }

  public boolean isAnnotationType() {
    return false;
  }

  public boolean isEnum() {
    return false;
  }

  @Nullable
  public PsiReferenceList getExtendsList() {
    return null;
  }

  @Nullable
  public PsiReferenceList getImplementsList() {
    return null;
  }

  @NotNull
  public PsiClassType[] getExtendsListTypes() {
    //todo implement me
    return new PsiClassType[0];
  }

  @NotNull
  public PsiClassType[] getImplementsListTypes() {
    return PsiClassType.EMPTY_ARRAY;
  }

  @Nullable
  public PsiClass getSuperClass() {
    //todo make field
    String GROOVY_PAGE_BASE_CLASS_NAME = "org.codehaus.groovy.grails.web.pages.GroovyPage";
    JavaPsiFacade manager = JavaPsiFacade.getInstance(getProject());
    return manager.findClass(GROOVY_PAGE_BASE_CLASS_NAME, getResolveScope());
  }

  public PsiClass[] getInterfaces() {
    return PsiClass.EMPTY_ARRAY;
  }

  @NotNull
  public PsiClass[] getSupers() {
    final PsiClass superClass = getSuperClass();
    return superClass != null ? new PsiClass[]{superClass} : PsiClass.EMPTY_ARRAY;
  }

  @NotNull
  public PsiClassType[] getSuperTypes() {
    return new PsiClassType[]{JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeByFQClassName("org.codehaus.groovy.grails.web.pages.GroovyPage", getResolveScope())};
  }

  @NotNull
  public GrField[] getFields() {
    GrGspDeclarationHolder[] holders = getRunMethod().getRunBlock().getDeclarationHolders();
    ArrayList<GrField> fields = new ArrayList<GrField>();
    for (GrGspDeclarationHolder holder : holders) {
      fields.addAll(Arrays.asList(holder.getFields()));
    }
    return fields.toArray(new GrField[fields.size()]);
  }

  @NotNull
  public GrMethod[] getMethods() {
    GrGspDeclarationHolder[] holders = getRunMethod().getRunBlock().getDeclarationHolders();
    ArrayList<GrMethod> methods = new ArrayList<GrMethod>();
    for (GrGspDeclarationHolder holder : holders) {
      methods.addAll(Arrays.asList(holder.getMethods()));
    }
    return methods.toArray(new GrMethod[methods.size()]);
  }

  @NotNull
  public PsiMethod[] getConstructors() {
    return PsiMethod.EMPTY_ARRAY;
  }

  @NotNull
  public PsiClass[] getInnerClasses() {
    return PsiClass.EMPTY_ARRAY;
  }

  @NotNull
  public PsiClassInitializer[] getInitializers() {
    return PsiClassInitializer.EMPTY_ARRAY;
  }

  @NotNull
  public PsiField[] getAllFields() {
    return PsiField.EMPTY_ARRAY;
  }

  @NotNull
  public PsiMethod[] getAllMethods() {
    return PsiMethod.EMPTY_ARRAY;
  }

  @NotNull
  public PsiClass[] getAllInnerClasses() {
    return PsiClass.EMPTY_ARRAY;
  }

  @Nullable
  public PsiField findFieldByName(String name, boolean checkBases) {
    if (!checkBases) {
      for (GrField field : getFields()) {
        if (name.equals(field.getName())) return field;
      }
      return null;
    }
    Map<String, CandidateInfo> fieldsMap = CollectClassMembersUtil.getAllFields(this);
    final CandidateInfo info = fieldsMap.get(name);
    return info == null ? null : (PsiField) info.getElement();
  }

  @Nullable
  public PsiMethod findMethodBySignature(PsiMethod patternMethod, boolean checkBases) {
    return null;
  }

  @NotNull
  public PsiMethod[] findMethodsBySignature(PsiMethod patternMethod, boolean checkBases) {
    return PsiMethod.EMPTY_ARRAY;
  }

  @NotNull
  public PsiMethod[] findMethodsByName(@NonNls String name, boolean checkBases) {
    if (!checkBases) {
      List<PsiMethod> result = new ArrayList<PsiMethod>();
      for (GrMethod method : getMethods()) {
        if (name.equals(method.getName())) result.add(method);
      }

      return result.toArray(new PsiMethod[result.size()]);
    }

    Map<String, List<CandidateInfo>> methodsMap = CollectClassMembersUtil.getAllMethods(this, true);
    return PsiImplUtil.mapToMethods(methodsMap.get(name));
  }

  @NotNull
  public List<Pair<PsiMethod, PsiSubstitutor>> findMethodsAndTheirSubstitutorsByName(String name, boolean checkBases) {
    return Collections.emptyList();
  }

  @NotNull
  public List<Pair<PsiMethod, PsiSubstitutor>> getAllMethodsAndTheirSubstitutors() {
    return Collections.emptyList();
  }

  @Nullable
  public PsiClass findInnerClassByName(String name, boolean checkBases) {
    return null;
  }

  @Nullable
  public PsiJavaToken getLBrace() {
    return null;
  }

  @Nullable
  public PsiJavaToken getRBrace() {
    return null;
  }

  @Nullable
  public PsiIdentifier getNameIdentifier() {
    return null;
  }

  public PsiElement getScope() {
    return null;
  }

  public boolean isInheritor(@NotNull PsiClass baseClass, boolean checkDeep) {
    return false;
  }

  public boolean isInheritorDeep(PsiClass baseClass, @Nullable PsiClass classToByPass) {
    return false;
  }

  @Nullable
  public PsiClass getContainingClass() {
    return null;
  }

  @NotNull
  public Collection<HierarchicalMethodSignature> getVisibleSignatures() {
    return Collections.emptyList();
  }

  @NotNull
  public GrGspRunMethod getRunMethod() {
    GrGspRunMethod method = findChildByClass(GrGspRunMethod.class);
    assert method != null;
    return method;
  }

  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    throw new IncorrectOperationException("There is no way to set up gsp class name");
  }

  @Nullable
  public PsiModifierList getModifierList() {
    return null;
  }

  public boolean hasModifierProperty(@NonNls @NotNull String name) {
    return false;
  }

  @Nullable
  public PsiDocComment getDocComment() {
    return null;
  }

  public boolean isDeprecated() {
    return false;
  }

  @Nullable
  public PsiMetaData getMetaData() {
    return null;
  }

  public boolean isMetaEnough() {
    return false;
  }

  public boolean hasTypeParameters() {
    return false;
  }

  @Nullable
  public PsiTypeParameterList getTypeParameterList() {

    return null;
  }

  @NotNull
  public PsiTypeParameter[] getTypeParameters() {
    return new PsiTypeParameter[0];
  }

  public String getName() {
    return GSPCLASS_SYNTHETIC_NAME;
  }

  public boolean processDeclarations(@NotNull final PsiScopeProcessor processor, @NotNull final PsiSubstitutor substitutor, final PsiElement lastParent, @NotNull final PsiElement place) {

    final GrGspRunMethod runMethod = getRunMethod();
    for (PsiMethod method : getMethods()) {
      if (method == runMethod) continue;
      if (!ResolveUtil.processElement(processor, method)) return false;
    }

    for (GrField field : getFields()) {
      if (!ResolveUtil.processElement(processor, field)) return false;
    }

    // todo implement processdeclarations for included files
    return true;
  }


}
