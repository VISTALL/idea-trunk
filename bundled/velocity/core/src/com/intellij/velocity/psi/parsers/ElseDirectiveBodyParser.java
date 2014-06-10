package com.intellij.velocity.psi.parsers;

import com.intellij.lang.PsiBuilder;
import com.intellij.velocity.psi.VtlCompositeStarterTokenType;
import com.intellij.psi.tree.IElementType;
import static com.intellij.velocity.psi.VtlElementTypes.*;


/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class ElseDirectiveBodyParser extends CompositeBodyParser {

    public static final ElseDirectiveBodyParser INSTANCE = new ElseDirectiveBodyParser();

    private ElseDirectiveBodyParser() {}

    static final CompositeEndDetector elseCompositeEndDetector = new CompositeEndDetector() {
        @Override
        public boolean isCompositeFinished(PsiBuilder builder) {
            IElementType tokenType = builder.getTokenType();
            return tokenType == SHARP_ELSE || tokenType == SHARP_ELSEIF
                    || builder.getTokenType() == SHARP_END;
        }

        @Override
        public boolean isTokenInvalid(IElementType tokenType) {
            return !(tokenType == TEMPLATE_TEXT || tokenType == SHARP_STOP
                    || tokenType instanceof VtlCompositeStarterTokenType);
        }
    };

    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        VtlParser.parseCompositeElements(builder, elseCompositeEndDetector);
        bodyMarker.done(DIRECTIVE_ELSE);
    }
    
}
