package com.intellij.velocity.psi.parsers;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.velocity.VelocityBundle;
import static com.intellij.velocity.psi.VtlCompositeElementTypes.PARAMETER;
import static com.intellij.velocity.psi.VtlCompositeElementTypes.RANGE_EXPRESSION;
import com.intellij.velocity.psi.VtlElementTypes;
import static com.intellij.velocity.psi.VtlElementTypes.*;
import com.intellij.velocity.psi.VtlTokenType;
import static com.intellij.velocity.psi.parsers.CompositeBodyParser.assertToken;
import static com.intellij.velocity.psi.parsers.CompositeBodyParser.consumeTokenIfPresent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
* User: Alexey Chmutov
* Date: 02.07.2008
*/
class ListHandler {
    private final IElementType myTerminator;

    private ListHandler(@NotNull VtlTokenType terminator) {
        myTerminator = terminator;
    }

    public boolean parseListElement(PsiBuilder builder) {
        return com.intellij.velocity.psi.parsers.VtlParser.parseOperand(builder, false);
    }

    public boolean parseSeparator(PsiBuilder builder) {
        return consumeTokenIfPresent(builder, VtlElementTypes.COMMA);
    }

    public final boolean isListFinished(PsiBuilder builder) {
        return builder.getTokenType() == myTerminator;
    }

    static final ListHandler GENERAL_LIST_HANDLER = new ListHandler(RIGHT_PAREN);

    static final ListHandler MAP_HANDLER = new ListHandler(RIGHT_BRACE_IN_EXPR) {
        @Override
        public boolean parseListElement(PsiBuilder builder) {
            if(!super.parseListElement(builder)) {
                return false;
            }
            if(assertToken(builder, COLON)) {
                super.parseListElement(builder);
            }
            return true;
        }
    };

    static final ListHandler LIST_HANDLER = new ListHandler(RIGHT_BRACKET) {
        @Override
        public boolean parseListElement(PsiBuilder builder) {
            PsiBuilder.Marker rangeOperator = builder.mark();
            boolean elementFound = super.parseListElement(builder);
            if (!elementFound || builder.getTokenType() != RANGE) {
                rangeOperator.drop();
                return elementFound;
            }
            builder.advanceLexer();
            super.parseListElement(builder);
            rangeOperator.done(RANGE_EXPRESSION);
            return true;
        }
    };

    static final ListHandler PARAMETER_LIST_HANDLER = new ListHandler(RIGHT_PAREN) {
        @Override
        public boolean parseListElement(PsiBuilder builder) {
            PsiBuilder.Marker elementMarker = builder.mark();
            final IElementType elementStarter = builder.getTokenType();
            builder.advanceLexer();

            if (elementStarter != START_REFERENCE && elementStarter != START_REF_FORMAL) {
                elementMarker.error(VelocityBundle.message("parameter.expected"));
                return false;
            }
            assertToken(builder, IDENTIFIER);
            if (elementStarter == START_REF_FORMAL) {
                assertToken(builder, RIGHT_BRACE);
            }
            elementMarker.done(PARAMETER);
            return true;
        }
    };

}
