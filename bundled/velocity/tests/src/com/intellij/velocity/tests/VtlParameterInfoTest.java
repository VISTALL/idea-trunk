package com.intellij.velocity.tests;

import com.intellij.codeInsight.hint.ShowParameterInfoContext;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.velocity.VtlParameterInfoHandler;
import com.intellij.velocity.psi.VtlArgumentList;
import com.intellij.velocity.psi.VtlCallExpression;
import com.intellij.velocity.psi.VtlCallable;
import static org.easymock.EasyMock.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author Alexey Chmutov
 */
public class VtlParameterInfoTest extends JavaCodeInsightFixtureTestCase {

    protected String getBasePath() {
        return "/svnPlugins/velocity/tests/testData/paramInfo/";
    }

    public void testJavaMethodName() throws Throwable {
        myFixture.addClass("package my.pack; " +
                "public interface SomeInterface {" +
                "  public void foo();" +
                "  public void foo(String a);" +
                "}");
        doParamsTest(-1, new ParamInfo("<no parameters>", -1, -1, false, false),
                new ParamInfo("String a", -1, -1, false, false));
    }

    public void testJavaMethodFirstParam() throws Throwable {
        myFixture.addClass("package my.pack; " +
                "public interface SomeInterface {" +
                "  public void foo(int a);" +
                "  public void foo(String a);" +
                "}");
        doParamsTest(0,
                new ParamInfo("int a", 0, 5, false, false),
                new ParamInfo("String a", 0, 8, false, false));
    }

    public void testJavaMethodWithTemplateTextArg() throws Throwable {
        myFixture.addClass("package my.pack; " +
                "public interface SomeInterface {" +
                "  public void foo(int a);" +
                "  public void foo(String a);" +
                "}");

//        myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
//        Util.mapTemplateDataLanguageFor(myFixture.getFile(), StdLanguages.JAVA);
        doParamsTest(0,
                new ParamInfo("int a", 0, 5, false, false),
                new ParamInfo("String a", 0, 8, false, false));
    }

    public void testMacroWithTemplateTextArg() throws Throwable {
        doParamsTest(0, new ParamInfo("$a", 0, 2, false, false));
    }

    public void testJavaMethodSecondParam() throws Throwable {
        Util.addJavaInterfaceWithOverloadedMethodTo(myFixture);
        doParamsTest(1,
                new ParamInfo("<no parameters>", -1, -1, true, false),
                new ParamInfo("int a", -1, -1, true, false),
                new ParamInfo("String a", -1, -1, true, false),
                new ParamInfo("String a, int b", 10, 15, false, false),
                new ParamInfo("int a, String b", 7, 15, true, true),
                new ParamInfo("String a, String b, String c", 10, 18, false, false)
        );
    }

    public void testJavaMethodNoParams() throws Throwable {
        myFixture.addClass("package my.pack; " +
                "public interface SomeInterface {" +
                "  public void foo();" +
                "}");
        doParamsTest(0, new ParamInfo("<no parameters>", -1, -1, false, false));
    }

    public void testJavaMethodNoParamsOverload() throws Throwable {
        myFixture.addClass("package my.pack; " +
                "public interface SomeInterface {" +
                "  public void foo();" +
                "  public void foo(String a);" +
                "}");
        doParamsTest(0, new ParamInfo("<no parameters>", -1, -1, false, false),
                new ParamInfo("String a", 0, 8, false, false));
    }

    public void testJavaMethodNoParamsDisabled() throws Throwable {
        myFixture.addClass("package my.pack; " +
                "public interface SomeInterface {" +
                "  public void foo();" +
                "  public void foo(String a, String b);" +
                "}");
        doParamsTest(1, new ParamInfo("<no parameters>", -1, -1, false, false),
                new ParamInfo("String a, String b", 10, 18, false, false));
    }

    public void testMacroName() throws Throwable {
        doParamsTest(-1, new ParamInfo("$a $b", -1, -1, false, false));
    }

    public void testMacroFirstParam() throws Throwable {
        doParamsTest(0, new ParamInfo("$a $b", 0, 2, false, false));
    }

    public void testMacroSecondParam() throws Throwable {
        doParamsTest(1, new ParamInfo("$a $b", 3, 5, false, false));
    }

    public void testMoveCaretToAnotherArgumentList() throws Throwable {
        doParamsTest(1, new ParamInfo("$a $b", 3, 5, false, false));

        VtlArgumentList argumentList = getArgumentListAtCaret();

        myFixture.getEditor().getCaretModel().moveCaretRelatively(0, 1, false, false, false);
        VtlArgumentList anotherArgumentList = getArgumentListAtCaret();
        assertNotSame(argumentList, anotherArgumentList);

        final UpdateParameterInfoContext updateContext = createMock(UpdateParameterInfoContext.class);

        expect(updateContext.getParameterOwner()).andReturn(argumentList);
        updateContext.setParameterOwner(anotherArgumentList);
        updateContext.removeHint();
        updateContext.setCurrentParameter(-1);
        replay(updateContext);

        final VtlParameterInfoHandler handler = new VtlParameterInfoHandler();
        handler.updateParameterInfo(anotherArgumentList, updateContext);
        verify(updateContext);
    }

    private VtlArgumentList getArgumentListAtCaret() {
        final VtlCallExpression call = PsiTreeUtil.findElementOfClassAtOffset(myFixture.getFile(),
                myFixture.getEditor().getCaretModel().getOffset(), VtlCallExpression.class, false);
        assertNotNull(call);
        return call.findArgumentList();
    }

    private CreateParameterInfoContext doParamsTest(final int paramIndex, final ParamInfo... expected) throws Throwable {
        if (myFixture.getFile() == null) {
            myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
        }
        final VtlParameterInfoHandler handler = new VtlParameterInfoHandler();
        final CreateParameterInfoContext createContext = createCreateParameterInfoContext(myFixture);

        final VtlArgumentList argumentList = assertInstanceOf(handler.findElementForParameterInfo(createContext), VtlArgumentList.class);
        handler.showParameterInfo(argumentList, createContext);

        checkParameterIndex(argumentList, createContext, paramIndex, handler);

        final Object[] items = createContext.getItemsToShow();
        assertNotNull(items);
        assertEquals(expected.length, items.length);
        for (int i = paramIndex; 0 <= i && i < items.length; i++) {
            checkParamInfo(argumentList, paramIndex, (VtlCallable) items[i], expected[i], handler);
        }
        return createContext;
    }

    private static CreateParameterInfoContext createCreateParameterInfoContext(CodeInsightTestFixture fixture) {
        return new ShowParameterInfoContext(fixture.getEditor(), fixture.getProject(), fixture.getFile(), fixture.getEditor().getCaretModel().getOffset(), 0) {
            @Override
            public void showHint(final PsiElement element, final int offset, final ParameterInfoHandler handler) {
            }
        };
    }

    private static void checkParameterIndex(@NotNull final VtlArgumentList argumentList, final CreateParameterInfoContext createContext, final int paramIndex, VtlParameterInfoHandler handler) {
        final UpdateParameterInfoContext updateContext = createMock(UpdateParameterInfoContext.class);

        expect(updateContext.getOffset()).andReturn(createContext.getOffset());
        expect(updateContext.getFile()).andReturn(createContext.getFile());
        expect(updateContext.getEditor()).andReturn(createContext.getEditor());

        expect(updateContext.getParameterOwner()).andReturn(createContext.getHighlightedElement());
        expect(updateContext.getEditor()).andReturn(createContext.getEditor());
        expect(updateContext.getObjectsToView()).andStubReturn(createContext.getItemsToShow());
        updateContext.setParameterOwner(argumentList);
        updateContext.setCurrentParameter(paramIndex);
        replay(updateContext);

        VtlArgumentList foundForUpdating = handler.findElementForUpdatingParameterInfo(updateContext);
        assertSame(argumentList, foundForUpdating);
        handler.updateParameterInfo(foundForUpdating, updateContext);
        verify(updateContext);
    }

    private void checkParamInfo(final VtlArgumentList argumentList, final int paramIndex, final VtlCallable type,
                                final ParamInfo info, VtlParameterInfoHandler handler) {
        final ParameterInfoUIContext uiContext = createMock(ParameterInfoUIContext.class);
        expect(uiContext.getCurrentParameterIndex()).andStubReturn(paramIndex);
        expect(uiContext.getParameterOwner()).andStubReturn(argumentList);
        expect(uiContext.getDefaultParameterColor()).andStubReturn(Color.RED);
        uiContext.setupUIComponentPresentation(info.text, info.highlightStartOffset, info.highlightEndOffset, info.disabled, info.strikeout, false, Color.RED);
        replay(uiContext);
        handler.updateUI(type, uiContext);
        verify(uiContext);
    }

    private static class ParamInfo {
        public final String text;
        public final int highlightStartOffset;
        public final int highlightEndOffset;
        public final boolean disabled;
        public final boolean strikeout;

        ParamInfo(final String text,
                  final int highlightStartOffset,
                  final int highlightEndOffset,
                  final boolean disabled,
                  final boolean strikeout) {

            this.text = text;
            this.highlightStartOffset = highlightStartOffset;
            this.highlightEndOffset = highlightEndOffset;
            this.disabled = disabled;
            this.strikeout = strikeout;
        }
    }

}