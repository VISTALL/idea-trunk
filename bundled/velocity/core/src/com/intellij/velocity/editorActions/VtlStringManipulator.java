package com.intellij.velocity.editorActions;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import com.intellij.velocity.psi.VtlLiteralExpressionType.VtlStringLiteral;

/**
 * @author Alexey Chmutov
 */
public class VtlStringManipulator extends AbstractElementManipulator<VtlStringLiteral> {
    public VtlStringLiteral handleContentChange(final VtlStringLiteral element, final TextRange range, final String newContent)
            throws IncorrectOperationException {
        return element.setStringValue(range, newContent);
    }

    public TextRange getRangeInElement(final VtlStringLiteral element) {
        return element.getValueRange();
    }
}
