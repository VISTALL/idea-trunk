package com.intellij.velocity.tests;

import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.codeInsight.navigation.actions.GotoTypeDeclarationAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.cache.impl.CacheUtil;
import com.intellij.psi.search.IndexPattern;
import com.intellij.psi.search.IndexPatternOccurrence;
import com.intellij.psi.search.searches.IndexPatternSearch;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

/**
 * @author Alexey Chmutov
 */
public class VtlActionsTest extends JavaCodeInsightFixtureTestCase {

    public String getBasePath() {
        return "/svnPlugins/velocity/tests/testData/actions/";
    }

    public void testCommentBlock() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }

    public void testCommentLine() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_LINE);
    }

    public void testUncommentBlock() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }

    public void testCommentPlainTextBlock() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }

    public void testCommentPlainTextLine() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_LINE);
    }

    public void testUncommentLine() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_LINE);
    }

    public void testGotoTypeDeclaration() throws Throwable {
        final PsiClass barClass = Util.addEmptyJavaClassTo(myFixture);
        configureByFile();
        PsiElement foundByAction = GotoTypeDeclarationAction.findSymbolType(myFixture.getEditor(),
                myFixture.getEditor().getCaretModel().getOffset());
        assertEquals(barClass, foundByAction);
    }

    public void testTodo() throws Throwable {
        configureByFile();
        final IndexPattern indexPattern = CacheUtil.getIndexPatternProviders()[0].getIndexPatterns()[0];
        assertEquals(2, IndexPatternSearch.search(myFixture.getFile(), indexPattern).toArray(new IndexPatternOccurrence[0]).length);
    }

    private void doTest(final String actionId) throws Throwable {
        configureByFile();
        CodeInsightAction action = (CodeInsightAction) ActionManager.getInstance().getAction(actionId);
        action.actionPerformedImpl(myModule.getProject(), myFixture.getEditor());
        myFixture.checkResultByFile(Util.getExpectedResultFileName(getTestName(true)));
    }

    private void configureByFile() throws Throwable {
        myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
    }
}