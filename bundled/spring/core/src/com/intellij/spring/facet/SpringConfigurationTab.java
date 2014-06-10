/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.facet;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.SpringManager;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class SpringConfigurationTab extends FacetEditorTab {
  
  private JPanel myMainPanel;
  private SimpleTree myTree;

  private JButton myAddSetButton;
  private JButton myRemoveButton;
  private JButton myEditButton;

  private final SpringFacetConfiguration myConfiguration;
  private final FacetEditorContext myContext;
  private final Set<SpringFileSet> myBuffer = new HashSet<SpringFileSet>();
  private final SimpleTreeBuilder myBuilder;
  private final SpringConfigsSearcher mySearcher;
  private boolean myModified;

  private final SimpleNode myRoot = new SimpleNode() {
    public SimpleNode[] getChildren() {
      List<SimpleNode> nodes = new ArrayList<SimpleNode>(myBuffer.size());
      for (SpringFileSet entry: myBuffer) {
        if (!entry.isRemoved()) {
          final FileSetNode setNode = new FileSetNode(entry);
          nodes.add(setNode);
        }
      }
      return nodes.toArray(new SimpleNode[nodes.size()]);
    }

    public boolean isAutoExpandNode() {
      return true;
    }

  };

  public SpringConfigurationTab(final SpringFacetConfiguration configuration, FacetEditorContext context) {

    myConfiguration = configuration;
    myContext = context;
    mySearcher = new SpringConfigsSearcher(context);

    final SimpleTreeStructure structure = new SimpleTreeStructure() {
      public Object getRootElement() {
        return myRoot;
      }
    };
    myTree.setRootVisible(false);
    myBuilder = new SimpleTreeBuilder(myTree, (DefaultTreeModel)myTree.getModel(), structure, null);
    myBuilder.initRoot();

    myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        final SpringFileSet fileSet = getCurrentFileSet();
        myEditButton.setEnabled(fileSet != null);
        myRemoveButton.setEnabled(fileSet != null);
      }
    });

    myAddSetButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final SpringFileSet fileSet = new SpringFileSet(SpringFileSet.getUniqueId(myBuffer),
                                                        SpringFileSet.getUniqueName(SpringBundle.message("default.fileset.name"), myBuffer),
                                                        myConfiguration) {
          public boolean isNew() {
            return true;
          }
        };

        final FileSetEditor editor = new FileSetEditor(myMainPanel, fileSet, myBuffer, mySearcher, myContext.getProject());
        editor.show();
        if (editor.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
          SpringFileSet editedFileSet = editor.getEditedFileSet();
          Disposer.register(configuration, editedFileSet);          
          myBuffer.add(editedFileSet);
          myModified = true;
          myBuilder.updateFromRoot();
          selectFileSet(fileSet);
        }
        myTree.requestFocus();        
      }
    });

    myRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        remove();
      }
    });

    myEditButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final SpringFileSet fileSet = getCurrentFileSet();
        if (fileSet != null) {
          final FileSetEditor editor = new FileSetEditor(myMainPanel, fileSet, myBuffer, mySearcher, myContext.getProject());
          editor.show();
          if (editor.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            myModified = true;
            myBuffer.remove(fileSet);
            final SpringFileSet edited = editor.getEditedFileSet();
            Disposer.register(configuration, edited);          
            myBuffer.add(edited);
            edited.setAutodetected(false);
            edited.setIcon(SpringIcons.FILESET);
            myBuilder.updateFromRoot();
            selectFileSet(edited);
          }
          myTree.requestFocus();
        }
      }
    });
  }

  private void remove() {
    final SimpleNode[] nodes = myTree.getSelectedNodesIfUniform();
    for (SimpleNode node : nodes) {

      if (node instanceof DependencyNode) {
        final SpringFileSet fileSet = ((FileSetNode)node.getParent()).mySet;
        fileSet.removeDependency(((DependencyNode)node).myFileSet.getId());
      } else if (node instanceof FileSetNode) {
        final SpringFileSet fileSet = ((FileSetNode)node).mySet;
        final int result = Messages.showYesNoDialog(myMainPanel,
                                                    SpringBundle.message("config.remove.button.message", fileSet.getName()),
                                                    SpringBundle.message("config.remove.button.title"),
                                                    Messages.getQuestionIcon());
        if (result == 0) {
          if (fileSet.isAutodetected()) {
            fileSet.setRemoved(true);
            myBuffer.add(fileSet);
          } else {
            myBuffer.remove(fileSet);
          }
          for (SpringFileSet set : myBuffer) {
            set.removeDependency(fileSet.getId());
          }
        }
      } else if (node instanceof ConfigFileNode) {
        final VirtualFilePointer filePointer = ((ConfigFileNode)node).myFilePointer;
        final SpringFileSet fileSet = ((FileSetNode)node.getParent()).mySet;
        fileSet.removeFile(filePointer);
      }
    }

    myModified = true;
    myBuilder.updateFromRoot();
    myTree.requestFocus();
  }

  @Nullable
  private SpringFileSet getCurrentFileSet() {
    final FileSetNode currentFileSetNode = getCurrentFileSetNode();
    return currentFileSetNode == null ? null : currentFileSetNode.mySet;
  }

  @Nullable
  private FileSetNode getCurrentFileSetNode() {
    final SimpleNode selectedNode = myTree.getSelectedNode();
    if (selectedNode == null) {
      return null;
    }
    if (selectedNode instanceof FileSetNode) {
      return (FileSetNode)selectedNode;
    } else if (selectedNode.getParent() instanceof FileSetNode) {
      return (FileSetNode)selectedNode.getParent();
    } else {
      final SimpleNode parent = selectedNode.getParent();
      if (parent != null && parent.getParent() instanceof FileSetNode) {
        return (FileSetNode)selectedNode.getParent().getParent();
      }
    }
    return null;
  }

  @Nls
  public String getDisplayName() {
    return SpringBundle.message("config.display.name");
  }

  public JComponent createComponent() {
    return myMainPanel;
  }

  public boolean isModified() {
    return myModified;
  }

  public void apply() throws ConfigurationException {
    final Set<SpringFileSet> fileSets = myConfiguration.getFileSets();
    fileSets.clear();
    for (SpringFileSet fileSet : myBuffer) {
      if (!fileSet.isAutodetected() || fileSet.isRemoved()) {
        fileSets.add(fileSet);
      }
    }
    myConfiguration.setModified();
  }

  public void reset() {
    myBuffer.clear();
    final Module module = myContext.getModule();
    final SpringFacet springFacet = (SpringFacet)myContext.getFacet();
    final Set<SpringFileSet> sets = SpringManager.getInstance(module.getProject()).getAllSets(springFacet);
    for (SpringFileSet fileSet : sets) {
      myBuffer.add(new SpringFileSet(fileSet));
    }

    myBuilder.updateFromRoot();

    myTree.setSelectionRow(0);
  }

  public void disposeUIResources() {
    Disposer.dispose(myBuilder);
  }

  private void selectFileSet(final SpringFileSet fileSet) {    
    myTree.select(myBuilder, new SimpleNodeVisitor() {
      public boolean accept(final SimpleNode simpleNode) {
        if (simpleNode instanceof FileSetNode) {
          if (((FileSetNode)simpleNode).mySet.equals(fileSet)) {
            return true;
          }
        }
        return false;
      }
    }, false);
  }

  private class FileSetNode extends SimpleNode {

    protected final SpringFileSet mySet;

    FileSetNode(SpringFileSet fileSet) {
      mySet = fileSet;

      final String name = mySet.getName();
      setPlainText(mySet.isAutodetected() ? name + " " + SpringBundle.message("config.fileset.autodetected") : name);
      setIcons(fileSet.getIcon(), fileSet.getIcon());
    }

    public SimpleNode[] getChildren() {
      final ArrayList<SimpleNode> nodes = new ArrayList<SimpleNode>();
      deps: for (String dep: mySet.getDependencies()) {
        final Module module = myContext.getModule();
        for (SpringFileSet fileSet : myBuffer) {
          if (fileSet.getId().equals(dep)) {
            nodes.add(new DependencyNode(fileSet, this));
            continue deps;
          }
        }
        final SpringFacet springFacet = SpringFacet.getInstance(module);
        assert springFacet != null;
        final List<SpringFileSet> models = SpringManager.getInstance(module.getProject()).getProvidedModels(springFacet);
        for (SpringFileSet fileSet : models) {
          if (fileSet.getId().equals(dep)) {
            nodes.add(new DependencyNode(fileSet, this));
            continue deps;
          }
        }
      }
      for (VirtualFilePointer file: mySet.getFiles()) {
        nodes.add(new ConfigFileNode(file, this));
      }
      return nodes.toArray(new SimpleNode[nodes.size()]);
    }

    public boolean isAutoExpandNode() {
      return true;
    }

    public Object[] getEqualityObjects() {
      return new Object[] { mySet, mySet.getName(), mySet.getFiles(), mySet.getDependencies() };
    }
  }

  private static class DependencyNode extends SimpleNode {
    private final SpringFileSet myFileSet;

    DependencyNode(SpringFileSet fileSet, SimpleNode parent) {
      super(parent);
      myFileSet = fileSet;
      setPlainText(fileSet.getName());
      setIcons(SpringIcons.DEPENDENCY, SpringIcons.DEPENDENCY);
    }

    public SimpleNode[] getChildren() {
      return NO_CHILDREN;
    }
  }

  private class ConfigFileNode extends SimpleNode {
    private final VirtualFilePointer myFilePointer;

    ConfigFileNode(VirtualFilePointer name, SimpleNode parent) {
      super(parent);
      myFilePointer = name;
      setIcons(SpringIcons.CONFIG_FILE, SpringIcons.CONFIG_FILE);
    }

    protected void doUpdate() {
      final VirtualFile file = myFilePointer.getFile();
      if (file != null) {
        final Project project = myContext.getProject();
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (!(psiFile instanceof XmlFile) || !SpringManager.getInstance(project).isSpringBeans((XmlFile)psiFile)) {
          renderFile(SimpleTextAttributes.ERROR_ATTRIBUTES,
                     SimpleTextAttributes.ERROR_ATTRIBUTES,
                     SpringBundle.message("config.file.is.not.spring"));
          return;
        }
        renderFile(SimpleTextAttributes.REGULAR_ATTRIBUTES,
                   SimpleTextAttributes.GRAYED_ATTRIBUTES,
                   null);
      } else {
        renderFile(SimpleTextAttributes.ERROR_ATTRIBUTES,
                   SimpleTextAttributes.ERROR_ATTRIBUTES,
                   SpringBundle.message("config.file.not.found"));
      }
    }

    private void renderFile(SimpleTextAttributes main, SimpleTextAttributes full, @Nullable String toolTip) {
      addColoredFragment(myFilePointer.getFileName(), toolTip, main);
      addColoredFragment(" (" + myFilePointer.getPresentableUrl() + ")", toolTip, full);
    }

    public SimpleNode[] getChildren() {
      return NO_CHILDREN;
    }
  }
}
