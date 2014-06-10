package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class JobsMasterDetails extends MasterDetailsComponent {
  private final Project myProject;
  private ListSelectionListener myListSelectionListener;

  public JobsMasterDetails(Project project) {
    myProject = project;
    initTree();
    init();

    getSplitter().setProportion(0.3f);
  }

  public void onlyMain() {
    getSplitter().setProportion(0f);
    getSplitter().setSecondComponent(null);
  }

  protected void processRemovedItems() {

  }

  public JComponent getPrefferedFocusTarget() {
    return myTree;
  }

  public void setSelectionListener(ListSelectionListener selectionListener) {
    myListSelectionListener = selectionListener;
  }
  
  protected boolean wasObjectStored(Object editableObject) {
    return false;
  }

  @Nls
  public String getDisplayName() {
    return null;
  }

  public Icon getIcon() {
    return null;
  }

  @Nullable
  public PerforceJob getSelectedJob() {
    final MyNode[] myNodes = myTree.getSelectedNodes(MyNode.class, null);
    return myNodes == null ? null :
           (myNodes.length != 1 ? null : (PerforceJob) myNodes[0].getConfigurable().getEditableObject());
  }

  public void removeSelectedJob() {
    final TreePath path = myTree.getSelectionPath();
    if (path != null) {
      removePaths(path);
    }
    if (myTree.getRowCount() > 0) {
      myTree.setSelectionRow(0);
    }
  }

  public void addJob(final PerforceJob job) {
    final MyConfigurable configurable = new MyConfigurable(myProject, job);
    myRoot.add(new MyNode(configurable));

    ((DefaultTreeModel) myTree.getModel()).reload(myRoot);

    myTree.setSelectionRow(myTree.getRowCount() - 1);
  }

  public List<PerforceJob> getJobs() {
    final List<PerforceJob> result = new ArrayList<PerforceJob>();

    final int childCount = myRoot.getChildCount();
    for (int i = 0; i < childCount; i++) {
      final MyNode node = (MyNode) myRoot.getChildAt(i);
      result.add((PerforceJob) node.getConfigurable().getEditableObject());
    }
    return result;
  }

  @Override
  protected Dimension getPanelPrefferedSize() {
    return new Dimension(400, 200);
  }

  private void init() {
    myTree.setCellRenderer(new PerforceJobCellRenderer());
    myTree.setShowsRootHandles(false);
    myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    myTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        if (myListSelectionListener != null) {
          myListSelectionListener.valueChanged(null);
        }
      }
    });
  }

  public void fillTree(final List<PerforceJob> jobs, final PerforceJob selectJob) {
    myRoot.removeAllChildren();

    int idx = -1;
    for (int i = 0; i < jobs.size(); i++) {
      final PerforceJob job = jobs.get(i);
      final MyConfigurable configurable = new MyConfigurable(myProject, job);
      myRoot.add(new MyNode(configurable));
      if (selectJob != null && job.getName().equals(selectJob.getName())) {
        idx = i;
      }
    }

    ((DefaultTreeModel) myTree.getModel()).reload(myRoot);
    if (idx >= 0) {
      myTree.setSelectionRow(idx);
    }
  }

  private static class PerforceJobCellRenderer implements TreeCellRenderer {
    private final ColoredTreeCellRenderer myTop;
    private final ColoredTreeCellRenderer myBottom;
    private final JPanel myPanel;

    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
      if (! (value instanceof MyNode)) return myPanel;
      /*final GridBagConstraints gb = DefaultGb.create();
      gb.insets = new Insets(0, 0,0,0);

      myPanel.add(myTop.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus), gb);
      //gb.insets = new Insets(1,1,1,1);
      ++ gb.gridy;
      myPanel.add(myBottom.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus), gb);
      return myPanel;*/

      return myTop.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

    private PerforceJobCellRenderer() {
      myPanel = new JPanel(new GridBagLayout());
      myPanel.setBackground(Color.white);

      myTop = new ColoredTreeCellRenderer() {
        @Override
        public void customizeCellRenderer(JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
          if (! (value instanceof MyRootNode)) {
            final MyNode node = (MyNode)value;
            final MyConfigurable configurable = (MyConfigurable) node.getConfigurable();
            final PerforceJob job = configurable.getEditableObject();

            append(job.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            append(" <" + job.getValueForStandardField(StandardJobFields.status.getFixedCode()).getValue() + ">",
                   SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
            String text = job.getValueForStandardField(StandardJobFields.description.getFixedCode()).getValue();
            text = (text == null) ? "" : (text.length() > 30) ? text.substring(0, 30) + "..." : text;
            text = " " + text;
            append(text, SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
          }
        }
      };
      myBottom = new ColoredTreeCellRenderer() {
        @Override
        public void customizeCellRenderer(JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
          if (! (value instanceof MyRootNode)) {
            final MyNode node = (MyNode)value;
            final MyConfigurable configurable = (MyConfigurable) node.getConfigurable();
            final PerforceJob job = configurable.getEditableObject();

            String text = job.getValueForStandardField(StandardJobFields.description.getFixedCode()).getValue();
            text = (text == null) ? "" : (text.length() > 30) ? text.substring(0, 30) + "..." : text;
            append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
          }
        }
      };

    }

  }

  private static class MyConfigurable extends NamedConfigurable<PerforceJob> {
    private final SelfLoadingJobDetailsPanel mySelfLoadingJobDetailsPanel;
    private final PerforceJob myJob;

    private MyConfigurable(final Project project, final PerforceJob job) {
      mySelfLoadingJobDetailsPanel = new SelfLoadingJobDetailsPanel(project, job);
      myJob = job;
    }

    public void setDisplayName(String name) {
    }

    public PerforceJob getEditableObject() {
      return myJob;
    }

    public String getBannerSlogan() {
      return myJob.getName();
    }

    public JComponent createOptionsPanel() {
      return mySelfLoadingJobDetailsPanel.getPanel();
    }

    @Nls
    public String getDisplayName() {
      return myJob.getName();
    }

    public Icon getIcon() {
      return null;
    }

    public String getHelpTopic() {
      return null;
    }

    public boolean isModified() {
      return false;
    }

    public void apply() throws ConfigurationException {
    }

    public void reset() {
    }

    public void disposeUIResources() {
    }
  }
}
