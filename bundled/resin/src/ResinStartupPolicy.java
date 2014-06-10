package org.intellij.j2ee.web.resin;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.run.configuration.JavaCommandLineStartupPolicy;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathsList;
import org.intellij.j2ee.web.resin.resin.ResinConfiguration;
import org.intellij.j2ee.web.resin.resin.WebApp;
import org.intellij.j2ee.web.resin.resin.version.ResinVersion;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;

public class ResinStartupPolicy implements JavaCommandLineStartupPolicy {
    //Constants
    private static final String RESIN_RUN_PROP_FILE = "ResinRun.properties";
    private static final String DEBUG_VM_PARAMS_PROP = "resin.debug.vm.param";
    private static final String RESINHOME_VM_PARAMS_PROP = "resin.home.vm.param";
    private static final String JAVA_LIB_PATH_VM_PARAMS_PROP = "java.lib.path.vm.param";
    private static final String RESIN_COMMAND_LINE_ARG_PROP = "resin.command.line.conf.arg.name";
    private static final String RESIN_VERSIONS_INVALID_PATHS_PROP = "resin.versions.not.allow.white.spaces";
    //Variables
    private Properties resinRunProps = null;

    public JavaParameters createCommandLine(final CommonModel commonModel) throws ExecutionException {
        try {
            final ResinModel resinModel = (ResinModel) commonModel.getServerModel();

            final ResinConfiguration resinConfiguration = resinModel.getResinConfiguration(false);
            createInitialResinConf(commonModel, resinConfiguration);

            final File resinHome = new File(resinModel.getPersistentData().RESIN_HOME);
            final JavaParameters parameters = new JavaParameters();

            if (resinModel.CHARSET != null && resinModel.CHARSET.trim().length() > 0)
                parameters.setCharset(Charset.forName(resinModel.CHARSET));

            if (resinConfiguration.getResinInstallation().getVersion().allowXdebug())
                loadResinRunProp(DEBUG_VM_PARAMS_PROP, parameters);

            final Sdk sdk = ProjectRootManager.getInstance(commonModel.getProject()).getProjectJdk();
            if ((sdk == null) || !(sdk.getSdkType() instanceof JavaSdkType))
                throw new ExecutionException(ResinBundle.message("resin.run.error.no.jdk"));

            parameters.setJdk(sdk);
            parameters.getClassPath().add(((JavaSdkType) sdk.getSdkType()).getToolsPath(sdk));

            ResinVersion resinVersion = resinConfiguration.getResinInstallation().getVersion();
            parameters.setWorkingDirectory(resinHome);
            parameters.setMainClass(resinVersion.getStartupClass());

            loadResinRunProp(RESINHOME_VM_PARAMS_PROP, parameters, resinHome.getPath());

            if (resinHome.getPath().indexOf(' ') != -1 && !allowsRunWithWhiteSpace(resinVersion))
                throw new ExecutionException(ResinBundle.message("resin.run.error.invalid.path", resinHome.getPath()));

            loadResinRunProp(JAVA_LIB_PATH_VM_PARAMS_PROP, parameters, resinHome.getPath());

            parameters.getProgramParametersList().add(getResinRunProperty(RESIN_COMMAND_LINE_ARG_PROP)[0], resinConfiguration.getConfPath());

            if (resinModel.ADDITIONAL_PARAMETERS != null && resinModel.ADDITIONAL_PARAMETERS.trim().length() > 0)
                parameters.getProgramParametersList().addParametersString(resinModel.ADDITIONAL_PARAMETERS);

            final PathsList classpath = parameters.getClassPath();
            
            //Include application server libraries
            VirtualFile[] files = commonModel.getApplicationServer().getLibrary().getFiles(OrderRootType.CLASSES);
            for (VirtualFile file : files)
                classpath.add(file.getPresentableUrl());

            File[] allJars = resinConfiguration.getResinInstallation().getLibFiles(true);
            for (File jar : allJars) {
                String s = jar.getAbsolutePath();
                if (!classpath.getPathList().contains(s))
                    classpath.add(s);
            }

            if (resinModel.AUTO_BUILD_CLASSPATH) {
                Collection<VirtualFile> outputAndLibs = new ArrayList<VirtualFile>();
                for (DeploymentModel model : commonModel.getDeploymentModels()) {
                    final JavaeeFacet facet = model.getFacet();
                    if (facet != null) {
                        ModuleRootManager mrm = ModuleRootManager.getInstance(facet.getModule());
                        OrderEntry[] orderEntries = mrm.getOrderEntries();
                        for (int j = 0; orderEntries != null && j < orderEntries.length; j++) {
                            OrderEntry oe = orderEntries[j];
                            if (!(oe instanceof JdkOrderEntry)) {
                                VirtualFile[] vfiles = oe.getFiles(OrderRootType.COMPILATION_CLASSES);
                                for (VirtualFile vfile : vfiles) {
                                    if (!outputAndLibs.contains(vfile)) {
                                        outputAndLibs.addAll(Arrays.asList(vfiles));
                                    }
                                }
                            }
                        }
                    }
                }

                for (VirtualFile vfile : outputAndLibs) {
                    classpath.add(vfile);
                }
            }

            return parameters;
        }
        catch (Exception e) {
            throw new ExecutionException(e.getMessage());
        }
    }

    private void createInitialResinConf(final CommonModel commonModel, final ResinConfiguration resinConfiguration) throws ExecutionException {
        try {
            final ResinModel resinModel = (ResinModel) commonModel.getServerModel();
            for (DeploymentModel model : commonModel.getDeploymentModels()) {
                ResinModuleDeploymentModel deploymentModel = (ResinModuleDeploymentModel)model;

                final String deploymentPath = ResinDeploymentProvider.getModuleDeployment(deploymentModel);
                if (deploymentPath != null) {
                    resinConfiguration.deploy(new WebApp(deploymentModel.CONTEXT_PATH, deploymentModel.HOST, deploymentPath, resinModel.CHARSET));
                }
            }

            resinConfiguration.save();
        }
        catch (IOException e) {
            Messages.showErrorDialog(e.getMessage(), ResinBundle.message("message.error.resin.conf.create"));
        }
    }

    /**
     * Checks if this resin version can run within a resin home path with white spaces
     *
     * @param resinVersion the resin version
     * @return true if the version can run within white space path. otherwise false
     * @throws ExecutionException if the property file is invalid (no invalid version prop specify)
     */
    private boolean allowsRunWithWhiteSpace(ResinVersion resinVersion) throws ExecutionException {
        String[] value = getResinRunProperty(RESIN_VERSIONS_INVALID_PATHS_PROP);
        List invalids = Arrays.asList(value);

        String verNumber = resinVersion.getVersionNumber();
        if (invalids.contains(verNumber))
            return false;

        //Fallback into wildcard version
        String[] tocheck = verNumber.split("\\.");
        for (String actual : tocheck) {
            if (invalids.contains(actual + ".x"))
                return false;
        }


        return true;
    }

    /**
     * Loads a resin run property, and add it to the Java VM parameter list
     *
     * @param prop         the property to load
     * @param parameters   the java parameter list
     * @param substitution property substituion parameters
     * @throws ExecutionException if the property doesn't exist
     */
    private void loadResinRunProp(String prop, JavaParameters parameters, Object... substitution) throws ExecutionException {
        String[] values = getResinRunProperty(prop, substitution);
        for (String value : values)
            parameters.getVMParametersList().add(value);
    }

    /**
     * Gets a resin run property
     *
     * @param prop         the property to get
     * @param substitution property substituion parameters
     * @return property value
     * @throws ExecutionException if the property doesn't exist
     */
    private String[] getResinRunProperty(String prop, Object... substitution) throws ExecutionException {
        loadResinRunProperties();

        String value = resinRunProps.getProperty(prop);
        if (value == null)
            throw new ExecutionException(ResinBundle.message("resin.run.property.missing", prop));

        String[] res = value.split(" ");
        if (substitution != null) {
            for (int i = 0; i < res.length; i++)
                res[i] = MessageFormat.format(res[i], substitution);
        }

        return res;
    }

    /**
     * Loads resin run properties
     *
     * @throws ExecutionException if any exception occurs during properties read
     */
    private void loadResinRunProperties() throws ExecutionException {
        if (resinRunProps != null)
            return;
        resinRunProps = new Properties();
        try {
            resinRunProps.load(this.getClass().getResourceAsStream(RESIN_RUN_PROP_FILE));
        }
        catch (IOException e) {
            throw new ExecutionException(ResinBundle.message("resin.run.startup.no.prop"));
        }
    }
}
