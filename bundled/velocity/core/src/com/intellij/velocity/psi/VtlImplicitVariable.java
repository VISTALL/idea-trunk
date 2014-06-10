/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.openapi.util.Factory;
import com.intellij.psi.*;
import com.intellij.psi.impl.RenameableFakePsiElement;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.VelocityBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * @author Alexey Chmutov
 */
public class VtlImplicitVariable extends RenameableFakePsiElement implements VtlVariable {
    @Nullable
    private final PsiComment myComment;
    private final String myName;
    private String myType;
    private final VtlFile myScopeFile;

    private VtlImplicitVariable(@NotNull final PsiFile containingFile, @Nullable final PsiComment comment, @NotNull final String name, @Nullable VtlFile scopeFile) {
        super(containingFile);
        myComment = comment;
        myName = name;
        myScopeFile = scopeFile;
    }

    @NotNull
    public String getName() {
        return myName;
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
        return myComment != null ? myComment : getContainingFile();
    }

    public PsiElement getParent() {
        return myComment;
    }

    public String getTypeName() {
        return VelocityBundle.message("type.name.variable");
    }

    @Override
    public String toString() {
        return "ImplicitVariable " + myName;
    }

    public void setType(final String type) {
        myType = type;
    }

    @Nullable
    public PsiType getPsiType() {
        if (myType == null) {
            return null;
        }
        try {
            return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeFromText(myType, myComment);
        } catch (IncorrectOperationException e) {
            return null;
        }
    }

    public Icon getIcon() {
        return Icons.VARIABLE_ICON;
    }

    public static VtlImplicitVariable getOrCreate(@NotNull final Map<String, VtlImplicitVariable> mapToAddTo, @NotNull final PsiFile containingFile, @Nullable final PsiComment comment, final String name, @Nullable final VtlFile scopeFile) {
        assert comment == null || comment.getContainingFile() == containingFile;
        return ContainerUtil.getOrCreate(mapToAddTo, name, new Factory<VtlImplicitVariable>() {
            public VtlImplicitVariable create() {
                return new VtlImplicitVariable(containingFile, comment, name, scopeFile);
            }
        });
    }


    public boolean isVisibleIn(@Nullable VtlFile placeFile) {
        return placeFile == null || myScopeFile == null || placeFile.isEquivalentTo(myScopeFile);
    }
}
