package com.intellij.hibernate.model.converters;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gregory.Shrago
 */
public class SessionFactoryNameConverter extends Converter<String> {
  public String fromString(@Nullable @NonNls final String s, final ConvertContext context) {
    if (s != null) return s;
    final Project project = context.getFile().getProject();
    final VirtualFile vFile = context.getFile().getVirtualFile();
    final VirtualFile root = vFile == null? null : ProjectRootManager.getInstance(project).getFileIndex().getContentRootForFile(vFile);
    final String relativePath = root == null? null : VfsUtil.getRelativePath(vFile, root, '/');

    final String fileName = relativePath != null? relativePath : context.getFile().getName();
    return fileName;
    //final PersistencePackage unit = DomUtil.getParentOfType(context.getInvocationElement(), PersistencePackage.class, true);
    //assert unit != null;
    //final String typeName = ElementPresentationManager.getTypeNameForObject(unit);
    //return typeName + ": " + fileName;
  }

  public String toString(@Nullable final String s, final ConvertContext context) {
    return s;
  }
}
