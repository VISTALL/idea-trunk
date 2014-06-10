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

package com.intellij.uml.java.dependency;

import com.intellij.uml.UmlRelationshipInfo;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.uml.java.JavaUmlRelationships;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */

public class JavaClassDependencyAnalyzer {
  private final PsiClass myClass;

  public JavaClassDependencyAnalyzer(PsiClass clazz) {
    myClass = clazz;
  }

  public PsiClass getPsiClass() {
    return myClass;
  }

  private static Pair<PsiClass, UmlRelationshipInfo> createPair(PsiClass cl, UmlRelationshipInfo relationship) {
    return new Pair<PsiClass, UmlRelationshipInfo>(cl, relationship);
  }

  public List<Pair<PsiClass, UmlRelationshipInfo>> computeUsedClasses() {
    final List<Pair<PsiClass, UmlRelationshipInfo>> dependencies = new ArrayList<Pair<PsiClass, UmlRelationshipInfo>>();
    JavaRecursiveElementVisitor visitor = new JavaRecursiveElementVisitor() {
      private boolean insideField;
      private boolean insideNew;
      private boolean insideCollection;
      private boolean insideClassObjectAccess;
      private boolean insideInner;

      public void visitClass(PsiClass aClass) {
        if (insideInner || (aClass instanceof PsiAnonymousClass)) {
          super.visitClass(aClass);
        }
        else {
          insideInner = true;
          super.visitClass(aClass);
          insideInner = false;
        }
      }

      public void visitField(PsiField field) {
        if (!insideInner) {
          insideField = true;
          super.visitField(field);
          insideField = false;
        }
      }

      public void visitNewExpression(PsiNewExpression expression) {
        if (insideInner) return;
        insideNew = true;
        PsiType type = expression.getType();
        if (type != null) {
          PsiClass psiClass = findConcreteClass(typeToClass(type));
          if (psiClass != null) {
            dependencies.add(createPair(psiClass, JavaUmlRelationships.CREATE));
          }
        }
        super.visitNewExpression(expression);
        insideNew = false;
      }

      public void visitClassObjectAccessExpression(PsiClassObjectAccessExpression expression) {
        if (!insideInner) {
          insideClassObjectAccess = true;
          super.visitClassObjectAccessExpression(expression);
          insideClassObjectAccess = false;
        }
      }

      public void visitTypeElement(PsiTypeElement typeElement) {
        if (insideInner) return;
        PsiType type = typeElement.getType();
        PsiClass psiClass = typeToClass(type);
        if (psiClass != null && !(psiClass instanceof PsiTypeParameter)) {
          if (insideClassObjectAccess) {
            dependencies.add(createPair(psiClass, JavaUmlRelationships.DEPENDENCY));
          }
          else if (insideField && !insideNew && !insideCollection && !(type instanceof PsiArrayType)) {
            dependencies.add(createPair(psiClass, JavaUmlRelationships.TO_ONE));
          }
          else if (insideField && !insideNew && insideCollection && !(type instanceof PsiArrayType)) {
            dependencies.add(createPair(psiClass, JavaUmlRelationships.TO_MANY));
          }
          else {
            dependencies.add(createPair(psiClass, JavaUmlRelationships.DEPENDENCY));
          }
        }
        if (insideField && !insideNew && !insideCollection && isCollection(typeElement)) {
          insideCollection = true;
          super.visitTypeElement(typeElement);
          insideCollection = false;
        }
        else {
          super.visitTypeElement(typeElement);
        }
      }

      public void visitReferenceExpression(PsiReferenceExpression referenceExpression) {
        if (insideInner) return;
        PsiExpression qualifierExpression = referenceExpression.getQualifierExpression();
        if (qualifierExpression != null) {
          PsiReference psiReference = qualifierExpression.getReference();
          if (psiReference != null) {
            PsiElement reference = psiReference.resolve();
            if (reference instanceof PsiClass) {
              dependencies.add(createPair(((PsiClass)reference), JavaUmlRelationships.DEPENDENCY));
            }
          }
        }
        super.visitReferenceExpression(referenceExpression);
      }
    };
    visitor.visitElement(myClass);
    return dependencies;
  }

  public List<Pair<PsiClass, UmlRelationshipInfo>> computeUsingClasses() {
    final List<Pair<PsiClass, UmlRelationshipInfo>> dependencies = new ArrayList<Pair<PsiClass, UmlRelationshipInfo>>();
    Query<PsiReference> query = ReferencesSearch.search(myClass);
    Collection<PsiReference> references = query.findAll();
    for (PsiReference reference : references) {
      PsiElement referencingElement = reference.getElement();
      PsiClass referencingClass = PsiTreeUtil.getParentOfType(referencingElement, PsiClass.class);
      referencingClass = findConcreteClass(referencingClass);
      if (referencingClass != null) {
        PsiElement parentElement = referencingElement.getParent();
        if (parentElement != null) {
          if (parentElement instanceof PsiTypeElement) {
            PsiTypeElement typeElement = (PsiTypeElement)parentElement;
            PsiClassObjectAccessExpression classObject = PsiTreeUtil.getParentOfType(typeElement, PsiClassObjectAccessExpression.class);
            if (classObject != null) {
              dependencies.add(createPair(referencingClass, JavaUmlRelationships.DEPENDENCY));
            }
            else {
              PsiField field = PsiTreeUtil.getParentOfType(typeElement, PsiField.class);
              if (field != null) {
                boolean collection = false;
                for (parentElement = PsiTreeUtil.getParentOfType(parentElement, PsiTypeElement.class);
                     parentElement != null && (parentElement instanceof PsiTypeElement) && !collection;
                     parentElement = PsiTreeUtil.getParentOfType(parentElement, PsiTypeElement.class)) {
                  if (isCollection((PsiTypeElement)parentElement)) collection = true;
                }
                dependencies.add(createPair(referencingClass, collection ? JavaUmlRelationships.TO_MANY : JavaUmlRelationships.TO_ONE));
              }
              else {
                dependencies.add(createPair(referencingClass, JavaUmlRelationships.DEPENDENCY));
              }
            }
          }
          else if (parentElement instanceof PsiReferenceList) {
            //PsiReferenceList referenceList = (PsiReferenceList)parentElement;
            //PsiElement firstChild = referenceList.getFirstChild();
            //if (firstChild != null) {
            //  if (firstChild.getText().equals("implements")) {
            //    dependencies.add(createPair(referencingClass, UmlRelationship.REALIZATION));
            //  }
            //  if (firstChild.getText().equals("extends")) {
            //    dependencies.add(createPair(referencingClass, UmlRelationship.GENERALIZATION));
            //  }
            //}
          }
          else if (parentElement instanceof PsiNewExpression) {
            dependencies.add(createPair(referencingClass, JavaUmlRelationships.CREATE));
          }
          else if (parentElement instanceof PsiReferenceExpression) {
            dependencies.add(createPair(referencingClass, JavaUmlRelationships.DEPENDENCY));
          }
        }
      }
    }
    return dependencies;
  }

  @Nullable
  private static PsiClass typeToClass(@NotNull PsiType psiType) {
    if (psiType instanceof PsiClassType) {
      PsiClass psiClass = ((PsiClassType)psiType).resolve();
      if (psiClass != null) return psiClass;
    }
    else if (psiType instanceof PsiArrayType) {
      PsiType deepComponentType = ((PsiArrayType)psiType).getComponentType().getDeepComponentType();
      if (deepComponentType instanceof PsiClassType) {
        PsiClass psiClass = ((PsiClassType)deepComponentType).resolve();
        if (psiClass != null) return psiClass;
      }
    }
    return null;
  }

  private static boolean isCollection(@NotNull PsiTypeElement type) {
    if (type.getType() instanceof PsiArrayType) return true;
    PsiClass aClass = typeToClass(type.getType());
    if (aClass == null) return false;
    List<PsiClass> classes = getAllInterfaces(aClass);
    for (PsiClass cl : classes) {
      if (COLLECTIONS.contains(cl.getQualifiedName())) return true;
    }
    return false;
  }

  private static final List<String> COLLECTIONS = new ArrayList<String>();

  static {
    COLLECTIONS.add("java.lang.Iterable");
    COLLECTIONS.add("java.util.Map");
  }

  private static List<PsiClass> getAllInterfaces(PsiClass psiClass) {
    List<PsiClass> interfaces = new ArrayList<PsiClass>();
    PsiClass[] inters = psiClass.getInterfaces();
    for (PsiClass inter : inters) {
      interfaces.add(inter);
      interfaces.addAll(getAllInterfaces(inter));
    }

    return interfaces;
  }

  @Nullable
  private static PsiClass findConcreteClass(PsiClass psiClass) {
    while (psiClass != null && (psiClass instanceof PsiAnonymousClass)) {
      psiClass = PsiUtil.resolveClassInType(((PsiAnonymousClass)psiClass).getBaseClassType());
    }
    return psiClass;
  }

}

