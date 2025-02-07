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

package org.jetbrains.android.resourceManagers;

import com.android.sdklib.SdkConstants;
import com.intellij.CommonBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.android.AndroidFileTemplateProvider;
import org.jetbrains.android.actions.CreateResourceFileAction;
import org.jetbrains.android.dom.attrs.AttributeDefinitions;
import org.jetbrains.android.dom.resources.*;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidRootUtil;
import org.jetbrains.android.util.AndroidBundle;
import static org.jetbrains.android.util.AndroidUtils.loadDomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Mar 30, 2009
 * Time: 7:44:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class LocalResourceManager extends ResourceManager {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.android.resourceManagers.ResourceManager");
  private AttributeDefinitions myAttrDefs;

  public LocalResourceManager(@NotNull Module module) {
    super(module);
  }

  @Override
  public VirtualFile getResourceDir() {
    return AndroidRootUtil.getResourceDir(getModule());
  }

  @Nullable
  public static LocalResourceManager getInstance(@NotNull Module module) {
    AndroidFacet facet = AndroidFacet.getInstance(module);
    return facet != null ? facet.getLocalResourceManager() : null;
  }

  @Nullable
  public static LocalResourceManager getInstance(@NotNull PsiElement element) {
    AndroidFacet facet = AndroidFacet.getInstance(element);
    return facet != null ? facet.getLocalResourceManager() : null;
  }

  @NotNull
  public AttributeDefinitions getAttributeDefinitions() {
    if (myAttrDefs == null) {
      List<XmlFile> xmlResFiles = new ArrayList<XmlFile>();
      for (PsiFile file : findResourceFiles("values")) {
        if (file instanceof XmlFile) {
          xmlResFiles.add((XmlFile)file);
        }
      }
      myAttrDefs = new AttributeDefinitions(xmlResFiles.toArray(new XmlFile[xmlResFiles.size()]));
    }
    return myAttrDefs;
  }

  public void invalidateAttributeDefinitions() {
    myAttrDefs = null;
  }

  @Nullable
  public List<XmlAttributeValue> findIdDeclarations(@NotNull String id) {
    return createIdMap().get(id);
  }

  @NotNull
  public Collection<String> getIds() {
    return createIdMap().keySet();
  }

  @NotNull
  public List<Attr> findAttrs(@NotNull String name) {
    List<Attr> list = new ArrayList<Attr>();
    for (Resources res : getResourceElements()) {
      for (Attr attr : res.getAttrs()) {
        if (name.equals(attr.getName().getValue())) {
          list.add(attr);
        }
      }
      for (DeclareStyleable styleable : res.getDeclareStyleables()) {
        for (Attr attr : styleable.getAttrs()) {
          if (name.equals(attr.getName().getValue())) {
            list.add(attr);
          }
        }
      }
    }
    return list;
  }

  public List<DeclareStyleable> findStyleables(@NotNull String name) {
    List<DeclareStyleable> list = new ArrayList<DeclareStyleable>();
    for (Resources res : getResourceElements()) {
      for (DeclareStyleable styleable : res.getDeclareStyleables()) {
        if (name.equals(styleable.getName().getValue())) {
          list.add(styleable);
        }
      }
    }
    return list;
  }

  @Nullable
  private VirtualFile findOrCreateResourceFile(@NotNull final String fileName) {
    VirtualFile dir = getResourceDir();
    if (dir == null) {
      Messages.showErrorDialog(myModule.getProject(), AndroidBundle.message("check.resource.dir.error"), CommonBundle.getErrorTitle());
      return null;
    }
    final VirtualFile valuesDir = findOrCreateChildDir(dir, SdkConstants.FD_VALUES);
    if (valuesDir == null) return null;
    VirtualFile child = valuesDir.findChild(fileName);
    if (child != null) return child;
    try {
      AndroidFileTemplateProvider
        .createFromTemplate(myModule.getProject(), valuesDir, AndroidFileTemplateProvider.VALUE_RESOURCE_FILE_TEMPLATE, fileName);
    }
    catch (Exception e) {
      LOG.error(e);
      return null;
    }
    VirtualFile result = valuesDir.findChild(fileName);
    if (result == null) {
      LOG.error("Can't create resource file");
    }
    return result;
  }

  // must be invoked in a write action
  @Nullable
  public VirtualFile addResourceFileAndNavigate(@NotNull final String fileOrResourceName, @NotNull String resType) {
    VirtualFile resDir = getResourceDir();
    Project project = myModule.getProject();
    if (resDir == null) {
      Messages.showErrorDialog(project, AndroidBundle.message("check.resource.dir.error"), CommonBundle.getErrorTitle());
      return null;
    }
    PsiElement[] createdElements = CreateResourceFileAction.createResourceFile(project, resDir, resType, fileOrResourceName);
    if (createdElements.length == 0) return null;
    assert createdElements.length == 1;
    PsiElement element = createdElements[0];
    assert element instanceof PsiFile;
    return ((PsiFile)element).getVirtualFile();
  }

  // must be invoked in a write action
  @Nullable
  public ResourceElement addValueResource(@NotNull final String type, @NotNull final String name, @Nullable final String value) {
    String resourceFileName = getDefaultResourceFileName(type);
    if (resourceFileName == null) return null;
    VirtualFile resFile = findOrCreateResourceFile(resourceFileName);
    if (resFile == null) return null;
    final Resources resources = loadDomElement(myModule, resFile, Resources.class);
    if (resources == null) {
      Messages.showErrorDialog(myModule.getProject(), AndroidBundle.message("not.resource.file.error", resourceFileName),
                               CommonBundle.getErrorTitle());
      return null;
    }
    ResourceElement element = addValueResource(type, resources);
    assert element != null;
    element.getName().setValue(name);
    if (value != null) {
      element.setValue(value);
    }
    return element;
  }

  @Nullable
  private static ResourceElement addValueResource(@NotNull final String type, @NotNull final Resources resources) {
    if (type.equals("string")) {
      return resources.addString();
    }
    if (type.equals("dimen")) {
      return resources.addDimen();
    }
    if (type.equals("color")) {
      return resources.addColor();
    }
    if (type.equals("drawable")) {
      return resources.addDrawable();
    }
    if (type.equals("style")) {
      return resources.addStyle();
    }
    if (type.equals("array")) {
      return resources.addStringArray();
    }
    if (type.equals("id")) {
      Item item = resources.addItem();
      item.getType().setValue("id");
      return item;
    }
    return null;
  }

  @Nullable
  private VirtualFile findOrCreateChildDir(@NotNull final VirtualFile dir, @NotNull final String name) {
    VirtualFile child = dir.findChild(name);
    if (child != null) return child;
    try {
      return dir.createChildDirectory(myModule.getProject(), name);
    }
    catch (IOException e) {
      LOG.error(e);
      return null;
    }
  }
}
