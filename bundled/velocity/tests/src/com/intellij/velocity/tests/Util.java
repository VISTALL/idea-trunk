package com.intellij.velocity.tests;

import com.intellij.lang.Language;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import junit.framework.Assert;
import org.jetbrains.annotations.NonNls;

import java.io.FileReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Alexey Chmutov
 *         Date: 03.04.2008
 */
public class Util {
    @NonNls
    static final String INPUT_DATA_FILE_EXT = "test.vm";
    @NonNls
    static final String EXPECTED_RESULT_FILE_EXT = "expected";

    private Util() {
    }

    static String getFileText(final String filePath) {
        try {
            final FileReader reader = new FileReader(filePath);
            return FileUtil.loadTextAndClose(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String getInputData(final String dataSubpath, final String testName) {
        return getFileText(getTestDataFilePath(dataSubpath, testName, INPUT_DATA_FILE_EXT));
    }

    static String getExpectedResultFilePath(final String dataSubpath, final String testName) {
        return getTestDataFilePath(dataSubpath, testName, EXPECTED_RESULT_FILE_EXT);
    }

    private static String getTestDataFilePath(final String dataSubpath, final String testName, final String fileExtension) {
        return PathManager.getHomePath() + "/" + dataSubpath + "/" + testName + "." + fileExtension;
    }

    static String getInputDataFileName(final String testName) {
        return testName + "." + INPUT_DATA_FILE_EXT;
    }

    static String getExpectedResultFileName(final String testName) {
        return testName + "." + EXPECTED_RESULT_FILE_EXT;
    }

    static PsiClass addEmptyJavaClassTo(JavaCodeInsightTestFixture fixture) throws IOException {
        return fixture.addClass("package foo; public class Bar {}");
    }

    static PsiClass addJavaClassTo(JavaCodeInsightTestFixture fixture) throws IOException {
        return fixture.addClass("package my.pack; " +
                "   public class MyClass {" +
                "       public int xField1;" +

                "       private int prop1;" +
                "       public int getProp1() { return prop1; }" +
                "       public void setProp1(int prop1) { this.prop1 = prop1; }" +

                "       private int prop2;" +
                "       public int getProp2() { return prop2; }" +

                "       public MyClass getPropObject() { return this; }" +

                "       private int prop3;" +
                "       public void setProp3(int prop3) { this.prop3 = prop3; }" +

                "       public static void meth1() { ; }" +
                "       public MyClass returnObj() { return this; }" +
                "   }");
    }

    static PsiClass addJavaSubclassTo(JavaCodeInsightTestFixture fixture) throws IOException {
        addJavaClassTo(fixture);
        return fixture.addClass("package my.pack; " +
                "   public class Subclass extends MyClass {" +
                "       protected void meth12() { ; }" +
                "       void meth13() { ; }" +
                "       private void meth14() { ; }" +
                "       public void meth11() { ; }" +
                "   }");
    }

    static PsiClass addJavaClassWithInnerClassTo(JavaCodeInsightTestFixture fixture) throws IOException {
        return fixture.addClass("package my.pack; " +
                "   public class Class2 {" +
                "       public void meth11() { ; }" +
                "       public class InnerClass { } " +
                "       public interface InnerInterface { } " +
                "   }");
    }

    static PsiClass addJavaInterfaceWithOverloadedMethodTo(JavaCodeInsightTestFixture fixture) throws IOException {
        fixture.addClass("package java.lang; public @interface Deprecated {}");
        return fixture.addClass("package my.pack; " +
                "public interface SomeInterface {" +
                "  public void foo();" +
                "  public void foo(int a);" +
                "  public void foo(String a);" +
                "  public void foo(String a, int b);" +
                "  @Deprecated public void foo(int a, String b);" +
                "  public void foo(String a, String b, String c);" +
                "  public int[] getNumbers();" +
                "}");
    }

    static PsiClass addJavaClassWith3Getters(JavaCodeInsightTestFixture fixture) throws IOException {
        return fixture.addClass("package my.pack; " +
                "public class GetterOwner {" +
                "  public Object getlowerCase() { return null;}" +
                "  public Object getLowerCase() { return null;}" +
                "  public Object get(String s) { return s;}" +
                "}");
    }

    static PsiClass addJavaClassWith2Getters(JavaCodeInsightTestFixture fixture) throws IOException {
        return fixture.addClass("package my.pack; " +
                "public class GetterOwner {" +
                "  public Object getUpperCase() { return null;}" +
                "  public Object get(String s) { return s;}" +
                "}");
    }

    static void mapTemplateDataLanguageFor(PsiFile file, Language dataLanguage) {
        TemplateDataLanguageMappings mappings = TemplateDataLanguageMappings.getInstance(file.getProject());
        mappings.setMapping(file.getViewProvider().getVirtualFile(), dataLanguage);
        Assert.assertSame(dataLanguage, mappings.getMapping(file.getViewProvider().getVirtualFile()));
    }

}