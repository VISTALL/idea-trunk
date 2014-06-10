package com.intellij.velocity.psi;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlCommentType extends VtlTokenType {
    private final int myStartDelta;
    private final int myEndDelta;
    public VtlCommentType(@NotNull @NonNls final String debugName, int startDelta, int endDelta) {
        super(debugName);
        myStartDelta = startDelta;
        myEndDelta = endDelta;
    }

    public int getStartDelta() {
        return myStartDelta;
    }

    public int getEndDelta() {
        return myEndDelta;
    }
}
