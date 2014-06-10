package com.intellij.eclipse.export.exporter;

import com.intellij.eclipse.export.IdeaProjectFileConstants;
import com.intellij.eclipse.export.ResourceUtil;
import com.intellij.eclipse.export.model.*;
import com.intellij.eclipse.export.model.codestyle.AbstractCodeStyleParam;
import com.intellij.eclipse.export.model.codestyle.CodeStyle;
import com.intellij.eclipse.export.model.codestyle.CodeStyleContainerParam;
import com.intellij.eclipse.export.model.codestyle.CodeStyleParam;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Exporter implements IdeaProjectFileConstants {
  private IdeaProject ideaProject;
  private String outputPath;
  private boolean shouldCopyContent;
  private boolean shouldUsePathVariables;
  private boolean shouldDeclareLibraries;

  // Default values for project file properties

  private static final String PROJECT_VERSION = "4";
  private static final String MODULE_VERSION = PROJECT_VERSION;

  private static final boolean USE_MODULE_RELATIVE_PATHS = true;

  private boolean bundledAnt = true;

  private boolean isTestSource = false;

  private String antConfigurationComponentName = "AntConfiguration";

  private static final String PROJECT_MODULE_MANAGER_COMPONENT_NAME = "ProjectModuleManager";

  private String codeStyleComponentName = "CodeStyleSettingsManager";

  private static final String PROJECT_LIBRARY_TABLE_NAME = "libraryTable";

  private String javaModuleType = "JAVA_MODULE";
  private String ejbModuleType = "J2EE_EJB_MODULE";
  private String webModuleType = "J2EE_WEB_MODULE";
  private String earModuleType = "J2EE_APPLICATION_MODULE";

  private String newModuleRootManager = "NewModuleRootManager";

  private String orderEntryType1 = "inheritedJdk";

  private String orderEntryType2 = "sourceFolder";

  private String ORDER_ENTRY_LEVEL_PROJECT = "project";
  private String ORDER_ENTRY_LEVEL_APPLICATION = "application";

  private String orderEntryTypeLibrary = "library";

  private String orderEntryTypeJDK = "jdk";

  private static final String ORDER_ENTRY_TYPE_MODULE_LIBRARY = "module-library";

  private String orderEntryTypeModule = "module";

  private String containerTypeModule = "module";

  private String ejbModulePropertiesName = "EjbModuleProperties";

  private String ejbDeploymentDescriptorVersion = "1.4";
  private String ejbDeploymentDescriptorName = "ejb-jar.xml";

  private String webModulePropertiesName = "WebModuleProperties";
  private String webDeploymentDescriptorVersion = "2.3";
  private String webDeploymentDescriptorName = "web.xml";

  private String earModulePropertiesName = "ApplicationModuleProperties";
  private String earDeploymentDescriptorVersion = "1.3";
  private String earDeploymentDescriptorName = "application.xml";
  private String containerElementMethodValue = "4";
  private String containerElementContextRootValue = "<N/A>";

  public Exporter(IdeaProject ideaProject,
                  String outputPath,
                  boolean shouldCopyContent,
                  boolean shouldUsePathVariables,
                  boolean shouldDeclareLibraries) {
    this.ideaProject = ideaProject;
    this.outputPath = outputPath;
    this.shouldCopyContent = shouldCopyContent;
    this.shouldUsePathVariables = shouldUsePathVariables;
    this.shouldDeclareLibraries = shouldDeclareLibraries;
  }

  protected void writeProject() throws ParserConfigurationException,
                                       IOException {
    prepareProjectDirectory();
    OutputStream out = new FileOutputStream(getProjectFile());

    Document document = printProjectToXML();

    XMLOutputter outputter = new XMLOutputter();
    Format format = Format.getPrettyFormat();
    outputter.setFormat(format);

    outputter.output(document, out);
    out.close();
  }

  protected File getProjectFile() {
    return new File(outputPath, ideaProject.getRelativeFile().toString());
  }

  protected void prepareProjectDirectory() {
    getProjectFile().getParentFile().mkdirs();
  }

  protected Document printProjectToXML() throws ParserConfigurationException {
    Document document = createDocument();
    Element root = createElement(PROJECT_ROOT_TAG, document);
    root.setAttribute(PROJECT_VERSION_PROPERTY, PROJECT_VERSION);
    root.setAttribute(PROJECT_RELATIVE_PATH_PROPERTY,
                      Boolean.toString(USE_MODULE_RELATIVE_PATHS));

    createAntConfigurationComponent(root);
    createProjectRootManager(root);
    createProjectModuleManagerComponent(root);
    createProjectLibraryTable(root);
    createPathVariablesTable(root);

    return document;
  }

  protected void createAntConfigurationComponent(Element projectRoot) {
    Element component = createElement(COMPONENT_TAG, projectRoot);
    component.setAttribute(COMPONENT_NAME_PROPERTY,
                           antConfigurationComponentName);
    Element element = createElement(DEFAULT_ANT_TAG, component);
    element.setAttribute(BUNDLED_ANT_PROPERTY, Boolean.toString(bundledAnt));
  }

  protected void createProjectRootManager(Element projectRoot) {
    Element component = createElement(COMPONENT_TAG, projectRoot);
    component.setAttribute(COMPONENT_NAME_PROPERTY, "ProjectRootManager");
    component.setAttribute("version", "2");

    LanguageLevel langLevel = ideaProject.getLanguageLevel();

    if (langLevel != null) {
      boolean hasAssertKeyword = langLevel.hasAssertKeyword();
      boolean isJdk15 = langLevel.isJdk15();

      component.setAttribute("assert-keyword", Boolean.toString(hasAssertKeyword));
      component.setAttribute("jdk-15", Boolean.toString(isJdk15));
    }

    if (ideaProject.getJdkName() != null) {
      component.setAttribute("project-jdk-name", ideaProject.getJdkName());
      component.setAttribute("project-jdk-type", "JavaSDK"); // bad
    }
  }

  protected void createProjectLibraryTable(Element projectRoot) {
    Element component = createElement(COMPONENT_TAG, projectRoot);
    component.setAttribute(COMPONENT_NAME_PROPERTY, PROJECT_LIBRARY_TABLE_NAME);

    if (!shouldDeclareLibraries) return;

    for (ProjectLibrary lib : ideaProject.getProjectLibraries()) {
      Element library = createElement(LIBRARY_TAG, component);
      library.setAttribute(LIBRARY_NAME_PROPERTY, lib.getName());

      Element classes = createElement(CLASSES_TAG, library);
      createProjectClasspathElement(lib, classes);
    }
  }

  private void createProjectClasspathElement(ProjectLibrary classPathEntry, Element classes) {
    for (Resource res : classPathEntry.getResources()) {
      String path = shouldUsePathVariables
                    ? res.getVariablePath()
                    : res.getAbsolutePath();
      String formattedPath = ResourceUtil.formatFileURL(path);
      Element classesRoot = createElement(CLASSES_ROOT_TAG, classes);
      classesRoot.setAttribute(CLASSES_ROOT_URL_PROPERTY, formattedPath);
    }
  }

  protected void createPathVariablesTable(Element parent) {
    if (!shouldUsePathVariables) return;

    Element table = createElement("UsedPathMacros", parent);

    for (String variable : ideaProject.getPathVariables()) {
      Element el = createElement("macro", table);
      el.setAttribute("name", variable);
    }
  }

  protected void createProjectModuleManagerComponent(Element projectRoot) {
    Element component = createElement(COMPONENT_TAG, projectRoot);
    component.setAttribute(COMPONENT_NAME_PROPERTY, PROJECT_MODULE_MANAGER_COMPONENT_NAME);

    Element modules = createElement(MODULES_TAG, component);

    for (IdeaModule ideaModule : ideaProject.getModules()) {
      Element el = createElement(MODULE_TAG, modules);

      String moduleFileUrl = FILE_FULL_URL_PREFIX + PROJECT_DIR_URL_VAR + "/../"
                             + ideaModule.getRelativeFile();

      try {
        URL url = new URL(moduleFileUrl);

        el.setAttribute(MODULE_FILE_URL_PROPERTY, url.toString());
        el.setAttribute(MODULE_FILE_PATH_PROPERTY, url.toString());
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void export() throws ParserConfigurationException, IOException {
    writeProject();

    for (IdeaModule ideaModule : ideaProject.getModules()) {
      File moduleFile = writeModule(ideaModule);

      if (shouldCopyContent) {
        copySources(ideaModule, moduleFile.getParentFile());
      }
    }
  }

  private static Class[] CLASS_PATH_ENTRY_PARAMS_30 = new Class[]{
    int.class,
    int.class,
    IPath.class,
    IPath[].class,
    IPath[].class,
    IPath.class,
    IPath.class,
    IPath.class,
    boolean.class
  };

  private Object accessRuleArray = null;
  private Object classpathAttributeArray = null;

  private static final String ACCESS_RULE_CLASS_NAME = "org.eclipse.jdt.core.IAccessRule";
  private static final String CLASS_PATH_ATTRIBUTE_CLASS_NAME =
    "org.eclipse.jdt.core.IClasspathAttribute";
  private static Class[] CLASS_PATH_ENTRY_PARAMS_31 = null;

  {
    CLASS_PATH_ENTRY_PARAMS_31 = new Class[CLASS_PATH_ENTRY_PARAMS_30.length + 3];
    int i;
    for (i = 0; i < CLASS_PATH_ENTRY_PARAMS_30.length; i++) {
      CLASS_PATH_ENTRY_PARAMS_31[i] = CLASS_PATH_ENTRY_PARAMS_30[i];
    }
    try {
      Class c = Class.forName("[L" + ACCESS_RULE_CLASS_NAME + ";");
      CLASS_PATH_ENTRY_PARAMS_31[i++] = c;

      c = Class.forName(ACCESS_RULE_CLASS_NAME);
      accessRuleArray = Array.newInstance(c, 0);

      CLASS_PATH_ENTRY_PARAMS_31[i++] = boolean.class;

      c = Class.forName("[L" + CLASS_PATH_ATTRIBUTE_CLASS_NAME + ";");
      CLASS_PATH_ENTRY_PARAMS_31[i++] = c;

      c = Class.forName(CLASS_PATH_ATTRIBUTE_CLASS_NAME);
      accessRuleArray = Array.newInstance(c, 0);
    } catch (ClassNotFoundException cne) {
      CLASS_PATH_ENTRY_PARAMS_31 = null;
    }
  }

  protected void copySources(IdeaModule ideaModule, File moduleDir) throws IOException {
    copyJavaProjectSources(ideaModule, moduleDir);
    copyWebProjectSources(ideaModule, moduleDir);
  }

  private void copyJavaProjectSources(IdeaModule ideaModule, File destDir) throws IOException {
    ideaModule.copyContentTo(destDir);
  }

  protected File writeModule(IdeaModule ideaModule)
    throws ParserConfigurationException, IOException {
    prepareModuleDirectory(ideaModule);

    OutputStream out = new FileOutputStream(getModuleFile(ideaModule));

    Document document = printModuleToXML(ideaModule);

    XMLOutputter outputter = new XMLOutputter();
    Format format = Format.getPrettyFormat();
    outputter.setFormat(format);

    outputter.output(document, out);
    out.close();

    return getModuleFile(ideaModule);
  }

  protected void prepareModuleDirectory(IdeaModule ideaModule) {
    getModuleFile(ideaModule).getParentFile().mkdirs();
  }

  protected File getModuleFile(IdeaModule ideaModule) {
    return new File(outputPath, ideaModule.getRelativeFile().toString());
  }

  protected Document printModuleToXML(IdeaModule ideaModule) {
    Document document = createDocument();

    Element moduleRoot = createElement(MODULE_ROOT_TAG, document);
    int moduleType = ideaModule.getType();

    moduleRoot.setAttribute(MODULE_VERSION_PROPERTY, MODULE_VERSION);
    moduleRoot.setAttribute(MODULE_TYPE_PROPERTY, getModuleTypeName(moduleType));
    moduleRoot.setAttribute(MODULE_RELATIVE_PATH_PROPERTY,
                            Boolean.toString(USE_MODULE_RELATIVE_PATHS));

    createNewModuleRootManager(moduleRoot, ideaModule);

    switch (moduleType) {
      case IdeaModule.MT_J2EE_EJB:
        createEJBDescriptor(moduleRoot, ideaModule);
        break;
      case IdeaModule.MT_J2EE_WEB:
        createWEBDescriptor(moduleRoot, ideaModule);
        break;
      case IdeaModule.MT_J2EE_EAR:
        createEARDescriptor(moduleRoot, ideaModule);
        break;
    }

    return document;
  }

  protected String getOutputPath
    () {
    int pos1 = outputPath.lastIndexOf('/');
    pos1 = pos1 < 0 ? 0 : pos1 + 1;
    int pos2 = outputPath.lastIndexOf('\\');
    pos2 = pos2 < 0 ? 0 : pos2 + 1;
    int pos = pos1 > pos2 ? pos1 : pos2;
    String res = outputPath.substring(0, pos);

    return res;
  }

  protected void createNewModuleRootManager(Element moduleRoot, IdeaModule ideaModule) {
    Element component = createElement(COMPONENT_TAG, moduleRoot);
    component.setAttribute(COMPONENT_NAME_PROPERTY, newModuleRootManager);
    if (ideaModule.getLanguageLevel() != null) {
      component.setAttribute("LANGUAGE_LEVEL",
                             ideaModule.getLanguageLevel().toString()); //todo: bad;
    }

    createElement("exclude-output", component); //todo: bad

    createModuleOutput(component, ideaModule);
    createModuleContent(component, ideaModule);
    createModuleOrderEntries(component, ideaModule);
  }

  protected void createModuleContent(Element root, IdeaModule ideaModule) {
    Element content = createElement(CONTENT_TAG, root);

    createContentElement(content, ideaModule, ideaModule.getContentRoot());

    for (String location : ideaModule.getSourceLocations()) {
      Element sourceFolder = createElement(SOURCE_FOLDER_TAG, content);
      createContentElement(sourceFolder, ideaModule, location);
      sourceFolder.setAttribute(IS_TEST_SOURCE_PROPERTY, Boolean.toString(isTestSource));
    }
  }

  private void createContentElement(Element sourceFolder,
                                    IdeaModule module,
                                    String sourceURL) {
    if (shouldCopyContent) {
      //sourceURL = ResourceUtil.insertModuleDirPrefix(module, sourceURL);
      sourceURL = IdeaProjectFileConstants.MODULE_DIR_URL_VAR
                  + "/" + module.relativize(sourceURL);
    }
    sourceFolder.setAttribute(MODULE_URL_PROPERTY, ResourceUtil.formatFileURL(sourceURL));
  }

  protected void createModuleOutput(Element moduleRootManager, IdeaModule ideaModule) {
    String outputURL = "$MODULE_DIR$/classes";
    Element outputFolder = createElement(OUTPUT_FOLDER_TAG, moduleRootManager);
    outputFolder.setAttribute(MODULE_URL_PROPERTY, ResourceUtil.formatFileURL(outputURL));
  }

  protected void createModuleOrderEntries(Element moduleRootManager, IdeaModule ideaModule) {
    createJDKLink(moduleRootManager, ideaModule);
    createSourceFolder(moduleRootManager);

    createModuleLibraries(moduleRootManager, ideaModule);
    createModuleModuleLibraries(moduleRootManager, ideaModule);
    createModuleDependencies(moduleRootManager, ideaModule);

    createOrderEntryProperties(moduleRootManager);
  }

  private void createJDKLink(Element moduleRootManager, IdeaModule ideaModule) {
    Element orderEntry = createElement(ORDER_ENTRY_TAG, moduleRootManager);

    String jdkName = ideaModule.getJdkName();
    if (jdkName == null) {
      orderEntry.setAttribute(ORDER_ENTRY_TYPE_PROPERTY, orderEntryType1);
    } else {
      orderEntry.setAttribute(ORDER_ENTRY_TYPE_PROPERTY, orderEntryTypeJDK);
      orderEntry.setAttribute(ORDER_ENTRY_JDKNAME_PROPERTY, jdkName);
      orderEntry.setAttribute(ORDER_ENTRY_JDKTYPE_PROPERTY, "JavaSDK"); //todo bad
    }
  }

  private void createSourceFolder(Element moduleRootManager) {
    Element orderEntry = createElement(ORDER_ENTRY_TAG, moduleRootManager);
    orderEntry.setAttribute(ORDER_ENTRY_TYPE_PROPERTY, orderEntryType2);
    orderEntry.setAttribute(IS_TEST_SOURCE_PROPERTY, Boolean
      .toString(isTestSource));
  }

  protected void createModuleLibraries(Element parent, IdeaModule ideaModule) {
    for (Library moduleLibrary : ideaModule.getLibraries()) {
      Element orderEntry = createElement(ORDER_ENTRY_TAG, parent);
      orderEntry.setAttribute(ORDER_ENTRY_TYPE_PROPERTY, ORDER_ENTRY_TYPE_MODULE_LIBRARY);

      if (moduleLibrary.isExported()) {
        orderEntry.setAttribute("exported", "");
      }

      Element library = createElement(LIBRARY_TAG, orderEntry);
      Element classes = createElement(CLASSES_TAG, library);

      createModuleClasspathElement(classes, moduleLibrary, ideaModule);
    }
  }

  private void createModuleClasspathElement(Element parent,
                                            Library lib,
                                            IdeaModule module) {
    Resource res = lib.getResource();
    String path;

    if (lib.isLocal()) {
      path = res.getAbsolutePath();
      if (shouldCopyContent) {
        path = IdeaProjectFileConstants.MODULE_DIR_URL_VAR
               + "/" + module.relativize(path);
      }
    } else {
      path = res.isVariable() && shouldUsePathVariables
             ? res.getVariablePath()
             : res.getAbsolutePath();
    }

    String formattedPath = ResourceUtil.formatFileURL(path);
    Element classesRoot = createElement(CLASSES_ROOT_TAG, parent);
    classesRoot.setAttribute(CLASSES_ROOT_URL_PROPERTY, formattedPath);
  }

  protected void createModuleModuleLibraries(Element moduleRootManager,
                                             IdeaModule ideaModule) {
    for (ModuleLibrary lib : ideaModule.getModuleLibraries()) {
      Element orderEntry = createElement(ORDER_ENTRY_TAG, moduleRootManager);
      String name = lib.getName();

      orderEntry.setAttribute(ORDER_ENTRY_TYPE_PROPERTY, orderEntryTypeLibrary);

      if (lib.isExported()) {
        orderEntry.setAttribute("exported", ""); //  TODO generalize
      }

      orderEntry.setAttribute(ORDER_ENTRY_NAME_PROPERTY, name);
      orderEntry.setAttribute(ORDER_ENTRY_LEVEL_PROPERTY,
                              shouldDeclareLibraries
                              ? ORDER_ENTRY_LEVEL_PROJECT
                              : ORDER_ENTRY_LEVEL_APPLICATION);
    }
  }

  protected void createModuleDependencies(Element moduleRootManager, IdeaModule ideaModule) {
    for (IdeaModule refModule : ideaModule.getReferencedModules()) {
      Element orderEntry = createElement(ORDER_ENTRY_TAG, moduleRootManager);
      orderEntry.setAttribute(ORDER_ENTRY_TYPE_PROPERTY, orderEntryTypeModule);
      orderEntry.setAttribute(ORDER_ENTRY_MODULE_NAME_PROPERTY, refModule.getName());
    }
  }

  private void createOrderEntryProperties(Element moduleRootManager) {
    createElement(ORDER_ENTRY_PROPERTIES_TAG, moduleRootManager);
  }

  protected void createCodeStyle(Element moduleRootManager) {
    Element component = createElement(COMPONENT_TAG, moduleRootManager);
    component.setAttribute(COMPONENT_NAME_PROPERTY, codeStyleComponentName);
    CodeStyle codeStyle = ideaProject.getCodeStyle();
    configureCodeStyleParam(component, codeStyle);
  }

  protected void configureCodeStyleParam(Element root,
                                         CodeStyleContainerParam containerParam) {
    AbstractCodeStyleParam[] params = containerParam.getChildren();

    Element value = null;
    if (!(containerParam instanceof CodeStyle)) {
      Element option = createElement(CODE_STYLE_OPTION_TAG, root);
      option.setAttribute(CODE_STYLE_OPTION_NAME_PROPERTY, containerParam.getName());

      value = createElement(CODE_STYLE_VALUE_TAG, option);
    } else
      value = root;

    for (AbstractCodeStyleParam param : params) {
      if (param instanceof CodeStyleParam) {
        CodeStyleParam codeStyleParam = (CodeStyleParam)param;

        Element childOption = createElement(CODE_STYLE_OPTION_TAG, value);
        childOption.setAttribute(CODE_STYLE_OPTION_NAME_PROPERTY, codeStyleParam.getName());
        childOption.setAttribute(CODE_STYLE_OPTION_VALUE_PROPERTY, codeStyleParam.getValue());
      } else if (param instanceof CodeStyleContainerParam) {
        CodeStyleContainerParam codeStyleContainerParam = (CodeStyleContainerParam)param;
        configureCodeStyleParam(value, codeStyleContainerParam);
      }
    }
  }

  private String getModuleTypeName(int moduleType) {
    switch (moduleType) {
      case IdeaModule.MT_J2EE_EAR:
        return earModuleType;
      case IdeaModule.MT_J2EE_WEB:
        return webModuleType;
      case IdeaModule.MT_J2EE_EJB:
        return ejbModuleType;
      case IdeaModule.MT_JAVA:
        return javaModuleType;
      default:
        throw new RuntimeException(
          "Wrong value of argument exception. moduleType = " + moduleType);
    }
  }

  protected void createEJBDescriptor(Element moduleRoot, IdeaModule ideaModule) {

    Element descriptorElement = createElement(COMPONENT_TAG, moduleRoot);
    descriptorElement.setAttribute(COMPONENT_NAME_PROPERTY, ejbModulePropertiesName);

    Element deploymentDescriptorElement =
      createElement(DEPLOYMENT_DESCRIPTOR_TAG, descriptorElement);
    deploymentDescriptorElement
      .setAttribute(DEPLOYMENT_DESCRIPTOR_NAME_PROPERTY, ejbDeploymentDescriptorName);
    String deploymentUrl = ideaModule.getProperty(DEPLOYMENT_DESCRIPTOR_URL_PROPERTY);
    if (shouldCopyContent) {
      deploymentUrl = ResourceUtil.insertModuleDirPrefix(ideaModule,
                                                         deploymentUrl);
    }
    deploymentDescriptorElement
      .setAttribute(DEPLOYMENT_DESCRIPTOR_URL_PROPERTY, FILE_FULL_URL_PREFIX + deploymentUrl);
    deploymentDescriptorElement
      .setAttribute(DEPLOYMENT_DESCRIPTOR_VERSION_PROPERTY, ejbDeploymentDescriptorVersion);
  }

  protected void createWEBDescriptor(Element moduleRoot, IdeaModule ideaModule) {
    Element descriptorElement = createElement(COMPONENT_TAG, moduleRoot);
    descriptorElement.setAttribute(COMPONENT_NAME_PROPERTY, webModulePropertiesName);

    Element deploymentDescriptorElement =
      createElement(DEPLOYMENT_DESCRIPTOR_TAG, descriptorElement);
    deploymentDescriptorElement
      .setAttribute(DEPLOYMENT_DESCRIPTOR_NAME_PROPERTY, webDeploymentDescriptorName);
    String deploymentUrl = ideaModule.getProperty(DEPLOYMENT_DESCRIPTOR_URL_PROPERTY);
    if (shouldCopyContent) {
      deploymentUrl = ResourceUtil.insertModuleDirPrefix(ideaModule,
                                                         deploymentUrl);
    }

    deploymentDescriptorElement
      .setAttribute(DEPLOYMENT_DESCRIPTOR_URL_PROPERTY, FILE_FULL_URL_PREFIX + deploymentUrl);
    deploymentDescriptorElement
      .setAttribute(DEPLOYMENT_DESCRIPTOR_VERSION_PROPERTY, webDeploymentDescriptorVersion);
  }

  protected void createEARDescriptor(Element moduleRoot, IdeaModule ideaModule) {

    Element descriptorElement = createElement(COMPONENT_TAG, moduleRoot);
    descriptorElement.setAttribute(COMPONENT_NAME_PROPERTY, earModulePropertiesName);

    Element deploymentDescriptorElement =
      createElement(DEPLOYMENT_DESCRIPTOR_TAG, descriptorElement);
    deploymentDescriptorElement
      .setAttribute(DEPLOYMENT_DESCRIPTOR_NAME_PROPERTY, earDeploymentDescriptorName);
    String deploymentUrl = ideaModule.getProperty(DEPLOYMENT_DESCRIPTOR_URL_PROPERTY);
    if (shouldCopyContent) {
      deploymentUrl = ResourceUtil.insertModuleDirPrefix(ideaModule,
                                                         deploymentUrl);
    }
    deploymentDescriptorElement
      .setAttribute(DEPLOYMENT_DESCRIPTOR_URL_PROPERTY, FILE_FULL_URL_PREFIX + deploymentUrl);
    deploymentDescriptorElement
      .setAttribute(DEPLOYMENT_DESCRIPTOR_VERSION_PROPERTY, earDeploymentDescriptorVersion);
    createEARDependencies(descriptorElement, ideaModule);
  }

  protected void createEARDependencies(Element descriptorElement, IdeaModule ideaModule) {
    for (IdeaModule module : ideaModule.getReferencedModules()) {
      if (module.getType() == IdeaModule.MT_J2EE_EJB ||
          module.getType() == IdeaModule.MT_J2EE_WEB) {
        Element containerElement = createElement(CONTAINER_ELEMENT_TAG, descriptorElement);
        containerElement.setAttribute(CONTAINER_ELEMENT_TYPE_PROPERTY, containerTypeModule);
        containerElement.setAttribute(CONTAINER_ELEMENT_NAME_PROPERTY, module.getName());

        Element attribute = createElement(ATTRIBUTE_TAG, containerElement);
        attribute.setAttribute(ATTRIBUTE_NAME_PROPERTY, CONTAINER_ELEMENT_METHOD_VALUE);
        attribute.setAttribute(ATTRIBUTE_VALUE_PROPERTY, containerElementMethodValue);

        attribute = createElement(ATTRIBUTE_TAG, containerElement);
        attribute.setAttribute(ATTRIBUTE_NAME_PROPERTY, CONTAINER_ELEMENT_URI_VALUE);
        String ext = module.getType() == IdeaModule.MT_J2EE_EJB ? JAR_EXT : WAR_EXT;
        attribute.setAttribute(ATTRIBUTE_VALUE_PROPERTY, ideaModule.getName() + "." + ext);

        attribute = createElement(ATTRIBUTE_TAG, containerElement);
        attribute.setAttribute(ATTRIBUTE_NAME_PROPERTY, CONTAINER_ELEMENT_CONTEXT_ROOT_VALUE);
        attribute.setAttribute(ATTRIBUTE_VALUE_PROPERTY, containerElementContextRootValue);
      }
    }
  }

  private static Element createElement(String name, Element parent) {
    Element res = new Element(name);
    List content = parent.getContent();
    if (content == null) {
      content = new ArrayList();
      parent.setContent(content);
    }

    content.add(res);
    return res;
  }

  private static Element createElement(String name, Document document) {
    Element res = new Element(name);
    document.setRootElement(res);
    return res;
  }

  private static Document createDocument() {
    return new Document();
  }

  private void copyWebProjectSources(IdeaModule ideaModule, File moduleDir)
    throws IOException {
    IPath webContextRoot = ideaModule.getWebContextRoot();

    if (webContextRoot != null) {
      IClasspathEntry classpathEntry = null;
      try {
        Constructor ctr = ClasspathEntry.class.getConstructor(CLASS_PATH_ENTRY_PARAMS_30);
        if (ctr != null) {
          ctr.newInstance(IClasspathEntry.CPE_SOURCE,
                          IClasspathEntry.CPE_SOURCE,
                          webContextRoot,
                          new IPath[0],
                          new IPath[0],
                          webContextRoot,
                          webContextRoot,
                          webContextRoot,
                          Boolean.TRUE);
        } else if (ctr == null) {
          ctr = ClasspathEntry.class.getConstructor(CLASS_PATH_ENTRY_PARAMS_31);
          if (ctr != null) {
            ctr.newInstance(IClasspathEntry.CPE_SOURCE,
                            IClasspathEntry.CPE_SOURCE,
                            webContextRoot,
                            new IPath[0],
                            new IPath[0],
                            webContextRoot,
                            webContextRoot,
                            webContextRoot,
                            Boolean.TRUE,
                            accessRuleArray,
                            Boolean.TRUE,
                            classpathAttributeArray);
          }
        }
      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }

      //SourceCopier sourceCopier =
      //  new SourceCopier(ideaModule.getEclipseProject(), classpathEntry, moduleDir);
      //sourceCopier.copy();
    }
  }
}