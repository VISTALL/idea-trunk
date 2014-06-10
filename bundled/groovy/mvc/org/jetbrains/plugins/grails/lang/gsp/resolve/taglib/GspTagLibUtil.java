/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.grails.lang.gsp.resolve.taglib;

import com.intellij.jsp.impl.TldDescriptor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.PairProcessor;
import com.intellij.util.containers.HashMap;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author ilyas
 */
public class GspTagLibUtil {

  @NonNls public static final String DEFAULT_TAGLIB_PREFIX = "g";
  @NonNls public static final String BUILT_IN_TAG_NAME_PACKAGE = "org.codehaus.groovy.grails.web.taglib";
  @NonNls public final static String DYNAMIC_TAGLIB_PACKAGE = "org.codehaus.groovy.grails.plugins.web.taglib";
  @NonNls public static final String TAGLIB_NAME_POSTFIX = "TagLib";
  @NonNls public static final String NAMESPACE_FIELD = "namespace";

  private static final String GRAILS_TLD_FILE = GrailsUtils.webAppDir + "/" + GrailsUtils.webInfDir + "/tld/grails.tld";

  private GspTagLibUtil() {
  }

  public static PsiDirectory[] getTagLibDirectories(@Nullable PsiElement place) {
    Application application = ApplicationManager.getApplication();

    if (place == null) return new PsiDirectory[0];

    final PsiPackage defaultPackage = JavaPsiFacade.getInstance(place.getProject()).findPackage("");

    ArrayList<PsiDirectory> tagLibDirs = new ArrayList<PsiDirectory>();
    if (defaultPackage != null) {
      for (PsiDirectory dir : defaultPackage.getDirectories(place.getResolveScope())) {
        if (dir.getName().equals(GrailsUtils.TAGLIB_DIRECTORY)) {
          tagLibDirs.add(dir);
        }
      }
    }
    if (application.isUnitTestMode()) {
      tagLibDirs.add(getMockTagLibDirectory(place.getProject()));
    }

    return tagLibDirs.toArray(new PsiDirectory[tagLibDirs.size()]);
  }

  @Nullable
  private static VirtualFile getProjectGrailsTldFile(Project project) {
    final ModuleManager manager = ModuleManager.getInstance(project);
    final Module[] modules = manager.getModules();
    for (Module module : modules) {
      final VirtualFile root = GrailsUtils.findGrailsAppRoot(module);
      if (root != null) {
        return VirtualFileManager.getInstance().findFileByUrl(root.getUrl() + "/" + GRAILS_TLD_FILE);
      }
    }
    return null;
  }

  @Nullable
  public static XmlElementDescriptor getTldElementDescriptor(@NotNull String tagName, Project project) {
    TldDescriptor descriptor = getGrailsTldDescriptor(project);
    return descriptor == null ? null : descriptor.getElementDescriptor(tagName);
  }

  @Nullable
  public static TldDescriptor getGrailsTldDescriptor(@NotNull Project project) {
    final VirtualFile file = getProjectGrailsTldFile(project);
    if (file == null) return null;

    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    if (!(psiFile instanceof XmlFile)) return null;

    return getTldDescriptor((XmlFile)psiFile);
  }

  @Nullable
  public static TldDescriptor getTldDescriptor(@Nullable XmlFile xmlFile) {
    if (xmlFile == null) return null;

    final XmlDocument document = xmlFile.getDocument();
    if (document == null) return null;

    final PsiMetaData metaData = document.getMetaData();
    if (!(metaData instanceof TldDescriptor)) return null;

    return (TldDescriptor)metaData;
  }

  @Nullable
  private static PsiDirectory getMockTagLibDirectory(Project project) {
    String path = FileUtil.toSystemIndependentName(PathManager.getHomePath()) + "/svnPlugins/groovy/mvc-testdata/mockTagLib";
    VirtualFile fileByUrl = VirtualFileManager.getInstance().findFileByUrl("file://" + path);
    assert fileByUrl != null;
    return PsiManager.getInstance(project).findDirectory(fileByUrl);
  }

  public static PsiClass[] getCustomTagLibClasses(@NotNull PsiElement place, @Nullable String tagLibPrefix) {
    PsiDirectory[] libDirectories = getTagLibDirectories(place);
    String currentLibName = tagLibPrefix == null || tagLibPrefix.trim().length() == 0 ? DEFAULT_TAGLIB_PREFIX : tagLibPrefix.trim();
    if (libDirectories == null) return PsiClass.EMPTY_ARRAY;
    ArrayList<PsiClass> tagLibClasses = new ArrayList<PsiClass>();

    for (PsiDirectory libDirectory : libDirectories) {
      for (PsiClass aClass : getTagLibClasses(libDirectory)) {
        if (tagLibPrefix == null || currentLibName.equals(getPrefixByTagLibClass(aClass))) {
          tagLibClasses.add(aClass);
        }
      }
    }

    return tagLibClasses.toArray(new PsiClass[tagLibClasses.size()]);
  }

  private static PsiClass[] getTagLibClasses(PsiDirectory libDirectory) {
    if (libDirectory == null) return PsiClass.EMPTY_ARRAY;
    Project project = libDirectory.getProject();
    final PsiManager manager = PsiManager.getInstance(project);
    final ArrayList<PsiClass> classes = new ArrayList<PsiClass>();

    ContentIterator iterator = new ContentIterator() {
      public boolean processFile(VirtualFile fileOrDir) {
        PsiFile gfile = manager.findFile(fileOrDir);
        if (gfile instanceof GroovyFile) {
          PsiClass[] fileClasses = ((GroovyFile)gfile).getClasses();
          classes.addAll(Arrays.asList(fileClasses));
        }
        return true;
      }
    };

    ProjectRootManager.getInstance(project).getFileIndex().iterateContentUnderDirectory(libDirectory.getVirtualFile(), iterator);
    return classes.toArray(new PsiClass[classes.size()]);
  }

  public static PsiClass[] getDynamicTagLibClasses(PsiElement place) {
    PsiPackage psiPackage = JavaPsiFacade.getInstance(place.getProject()).findPackage(DYNAMIC_TAGLIB_PACKAGE);
    if (psiPackage == null) return PsiClass.EMPTY_ARRAY;
    ArrayList<PsiClass> tagLibClasses = new ArrayList<PsiClass>();
    for (PsiClass tagLibClass : psiPackage.getClasses(place.getResolveScope())) {
      String name = tagLibClass.getName();
      if (name != null && name.endsWith(TAGLIB_NAME_POSTFIX)) {
        tagLibClasses.add(tagLibClass);
      }
    }
    return tagLibClasses.toArray(new PsiClass[tagLibClasses.size()]);
  }

  private static final Map<String, String> TAG_NAME_TO_CLASS_NAME = new HashMap<String, String>();

  static {
    TAG_NAME_TO_CLASS_NAME.put("renderInput", "RenderInputTag");
    TAG_NAME_TO_CLASS_NAME.put("each", "GroovyEachTag");
    TAG_NAME_TO_CLASS_NAME.put("if", "GroovyIfTag");
    TAG_NAME_TO_CLASS_NAME.put("else", "GroovyElseTag");
    TAG_NAME_TO_CLASS_NAME.put("elseif", "GroovyElseIfTag");
    TAG_NAME_TO_CLASS_NAME.put("findAll", "GroovyFindAllTag");
    TAG_NAME_TO_CLASS_NAME.put("collect", "GroovyCollectTag");
    TAG_NAME_TO_CLASS_NAME.put("grep", "GroovyGrepTag");
    TAG_NAME_TO_CLASS_NAME.put("while", "GroovyWhileTag");
    TAG_NAME_TO_CLASS_NAME.put("def", "GroovyDefTag");
  }

  @Nullable
  public static PsiClass getBuiltInTagByName(@NotNull String name, PsiElement place) {
    final String className = TAG_NAME_TO_CLASS_NAME.get(name);
    if (className == null) return null;

    String tagClassQualifiedName = BUILT_IN_TAG_NAME_PACKAGE + "." + className;
    return JavaPsiFacade.getInstance(place.getProject()).findClass(tagClassQualifiedName, place.getResolveScope());
  }

  @Nullable
  public static String getPrefixByTagLibClass(PsiClass clazz) {
    PsiField field = clazz.findFieldByName(NAMESPACE_FIELD, true);
    if (field == null) return DEFAULT_TAGLIB_PREFIX;
    PsiModifierList modifierList = field.getModifierList();
    if (modifierList != null && modifierList.hasExplicitModifier(PsiModifier.STATIC)) {
      PsiClassType stringType = PsiType.getJavaLangString(field.getManager(), field.getResolveScope());
      if (!(field instanceof GrField)) return DEFAULT_TAGLIB_PREFIX;

      GrExpression initializer = ((GrField)field).getInitializerGroovy();
      if (initializer == null) return DEFAULT_TAGLIB_PREFIX;

      PsiType initType = initializer.getType();
      if ((initType == null) || !stringType.isAssignableFrom(initType)) return DEFAULT_TAGLIB_PREFIX;

      if (!(initializer instanceof GrLiteral)) return DEFAULT_TAGLIB_PREFIX;

      String text = initializer.getText();
      while (text.charAt(0) == '\'' || text.charAt(0) == '\"') {
        text = text.substring(1);
      }
      while (text.charAt(text.length() - 1) == '\'' || text.charAt(text.length() - 1) == '\"') {
        text = text.substring(0, text.length() - 1);
      }
      return text.trim().length() == 0 ? DEFAULT_TAGLIB_PREFIX : text.trim();
    }
    return DEFAULT_TAGLIB_PREFIX;
  }


  public static String[] getKnownPrefixes(PsiElement place, boolean withDefault) {
    ArrayList<String> prefixes = new ArrayList<String>();

    for (PsiClass aClass : getCustomTagLibClasses(place, null)) {
      String prefix = getPrefixByTagLibClass(aClass);
      if (!prefixes.contains(prefix)) {
        prefixes.add(prefix);
      }
    }
    if (withDefault && !prefixes.contains(DEFAULT_TAGLIB_PREFIX)) {
      prefixes.add(DEFAULT_TAGLIB_PREFIX);
    }
    if (!withDefault && prefixes.contains(DEFAULT_TAGLIB_PREFIX)) {
      prefixes.remove(DEFAULT_TAGLIB_PREFIX);
    }
    return prefixes.toArray(new String[prefixes.size()]);
  }

  public static List<PsiClass> getTagLibClasses(String prefix, final PsiElement place) {
    List<PsiClass> taglibClasses = new ArrayList<PsiClass>();
    taglibClasses.addAll(Arrays.asList(getCustomTagLibClasses(place, prefix)));
    if (DEFAULT_TAGLIB_PREFIX.equals(prefix)) {
      taglibClasses.addAll(Arrays.asList(getDynamicTagLibClasses(place)));
    }
    return taglibClasses;
  }

  public static void processBuiltInTagClasses(PsiElement place, PairProcessor<String, PsiClass> processor) {
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(place.getProject());
    for (final String tagName : TAG_NAME_TO_CLASS_NAME.keySet()) {
      final PsiClass builtInClass = facade.findClass(BUILT_IN_TAG_NAME_PACKAGE + "." + TAG_NAME_TO_CLASS_NAME.get(tagName), place.getResolveScope());
      if (builtInClass != null && !processor.process(tagName, builtInClass)) {
        return;
      }
    }
  }
}
