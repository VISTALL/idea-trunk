/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.highlighting;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomFileElement;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
*/
public class EnableAspectJQuickFix implements LocalQuickFix {
  private static final Logger LOG = Logger.getInstance("#com.intellij.spring.model.highlighting.EnableAspectJQuickFix");
  private final SpringModel myModel;

  public EnableAspectJQuickFix(final SpringModel beans) {
    myModel = beans;
  }

  @NotNull
  public String getName() {
    return SpringBundle.message("aop.enable.aspectj.fix.text");
  }

  @NotNull
  public String getFamilyName() {
    return getName();
  }

  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    final DomFileElement<Beans> fileElement = myModel.getRoots().get(0);
    if (ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(fileElement.getFile().getVirtualFile()).hasReadonlyFiles()) return;

    final XmlTag root = fileElement.getRootElement().ensureTagExists();
    try {
      if (isSchemaStyle(root)) {
        addAspectjAutoproxy(root);
      } else {
        final XmlTag childTag = root.createChildTag("bean", root.getNamespace(), "", false);
        childTag.setAttribute("class", SpringConstants.ASPECTJ_AUTOPROXY_BEAN_CLASS);
        root.add(childTag);
      }
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }

  public static boolean isSchemaStyle(@Nullable final XmlTag root) {
    return root != null && root.getNamespace().contains("http://www.springframework.org/schema/");
  }

  public static void addAspectjAutoproxy(final XmlTag root) {
    try {
      if (root.getPrefixByNamespace(SpringConstants.AOP_NAMESPACE) == null && root.getNamespaceByPrefix("aop") == XmlUtil.EMPTY_URI) {
        root.setAttribute("xmlns:aop", SpringConstants.AOP_NAMESPACE);
      }
      root.add(root.createChildTag(SpringConstants.ASPECTJ_AUTOPROXY, SpringConstants.AOP_NAMESPACE, "", false));
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }
}
