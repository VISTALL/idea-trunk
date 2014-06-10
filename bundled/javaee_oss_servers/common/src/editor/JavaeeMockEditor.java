/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.editor;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.ui.MockJavaeeDomElementsEditor;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.ui.*;
import org.jetbrains.annotations.Nullable;

public abstract class JavaeeMockEditor extends MockJavaeeDomElementsEditor {

    protected JavaeeMockEditor(JavaeeFacet facet) {
        super(facet);
    }

    protected String getTitle() {
        return JavaeeBundle.getText("Editor.title", JavaeeIntegration.getInstance().getName());
    }

    protected final DomFileEditor<?> initEditor(CommittablePanel panel, DomElement element) {
        CaptionComponent caption = new CaptionComponent(getTitle(), JavaeeIntegration.getInstance().getBigIcon());
        caption = DomUIFactory.getDomUIFactory().addErrorPanel(caption, element);
        BasicDomElementComponent<?> component = DomFileEditor.createComponentWithCaption(panel, caption, element);
        return initFileEditor(component, DomUtil.getFile(element).getVirtualFile(), caption.getText());
    }

    protected void addWatchedElement(DomFileEditor<?> editor, @Nullable DomElement element) {
        if (element != null) {
            editor.addWatchedElement(element);
        }
    }
}
