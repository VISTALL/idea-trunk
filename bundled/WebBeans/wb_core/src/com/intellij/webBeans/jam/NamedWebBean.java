package com.intellij.webBeans.jam;

import com.intellij.jam.JamElement;
import com.intellij.jam.JamPomTarget;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.PomTarget;
import com.intellij.pom.references.PomService;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NamedWebBean<T extends PsiMember & PsiNamedElement> implements JamElement {

  private static final JamStringAttributeMeta.Single<String> NAME_VALUE_META = JamAttributeMeta.singleString("value");

  private final JamAnnotationMeta myMeta;

  protected final T myMember;

  private boolean isStereotype;

  public NamedWebBean(T psiMember, final String annoName) {
    myMember = psiMember;
    myMeta = new JamAnnotationMeta(annoName);
    isStereotype = !WebBeansAnnoConstants.NAMED_ANNOTATION.equals(annoName);
  }

  public boolean isStereotypeAnnotated() {
    return isStereotype;
  }

  @NotNull
  public String getName() {
    if (isStereotypeAnnotated()) return getDefaultName();

    String nameValue = getNamedStringAttributeElement().getStringValue();

    return StringUtil.isEmptyOrSpaces(nameValue) ? getDefaultName() : nameValue;
  }

  @NotNull
  private JamStringAttributeElement<String> getNamedStringAttributeElement() {
    return myMeta.getAttribute(myMember, NAME_VALUE_META);
  }

  public abstract String getDefaultName();

  @Nullable
  public abstract PsiType getType();

  public PsiNamedElement getIdentifyingPsiElement() {
    return (PsiNamedElement)PomService.convertToPsi(myMember.getProject(), getPomTarget());
  }

  public PomTarget getPomTarget() {
    if (isStereotypeAnnotated()) {
      return getDefaultTarget(true);
    }

    final JamStringAttributeElement<String> namedAttributeValue = getNamedStringAttributeElement();
    if (StringUtil.isEmptyOrSpaces(namedAttributeValue.getStringValue())) {
      return getDefaultTarget(false);
    }

    return new JamPomTarget(this, namedAttributeValue);
  }

  private PomTarget getDefaultTarget(final boolean stereotype) {
    return new RenameableDelegatePsiTarget(myMember) {
      @Override
      public RenameableDelegatePsiTarget setName(@NotNull String newName) {
        if (stereotype) {
          //todo should rename class or method
          return this;
        }
        getNamedStringAttributeElement().setStringValue(newName);

        return this;
      }

      @NotNull
      @Override
      public String getName() {
        return getDefaultName();
      }
    };
  }

  @NotNull
  public T getPsiElement() {
    return myMember;
  }

  public static class ClassMapping extends NamedWebBean<PsiClass> {

    public ClassMapping(PsiClass psiClass, final String annoName) {
      super(psiClass, annoName);
    }

    public String getDefaultName() {
      return StringUtil.decapitalize(getPsiElement().getName());
    }

    @NotNull
    public PsiType getType() {
      final PsiClass psiClass = getPsiElement();
      return JavaPsiFacade.getElementFactory(psiClass.getProject()).createType(psiClass);
    }
  }

  public static class ProducerMethodMapping extends NamedWebBean<PsiMethod> {

    public ProducerMethodMapping(PsiMethod method, final String annoName) {
      super(method, annoName);
    }

    public String getDefaultName() {
      String propertyName = PropertyUtil.getPropertyName(getPsiElement());
      return propertyName == null ? getPsiElement().getName() : propertyName;
    }

    @Nullable
    public PsiType getType() {
      return getPsiElement().getReturnType();
    }
  }

  public static class FieldMapping extends NamedWebBean<PsiField> {

    public FieldMapping(PsiField psiField, final String annoName) {
      super(psiField, annoName);
    }

    public String getDefaultName() {
      return getPsiElement().getName();
    }

    @Nullable
    public PsiType getType() {
      return getPsiElement().getType();
    }
  }
}

