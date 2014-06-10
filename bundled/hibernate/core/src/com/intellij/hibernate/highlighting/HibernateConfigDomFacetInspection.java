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

package com.intellij.hibernate.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.IntentionAndQuickFixAction;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.hibernate.facet.HibernateFacetType;
import com.intellij.hibernate.model.HibernateConstants;
import static com.intellij.hibernate.model.HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA;
import com.intellij.hibernate.model.xml.config.HibernateConfiguration;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.descriptors.ConfigFile;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomElementAnnotationsManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class HibernateConfigDomFacetInspection extends BasicDomElementsInspection<HibernateConfiguration> {

  public HibernateConfigDomFacetInspection() {
    super(HibernateConfiguration.class);
  }

  @NotNull
  public String getGroupDisplayName() {
    return HibernateConstants.HIBERNATE_INSPECTIONS_GROUP;
  }

  @NotNull
  public String getDisplayName() {
    return HibernateMessages.message("inspection.name.hibernate.configuration.facet");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "HibernateConfigDomFacetInspection";
  }

  @NotNull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  @Override
  public void checkFileElement(final DomFileElement<HibernateConfiguration> element, final DomElementAnnotationHolder holder) {
    final Module elementModule = element.getModule();
    final VirtualFile virtualFile = element.getFile().getVirtualFile();
    assert virtualFile != null;
    boolean found = false;
    final Project project = element.getManager().getProject();
    final Module[] modulesToLookAt = elementModule == null
                                     ? ModuleManager.getInstance(project).getModules()
                                     : JamCommonUtil.getAllDependentModules(elementModule);
    if (modulesToLookAt.length == 0) return;
    final List<HibernateFacet> facets = ContainerUtil.concat(modulesToLookAt, new Function<Module, Collection<? extends HibernateFacet>>() {
      public Collection<? extends HibernateFacet> fun(final Module module) {
        return FacetManager.getInstance(module).getFacetsByType(HibernateFacet.ID);
      }
    });
    for (HibernateFacet facet : facets) {
      for (ConfigFile configFile : facet.getDescriptors()) {
        if (HIBERNATE_CONFIGURATION_META_DATA == configFile.getMetaData() &&
            virtualFile.equals(configFile.getVirtualFile())) {
          found = true;
        }
      }
    }
    if (!found) {
      for (HibernateFacet facet : facets) {
        if (facet.getPersistenceUnits().contains(element.getRootElement().getSessionFactory())) {
          found = true;
          break;
        }
      }
    }
    if (facets.isEmpty()) {
      holder.createProblem(element, HighlightSeverity.WARNING, HibernateMessages.message("warning.cfg.xml.not.in.any.facet"), new AddFacetFix(modulesToLookAt, facets));
    }
    else if (!found) {
      holder.createProblem(element, HighlightSeverity.WARNING, HibernateMessages.message("warning.cfg.xml.not.in.any.facet"), new AddToFacetConfigurationFix(modulesToLookAt, facets));
    }
  }

  private static class AddToFacetConfigurationFix extends IntentionAndQuickFixAction {
    private final Module[] myModules;
    private final List<HibernateFacet> myFacets;

    public AddToFacetConfigurationFix(final Module[] modules, final List<HibernateFacet> facets) {
      myModules = modules;
      myFacets = facets;
    }

    @NotNull
    public String getName() {
      return HibernateMessages.message("name.fix.add.to.facet.configuration");
    }

    @NotNull
    public String getFamilyName() {
      return HibernateMessages.message("hibernate.quickfix.family");
    }

    @Override
    public boolean startInWriteAction() {
      return false;
    }

    public void applyFix(final Project project, final PsiFile file, @Nullable final Editor editor) {
      doFix(project, file.getVirtualFile(), editor);
    }

    private void doFix(final Project project, final VirtualFile file, final Editor editor) {
      final String url = file.getUrl();
      if (myModules.length == 1 && (myFacets.isEmpty() || myFacets.size() == 1)) {
        doAddToFacet(myModules[0], myFacets.isEmpty() ? null : myFacets.get(0), url);
      }
      else {
        final ArrayList<Object> list = new ArrayList<Object>();
        list.addAll(Arrays.asList(myModules));
        list.addAll(myFacets);
        for (HibernateFacet facet : myFacets) {
          list.remove(facet.getModule());
        }
        Collections.sort(list, new Comparator<Object>() {
          public int compare(final Object o1, final Object o2) {
            return Comparing.compare(getItemName(o1), getItemName(o2));
          }
        });
        final ListPopup popup = JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<Object>(HibernateMessages.message("popup.title.choose.module.and.facet"), list) {
          @NotNull
          public String getTextFor(final Object o) {
            return getItemName(o);
          }

          public Icon getIconFor(final Object o) {
            return o instanceof Module? ((Module)o).getModuleType().getNodeIcon(false) : ((Facet)o).getType().getIcon();
          }

          public PopupStep onChosen(final Object o, final boolean finalChoice) {
            final Module module = o instanceof Module ? (Module)o : ((Facet)o).getModule();
            final HibernateFacet facet = o instanceof Module ? null : (HibernateFacet)o;
            doAddToFacet(module, facet, url);
            return PopupStep.FINAL_CHOICE;
          }
        });
        if (editor != null) {
          popup.showInBestPositionFor(editor);
        }
        else {
          popup.showInScreenCoordinates(WindowManager.getInstance().getFrame(project), MouseInfo.getPointerInfo().getLocation());
        }
      }
    }

    private static String getItemName(final Object o) {
      return o instanceof Module? ((Module)o).getName() : ((Facet)o).getModule().getName()+"/"+ ((Facet)o).getName();
    }

    private void doAddToFacet(@NotNull final Module module, @Nullable final HibernateFacet facet, final String url) {
      new WriteCommandAction.Simple(module.getProject(), getName()) {
        protected void run() throws Throwable {
          final HibernateFacet chosenFacet = facet != null ? facet : FacetManager.getInstance(module).addFacet(HibernateFacetType.INSTANCE, HibernateFacetType.INSTANCE.getDefaultFacetName(), null);
          chosenFacet.getConfiguration().getDescriptorsConfiguration().addConfigFile(HIBERNATE_CONFIGURATION_META_DATA, url);
        }
      }.execute();
      DomElementAnnotationsManager.getInstance(module.getProject()).dropAnnotationsCache();
      DaemonCodeAnalyzer.getInstance(module.getProject()).restart();
    }
  }

  private static class AddFacetFix extends AddToFacetConfigurationFix {
    public AddFacetFix(final Module[] modules, final List<HibernateFacet> facets) {
      super(modules, facets);
    }

    @NotNull
    public String getName() {
      return HibernateMessages.message("name.fix.create.facet.and.add.to.facet.configuration");
    }
  }

}