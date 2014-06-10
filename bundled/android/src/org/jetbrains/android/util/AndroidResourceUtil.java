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

package org.jetbrains.android.util;

import com.android.sdklib.SdkConstants;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModulePackageIndex;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.dom.resources.Attr;
import org.jetbrains.android.dom.resources.DeclareStyleable;
import org.jetbrains.android.dom.resources.ResourceElement;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.resourceManagers.LocalResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 5, 2009
 * Time: 8:47:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidResourceUtil {
  public static final String NEW_ID_PREFIX = "@+id/";

  @Nullable
  public static PsiField findResourceField(@NotNull AndroidFacet facet, @NotNull String resClassName, @NotNull String resourceName) {
    PsiJavaFile rClassFile = findRClassFile(facet);
    if (rClassFile == null) return null;
    PsiClass rClass = findClass(rClassFile.getClasses(), AndroidUtils.R_CLASS_NAME);
    if (rClass == null) return null;
    PsiClass resourceTypeClass = findClass(rClass.getInnerClasses(), resClassName);
    if (resourceTypeClass == null) return null;
    return resourceTypeClass.findFieldByName(resourceName, false);
  }

  @Nullable
  public static PsiJavaFile findRClassFile(@NotNull AndroidFacet facet) {
    Module module = facet.getModule();
    Project project = module.getProject();
    Manifest manifest = facet.getManifest();
    if (manifest == null) return null;
    String packageName = manifest.getPackage().getValue();
    if (packageName != null) {
      final PsiManager manager = PsiManager.getInstance(module.getProject());
      final List<PsiDirectory> dirs = new ArrayList<PsiDirectory>();
      ModulePackageIndex.getInstance(module).getDirsByPackageName(packageName, false).forEach(new Processor<VirtualFile>() {
        public boolean process(final VirtualFile directory) {
          dirs.add(manager.findDirectory(directory));
          return true;
        }
      });
      for (PsiDirectory dir : dirs) {
        VirtualFile file = dir.getVirtualFile().findChild(AndroidUtils.R_JAVA_FILENAME);
        if (file != null) {
          PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
          if (psiFile instanceof PsiJavaFile) {
            return (PsiJavaFile)psiFile;
          }
        }
      }
    }
    return null;
  }

  @Nullable
  public static PsiClass findClass(PsiClass[] classes, @NotNull String name) {
    for (PsiClass c : classes) {
      if (name.equals(c.getName())) {
        return c;
      }
    }
    return null;
  }

  @Nullable
  public static PsiField findResourceFieldForFileResource(PsiFile file) {
    AndroidFacet facet = AndroidFacet.getInstance(file);
    if (facet != null) {
      LocalResourceManager manager = facet.getLocalResourceManager();
      String resourceType = manager.getFileResourceType(file);
      if (resourceType != null) {
        String resourceName = FileUtil.getNameWithoutExtension(file.getName());
        return AndroidResourceUtil.findResourceField(facet, resourceType, resourceName);
      }
    }
    return null;
  }

  @Nullable
  public static PsiField findResourceFieldForValueResource(XmlTag tag) {
    AndroidFacet facet = AndroidFacet.getInstance(tag);
    if (facet != null) {
      LocalResourceManager manager = facet.getLocalResourceManager();
      String fileResType = manager.getFileResourceType(tag.getContainingFile());
      if ("values".equals(fileResType)) {
        String resClassName = tag.getName();
        if (resClassName.equals("item")) {
          resClassName = tag.getAttributeValue("type", null);
        }
        else if (resClassName.equals("declare-styleable")) {
          resClassName = "styleable";
        }
        if (resClassName != null) {
          String resourceName = tag.getAttributeValue("name");
          if (resourceName != null) {
            resourceName = resourceName.replace('.', '_');
            return AndroidResourceUtil.findResourceField(facet, resClassName, resourceName);
          }
        }
      }
    }
    return null;
  }

  @Nullable
  public static String getResourceClassName(@NotNull PsiField field) {
    PsiClass resourceClass = field.getContainingClass();
    if (resourceClass != null) {
      PsiClass parentClass = resourceClass.getContainingClass();
      if (parentClass != null) {
        if (AndroidUtils.R_CLASS_NAME.equals(parentClass.getName()) && parentClass.getContainingClass() == null) {
          return resourceClass.getName();
        }
      }
    }
    return null;
  }

  // result contains XmlAttributeValue or PsiFile
  @NotNull
  public static List<PsiElement> findResourcesByField(@NotNull PsiField field) {
    LocalResourceManager manager = LocalResourceManager.getInstance(field);
    if (manager != null) {
      Map<String, List<XmlAttributeValue>> idMap = manager.createIdMap();
      return findResourcesByField(manager, field, idMap);
    }
    return new ArrayList<PsiElement>();
  }

  @NotNull
  public static List<PsiElement> findResourcesByField(@NotNull LocalResourceManager manager,
                                                      @NotNull PsiField field,
                                                      @NotNull Map<String, List<XmlAttributeValue>> idMap) {
    String type = getResourceClassName(field);
    List<PsiElement> targets = new ArrayList<PsiElement>();
    if (type != null) {
      String name = field.getName();
      if (type.equals("id")) {
        List<XmlAttributeValue> attrs = idMap.get(name);
        if (attrs != null) {
          targets.addAll(attrs);
        }
      }
      for (PsiFile file : manager.findResourceFiles(type, name, false)) {
        targets.add(file);
      }
      for (ResourceElement element : manager.findValueResources(type, name, false)) {
        targets.add(element.getName().getXmlAttributeValue());
      }
      if (type.equals("attr")) {
        for (Attr attr : manager.findAttrs(name)) {
          targets.add(attr.getName().getXmlAttributeValue());
        }
      }
      else if (type.equals("styleable")) {
        for (DeclareStyleable styleable : manager.findStyleables(name)) {
          targets.add(styleable.getName().getXmlAttributeValue());
        }
      }
    }
    return targets;
  }

  public static boolean isResourceField(PsiField field) {
    PsiClass c = field.getContainingClass();
    if (c == null) return false;
    c = c.getContainingClass();
    if (c != null && c.getName().equals(AndroidUtils.R_CLASS_NAME)) {
      AndroidFacet facet = AndroidFacet.getInstance(field);
      if (facet != null) {
        PsiFile file = c.getContainingFile();
        if (AndroidUtils.isRClassFile(facet, file)) {
          return true;
        }
      }
    }
    return false;
  }


  @Nullable
  public static PsiField findIdField(XmlAttributeValue value) {
    if (value.getParent() instanceof XmlAttribute) {
      return findIdField((XmlAttribute)value.getParent());
    }
    return null;
  }

  public static boolean isIdDeclaration(XmlAttributeValue value) {
    if (value.getParent() instanceof XmlAttribute) {
      XmlAttribute attribute = (XmlAttribute)value.getParent();
      return isIdDeclaration(attribute);
    }
    return false;
  }

  public static boolean isIdDeclaration(XmlAttribute attribute) {
    if (attribute.getLocalName().equals("id") && SdkConstants.NS_RESOURCES.equals(attribute.getNamespace())) {
      String idAttrValue = attribute.getValue();
      return idAttrValue.startsWith(NEW_ID_PREFIX);
    }
    return false;
  }

  @Nullable
  public static PsiField findIdField(XmlAttribute attribute) {
    if (isIdDeclaration(attribute)) {
      String id = getResourceNameByReferenceText(attribute.getValue());
      if (id != null) {
        AndroidFacet facet = AndroidFacet.getInstance(attribute);
        if (facet != null) {
          return AndroidResourceUtil.findResourceField(facet, "id", id);
        }
      }
    }
    return null;
  }

  @Nullable
  public static String getResourceNameByReferenceText(@NotNull String text) {
    int i = text.indexOf('/');
    if (i < text.length() - 1) {
      return text.substring(i + 1, text.length());
    }
    return null;
  }
}
