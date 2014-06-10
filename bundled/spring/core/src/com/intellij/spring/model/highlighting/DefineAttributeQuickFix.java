/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.highlighting;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringBundle;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class DefineAttributeQuickFix implements LocalQuickFix {
  private static final Logger LOG = Logger.getInstance("#com.intellij.spring.model.highlighting.DefineAttributeQuickFix");
  private final String myAttrName;

  public DefineAttributeQuickFix(@NonNls final String attrName) {
    myAttrName = attrName;
  }

  @NotNull
  public String getName() {
    return getFamilyName();
  }

  @NotNull
  public String getFamilyName() {
    return SpringBundle.message("aop.quickfix.define.0.attr", myAttrName);
  }

  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    try {
      final XmlTag tag = (XmlTag)descriptor.getPsiElement();
      if (ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(descriptor.getPsiElement().getContainingFile().getVirtualFile()).hasReadonlyFiles()) return;
      final XmlAttribute attribute = tag.setAttribute(myAttrName, "", "");
      new OpenFileDescriptor(project, tag.getContainingFile().getVirtualFile(), attribute.getValueElement().getTextRange().getStartOffset() + 1).navigate(true);
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
    }
  }
}
