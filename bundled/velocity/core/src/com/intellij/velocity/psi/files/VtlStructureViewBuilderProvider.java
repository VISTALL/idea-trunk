/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi.files;

import com.intellij.ide.structureView.StructureView;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.structureView.impl.StructureViewComposite;
import com.intellij.ide.structureView.impl.TemplateLanguageStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.psi.PsiFile;
import com.intellij.velocity.VelocityBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VtlStructureViewBuilderProvider implements PsiStructureViewFactory {

    @Nullable
    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
        return new TemplateLanguageStructureViewBuilder(psiFile) {
            protected StructureViewComposite.StructureViewDescriptor createMainView(final FileEditor fileEditor, final PsiFile mainFile) {
                StructureView mainView = new TreeBasedStructureViewBuilder() {
                    @NotNull
                    public StructureViewModel createStructureViewModel() {
                        return new VtlStructureViewModel((VtlFile) mainFile);
                    }
                }.createStructureView(fileEditor, mainFile.getProject());
                return new StructureViewComposite.StructureViewDescriptor(VelocityBundle.message("tab.structureview.vtl.view"), mainView, mainFile
                        .getFileType().getIcon());
            }
        };
    }
}
