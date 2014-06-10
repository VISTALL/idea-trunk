package com.intellij.coldFusion.UI.runner;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 21.04.2009
 * Time: 16:33:23
 * To change this template use File | Settings | File Templates.
 */
public class CfmlRunnerParameters {
    private String myRootURL = "";
    private String myStartFile = "";

    public CfmlRunnerParameters(String myRootURL, String myStartFile) {
        this.myRootURL = myRootURL;
        this.myStartFile = myStartFile;
    }

    public String getRootURL() {
        return myRootURL;
    }

    public String getStartFile() {
        return myStartFile;
    }

    public void setRootURL(String myRootURL) {
        this.myRootURL = myRootURL;
    }

    public void setStartFile(String myStartFile) {
        this.myStartFile = myStartFile;
    }
}
