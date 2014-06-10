package org.intellij.j2ee.web.resin;

import com.intellij.facet.FacetTypeId;
import com.intellij.javaee.appServerIntegrations.AppServerIntegration;
import com.intellij.javaee.appServerIntegrations.ApplicationServerHelper;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.facet.JavaeeFacetUtil;
import com.intellij.javaee.openapi.ex.AppServerIntegrationsManager;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

public class ResinManager extends AppServerIntegration {
    //Variables
    public static final Icon ICON_RESIN = IconLoader.getIcon("icons/resin.png");
    private final ApplicationServerHelper resinApplicationServerHelper = new ResinApplicationServerHelper();

    /**
     * Public constructor
     */
    public ResinManager(){
        super();
    }

    public Icon getIcon() {
        return ICON_RESIN;
    }

    public String getPresentableName() {
        return ResinBundle.message("resin.application.server.name");
    }

    @NotNull @NonNls
    public String getComponentName() {
        return "#" + this.getClass().getName();
    }

    public void initComponent() {

    }

    public void disposeComponent() {

    }

    //TODO SCF new method
    /*public DeploymentProvider getDeploymentProvider() {
        return myTomcatDeploymentProvider;
    }*/

    public static ResinManager getInstance() {
        return AppServerIntegrationsManager.getInstance().getIntegration(ResinManager.class);
    }

    public ApplicationServerHelper getApplicationServerHelper() {
        return resinApplicationServerHelper;
    }

    @NotNull
    public Collection<FacetTypeId<? extends JavaeeFacet>> getSupportedFacetTypes() {
        return JavaeeFacetUtil.getInstance().getSingletonCollection(WebFacet.ID);
    }

    //TODO SCF new method
    /*public @NotNull AppServerDeployedFileUrlProvider getDeployedFileUrlProvider() {
        return TomcatDeployedFileUrlProvider.INSTANCE;
    }*/

    //TODO SCF new method
    /*public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        final FileTemplateGroupDescriptor root = new FileTemplateGroupDescriptor(TomcatBundle.message("templates.group.title"), getIcon());
        root.addTemplate(new FileTemplateDescriptor(TomcatConstants.CONTEXT_XML_TEMPLATE_FILE_NAME, StdFileTypes.XML.getIcon()));
        return root;
    }*/

/*
public ModuleType[] getSupportedModuleTypes() {
    return new ModuleType[] { ModuleType.WEB };
}

public DataSourceProvider getDataSourceProvider() {
    return null;
}*/
}
