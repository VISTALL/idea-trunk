package com.intellij.coldFusion.UI.runner;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.impl.FileChooserFactoryImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Lera Nikolaenko
 * Date: 07.04.2009
 */
public class CfmlRunConfigForm {
    private JPanel myMainPanel;
    private JTextField myFileToView;
    private JButton myFileChooserButton;
    private JTextField myRootURL;

    public CfmlRunConfigForm(final Project project) {
        myRootURL.setText("localhost:8500");
        myFileChooserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (ProjectManager.getInstance().getOpenProjects().length == 0) {
                    return;
                }
                FileChooserDialog fileChooserDialog = FileChooserFactoryImpl.getInstance().createFileChooser(
                        FileChooserDescriptorFactory.createSingleLocalFileDescriptor(), project);
                VirtualFile[] files = fileChooserDialog.choose(null, null);
                if (files.length == 1) {
                    String textFieldContent = files[0].getPath();
                    myFileToView.setText(textFieldContent);
                }
            }
        });
    }

    public JPanel getMainPanel() {
        return myMainPanel;
    }

    public String getRootURL() {
        return myRootURL.getText();
    }

    public void setRootURL(String s) {
        myRootURL.setText(s);
    }

    public String getFileToView() {
        return myFileToView.getText();
    }

    public void setFileToView(String s) {
        myFileToView.setText(s);
    }
}
