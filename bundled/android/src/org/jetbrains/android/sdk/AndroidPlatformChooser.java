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

package org.jetbrains.android.sdk;

import com.android.sdklib.IAndroidTarget;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 13, 2009
 * Time: 8:00:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidPlatformChooser implements Disposable {
  private final AndroidLibraryManager myLibraryManager;
  private AndroidPlatformsComboBox myPlatformsComboBox;
  private JButton myNewButton;
  private JButton myEditButton;
  private JPanel myPanel;
  private JButton myRemoveButton;
  private JPanel myComboBoxWrapper;
  private JButton myViewClasspathButton;
  private final Project myProject;

  // project is null, if we aren't inside ProjectStructure 
  public AndroidPlatformChooser(@NotNull LibraryTable.ModifiableModel model, @Nullable Project project) {
    myProject = project;
    myLibraryManager = new AndroidLibraryManager(model);
    myPlatformsComboBox = new AndroidPlatformsComboBox(myLibraryManager.getModel(), myLibraryManager.getLibraryModels());
    myComboBoxWrapper.setLayout(new BorderLayout(1, 1));
    myComboBoxWrapper.add(myPlatformsComboBox);
    myNewButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VirtualFile[] files = AndroidSdkUtils.chooseAndroidSdkPath(myPanel);
        if (files.length > 0) {
          assert files.length == 1;
          final String path = files[0].getPath();
          AndroidSdk newSdk = AndroidSdk.parse(path, myPanel);
          if (newSdk != null) {
            final IAndroidTarget[] targets = newSdk.getTargets();
            if (targets.length == 0) {
              Messages.showErrorDialog(myPanel, AndroidBundle.message("no.android.targets.error"));
              return;
            }
            int selected = 0;
            if (targets.length > 1) {
              String[] targetPresentableNames = new String[targets.length];
              for (int i = 0, targetsLength = targets.length; i < targetsLength; i++) {
                targetPresentableNames[i] = AndroidSdkUtils.getPresentableTargetName(targets[i]);
              }
              selected = Messages.showChooseDialog(myPanel, AndroidBundle.message("select.target.dialog.text"),
                                                   AndroidBundle.message("select.target.dialog.title"), targetPresentableNames,
                                                   newSdk.getDefaultTargetName(), Messages.getQuestionIcon());
            }
            if (selected >= 0) {
              final int finalSelected = selected;
              Library library = ApplicationManager.getApplication().runWriteAction(new Computable<Library>() {
                public Library compute() {
                  return myLibraryManager.createNewAndroidPlatform(targets[finalSelected], path);
                }
              });
              myPlatformsComboBox.addLibrary(library);
              myPlatformsComboBox.setSelectedItem(library);
            }
          }
        }
      }
    });
    myEditButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        AndroidPlatform platform = myPlatformsComboBox.getSelectedPlatform();
        assert platform != null;
        Library library = (Library)myPlatformsComboBox.getSelectedItem();
        AndroidPlatformEditor editor = new AndroidPlatformEditor(myPanel, platform, myLibraryManager.getModifiableModelForLibrary(library));
        editor.show();
        myPlatformsComboBox.rebuildPlatforms();
        myPlatformsComboBox.setSelectedItem(library);
      }
    });
    myRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Library library = getSelectedPlatform().getLibrary();
        myLibraryManager.removeLibrary(library);
        myPlatformsComboBox.removeLibrary(library);
      }
    });
    boolean insideProjectStructure = project != null;
    myViewClasspathButton.setEnabled(insideProjectStructure);
    if (insideProjectStructure) {
      myViewClasspathButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Library library = getSelectedPlatform().getLibrary();
          ProjectStructureConfigurable.getInstance(myProject).selectProjectOrGlobalLibrary(library, true);
        }
      });
    }
    Disposer.register(this, myPlatformsComboBox);
    myPlatformsComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateComponents();
      }
    });
    updateComponents();
  }

  private void updateComponents() {
    boolean enabled = myPlatformsComboBox.getSelectedPlatform() != null;
    myEditButton.setEnabled(enabled);
    myRemoveButton.setEnabled(enabled);
    myViewClasspathButton.setEnabled(myProject != null && enabled);
  }

  @Nullable
  public AndroidPlatform getSelectedPlatform() {
    return myPlatformsComboBox.getSelectedPlatform();
  }

  public JPanel getComponent() {
    return myPanel;
  }

  public void setSelectedPlatform(@Nullable AndroidPlatform platform) {
    if (platform == null) {
      myPlatformsComboBox.setSelectedItem(null);
    }
    else {
      myPlatformsComboBox.setSelectedItem(platform.getLibrary());
    }
  }

  public void apply() {
    myLibraryManager.apply();
  }

  public void rebuildPlatforms() {
    myPlatformsComboBox.rebuildPlatforms();
  }

  public void dispose() {
  }
}
