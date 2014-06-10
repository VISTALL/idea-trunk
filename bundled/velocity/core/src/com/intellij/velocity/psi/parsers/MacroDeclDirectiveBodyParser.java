package com.intellij.velocity.psi.parsers;

import com.intellij.lang.PsiBuilder;
import static com.intellij.velocity.psi.VtlElementTypes.*;
import com.intellij.velocity.VelocityBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class MacroDeclDirectiveBodyParser extends CompositeBodyParser {

    public static final MacroDeclDirectiveBodyParser INSTANCE = new MacroDeclDirectiveBodyParser();

    private MacroDeclDirectiveBodyParser() {}

    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        PsiBuilder.Marker directiveHeader = builder.mark();
        if(assertToken(builder, LEFT_PAREN)) {
            assertToken(builder, IDENTIFIER);
            if(builder.getTokenType() != RIGHT_PAREN) {
                VtlParser.parseList(builder, ListHandler.PARAMETER_LIST_HANDLER, false);
                assertToken(builder, RIGHT_PAREN);
            } else {
                builder.advanceLexer();
            }
        } else {
            builder.error(VelocityBundle.message("macro.declaration.expected"));
        }
        directiveHeader.done(DIR_HEADER);
        VtlParser.parseCompositeElements(builder, COMMON_END_DETECTOR);
        finishCompositeWithEnd(builder, bodyMarker, DIRECTIVE_MACRODECL);
    }
    
}
