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

package com.advancedtools.webservices.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @by maxim
 */
public abstract class BaseWSFromFileAction extends BaseWSAction {
  public void update(AnActionEvent e) {
    super.update(e);
    if (! e.getPresentation().isEnabled()) return;

    VirtualFile file = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
    boolean status = file != null && isAcceptableFile(file);
    e.getPresentation().setEnabled(status);
    e.getPresentation().setVisible(status);
  }

  public boolean isAcceptableFile(VirtualFile file) {
    return file.getFileType() == StdFileTypes.JAVA;
  }
}
