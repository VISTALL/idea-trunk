package com.intellij.spring.model.structure;

import com.intellij.ide.structureView.StructureView;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpringXmlStructureViewBuilderProvider implements XmlStructureViewBuilderProvider {
  @Nullable
  public StructureViewBuilder createStructureViewBuilder(@NotNull final XmlFile file) {
    final SpringManager springManager = SpringManager.getInstance(file.getProject());
    if (springManager.isSpringBeans(file)) {
      final SpringModel localSpringModel = springManager.getLocalSpringModel(file);
      if (localSpringModel != null) {
        return new TreeBasedStructureViewBuilder() {
          @NotNull
          public StructureViewModel createStructureViewModel() {
            return new SpringStructureViewModel(file) {
              @NotNull
              public Sorter[] getSorters() {
                final SpringManager springManager = SpringManager.getInstance(file.getProject());
                return springManager.isSpringBeans(file) ? new Sorter[]{Sorter.ALPHA_SORTER} : Sorter.EMPTY_ARRAY;
              }
            };
          }

          public boolean isRootNodeShown() {
            return true;
          }

          @NotNull
          public StructureView createStructureView(final FileEditor fileEditor, final Project project) {
            return new SpringStructureViewComponent(fileEditor, createStructureViewModel(), project);
          }
        };
      }
    }
    return null;
  }


}
