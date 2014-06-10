package com.intellij.hibernate.facet.ui;

import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.libraries.FacetLibrariesValidator;
import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.hibernate.facet.HibernateFacetConfiguration;
import com.intellij.hibernate.model.HibernateDescriptorsConstants;
import com.intellij.j2ee.HelpID;
import com.intellij.javaee.ui.ConfigFilesEditor;
import com.intellij.javaee.ui.DeploymentDescriptorsOwnerBase;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.persistence.PersistenceHelper;
import com.intellij.util.descriptors.ConfigFileFactory;
import com.intellij.util.descriptors.ConfigFileInfoSet;
import com.intellij.util.descriptors.ConfigFileMetaData;
import com.intellij.util.descriptors.ConfigFileMetaDataProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

/**
 * @author Gregory.Shrago
 */
public class HibernateGeneralEditorTab extends FacetEditorTab {
  private JPanel myMainPanel;
  private JPanel myDatasourceMapPanel;

  @Nullable
  private final UnnamedConfigurable myDataSourceComponent;
  @Nullable
  private final HibernateFacet myFacet;
  private DeploymentDescriptorsOwnerBase myDescriptorsOwner;
  private ConfigFileInfoSet myFileInfoSet;
  private ConfigFilesEditor myConfigFilesEditor;
  private JPanel myDescriptorsPanel;
  private final HibernateFacetConfiguration myConfiguration;
  private final FacetLibrariesValidator myLibrariesValidator;

  public HibernateGeneralEditorTab(final HibernateFacetConfiguration configuration, final FacetEditorContext editorContext, final FacetLibrariesValidator librariesValidator) {
    myConfiguration = configuration;
    myLibrariesValidator = librariesValidator;
    myFacet = (HibernateFacet)editorContext.getFacet();
    myDatasourceMapPanel.setLayout(new BorderLayout());
    myDataSourceComponent = PersistenceHelper.getHelper().createUnitToDataSourceMappingComponent(myFacet, false);
    myDatasourceMapPanel.add(myDataSourceComponent.createComponent(), BorderLayout.CENTER);
    createDescriptorsEditor(editorContext);
  }

  private void createDescriptorsEditor(final FacetEditorContext editorContext) {
    myDescriptorsOwner = new DeploymentDescriptorsOwnerBase(editorContext) {
      public boolean canBeParentForDescriptors(VirtualFile dir) {
        return true;
      }
    };
    final ConfigFileMetaData[] metaData = {HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA};
    final ConfigFileMetaDataProvider provider = ConfigFileFactory.getInstance().createMetaDataProvider(metaData);
    myFileInfoSet = myFacet == null ? ConfigFileFactory.getInstance().createConfigFileInfoSet(provider) : myFacet.getConfiguration().getDescriptorsConfiguration();
    final Module module = editorContext.getModule();
    final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
    final File defaultParent = sourceRoots.length > 0
                               ? new File(sourceRoots[0].getPath())
                               : new File(module.getModuleFilePath()).getAbsoluteFile().getParentFile();

    myConfigFilesEditor = new ConfigFilesEditor(null, metaData, myFileInfoSet, defaultParent, editorContext.isNewFacet(), myDescriptorsOwner, false);
    myDescriptorsPanel.setLayout(new BorderLayout());
    myDescriptorsPanel.add(myConfigFilesEditor.createComponent(), BorderLayout.CENTER);
  }

  @Nls
  public String getDisplayName() {
    return HibernateMessages.message("tab.title.hibernate.general.settings");
  }

  public JComponent createComponent() {
    return myMainPanel;
  }

  public boolean isModified() {
    if (myConfigFilesEditor.isModified()) return true;
    if (myDataSourceComponent != null && myDataSourceComponent.isModified()) return true;
    if (myLibrariesValidator.isLibrariesAdded()) return true;
    return false;
  }

  public void apply() throws ConfigurationException {
    if (myDataSourceComponent != null) {
      myDataSourceComponent.apply();
    }
    myConfigFilesEditor.appyChanges();
  }

  public void reset() {
    if (myDataSourceComponent != null) {
      myDataSourceComponent.reset();
    }
  }

  public void disposeUIResources() {
    if (myDataSourceComponent != null) {
      myDataSourceComponent.disposeUIResources();
    }
  }

  public void onFacetInitialized(@NotNull final Facet facet) {
    myDescriptorsOwner.createAndDeleteFiles(facet.getModule().getProject());
    ((HibernateFacet)facet).getConfiguration().getDescriptorsConfiguration().setConfigFileInfos(Arrays.asList(myFileInfoSet.getConfigFileInfos()));
    myLibrariesValidator.onFacetInitialized(facet);
  }


  @Nullable
  @NonNls
  public String getHelpTopic() {
    return HelpID.JPA_FACET_GENERAL_SETTINGS;
  }
}
