package com.intellij.spring.refactoring;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.impl.SpringModelImpl;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class ConfigFileChooser extends DialogWrapper {

  private SimpleTree myTree;
  private JPanel myPanel;

  public ConfigFileChooser(Project project, final PsiFile fileToIgnore) {

    super(project, false);
    setTitle(SpringBundle.message("choose.configuration.file"));

    final List<DomFileElement<Beans>> list = DomService.getInstance().getFileElements(Beans.class,
                                                                                      project, GlobalSearchScope.allScope(project));
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    final List<SpringModel> springModels = new ArrayList<SpringModel>();
    for (Module module : modules) {
      springModels.addAll(SpringManager.getInstance(project).getAllModels(module));
    }
    final SimpleTreeStructure structure = new SimpleTreeStructure() {
      public Object getRootElement() {
        return new SimpleNode() {
          public SimpleNode[] getChildren() {
            final ArrayList<XmlFile> files = new ArrayList<XmlFile>(list.size());
            for (DomFileElement<Beans> element : list) {
              files.add(element.getFile());
            }
            if (fileToIgnore instanceof XmlFile) {
              files.remove((XmlFile)fileToIgnore);
            }
            final List<SimpleNode> nodes = new ArrayList<SimpleNode>();
            for (SpringModel springModel : springModels) {
              nodes.add(new FileSetNode(springModel));
              for (XmlFile file: springModel.getConfigFiles()) {
                files.remove(file);
              }
            }
            for (XmlFile file : files) {
              try {
                final VirtualFile vFile = file.getVirtualFile();
                if (vFile != null && (vFile.getPath().indexOf(JarFileSystem.JAR_SEPARATOR) < 0 || JarFileSystem.getInstance().getJarFile(vFile) == null)) {
                  nodes.add(new ConfigFileNode(file));
                }
              }
              catch (IOException e) {
                // just ignore it
              }
            }
            return nodes.toArray(new SimpleNode[nodes.size()]);
          }

          public boolean isAutoExpandNode() {
            return true;
          }
        };
      }
    };
    myTree.setRootVisible(false);
    final SimpleTreeBuilder builder = new SimpleTreeBuilder(myTree, (DefaultTreeModel)myTree.getModel(), structure, null);
    builder.initRoot();

    init();
    
    builder.updateFromRoot();


    myTree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2 && getSelectedFile() != null) {
          doOKAction();
        }
      }
    });

    myTree.requestFocus();
  }

  @Nullable
  public XmlFile getSelectedFile() {
    final SimpleNode simpleNode = myTree.getSelectedNode();
    return simpleNode instanceof ConfigFileNode ? ((ConfigFileNode)simpleNode).myFile : null;
  }

  public boolean isOKActionEnabled() {
    return getSelectedFile() != null;
  }

  protected JComponent createCenterPanel() {
    return myPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    return myTree;
  }

  protected String getDimensionServiceKey() {
    return "spring config file chooser";
  }

  private static class FileSetNode extends SimpleNode {

    private final SpringModel myModel;

    private FileSetNode(SpringModel model) {
      myModel = model;
      if (model instanceof SpringModelImpl && ((SpringModelImpl)model).getFileSet() != null) {
        final SpringFileSet springFileSet = ((SpringModelImpl)model).getFileSet();
        setPlainText(springFileSet.getName());
        setUniformIcon(springFileSet.getIcon());
      } else {
        setPlainText(model.getId());
        setUniformIcon(SpringIcons.FILESET);
      }
    }

    public boolean isAutoExpandNode() {
      return true;
    }

    public SimpleNode[] getChildren() {
      final Set<XmlFile> files = myModel.getConfigFiles();
      return ContainerUtil.map2Array(files, SimpleNode.class, new Function<XmlFile, SimpleNode>() {
        public SimpleNode fun(final XmlFile xmlFile) {
          return new ConfigFileNode(xmlFile);
        }
      });
    }
  }

  private static class ConfigFileNode extends SimpleNode {
    private final XmlFile myFile;

    private ConfigFileNode(XmlFile file) {
      myFile = file;
      setUniformIcon(file.getIcon(0));
      addColoredFragment(myFile.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      final VirtualFile virtualFile = myFile.getVirtualFile();
      assert virtualFile != null;
      addColoredFragment(" (" + virtualFile.getPath() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
    }

    public SimpleNode[] getChildren() {
      return new SimpleNode[0];
    }
  }
}
