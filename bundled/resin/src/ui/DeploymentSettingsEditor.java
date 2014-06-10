package org.intellij.j2ee.web.resin.ui;

import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.Factory;
import com.intellij.facet.pointers.FacetPointersManager;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactPointerManager;
import com.intellij.packaging.artifacts.ArtifactPointer;
import org.intellij.j2ee.web.resin.ResinModuleDeploymentModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DeploymentSettingsEditor extends SettingsEditor<DeploymentModel> {
    private JTextField hostField;
    private JTextField applicationContextField;
    private JPanel mainPanel;

    public DeploymentSettingsEditor(final CommonModel configuration, final JavaeeFacet facet) {
        super(new Factory<DeploymentModel>() {
            public ResinModuleDeploymentModel create() {
                final FacetPointersManager manager = FacetPointersManager.getInstance(facet.getModule().getProject());
                return new ResinModuleDeploymentModel(configuration, manager.create(facet));
            }
        });
    }

    public DeploymentSettingsEditor(final CommonModel commonModel, final Artifact artifact) {
        super(new Factory<DeploymentModel>() {
            public ResinModuleDeploymentModel create() {
                final ArtifactPointer pointer = ArtifactPointerManager.getInstance(commonModel.getProject()).create(artifact);
                return new ResinModuleDeploymentModel(commonModel, pointer);
            }
        });
    }

    public void resetEditorFrom(DeploymentModel settings) {
        ResinModuleDeploymentModel resinDeploymentModel = (ResinModuleDeploymentModel) settings;
        applicationContextField.setText(resinDeploymentModel.CONTEXT_PATH);
        hostField.setText(resinDeploymentModel.HOST);
    }

    public void applyEditorTo(DeploymentModel settings) throws ConfigurationException {
        ResinModuleDeploymentModel resinDeploymentModel = (ResinModuleDeploymentModel) settings;
        resinDeploymentModel.CONTEXT_PATH = applicationContextField.getText();
        resinDeploymentModel.HOST = hostField.getText();
    }

    @NotNull
    protected JComponent createEditor() {
        return mainPanel;
    }

    protected void disposeEditor() {
    }

}
