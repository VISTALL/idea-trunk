package com.intellij.velocity.psi.parsers;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import static com.intellij.velocity.psi.VtlElementTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class IfDirectiveBodyParser extends CompositeBodyParser {

    public static final IfDirectiveBodyParser INSTANCE = new IfDirectiveBodyParser();

    private IfDirectiveBodyParser() {}
    
    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        parseConditionalExpression(builder);

        CompositeEndDetector endDetector = new CompositeEndDetector() {
            boolean elseDirectiveFound = false;

            @Override
            public boolean isTokenInvalid(IElementType tokenType) {
                boolean result = elseDirectiveFound && (tokenType == SHARP_ELSE || tokenType == SHARP_ELSEIF);
                if(tokenType == SHARP_ELSE) {
                    elseDirectiveFound = true;
                }
                return result;
            }
        };
        VtlParser.parseCompositeElements(builder, endDetector);
        finishCompositeWithEnd(builder, bodyMarker, DIRECTIVE_IF);
    }

}
