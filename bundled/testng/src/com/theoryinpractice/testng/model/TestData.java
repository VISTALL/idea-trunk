package com.theoryinpractice.testng.model;

import com.intellij.execution.ExternalizablePath;
import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.Location;
import com.intellij.execution.RunJavaConfiguration;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.junit.JUnitUtil;
import com.intellij.execution.testframework.TestSearchScope;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.*;

/**
 * @author Hani Suleiman Date: Jul 20, 2005 Time: 1:11:01 PM
 */
public class TestData implements Cloneable
{
  public String SUITE_NAME;
  public String PACKAGE_NAME;
  public String MAIN_CLASS_NAME;
  public String METHOD_NAME;
  public String GROUP_NAME;
  public String TEST_OBJECT;
  public String VM_PARAMETERS;
  public String PARAMETERS;
  public String WORKING_DIRECTORY;
  public String OUTPUT_DIRECTORY;
  public String ANNOTATION_TYPE;

  public String ENV_VARIABLES;
  private Map<String, String> ENVS = new LinkedHashMap<String, String>();
  public boolean PASS_PARENT_ENVS = true;

  public TestSearchScope.Wrapper TEST_SEARCH_SCOPE;
  public Map<String, String> TEST_PROPERTIES = new HashMap<String, String>();
  public List<String> TEST_LISTENERS = new ArrayList<String>();
  public String PROPERTIES_FILE;

  public TestData() {
    TEST_OBJECT = TestType.CLASS.getType();
    TEST_SEARCH_SCOPE = new TestSearchScope.Wrapper();
  }

  public TestSearchScope getScope() {
    return TEST_SEARCH_SCOPE.getScope();
  }

  public void setScope(TestSearchScope testseachscope) {
    TEST_SEARCH_SCOPE.setScope(testseachscope);
  }

  public String getPackageName() {
    return PACKAGE_NAME == null ? "" : PACKAGE_NAME;
  }

  public String getGroupName() {
    return GROUP_NAME == null ? "" : GROUP_NAME;
  }

  public String getMethodName() {
    return METHOD_NAME == null ? "" : METHOD_NAME;
  }

  public String getSuiteName() {
    return SUITE_NAME == null ? "" : SUITE_NAME;
  }

  public String getPropertiesFile() {
    return PROPERTIES_FILE == null ? "" : PROPERTIES_FILE;
  }

  public String getOutputDirectory() {
    return OUTPUT_DIRECTORY == null ? "" : OUTPUT_DIRECTORY;
  }

  public String getAnnotationType() {
    return ANNOTATION_TYPE == null ? "" : ANNOTATION_TYPE;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TestData)) {
      return false;
    } else {
      TestData data = (TestData) obj;
      return Comparing.equal(TEST_OBJECT, data.TEST_OBJECT)
          && Comparing.equal(getMainClassName(), data.getMainClassName())
          && Comparing.equal(getPackageName(), data.getPackageName())
          && Comparing.equal(getSuiteName(), data.getSuiteName())
          && Comparing.equal(getMethodName(), data.getMethodName())
          && Comparing.equal(WORKING_DIRECTORY, data.WORKING_DIRECTORY)
          && Comparing.equal(OUTPUT_DIRECTORY, data.OUTPUT_DIRECTORY)
          && Comparing.equal(VM_PARAMETERS, data.VM_PARAMETERS)
          && Comparing.equal(PARAMETERS, data.PARAMETERS);
    }
  }

  @Override
  public int hashCode() {
    return Comparing.hashcode(getMainClassName()) ^
        Comparing.hashcode(getMethodName()) ^
        Comparing.hashcode(getGroupName()) ^
        Comparing.hashcode(getSuiteName()) ^
        Comparing.hashcode(TEST_OBJECT) ^
        Comparing.hashcode(WORKING_DIRECTORY) ^
        Comparing.hashcode(OUTPUT_DIRECTORY) ^
        Comparing.hashcode(VM_PARAMETERS) ^
        Comparing.hashcode(PARAMETERS);
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    TestData data = (TestData) super.clone();
    data.TEST_SEARCH_SCOPE = new TestSearchScope.Wrapper();

    data.TEST_PROPERTIES = new HashMap<String, String>();
    data.TEST_PROPERTIES.putAll(TEST_PROPERTIES);

    data.TEST_LISTENERS = new ArrayList<String>();
    data.TEST_LISTENERS.addAll(TEST_LISTENERS);

    data.setScope(getScope());
    return data;
  }

  public String getProperty(int type, Project project) {
    switch (type) {
      case RunJavaConfiguration.PROGRAM_PARAMETERS_PROPERTY:
        return PARAMETERS;

      case RunJavaConfiguration.VM_PARAMETERS_PROPERTY:
        return VM_PARAMETERS;

      case RunJavaConfiguration.WORKING_DIRECTORY_PROPERTY:
        return getWorkingDirectory(project);
    }
    throw new RuntimeException("Unknown property: " + type);
  }

  private String getWorkingDirectory(Project project) {
    if (WORKING_DIRECTORY != null && WORKING_DIRECTORY.length() > 0)
      return ExternalizablePath.localPathValue(WORKING_DIRECTORY);

    return project.getBaseDir().getPath();
  }

  public void setProperty(int type, String value, Project project) {
    switch (type) {
      case RunJavaConfiguration.PROGRAM_PARAMETERS_PROPERTY:
        PARAMETERS = value;
        break;

      case RunJavaConfiguration.VM_PARAMETERS_PROPERTY:
        VM_PARAMETERS = value;
        break;

      case RunJavaConfiguration.WORKING_DIRECTORY_PROPERTY:
        //value = value.replace('/', File.separatorChar);
        //if(value.length() > 0 && value.charAt(0) != File.separatorChar) {
        //    value = new File(project.getProjectFilePath()).getParent() + File.separatorChar + value;
        //}
        WORKING_DIRECTORY = ExternalizablePath.urlValue(value);
        break;

      default:
        throw new RuntimeException("Unknown property: " + type);
    }
  }

  public boolean isGeneratedName(String s, JavaRunConfigurationModule config) {
    if (TEST_OBJECT == null) return true;
    if ((TestType.CLASS.getType().equals(TEST_OBJECT) || TestType.METHOD.getType().equals(TEST_OBJECT)) && getMainClassName().length() == 0)
      return JavaExecutionUtil.isNewName(s);
    if (TestType.METHOD.getType().equals(TEST_OBJECT) && getMethodName().length() == 0)
      return JavaExecutionUtil.isNewName(s);
    else return Comparing.equal(s, getGeneratedName(config));
  }

  public String getGeneratedName(JavaRunConfigurationModule runconfigurationmodule) {
    if (TestType.PACKAGE.getType().equals(TEST_OBJECT)) if (getPackageName().length() == 0) return "<default>";
    else return getPackageName();
    String name = JavaExecutionUtil.getPresentableClassName(getMainClassName(), runconfigurationmodule);
    if (TestType.METHOD.getType().equals(TEST_OBJECT)) return name + '.' + getMethodName();
    else return name;
  }

  public String getMainClassName() {
    return MAIN_CLASS_NAME == null ? "" : MAIN_CLASS_NAME;
  }

  public String getMainClassPsiName() {
    return getMainClassName().replace('$', '.');
  }

  public Module setTestMethod(Location<PsiMethod> location) {
    final PsiMethod method = location.getPsiElement();
    METHOD_NAME = method.getName();
    TEST_OBJECT = TestType.METHOD.getType();
    return setMainClass(method.getContainingClass());
  }

  public Module setPackage(PsiPackage pkg) {
    PACKAGE_NAME = pkg.getQualifiedName();
    return null;
  }

  public Module setMainClass(PsiClass psiclass) {
    MAIN_CLASS_NAME = JavaExecutionUtil.getRuntimeQualifiedName(psiclass);
    PsiPackage psipackage = JUnitUtil.getContainingPackage(psiclass);
    PACKAGE_NAME = psipackage == null ? "" : psipackage.getQualifiedName();
    return JavaExecutionUtil.findModule(psiclass);
  }

  public boolean isConfiguredByElement(PsiElement element) {
    if (TEST_OBJECT.equals(TestType.PACKAGE.getType())) {
      if (element instanceof PsiPackage) {
        return Comparing.strEqual(PACKAGE_NAME, ((PsiPackage) element).getQualifiedName());
      } else if (element instanceof PsiDirectory) {
        final PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(((PsiDirectory)element));
        return psiPackage != null && Comparing.strEqual(PACKAGE_NAME, psiPackage.getQualifiedName());
      }
    }

    element = PsiTreeUtil.getParentOfType(element, PsiModifierListOwner.class, false);
    if (element instanceof PsiMethod && TEST_OBJECT.equals(TestType.METHOD.getType())) {
      final PsiClass aClass = ((PsiMethod) element).getContainingClass();
      return Comparing.strEqual(MAIN_CLASS_NAME, JavaExecutionUtil.getRuntimeQualifiedName(aClass)) &&
          Comparing.strEqual(METHOD_NAME, ((PsiMethod) element).getName());
    } else if (element instanceof PsiClass && TEST_OBJECT.equals(TestType.CLASS.getType())) {
      return Comparing.strEqual(MAIN_CLASS_NAME, JavaExecutionUtil.getRuntimeQualifiedName((PsiClass) element));
    }
    return false;
  }

  public Map<String, String> getEnvs() {
    return ENVS;
  }

  public void setEnvs(final Map<String, String> envs) {
    ENVS = envs;
  }
}
