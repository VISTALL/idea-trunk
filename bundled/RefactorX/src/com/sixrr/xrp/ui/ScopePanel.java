package com.sixrr.xrp.ui;

import com.intellij.find.FindSettings;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlDoctype;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlProlog;
import com.intellij.ui.IdeBorderFactory;
import com.sixrr.xrp.context.*;
import com.sixrr.xrp.psi.PsiUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"OverridableMethodCallInConstructor"})
public class ScopePanel extends JPanel{
    private final JRadioButton projectButton;
    private final JRadioButton moduleButton;
    private final JRadioButton directoryButton;
    private final JRadioButton currentFileButton;
    private final JCheckBox recursiveCheckbox;
    private final JCheckBox filterByURICheckbox;
    private final JCheckBox filterByRegexCheckbox;
    private final JTextField regexFilterField;

    private final ComboBox moduleComboBox;
    private final ComboBox directoryComboBox;
    private final FixedSizeButton selectDirectoryButton;

    private final XmlFile file;
    private final ScopePanelListener parent;
    private final String uri;

    public ScopePanel(XmlFile file, ScopePanelListener parent){
        super();
        this.file = file;
        uri = calculateDocTypeURI();
        this.parent = parent;
        final Project project = file.getProject();
        setLayout(new GridBagLayout());
        final TitledBorder border = IdeBorderFactory.createTitledBorder("Scope");
        setBorder(border);
        final GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;
        gbConstraints.gridwidth = 2;
        gbConstraints.weightx = 1.0;
        projectButton = new JRadioButton("Whole project", true);
        projectButton.setMnemonic('p');
        add(projectButton, gbConstraints);

        gbConstraints.gridx = 0;
        gbConstraints.gridy = 1;
        gbConstraints.weightx = 0.0;
        gbConstraints.gridwidth = 1;
        moduleButton = new JRadioButton("Module: ", false);
        moduleButton.setMnemonic('o');
        add(moduleButton, gbConstraints);

        gbConstraints.gridx = 1;
        gbConstraints.gridy = 1;
        gbConstraints.weightx = 1.0;
        final ModuleManager moduleManager = ModuleManager.getInstance(project);
        final Module[] modules = moduleManager.getModules();
        final String[] names = new String[modules.length];
        for(int i = 0; i < modules.length; i++){
            names[i] = modules[i].getName();
        }

        Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
        moduleComboBox = new ComboBox(names, -1);
        add(moduleComboBox, gbConstraints);

        gbConstraints.gridx = 0;
        gbConstraints.gridy = 2;
        gbConstraints.weightx = 0.0;
        gbConstraints.gridwidth = 1;
        directoryButton = new JRadioButton("Directory: ", false);
        directoryButton.setMnemonic('D');
        add(directoryButton, gbConstraints);

        gbConstraints.gridx = 0;
        gbConstraints.gridy = 4;
        gbConstraints.weightx = 0.0;
        gbConstraints.gridwidth = 1;
        currentFileButton = new JRadioButton("Current file only", false);
        currentFileButton.setMnemonic('C');
        add(currentFileButton, gbConstraints);

        if(uri != null){
            gbConstraints.gridx = 0;
            gbConstraints.gridy = 5;
            gbConstraints.weightx = 1.0;
            gbConstraints.gridwidth = 2;
            filterByURICheckbox = new JCheckBox("Limit to files with DTD: " + uri, true);
            filterByURICheckbox.setSelected(true);
            filterByURICheckbox.setMnemonic('D');
            add(filterByURICheckbox, gbConstraints);
        } else{
            filterByURICheckbox = null;
        }
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 6;
        gbConstraints.weightx = 1.0;
        gbConstraints.gridwidth = 2;
        filterByRegexCheckbox = new JCheckBox("Limit to files with names matching regular expression: ");
        filterByRegexCheckbox.setSelected(false);
        filterByRegexCheckbox.setMnemonic('r');
        add(filterByRegexCheckbox, gbConstraints);
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 7;
        gbConstraints.weightx = 1.0;
        gbConstraints.gridwidth = 2;
        gbConstraints.insets = new Insets(0, 16, 0, 0);
        regexFilterField = new JTextField();
        add(regexFilterField, gbConstraints);

        gbConstraints.gridx = 1;
        gbConstraints.gridy = 2;
        gbConstraints.weightx = 1.0;
        gbConstraints.insets = new Insets(0, 16, 0, 0);

        directoryComboBox = new ComboBox(-1);
        final Component editorComponent = directoryComboBox.getEditor().getEditorComponent();
        if(editorComponent instanceof JTextField){
            final JTextField field = (JTextField) editorComponent;
            field.setColumns(40);
        }
        add(directoryComboBox, gbConstraints);

        gbConstraints.weightx = 0.0;
        gbConstraints.gridx = 2;
        gbConstraints.insets = new Insets(0, 1, 0, 0);
        selectDirectoryButton = new FixedSizeButton(directoryComboBox);
        TextFieldWithBrowseButton.MyDoClickAction.addTo(selectDirectoryButton, directoryComboBox);
        selectDirectoryButton.setMargin(new Insets(0, 0, 0, 0));
        add(selectDirectoryButton, gbConstraints);

        gbConstraints.gridx = 0;
        gbConstraints.gridy = 3;
        gbConstraints.weightx = 1.0;
        gbConstraints.gridwidth = 2;
        gbConstraints.insets = new Insets(0, 16, 0, 0);
        recursiveCheckbox = new JCheckBox("Recursively", true);
        recursiveCheckbox.setSelected(true);
        recursiveCheckbox.setMnemonic('r');
        add(recursiveCheckbox, gbConstraints);

        final ButtonGroup bgScope = new ButtonGroup();
        bgScope.add(currentFileButton);
        bgScope.add(directoryButton);
        bgScope.add(projectButton);
        bgScope.add(moduleButton);

        projectButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                validateScopeControls();
                validateRefactoring();
            }
        });
        currentFileButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                validateScopeControls();
                validateRefactoring();
            }
        });
        filterByRegexCheckbox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                validateScopeControls();
                validateRefactoring();
            }
        });

        directoryButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                validateScopeControls();
                validateRefactoring();
                directoryComboBox.getEditor().getEditorComponent().requestFocusInWindow();
            }
        });

        moduleButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                validateScopeControls();
                validateRefactoring();
                moduleComboBox.requestFocusInWindow();
            }
        });

        selectDirectoryButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
                final VirtualFile[] files = FileChooser.chooseFiles(project, descriptor);
                if(files.length != 0){
                    directoryComboBox.setSelectedItem(files[0].getPresentableUrl());
                    validateRefactoring();
                }
            }
        });
        final String directoryName = file.getContainingDirectory().getVirtualFile().getPath();
        final List recentDirectories = FindSettings.getInstance().getRecentDirectories();
        setDirectories(recentDirectories, directoryName);
        final Module moduleForFile = PsiUtils.getModuleForFile(file);
        if(moduleForFile != null){
            final String selectedModuleName = moduleForFile.getName();
            moduleComboBox.setSelectedItem(selectedModuleName);
        }
        projectButton.setEnabled(true);
        moduleButton.setEnabled(true);
        directoryButton.setEnabled(true);
        currentFileButton.setEnabled(true);

        moduleComboBox.setEnabled(false);
        recursiveCheckbox.setEnabled(false);
        directoryComboBox.setEnabled(false);
        selectDirectoryButton.setEnabled(false);

        projectButton.setSelected(true);
        recursiveCheckbox.setSelected(true);
        filterByRegexCheckbox.setSelected(false);

        initCombobox(directoryComboBox);
    }

    private String calculateDocTypeURI(){
        final XmlDocument document = file.getDocument();
        if(document == null){
            return null;
        }
        final XmlProlog prolog = document.getProlog();
        if(prolog == null){
            return null;
        }
        final XmlDoctype doctype = prolog.getDoctype();
        if(doctype == null){
            return null;
        }
        return doctype.getDtdUri();
    }

    public Context getContext(){
        final Project project = file.getProject();
        final String regex = getRegex();
        if(projectButton.isSelected()){
            Context context = new ProjectContext(project);
            if(uri != null && filterByURICheckbox.isSelected()){
                context = new URIFilteredContext(context, uri);
            }
            if(regex != null && regex.length() != 0){
                context = new RegexFilteredContext(context, regex);
            }
            return context;
        } else if(moduleButton.isSelected()){
            final String moduleName = (String) moduleComboBox.getSelectedItem();
            final ModuleManager moduleManager = ModuleManager.getInstance(project);
            final Module module = moduleManager.findModuleByName(moduleName);
            Context context = new ModuleContext(module);
            if(uri != null && filterByURICheckbox.isSelected()){
                context = new URIFilteredContext(context, uri);
            }

            if(regex != null && regex.length() != 0){
                context = new RegexFilteredContext(context, regex);
            }
            return context;
        } else if(directoryButton.isSelected()){
            final boolean recursive = recursiveCheckbox.isSelected();
            final String directoryName = (String) directoryComboBox.getSelectedItem();
            final PsiManager manager = file.getManager();
            final VirtualFileManager fileManager = VirtualFileManager.getInstance();
            final VirtualFile virtualDirectory = fileManager.findFileByUrl("file://" + directoryName);
            if(virtualDirectory != null){
                final PsiDirectory directory = manager.findDirectory(virtualDirectory);
                Context context = new DirectoryContext(directory, recursive);
                if(uri != null && filterByURICheckbox.isSelected()){
                    context = new URIFilteredContext(context, uri);
                }

                if(regex != null && regex.length() != 0){
                    context = new RegexFilteredContext(context, regex);
                }
                return context;
            } else{
                return new EmptyContext();
            }
        } else{
            return new SingleFileContext(file);
        }
    }

    private String getRegex(){
        if(!filterByRegexCheckbox.isSelected()){
            return null;
        }
        return regexFilterField.getText();
    }

    private void setDirectories(List strings, String directoryName){

        if(directoryName != null && directoryName.length() > 0){
            if(strings.contains(directoryName)){
                strings.remove(directoryName);
            }
            directoryComboBox.addItem(directoryName);
        }
        for(int i = strings.size() - 1; i >= 0; i--){
            directoryComboBox.addItem(strings.get(i));
        }
        if(directoryComboBox.getItemCount() == 0){
            directoryComboBox.addItem("");
        }
    }

    private void validateScopeControls(){
        final boolean directorySelected = directoryButton.isSelected();
        if(directorySelected){
            recursiveCheckbox.setEnabled(true);
        } else{
            recursiveCheckbox.setEnabled(false);
        }
        regexFilterField.setEnabled(filterByRegexCheckbox.isSelected());
        directoryComboBox.setEnabled(directorySelected);
        selectDirectoryButton.setEnabled(directorySelected);
        final boolean moduleSelected = moduleButton.isSelected();
        moduleComboBox.setEnabled(moduleSelected);
    }

    private void validateRefactoring(){
        parent.scopeSelectionHasChanged();
    }

    public boolean isScopeValid(){
        if(directoryButton.isSelected()){
            return getDirectory() != null && getDirectory().length() != 0;
        } else{
            return true;
        }
    }

    private String getDirectory(){
        return (String) directoryComboBox.getSelectedItem();
    }

    private void initCombobox(final ComboBox comboBox){
        comboBox.setEditable(true);
        comboBox.setMaximumRowCount(8);

        comboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                validateRefactoring();
            }
        });

        final Component editorComponent = comboBox.getEditor().getEditorComponent();
        editorComponent.addKeyListener(
                new KeyAdapter(){
                    public void keyReleased(KeyEvent e){
                        final Object item = comboBox.getEditor().getItem();
                        if(item != null && !item.equals(comboBox.getSelectedItem())){
                            final int caretPosition = getCaretPosition(comboBox);
                            comboBox.setSelectedItem(item);
                            setCaretPosition(comboBox, caretPosition);
                        }
                        validateRefactoring();
                    }
                }
        );
    }

    private static int getCaretPosition(JComboBox comboBox){
        final ComboBoxEditor editor = comboBox.getEditor();
        final Component editorComponent = editor.getEditorComponent();
        if(editorComponent instanceof JTextField){
            final JTextField textField = (JTextField) editorComponent;
            return textField.getCaretPosition();
        }
        return 0;
    }

    private static void setCaretPosition(JComboBox comboBox, int position){
        final ComboBoxEditor editor = comboBox.getEditor();
        final Component editorComponent = editor.getEditorComponent();
        if(editorComponent instanceof JTextField){
            final JTextField textField = (JTextField) editorComponent;
            textField.setCaretPosition(position);
        }
    }
}
