package org.jetbrains.plugins.groovy.mvc.projectView;

import com.intellij.ide.IconProvider;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dmitry Krasilschikov
 */
public class AbstractFolderNode extends AbstractMvcPsiNodeDescriptor {
  @Nullable private final String myLocationMark;

  protected AbstractFolderNode(@NotNull final Module module,
                               @NotNull final PsiDirectory directory,
                               @Nullable final String locationMark,
                               final ViewSettings viewSettings) {
    super(module, viewSettings, createId(directory, locationMark));
    myLocationMark = locationMark;
  }

  @Override
  protected String getTestPresentationImpl(@NotNull final NodeId nodeId, @NotNull final PsiElement psiElement) {
    final VirtualFile virtualFile = getVirtualFile();
    assert virtualFile != null;

    return "Folder: " + virtualFile.getPresentableName();
  }

  @NotNull
  protected PsiDirectory getPsiDirectory() {
    return (PsiDirectory)extractPsiFromValue();
  }

  @Nullable
  protected Collection<AbstractTreeNode> getChildrenImpl() {
    final List<AbstractTreeNode> children = new ArrayList<AbstractTreeNode>();

    // We should be sure that nobody will refresh VFS during this operation.
    // Or reimplement all nodes using only PSI files and directories.
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        final PsiDirectory directory = getPsiDirectory();
        if (!directory.isValid()) {
          return;
        }

        // scan folder's children
        for (PsiDirectory subDir : directory.getSubdirectories()) {
          children.add(new AbstractFolderNode(getModule(), subDir, myLocationMark, getSettings()) {
            @Override
            protected void processNotDirectoryFile(List<AbstractTreeNode> nodes, PsiFile file) {
              AbstractFolderNode.this.processNotDirectoryFile(nodes, file);
            }

            @Override
            protected AbstractTreeNode createClassNode(GrTypeDefinition typeDefinition) {
              return AbstractFolderNode.this.createClassNode(typeDefinition);
            }
          });
        }
        for (PsiFile file : directory.getFiles()) {
          processNotDirectoryFile(children, file);
        }
      }
    });

    return children;
  }

  @Override
  protected void updateImpl(final PresentationData data) {
    final PsiDirectory psiDirectory = getPsiDirectory();

    final VirtualFile virtualFile = psiDirectory.getVirtualFile();

    data.setPresentableText(virtualFile.getName());

    for (final IconProvider provider : Extensions.getExtensions(IconProvider.EXTENSION_POINT_NAME)) {
      final Icon openIcon = provider.getIcon(psiDirectory, Iconable.ICON_FLAG_OPEN);
      if (openIcon != null) {
        final Icon closedIcon = provider.getIcon(psiDirectory, Iconable.ICON_FLAG_CLOSED);
        if (closedIcon != null) {
          data.setOpenIcon(openIcon);
          data.setClosedIcon(closedIcon);
          return;
        }
      }
    }
  }

  @Override
  protected boolean containsImpl(@NotNull final VirtualFile file) {
    final PsiElement psiElement = extractPsiFromValue();
    if (psiElement == null || !psiElement.isValid()) {
      return false;
    }

    final VirtualFile valueFile = ((PsiDirectory)psiElement).getVirtualFile();
    if (!VfsUtil.isAncestor(valueFile, file, false)) {
      return false;
    }

    final Project project = psiElement.getProject();
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final Module module = fileIndex.getModuleForFile(valueFile);
    if (module == null) {
      return fileIndex.getModuleForFile(file) == null;
    }

    return ModuleRootManager.getInstance(module).getFileIndex().isInContent(file);
  }

  protected void processNotDirectoryFile(final List<AbstractTreeNode> nodes, final PsiFile file) {
    if (file instanceof GroovyFile) {
      final GrTypeDefinition[] definitions = ((GroovyFile)file).getTypeDefinitions();
      if (definitions.length > 0) {
        for (final GrTypeDefinition typeDefinition : definitions) {
          nodes.add(createClassNode(typeDefinition));
        }
        return;
      }
    }
    nodes.add(new FileNode(getModule(), file, myLocationMark, getSettings()));
  }

  protected AbstractTreeNode createClassNode(final GrTypeDefinition typeDefinition) {
    final NodeId nodeId = getValue();
    assert nodeId != null;

    return new ClassNode(getModule(), typeDefinition, nodeId.getLocationRootMark(), getSettings());
  }

  @Override
  public boolean shouldDrillDownOnEmptyElement() {
    return true; //TODO what is it?
  }

  private static NodeId createId(final PsiDirectory directory, final String locationMark) {
    return new NodeId(directory, locationMark);
  }

  @NotNull
  @Override
  public SortInfo getSortInformation() {
    return SortInfo.FOLDER;
  }
}
