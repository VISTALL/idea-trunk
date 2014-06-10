package com.intellij.coldFusion.UI.editorActions.structureView;

import com.intellij.ide.structureView.StructureView;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.structureView.impl.StructureViewComposite;
import com.intellij.ide.structureView.impl.TemplateLanguageStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 20.01.2009
 */
public class CfmlStructureViewProvider implements PsiStructureViewFactory {
    /* // to view html tags structure
  @Nullable
  public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
    return new TemplateLanguageStructureViewBuilder(psiFile) {
      protected StructureViewComposite.StructureViewDescriptor createMainView(final FileEditor fileEditor,
                                                                              final PsiFile mainFile) {
        return null;
      }
    };
  }
  */

    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
        return new TemplateLanguageStructureViewBuilder(psiFile) {
            protected StructureViewComposite.StructureViewDescriptor createMainView(final FileEditor fileEditor, final PsiFile mainFile) {
                StructureView mainView = new TreeBasedStructureViewBuilder() {
                    @NotNull
                    public StructureViewModel createStructureViewModel() {
                        return new CfmlStructureViewModel(psiFile);
                    }
                }.createStructureView(fileEditor, mainFile.getProject());
                return new StructureViewComposite.StructureViewDescriptor("CFML View", mainView, mainFile.getFileType().getIcon());
            }
        };
        /*

        return new TreeBasedStructureViewBuilder() {
            @NotNull
            public StructureViewModel createStructureViewModel() {
                return new CfmlStructureViewModel(psiFile,
                        new TemplateLanguageStructureViewBuilder(psiFile) {
                            protected StructureViewComposite.StructureViewDescriptor createMainView(final FileEditor fileEditor,
                                                                                                    final PsiFile mainFile) {
                                return null;
                            }
                        });
            }
        };
        */
    }
}
