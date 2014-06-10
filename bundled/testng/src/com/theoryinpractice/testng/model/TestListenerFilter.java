package com.theoryinpractice.testng.model;

import com.intellij.execution.configurations.ConfigurationUtil;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.theoryinpractice.testng.util.TestNGUtil;

/**
 * @author Mark Derricutt
 */
public class TestListenerFilter implements TreeClassChooser.ClassFilterWithScope
{
  private final GlobalSearchScope scope;
  private final Project project;

  public TestListenerFilter(GlobalSearchScope scope, Project project) {
    this.scope = scope;
    this.project = project;
  }

  public boolean isAccepted(PsiClass psiClass) {
    if (!ConfigurationUtil.PUBLIC_INSTANTIATABLE_CLASS.value(psiClass)) return false;

    return TestNGUtil.inheritsITestListener(psiClass);
  }

  public Project getProject() {
    return project;
  }

  public GlobalSearchScope getScope() {
    return scope;
  }
}