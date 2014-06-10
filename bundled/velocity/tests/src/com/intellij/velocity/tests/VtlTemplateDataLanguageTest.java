package com.intellij.velocity.tests;

import com.intellij.lang.Language;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.velocity.psi.VtlLanguage;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.files.VtlFileViewProvider;
import com.intellij.velocity.psi.files.VtlFileViewProviderFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 18.06.2008
 */
public class VtlTemplateDataLanguageTest extends JavaCodeInsightFixtureTestCase {
    public void testManuallyMappedDataLanguage() throws Throwable {
        VtlFile file = createFile("file1.vm");
        Util.mapTemplateDataLanguageFor(file, StdLanguages.JAVA);
        VirtualFile vFile = file.getViewProvider().getVirtualFile();
        VtlFileViewProvider vtlFileViewProvider = new VtlFileViewProviderFactory().createFileViewProvider(vFile, VtlLanguage.INSTANCE, getPsiManager(), false);
        assertSame(StdLanguages.JAVA, vtlFileViewProvider.getTemplateDataLanguage());
    }

    public void testDataLanguageFromExtension() throws Throwable {
        VtlFile file = createFile("file1.java.vm");
        Language dataLanguage = getTemplateDataLanguage(file);
        assertSame(StdLanguages.JAVA, dataLanguage);
    }

    public void testDataLanguageDefaultsToHtml() throws Throwable {
        VtlFile file = createFile("file1.vm");
        Language dataLanguage = getTemplateDataLanguage(file);
        assertSame(StdLanguages.HTML, dataLanguage);
    }

    private Language getTemplateDataLanguage(VtlFile file) {
        FileViewProvider fileViewProvider = file.getViewProvider();
        VtlFileViewProvider vtlFileViewProvider = assertInstanceOf(fileViewProvider, VtlFileViewProvider.class);
        return vtlFileViewProvider.getTemplateDataLanguage();
    }

    private VtlFile createFile(String name) {
        PsiFile file = PsiFileFactory.getInstance(myFixture.getProject()).createFileFromText(name, "");
        assertInstanceOf(file, VtlFile.class);
        return (VtlFile) file;
    }
}
