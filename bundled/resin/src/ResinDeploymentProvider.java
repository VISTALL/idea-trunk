package org.intellij.j2ee.web.resin;

import com.intellij.facet.pointers.FacetPointer;
import com.intellij.javaee.deployment.*;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.serverInstances.J2EEServerInstance;
import com.intellij.javaee.web.artifact.WebArtifactUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactPointer;
import com.intellij.packaging.artifacts.ArtifactType;
import org.intellij.j2ee.web.resin.resin.ResinConfiguration;
import org.intellij.j2ee.web.resin.resin.WebApp;
import org.intellij.j2ee.web.resin.ui.DeploymentSettingsEditor;

import java.util.Collection;
import java.util.Arrays;

public class ResinDeploymentProvider extends DeploymentProviderEx {

    public void doDeploy(Project project, J2EEServerInstance instance, DeploymentModel deploymentModel) {
        /*try {
            ResinModel resinModel = (ResinModel) deploymentModel.getServerModel();
            ResinModuleDeploymentModel resinDeploymentModel = (ResinModuleDeploymentModel) deploymentModel;

            ResinConfiguration resinConfiguration = resinModel.getResinConfiguration(true);
            resinConfiguration.deploy(new WebApp(resinDeploymentModel.CONTEXT_PATH,
                    resinDeploymentModel.HOST,
                    getModuleDeployment(resinDeploymentModel)));
            resinConfiguration.save();*/

            setDeploymentStatus(instance, deploymentModel, DeploymentStatus.DEPLOYED);
        /*}
        catch (Exception e) {
            Module module = deploymentModel.getFacet().getModule();
            Messages.showErrorDialog(e.getMessage(), ResinBundle.message("message.text.error.deploying.facet", module.getName()));
            setDeploymentStatus(instance, deploymentModel, DeploymentStatus.FAILED);
        }*/
    }

    @Override
    public DeploymentModel createNewDeploymentModel(CommonModel commonModel, ArtifactPointer artifactPointer) {
        return new ResinModuleDeploymentModel(commonModel, artifactPointer);
    }

    public DeploymentModel createNewDeploymentModel(CommonModel configuration, FacetPointer<JavaeeFacet> javaeeFacetPointer) {
        return new ResinModuleDeploymentModel(configuration, javaeeFacetPointer);
    }

    @Override
    public SettingsEditor<DeploymentModel> createAdditionalDeploymentSettingsEditor(CommonModel commonModel, Artifact artifact) {
        return new DeploymentSettingsEditor(commonModel, artifact);
    }

    public SettingsEditor<DeploymentModel> createAdditionalDeploymentSettingsEditor(CommonModel configuration, JavaeeFacet javaeeFacet) {
        return new DeploymentSettingsEditor(configuration, javaeeFacet);
    }

    @Override
    public Collection<? extends ArtifactType> getSupportedArtifactTypes() {
        return Arrays.asList(WebArtifactUtil.getInstance().getExplodedWarArtifactType(), WebArtifactUtil.getInstance().getWarArtifactType());
    }

    public void startUndeploy(J2EEServerInstance instance, DeploymentModel deploymentModel) {
        try {
            ResinModel resinModel = (ResinModel) deploymentModel.getServerModel();
            ResinModuleDeploymentModel resinDeploymentModel = (ResinModuleDeploymentModel) deploymentModel;

            ResinConfiguration resinConfiguration = resinModel.getResinConfiguration(true);
            if (resinConfiguration.undeploy(
              new WebApp(resinDeploymentModel.CONTEXT_PATH, resinDeploymentModel.HOST, getModuleDeployment(resinDeploymentModel),
                         resinModel.CHARSET))) {
              resinConfiguration.save();
              setDeploymentStatus(instance, deploymentModel, DeploymentStatus.NOT_DEPLOYED);
            }
        }
        catch (Exception e) {
            Module module = deploymentModel.getFacet().getModule();
            Messages.showErrorDialog(e.getMessage(), ResinBundle.message("message.text.error.deploying.facet", module.getName()));
            setDeploymentStatus(instance, deploymentModel, DeploymentStatus.FAILED);
        }
    }

    public void updateDeploymentStatus(J2EEServerInstance j2EEServerInstance, DeploymentModel deploymentModel) {
    }

    public String getHelpId() {
        return null;
    }

    public DeploymentMethod[] getAvailableMethods() {
        return null;
    }

    private void setDeploymentStatus(J2EEServerInstance instance, DeploymentModel model, DeploymentStatus status) {
        CommonModel configuration = instance.getCommonModel();
        ResinModel resinModel = ((ResinModel) configuration.getServerModel());
        JavaeeFacet item = model.getFacet();
        DeploymentManager.getInstance(resinModel.getProject()).setDeploymentStatus(item, status, configuration, instance);
    }

    public static String getModuleDeployment(DeploymentModel deploymentModel) {
        return DeploymentManager.getInstance(deploymentModel.getCommonModel().getProject()).getDeploymentSourcePath(deploymentModel);
    }


}
