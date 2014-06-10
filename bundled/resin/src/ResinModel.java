package org.intellij.j2ee.web.resin;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.javaee.appServerIntegrations.ApplicationServer;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentProvider;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.run.configuration.ServerModel;
import com.intellij.javaee.run.execution.DefaultOutputProcessor;
import com.intellij.javaee.run.execution.OutputProcessor;
import com.intellij.javaee.serverInstances.J2EEServerInstance;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import org.intellij.j2ee.web.resin.resin.ResinConfiguration;
import org.intellij.j2ee.web.resin.resin.ResinInstallation;
import org.intellij.j2ee.web.resin.ui.RunConfigurationEditor;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResinModel implements ServerModel {
    //Constants
    @NonNls
    protected static final String HTTP_PROTOCOL = "http://";

    //Variables
    private CommonModel commonModel;
    private final ResinDeploymentProvider deploymentProvider = new ResinDeploymentProvider();
    public String DEFAULT_PORT = "80";
    public String PORT = DEFAULT_PORT;
    public String CHARSET = "";

    private File tempResinConfFile;
    public String RESIN_CONF = "";
    public boolean DEBUG_CONFIGURATION;
    public boolean AUTO_BUILD_CLASSPATH;
    public boolean READ_ONLY_CONFIGURATION;
    public String ADDITIONAL_PARAMETERS = "";

    public ResinModel() {
        try {
            tempResinConfFile = File.createTempFile("resin", ".conf");
            tempResinConfFile.deleteOnExit();
        } catch (IOException e) {
            Messages.showErrorDialog(e.getMessage(), "Can't create temp file for intellij configuration info");
        }
    }

    public J2EEServerInstance createServerInstance() throws ExecutionException {
        return new ResinServerInstance(commonModel);
    }

    public DeploymentProvider getDeploymentProvider() {
        return commonModel.isLocal() ? deploymentProvider : null;
    }

    public String getDefaultUrlForBrowser() {
        return getUrlForBrowser(false);
    }

    public String getUrlForBrowser(final boolean addContextPath) {
        StringBuffer result = new StringBuffer();
        result.append(HTTP_PROTOCOL);
        result.append(commonModel.getHost());
        result.append(":");
        result.append(String.valueOf(commonModel.getPort()));
        if (addContextPath) {
            String defaultContext = getDefaultContext();
            if (defaultContext != null && !defaultContext.equals("/")) {
                if (!StringUtil.startsWithChar(defaultContext, '/')) {
                    result.append("/");
                }
                result.append(defaultContext);
            }
        }
        result.append("/");
        return result.toString();
    }

    private String getDefaultContext() {
        for (DeploymentModel deploymentModel : commonModel.getDeploymentModels()) {
            if (deploymentModel instanceof ResinModuleDeploymentModel) {
                return ((ResinModuleDeploymentModel) deploymentModel).CONTEXT_PATH;
            }
        }
        return null;
    }

    public SettingsEditor<CommonModel> getEditor() {
      if (commonModel.isLocal()) {
        return new RunConfigurationEditor();
      }
      return null;
    }

    public OutputProcessor createOutputProcessor(ProcessHandler processHandler, J2EEServerInstance j2EEServerInstance) {
        return new DefaultOutputProcessor(processHandler);
    }

    public List<Pair<String, Integer>> getAddressesToCheck() {
        List<Pair<String, Integer>> result = new ArrayList<Pair<String, Integer>>();
        result.add(Pair.create(commonModel.getHost(), commonModel.getPort()));
        return result;
    }

    public void checkConfiguration() throws RuntimeConfigurationException {
        Set<String> contexts = new HashSet<String>();
        for (DeploymentModel deploymentModel : commonModel.getDeploymentModels()) {
            final ResinModuleDeploymentModel model = (ResinModuleDeploymentModel)deploymentModel;
            if (!contexts.add(model.CONTEXT_PATH)) {
              throw new RuntimeConfigurationError(ResinBundle.message("error.duplicated.context.path.text", model.CONTEXT_PATH));
            }
        }
    }

    public int getDefaultPort() {
        return ResinUtil.DEFAULT_PORT;
    }

    public void setCommonModel(CommonModel commonModel) {
        this.commonModel = commonModel;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getLocalPort() {
        try{
            return Integer.parseInt(PORT);
        }
        catch(NumberFormatException e){
            return Integer.parseInt(DEFAULT_PORT);            
        }
    }

    public void readExternal(org.jdom.Element element) throws InvalidDataException {
        //XmlSerializer
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(org.jdom.Element element) throws WriteExternalException {
        //XmlSerializer
        DefaultJDOMExternalizer.writeExternal(this, element);
    }

    public ResinPersistentData getPersistentData() {
        ApplicationServer applicationServer = commonModel.getApplicationServer();
        if (applicationServer == null) {
            return null;
        }
        return (ResinPersistentData) applicationServer.getPersistentData();
    }


    public Project getProject() {
        return commonModel.getProject();
    }

    public ResinConfiguration getResinConfiguration(boolean keep) throws IOException, JDOMException, ExecutionException {
        File resinHome = new File(getPersistentData().RESIN_HOME);

        ResinInstallation resinInstallation = new ResinInstallation(resinHome);
        File baseResinConf = null;

        if (getPersistentData().RESIN_CONF != null && getPersistentData().RESIN_CONF.trim().length() != 0) {
            baseResinConf = new File(getPersistentData().RESIN_CONF);
        }

        if (RESIN_CONF != null && RESIN_CONF.trim().length() != 0) {
            baseResinConf = new File(RESIN_CONF);
        }
        
        if (baseResinConf != null) {
            if (!baseResinConf.exists()) {
                throw new ExecutionException(ResinBundle.message("message.error.resin.conf.doesnt.exist", baseResinConf.getAbsolutePath()));
            }
            if (baseResinConf.isDirectory()) {
                throw new ExecutionException(ResinBundle.message("message.error.resin.conf.directory", baseResinConf.getAbsolutePath()));
            }
        }

        ResinConfiguration resinConfiguration = new ResinConfiguration(resinInstallation, READ_ONLY_CONFIGURATION ? null : tempResinConfFile, baseResinConf);

        if (! keep) {
            resinConfiguration.clearTempFile();
        }

        if (!READ_ONLY_CONFIGURATION) {
          resinConfiguration.setPort(Integer.parseInt(PORT));
        }
        return resinConfiguration;
    }
}
