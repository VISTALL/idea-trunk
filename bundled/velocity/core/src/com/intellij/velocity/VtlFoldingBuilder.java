package com.intellij.velocity;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.velocity.psi.directives.VtlDirective;
import com.intellij.velocity.psi.files.VtlFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexey Chmutov
 */
public class VtlFoldingBuilder implements FoldingBuilder, DumbAware {
    @NotNull
    public FoldingDescriptor[] buildFoldRegions(@NotNull final ASTNode node, @NotNull final Document document) {
        final PsiElement element = node.getPsi();
        if (!(element instanceof VtlFile)) {
            return FoldingDescriptor.EMPTY;
        }
        List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
        for (VtlDirective composite : ((VtlFile) element).getDirectiveChildren()) {
            addFoldingDescriptors(descriptors, composite);
        }
        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    private static void addFoldingDescriptors(final List<FoldingDescriptor> descriptors, final VtlDirective composite) {
        final int start = composite.getFoldingStartOffset();
        final int end = composite.getFoldingEndOffset();
        final ASTNode node = composite.getNode();
        if (start + 1 >= end || node == null) {
            return;
        }
        descriptors.add(new FoldingDescriptor(node, new TextRange(start, end)));
        for (final VtlDirective child : composite.getDirectiveChildren()) {
            addFoldingDescriptors(descriptors, child);
        }
    }

    public String getPlaceholderText(@NotNull final ASTNode node) {
        return "...";
    }

    public boolean isCollapsedByDefault(@NotNull final ASTNode node) {
        return false;
    }
}

