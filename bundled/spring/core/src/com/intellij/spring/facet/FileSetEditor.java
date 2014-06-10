package com.intellij.spring.facet;

import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.spring.SpringBundle;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.EditorTextField;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.ui.tree.TreeModelAdapter;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class FileSetEditor extends DialogWrapper {

  public static final DefaultListCellRenderer FILESET_RENDERER = new DefaultListCellRenderer() {
    public Component getListCellRendererComponent(final JList list,
                                                  Object value,
                                                  final int index, final boolean isSelected, final boolean cellHasFocus) {
      if (value == null) {
        value = SpringBundle.message("fileset.none");
      } else if (((SpringFileSet)value).isNew()) {
        value = SpringBundle.message("fileset.new");
      }

      return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
  };

  private JPanel myMainPanel;
  private EditorTextField mySetName;
  private SpringFilesTree myFilesTree;
  private JComboBox myParentBox;

  private final SpringFileSet myFileSet;
  private final CheckedTreeNode myRoot = new CheckedTreeNode(null);
  private final SpringFileSet myOriginalSet;

  public FileSetEditor(final @NotNull Module module, final SpringFileSet fileSet, final Set<SpringFileSet> allSets) {
    super(module.getProject(), true);

    myOriginalSet = fileSet;
    myFileSet = new SpringFileSet(fileSet);

    init(fileSet, allSets, new SpringConfigsSearcher(module), module.getProject());
  }

  protected FileSetEditor(final Component parent,
                          final SpringFileSet fileSet,
                          final Collection<SpringFileSet> allSets,
                          final SpringConfigsSearcher searcher,
                          final Project project) {

    super(parent, true);

    myOriginalSet = fileSet;
    myFileSet = new SpringFileSet(fileSet);

    init(fileSet, allSets, searcher, project);
  }

  private void init(final SpringFileSet fileSet,
                    final Collection<SpringFileSet> allSets,
                    final SpringConfigsSearcher searcher,
                    @Nullable final Project project) {

    setTitle(SpringBundle.message("config.fileset.editor.title"));
    myFilesTree.setModel(new DefaultTreeModel(myRoot));
    searcher.search();
    final MultiMap<Module,PsiFile> files = searcher.getFilesByModules();
    final MultiMap<VirtualFile, PsiFile> jars = searcher.getJars();
    final Set<PsiFile> psiFiles = myFilesTree.buildModuleNodes(files, jars, fileSet);
    final List<VirtualFile> virtualFiles = searcher.getVirtualFiles();

    for (VirtualFile virtualFile : virtualFiles) {
      myFilesTree.addFile(virtualFile);
    }

    if (project != null) {
      final PsiManager psiManager = PsiManager.getInstance(project);
      final List<VirtualFilePointer> list = fileSet.getFiles();
      for (VirtualFilePointer pointer: list) {
        final VirtualFile file = pointer.getFile();
        if (file != null) {
          final PsiFile psiFile = psiManager.findFile(file);
          if (psiFile != null && psiFiles.contains(psiFile)) {
            continue;
          }
          myFilesTree.addFile(file);
        }
      }
    }

    TreeUtil.expandAll(myFilesTree);
    myFilesTree.getModel().addTreeModelListener(new TreeModelAdapter() {
      public void treeNodesChanged(final TreeModelEvent e) {
        updateFileSet();
      }
    });

    mySetName.setText(fileSet.getName());
    mySetName.addDocumentListener(new DocumentAdapter() {
      public void documentChanged(final DocumentEvent e) {
        updateFileSet();
      }
    });

    for (SpringFileSet set: allSets) {
      if (set.getId().equals(fileSet.getId())) {
        continue;
      }
      myParentBox.addItem(set);
    }
    myParentBox.addItem(null);
    myParentBox.setSelectedItem(myFileSet.getDependencies().size() > 0 ? myFileSet.getDependencies().get(0) : null);

    myParentBox.addItemListener(new ItemListener() {
      public void itemStateChanged(final ItemEvent e) {
        updateFileSet();
      }
    });
    myParentBox.setRenderer(FILESET_RENDERER);
    init();

    getOKAction().setEnabled(fileSet.isNew());
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @NonNls
  protected String getDimensionServiceKey() {
    return "spring file set editor";
  }

  public boolean isOKActionEnabled() {
    if (myOriginalSet.isNew()) {
      return true;
    }

    if (myFileSet.getFiles().size() != myOriginalSet.getFiles().size()) {
      return true;
    }
    final List<VirtualFilePointer> pointers = myFileSet.getFiles();
    for (int i = 0; i < pointers.size(); i++) {
      if (!pointers.get(i).getUrl().equals(myOriginalSet.getFiles().get(i).getUrl())) {
        return true;
      }
    }
    final boolean b = myFileSet.getDependencies().equals(myOriginalSet.getDependencies());
    return !myFileSet.getName().equals(myOriginalSet.getName()) ||
           !b;
  }

  protected void doOKAction() {
    updateFileSet();
    super.doOKAction();
  }

  private void updateFileSet() {
    myFileSet.setName(mySetName.getText());
    myFilesTree.updateFileSet(myFileSet);
    SpringFileSet parent = (SpringFileSet)myParentBox.getSelectedItem();
    myFileSet.setDependencies(parent == null ? Collections.<String>emptyList() : Arrays.asList(parent.getId()));
    getOKAction().setEnabled(isOKActionEnabled());
  }

  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  protected Action[] createLeftSideActions() {
    final AbstractAction locateAction = new AbstractAction(SpringBundle.message("config.locate.button")) {
      public void actionPerformed(final ActionEvent e) {
        final VirtualFile[] files = FileChooser.chooseFiles(myMainPanel, new FileChooserDescriptor(true, false, true, false, true, true));
        if (files.length > 0) {
          for (VirtualFile file: files) {
            myFilesTree.addFile(file);
          }
          updateFileSet();
          TreeUtil.expandAll(myFilesTree);
        }
      }
    };
    return new Action[] { locateAction };
  }

  public SpringFileSet getEditedFileSet() {
    return myFileSet;
  }
}
