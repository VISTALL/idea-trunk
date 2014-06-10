package com.intellij.seam.model.jam;

import com.intellij.jam.JamElement;
import com.intellij.jam.JamPomTarget;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.pom.PomTarget;
import com.intellij.pom.references.PomService;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.seam.constants.SeamAnnotationConstants;
import com.intellij.seam.model.SeamComponentScope;
import com.intellij.seam.utils.SeamCommonUtils;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Sergey.Vasiliev
 */
public abstract class SeamJamRole extends CommonModelElement.PsiBase implements JamElement, CommonModelElement {
  @NonNls public static final String NAME_ATTRIBUTE = "name";
  @NonNls public static final String SCOPE_ATTRIBUTE = "scope";
  public static JamStringAttributeMeta.Single<String> NAME_ATTRIBUTE_META = JamAttributeMeta.singleString(SeamJamRole.NAME_ATTRIBUTE);

  public static final JamAnnotationMeta ANNOTATION_META =
    new JamAnnotationMeta(SeamAnnotationConstants.ROLE_ANNOTATION).addAttribute(NAME_ATTRIBUTE_META);

  public static final JamClassMeta<SeamJamRole> META = new JamClassMeta<SeamJamRole>(SeamJamRole.class).addAnnotation(ANNOTATION_META).
    addPomTargetProducer(new PairConsumer<SeamJamRole, Consumer<PomTarget>>() {
      public void consume(SeamJamRole seamJamRole, Consumer<PomTarget> consumer) {
        consumer.consume(seamJamRole.getPsiTarget());
      }
    }

    );

  private PsiMember myPsiMember;
  private PsiRef<PsiAnnotation> myPsiAnnotation;

  public SeamJamRole(final PsiMember psiMember) {
    myPsiMember = psiMember;
    ANNOTATION_META.getAnnotationRef(psiMember);
  }

  public SeamJamRole(final PsiAnnotation psiAnnotation) {
    myPsiAnnotation = PsiRef.real(psiAnnotation);
    myPsiMember = PsiTreeUtil.getParentOfType(psiAnnotation, PsiMember.class, true);
  }

  protected JamStringAttributeElement<String> getNamedStringAttributeElement() {
    return NAME_ATTRIBUTE_META.getJam(myPsiAnnotation);
  }

  public PsiTarget getPsiTarget() {
    final JamStringAttributeElement<String> namedAttributeValue = getNamedStringAttributeElement();
    return new JamPomTarget(this, namedAttributeValue);
  }

  public PsiElement getIdentifyingPsiElement() {
    return PomService.convertToPsi(myPsiMember.getProject(), getPsiTarget());
  }
  
  @NotNull
  public PsiMember getPsiElement() {
    return myPsiMember;
  }

  @NonNls
  public String getName() {
    return NAME_ATTRIBUTE_META.getJam(myPsiAnnotation).getStringValue(); 
  }

  public PsiAnnotation getIdentifyingAnnotation() {
    return myPsiAnnotation.getPsiElement();
  }

  @Nullable
  public PsiType getComponentType() {
    PsiClass psiClass = (PsiClass)getPsiElement();

    final PsiType unwrapType = SeamCommonUtils.getUnwrapType(psiClass);

    return unwrapType == null ? JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass) : unwrapType;
  }

  public SeamComponentScope getScope() {
    return AnnotationModelUtil.getEnumValue(getIdentifyingAnnotation(), SCOPE_ATTRIBUTE, SeamComponentScope.class).getValue();
  }
}