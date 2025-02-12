/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jetbrains.android.run.testing;

import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.junit.JUnitUtil;
import com.intellij.execution.junit.JavaRuntimeConfigurationProducerBase;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 31, 2009
 * Time: 2:38:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidTestConfigurationProducer extends JavaRuntimeConfigurationProducerBase implements Cloneable {
  private PsiElement mySourceElement;

  public AndroidTestConfigurationProducer() {
    super(AndroidTestRunConfigurationType.getInstance());
  }

  public PsiElement getSourceElement() {
    return mySourceElement;
  }

  @Nullable
  protected RunnerAndConfigurationSettingsImpl createConfigurationByElement(Location location, ConfigurationContext context) {
    location = JavaExecutionUtil.stepIntoSingleClass(location);
    PsiElement element = location.getPsiElement();
    RunnerAndConfigurationSettingsImpl settings = createAllInPackageConfiguration(element, context);
    if (settings != null) return settings;

    settings = createMethodConfiguration(element, context);
    if (settings != null) return settings;

    return createClassConfiguration(element, context);
  }

  @Nullable
  private RunnerAndConfigurationSettingsImpl createAllInPackageConfiguration(PsiElement element, ConfigurationContext context) {
    PsiPackage p = checkPackage(element);
    if (p != null) {
      RunnerAndConfigurationSettingsImpl settings =
        checkFacetAndCreateConfiguration(p, context, AndroidTestRunConfiguration.TEST_ALL_IN_PACKAGE, p.getQualifiedName());
      if (settings == null) return null;
      AndroidTestRunConfiguration configuration = (AndroidTestRunConfiguration)settings.getConfiguration();
      configuration.PACKAGE_NAME = p.getQualifiedName();
      return settings;
    }
    return null;
  }

  @Nullable
  private RunnerAndConfigurationSettingsImpl createClassConfiguration(PsiElement element, ConfigurationContext context) {
    PsiClass elementClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
    while (elementClass != null) {
      if (JUnitUtil.isTestClass(elementClass)) {
        RunnerAndConfigurationSettingsImpl settings =
          checkFacetAndCreateConfiguration(elementClass, context, AndroidTestRunConfiguration.TEST_CLASS, elementClass.getQualifiedName());
        if (settings == null) return null;
        AndroidTestRunConfiguration configuration = (AndroidTestRunConfiguration)settings.getConfiguration();
        configuration.CLASS_NAME = elementClass.getQualifiedName();
        return settings;
      }
      elementClass = PsiTreeUtil.getParentOfType(elementClass, PsiClass.class);
    }
    return null;
  }

  @Nullable
  private RunnerAndConfigurationSettingsImpl createMethodConfiguration(PsiElement element, ConfigurationContext context) {
    PsiMethod elementMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    while (elementMethod != null) {
      if (isTestMethod(elementMethod)) {
        PsiClass c = elementMethod.getContainingClass();
        assert c != null;
        String name = c.getQualifiedName() + '.' + elementMethod.getName() + "()";
        RunnerAndConfigurationSettingsImpl settings =
          checkFacetAndCreateConfiguration(elementMethod, context, AndroidTestRunConfiguration.TEST_METHOD, name);
        if (settings == null) return null;
        AndroidTestRunConfiguration configuration = (AndroidTestRunConfiguration)settings.getConfiguration();
        configuration.CLASS_NAME = c.getQualifiedName();
        configuration.METHOD_NAME = elementMethod.getName();
        return settings;
      }
      elementMethod = PsiTreeUtil.getParentOfType(elementMethod, PsiMethod.class);
    }
    return null;
  }

  @Nullable
  private RunnerAndConfigurationSettingsImpl checkFacetAndCreateConfiguration(PsiElement element,
                                                                              ConfigurationContext context,
                                                                              int testingType,
                                                                              String configurationName) {
    if (AndroidFacet.getInstance(context.getModule()) == null) {
      return null;
    }
    mySourceElement = element;
    RunnerAndConfigurationSettingsImpl settings = cloneTemplateConfiguration(element.getProject(), context);
    AndroidTestRunConfiguration configuration = (AndroidTestRunConfiguration)settings.getConfiguration();
    configuration.TESTING_TYPE = testingType;
    configuration.setName(JavaExecutionUtil.getPresentableClassName(configurationName, configuration.getConfigurationModule()));
    setupConfigurationModule(context, configuration);
    return settings;
  }

  private static boolean isTestMethod(PsiMethod method) {
    PsiClass testClass = method.getContainingClass();
    if (testClass != null && JUnitUtil.isTestClass(testClass)) {
      return new JUnitUtil.TestMethodFilter(testClass).value(method);
    }
    return false;
  }

  public int compareTo(Object o) {
    return PREFERED;
  }
}
