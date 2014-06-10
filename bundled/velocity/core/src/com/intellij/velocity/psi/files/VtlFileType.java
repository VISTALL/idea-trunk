/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi.files;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.TemplateLanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.velocity.Icons;
import com.intellij.velocity.VelocityBundle;
import com.intellij.velocity.psi.VtlLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Alexey Chmutov
 */
public class VtlFileType extends LanguageFileType implements TemplateLanguageFileType {
    public static final VtlFileType INSTANCE = new VtlFileType();

    private VtlFileType() {
        super(VtlLanguage.INSTANCE);
    }

    @NotNull
    @NonNls
    public String getName() {
        return "VTL";
    }

    @NotNull
    public String getDescription() {
        return VelocityBundle.message("file.type.description");
    }

    @NotNull
    public EditorHighlighter getEditorHighlighter(@Nullable final Project project,
                                                  @Nullable final VirtualFile virtualFile,
                                                  @NotNull final EditorColorsScheme colors) {
        return new VtlEditorHighlighter(project, virtualFile, colors);
    }

    @NotNull
    @NonNls
    public String getDefaultExtension() {
        return "vm";
    }

    @Nullable
    public Icon getIcon() {
        return Icons.VTL_ICON;
    }

    @NotNull
    @NonNls
    public String[] getExtensions() {
        return new String[]{getDefaultExtension(), "ft", "vsl"};
    }
}
