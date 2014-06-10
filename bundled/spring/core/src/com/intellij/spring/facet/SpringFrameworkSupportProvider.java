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

package com.intellij.spring.facet;

import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.util.frameworkSupport.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.JarVersionDetectionUtil;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author nik
 */
public class SpringFrameworkSupportProvider extends FacetBasedFrameworkSupportProvider<SpringFacet> {
  
  private static final Logger LOG = Logger.getInstance("#com.intellij.spring.facet.SpringFrameworkSupportProvider");
  
  @NonNls private static final String SPRING_CONFIG_XML = "spring-config.xml";

  public SpringFrameworkSupportProvider() {
    super(SpringFacetType.INSTANCE);
  }

  @NotNull
  public List<FrameworkVersion> getVersions() {
    final List<FrameworkVersion> result = new ArrayList<FrameworkVersion>();
    for (SpringVersion version : SpringVersion.values()) {
      final String name = version.getName();
      result.add(new FrameworkVersion(name, "spring-" + name, version.getJars()));
    }
    return result;
  }

  private static SpringVersion getVersion(final String versionString) {
    SpringVersion version = ContainerUtil.find(SpringVersion.values(), new Condition<SpringVersion>() {
      public boolean value(final SpringVersion springVersion) {
        return springVersion.getName().equals(versionString);
      }
    });
    LOG.assertTrue(version != null, versionString);
    return version;
  }

  public String[] getPrecedingFrameworkProviderIds() {
    return new String[] { "facet:web", "facet:jsf"};
  }

  public String getTitle() {
    return SpringBundle.message("framework.title.spring");
  }

  @NotNull
  @Override
  public FrameworkSupportConfigurableBase createConfigurable(@NotNull final FrameworkSupportModel model) {
    return new SpringFrameworkSupportConfigurable(model);
  }

  public void setupConfiguration(final SpringFacet facet, final ModifiableRootModel rootModel, final FrameworkVersion version) {
  }

  @Override
  protected void onFacetCreated(final SpringFacet facet, final ModifiableRootModel rootModel, final FrameworkVersion version) {
    final Module module = facet.getModule();
    StartupManager.getInstance(module.getProject()).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        boolean configured = false;
        for (SpringConfigurator configurator: Extensions.getExtensions(SpringConfigurator.EP_NAME)) {
          configured = configurator.configure(module);
          if (configured) {
            break;
          }
        }
        if (!configured) {
          try {
            final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            if (sourceRoots.length > 0) {
              final PsiDirectory directory = PsiManager.getInstance(module.getProject()).findDirectory(sourceRoots[0]);
              if (directory != null && directory.findFile(SPRING_CONFIG_XML) == null) {
                final FileTemplate template = chooseTemplate(module);
                final PsiElement psiElement = FileTemplateUtil.createFromTemplate(template, SPRING_CONFIG_XML, null, directory);
                if (psiElement instanceof XmlFile) {
                  final Set<SpringFileSet> empty = Collections.emptySet();
                  final SpringFileSet fileSet = new SpringFileSet(SpringFileSet.getUniqueId(empty),
                                                                  SpringFileSet.getUniqueName(SpringBundle.message("default.fileset.name"), empty),
                                                                  facet.getConfiguration());
                  fileSet.addFile(((XmlFile)psiElement).getVirtualFile());
                  facet.getConfiguration().getFileSets().add(fileSet);
                }
              }
            }
          }
          catch (Exception e) {
            LOG.error(e);
          }
        }
      }
    });
  }

  public static FileTemplate chooseTemplate(final Module module) {
    final String version = JarVersionDetectionUtil.detectJarVersion(SpringConstants.SPRING_VERSION_CLASS, module);
    return (version != null && version.startsWith("1")) ?
                            SpringSchemaVersion.Spring_1_DTD.getTemplate() :
                            SpringSchemaVersion.Spring_2_Schema.getTemplate();
  }

  private class SpringFrameworkSupportConfigurable extends FrameworkSupportConfigurableBase {
    private boolean myMvcEnabled;
    private final FrameworkSupportModel myModel;

    public SpringFrameworkSupportConfigurable(FrameworkSupportModel model) {
      super(SpringFrameworkSupportProvider.this, SpringFrameworkSupportProvider.this.getVersions(), "Version:");
      myModel = model;
      model.addFrameworkListener(new FrameworkSupportModelListener() {
        public void frameworkSelected(@NotNull final FrameworkSupportProvider provider) {
          setMvc();
        }

        public void frameworkUnselected(@NotNull final FrameworkSupportProvider provider) {
          setMvc();
        }
      });
    }

    private void setMvc() {
      boolean oldValue = myMvcEnabled;
      myMvcEnabled = myModel.isFrameworkSelected("facet:web") && myModel.isFrameworkSelected(getId());
      if (myMvcEnabled != oldValue) {
        fireFrameworkVersionChanged();
      }
    }

    @Override
    public FrameworkVersion getSelectedVersion() {
      final FrameworkVersion version = super.getSelectedVersion();
      if (version != null) {
        final SpringVersion springVersion = getVersion(version.getVersionName());
        final LibraryInfo mvcJars = springVersion.getMvcJars();
        if (myMvcEnabled && mvcJars != null) {
          return new FrameworkVersion(version.getVersionName(), version.getLibraryName(), ArrayUtil.append(version.getLibraries(), mvcJars));
        }
      }
      return version;
    }
  }
}
