package org.jetbrains.idea.maven.wizards;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.SpeedSearchBase;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.indices.ArchetypeInfo;
import org.jetbrains.idea.maven.indices.MavenIndicesManager;
import org.jetbrains.idea.maven.navigator.SelectMavenProjectDialog;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.MavenId;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class MavenModuleWizardStep extends ModuleWizardStep {
  private static final Icon WIZARD_ICON = IconLoader.getIcon("/addmodulewizard.png");

  private static final String INHERIT_GROUP_ID_KEY = "MavenModuleWizard.inheritGroupId";
  private static final String INHERIT_VERSION_KEY = "MavenModuleWizard.inheritVersion";
  private static final String ARCHETYPE_ARTIFACT_ID_KEY = "MavenModuleWizard.archetypeArtifactIdKey";
  private static final String ARCHETYPE_GROUP_ID_KEY = "MavenModuleWizard.archetypeGroupIdKey";
  private static final String ARCHETYPE_VERSION_KEY = "MavenModuleWizard.archetypeVersionKey";

  private final Project myProjectOrNull;
  private final MavenModuleBuilder myBuilder;
  private MavenProject myAggregator;
  private MavenProject myParent;

  private String myInheritedGroupId;
  private String myInheritedVersion;

  private JPanel myMainPanel;

  private JLabel myAggregatorLabel;
  private JLabel myAggregatorNameLabel;
  private JButton mySelectAggregator;

  private JLabel myParentLabel;
  private JLabel myParentNameLabel;
  private JButton mySelectParent;

  private JTextField myGroupIdField;
  private JCheckBox myInheritGroupIdCheckBox;
  private JTextField myArtifactIdField;
  private JTextField myVersionField;
  private JCheckBox myInheritVersionCheckBox;

  private JCheckBox myUseArchetypeCheckBox;
  private JButton myAddArchetypeButton;
  private Tree myArchetypesTree;
  private JScrollPane myArchetypeDescriptionScrollPane;
  private JTextArea myArchetypeDescriptionField;

  public MavenModuleWizardStep(@Nullable Project project, MavenModuleBuilder builder) {
    myProjectOrNull = project;
    myBuilder = builder;

    initComponents();
    loadSettings();
  }

  private void initComponents() {
    mySelectAggregator.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myAggregator = doSelectProject(myAggregator);
        updateComponents();
      }
    });

    mySelectParent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myParent = doSelectProject(myParent);
        updateComponents();
      }
    });

    ActionListener updatingListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateComponents();
      }
    };
    myInheritGroupIdCheckBox.addActionListener(updatingListener);
    myInheritVersionCheckBox.addActionListener(updatingListener);

    myUseArchetypeCheckBox.addActionListener(updatingListener);
    myArchetypesTree.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    myAddArchetypeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doAddArchetype();
      }
    });

    myArchetypesTree.setRootVisible(false);
    myArchetypesTree.setShowsRootHandles(true);
    myArchetypesTree.setCellRenderer(new MyRenderer());
    myArchetypesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    myArchetypesTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        updateArchetypeDescription();
      }
    });

    new TreeSpeedSearch(myArchetypesTree, new Convertor<TreePath, String>() {
      public String convert(TreePath path) {
        ArchetypeInfo info = getArchetypeInfoFromPathComponent(path.getLastPathComponent());
        return info.groupId + ":" + info.artifactId + ":" + info.version;
      }
    }).setComparator(new SpeedSearchBase.SpeedSearchComparator(false) {
      @Override
      public void translateCharacter(StringBuilder buf, char ch) {
        if (ch == '*') {
          buf.append("(.)*");
        }
        else {
          super.translateCharacter(buf, ch);
        }
      }
    });

    myArchetypeDescriptionField.setEditable(false);
    myArchetypeDescriptionField.setBackground(UIUtil.getPanelBackground());
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myGroupIdField;
  }

  private MavenProject doSelectProject(MavenProject current) {
    assert myProjectOrNull != null : "must not be called when creating a new project";

    SelectMavenProjectDialog d = new SelectMavenProjectDialog(myProjectOrNull, current);
    d.show();
    if (!d.isOK()) return current;
    return d.getResult();
  }

  private void doAddArchetype() {
    MavenAddArchetypeDialog dialog = new MavenAddArchetypeDialog(myMainPanel);
    dialog.show();
    if (!dialog.isOK()) return;

    ArchetypeInfo archetype = dialog.getArchetype();
    MavenIndicesManager.getInstance().addArchetype(archetype);
    updateArchetypesList(archetype);
  }

  @Override
  public void onStepLeaving() {
    saveSettings();
  }

  private void loadSettings() {
    myBuilder.setInheritedOptions(getSavedValue(INHERIT_GROUP_ID_KEY, true),
                                  getSavedValue(INHERIT_VERSION_KEY, true));

    String archGroupId = getSavedValue(ARCHETYPE_GROUP_ID_KEY, null);
    String archArtifactId = getSavedValue(ARCHETYPE_ARTIFACT_ID_KEY, null);
    String archVersion = getSavedValue(ARCHETYPE_VERSION_KEY, null);
    if (archGroupId == null || archArtifactId == null || archVersion == null) {
      myBuilder.setArchetype(null);
    }
    else {
      myBuilder.setArchetype(new ArchetypeInfo(archGroupId, archArtifactId, archVersion, null, null));
    }
  }

  private void saveSettings() {
    saveValue(INHERIT_GROUP_ID_KEY, myInheritGroupIdCheckBox.isSelected());
    saveValue(INHERIT_VERSION_KEY, myInheritVersionCheckBox.isSelected());

    ArchetypeInfo arch = getSelectedArchetype();
    saveValue(ARCHETYPE_GROUP_ID_KEY, arch == null ? null : arch.groupId);
    saveValue(ARCHETYPE_ARTIFACT_ID_KEY, arch == null ? null : arch.artifactId);
    saveValue(ARCHETYPE_VERSION_KEY, arch == null ? null : arch.version);
  }

  private boolean getSavedValue(String key, boolean defaultValue) {
    return getSavedValue(key, String.valueOf(defaultValue)).equals(String.valueOf(true));
  }

  private String getSavedValue(String key, String defaultValue) {
    String value = PropertiesComponent.getInstance().getValue(key);
    return value == null ? defaultValue : value;
  }

  private void saveValue(String key, boolean value) {
    saveValue(key, String.valueOf(value));
  }

  private void saveValue(String key, String value) {
    PropertiesComponent props = PropertiesComponent.getInstance();
    props.setValue(key, value);
  }

  public JComponent getComponent() {
    return myMainPanel;
  }

  @Override
  public void updateStep() {
    if (isMavenizedProject()) {
      MavenProject parent = myBuilder.findPotentialParentProject(myProjectOrNull);
      myAggregator = parent;
      myParent = parent;
    }

    myArtifactIdField.setText(myBuilder.getName());
    myGroupIdField.setText(myParent == null ? myBuilder.getName() : myParent.getMavenId().getGroupId());
    myVersionField.setText(myParent == null ? "1.0" : myParent.getMavenId().getVersion());

    myInheritGroupIdCheckBox.setSelected(myBuilder.isInheritGroupId());
    myInheritVersionCheckBox.setSelected(myBuilder.isInheritVersion());

    ArchetypeInfo selectedArch = getSelectedArchetype();
    if (selectedArch == null) {
      selectedArch = myBuilder.getArchetype();
    }
    if (selectedArch != null) myUseArchetypeCheckBox.setSelected(true);

    updateArchetypesList(selectedArch);
    updateComponents();
  }

  private void updateArchetypesList(ArchetypeInfo selected) {
    TreeNode root = groupAndSortArchetypes(MavenIndicesManager.getInstance().getArchetypes());
    TreeModel model = new DefaultTreeModel(root);
    myArchetypesTree.setModel(model);

    if (selected != null) {
      TreePath path = findNodePath(selected, model, model.getRoot());
      if (path != null) {
        myArchetypesTree.expandPath(path.getParentPath());
        TreeUtil.selectPath(myArchetypesTree, path, true);
      }
    }

    updateArchetypeDescription();
  }

  private void updateArchetypeDescription() {
    ArchetypeInfo sel = getSelectedArchetype();
    String desc = sel == null ? null : sel.description;
    if (StringUtil.isEmptyOrSpaces(desc)) {
      myArchetypeDescriptionScrollPane.setVisible(false);
    } else {
      myArchetypeDescriptionScrollPane.setVisible(true);
      myArchetypeDescriptionField.setText(desc);
    }
    myMainPanel.revalidate();
  }

  private TreePath findNodePath(ArchetypeInfo object, TreeModel model, Object parent) {
    for (int i = 0; i < model.getChildCount(parent); i++) {
      DefaultMutableTreeNode each = (DefaultMutableTreeNode)model.getChild(parent, i);
      if (each.getUserObject().equals(object)) return new TreePath(each.getPath());

      TreePath result = findNodePath(object, model, each);
      if (result != null) return result;
    }
    return null;
  }

  private TreeNode groupAndSortArchetypes(Set<ArchetypeInfo> archetypes) {
    List<ArchetypeInfo> list = new ArrayList<ArchetypeInfo>(archetypes);

    Collections.sort(list, new Comparator<ArchetypeInfo>() {
      public int compare(ArchetypeInfo o1, ArchetypeInfo o2) {
        String key1 = o1.groupId + ":" + o1.artifactId;
        String key2 = o2.groupId + ":" + o2.artifactId;

        int result = key1.compareToIgnoreCase(key2);
        if (result != 0) return result;

        return o2.version.compareToIgnoreCase(o1.version);
      }
    });

    Map<String, List<ArchetypeInfo>> map = new TreeMap<String, List<ArchetypeInfo>>();

    for (ArchetypeInfo each : list) {
      String key = each.groupId + ":" + each.artifactId;
      List<ArchetypeInfo> versions = map.get(key);
      if (versions == null) {
        versions = new ArrayList<ArchetypeInfo>();
        map.put(key, versions);
      }
      versions.add(each);
    }

    DefaultMutableTreeNode result = new DefaultMutableTreeNode("root", true);
    for (List<ArchetypeInfo> each : map.values()) {
      ArchetypeInfo eachArchetype = each.get(0);
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(eachArchetype, true);
      for (ArchetypeInfo eachVersion : each) {
        DefaultMutableTreeNode versionNode = new DefaultMutableTreeNode(eachVersion, false);
        node.add(versionNode);
      }
      result.add(node);
    }

    return result;
  }

  private boolean isMavenizedProject() {
    return myProjectOrNull != null && MavenProjectsManager.getInstance(myProjectOrNull).isMavenizedProject();
  }

  private void updateComponents() {
    if (!isMavenizedProject()) {
      myAggregatorLabel.setEnabled(false);
      myAggregatorNameLabel.setEnabled(false);
      mySelectAggregator.setEnabled(false);

      myParentLabel.setEnabled(false);
      myParentNameLabel.setEnabled(false);
      mySelectParent.setEnabled(false);
    }
    myAggregatorNameLabel.setText(formatProjectString(myAggregator));
    myParentNameLabel.setText(formatProjectString(myParent));

    if (myParent == null) {
      myGroupIdField.setEnabled(true);
      myVersionField.setEnabled(true);
      myInheritGroupIdCheckBox.setEnabled(false);
      myInheritVersionCheckBox.setEnabled(false);
    }
    else {
      myGroupIdField.setEnabled(!myInheritGroupIdCheckBox.isSelected());
      myVersionField.setEnabled(!myInheritVersionCheckBox.isSelected());

      if (myInheritGroupIdCheckBox.isSelected()
          || myGroupIdField.getText().equals(myInheritedGroupId)) {
        myGroupIdField.setText(myParent.getMavenId().getGroupId());
      }
      if (myInheritVersionCheckBox.isSelected()
          || myVersionField.getText().equals(myInheritedVersion)) {
        myVersionField.setText(myParent.getMavenId().getVersion());
      }
      myInheritedGroupId = myGroupIdField.getText();
      myInheritedVersion = myVersionField.getText();

      myInheritGroupIdCheckBox.setEnabled(true);
      myInheritVersionCheckBox.setEnabled(true);
    }

    boolean archetypesEnabled = myUseArchetypeCheckBox.isSelected();
    myAddArchetypeButton.setEnabled(archetypesEnabled);
    myArchetypesTree.setEnabled(archetypesEnabled);
    myArchetypesTree.setBackground(archetypesEnabled ? UIUtil.getListBackground() : UIUtil.getPanelBackground());
  }

  private String formatProjectString(MavenProject project) {
    if (project == null) return "<none>";
    return project.getMavenId().getDisplayString();
  }

  @Override
  public void updateDataModel() {
    myBuilder.setAggregatorProject(myAggregator);
    myBuilder.setParentProject(myParent);

    myBuilder.setProjectId(new MavenId(myGroupIdField.getText(),
                                       myArtifactIdField.getText(),
                                       myVersionField.getText()));
    myBuilder.setInheritedOptions(myInheritGroupIdCheckBox.isSelected(),
                                  myInheritVersionCheckBox.isSelected());

    myBuilder.setArchetype(getSelectedArchetype());
  }

  private ArchetypeInfo getSelectedArchetype() {
    if (!myUseArchetypeCheckBox.isSelected() || myArchetypesTree.isSelectionEmpty()) return null;
    return getArchetypeInfoFromPathComponent(myArchetypesTree.getLastSelectedPathComponent());
  }

  private ArchetypeInfo getArchetypeInfoFromPathComponent(Object sel) {
    return (ArchetypeInfo)((DefaultMutableTreeNode)sel).getUserObject();
  }

  @Override
  public Icon getIcon() {
    return WIZARD_ICON;
  }

  private class MyRenderer extends ColoredTreeCellRenderer {
    public void customizeCellRenderer(JTree tree,
                                      Object value,
                                      boolean selected,
                                      boolean expanded,
                                      boolean leaf,
                                      int row,
                                      boolean hasFocus) {
      Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
      if (!(userObject instanceof ArchetypeInfo)) return;

      ArchetypeInfo info = (ArchetypeInfo)userObject;

      if (leaf) {
        append(info.artifactId, SimpleTextAttributes.GRAY_ATTRIBUTES);
        append(":" + info.version, SimpleTextAttributes.REGULAR_ATTRIBUTES);
      }
      else {
        append(info.groupId + ":", SimpleTextAttributes.GRAY_ATTRIBUTES);
        append(info.artifactId, SimpleTextAttributes.REGULAR_ATTRIBUTES);
      }
    }
  }

  @Override
  public String getHelpId() {
    return "reference.dialogs.new.project.fromScratch.maven";
  }
}

