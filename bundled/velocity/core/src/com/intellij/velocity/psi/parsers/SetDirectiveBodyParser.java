package com.intellij.velocity.psi.parsers;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.velocity.VelocityBundle;
import static com.intellij.velocity.psi.VtlElementTypes.*;
import com.intellij.velocity.psi.VtlCompositeStarterTokenType;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class SetDirectiveBodyParser extends CompositeBodyParser {

    public static final SetDirectiveBodyParser INSTANCE = new SetDirectiveBodyParser();

    private SetDirectiveBodyParser() {
    }

    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        assertToken(builder, LEFT_PAREN);
        PsiBuilder.Marker variable = builder.mark();
        final IElementType elementStarter = builder.getTokenType();
        builder.advanceLexer();
        if (elementStarter == START_REFERENCE || elementStarter == START_REF_FORMAL) {
            variable.drop();
            CompositeBodyParser bodyParser = ((VtlCompositeStarterTokenType) elementStarter).getCompositeBodyParser();
            bodyParser.parseBody(builder, null);
        } else {
            variable.error(VelocityBundle.message("token.expected", START_REFERENCE));
        }

        assertToken(builder, ASSIGN);
        VtlParser.parseBinaryExpression(builder);
        assertToken(builder, RIGHT_PAREN);
        bodyMarker.done(DIRECTIVE_SET);
    }
}
