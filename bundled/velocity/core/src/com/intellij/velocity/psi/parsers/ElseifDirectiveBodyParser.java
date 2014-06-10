package com.intellij.velocity.psi.parsers;

import com.intellij.lang.PsiBuilder;
import static com.intellij.velocity.psi.VtlElementTypes.*;


/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class ElseifDirectiveBodyParser extends CompositeBodyParser {

    public static final ElseifDirectiveBodyParser INSTANCE = new ElseifDirectiveBodyParser();

    private ElseifDirectiveBodyParser() {}

    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        parseConditionalExpression(builder);
        VtlParser.parseCompositeElements(builder, ElseDirectiveBodyParser.elseCompositeEndDetector);
        bodyMarker.done(DIRECTIVE_ELSEIF);
    }
    
}
