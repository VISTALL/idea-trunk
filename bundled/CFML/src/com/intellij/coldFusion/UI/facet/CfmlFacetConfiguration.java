package com.intellij.coldFusion.UI.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 02.04.2009
 * Time: 18:38:56
 * To change this template use File | Settings | File Templates.
 */
public class CfmlFacetConfiguration extends FacetEditorTab implements FacetConfiguration {
    private static final String SERVER_ROOT_PATH = "cfmlserverrootpath";
    private static final String SERVER_RELATIVE_PATH = "cfmlserverrelativepath";

    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        // myRootsPanel = new CfmlFacetSourceRootsPanel(editorContext.getModule(), this, true);
        myEditorForm.setRelativeFolder(myServerRelativePath);
        myEditorForm.setServerPath(myServerRootPath);
        return new FacetEditorTab[]{this};
    }

    public void readExternal(Element element) throws InvalidDataException {
        myServerRootPath = element.getAttribute(SERVER_ROOT_PATH).getValue();
        myServerRelativePath = element.getAttribute(SERVER_RELATIVE_PATH).getValue();
    }

    public void writeExternal(Element element) throws WriteExternalException {
        element.setAttribute(SERVER_ROOT_PATH, myServerRootPath);
        element.setAttribute(SERVER_RELATIVE_PATH, myServerRelativePath);
    }
/*
    public List<WebRoot> getWebRoots(boolean includeDependentModules) {
        return myWebRoots;
    }

    public void setWebRoots(List<WebRoot> webRoots) {
        myWebRoots = new LinkedList<WebRoot>(webRoots);
    }
    */
/*

    public void readExternal(final Element element) throws InvalidDataException {
        */
/*
      final Element webRootsElement = element.getChild(WEB_ROOTS_ELEMENT);
      if (webRootsElement != null) {
        //noinspection unchecked
        final List<Element> roots = webRootsElement.getChildren(WEB_ROOT_ELEMENT);
        for (Element root : roots) {
          final WebRoot webRoot = new WebRoot(root);
          myWebRoots.add(webRoot);
        }
      }
      *//*

        final Element serverPath = element.getChild(WEB_SERVER_ROOT_ELEMENT);
        myServerRootPath = serverPath.getContent().
        toString();
    }

    public void writeExternal(final Element element) throws WriteExternalException {
        */
/*
        final Element roots = new Element(WEB_ROOTS_ELEMENT);
        for (WebRoot webRoot : myWebRoots) {
        final Element root = new Element(WEB_ROOT_ELEMENT);
        webRoot.writeExternal(root);
        roots.addContent(root);
        }
        element.addContent(roots);
        *//*

        Element serverRoot = new Element(WEB_SERVER_ROOT_ELEMENT);
        serverRoot.addContent(myServerRootPath);
        element.addContent(serverRoot);

        Element relativePath = new Element(WEB_SERVER_RELATIVE_PATH_ELEMENT);
        relativePath.addContent(myServerRootPath);
        element.addContent(relativePath);
    }

*/

    @Nls
    public String getDisplayName() {
        return "CFML Facet";
    }

    public JComponent createComponent() {
        return myEditorForm.getMainPanel();
    }

    public boolean isModified() {
        return !myEditorForm.getServerPath().equals(myServerRootPath) ||
                !myEditorForm.getRelativeFolder().equals(myServerRelativePath);
    }

    public void apply() throws ConfigurationException {
        myServerRootPath = myEditorForm.getServerPath();
        myServerRelativePath = myEditorForm.getRelativeFolder();
        /*
        myRootsPanel.createDirectories();
        myRootsPanel.saveData();
        */
    }

    public void reset() {
    }

    public void disposeUIResources() {
        // myRootsPanel.dispose();
    }

    public String getDeploymentDirectoryPath() {
        return myServerRootPath + "/" + myServerRelativePath;
    }

    /*private List<WebRoot> myWebRoots = new LinkedList<WebRoot>();*/

    // private CfmlFacetSourceRootsPanel myRootsPanel;
    private CfmlFacetEditorForm myEditorForm = new CfmlFacetEditorForm("", "");
    private String myServerRootPath;
    private String myServerRelativePath;

    public String getMyServerRootPath() {
        return myServerRootPath;
    }

    public String getMyServerRelativePath() {
        return myServerRelativePath;
    }
}
