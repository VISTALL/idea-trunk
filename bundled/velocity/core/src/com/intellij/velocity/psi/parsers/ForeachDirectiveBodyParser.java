package com.intellij.velocity.psi.parsers;

import com.intellij.lang.PsiBuilder;
import com.intellij.velocity.VelocityBundle;
import static com.intellij.velocity.psi.VtlElementTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class ForeachDirectiveBodyParser extends CompositeBodyParser {

    public static final ForeachDirectiveBodyParser INSTANCE = new ForeachDirectiveBodyParser();

    private ForeachDirectiveBodyParser() {}

    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        PsiBuilder.Marker directiveHeader = builder.mark();
        incrementForeachCounter(builder);
        String errorMsg = VelocityBundle.message("loop.variable.expected");
        if(assertToken(builder, LEFT_PAREN)) {
            assertVariable(builder, LOOP_VARIABLE, errorMsg);
            assertToken(builder, IN);
            VtlParser.parseOperand(builder, false);
            assertToken(builder, RIGHT_PAREN);
        } else {
            builder.error(errorMsg);
        }
        directiveHeader.done(DIR_HEADER);
        VtlParser.parseCompositeElements(builder, COMMON_END_DETECTOR);
        finishCompositeWithEnd(builder, bodyMarker, DIRECTIVE_FOREACH);
        decrementForeachCounter(builder);
    }

}
