package com.intellij.coldFusion.UI.components;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 30.09.2008
 */
public class CfmlApplicationComponent implements ApplicationComponent {
    private String myTestServerDirectoryPath = null;

    @NotNull
    public String getComponentName() {
        return "Cold Fusion";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    public static CfmlApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(CfmlApplicationComponent.class);
    }

    public void setTestServerDirectoryPath(String directoryPath) {
        myTestServerDirectoryPath = directoryPath;
    }

    public String getTestServerDirectoryPath() {
        return myTestServerDirectoryPath;
    }
}
