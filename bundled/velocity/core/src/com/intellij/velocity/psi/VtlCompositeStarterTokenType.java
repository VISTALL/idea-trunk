package com.intellij.velocity.psi;

import com.intellij.velocity.psi.parsers.CompositeBodyParser;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class VtlCompositeStarterTokenType extends VtlTokenType {

    private final CompositeBodyParser myBodyParser;

    public VtlCompositeStarterTokenType(@NotNull @NonNls final String debugName, @NotNull CompositeBodyParser bodyParser) {
        super(debugName);
        this.myBodyParser = bodyParser;
    }

    @NotNull
    public CompositeBodyParser getCompositeBodyParser() {
        return myBodyParser;
    }
}
