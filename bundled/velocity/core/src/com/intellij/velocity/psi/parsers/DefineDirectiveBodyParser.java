package com.intellij.velocity.psi.parsers;

import com.intellij.lang.PsiBuilder;
import static com.intellij.velocity.psi.VtlElementTypes.*;
import com.intellij.velocity.VelocityBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class DefineDirectiveBodyParser extends CompositeBodyParser {

    public static final DefineDirectiveBodyParser INSTANCE = new DefineDirectiveBodyParser();

    private DefineDirectiveBodyParser() {}

    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        String errorMsg = VelocityBundle.message("defined.variable.expected");
        if(assertToken(builder, LEFT_PAREN)) {
            assertVariable(builder, REFERENCE_EXPRESSION, errorMsg);
            assertToken(builder, RIGHT_PAREN);
        } else {
            builder.error(errorMsg);
        }
        VtlParser.parseCompositeElements(builder, COMMON_END_DETECTOR);
        finishCompositeWithEnd(builder, bodyMarker, DIRECTIVE_DEFINE);
    }

}