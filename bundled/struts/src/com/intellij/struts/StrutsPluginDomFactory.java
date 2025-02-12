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

package com.intellij.struts;

import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.jsp.WebDirectoryUtil;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts.dom.PlugIn;
import com.intellij.struts.dom.SetProperty;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Dmitry Avdeev
 */
public abstract class StrutsPluginDomFactory<T extends DomElement, M extends NamedDomModel<T>> extends WebDomFactory<T, M> {

  private final String mySuperClass;
  private final String myConfigProperty;
  protected final StrutsDomFactory myStrutsFactory;

  protected StrutsPluginDomFactory(@NotNull Class<T> aClass,
                                   @NotNull String superClass,
                                   @NotNull String configProperty,
                                   @NotNull StrutsDomFactory strutsFactory,
                                   Project project,
                                   @NonNls String name) {

    super(aClass, project, name);
    myStrutsFactory = strutsFactory;
    mySuperClass = superClass;
    myConfigProperty = configProperty;
  }

  @Nullable
  private PlugIn getPlugin(@NotNull StrutsModel struts) {
    PsiClass superClass = null;
    for (final PlugIn plugin : struts.getMergedModel().getPlugIns()) {
      final PsiClass pluginClass = plugin.getClassName().getValue();
      if (pluginClass != null) {
        if (superClass == null) {
          superClass = JavaPsiFacade.getInstance(pluginClass.getProject()).findClass(mySuperClass, pluginClass.getResolveScope());
          if (superClass == null) {
            return null;
          }
        }
        if (InheritanceUtil.isInheritorOrSelf(pluginClass, superClass, true)) {
          return plugin;
        }
      }
    }
    return null;
  }

  @Nullable
  public List<M> computeAllModels(@NotNull final Module module) {

    final List<StrutsModel> strutsModels = myStrutsFactory.getAllModels(module);
    if (strutsModels.size() == 0) {
      return Collections.emptyList();
    }

    final ArrayList<M> list = new ArrayList<M>(strutsModels.size());

    for (final StrutsModel strutsModel : strutsModels) {
      final M model = getModelFromStruts(strutsModel);
      if (model != null) {
        list.add(model);
      }
    }
    return list;
  }

  @Nullable
  protected M getModelFromStruts(@NotNull final StrutsModel strutsModel) {
    final PlugIn plugin = getPlugin(strutsModel);
    if (plugin != null) {
      final Set<XmlFile> configFiles = new LinkedHashSet<XmlFile>();
      final XmlTag element = plugin.getXmlTag();
      assert element != null;
      final WebFacet webFacet = WebUtil.getWebFacet(element);
      if (webFacet == null) {
        return null;
      }
      final WebDirectoryUtil webDirectoryUtil = WebDirectoryUtil.getWebDirectoryUtil(plugin.getManager().getProject());

      for (final SetProperty prop : plugin.getSetProperties()) {
        final String name = prop.getProperty().getStringValue();
        if (myConfigProperty.equals(name)) {
          final String configString = prop.getValue().getStringValue();
          if (configString != null) {
            final String[] configPaths = configString.split(",");
            for (final String configPath : configPaths) {
              final PsiElement psiElement = resolveFile(configPath, webDirectoryUtil, webFacet);
              if (psiElement instanceof XmlFile) {
                configFiles.add((XmlFile) psiElement);
              }
            }
          }
        }
      }
      if (configFiles.size() > 0) {
        final DomFileElement<T> mergedModel = createMergedModelRoot(configFiles);
        return mergedModel == null ? null : createModel(configFiles, mergedModel, strutsModel);
      }
    }
    return null;
  }

  protected abstract M createModel(@NotNull Set<XmlFile> configFiles, @NotNull DomFileElement<T> mergedModel, StrutsModel strutsModel);

  @Nullable
  protected PsiElement resolveFile(final String path, final WebDirectoryUtil webDirectoryUtil, final WebFacet webFacet) {
    return webDirectoryUtil.findFileByPath(path.trim(), webFacet);
  }
}
