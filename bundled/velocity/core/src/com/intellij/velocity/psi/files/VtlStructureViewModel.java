package com.intellij.velocity.psi.files;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.psi.PsiFile;
import com.intellij.velocity.psi.directives.VtlDirective;
import com.intellij.velocity.psi.directives.VtlDirectiveHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexey Chmutov
 */
public class VtlStructureViewModel extends TextEditorBasedStructureViewModel {
    private final VtlFile myFile;

    public VtlStructureViewModel(final VtlFile file) {
        super(file);
        myFile = file;
    }

    protected PsiFile getPsiFile() {
        return myFile;
    }

    @NotNull
    public StructureViewTreeElement getRoot() {
        return new VtlTreeElementWrapper(myFile);
    }

    private static class VtlTreeElementWrapper extends PsiTreeElementBase<VtlDirectiveHolder> {

        public VtlTreeElementWrapper(final VtlDirectiveHolder element) {
            super(element);
        }

        @NotNull
        public Collection<StructureViewTreeElement> getChildrenBase() {
            final VtlDirectiveHolder element = getElement();
            if (element == null) {
                return Collections.emptyList();
            }

            List<StructureViewTreeElement> result = new ArrayList<StructureViewTreeElement>();
            for (final VtlDirective child : element.getDirectiveChildren()) {
                result.add(new VtlTreeElementWrapper(child));
            }
            return result;
        }

        public String getPresentableText() {
            final VtlDirectiveHolder element = getElement();
            return element == null ? "" : element.getPresentableName();
        }

    }
}
