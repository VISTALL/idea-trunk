package org.jetbrains.idea.eclipse.export;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.storage.ClasspathStorage;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.EclipseJDOMUtil;
import org.jetbrains.idea.eclipse.EclipseBundle;
import org.jetbrains.idea.eclipse.EclipseXml;
import org.jetbrains.idea.eclipse.IdeaXml;
import org.jetbrains.idea.eclipse.config.EclipseClasspathStorageProvider;
import org.jetbrains.idea.eclipse.conversion.ConversionException;
import org.jetbrains.idea.eclipse.conversion.DotProjectFileHelper;
import org.jetbrains.idea.eclipse.conversion.EclipseClasspathWriter;
import org.jetbrains.idea.eclipse.conversion.EclipseUserLibrariesHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExportEclipseProjectsAction extends AnAction implements DumbAware {
  private static final Logger LOG = Logger.getInstance("#" + ExportEclipseProjectsAction.class.getName());

  public void update(final AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    e.getPresentation().setEnabled( project != null );
  }

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    if ( project == null ) return;
    project.save(); // to flush iml files

    final List<Module> modules = new ArrayList<Module>();
    final List<Module> incompatibleModules = new ArrayList<Module>();
    for (Module module : ModuleManager.getInstance(project).getModules()) {
      if (!EclipseClasspathStorageProvider.ID.equals(ClasspathStorage.getStorageType(module))) {
        final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
        try {
          ClasspathStorage.getProvider(EclipseClasspathStorageProvider.ID).assertCompatible(model);
          modules.add(module);
        }
        catch (ConfigurationException e1) {
          incompatibleModules.add(module);
        } finally {
          model.dispose();
        }
      }
    }

    //todo suggest smth with hierarchy modules
    if (!incompatibleModules.isEmpty()) {
      if (Messages.showOkCancelDialog(project, "Eclipse incompatible modules found. Would you like to proceed and possibly loose your configurations?", "Eclipse Incompatible Modules Found", Messages.getWarningIcon()) != DialogWrapper.OK_EXIT_CODE) {
        return;
      }
    } else if (modules.isEmpty()){
      Messages.showInfoMessage(project, EclipseBundle.message("eclipse.export.nothing.to.do"), EclipseBundle.message("eclipse.export.dialog.title"));
      return;
    }

    modules.addAll(incompatibleModules);
    final ExportEclipseProjectsDialog dialog = new ExportEclipseProjectsDialog(project, modules);
    dialog.show ();
    if(dialog.isOK()){
      if (dialog.isLink()) {
        for (Module module : dialog.getSelectedModules()) {
          final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
          ClasspathStorage.setStorageType(model, EclipseClasspathStorageProvider.ID);
          model.dispose();
        }
      }
      else {
        for (Module module : dialog.getSelectedModules()) {
          final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
          final VirtualFile[] contentRoots = model.getContentRoots();
          final String storageRoot = contentRoots.length == 1 ? contentRoots[0].getPath() : ClasspathStorage.getStorageRootFromOptions(module);
          try {
            final Element classpathEleemnt = new Element(EclipseXml.CLASSPATH_TAG);

            final EclipseClasspathWriter classpathWriter = new EclipseClasspathWriter(model);
            classpathWriter.writeClasspath(classpathEleemnt, null);
            final File classpathFile = new File(storageRoot, EclipseXml.CLASSPATH_FILE);
            if (!FileUtil.createIfDoesntExist(classpathFile)) continue;
            EclipseJDOMUtil.output(new Document(classpathEleemnt), classpathFile, project);

            final Element ideaSpecific = new Element(IdeaXml.COMPONENT_TAG);
            if (classpathWriter.writeIDEASpecificClasspath(ideaSpecific)) {
              final File emlFile = new File(storageRoot, module.getName() + EclipseXml.IDEA_SETTINGS_POSTFIX);
              if (!FileUtil.createIfDoesntExist(emlFile)) continue;
              EclipseJDOMUtil.output(new Document(ideaSpecific), emlFile, project);
            }

            DotProjectFileHelper.saveDotProjectFile(module, storageRoot);
          }
          catch (ConversionException e1) {
            LOG.error(e1);
          }
          catch (IOException e1) {
            LOG.error(e1);
          }
          catch (WriteExternalException e1) {
            LOG.error(e1);
          }
          finally {
            model.dispose();
          }
        }
      }
      try {
        EclipseUserLibrariesHelper.appendProjectLibraries(project, dialog.getUserLibrariesFile());
      }
      catch (IOException e1) {
        LOG.error(e1);
      }
      project.save();
    }
  }

}
