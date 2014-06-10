package com.intellij.velocity.psi.files;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
class VtlFileProxy {
    private final VtlFile myFile;
    private final VtlFile myScopeFile;

    public VtlFileProxy(@NotNull VtlFile file, @Nullable VtlFile scopeFile) {
        myFile = file;
        myScopeFile = scopeFile;
    }

    @NotNull
    public VtlFile getFile() {
        return myFile;
    }

    public boolean isVisibleIn(@Nullable VtlFile placeFile) {
        return placeFile == null || myScopeFile == null || placeFile.isEquivalentTo(myScopeFile);
    }
}
