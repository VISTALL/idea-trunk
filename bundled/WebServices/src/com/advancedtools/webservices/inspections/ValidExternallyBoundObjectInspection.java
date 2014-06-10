package com.advancedtools.webservices.inspections;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.index.WSIndexEntry;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.utils.DeployUtils;
import com.advancedtools.webservices.utils.PsiUtil;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author Maxim
 */
public class ValidExternallyBoundObjectInspection extends BaseWebServicesInspection {
  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
                   
  @NotNull
  public String getDisplayName() {
    return WSBundle.message("webservices.inspections.valid.externally.bound.object.display.name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return WSBundle.message("webservices.inspections.valid.externally.bound.object.short.name");
  }

  private static final String PROBLEM_STR = " problem:";

  protected final void doCheckClass(PsiClass c, ProblemsHolder problemsHolder) {
    ExternallyBoundClassContext context = getClassContext(c);

    if (!context.isExternallyBound()) {
      return;
    }

    final String s = DeployUtils.checkAccessibleClass(c);

    if (s != null) {
      WSIndexEntry[] entries = context.getEntries(PsiUtil.findModule(c));
      final String messagePrefix = entries.length > 0 ?
        entries[0].getWsStatus(c) :
        context.annotation.getNameReferenceElement().getText().equals(WEB_SERVICE_ANNOTATION_NAME)? "Web Service":"Mapped Object";
      problemsHolder.registerProblem(c.getNameIdentifier(), messagePrefix + PROBLEM_STR +s,
        ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, EMPTY);
    }
    
    if (context.annotation != null && JWSDPWSEngine.isClassInDefaultPackageWithNoTargetNs(c)) {
      problemsHolder.registerProblem(c.getNameIdentifier(), WSBundle.message("class.in.default.package.should.have.targetnamespace.specified.validation.message"), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, EMPTY);
    }
  }

  protected final void checkMember(final ProblemsHolder problemsHolder,PsiMember member) {
    final PsiClass containingClass = member.getContainingClass();
    if (containingClass == null) return;
    final ExternallyBoundClassContext classContext = getClassContext(containingClass);
    if(!classContext.isExternallyBound()) return;

    final Module module = PsiUtil.findModule(member);
    final WSIndexEntry[] wsEntries = classContext.index.getWsEntries(Arrays.asList(classContext.getEntries(module)), member);

    if (wsEntries.length > 0 ||
          ( classContext.containingClassIsExternallyBound &&
            member instanceof PsiMethod &&
            PropertyUtil.getPropertyName((PsiMethod) member) == null &&
            ( member.hasModifierProperty(PsiModifier.PUBLIC) ||
              AnnotationUtil.findAnnotation(member, JWSDPWSEngine.wsMethodsSet) != null
            )
          )
       ) {
      if (member instanceof PsiField) {
        PsiField f = (PsiField) member;
        boolean acceptableField = DeployUtils.isAcceptableField(f);

        if (!acceptableField) {
          problemsHolder.registerProblem(
            f.getNameIdentifier(),
            wsEntries[0].getWsStatus(f) + PROBLEM_STR +"field should be public",
            ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
            EMPTY
          );
        } else {
          String problem = DeployUtils.getDeploymentProblemForType(f.getType());
          if (problem != null) {
            problemsHolder.registerProblem(
              f.getNameIdentifier(),
              wsEntries[0].getWsStatus(f) + PROBLEM_STR + problem,
              ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
              EMPTY
            );
          }
        }
      }
      else if (member instanceof PsiMethod && DeployUtils.canBeWebMethod(member)) { //IDEA-20924
        PsiMethod method = (PsiMethod) member;
        DeployUtils.DeploymentProcessor processor = new DeployUtils.DeploymentProcessor() {
          public void processMethod(PsiMethod method, String problem, List<String> nonelementaryTypes) {
            if (problem != null) {
              final String messagePrefix = wsEntries.length > 0 ? wsEntries[0].getWsStatus(method) : "Web Method";

              problemsHolder.registerProblem(
                method.getNameIdentifier(),
                messagePrefix + PROBLEM_STR + problem,
                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                EMPTY
              );
            }
          }
        };

        DeployUtils.processMethod(
          method,
          processor
        );

        final String name = PropertyUtil.getPropertyName(method);

        if (name != null) {
          if (PropertyUtil.isSimplePropertyGetter(method)) {
            final PsiMethod propertySetter = PropertyUtil.findPropertySetter(containingClass, name, false, false);

            if (propertySetter == null) {
              problemsHolder.registerProblem(
                method.getNameIdentifier(),
                "No setter method for property " + name,
                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                EMPTY
              );
            } else {
               DeployUtils.processMethod(
                propertySetter,
                processor
              );
            }
          }
        }
      }
    }
  }

}
