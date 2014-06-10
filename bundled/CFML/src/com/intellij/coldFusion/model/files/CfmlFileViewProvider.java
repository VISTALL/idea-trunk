package com.intellij.coldFusion.model.files;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.psi.CfmlElementTypes;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import com.intellij.sql.psi.SqlLanguage;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by Lera Nikolaenko
 * Date: 06.10.2008
 */
class CfmlFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider implements TemplateLanguageFileViewProvider {
    private static Language mySqlLanguageInstance = null;

    static {
        for (Language regLanguage : Language.getRegisteredLanguages()) {
            if (regLanguage.getID().equals("SQL")) {
                mySqlLanguageInstance = regLanguage;
            }
        }
        if (mySqlLanguageInstance == null) {
            mySqlLanguageInstance = SqlLanguage.getInstance();
        }

    }

    private static final THashSet<Language> ourRelevantLanguages =
            new THashSet<Language>(Arrays.asList(StdLanguages.HTML, CfmlLanguage.INSTANCE,
                    mySqlLanguageInstance));


    public CfmlFileViewProvider(final PsiManager manager, final VirtualFile virtualFile, final boolean physical) {
        super(manager, virtualFile, physical);
    }

    @NotNull
    public Language getBaseLanguage() {
        return CfmlLanguage.INSTANCE;
    }

    @NotNull
    public Set<Language> getLanguages() {
        return ourRelevantLanguages;
    }

    @Nullable
    protected PsiFile createFile(final Language lang) {
        if (lang == getTemplateDataLanguage()) {
            // final PsiFileImpl file = (PsiFileImpl)LanguageParserDefinitions.INSTANCE.forLanguage(lang).createFile(this);

            final PsiFileImpl file = (PsiFileImpl) LanguageParserDefinitions.INSTANCE.forLanguage(StdLanguages.HTML).createFile(this);
            file.setContentElementType(CfmlElementTypes.TEMPLATE_DATA);
            return file;
        }
        if (lang == mySqlLanguageInstance) {
            final PsiFileImpl file = (PsiFileImpl) LanguageParserDefinitions.INSTANCE.forLanguage(mySqlLanguageInstance).createFile(this);
            file.setContentElementType(CfmlElementTypes.SQL_DATA);
            return file;
        }

        if (lang == getBaseLanguage()) {
            return LanguageParserDefinitions.INSTANCE.forLanguage(lang).createFile(this);
        }
        return null;
    }

    protected CfmlFileViewProvider cloneInner(final VirtualFile copy) {
        return new CfmlFileViewProvider(getManager(), copy, false);
    }

    @NotNull
    public Language getTemplateDataLanguage() {
        return StdLanguages.HTML;
    }

    @NotNull
    @Override
    public PsiManager getManager() {
        return super.getManager();    //To change body of overridden methods use File | Settings | File Templates.
    }
}

