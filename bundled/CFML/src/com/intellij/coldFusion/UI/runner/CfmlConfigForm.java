package com.intellij.coldFusion.UI.runner;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.impl.FileChooserFactoryImpl;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Lera Nikolaenko
 * Date: 07.04.2009
 */
public class CfmlConfigForm implements ActionListener {
    private JTextField myFolderPathTextField;
    private JButton myFolderChooserButton;
    private JPanel myMainPanel;

    public CfmlConfigForm() {
        myFolderChooserButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (ProjectManager.getInstance().getOpenProjects().length == 0) {
            return;
        }
        FileChooserDialog fileChooserDialog = FileChooserFactoryImpl.getInstance().createFileChooser(FileChooserDescriptorFactory.createSingleFolderDescriptor(), getMyMainPanel());
        VirtualFile[] files = fileChooserDialog.choose(null, null);
        if (files.length == 1) {
            String textFieldContent = files[0].getPath();
            myFolderPathTextField.setText(textFieldContent);
        }
    }

    public JPanel getMyMainPanel() {
        return myMainPanel;
    }

    public String getMyFolderPath() {
        return myFolderPathTextField.getText();
    }
}
