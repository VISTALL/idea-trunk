package com.intellij.coldFusion.UI.runner;

import com.intellij.coldFusion.UI.CfmlIcons;
import com.intellij.coldFusion.UI.components.CfmlApplicationComponent;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 07.04.2009
 * Time: 11:34:22
 * To change this template use File | Settings | File Templates.
 */
public class CfmlConfigurable extends BaseConfigurable implements SearchableConfigurable {
    private CfmlConfigForm myForm;

    public CfmlConfigurable() {
        myForm = new CfmlConfigForm();
    }

    @Nls
    public String getDisplayName() {
        return "Cold Fusion";
    }

    public Icon getIcon() {
        return CfmlIcons.FILETYPE_ICON;
    }

    public String getHelpTopic() {
        return "";
    }

    public JComponent createComponent() {
        return myForm.getMyMainPanel();
    }

    public void apply() throws ConfigurationException {
        CfmlApplicationComponent.getInstance().setTestServerDirectoryPath(myForm.getMyFolderPath());
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void reset() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void disposeUIResources() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getId() {
        return getHelpTopic();
    }

    public Runnable enableSearch(String option) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isModified() {
        return true;
    }
}
