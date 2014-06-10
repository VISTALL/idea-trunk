package com.intellij.spring.references;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Key;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.PsiClass;
import com.intellij.spring.model.jam.utils.JamAnnotationTypeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class SpringQualifierNameContributor extends PsiReferenceContributor {
  private static final Key<PsiElement> PSI_ELEMENT_KEY = Key.create("psiElement");


  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(
        PsiJavaPatterns.literalExpression().save(PSI_ELEMENT_KEY).annotationParam(
            PlatformPatterns.string().with(new PatternCondition<String>("customSpringAnno") {
              public boolean accepts(@NotNull final String s, final ProcessingContext context) {
                final PsiElement element = context.get(PSI_ELEMENT_KEY);

                final Module module = ModuleUtil.findModuleForPsiElement(element);
                if (module != null) {
                  final List<PsiClass> classes = JamAnnotationTypeUtil.getQualifierAnnotationTypesWithChildren(module);
                  for (PsiClass qualifierAnnoClass : classes) {
                    if (s.equals(qualifierAnnoClass.getQualifiedName())) return true;
                  }

                }
                return false;
              }
            }), "value"), new SpringQualifierNameReferenceProvider());
  }
}
