package com.intellij.velocity;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.velocity.psi.VtlMacro;
import com.intellij.velocity.psi.files.VtlFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Alexey Chmutov
 */
public abstract class VtlGlobalMacroProvider {
    public static final ExtensionPointName<VtlGlobalMacroProvider> EP_NAME = ExtensionPointName.create("com.intellij.velocity.globalMacroProvider");

    @NotNull
    public abstract Collection<VtlMacro> getGlobalMacros(@NotNull VtlFile file);

}