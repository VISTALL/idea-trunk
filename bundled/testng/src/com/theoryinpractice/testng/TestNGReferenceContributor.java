/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 11/11/2006
 * Time: 16:15:10
 */
package com.theoryinpractice.testng;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.lookup.LookupValueFactory;
import com.intellij.codeInspection.InspectionProfile;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.theoryinpractice.testng.inspection.DependsOnGroupsInspection;
import com.theoryinpractice.testng.util.TestNGUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.List;

public class TestNGReferenceContributor extends PsiReferenceContributor {
  private static PsiElementPattern.Capture<PsiLiteralExpression> getElementPattern(String annotation) {
    return PlatformPatterns.psiElement(PsiLiteralExpression.class).and(new FilterPattern(new TestAnnotationFilter(annotation)));
  }

  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(getElementPattern("dependsOnMethods"), new PsiReferenceProviderBase() {
      @NotNull
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
        return new MethodReference[]{new MethodReference((PsiLiteralExpression)element)};
      }
    });

    registrar.registerReferenceProvider(getElementPattern("dataProvider"), new PsiReferenceProviderBase() {
      @NotNull
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
        return new DataProviderReference[]{new DataProviderReference((PsiLiteralExpression)element)};
      }
    });
    registrar.registerReferenceProvider(getElementPattern("groups"), new PsiReferenceProviderBase() {
      @NotNull
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
        return new GroupReference[]{new GroupReference(element.getProject(), (PsiLiteralExpression)element)};
      }
    });
    registrar.registerReferenceProvider(getElementPattern("dependsOnGroups"), new PsiReferenceProviderBase() {
      @NotNull
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
        return new GroupReference[]{new GroupReference(element.getProject(), (PsiLiteralExpression)element)};
      }
    });
  }

  private static class DataProviderReference extends PsiReferenceBase<PsiLiteralExpression> {

    public DataProviderReference(PsiLiteralExpression element) {
      super(element, false);
    }

    @Nullable
    public PsiElement resolve() {
      PsiClass cls = PsiUtil.getTopLevelClass(getElement());
      if (cls != null) {
        PsiMethod[] methods = cls.getMethods();
        @NonNls String val = getValue();
        for (PsiMethod method : methods) {
          if (AnnotationUtil.isAnnotated(method, DataProvider.class.getName(), false)) {
            PsiAnnotation dataProviderAnnotation = AnnotationUtil.findAnnotation(method, DataProvider.class.getName());
            PsiNameValuePair[] values = dataProviderAnnotation.getParameterList().getAttributes();
            for (PsiNameValuePair value : values) {
              if ("name".equals(value.getName())) {
                return method;
              }
            }
          }
        }
      }
      return null;
    }

    public Object[] getVariants() {
      final List<Object> list = new ArrayList<Object>();
      final PsiClass cls = PsiUtil.getTopLevelClass(getElement());
      if (cls != null) {
        final PsiMethod current = PsiTreeUtil.getParentOfType(getElement(), PsiMethod.class);
        final PsiMethod[] methods = cls.getMethods();
        for (PsiMethod method : methods) {
          if (current != null && method.getName().equals(current.getName())) continue;
          if (AnnotationUtil.isAnnotated(method, DataProvider.class.getName(), false)) {
            PsiAnnotation dataProviderAnnotation = AnnotationUtil.findAnnotation(method, DataProvider.class.getName());
            PsiNameValuePair[] values = dataProviderAnnotation.getParameterList().getAttributes();
            for (PsiNameValuePair value : values) {
              if ("name".equals(value.getName())) {
                list.add(LookupValueFactory.createLookupValue(value.getText(), null));
              }
            }
          }
        }
      }
      return list.toArray();
    }
  }

  private static class MethodReference extends PsiReferenceBase<PsiLiteralExpression> {

    public MethodReference(PsiLiteralExpression element) {
      super(element, false);
    }

    @Nullable
    public PsiElement resolve() {
      PsiClass cls = PsiUtil.getTopLevelClass(getElement());
      if (cls != null) {
        PsiMethod[] methods = cls.getMethods();
        @NonNls String val = getValue();
        int hackIndex = val.indexOf("IntellijIdeaRulezzz ");
        if (hackIndex > -1) {
          val = val.substring(0, hackIndex) + val.substring(hackIndex + 1, val.length());
        }
        for (PsiMethod method : methods) {
          if (TestNGUtil.hasTest(method)) {
            if (method.getName().equals(val)) {
              return method;
            }
          }
        }
      }
      return null;
    }

    public Object[] getVariants() {
      List<Object> list = new ArrayList<Object>();
      PsiClass cls = PsiUtil.getTopLevelClass(getElement());
      if (cls != null) {
        PsiMethod current = PsiTreeUtil.getParentOfType(getElement(), PsiMethod.class);
        PsiMethod[] methods = cls.getMethods();
        for (PsiMethod method : methods) {
          if (current != null && method.getName().equals(current.getName())) continue;
          if (TestNGUtil.hasTest(method) || TestNGUtil.hasConfig(method)) {
            list.add(LookupValueFactory.createLookupValue(method.getName(), null));
          }
        }
      }
      return list.toArray();
    }
  }

  private static class GroupReference extends PsiReferenceBase<PsiLiteralExpression> {
    private final Project myProject;

    public GroupReference(Project project, PsiLiteralExpression element) {
      super(element, false);
      myProject = project;
    }

    @Nullable
    public PsiElement resolve() {
      return null;
    }

    public Object[] getVariants() {
      List<Object> list = new ArrayList<Object>();

      InspectionProfile inspectionProfile = InspectionProjectProfileManager.getInstance(myProject).getInspectionProfile();
      final InspectionProfileEntry inspectionTool = inspectionProfile.getInspectionTool(DependsOnGroupsInspection.SHORT_NAME, myElement);
      DependsOnGroupsInspection inspection = (DependsOnGroupsInspection)((LocalInspectionToolWrapper)inspectionTool).getTool();

      for (String groupName : inspection.groups) {
        list.add(LookupValueFactory.createLookupValue(groupName, null));
      }

      if (!list.isEmpty()) {
        return list.toArray();
      }
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }
  }

  private static class TestAnnotationFilter implements ElementFilter {

    private final String myParameterName;

    public TestAnnotationFilter(@NotNull @NonNls String parameterName) {
      myParameterName = parameterName;
    }

    public boolean isAcceptable(Object element, PsiElement context) {
      PsiNameValuePair pair = PsiTreeUtil.getParentOfType(context, PsiNameValuePair.class);
      if (null == pair) return false;
      if (!myParameterName.equals(pair.getName())) return false;
      PsiAnnotation annotation = PsiTreeUtil.getParentOfType(pair, PsiAnnotation.class);
      if (annotation == null) return false;
      if (!TestNGUtil.isTestNGAnnotation(annotation)) return false;
      return true;
    }

    public boolean isClassAcceptable(Class hintClass) {
      return PsiLiteralExpression.class.isAssignableFrom(hintClass);
    }
  }
}
