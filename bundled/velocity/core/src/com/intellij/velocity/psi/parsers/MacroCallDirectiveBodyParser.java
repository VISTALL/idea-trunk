package com.intellij.velocity.psi.parsers;

import com.intellij.lang.PsiBuilder;
import static com.intellij.velocity.psi.VtlElementTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class MacroCallDirectiveBodyParser extends CompositeBodyParser {
    public static final MacroCallDirectiveBodyParser INSTANCE = new MacroCallDirectiveBodyParser();

    private MacroCallDirectiveBodyParser() {}

    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        boolean leftBracePresent = consumeTokenIfPresent(builder, LEFT_BRACE);
        assertToken(builder, IDENTIFIER, REFERENCE_EXPRESSION);
        if (leftBracePresent) {
            assertToken(builder, RIGHT_BRACE);
        }
        if (builder.getTokenType() == LEFT_PAREN) {
            parseArgumentList(builder, false);
        }
        bodyMarker.done(DIRECTIVE_MACROCALL);
    }
}
