package com.intellij.seam.model.jam.dataModel;

import com.intellij.jam.JamElement;
import com.intellij.jam.JamPomTarget;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.reflect.*;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.PomTarget;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.seam.constants.SeamAnnotationConstants;
import com.intellij.seam.model.SeamComponentScope;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SeamJamDataModel<T extends PsiMember & PsiNamedElement> extends CommonModelElement.PsiBase implements JamElement,
                                                                                                                            CommonModelElement {
  public static final JamAnnotationMeta ANNOTATION_META = new JamAnnotationMeta(SeamAnnotationConstants.JSF_DATA_MODEL_ANNOTATION);

  public final T myMember;

  public SeamJamDataModel(T member) {
    myMember = member;
  }

  public static final JamStringAttributeMeta.Single<String> NAME_META = JamAttributeMeta.singleString("value");

  public static class Method extends SeamJamDataModel<PsiMethod> {
    public static final JamMemberMeta<PsiMethod, Method> META = new JamMemberMeta<PsiMethod, Method>(null, Method.class).addPomTargetProducer(new PairConsumer<Method, Consumer<PomTarget>>() {
      public void consume(Method dataModel, Consumer<PomTarget> consumer) {
        consumer.consume(dataModel.getPsiTarget());
      }
    });
    static {
      META.addAnnotation(ANNOTATION_META);
    }

    public Method(PsiMethod member) {
      super(member);
    }

    @Override
    public PsiType getType() {
      return getPsiElement().getReturnType();
    }

    @Override
    public String getNameAlias(@Nullable String delegatePsiTargetName) {
      return PropertyUtil.getPropertyName(delegatePsiTargetName);
    }
  }

  public static class Field extends SeamJamDataModel<PsiField> {
    public static final JamMemberMeta<PsiField, Field> META = new JamFieldMeta<Field>(Field.class).addPomTargetProducer(new PairConsumer<Field, Consumer<PomTarget>>() {
      public void consume(Field dataModel, Consumer<PomTarget> consumer) {
        consumer.consume(dataModel.getPsiTarget());
      }
    });

    static {
      META.addAnnotation(ANNOTATION_META);
    }

    public Field(PsiField member) {
      super(member);
    }

    @Override
    public PsiType getType() {
      return getPsiElement().getType();
    }

    @Override
    public String getNameAlias(@Nullable String delegatePsiTargetName) {
      return delegatePsiTargetName;
    }
  }

  @Nullable
  public String getDefaultName() {
    return PropertyUtil.getPropertyName(myMember);
  }

  @Nullable
  public abstract PsiType getType();

  @NotNull
  public T getPsiElement() {
    return myMember;
  }

  @Nullable
  public String getName() {
    String nameValue = getNamedStringAttributeElement().getStringValue();

    return StringUtil.isEmptyOrSpaces(nameValue) ? getDefaultName() : nameValue;
  }

  @NotNull
  public JamStringAttributeElement<String> getNamedStringAttributeElement() {
    return ANNOTATION_META.getAttribute(getPsiElement(), NAME_META);
  }

  @Nullable
  public SeamComponentScope getScope() {
    return AnnotationModelUtil.getEnumValue(getIdentifyingAnnotation(), "scope", SeamComponentScope.class).getValue();
  }

  @Nullable
  public PsiAnnotation getIdentifyingAnnotation() {
    return ANNOTATION_META.getAnnotation(getPsiElement());
  }

  public PomTarget getPsiTarget() {
    final JamStringAttributeElement<String> namedAttributeValue = getNamedStringAttributeElement();
    if (StringUtil.isEmptyOrSpaces(namedAttributeValue.getStringValue())) {
      return getAliasingPsiTarget();
    }

    return new JamPomTarget(this, namedAttributeValue);
  }

  public PsiTarget getAliasingPsiTarget() {
    return new AliasingPsiTarget(getPsiElement()) {

      @Override
      public String getNameAlias(@Nullable String delegatePsiTargetName) {
        return SeamJamDataModel.this.getNameAlias(delegatePsiTargetName);
      }
    };
  }

  public abstract String getNameAlias(@Nullable String delegatePsiTargetName);
}