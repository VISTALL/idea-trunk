package com.theoryinpractice.testng.inspection;

import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameValuePair;
import com.theoryinpractice.testng.util.TestNGUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Hani Suleiman Date: Aug 3, 2005 Time: 3:34:56 AM
 */
public class DependsOnMethodInspection extends BaseJavaLocalInspectionTool
{
    private static final Logger LOGGER = Logger.getInstance("TestNG Runner");
    private static final Pattern PATTERN = Pattern.compile("\"([a-zA-Z1-9_\\(\\)]*)\"");
    private static final ProblemDescriptor[] EMPTY = new ProblemDescriptor[0];
    
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "TestNG";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "dependsOnMethods problem";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "dependsOnMethodTestNG";
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    @Nullable
    public ProblemDescriptor[] checkClass(@NotNull PsiClass psiClass, @NotNull InspectionManager manager, boolean isOnTheFly) {

        //LOGGER.info("Looking for dependsOnMethods problems in " + psiClass.getName());

        if (!psiClass.getContainingFile().isWritable()) return null;

        PsiAnnotation[] annotations = TestNGUtil.getTestNGAnnotations(psiClass);
        if(annotations.length == 0) return EMPTY;
        List<ProblemDescriptor> problemDescriptors = new ArrayList<ProblemDescriptor>();

        for (PsiAnnotation annotation : annotations) {
            PsiNameValuePair dep = null;
            PsiNameValuePair[] params = annotation.getParameterList().getAttributes();
            for (PsiNameValuePair param : params) {
                if ("dependsOnMethods".equals(param.getName())) {
                    dep = param;
                    break;
                }
            }

            if (dep != null) {
                if (dep.getValue() != null) {
                    Matcher matcher = PATTERN.matcher(dep.getValue().getText());
                    while (matcher.find()) {
                        String methodName = matcher.group(1);
                        checkMethodNameDependency(manager, psiClass, methodName, dep, problemDescriptors);
                    }
                }
            }
        }
        
        return problemDescriptors.toArray(new ProblemDescriptor[] {} );
    }

    private static void checkMethodNameDependency(InspectionManager manager, PsiClass psiClass, String methodName, PsiNameValuePair dep, List<ProblemDescriptor> problemDescriptors) {
        LOGGER.debug("Found dependsOnMethods with text: " + methodName);
        if (methodName.length() > 0 && methodName.charAt(methodName.length() - 1) == ')') {

            LOGGER.debug("dependsOnMethods contains ()" + psiClass.getName());
            // TODO Add quick fix for removing brackets on annotation
            ProblemDescriptor descriptor = manager.createProblemDescriptor(dep,
                                                               "Method '" + methodName + "' should not include () characters.",
                                                               (LocalQuickFix) null,
                                                               ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);

            problemDescriptors.add(descriptor);

        } else {
            PsiMethod[] foundMethods = psiClass.findMethodsByName(methodName, true);

            if (foundMethods.length == 0) {
                LOGGER.debug("dependsOnMethods method doesn't exist:" + methodName);
                ProblemDescriptor descriptor = manager.createProblemDescriptor(dep,
                                                                   "Method '" + methodName + "' unknown.",
                                                                   (LocalQuickFix) null,
                                                                   ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                problemDescriptors.add(descriptor);

            } else {
              boolean hasTestsOrConfigs = false;
              for (PsiMethod foundMethod : foundMethods) {
                 hasTestsOrConfigs |= TestNGUtil.hasTest(foundMethod) || TestNGUtil.hasConfig(foundMethod);
              }
              if (!hasTestsOrConfigs) {
                ProblemDescriptor descriptor = manager.createProblemDescriptor(dep,
                                                                     "Method '" + methodName + "' is not a test or configuration method.",
                                                                     (LocalQuickFix) null,
                                                                     ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                problemDescriptors.add(descriptor);
              }
            }
        }
    }

}