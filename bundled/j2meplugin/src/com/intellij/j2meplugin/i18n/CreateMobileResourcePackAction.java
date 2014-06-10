/*
 * Copyright (c) 2007, JetBrains c.r.o. All Rights Reserved.
 */

/*
 * User: anna
 * Date: 01-Feb-2007
 */
package com.intellij.j2meplugin.i18n;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.module.J2MEModuleType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CreateMobileResourcePackAction extends CreateElementActionBase {
  @NonNls public static final String MOBILE_RESOURCE_BUNDLE = "MobileResourceBundle";

  public CreateMobileResourcePackAction() {
    super(J2MEBundle.message("create.mobile.resource.bundle.action.text"),
          J2MEBundle.message("create.mobile.resource.bundle.action.text"),
          StdFileTypes.JAVA.getIcon());
  }


  @NotNull
  public PsiElement[] invokeDialog(Project project, PsiDirectory directory) {
    CreateElementActionBase.MyInputValidator validator = new CreateElementActionBase.MyInputValidator(project, directory);
    Messages.showInputDialog(project, IdeBundle.message("prompt.enter.new.class.name"),
                             IdeBundle.message("title.new.class"), Messages.getQuestionIcon(), "", validator);
    return validator.getCreatedElements();
  }

  protected void checkBeforeCreate(String newName, PsiDirectory directory) throws IncorrectOperationException {
    JavaDirectoryService.getInstance().checkCreateClass(directory, newName);
  }

  @NotNull
  protected PsiElement[] create(String newName, PsiDirectory directory) throws Exception {
    final PsiClass psiClass = JavaDirectoryService.getInstance().createClass(directory, newName, MOBILE_RESOURCE_BUNDLE);
    final PsiFile propertiesFile = directory.createFile(newName + "." + StdFileTypes.PROPERTIES.getDefaultExtension());
    final Module module = ModuleUtil.findModuleForPsiElement(directory);
    if (module != null) {
      /*final ResourceBundleManager[] managers = module.getProject().getExtensions(ResourceBundleManager.RESOURCE_BUNDLE_MANAGER);
      for (ResourceBundleManager manager : managers) {
        if (manager instanceof MobileResourceBundleManager) {
          ((MobileResourceBundleManager)manager).registerResourceBundle(psiClass);
          break;
        }
      }*/
      ResourceBeansContainer.getInstance(module.getProject()).registerResourceBundle(psiClass);
    }
    return new PsiElement[]{psiClass, propertiesFile};
  }

  protected String getErrorTitle() {
    return J2MEBundle.message("cannot.create.mobile.resource.bundle.error.title");
  }

  protected String getCommandName() {
    return J2MEBundle.message("create.mobile.resource.bundle.action.name");
  }

  protected String getActionName(PsiDirectory directory, String newName) {
    return J2MEBundle.message("create.mobile.resource.bundle.action.name");
  }

  public void update(final AnActionEvent e) {
    super.update(e);
    final Module module = LangDataKeys.MODULE.getData(e.getDataContext());
    if (module == null || !(module.getModuleType() instanceof J2MEModuleType)) {
      e.getPresentation().setVisible(false);
    }
  }
}