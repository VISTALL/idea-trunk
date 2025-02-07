/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.ruby.support.ui.entriesEditor;

import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by IntelliJ IDEA.
*
* @author: Roman Chernyatchik
* @date: Aug 20, 2007
*/
public abstract class RContentEntryEditorListenerAdapter implements RContentEntryEditorListener {
    public void editingStarted() {}

    public void beforeEntryDeleted(){}

    public void folderAdded(VirtualFile folder){}

    public void folderRemoved(VirtualFile file){}

    public void folderExcluded(VirtualFile file){}

    public void folderIncluded(VirtualFile file){}

    public void navigationRequested(RContentEntryEditor editor, VirtualFile file){}

    public void packagePrefixSet(SourceFolder folder){}
}
