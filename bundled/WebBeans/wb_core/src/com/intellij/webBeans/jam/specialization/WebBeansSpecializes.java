package com.intellij.webBeans.jam.specialization;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.jam.reflect.JamMethodMeta;
import com.intellij.psi.*;
import com.intellij.psi.ref.AnnotationChildLink;
import com.intellij.psi.search.searches.SuperMethodsSearch;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.semantic.SemKey;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class WebBeansSpecializes<T extends PsiMember> implements JamElement {
  public static final SemKey<WebBeansSpecializes> SEM_KEY = SemKey.createKey("SPECIALIZES");
  public static final JamClassMeta<WebBeansSpecializes> CLASS_META = new JamClassMeta<WebBeansSpecializes>(null, ClassMapping.class, SEM_KEY);
  public static final JamMethodMeta<WebBeansSpecializes> METHOD_META = new JamMethodMeta<WebBeansSpecializes>(null, ProducerMethodMapping.class, SEM_KEY);

  private PsiRef<PsiAnnotation> myAnno;

  public WebBeansSpecializes(T psiMember) {
    myAnno = AnnotationChildLink.createRef(psiMember, WebBeansAnnoConstants.SPECIALIZES_ANNOTATION);
  }

  @Nullable
  public abstract T getSpecializedMember();

  @Nullable
  public abstract PsiClass getContainingClass();

  @NotNull
  @JamPsiConnector
  public abstract T getPsiElement();

  @Nullable
  public PsiAnnotation getAnnotation() {
    return myAnno.getPsiElement();
  }

  public static abstract class ClassMapping extends WebBeansSpecializes<PsiClass> {

    public ClassMapping(PsiClass psiClass) {
      super(psiClass);
    }

    @Nullable
    public PsiClass getSpecializedMember() {
      return getPsiElement().getSuperClass();
    }

    public PsiClass getContainingClass() {
      return getPsiElement();
    }
  }

  public static abstract class ProducerMethodMapping extends WebBeansSpecializes<PsiMethod> {

    public ProducerMethodMapping(PsiMethod method) {
      super(method);
    }

    @Nullable
    public PsiMethod getSpecializedMember() {
      final PsiClass containingClass = getPsiElement().getContainingClass();

      if (containingClass != null) {
        final PsiClass superClass = containingClass.getSuperClass();
        if (superClass != null) {
          Collection<MethodSignatureBackedByPsiMethod> methods = SuperMethodsSearch.search(getPsiElement(), superClass, true, true).findAll();

          for (MethodSignatureBackedByPsiMethod backedByPsiMethod : methods) {
            final PsiMethod overridenMethod = backedByPsiMethod.getMethod();
            if (superClass.equals(overridenMethod.getContainingClass())) {
              return overridenMethod; // overriden method of direct superclass
            }
          }
        }
      }

      return null;
    }

    public PsiClass getContainingClass() {
      return getPsiElement().getContainingClass();
    }
  }
}