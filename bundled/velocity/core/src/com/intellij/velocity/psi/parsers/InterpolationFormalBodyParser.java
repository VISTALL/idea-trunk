package com.intellij.velocity.psi.parsers;

import com.intellij.lang.PsiBuilder;
import static com.intellij.velocity.psi.VtlElementTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class InterpolationFormalBodyParser extends CompositeBodyParser {

    public static final InterpolationFormalBodyParser INSTANCE = new InterpolationFormalBodyParser();

    private InterpolationFormalBodyParser() {}

    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        InterpolationBodyParser.parseBodyInternal(builder);
        assertToken(builder, RIGHT_BRACE);
        if(bodyMarker != null) {
            bodyMarker.done(INTERPOLATION);
        }
    }

}