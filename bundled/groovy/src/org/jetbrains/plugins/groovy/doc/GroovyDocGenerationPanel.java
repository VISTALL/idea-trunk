package org.jetbrains.plugins.groovy.doc;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.NonFocusableCheckBox;
import com.intellij.ui.ScrollPaneFactory;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.doc.actions.GroovyDocAddPackageAction;
import org.jetbrains.plugins.groovy.doc.actions.GroovyDocReducePackageAction;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GroovyDocGenerationPanel extends JPanel {
  JPanel myPanel;
  TextFieldWithBrowseButton myOutputDir;
  NonFocusableCheckBox myIsUse;
  NonFocusableCheckBox myIsPrivate;
  NonFocusableCheckBox myOpenInBrowserCheckBox;
  TextFieldWithBrowseButton myInputDir;
  private JTextField myWindowTitle;
  JList myPackagesList;
  private JPanel myPackagesPanel;

  private DefaultActionGroup myActionGroup;
  private final ActionToolbar myActionToolbar;

  private GroovyDocAddPackageAction myAddPackageAction;
  private GroovyDocReducePackageAction myReducePackageAction;
  private final DefaultListModel myDataModel;

  GroovyDocGenerationPanel() {
    myInputDir.addBrowseFolderListener(GroovyDocBundle.message("groovydoc.generate.input.directory.browse"), null, null,
                                       FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myOutputDir.addBrowseFolderListener(GroovyDocBundle.message("groovydoc.generate.output.directory.browse"), null, null,
                                        FileChooserDescriptorFactory.createSingleFolderDescriptor());

    myDataModel = new DefaultListModel();

    myPackagesList = new JList(myDataModel);
    myPackagesList.setMinimumSize(new Dimension(100, 150));

    JScrollPane packagesScrollPane = ScrollPaneFactory.createScrollPane(myPackagesList);
    myPackagesPanel.setLayout(new BorderLayout());
    myPackagesPanel.setBorder(IdeBorderFactory.createTitledBorder("Source packages"));

    myActionToolbar = ActionManager.getInstance().createActionToolbar("GroovyDoc", getActionGroup(), true);
    myPackagesPanel.add(myActionToolbar.getComponent(), BorderLayout.NORTH);
    myPackagesPanel.add(packagesScrollPane, BorderLayout.SOUTH);

    myActionToolbar.updateActionsImmediately();
  }

  private ActionGroup getActionGroup() {
    if (myActionGroup == null) {
      initActions();
      myActionGroup = new DefaultActionGroup();
      myActionGroup.add(myAddPackageAction);
      myActionGroup.add(myReducePackageAction);
    }
    return myActionGroup;
  }

  private void initActions() {
    myAddPackageAction = new GroovyDocAddPackageAction(myDataModel);
    myReducePackageAction = new GroovyDocReducePackageAction(myPackagesList, myDataModel);
  }

  public void setPackagesList(String packagesName) {
    myDataModel.removeAllElements();
    myDataModel.add(0, packagesName);
  }

  public static class MyPackagesModel extends DefaultListModel {
    List<String> packagesNames = new ArrayList<String>();

    public int getSize() {
      return packagesNames.size();
    }

    public Object getElementAt(final int index) {
      return packagesNames.get(index);
    }

    public void add(String packageName) {
      final int index = getSize();
      packagesNames.add(packageName);
      fireContentsChanged(this, 0, index);
    }

    public Object remove(int index) {
      return packagesNames.remove(index);
    }
  }

  public DefaultListModel getDataModel() {
    return myDataModel;
  }

  public void apply(GroovyDocConfiguration configuration) {
    configuration.OUTPUT_DIRECTORY = toSystemIndependentFormat(myOutputDir.getText());
    configuration.INPUT_DIRECTORY = toSystemIndependentFormat(myInputDir.getText());
    configuration.PACKAGES = toStringArray(getDataModel());

    configuration.OPEN_IN_BROWSER = myOpenInBrowserCheckBox.isSelected();
    configuration.OPTION_IS_USE = myIsUse.isSelected();
    configuration.OPTION_IS_PRIVATE = myIsPrivate.isSelected();

    configuration.WINDOW_TITLE = myWindowTitle.getText();
  }

  public void reset(GroovyDocConfiguration configuration){
    myOutputDir.setText(toUserSystemFormat(configuration.OUTPUT_DIRECTORY));
    myInputDir.setText(toUserSystemFormat(configuration.INPUT_DIRECTORY));

    setPackagesList(configuration.PACKAGES[0]);

    myOpenInBrowserCheckBox.setSelected(configuration.OPEN_IN_BROWSER);
    myIsUse.setSelected(configuration.OPTION_IS_USE);
    myIsPrivate.setSelected(configuration.OPTION_IS_PRIVATE);
  }

  @Nullable
  private static String toSystemIndependentFormat(String directory) {
    if (directory.length() == 0) {
      return null;
    }
    return directory.replace(File.separatorChar, '/');
  }

  private static String toUserSystemFormat(String directory) {
    if (directory == null) {
      return "";
    }
    return directory.replace('/', File.separatorChar);
  }

private static String[] toStringArray(final DefaultListModel model) {
    final int count = model.getSize();
    Set<String> result = new HashSet<String>();
    for (int i = 0; i < count; i++) {
      final Object o = model.getElementAt(i);
      assert o instanceof String;

      result.add((String)o);
    }

    return result.toArray(new String[result.size()]);
  }

  public JPanel getPanel() {
    return myPanel;
  }
}
