package org.jetbrains.idea.svn.config;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Ref;
import com.intellij.util.Icons;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnServerFileManager;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.KeyEvent;
import java.util.*;

public class SvnConfigureProxiesComponent extends MasterDetailsComponent {
  private static final Icon COPY_ICON = IconLoader.getIcon("/actions/copy.png");
  private final SvnServerFileManager myManager;

  private final CompositeRunnable myTreeUpdaterValidator;
  private final Runnable myValidator;
  private JComponent myComponent;
  private final TestConnectionPerformer myTestConnectionPerformer;
  private ConfigureProxiesOptionsPanel myDefaultGroupPanel;

  public SvnConfigureProxiesComponent(final SvnServerFileManager manager, final GroupsValidator validator, final TestConnectionPerformer testConnectionPerformer) {
    myTestConnectionPerformer = testConnectionPerformer;
    myValidator = validator;
    myTreeUpdaterValidator = new CompositeRunnable(TREE_UPDATER, myValidator);
    initTree();
    myManager = manager;
    fillTree();
    validator.add(this);
  }

  public JComponent createComponent() {
    if (myComponent == null) {
      myComponent = super.createComponent();
    }
    return myComponent;
  }

  protected void processRemovedItems() {
    // not used
  }

  protected boolean wasObjectStored(final Object editableObject) {
    return false;
  }

  public String getDisplayName() {
    return "HTTP proxies configuration";
  }

  public Icon getIcon() {
    return null;
  }

  public String getHelpTopic() {
    return null;
  }

  private String getNewName() {
    return "Unnamed";
  }

  private void addGroup(final ProxyGroup template) {
    final ProxyGroup group;
    if (template == null) {
      group = new ProxyGroup(getNewName(), "", Collections.EMPTY_MAP);
    } else {
      group = new ProxyGroup(getNewName(), template.getPatterns(), template.getProperties());
    }
    
    addNode(createNodeForObject(group), myRoot);
    selectNodeInTree(group);
  }

  public List<String> getGlobalGroupRepositories(final Collection<String> all) {
    // i.e. all-[all used]
    final List<String> result = new LinkedList<String>(all);

    for (int i = 0; i < myRoot.getChildCount(); i++) {
      final MyNode node = (MyNode) myRoot.getChildAt(i);
      final GroupConfigurable groupConfigurable = (GroupConfigurable) ((MyNode) node).getConfigurable();
      if (! groupConfigurable.getEditableObject().isDefault()) {
        result.removeAll(groupConfigurable.getRepositories());
      }
    }

    return result;
  }

  public boolean validate(final ValidationListener listener) {
    final Ref<String> errorMessageRef = new Ref<String>();
    final Set<String> checkSet = new HashSet<String>();
    final AmbiguousPatternsFinder ambiguousPatternsFinder = new AmbiguousPatternsFinder();
    
    for (int i = 0; i < myRoot.getChildCount(); i++) {
      final MyNode node = (MyNode) myRoot.getChildAt(i);
      final GroupConfigurable groupConfigurable = (GroupConfigurable) ((MyNode) node).getConfigurable();
      final String groupName = groupConfigurable.getEditableObject().getName();

      if (checkSet.contains(groupName)) {
        listener.onError(SvnBundle.message("dialog.edit.http.proxies.settings.error.same.group.names.text", groupName), myComponent, true);
        return false;
      }
      checkSet.add(groupName);
    }

    for (int i = 0; i < myRoot.getChildCount(); i++) {
      final MyNode node = (MyNode) myRoot.getChildAt(i);
      final GroupConfigurable groupConfigurable = (GroupConfigurable) ((MyNode) node).getConfigurable();
      groupConfigurable.applyImpl();
      if(! groupConfigurable.validate(errorMessageRef)) {
        listener.onError(errorMessageRef.get(), myComponent, false);
        return false;
      }

      if (! groupConfigurable.getEditableObject().isDefault()) {
        final String groupName = groupConfigurable.getEditableObject().getName();
        final List<String> urls = groupConfigurable.getRepositories();
        ambiguousPatternsFinder.acceptUrls(groupName, urls);
      }
    }

    if(! ambiguousPatternsFinder.isValid(errorMessageRef)) {
      listener.onError(errorMessageRef.get(), myComponent, false);
      return false;
    }
    return true;
  }

  protected ArrayList<AnAction> createActions(final boolean fromPopup) {
    ArrayList<AnAction> result = new ArrayList<AnAction>();
    result.add(new AnAction("Add", "Add", Icons.ADD_ICON) {
        {
            registerCustomShortcutSet(CommonShortcuts.INSERT, myTree);
        }
        public void actionPerformed(AnActionEvent event) {
          addGroup(null);
        }


    });
    result.add(new MyDeleteAction( new Condition<Object>(){
      public boolean value(final Object o) {
        if (o instanceof MyNode) {
          final MyNode node = (MyNode) o;
          if (node.getConfigurable() instanceof GroupConfigurable) {
            final ProxyGroup group = ((GroupConfigurable) node.getConfigurable()).getEditableObject();
            return ! group.isDefault();
          }
        }
        return false;
      }
    }) {
      public void actionPerformed(final AnActionEvent e) {
        final TreePath path = myTree.getSelectionPath();
        final MyNode node = (MyNode)path.getLastPathComponent();
        final MyNode parentNode = (MyNode) node.getParent();
        int idx = parentNode.getIndex(node);

        super.actionPerformed(e);

        idx = (idx == parentNode.getChildCount()) ? idx - 1 : idx;
        if (parentNode.getChildCount() > 0) {
          final TreePath newSelectedPath = new TreePath(parentNode.getPath()).pathByAddingChild(parentNode.getChildAt(idx));
          myTree.setSelectionPath(newSelectedPath);
        }
      }
    });

    result.add(new AnAction("Copy", "Copy", COPY_ICON) {
        {
            registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK)), myTree);
        }
        public void actionPerformed(AnActionEvent event) {
          // apply - for update of editable object
          try {
            getSelectedConfugurable().apply();
          } catch (ConfigurationException e) {
            // suppress & wait for OK
          }
          final ProxyGroup selectedGroup = (ProxyGroup) getSelectedObject();
          if (selectedGroup != null) {
            addGroup(selectedGroup);
          }
        }

        public void update(AnActionEvent event) {
            super.update(event);
            event.getPresentation().setEnabled(getSelectedObject() != null);
        }
    });
    return result;
  }

  public void apply() throws ConfigurationException {
    final List<ProxyGroup> groups = new ArrayList<ProxyGroup>(myRoot.getChildCount());

    for (int i = 0; i < myRoot.getChildCount(); i++) {
      final MyNode node = (MyNode) myRoot.getChildAt(i);
      final GroupConfigurable groupConfigurable = (GroupConfigurable) ((MyNode) node).getConfigurable();
      groupConfigurable.apply();
      groups.add(groupConfigurable.getEditableObject());
    }

    myManager.updateUserServerFile(groups);
  }

  public void reset() {
    super.reset();
    
    for (int i = 0; i < myRoot.getChildCount(); i++) {
      final MyNode node = (MyNode) myRoot.getChildAt(i);
      final GroupConfigurable groupConfigurable = (GroupConfigurable) ((MyNode) node).getConfigurable();
      groupConfigurable.reset();
    }
  }

  private MyNode createNodeForObject(final ProxyGroup group) {
    final ConfigureProxiesOptionsPanel panel = new ConfigureProxiesOptionsPanel(myValidator, myTestConnectionPerformer);
    final GroupConfigurable gc = new GroupConfigurable(group, myTreeUpdaterValidator, panel);
    if (group.isDefault()) {
      myDefaultGroupPanel = panel;
    }

    // first added node must be global /default group. since url-fillers for other groups forwards their updates to global group
    assert myDefaultGroupPanel != null;
    panel.setPatternsListener((group.isDefault()) ? PatternsListener.Empty.instance :
                              new RepositoryUrlFilter(panel, this, myDefaultGroupPanel));

    return new MyNode(gc, group.isDefault());
  }

  private void fillTree() {
    myRoot.removeAllChildren();

    DefaultProxyGroup defaultProxyGroup = myManager.getDefaultGroup();
    defaultProxyGroup = (defaultProxyGroup == null) ? new DefaultProxyGroup(Collections.EMPTY_MAP) : defaultProxyGroup;
    final Map<String, ProxyGroup> userGroups = myManager.getGroups();

    myRoot.add(createNodeForObject(defaultProxyGroup));
    for (Map.Entry<String, ProxyGroup> entry : userGroups.entrySet()) {
      myRoot.add(createNodeForObject(entry.getValue()));
    }

    TreeUtil.sort(myRoot, GroupNodesComparator.getInstance());
    ((DefaultTreeModel) myTree.getModel()).reload(myRoot);
  }

  protected void addNode(final MyNode nodeToAdd, final MyNode parent) {
    parent.add(nodeToAdd);
    TreeUtil.sort(parent, GroupNodesComparator.getInstance());
    ((DefaultTreeModel)myTree.getModel()).reload(parent);
  }

  private static class GroupNodesComparator implements Comparator {
    private final static GroupNodesComparator instance = new GroupNodesComparator();

    private static GroupNodesComparator getInstance() {
      return instance;
    }

    public int compare(final Object o1, final Object o2) {
      MyNode node1 = (MyNode)o1;
      MyNode node2 = (MyNode)o2;

      if ((node1.getConfigurable() instanceof GroupConfigurable) && (node2.getConfigurable() instanceof GroupConfigurable)) {
        final ProxyGroup group1 = ((GroupConfigurable) node1.getConfigurable()).getEditableObject();
        final ProxyGroup group2 = ((GroupConfigurable) node2.getConfigurable()).getEditableObject();
        if (group1.isDefault()) {
          return -1;
        }
        if (group2.isDefault()) {
          return 1;
        }
      }

      return node1.getDisplayName().compareToIgnoreCase(node2.getDisplayName());
    }
  }

  /**
   * total component made invalid => for test connection to be forbidden when for ex. ambiguous settings given
   */
  public void setIsValid(final boolean valid) {
    for (int i = 0; i < myRoot.getChildCount(); i++) {
      final MyNode node = (MyNode) myRoot.getChildAt(i);
      final GroupConfigurable groupConfigurable = (GroupConfigurable) ((MyNode) node).getConfigurable();
      groupConfigurable.setIsValid(valid);
    }
  }
}
