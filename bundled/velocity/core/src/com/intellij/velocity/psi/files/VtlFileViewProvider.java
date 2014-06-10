/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi.files;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutors;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.psi.templateLanguages.TemplateLanguage;
import com.intellij.velocity.psi.VtlLanguage;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Alexey Chmutov
 */
public class VtlFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider implements ConfigurableTemplateLanguageFileViewProvider {
    private final Language myTemplateDataLanguage;

    public VtlFileViewProvider(final PsiManager manager, final VirtualFile virtualFile, final boolean physical) {
        super(manager, virtualFile, physical);
        final Language language = getTemplateDataLanguage(virtualFile, manager.getProject());
        myTemplateDataLanguage = language instanceof TemplateLanguage ? PlainTextLanguage.INSTANCE : LanguageSubstitutors.INSTANCE.substituteLanguage(language, virtualFile, manager.getProject());
    }

    public VtlFileViewProvider(final PsiManager manager, final VirtualFile virtualFile, final boolean physical,
                               final Language templateDataLanguage) {
        super(manager, virtualFile, physical);
        myTemplateDataLanguage = templateDataLanguage;
    }

    @NotNull
    public Language getBaseLanguage() {
        return VtlLanguage.INSTANCE;
    }

    @NotNull
    public Set<Language> getLanguages() {
        return new THashSet<Language>(Arrays.asList(VtlLanguage.INSTANCE, getTemplateDataLanguage()));
    }

    @Nullable
    protected PsiFile createFile(final Language lang) {
        if (lang == getBaseLanguage()) {
            return createFileInner(lang);
        }
        if (lang == getTemplateDataLanguage()) {
            final PsiFileImpl file = (PsiFileImpl) createFileInner(lang);
            file.setContentElementType(VtlFileElementTypes.TEMPLATE_DATA);
            return file;
        }
        return null;
    }

    private PsiFile createFileInner(Language lang) {
        return LanguageParserDefinitions.INSTANCE.forLanguage(lang).createFile(this);
    }

    protected VtlFileViewProvider cloneInner(final VirtualFile copy) {
        return new VtlFileViewProvider(getManager(), copy, false, myTemplateDataLanguage);
    }

    @NotNull
    public Language getTemplateDataLanguage() {
        return myTemplateDataLanguage;
    }

    @NotNull
    static Language getTemplateDataLanguage(@NotNull VirtualFile virtualFile, @NotNull Project project) {
        final Language language = TemplateDataLanguageMappings.getInstance(project).getMapping(virtualFile);
        return language == null ? getTemplateDataLanguageByExtention(virtualFile) : language;
    }

    @NotNull
    private static Language getTemplateDataLanguageByExtention(VirtualFile virtualFile) {
        String name = virtualFile.getName();
        int index2 = name.lastIndexOf('.');
        if (index2 < 3) {
            return StdLanguages.HTML;
        }
        int index1 = 1 + name.lastIndexOf('.', index2 - 1);
        if (index1 < 1) {
            return StdLanguages.HTML;
        }
        String dataLanguageFileExtension = name.substring(index1, index2).toUpperCase();
        FileType fileType = FileTypeManager.getInstance().getStdFileType(dataLanguageFileExtension);
        if (fileType instanceof LanguageFileType) {
            return ((LanguageFileType) fileType).getLanguage();
        }
        return StdLanguages.HTML;
    }
}
