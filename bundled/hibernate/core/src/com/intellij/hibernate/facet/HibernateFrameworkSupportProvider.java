/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.facet;

import com.intellij.facet.FacetManager;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportConfigurableBase;
import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.model.HibernateDescriptorsConstants;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.jpa.facet.JpaFrameworkSupportProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public class HibernateFrameworkSupportProvider extends FacetBasedFrameworkSupportProvider<HibernateFacet> {
  private static final Logger LOG = Logger.getInstance("com.intellij.hibernate.facet.HibernateFrameworkSupportProvider");

  protected HibernateFrameworkSupportProvider() {
    super(HibernateFacetType.INSTANCE);
  }

  public String getTitle() {
    return HibernateMessages.message("framework.title.hibernate");
  }

  protected void setupConfiguration(final HibernateFacet facet, final ModifiableRootModel rootModel, final FrameworkVersion version) {
  }

  @NotNull
  @Override
  public List<FrameworkVersion> getVersions() {
    final HibernateVersion version = HibernateVersion.Hibernate_3_2_0;
    return Collections.singletonList(new FrameworkVersion(version.getName(), "hibernate3", version.getJars()));
  }

  @NotNull
  public FrameworkSupportConfigurableBase createConfigurable(final @NotNull FrameworkSupportModel model) {
    return new HibernateConfigurable();
  }


  private class HibernateConfigurable extends FrameworkSupportConfigurableBase {
    private JPanel myPanel;
    private JCheckBox myCreateMainClass;
    private JCheckBox myDbImport;

    public HibernateConfigurable() {
      super(HibernateFrameworkSupportProvider.this, getVersions(), null);
    }

    public JComponent getComponent() {
      if (myPanel == null) {
        myPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, true));
        final JComponent superC = super.getComponent();
        if (superC != null) {
          myPanel.add(superC);
        }
        myCreateMainClass = new JCheckBox(HibernateMessages.message("framework.configurable.generate.main.and.config"));
        myDbImport = new JCheckBox(HibernateMessages.message("framework.configurable.import.database"));
        myPanel.add(myCreateMainClass);
        myPanel.add(myDbImport);
      }
      return myPanel;
    }

    public void addSupport(@NotNull final Module module, @NotNull final ModifiableRootModel rootModel, final @Nullable Library library) {
      super.addSupport(module, rootModel, library);
      final HibernateFacet facet = FacetManager.getInstance(module).getFacetByType(HibernateFacet.ID);
      assert facet != null;
      final boolean isCreateMain = myCreateMainClass.isSelected();
      final boolean isDbImport = myDbImport.isSelected();
      final Runnable runnable = new Runnable() {
        public void run() {
          try {
            if (isCreateMain) {
              final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
              if (sourceRoots.length > 0) {
                final PsiDirectory directory = PsiManager.getInstance(module.getProject()).findDirectory(sourceRoots[0]);
                final String fileName = HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA.getFileName();
                if (directory != null) {
                  ModuleUtil.findModuleForPsiElement(directory);
                  PsiElement configElement = directory.findFile(fileName);
                  if (configElement == null) {
                    final FileTemplate configTemplate = FileTemplateManager.getInstance().getJ2eeTemplate(HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA.getDefaultVersion().getTemplateName());
                    configElement = FileTemplateUtil.createFromTemplate(configTemplate, fileName, null, directory);
                  }
                  if (configElement instanceof XmlFile) {
                    final XmlFile xmlFile = (XmlFile)configElement;
                    final String url = xmlFile.getVirtualFile().getUrl();
                    facet.getDescriptorsContainer().getConfiguration().addConfigFile(HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA, url);
                  }
                  @NonNls
                  final String mainFileName = "Main.java";
                  PsiElement mainElement = directory.findFile(mainFileName);
                  if (mainElement == null) {
                    final FileTemplate mainTemplate = FileTemplateManager.getInstance().getJ2eeTemplate(HibernateVersion.Hibernate_3_2_0.getMainTemplateName());
                    mainElement = FileTemplateUtil.createFromTemplate(mainTemplate, mainFileName, null, directory);
                  }
                  if (mainElement instanceof PsiFile) {
                    final PsiFile psiFile = (PsiFile)mainElement;
                  }
                }
              }
            }
            if (isDbImport) {
              JpaFrameworkSupportProvider.scheduleDbImport(module, facet);
            }
          }
          catch (Exception e) {
            LOG.error(e);
          }
        }
      };
      JpaFrameworkSupportProvider.scheduleRunnable(module, runnable);
    }
  }
}
