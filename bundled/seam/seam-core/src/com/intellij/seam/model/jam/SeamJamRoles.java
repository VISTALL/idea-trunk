package com.intellij.seam.model.jam;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationAttributeMeta;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.pom.PomTarget;
import com.intellij.psi.PsiClass;
import com.intellij.seam.constants.SeamAnnotationConstants;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public abstract class SeamJamRoles implements JamElement {
  public static final JamAnnotationAttributeMeta.Collection<SeamJamRole> ROLES_COLLECTION_ATTRIBUTE =
    JamAttributeMeta.annoCollection("value", SeamJamRole.ANNOTATION_META, SeamJamRole.class).addPomTargetProducer(new PairConsumer<SeamJamRole, Consumer<PomTarget>>() {
      public void consume(SeamJamRole seamJamRole, Consumer<PomTarget> consumer) {
        consumer.consume(seamJamRole.getPsiTarget());
      }
    });

  public static final JamAnnotationMeta ANNOTATION_META =
    new JamAnnotationMeta(SeamAnnotationConstants.ROLES_ANNOTATION).addAttribute(ROLES_COLLECTION_ATTRIBUTE);

  public static final JamClassMeta<SeamJamRoles> META = new JamClassMeta<SeamJamRoles>(SeamJamRoles.class)
    .addAnnotation(ANNOTATION_META);


  @NotNull
  @JamPsiConnector
  public abstract PsiClass getPsiElement();

}