package org.jetbrains.android.facet;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.GlobalLibrariesConfigurable;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.sdk.AndroidPlatform;
import org.jetbrains.android.sdk.AndroidPlatformChooser;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

/**
 * @author yole
 */
public class AndroidFacetEditorTab extends FacetEditorTab {
  private final AndroidPlatformChooser myPlatformChooser;
  private final AndroidFacetConfiguration myConfiguration;

  public AndroidFacetEditorTab(FacetEditorContext context, AndroidFacetConfiguration androidFacetConfiguration) {
    Project project = context.getProject();
    LibraryTable.ModifiableModel model = GlobalLibrariesConfigurable.getInstance(project).getModelProvider(true).getModifiableModel();
    myPlatformChooser = new AndroidPlatformChooser(model, project);
    myConfiguration = androidFacetConfiguration;
  }

  @Nls
  public String getDisplayName() {
    return "Android SDK Settings";
  }

  public JComponent createComponent() {
    return myPlatformChooser.getComponent();
  }

  public boolean isModified() {
    return !Comparing.equal(myPlatformChooser.getSelectedPlatform(), myConfiguration.getAndroidPlatform());
  }

  public void apply() throws ConfigurationException {
    if (!isModified()) return;
    final AndroidPlatform oldPlatform = myConfiguration.getAndroidPlatform();
    final AndroidPlatform platform = myPlatformChooser.getSelectedPlatform();
    myConfiguration.setAndroidPlatform(platform);
    final AndroidFacet facet = myConfiguration.getFacet();
    if (facet != null) {
      myPlatformChooser.apply();
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          final ModifiableRootModel model = ModuleRootManager.getInstance(facet.getModule()).getModifiableModel();
          if (platform != null) {
            model.addLibraryEntry(platform.getLibrary());
          }
          if (oldPlatform != null) {
            LibraryOrderEntry entry = model.findLibraryOrderEntry(oldPlatform.getLibrary());
            if (entry != null) {
              model.removeOrderEntry(entry);
            }
          }
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              model.commit();
            }
          });
        }
      });
    }
  }

  public void reset() {
    myPlatformChooser.rebuildPlatforms();
    AndroidPlatform platform = myConfiguration.getAndroidPlatform();
    myPlatformChooser.setSelectedPlatform(platform);
  }

  public void disposeUIResources() {
    Disposer.dispose(myPlatformChooser);
  }
}
