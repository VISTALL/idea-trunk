package com.intellij.coldFusion.model;

import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateLanguage;

/**
 * Created by Lera Nikolaenko
 * Date: 30.09.2008
 */
public class CfmlLanguage extends Language implements TemplateLanguage {
    public static final CfmlLanguage INSTANCE = new CfmlLanguage();

    private CfmlLanguage() {
        super("CFML");
        /*
        LanguageParserDefinitions.INSTANCE.addExplicitExtension(this, new CfmlParserDefinition());
        LanguageBraceMatching.INSTANCE.addExplicitExtension(this, new CfmlPairedBraceMatcher());
        BraceMatchingUtil.registerBraceMatcher(CfmlFileType.INSTANCE, new CfmlBraceMatcher());
        */
    }
}