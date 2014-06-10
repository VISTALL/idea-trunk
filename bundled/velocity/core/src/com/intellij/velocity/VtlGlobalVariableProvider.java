package com.intellij.velocity;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.velocity.psi.VtlVariable;
import com.intellij.velocity.psi.files.VtlFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Alexey Chmutov
 */
public abstract class VtlGlobalVariableProvider {
    public static final ExtensionPointName<VtlGlobalVariableProvider> EP_NAME = ExtensionPointName.create("com.intellij.velocity.globalVariableProvider");

    @NotNull
    public abstract Collection<? extends VtlVariable> getGlobalVariables(VtlFile file);

}
