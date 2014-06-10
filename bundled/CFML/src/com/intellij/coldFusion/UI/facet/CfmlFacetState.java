package com.intellij.coldFusion.UI.facet;

public class CfmlFacetState {
    public String serverRootPath;
    public String serverRelativePath;

    public CfmlFacetState(String serverRootPath, String serverRelativePath) {
        this.serverRootPath = serverRootPath;
        this.serverRelativePath = serverRelativePath;
    }
}