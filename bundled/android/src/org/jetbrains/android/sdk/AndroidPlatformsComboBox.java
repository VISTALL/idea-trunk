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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 15, 2009
 * Time: 4:31:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidPlatformsComboBox extends JComboBox implements Disposable {
  private final Map<String, AndroidSdk> myParsedSdks = new HashMap<String, AndroidSdk>();
  private final Map<Library, AndroidPlatform> myCachedPlatforms = new HashMap<Library, AndroidPlatform>();
  private DefaultComboBoxModel myModel;
  private final LibraryTable.ModifiableModel myLibraryTableModel;
  private final Map<Library, Library.ModifiableModel> myModelMap;
  private Condition<Library> myFilter;

  private boolean checkLibraryAndUpdateCache(@NotNull Library library) {
    Library.ModifiableModel model = myModelMap != null ? myModelMap.get(library) : library.getModifiableModel();
    if (model == null && myLibraryTableModel instanceof LibrariesModifiableModel) {
      model = ((LibrariesModifiableModel)myLibraryTableModel).getLibraryEditor(library).getModel();
    }
    AndroidPlatform platform = AndroidPlatform.parse(library, model, myParsedSdks);
    if (platform != null) {
      myCachedPlatforms.put(library, platform);
      return true;
    }
    else {
      myCachedPlatforms.remove(library);
    }
    return false;
  }

  public void setFilter(Condition<Library> filter) {
    myFilter = filter;
  }

  public AndroidPlatformsComboBox(@NotNull LibraryTable.ModifiableModel libraryTableModel,
                                  @Nullable Map<Library, Library.ModifiableModel> modelMap) {
    myLibraryTableModel = libraryTableModel;
    myModelMap = modelMap;
    setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value != null) {
          Library library = (Library)value;
          setIcon(AndroidUtils.ANDROID_ICON);
          setText(library.getName());
        }
        else {
          setText("<html><font color='red'>[none]</font></html>");
        }
        return this;
      }
    });
  }

  public void addLibrary(final Library library) {
    if (checkLibraryAndUpdateCache(library)) {
      myModel.addElement(library);
    }
  }

  public void removeLibrary(Library library) {
    myModel.removeElement(library);
    myCachedPlatforms.remove(library);
  }

  public void rebuildPlatforms() {
    myCachedPlatforms.clear();
    Library[] libraries = myLibraryTableModel.getLibraries();
    List<Library> platforms = new ArrayList<Library>();
    for (Library library : libraries) {
      if ((myFilter == null || myFilter.value(library)) && checkLibraryAndUpdateCache(library)) {
        platforms.add(library);
      }
    }
    myModel = new DefaultComboBoxModel(platforms.toArray(new Library[platforms.size()]));
    setModel(myModel);
    if (myModel.getSize() > 0) {
      setSelectedIndex(0);
    }
  }

  @Nullable
  public AndroidPlatform getSelectedPlatform() {
    Library library = (Library)getSelectedItem();
    if (library != null) {
      return myCachedPlatforms.get(library);
    }
    return null;
  }

  public void dispose() {
  }
}
