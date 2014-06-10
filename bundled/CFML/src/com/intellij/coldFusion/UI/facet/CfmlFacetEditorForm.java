package com.intellij.coldFusion.UI.facet;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.impl.FileChooserFactoryImpl;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 08.04.2009
 * Time: 20:00:32
 * To change this template use File | Settings | File Templates.
 */
public class CfmlFacetEditorForm {
    private JTextField myServerRootTextField;
    private JButton myChooserServerDirButton;
    private JPanel myMainPanel;
    private JPanel myTablePanel;
    private JTextField myFolderTextField;

    private CfmlFacetSourceRootsPanel myRootsPanel;

    public CfmlFacetEditorForm(String serverRootPath, String relativePath/*CfmlFacetSourceRootsPanel rootsPanel*/) {
        myServerRootTextField.setText(serverRootPath);
        myFolderTextField.setText(relativePath);
        //myRootsPanel = rootsPanel;
        myChooserServerDirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (ProjectManager.getInstance().getOpenProjects().length == 0) {
                    return;
                }
                FileChooserDialog fileChooserDialog = FileChooserFactoryImpl.getInstance().createFileChooser(FileChooserDescriptorFactory.createSingleFolderDescriptor(), getMainPanel());
                VirtualFile[] files = fileChooserDialog.choose(null, null);
                if (files.length == 1) {
                    String textFieldContent = files[0].getPath();
                    myServerRootTextField.setText(textFieldContent);
                }
            }
        });
    }

    public void setServerPath(String defaultServerPath) {
        myServerRootTextField.setText(defaultServerPath);
    }

    public String getServerPath() {
        return myServerRootTextField.getText();
    }

    public JPanel getMainPanel() {
        return myMainPanel;
    }

    public String getRelativeFolder() {
        return myFolderTextField.getText();
    }

    public void setRelativeFolder(String relativeFolder) {
        myFolderTextField.setText(relativeFolder);
    }

    private void createUIComponents() {
        /*
        myTablePanel = new JPanel();
        myTablePanel.add(myRootsPanel.getComponent());
        */
    }
}
