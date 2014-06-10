package com.intellij.coldFusion.model.files;

import com.intellij.coldFusion.UI.CfmlIcons;
import com.intellij.coldFusion.UI.highlighting.CfmlHighlighter;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Lera Nikolaenko
 * Date: 30.09.2008
 */
public class CfmlFileType extends LanguageFileType {
    public static final CfmlFileType INSTANCE = new CfmlFileType();

    private CfmlFileType() {
        super(CfmlLanguage.INSTANCE);
    }

    @NotNull
    public String getName() {
        return "CFML";
    }

    @NotNull
    public String getDescription() {
        return "Cold Fusion";
    }

    @NotNull
    public String getDefaultExtension() {
        return "cfm";
    }

    public Icon getIcon() {
        return CfmlIcons.FILETYPE_ICON;
    }

    @NotNull
    public EditorHighlighter getEditorHighlighter(@Nullable final Project project,
                                                  @Nullable final VirtualFile virtualFile,
                                                  @NotNull final EditorColorsScheme colors) {
        return new CfmlHighlighter(project, virtualFile, colors);
    }

    @NotNull
    @NonNls
    public String[] getExtensions() {
        return new String[]{"cfm", "cfml", "cfc"};
    }
}
