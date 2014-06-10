package com.intellij.seam.model.jam.bijection;

import com.intellij.jam.JamElement;
import com.intellij.jam.JamPomTarget;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamMemberMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.PomTarget;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.seam.model.SeamComponentScope;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Serega.Vasiliev
 */
public abstract class SeamJamBijection<T extends PsiMember & PsiNamedElement> extends CommonModelElement.PsiBase implements JamElement,
                                                                                                                            CommonModelElement {
  public final JamMemberMeta<PsiMember, SeamJamBijection> COMMON_META =
    new JamMemberMeta<PsiMember, SeamJamBijection>(null, SeamJamBijection.class)
      .addPomTargetProducer(new PairConsumer<SeamJamBijection, Consumer<PomTarget>>() {
        public void consume(SeamJamBijection bijection, Consumer<PomTarget> consumer) {
          consumer.consume(bijection.getPsiTarget());
        }
      });


  protected final T myMember;

  protected SeamJamBijection(T member) {
    myMember = member;
  }

  public static final JamStringAttributeMeta.Single<String> NAME_META = JamAttributeMeta.singleString("value");

  @Nullable
  public String getDefaultName() {
    return PropertyUtil.getPropertyName(myMember);
  }

  @Nullable
  public abstract PsiType getType();

  @NotNull
  protected abstract JamAnnotationMeta getJamAnnotationMeta();

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
  private JamStringAttributeElement<String> getNamedStringAttributeElement() {
    return getJamAnnotationMeta().getAttribute(getPsiElement(), NAME_META);
  }

  public boolean isRequred() {
    PsiAnnotation annotation = getIdentifyingAnnotation();
    if (annotation == null) return false;

    Boolean value = AnnotationModelUtil.getObjectValue(annotation.findDeclaredAttributeValue("required"), Boolean.class);

    return value != null && value;
  }

  @Nullable
  public SeamComponentScope getScope() {
    return AnnotationModelUtil.getEnumValue(getIdentifyingAnnotation(), "scope", SeamComponentScope.class).getValue();
  }

  @Nullable
  public PsiAnnotation getIdentifyingAnnotation() {
    return getJamAnnotationMeta().getAnnotation(getPsiElement());
  }

  private PomTarget getPsiTarget() {
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
        return SeamJamBijection.this.getNameAlias(delegatePsiTargetName);
      }
    };
  }

  public abstract String getNameAlias(@Nullable String delegatePsiTargetName);

}
