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

package com.intellij.uml.actions.fs;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import com.intellij.uml.UmlClassDiagramEditorProvider;
import com.intellij.uml.actions.UmlAction;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.utils.UmlUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NonNls;

import java.io.OutputStream;

/**
 * @author Konstantin Bulenkov
 */
public class SaveClassDiagramAction extends UmlAction {
  public void actionPerformed(AnActionEvent e) {
    final Project project = DataKeys.PROJECT.getData(e.getDataContext());
    final GraphBuilder<UmlNode,UmlEdge> builder = getBuilder(e);
    final FileEditor editor = DataKeys.FILE_EDITOR.getData(e.getDataContext());

    if (project == null || builder == null || editor == null) return;

    final PsiElement element = UmlUtils.getDataModel(builder).getInitialElement();
    @NonNls String name = element instanceof PsiClass ?  ((PsiClass)element).getName() : null;
    name = element instanceof PsiPackage ? ((PsiPackage)element).getQualifiedName() : name;
    name = name == null ? "diagram.uml" : name + ".uml";
    Module module = null;
    VirtualFile vf;
    if (element == null) {
      vf = UmlUtils.getDataModel(builder).getFile();
    } else {
      final PsiFile containingFile = element.getContainingFile();
      vf = containingFile == null ? null : containingFile.getVirtualFile();      
    }
    if (vf != null) {
      module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(vf);
    }
    final VirtualFile moduleFile = module == null ? null : module.getModuleFile();
    VirtualFile baseDir = moduleFile == null ? project.getBaseDir() : moduleFile.getParent();

    if (vf != null && vf.getName().endsWith(".uml")) {
      final VirtualFile parent = vf.getParent();
      if (parent != null) baseDir = parent;
      name = vf.getName();
    }

    final VirtualFileWrapper wrapper = FileChooserFactory.getInstance()
      .createSaveFileDialog(new FileSaverDescriptor("Save file", "Save UML class diagram as .uml", "uml"), project)
      .save(baseDir, name);

    if (wrapper == null) return;

    final Document xml = new Document(new Element("ClassDiagramm"));
    UmlClassDiagramEditorProvider.saveUmlState(editor.getState(FileEditorStateLevel.FULL), xml.getRootElement());
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
