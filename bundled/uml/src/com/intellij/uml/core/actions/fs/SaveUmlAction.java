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

package com.intellij.uml.core.actions.fs;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.uml.*;
import com.intellij.uml.core.actions.UmlAction;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.OutputStream;

/**
 * @author Konstantin Bulenkov
 */
public class SaveUmlAction extends UmlAction {
  public void actionPerformed(AnActionEvent e) {
    final Project project = DataKeys.PROJECT.getData(e.getDataContext());
    final GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    final FileEditor fileEditor = DataKeys.FILE_EDITOR.getData(e.getDataContext());

    if (project == null || builder == null || fileEditor == null || !(fileEditor instanceof UmlFileEditor)) return;

    String filename = "diagram.uml";
    VirtualFile baseDir = null;
    final UmlFileEditor editor = (UmlFileEditor)fileEditor;
    final VirtualFile vf = editor.getOriginalVirtualFile();
    if (vf != null && vf.getFileSystem() instanceof LocalFileSystem) {
      filename = vf.getName();
      baseDir = vf.getParent();
    } else {
      final String fqn = Utils.getProvider(builder).getElementManager().getElementTitle(editor.getOriginalElement());
      if (fqn != null) {
        filename = fqn + ".uml";
      }
    }

    //Module module = null;
    //VirtualFile vf;
    //if (fqn == null) {
    //  vf = Utils.getDataModel(builder).getFile();
    //} else {
    //  final PsiFile containingFile = element.getContainingFile();
    //  vf = containingFile == null ? null : containingFile.getVirtualFile();
    //}
    //if (vf != null) {
    //  module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(vf);
    //}
    //final VirtualFile moduleFile = module == null ? null : module.getModuleFile();
    //VirtualFile baseDir = moduleFile == null ? project.getBaseDir() : moduleFile.getParent();
    //
    //if (vf != null && vf.getName().endsWith(".uml")) {
    //  final VirtualFile parent = vf.getParent();
    //  if (parent != null) baseDir = parent;
    //  filename = vf.getName();
    //}

    final VirtualFileWrapper wrapper = FileChooserFactory.getInstance()
      .createSaveFileDialog(new FileSaverDescriptor("Save file", "Save UML class diagram as .uml", "uml"), project)
      .save(baseDir, filename);

    if (wrapper == null) return;

    final Document xml = new Document(new Element("Diagramm"));
    UmlEditorProvider.saveUmlState(editor.getState(FileEditorStateLevel.FULL), xml.getRootElement());
    OutputStream os;

    try {
      final VirtualFile file = wrapper.getVirtualFile(true);
      final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(file);
      if (status.hasReadonlyFiles()) {
        Messages.showErrorDialog(project, "Can't save UML diagram to " + wrapper.getFile().getPath() + " File is read-only", "Error");
        return;
      }
      os = file.getOutputStream(null);
      final XMLOutputter out = new XMLOutputter();
      out.setFormat(Format.getPrettyFormat());
      out.output(xml, os);
      os.close();
    } catch (Exception e1) {
      Messages.showErrorDialog(project, "Can't save UML diagram to " + wrapper.getFile().getPath(), "Error");
    }         
  }
}
