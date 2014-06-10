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

package com.advancedtools.webservices.utils;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.index.FileBasedWSIndex;
import com.advancedtools.webservices.index.WSIndexEntry;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.jsp.WebDirectoryElement;
import com.intellij.psi.xml.XmlChildRole;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.intellij.util.containers.ArrayListSet;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @by maxim
 */
public class DeployUtils {
  public static Logger LOG = Logger.getInstance("webservicesplugin.enablewssupport");

  public static boolean isAcceptableMethod(PsiMethod method) {
    return method.hasModifierProperty("public") && !method.isConstructor();
  }

  public static String checkAccessibleClass(PsiClass myClass) {
    boolean classIsPublic = checkInstanciatableClass(myClass);

    if (!classIsPublic) {
      return WSBundle.message("class.should.be.instanciatable.validation.problem");
    }

    if (myClass.isAnnotationType()) {
      return WSBundle.message("class.should.not.be.enum.annotation.type.validation.problem");
    }
    return null;
  }

  public static VirtualFile findFileByPath(Module module, final String[] pathComponents,boolean findInContentRoot) {
    final ModuleRootManager moduleManager = ModuleRootManager.getInstance(module);

    if (findInContentRoot && EnvironmentFacade.getInstance().isWebModule(module)) {
      WebDirectoryElement element = EnvironmentFacade.getInstance().findWebDirectoryByElement(buildPath(pathComponents,pathComponents.length), module);
      if (element != null) return element.getOriginalVirtualFile();
      return null;
    }

    final VirtualFile[] sourceRoots = findInContentRoot ? moduleManager.getContentRoots() : moduleManager.getSourceRoots();
    if (sourceRoots.length == 0) return null;

    for(VirtualFile file:sourceRoots) {
      VirtualFile current = file;

      for(String pathComponent:pathComponents) {
        current = current.findChild(pathComponent);
        if (current == null) break;
      }
      if (current != null) {
        return current;
      }
    }
    return null;
  }

  private static String buildPath(String[] pathComponents, int last) {
    StringBuilder pathBuilder = new StringBuilder();
    for(int i = 0; i < last; ++i) {
      final String pathComponent = pathComponents[i];
      if (pathBuilder.length() > 0) pathBuilder.append('/');
      pathBuilder.append(pathComponent);
    }
    return pathBuilder.toString();
  }

  public static void addFileToModuleFromTemplate(final @NotNull Module module, final @NotNull String[] pathComponents,
                                                 final @NonNls @NotNull String templateName,
                                                 final boolean createInContentRoot) {
    addFileToModuleFromTemplate(
      module,
      pathComponents, LibUtils.getResourcesStream(templateName),
      createInContentRoot,
      false
    );
  }

  public static VirtualFile addFileToModuleFromTemplate(final @NotNull Module module, final @NotNull String[] pathComponents,
                                                 final @NotNull InputStream templateInputStream,
                                                 final boolean createInContentRoot,
                                                 final boolean overwriteExisitingFile
                                                 ) {
    try {
      final ModuleRootManager moduleManager = ModuleRootManager.getInstance(module);

      VirtualFile[] sourceRoots;

      if (createInContentRoot) {
        if (EnvironmentFacade.getInstance().isWebModule(module)) {
          final WebDirectoryElement element = EnvironmentFacade.getInstance().findWebDirectoryByElement("/", module);

          if (element != null) {
            final List<VirtualFile> list = element.getOriginalVirtualFiles();
            sourceRoots = list.toArray(new VirtualFile[list.size()]);
            if (sourceRoots.length == 0) sourceRoots = moduleManager.getContentRoots();
          } else {
            sourceRoots = moduleManager.getContentRoots();
          }
        } else {
          sourceRoots = moduleManager.getContentRoots();
        }
      } else {
        sourceRoots = moduleManager.getSourceRoots();
      }
      if (sourceRoots.length == 0) return null;

      final VirtualFile[] sourceRoots1 = sourceRoots;
      return ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
        public VirtualFile compute() {
          VirtualFile[] fileComponents = new VirtualFile[pathComponents.length];
          int i = 0;
          final EnvironmentFacade environmentFacade = EnvironmentFacade.getInstance();

          if (createInContentRoot && environmentFacade.isWebModule(module)) {
            for(i = fileComponents.length - 1; i >=0; --i) {
              final WebDirectoryElement element = environmentFacade.findWebDirectoryByElement(buildPath(pathComponents, i + 1), module);
              if (element != null) {
                final VirtualFile file = element.getOriginalVirtualFile();
                if (file == null) continue;
                fileComponents[i] = file;
                i++;
                break;
              }
            }
          } else {
            for(VirtualFile file: sourceRoots1) {
              for(i = 0; i < fileComponents.length; ++i) {
                fileComponents[i] = (i == 0 ? file:fileComponents[i - 1]).findChild(pathComponents[i]);
                if (fileComponents[i] == null) break;
              }
            }
          }

          try {
            boolean alreadyHasServicesXml = i == fileComponents.length;
            if (alreadyHasServicesXml && overwriteExisitingFile) alreadyHasServicesXml = false;

            for(int j = Math.max(i,0); j < fileComponents.length; ++j) {
              final VirtualFile parentVirtualFile = (j == 0 ? sourceRoots1[0] : fileComponents[j - 1]);
              fileComponents[j] =
                (j +1 != fileComponents.length) ?
                  parentVirtualFile.createChildDirectory(null,pathComponents[j]) :
                  parentVirtualFile.createChildData(null,pathComponents[j]);
            }

            if (fileComponents[fileComponents.length - 1] != null && !alreadyHasServicesXml) {
              OutputStream outputStream = fileComponents[fileComponents.length - 1].getOutputStream(this);
              FileUtil.copy(templateInputStream, outputStream);
              outputStream.flush();
              outputStream.close();

              return fileComponents[fileComponents.length - 1];
            }
          } catch (IOException e) {
            LOG.error(e);
          }

          return null;
        }
      });
    } finally {
      try { templateInputStream.close(); } catch (IOException ex) {}
    }
  }

  public static String checkAccessibleClassPrerequisites(final Project project, final PsiClass myClass) {

    String s = checkAccessibleClass(myClass);

    if (s != null) {
      return s;
    }

    return checkIfClassIsUpToDate(project, myClass);
  }

  public static String checkIfClassIsUpToDate(Project project, PsiClass myClass) {
    final Module moduleForFile = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(
      myClass.getContainingFile().getVirtualFile()
    );

    VirtualFile compilerOutputPath = EnvironmentFacade.getInstance().getCompilerOutputPath(moduleForFile);
    VirtualFile relativeFile = EnvironmentFacade.getInstance().findRelativeFile(myClass.getQualifiedName().replace('.', '/') + ".class", compilerOutputPath);

    if (relativeFile==null) {
      return MessageFormat.format(WSBundle.message("class.is.not.compiled.deployment.problem"), myClass.getName());
    } else if (relativeFile.getModificationStamp() < myClass.getContainingFile().getModificationStamp()) {
      return MessageFormat.format(WSBundle.message("class.was.not.recompiled.after.changes.please.compile.before.proceeding.deploymnet.problem"), myClass.getName());
    }

    return null;
  }

  public static boolean checkInstanciatableClass(PsiClass myClass) {
    boolean classIsPublic = false;

    final PsiModifierList modifierList = myClass.getModifierList();

    if (modifierList != null &&
        modifierList.hasModifierProperty("public") &&
        (myClass.getParent() instanceof PsiFile || modifierList.hasModifierProperty(PsiModifier.STATIC)) &&
        !myClass.isInterface()
        ) {
//      if (myClass.getModifierList().hasModifierProperty("abstract")) {
//        return "java.util.Calendar".equals(myClass.getQualifiedName()); // WTF?
//      }

      if (myClass.isEnum()) return true;
      
      final PsiMethod[] constructors = myClass.getConstructors();

      if (constructors.length==0) classIsPublic = true;
      else {
        for (PsiMethod constructor : constructors) {
          if (constructor.getParameterList().getParameters().length == 0) {
            if (constructor.hasModifierProperty("public")) {
              classIsPublic = true;
            }
            break;
          }
        }
      }
    }
    return classIsPublic;
  }

  public static boolean isAcceptableField(PsiField f) {
    return f.hasModifierProperty("public");
  }

  static void findReferencedTypesForVars(PsiVariable[] vars, Set<PsiClass> visited, Set<String> unresolved) {
    for (PsiVariable var : vars) {
      findReferencedTypesForType(var.getType(), visited, unresolved);
    }
  }

  static void findReferencedTypesForType(PsiType parameterType, Set<PsiClass> visited, Set<String> unresolved) {
    findReferencedTypesForType(parameterType, visited, unresolved, false);
  }

  static void findReferencedTypesForType(PsiType parameterType, Set<PsiClass> visited, Set<String> unresolved, boolean findInMethods) {
    if (parameterType instanceof PsiArrayType) {
      parameterType = ((PsiArrayType) parameterType).getComponentType();
    }

    if (parameterType instanceof PsiClassType) {
      final PsiClass psiClass = ((PsiClassType) parameterType).resolve();

      if (psiClass != null) {
        if (!visited.contains(psiClass)) {
          visited.add(psiClass);
          final String qualifiedName = psiClass.getQualifiedName();

          if (qualifiedName != null &&
              !qualifiedName.startsWith("javax.") &&
              !qualifiedName.startsWith("java.")
             ) {
            findReferencedTypesForVars(psiClass.getFields(), visited, unresolved);
            if (findInMethods) {
              findReferencedTypesForMethods(psiClass.getMethods(), visited, unresolved);
            }
          }
        }
      } else {
        unresolved.add(parameterType.getCanonicalText());
      }
    }
  }

  static void findReferencedTypesForMethods(PsiMethod[] methods, Set<PsiClass> visited, Set<String> unresolved) {
    for (PsiMethod method : methods) {
      findReferencedTypesForMethod(method, visited, unresolved);
    }
  }

  static void findReferencedTypesForMethod(PsiMethod method, Set<PsiClass> visited, Set<String> unresolved) {
    if (method != null) {
      if (method.getReturnType() != null) {
        findReferencedTypesForType(method.getReturnType(), visited, unresolved);
      }

      for (PsiParameter param : method.getParameterList().getParameters()) {
        findReferencedTypesForType(param.getType(), visited, unresolved);
      }
    }
  }

  private static boolean builtinClass(PsiClass psiClass) {
    final String qualifiedName = psiClass.getQualifiedName();
    PsiClass collectionsClass;

    return qualifiedName != null &&
      ( qualifiedName.equals("java.util.Calendar") ||
        qualifiedName.equals("java.math.BigDecimal") ||
        qualifiedName.equals("java.lang.Float") ||
        qualifiedName.equals("java.lang.Double") ||
        qualifiedName.equals("java.lang.Long") ||
        qualifiedName.equals("java.lang.Integer") ||
        qualifiedName.equals("java.lang.Short") ||
        qualifiedName.equals("java.lang.Character") ||
        qualifiedName.equals("java.lang.Byte") ||
        qualifiedName.equals("java.math.BigInteger") ||
        qualifiedName.equals("javax.xml.namespace.QName") ||
        qualifiedName.equals("java.lang.String") ||
        qualifiedName.equals("java.util.Hashtable") ||
        qualifiedName.equals("javax.activation.DataHandler") ||
        ( (collectionsClass = EnvironmentFacade.getInstance().findClass("java.util.Collection", psiClass.getProject(), psiClass.getResolveScope())) != null &&
          ( psiClass == collectionsClass ||
            psiClass.isInheritor(collectionsClass, true)
          )
        )
      );
  }

  public static String getDeploymentProblemForType(PsiType type) {
    HashSet<PsiClass> visitedSet = new HashSet<PsiClass>();
    HashSet<String> unresolvedSet = new HashSet<String>();
    findReferencedTypesForType(type,visitedSet, unresolvedSet);
    return findDeploymentProblem(visitedSet, null, unresolvedSet);
  }

  public static void removeFromConfigFile(String[] pathComponents, boolean at, final Module module, final Processor<XmlTag> processor) {
    if (!EnvironmentFacade.getInstance().isWebModule(module)) return;

    final VirtualFile fileByPath = findFileByPath(module, pathComponents, at);

    if (fileByPath != null) {
      final Runnable action = new Runnable() {
        public void run() {
          PsiManager instance = PsiManager.getInstance(module.getProject());
          PsiFile file = instance.findFile(fileByPath);

          try {
            if (file instanceof XmlFile) {
              final XmlTag rootTag = ((XmlFile) file).getDocument().getRootTag();
              if (rootTag != null) {
                for(XmlTag t:rootTag.getSubTags()) {
                  if (processor.process(t)) t.delete();
                }
              }
            }
          } catch(IncorrectOperationException ex) {
            LOG.error(ex);
          }
        }
      };

      CommandProcessor.getInstance().executeCommand(
        module.getProject(),
        new Runnable() {
          public void run() {
            ApplicationManager.getApplication().runWriteAction(action);
          }
        },
        "Remove from web services deployment descriptor",
        null
      );
    }
  }

  public static void addToConfigFile(String[] pathComponents, boolean at, final Module module, final @NonNls String fragmentToInsert, WSEngine engine) {
    if (!EnvironmentFacade.getInstance().isWebModule(module)) return;

    VirtualFile fileByPath = findFileByPath(module, pathComponents, at);

    if (fileByPath == null) {
      engine.doAdditionalWSServerSetup(module);
      LibUtils.doFileSystemRefresh();
      fileByPath = findFileByPath(module, pathComponents, at);
    }

    if (fileByPath != null) {
      final VirtualFile fileByPath1 = fileByPath;
      final Runnable action = new Runnable() {
        public void run() {
          PsiManager instance = PsiManager.getInstance(module.getProject());
          PsiFile file = instance.findFile(fileByPath1);

          try {
            final PsiFile fileFromText = EnvironmentFacade.getInstance().createFileFromText("a.xml", fragmentToInsert, instance.getProject());
            if (fileFromText instanceof XmlFile && file instanceof XmlFile) {
              final XmlTag rootTag = ((XmlFile) file).getDocument().getRootTag();
              if (rootTag != null) {
                rootTag.add(((XmlFile)fileFromText).getDocument().getRootTag());
                //System.out.println(rootTag.getText());
//                CodeStyleManager.getInstance(instance).reformat(rootTag);
              }
            }
          } catch(IncorrectOperationException ex) {
            LOG.error(ex);
          }
        }
      };

      CommandProcessor.getInstance().executeCommand(
        module.getProject(),
        new Runnable() {
          public void run() {
            ApplicationManager.getApplication().runWriteAction(action);
          }
        },
        "Add to web services deployment descriptor",
        null
      );
    }
  }

  public static PsiClass getCurrentClassFromDataContext(DataContext dataContext) {
    Object file = dataContext.getData("psi.File");
    PsiJavaFile psiFile = (file instanceof PsiJavaFile ? (PsiJavaFile) file : null);
    PsiClass[] classes = psiFile != null ? psiFile.getClasses() : PsiClass.EMPTY_ARRAY;
    return classes.length > 0 ? classes[0] : null;
  }

  public static void processTagsInConfigFile(String[] pathComponents, boolean at, Module module, Processor<XmlTag> processor) {
    if (!EnvironmentFacade.getInstance().isWebModule(module)) return;
    VirtualFile fileByPath = findFileByPath(module, pathComponents, at);

    if (fileByPath != null) {
      PsiManager instance = PsiManager.getInstance(module.getProject());
      PsiFile file = instance.findFile(fileByPath);
      final XmlDocument document = ((XmlFile) file).getDocument();
      final XmlTag tag = document != null ? document.getRootTag():null;

      if (tag != null) {
        for(XmlTag t: tag.getSubTags()) {
          if (!processor.process(t)) {
            return;
          }
        }
      }
    }
  }

  public static void updateWebXml(final Project project, VirtualFile result, @NotNull String deploymentServletName) {
    try {
      boolean seenWsServlet;

      final XmlFile file = (XmlFile) PsiManager.getInstance(project).findFile(result);
      final XmlTag rootTag = file.getDocument().getRootTag();
      XmlTag[] tags = rootTag.findSubTags("servlet");
      seenWsServlet = LibUtils.findServletWithName(tags, deploymentServletName) != null;

      if (!seenWsServlet) {
        String s = loadWSWebXml(deploymentServletName);
        XmlTag tagFromText = EnvironmentFacade.getInstance().createTagFromText(s, project);
        final XmlTag[] nestedTags = tagFromText.getSubTags();

        CommandProcessor.getInstance().executeCommand(
          project,
          new Runnable() {
            public void run() {
              ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                  try {
                    for (XmlTag nestedTag : nestedTags) {
                      if (nestedTag.getName().equals("servlet")) {
                        final XmlElementDescriptor tagDescriptor = rootTag.getDescriptor();
                        final XmlElementDescriptor descriptor = tagDescriptor != null ? tagDescriptor.getElementDescriptor(nestedTag, null):null;

                        if (descriptor == null) {
                          rootTag.add(nestedTag);
                          continue;
                        }

                        final StringBuilder tagText = new StringBuilder();

                        tagText.append("<").append(
                          XmlChildRole.START_TAG_NAME_FINDER.findChild(
                            nestedTag.getNode()
                          ).getText()
                        ).append(">\n");

                        for(XmlElementDescriptor e:descriptor.getElementsDescriptors(nestedTag)) {
                          final XmlTag tag = nestedTag.findFirstSubTag(e.getName());
                          if (tag != null) tagText.append(tag.getText() + "\n");
                        }

                        tagText.append("</").append(
                          XmlChildRole.CLOSING_TAG_NAME_FINDER.findChild(
                            nestedTag.getNode()
                          ).getText()
                        ).append(">");

                        rootTag.add(EnvironmentFacade.getInstance().createTagFromText(tagText.toString(), project));
                      } else {
                        rootTag.add(nestedTag);
                      }
                    }
                  } catch (IncorrectOperationException e) {
                    LOG.error(e);
                  }
                }
              });
            }
          },
          "Edit web.xml",
          null
        );
      }
    } catch (IOException e) {
      LOG.error(e);
    } catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  public static String loadWSWebXml(final @NotNull String deploymentServletName) throws IOException {
    LineNumberReader reader = new LineNumberReader(
      new InputStreamReader(
        LibUtils.getResourcesStream( deploymentServletName + ".web.xml")
      )
    );

    final StringBuffer xmlText = new StringBuffer();

    LibUtils.doScanFile(reader, new LibUtils.FileProcessor() {
      final String urlPattern = "<url-pattern>";
      final Pattern p = Pattern.compile("(.*" + urlPattern + ")(/services)(.*)");
      public void fileScanningEnded() throws IOException {}

      public boolean process(String s) throws IOException {
        if (s.indexOf(urlPattern) != -1) {
          final Matcher m = p.matcher(s);
          if (m.matches()) {
            final String escapedPrefix = EnvironmentFacade.escapeXmlString(WebServicesPluginSettings.getInstance().getWebServicesUrlPathPrefix());
            xmlText.append(m.group(1)).append(escapedPrefix).append(m.group(3));
            return true;
          }
        }
        xmlText.append(s);
        return true;
      }
    });

    return xmlText.toString();
  }

  public static String determineWhereToPlaceTheFileUnderWebInf(Module module, @NonNls String baseWsdlFileName) {
    String outputFileName = null;

    final WebDirectoryElement webInf = EnvironmentFacade.getInstance().findWebDirectoryByElement("/WEB-INF", module);
    if (webInf != null) {
      final VirtualFile originalVirtualFile = webInf.getOriginalVirtualFile();
      if (originalVirtualFile != null) outputFileName = originalVirtualFile.getPath() + File.separatorChar + baseWsdlFileName;
    }

    @NonNls String wsdlFileName = "WEB-INF" + File.separatorChar + baseWsdlFileName;

    if (outputFileName == null) {
      WebDirectoryElement rootWebDirectory = EnvironmentFacade.getInstance().findWebDirectoryByElement("/", module);
      if (rootWebDirectory != null) {
        final VirtualFile originalVirtualFile = rootWebDirectory.getOriginalVirtualFile();
        if (originalVirtualFile != null) outputFileName = originalVirtualFile.getPath() + wsdlFileName;
      }
    }

    if (outputFileName == null) {
      outputFileName = ModuleRootManager.getInstance(module).getContentRoots()[0].getPath() + File.separatorChar + wsdlFileName;
    }
    return outputFileName;
  }

  public static <T> boolean isMethodAnnotatedAs(PsiMethod method, String name, String attribute, Class<T> type, T value) {
    final PsiAnnotation annotation = AnnotationUtil.findAnnotation(method, name);
    if (annotation == null) return false;
    final String val = AnnotationModelUtil.getObjectValue(annotation, attribute, type).getStringValue();
    //TODO remove dirty hack and make 'equals' relevant for all primitive types
    return val != null && val.equals(value.toString());
  }

  public static boolean canBeWebMethod(PsiMember member) {
    if (! (member instanceof PsiMethod)) return false;

    final PsiMethod method = (PsiMethod)member;
    if (isMethodAnnotatedAs(method, JWSDPWSEngine.wsWebMethod, "exclude", Boolean.TYPE, false)) return true;
    if (isMethodAnnotatedAs(method, JWSDPWSEngine.wsWebMethod, "exclude", Boolean.TYPE, true)) return false;

    PsiClass psiClass = method.getContainingClass();
    final WSIndexEntry[] entries = FileBasedWSIndex.getInstance().getWsEntries(member.getContainingClass());

    if (entries.length > 0 && entries[0].hasNonImplicitRef(member)) return true;
    if (psiClass == null || !isAcceptableMethod(method)) return false;
    if (AnnotationUtil.findAnnotation(psiClass, JWSDPWSEngine.wsClassesSet) == null ) {
      return isAcceptableMethod(method);
    }

    return !isMethodAnnotatedAsExist(JWSDPWSEngine.wsWebMethod, psiClass.getMethods());
  }

  private static boolean isMethodAnnotatedAsExist(String annoFQN, PsiMethod... methods) {
    for (PsiMethod method : methods) {
      if (AnnotationUtil.findAnnotation(method, annoFQN) != null) {
        return true;
      }
    }
    return false;
  }

  public interface DeploymentProcessor {
    void processMethod(PsiMethod method, String problem, List<String> nonelementaryTypes);
  }

  public static void processClassMethods(PsiMethod[] methods, DeploymentProcessor processor) {
    for (PsiMethod method : methods) {
      processMethod(method, processor);
    }
  }

  public static void processMethod(PsiMethod method, DeploymentProcessor processor) {
    if (DeployUtils.isAcceptableMethod(method)) {
      HashSet<PsiClass> visitedSet = new HashSet<PsiClass>();
      HashSet<String> unresolvedSet = new HashSet<String>();
      List<String> nonelementaryTypes = new LinkedList<String>();

      DeployUtils.findReferencedTypesForVars(
        method.getParameterList().getParameters(),
        visitedSet,
        unresolvedSet
      );

      DeployUtils.findReferencedTypesForType(method.getReturnType(),visitedSet, unresolvedSet);
      String problem = findDeploymentProblem(visitedSet, nonelementaryTypes, unresolvedSet);

      processor.processMethod(method, problem,nonelementaryTypes);
    } else {
      processor.processMethod(
        method,
        method.isConstructor() ? WSBundle.message("method.is.contructor.validation.message") : WSBundle.message("method.is.not.public.validation.message"),
        null
      );
    }
  }

  private static String findDeploymentProblem(HashSet<PsiClass> visitedSet, List<String> nonelementaryTypes, HashSet<String> unresolvedSet) {
    String problem = null;

    for (PsiClass clazz:visitedSet) {
      if (builtinClass(clazz)) continue;
      if (!checkInstanciatableClass(clazz)) {
        problem = MessageFormat.format(WSBundle.message("class.not.public.or.does.not.allow.instantiation.validation.message"), clazz.getQualifiedName());
        break;
      }
      if (nonelementaryTypes != null) nonelementaryTypes.add(clazz.getQualifiedName());
    }

    if (problem == null && unresolvedSet.size() > 0) {
      problem = MessageFormat.format(WSBundle.message("class.not.found.validation.message"), unresolvedSet.iterator().next());
    }
    return problem;
  }

  static void removeBuildInClasses(Set<PsiClass> visited) {
    Collection<PsiClass> buildin = new ArrayList<PsiClass>();
    for (PsiClass clazz : visited) {
      if (builtinClass(clazz)) buildin.add(clazz);
    }
    visited.removeAll(buildin);
  }

  public static PsiClass[] searchReferencedTypesForClass(PsiType type) {
    final Set<PsiClass> visited = new ArrayListSet<PsiClass>();
    final Set<String> unresolved = new ArrayListSet<String>();
    findReferencedTypesForType(type, visited, unresolved, true);
    removeBuildInClasses(visited);
    return visited.toArray(new PsiClass[visited.size()]);
  }
}
