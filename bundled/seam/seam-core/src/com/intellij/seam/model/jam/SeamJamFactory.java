package com.intellij.seam.model.jam;

import com.intellij.jam.JamElement;
import com.intellij.jam.JamPomTarget;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.*;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.seam.constants.SeamAnnotationConstants;
import com.intellij.seam.model.CommonSeamComponent;
import com.intellij.seam.model.CommonSeamFactoryComponent;
import com.intellij.seam.model.SeamComponentScope;
import com.intellij.pom.PomTarget;
import com.intellij.pom.references.PomService;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SeamJamFactory extends CommonModelElement.PsiBase implements JamElement, CommonSeamComponent, CommonSeamFactoryComponent {

  public static final JamStringAttributeMeta.Single<String> NAME_VALUE_META = JamAttributeMeta.singleString("value");

  public static final JamAnnotationMeta ANNOTATION_META = new JamAnnotationMeta(SeamAnnotationConstants.FACTORY_ANNOTATION).addAttribute(NAME_VALUE_META);

  public static final JamMemberMeta<PsiMethod,SeamJamFactory> META = new JamMethodMeta<SeamJamFactory>(SeamJamFactory.class).addPomTargetProducer(new PairConsumer<SeamJamFactory, Consumer<PomTarget>>() {
    public void consume(SeamJamFactory seamJamFactory, Consumer<PomTarget> consumer) {
      consumer.consume(seamJamFactory.getPsiTarget());
    }
  }).addAnnotation(ANNOTATION_META);


  protected JamStringAttributeElement<String> getNamedStringAttributeElement() {
    return ANNOTATION_META.getAttribute(getPsiElement(), NAME_VALUE_META);
  }

  public PsiTarget getPsiTarget() {
    final JamStringAttributeElement<String> namedAttributeValue = getNamedStringAttributeElement();
    if (StringUtil.isEmptyOrSpaces(namedAttributeValue.getStringValue())) {
      return getAliasingPsiTarget();
    }

    return new JamPomTarget(this, namedAttributeValue);
  }

  private PsiTarget getAliasingPsiTarget() {
    return new AliasingPsiTarget(getPsiElement()) {

      @Override
      public String getNameAlias(@Nullable String delegatePsiTargetName) {
        return PropertyUtil.getPropertyName(delegatePsiTargetName);
      }
    };
  }

  public PsiElement getIdentifyingPsiElement() {
    return PomService.convertToPsi(getPsiElement().getProject(), getPsiTarget());
  }

  @NotNull
  @JamPsiConnector
  public abstract PsiMethod getPsiElement();

  @Nullable
  public String getFactoryName() {
    String nameValue = getNamedStringAttributeElement().getStringValue();

    return StringUtil.isEmptyOrSpaces(nameValue) ? PropertyUtil.getPropertyName(getPsiElement()) : nameValue;
  }

  public PsiAnnotation getIdentifyingAnnotation() {
    return ANNOTATION_META.getAnnotation(getPsiElement());
  }

  @Nullable
  private String getVoidMethodFactoryName() {
    PsiMethod method = getPsiElement();

    if (hasGetterName(method)) return PropertyUtil.getPropertyNameByGetter(method);

    return null;
  }


  private static boolean hasGetterName(@NotNull final PsiMethod method) {
    if (method.isConstructor()) return false;

    String methodName = method.getName();

    final String[] prefixes = {"get", "is"};
    for (String prefix : prefixes) {
      if (methodName.startsWith(prefix) && methodName.length() > prefix.length()) {
        if (Character.isLowerCase(methodName.charAt(prefix.length())) &&
            (methodName.length() == prefix.length() + 1 || Character.isLowerCase(methodName.charAt(prefix.length() + 1)))) {
          return false;
        }
      }
    }

    return true;
  }

  @Nullable
  public SeamComponentScope getFactoryScope() {
    return AnnotationModelUtil.getEnumValue(getIdentifyingAnnotation(), "scope", SeamComponentScope.class).getValue();
  }

  @Nullable
  public PsiType getFactoryType() {
    PsiMethod method = getPsiElement();

    return method.getReturnType();
  }
}