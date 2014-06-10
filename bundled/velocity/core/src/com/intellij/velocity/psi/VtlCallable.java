package com.intellij.velocity.psi;

/**
 * @author Alexey Chmutov
 */
public interface VtlCallable {
    VtlCallable[] EMPTY_ARRAY = new VtlCallable[0];

    VtlVariable[] getParameters();

    boolean isDeprecated();
}
