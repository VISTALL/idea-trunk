package com.sixrr.xrp;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.sixrr.xrp.references.IdrefReferenceProvider;
import org.jetbrains.annotations.NotNull;

public class RefactorXIntentions implements ProjectComponent {
    private final Project project;

    public RefactorXIntentions(Project project) {
        super();
        this.project = project;
    }

    public void projectOpened() {
      ReferenceProvidersRegistry.getInstance(project).registerReferenceProvider(XmlPatterns.xmlAttributeValue().withLocalName("idref"),
                                                                                new IdrefReferenceProvider(),
                                                                                ReferenceProvidersRegistry.LOWER_PRIORITY);
    }

    public void projectClosed() {
    }

    @NotNull
    public String getComponentName() {
        return "RefactorXIntentions";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }
}
