package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;

/**
 * Created by Lera Nikolaenko
 * Date: 06.10.2008
 */
public interface CfmlElementTypes {
    // IElementType SOME = new CfmlElementType("SOME");
    IElementType CF_SCRIPT = new CfmlElementType("CF_SCRIPT");

    IElementType TEMPLATE_TEXT = new CfmlElementType("CFML_TEMPLATE_TEXT");

    IElementType OUTER_ELEMENT_TYPE = new CfmlElementType("CFML_FRAGMENT");

    IElementType SQL = new CfmlElementType("SQL");

    IElementType SQL_DATA = new TemplateDataElementType("SQL_DATA", CfmlLanguage.INSTANCE, SQL,
            OUTER_ELEMENT_TYPE);
    TemplateDataElementType TEMPLATE_DATA =
      new TemplateDataElementType("CFML_TEMPLATE_DATA", CfmlLanguage.INSTANCE, TEMPLATE_TEXT, OUTER_ELEMENT_TYPE);

    IFileElementType CFML_FILE = new IFileElementType("CFML_FILE", CfmlLanguage.INSTANCE);
    IElementType CFML_FILE_CONTENT = new CfmlElementType("CFML_FILE_CONTENT");
}
