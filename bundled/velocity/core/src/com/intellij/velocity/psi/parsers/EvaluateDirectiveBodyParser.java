package com.intellij.velocity.psi.parsers;

import com.intellij.lang.PsiBuilder;
import static com.intellij.velocity.psi.VtlElementTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class EvaluateDirectiveBodyParser extends CompositeBodyParser {

    public static final EvaluateDirectiveBodyParser INSTANCE = new EvaluateDirectiveBodyParser();

    private EvaluateDirectiveBodyParser() {}

    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        parseArgumentList(builder, false);
        bodyMarker.done(DIRECTIVE_EVALUATE);
    }

}