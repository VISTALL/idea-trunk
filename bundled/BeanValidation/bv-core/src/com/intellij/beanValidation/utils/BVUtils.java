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

package com.intellij.beanValidation.utils;

import com.intellij.beanValidation.constants.BvAnnoConstants;
import static com.intellij.beanValidation.constants.BvCommonConstants.BEAN_VALIDATION_CONFIG_FILENAME;
import com.intellij.beanValidation.facet.BeanValidationFacet;
import com.intellij.beanValidation.model.xml.Bean;
import com.intellij.beanValidation.model.xml.ValidationConfig;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Function;
import com.intellij.util.Query;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class BVUtils {

  private static final Key<CachedValue<Collection<PsiClass>>> MODULE_SCOPE_CONSTRAINT_ANNOTATIONS =
    new Key<CachedValue<Collection<PsiClass>>>("MODULE_SCOPE_CONSTRAINT_ANNOTATIONS");

  private BVUtils() {
  }


  @NotNull
  public static Collection<PsiClass> getConstraintClasses(@NotNull final Module module) {
    return JamCommonUtil
      .getAnnotatedTypes(module, MODULE_SCOPE_CONSTRAINT_ANNOTATIONS, BvAnnoConstants.CONSTRAINT);
  }

  @NotNull
  public static Collection<String> getQualifiedNames(final Iterable<PsiClass> annotations) {
    return ContainerUtil.mapNotNull(annotations, new Function<PsiClass, String>() {
      public String fun(PsiClass psiClass) {
        return psiClass.getQualifiedName();
      }
    });
  }


  public static boolean isBeanValidationFacetDefined(Module module) {
    if (module == null) return false;

    if (isModuleContainsBeanValidationFacet(module)) return true;

    // check module dependencies
    for (Module depModule : JamCommonUtil.getAllModuleDependencies(module)) {
      if (isModuleContainsBeanValidationFacet(depModule)) return true;
    }

    return false;
  }

  public static boolean isModuleContainsBeanValidationFacet(final Module module) {
    return BeanValidationFacet.getInstance(module) != null;
  }

  public static List<String> getAnnotations(Class clazz) {
    List<String> annotations = new ArrayList<String>();
    try {
      for (Field field : clazz.getFields()) {
        final Object value = field.get(null);
        if (value instanceof String) {
          annotations.add((String)value);
        }
      }
    }
    catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
    return annotations;
  }

  public static boolean isInLibrary(@Nullable final PsiElement psiElement) {
    if (psiElement == null) return false;
    final PsiFile psiFile = psiElement.getContainingFile();
    if (psiFile == null) return false;
    final VirtualFile virtualFile = psiFile.getVirtualFile();
    if (virtualFile == null) return false;
    return ProjectRootManager.getInstance(psiElement.getProject()).getFileIndex().isInLibraryClasses(virtualFile);
  }

  public static Collection<PsiClass> getConstraintValidators(@NotNull Module module) {
    final GlobalSearchScope searchScope = GlobalSearchScope.moduleWithLibrariesScope(module);
    final PsiClass psiClass =
      JavaPsiFacade.getInstance(module.getProject()).findClass(BvAnnoConstants.CONSTRAINT_VALIDATOR, searchScope);
    if (psiClass == null) {
      return Collections.emptyList();
    }
    final Query<PsiClass> query = ClassInheritorsSearch.search(psiClass, searchScope, true, true, false);
    return query.findAll();    
  }

  @Nullable
  public static PsiClass getBeanClass(ConvertContext context) {
    final Bean bean = context.getInvocationElement().getParentOfType(Bean.class, true);
    return bean == null ? null : bean.getClassAttr().getValue();
  }

  @Nullable
  public static XmlFile getValidationXml(Module module) {
    for (VirtualFile root : ModuleRootManager.getInstance(module).getSourceRoots()) {
      final VirtualFile metaInf = root.findChild("META-INF");
      if (metaInf != null) {
        final VirtualFile config = metaInf.findChild(BEAN_VALIDATION_CONFIG_FILENAME);
        if (config != null) {
          final PsiFile file = PsiManager.getInstance(module.getProject()).findFile(config);
          if (file instanceof XmlFile) {
            return (XmlFile)file;
          }
        }
      }
    }
    return null;
  }

  private static final XmlFile[] EMPTY_ARRAY_OF_XML_FILES = new XmlFile[0];
  public static XmlFile[] getConstraintFiles(Module module) {
    final XmlFile config = getValidationXml(module);
    if (config != null) {
      final DomFileElement<ValidationConfig> domFileElement =
        DomManager.getDomManager(module.getProject()).getFileElement(config, ValidationConfig.class);
      if (domFileElement != null) {
        final ValidationConfig root = domFileElement.getRootElement();
        List<XmlFile> files = new ArrayList<XmlFile>();
        for (GenericDomValue<XmlFile> domValue : root.getConstraintMappings()) {
          final XmlFile file = domValue.getValue();
          if (file != null) {
            files.add(file);
          }
        }
        return files.toArray(new XmlFile[files.size()]);
      }
    }
    return EMPTY_ARRAY_OF_XML_FILES;
  }
}
