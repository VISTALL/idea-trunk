/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
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

package com.intellij.struts.psi;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.jsp.WebDirectoryUtil;
import com.intellij.psi.jsp.WebDirectoryElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts.*;
import com.intellij.struts.dom.StrutsConfig;
import com.intellij.struts.dom.StrutsRootElement;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.ExtendClass;
import com.intellij.util.xml.reflect.DomAttributeChildDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: DAvdeev
 * Date: 21.10.2005
 * Time: 18:07:22
 * To change this template use File | Settings | File Templates.
 */
public class StrutsManagerImpl extends StrutsManager {

  @Nullable
  public StrutsModel getStrutsModel(@Nullable final PsiElement psiElement) {
    if (psiElement == null) {
      return null;
    }
    final PsiElement p = psiElement.getParent();
    if (p == null) {
      return null;
    }
    final PsiElement parent = p.getParent();
    if (parent instanceof XmlTag) {
      final String modulePrefix = ((XmlTag)parent).getAttributeValue("module");
      if (modulePrefix != null) {
        final Module module = ModuleUtil.findModuleForPsiElement(psiElement);
        if (module != null) {
          final StrutsModel model = getModelByPrefix(module, modulePrefix);
          if (model != null) {
            return model;
          }
        }
      }
    }
    PsiFile file = psiElement.getContainingFile();
    if (file == null) {
      return null;
    }
    return getModelByFile(file);
  }

  @NotNull
  public StrutsConfig getContext(@NotNull final StrutsRootElement element) {
    final XmlElement xmlElement = element.getXmlElement();
    final StrutsModel model = getStrutsModel(xmlElement);
    assert xmlElement != null;
    final StrutsConfig strutsConfig = model == null ? getStrutsConfig(xmlElement.getContainingFile()) : model.getMergedModel();
    assert strutsConfig != null;
    return strutsConfig;
  }

  @Nullable
  private StrutsModel getModelByFile(PsiFile file) {
    file = file.getOriginalFile();
    final FileType fileType = file.getFileType();
    if (fileType == StdFileTypes.XML) {
      return StrutsProjectComponent.getInstance(file.getProject()).getStrutsFactory().getModel(file);
    } else {
      final Module module = ModuleUtil.findModuleForPsiElement(file);
      if (module != null) {
        final VirtualFile virtualFile = file.getVirtualFile();
        assert virtualFile != null;
        final WebDirectoryElement dir = WebDirectoryUtil.findParentWebDirectory(module.getProject(), virtualFile);
        if (dir != null) {
          final List<StrutsModel> strutsModels = getAllStrutsModels(module);
          String path = dir.getPath();
          while (true) {
            for (StrutsModel model: strutsModels) {
              final WebDirectoryElement moduleRoot = model.getModuleRoot();
              if (moduleRoot != null && path.equals(moduleRoot.getPath())) {
                return model;
              }
            }
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash == -1) {
              break;
            }
            path = path.substring(0, lastSlash);
          }
        }
        return getCombinedStrutsModel(module);
      }
    }
    return null;
  }

  @NotNull
  public List<StrutsModel> getAllStrutsModels(@NotNull final Module module) {
    return StrutsProjectComponent.getInstance(module.getProject()).getStrutsFactory().getAllModels(module);
  }

  @Nullable
  public StrutsConfig getStrutsConfig(@NotNull final PsiFile configFile) {
    return configFile instanceof XmlFile ? StrutsProjectComponent.getInstance(configFile.getProject()).getStrutsFactory().getDom((XmlFile)configFile) : null;
  }

  @Nullable
  public StrutsModel getCombinedStrutsModel(@Nullable final Module module) {
    return module == null ? null : StrutsProjectComponent.getInstance(module.getProject()).getStrutsFactory().getCombinedModel(module);
  }

  @Nullable
  public StrutsModel getModelByPrefix(@NotNull final Module module, @NotNull @NonNls final String modulePrefix) {
    final List<StrutsModel> models = getAllStrutsModels(module);
    for (StrutsModel model : models) {
      if (model.getModulePrefix().equals(modulePrefix)) {
        return model;
      }
    }
    return null;
  }

  @Nullable
  public ValidationModel getValidation(@Nullable final PsiElement psiElement) {
    return psiElement == null ? null : StrutsProjectComponent.getInstance(psiElement.getProject()).getValidatorFactory().getModel(psiElement);
  }

  @NotNull
  public List<ValidationModel> getAllValidationModels(@NotNull final Module module) {
    return StrutsProjectComponent.getInstance(module.getProject()).getValidatorFactory().getAllModels(module);
  }

  @Nullable
  public String getDefaultClassname(final String attrName, final XmlTag tag) {
    final DomElement domElement = DomManager.getDomManager(tag.getProject()).getDomElement(tag);
    if (domElement != null) {
      final DomAttributeChildDescription<?> childDescription = domElement.getGenericInfo().getAttributeChildDescription(attrName);
      if (childDescription != null) {
        final ExtendClass annotation = childDescription.getAnnotation(ExtendClass.class);
        if (annotation != null) {
          return annotation.value();
        }
      }
    }
    return null;
  }

  @Nullable
  public TilesModel getTiles(@Nullable final PsiElement psiElement) {
    return psiElement == null ? null : StrutsProjectComponent.getInstance(psiElement.getProject()).getTilesFactory().getModel(psiElement);
  }

  @NotNull
  public List<TilesModel> getAllTilesModels(@NotNull final Module module) {
    return StrutsProjectComponent.getInstance(module.getProject()).getTilesFactory().getAllModels(module);
  }

  public boolean isStrutsConfig(@NotNull final XmlFile file) {
    return DomManager.getDomManager(file.getProject()).getFileElement(file, StrutsConfig.class) != null;
  }

  @NotNull
  public Set<XmlFile> getStrutsConfigFiles(@Nullable final PsiElement psiElement) {
    final StrutsModel model = getStrutsModel(psiElement);
    if (model == null) {
      return Collections.emptySet();
    }
    else {
      return model.getConfigFiles();
    }
  }

}
