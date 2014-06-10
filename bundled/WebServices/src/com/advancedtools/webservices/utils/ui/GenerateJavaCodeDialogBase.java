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

package com.advancedtools.webservices.utils.ui;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPlugin;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.LibUtils;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.ComboboxWithBrowseButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author maxim
 */
public abstract class GenerateJavaCodeDialogBase extends GenerateDialogBase {
  public static final String SEPARATOR_CHAR = "\0";

  public GenerateJavaCodeDialogBase(Project _project) {
    super(_project);
  }

  protected void restoreCommonFieldsFromPreviousSession(GenerateJavaCodeDialogBase previousDialog) {
    if (previousDialog != null) {
      final JComboBox pathes = getOutputPathes();
      if (pathes != null) pathes.setSelectedItem(previousDialog.getOutputPathes().getSelectedItem());
    }
  }
  
  protected void init() {
    super.init();

    DataContext dataContext = DataManager.getInstance().getDataContext();
    PsiFile file = dataContext != null ? (PsiFile) dataContext.getData(DataConstants.PSI_FILE):null;

    VirtualFile virtualFile = file != null ? file.getVirtualFile():null;
    if (file != null && isAcceptableFile(virtualFile)) {
      initVirtualFile(virtualFile);
    }

    final JComboBox outputPathes = getOutputPathes();

    if (outputPathes != null) {
      Module selectedModule;
      selectedModule = getForcedModule();
      if (selectedModule == null) selectedModule = (Module) (dataContext != null ? dataContext.getData(DataConstants.MODULE) : null);
      String selectedPath = null;

      Module[] modules = ModuleManager.getInstance(myProject).getModules();
      List<String> outputPathesList = new LinkedList<String>();

      for (Module module : modules) {
        final VirtualFile[] urls = ModuleRootManager.getInstance(module).getFiles(OrderRootType.SOURCES);

        for (VirtualFile url : urls) {
          if (url.getExtension() == null && url.isWritable()) {
            String presentableUrl = url.getPresentableUrl();
            outputPathesList.add(presentableUrl);

            if (selectedModule == module && selectedPath == null) {
              selectedPath = presentableUrl;
            }
          }
        }
      }

      Collections.sort(outputPathesList);

      outputPathes.setModel(new DefaultComboBoxModel(outputPathesList.toArray(new String[outputPathesList.size()])));
      if (outputPathesList.size() > 0) {
        outputPathes.setSelectedIndex( selectedPath == null ? 0 : outputPathesList.indexOf(selectedPath));
      }
      doInitFor(getOutputPathesText(), outputPathes, 'o');
    }

    final WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();

    final JComboBox packagePrefix = getPackagePrefix();

    if (packagePrefix != null) {
      configureComboBox(packagePrefix, instance.getLastPackagePrefixes());
      if (packagePrefix.getItemCount() == 0) packagePrefix.setSelectedItem("mypackage");
      doInitFor(getPackagePrefixText(), packagePrefix, 'a');
    }

    final JCheckBox addLibs = getAddLibs();
    if (addLibs != null) {
      addLibs.setMnemonic('r');
      addLibs.setSelected(WebServicesPlugin.getInstance(myProject).isToAddRequiredLibraries());
    }
  }

  protected Module getForcedModule() {
    return null;
  }

  protected void initVirtualFile(VirtualFile virtualFile) {
    getUrl().getComboBox().setSelectedItem(fixIDEAUrl(virtualFile.getUrl()));
  }

  public static String fixIDEAUrl(String url) {
    return SystemInfo.isWindows ? VfsUtil.fixIDEAUrl(url) : url;
  }

  protected ValidationResult doValidate(ValidationData _data) {
    return doValidateWithData((MyValidationData) _data);
  }

  protected ValidationResult doValidateWithData(MyValidationData data) {
    Object selectedItem = data.currentPackagePrefix;

    if (selectedItem != null) {
      String message = validatePackagePrefix((String) selectedItem);
      if (message != null) {
        return new ValidationResult(message, getPackagePrefix());
      }
    }

    selectedItem = data.currentUrl;
    if (isNotValidUrl(selectedItem)) {
      return new ValidationResult(WSBundle.message("url.is.empty.validation.problem"),getUrl());
    }

    return doValidUrlCheck(data);
  }

  protected static boolean isNotValidUrl(Object selectedItem) {
    return selectedItem == null || selectedItem.toString().length() == 0;
  }

  protected ValidationResult doValidUrlCheck(MyValidationData data) {
    if (isMultipleFileSelection()) {
      final StringTokenizer tokenizer = new StringTokenizer((String)data.currentUrl,SEPARATOR_CHAR);

      while(tokenizer.hasMoreTokens()) {
        final ValidationResult result = checkOneUrl(tokenizer.nextToken(), getUrl().getComboBox());
        if (result != null) return result;
      }
      return null;
    } else {
      return checkOneUrl((String)data.currentUrl, getUrl().getComboBox());
    }
  }

  protected VirtualFile findFileByUrl(String s) {
    if (s == null) s = "";
    if (s.startsWith(LibUtils.FILE_URL_PREFIX)) s = s.substring(LibUtils.FILE_URL_PREFIX.length());

    final String s1 = s;

    return ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {
      public VirtualFile compute() {
        return EnvironmentFacade.getInstance().findRelativeFile(s1, null);
      }
    });
  }

  protected ValidationResult checkOneUrl(String s, JComponent from) {
    VirtualFile relativeFile = findFileByUrl(s);
    if (relativeFile == null) {
      return new ValidationResult(MessageFormat.format(WSBundle.message("file.doesn.t.exist.validation.message"), s), from);
    }

    if (!isAcceptableFile(relativeFile)) {
      return new ValidationResult(
        MessageFormat.format(WSBundle.message("file.0.has.inappropriate.file.type.validation.message"), s),
        from
      );
    }
    return null;
  }

  protected boolean hasClassNameInUI() {
    return false;
  }

  public void setCurrentFile(PsiFile aFile) {
    getUrl().getComboBox().setSelectedItem(
      aFile != null ?
        fixIDEAUrl(aFile.getVirtualFile().getUrl()):
        null
    );
    super.setCurrentFile(aFile);
  }

  private boolean multipleFileSelection = false;

  protected boolean isMultipleFileSelection() { return multipleFileSelection; }

  protected void configureBrowseButton(final Project myProject,
                                       final ComboboxWithBrowseButton wsdlUrl,
                                       final String[] _extensions,
                                       final String selectFileDialogTitle,
                                       final boolean multipleFileSelection) {
    this.multipleFileSelection = multipleFileSelection;

    wsdlUrl.getComboBox().setEditable(true);
    wsdlUrl.getButton().setToolTipText(WebServicesPlugin.message("webservice.browse.tooltip"));
    final List<String> extensions = Arrays.asList(_extensions);
    final boolean useJars = extensions.contains("jar");
    wsdlUrl.getButton().addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          final FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, useJars, useJars, false, isMultipleFileSelection()) {            

            public boolean isFileSelectable(VirtualFile virtualFile) {
              return extensions.indexOf(virtualFile.getExtension()) != -1;
            }
          };

          fileChooserDescriptor.setTitle(selectFileDialogTitle);

          final FileChooserDialog fileChooser = FileChooserFactory.getInstance().createFileChooser(
            fileChooserDescriptor,
            myProject
          );

          VirtualFile initialFile = EnvironmentFacade.getInstance().getProjectFileDirectory(myProject);
          Object selectedItem = wsdlUrl.getComboBox().getSelectedItem();
          if (selectedItem != null && selectedItem.toString().startsWith(LibUtils.FILE_URL_PREFIX)) {
            VirtualFile fileByPath = EnvironmentFacade.getInstance().findRelativeFile(VfsUtil.fixURLforIDEA(selectedItem.toString()), null);
            if (fileByPath != null) initialFile = fileByPath;
          }

          final VirtualFile[] virtualFiles = fileChooser.choose(initialFile, myProject);
          if (virtualFiles != null && virtualFiles.length >= 1) {
            String url = fixIDEAUrl(virtualFiles[0].getUrl());
            for(int i = 1; i < virtualFiles.length; ++i) {
              url += SEPARATOR_CHAR + fixIDEAUrl(virtualFiles[i].getUrl());
            }
            wsdlUrl.getComboBox().setSelectedItem(url);
          }
        }
      }
    );
  }

  protected void configureBrowseButton(final Project myProject,
                                       final ComboboxWithBrowseButton wsdlUrl,
                                       final String[] _extensions,
                                       final String selectFileDialogTitle) {
    configureBrowseButton(myProject, wsdlUrl, _extensions, selectFileDialogTitle, false);
  }

  protected class MyValidationData extends ValidationData {
    Object currentPackagePrefix;
    public Object currentUrl;

    protected void doAcquire() {
      JComboBox packagePrefix = getPackagePrefix();
      currentPackagePrefix = packagePrefix != null ? packagePrefix.getEditor().getItem():null;
      currentUrl = getUrl().getComboBox().getEditor().getItem();
    }
  }

  protected MyValidationData createValidationData() {
    return new MyValidationData();
  }

  protected void doOKAction() {
    final JCheckBox addLib = getAddLibs();
    if (addLib != null) WebServicesPlugin.getInstance(myProject).setToAddRequiredLibraries(addLib.isSelected());
    super.doOKAction();
  }

  protected abstract JCheckBox getAddLibs();
  protected abstract JComboBox getPackagePrefix();
  protected abstract JComboBox getOutputPathes();
  protected abstract JLabel getOutputPathesText();
  protected abstract JLabel getPackagePrefixText();
  protected abstract ComboboxWithBrowseButton getUrl();
  protected abstract JLabel getUrlText();
}
