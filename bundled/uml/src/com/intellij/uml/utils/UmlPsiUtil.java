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

package com.intellij.uml.utils;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.actions.ReformatAndOptimizeImportsProcessor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Konstantin Bulenkov
 */
public class UmlPsiUtil {
  private UmlPsiUtil() {
  }

  public static void runWriteActionInCommandProcessor(final Runnable run) {
    CommandProcessor.getInstance().runUndoTransparentAction(new Runnable(){
      public void run() {
        ApplicationManager.getApplication().runWriteAction(run);
      }
    });
  }

  public static void reformat(final PsiClass psiClass) {
    final Runnable reformat = new Runnable() {
      public void run() {
        new ReformatAndOptimizeImportsProcessor(psiClass.getProject(), psiClass.getContainingFile()).run();
      }
    };
    runWriteActionInCommandProcessor(reformat);
  }

  @Nullable
  public static String createInheritanceBetween(final PsiClass child, final PsiClass parent) throws IncorrectOperationException {
    if (child.isInheritor(parent, true) || parent.isInheritor(child, true)) {
      return UmlBundle.message("relationship.already.exists", child.getName(), parent.getName());
    }
    if (child.equals(parent)) return null;
    final PsiModifierList modifiers = parent.getModifierList();
    if (modifiers == null) return null;
    if (child.isAnnotationType() && !parent.isAnnotationType()) {
      return UmlBundle.message("annotation.class.cant.be.extended.or.implemented");
    }    
    if (modifiers.hasModifierProperty(PsiModifier.FINAL)) {
      return UmlBundle.message("final.class.cant.be.inherited", parent.getName());
    }

    final JavaPsiFacade facade = JavaPsiFacade.getInstance(child.getProject());
    final PsiElementFactory factory = facade.getElementFactory();
    if (parent.isInterface()) {
      final PsiJavaCodeReferenceElement ref = factory.createClassReferenceElement(parent);
      final PsiReferenceList implementsList = child.isInterface() ? child.getExtendsList() : child.getImplementsList();
      if (implementsList != null) {
        implementsList.add(ref);
      }
    } else {
      if (child.isInterface()) {
        return UmlBundle.message("node.is.interface", child.getName());
      }
      final PsiClass superClass = child.getSuperClass();
      if (superClass != null) {
        final PsiJavaCodeReferenceElement ref = factory.createClassReferenceElement(parent);
        final PsiReferenceList extendsList = child.getExtendsList();
        if (extendsList != null) {
          if (extendsList.getReferenceElements().length > 0) {
            extendsList.getReferenceElements()[0].replace(ref);
          } else {
            extendsList.add(ref);
          }
        }
      } else {
        //if (superClass != null) {
        //  return UmlBundle.message("already.extends", child.getName(), superClass.getQualifiedName());
        //}
      }
    }
    return null;
  }

  public static void makeClassAbstract(final PsiClass child) {
    runWriteActionInCommandProcessor(new Runnable(){
      public void run() {
        try {
          final PsiModifierList modifierList = child.getModifierList();
          if (modifierList != null) {
            modifierList.setModifierProperty(PsiModifier.ABSTRACT, true);
          }
        } catch (IncorrectOperationException e) {//
        }
      }
    });
  }

  public static boolean isAbstract(final PsiClass child) {
    final PsiModifierList modifierList = child.getModifierList();
    return modifierList != null && modifierList.hasModifierProperty(PsiModifier.ABSTRACT);
  }

  @Nullable
  public static String annotateClass(PsiClass psiClass, PsiClass anno) {
    if (psiClass == null || anno == null) return null;
    assert anno.isAnnotationType();
    final PsiModifierList modifierList = psiClass.getModifierList();
    if (modifierList == null) return null;

    final String fqn = anno.getQualifiedName();
    if (fqn == null || modifierList.findAnnotation(fqn) != null) return null;
    return addAnnotation(psiClass, fqn) ? null : "Can't annotate class " + psiClass.getQualifiedName();
  }

  @Nullable
  public static PsiClass findAnnotationClass(PsiAnnotation annotation) {
    if (annotation == null) return null;

    final PsiJavaCodeReferenceElement ref = annotation.getNameReferenceElement();
    if (ref == null) return null;
    final PsiElement refElement = ref.getElement();
    if (refElement instanceof PsiJavaCodeReferenceElement) {
      final PsiElement psiClass = ((PsiJavaCodeReferenceElement)refElement).resolve();
      if (psiClass instanceof PsiClass) {
        return (PsiClass)psiClass;
      }
    }

    return null;
  }


  public static Set<PsiClass> findAnnotationsForClass(final PsiClass psiClass) {
    final Set<PsiClass> classes = new HashSet<PsiClass>();
    if (psiClass == null) return classes;

    final PsiModifierList modifierList = psiClass.getModifierList();
    if (modifierList == null) return classes;

    for (PsiAnnotation annotation : modifierList.getAnnotations()) {
      final PsiClass aClass = findAnnotationClass(annotation);
      if (aClass != null) classes.add(aClass);
    }
    return classes;
  }

  /**
   * Annotates element by annotation
   * @param psiElement element for annotation
   * @param fqn annotation's fqn
   * @return <code>true</code> if element has been annotated successfully
   */
  public static boolean addAnnotation(final PsiClass psiElement, final String fqn) {
    final boolean[] result = {false};
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        try {
          PsiModifierList modifierList = psiElement.getModifierList();
          if (modifierList == null) return;

          if (!CodeInsightUtilBase.prepareFileForWrite(psiElement.getContainingFile())) return;

          final Project project = psiElement.getProject();
          final PsiAnnotation psiAnnotation = JavaPsiFacade.getInstance(project)
            .getElementFactory().createAnnotationFromText("@"+fqn, psiElement);

          final PsiElement element = modifierList.getFirstChild();

          if (element != null) {
            modifierList.addBefore(psiAnnotation, element);
          } else {
            modifierList.add(psiAnnotation);
          }

          JavaCodeStyleManager.getInstance(project).shortenClassReferences(modifierList);
          result[0] = true;
        } catch (IncorrectOperationException e) {//
        }
      }
    });
    return result[0];
  }

  @Nullable
  public static PsiClass[] removeAnonymous(@Nullable final PsiElement[] psiElements) {
    if (psiElements == null) return null;
    List<PsiClass> classes = new ArrayList<PsiClass>();
    for (PsiElement element : psiElements) {
      if (element instanceof PsiClass && !(element instanceof PsiAnonymousClass)) {
        classes.add((PsiClass)element);
      }
    }
    return classes.toArray(new PsiClass[classes.size()]);
  }

  public static PsiClass[] removeExisten(@Nullable final PsiClass[] classes, @Nullable final List<PsiClass> existen) {
    final List<PsiClass> list = new ArrayList<PsiClass>(Arrays.asList(classes));
    list.removeAll(existen);
    return list.toArray(new PsiClass[list.size()]);
  }

  public static Collection<PsiClass> getAllInnerClasses(PsiClass psiClass) {
    Set<PsiClass> inners = new HashSet<PsiClass>();
    for (PsiClass inner : psiClass.getInnerClasses()) {
      if (inner.getName() != null) {
        inners.add(inner);
        inners.addAll(getAllInnerClasses(inner));
      }
    }
    return inners;
  }
}
