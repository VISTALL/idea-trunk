package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;

/**
 * Created by Lera Nikolaenko
 * Date: 17.02.2009
 */
public class CfmlChangeUtil {
    public static ASTNode createStringTextElement(Project project, String name) {
        /*
        final CfmlFile dummyFile = getDummyFile(project,
                "<cfinclude template = \"" + name + "\">");
        return  dummyFile.getNode().getFirstChildNode().findChildByType(CfmlTokenTypes.ATTRIBUTE).findChildByType(CfmlTokenTypes.STRING_TEXT);
        */
        return null;
    }

    public static ASTNode createNameIdentifier(Project project, String name) {
/*
        final CfmlFile dummyFile = getDummyFile(project, "<cfset " + name + ">");
        return dummyFile.getFirstChild().getNode().findChildByType(CfmlElementTypes.ASSIGNMENT).findChildByType(CfscriptElementTypes.VAR_DEF).getFirstChildNode();
*/
        return null;
    }

    private static CfmlFile getDummyFile(Project project, String fileText) {
/*
        final String fileName = "dummy." + CfmlFileType.INSTANCE.getDefaultExtension();
        return (CfmlFile) PsiFileFactory.getInstance(project).
                createFileFromText(fileName, CfmlLanguage.INSTANCE, fileText);
*/
        return null;
    }
}
