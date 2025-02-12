/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jetbrains.android.actions;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase;
import com.intellij.codeInsight.completion.CompletionProgressIndicator;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.impl.CompletionServiceImpl;
import com.intellij.codeInsight.lookup.LookupAdapter;
import com.intellij.codeInsight.lookup.LookupEvent;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.xml.refactoring.XmlTagInplaceRenamer;
import org.jetbrains.android.AndroidFileTemplateProvider;
import org.jetbrains.android.resourceManagers.ResourceManager;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 20, 2009
 * Time: 9:36:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateTypedResourceFileAction extends CreateElementActionBase {
  private static final String ROOT_TAG_PROPERTY = "ROOT_TAG";

  private final String myResourceType;
  private final String myResourcePresentableName;
  private final String myDefaultRootTag;
  private final boolean myValuesResourceFile;
  private final boolean myChooseTagName;

  public CreateTypedResourceFileAction(@NotNull String presentableName, @NotNull String resourceType, @NotNull String defaultRootTag) {
    this(presentableName, resourceType, defaultRootTag, false, true);
  }

  public CreateTypedResourceFileAction(@NotNull String resourcePresentableName,
                                       @NotNull String resourceType,
                                       @NotNull String defaultRootTag,
                                       boolean valuesResourceFile,
                                       boolean chooseTagName) {
    super(AndroidBundle.message("new.typed.resource.action.title", resourcePresentableName),
          AndroidBundle.message("new.typed.resource.action.description", resourcePresentableName), StdFileTypes.XML.getIcon());
    myResourceType = resourceType;
    myResourcePresentableName = resourcePresentableName;
    myDefaultRootTag = defaultRootTag;
    myValuesResourceFile = valuesResourceFile;
    myChooseTagName = chooseTagName;
  }

  public String getResourceType() {
    return myResourceType;
  }

  @NotNull
  @Override
  protected PsiElement[] invokeDialog(Project project, PsiDirectory directory) {
    MyInputValidator validator = new MyInputValidator(project, directory);
    Messages.showInputDialog(project, AndroidBundle.message("new.file.dialog.text"), getCommandName(), Messages.getQuestionIcon(), "", validator);
    return validator.getCreatedElements();
  }

  @Override
  protected void checkBeforeCreate(String name, PsiDirectory directory) throws IncorrectOperationException {
    directory.checkCreateFile(AndroidFileTemplateProvider.getFileNameByNewElementName(name));
  }

  @NotNull
  @Override
  protected PsiElement[] create(String newName, PsiDirectory directory) throws Exception {
    FileTemplateManager manager = FileTemplateManager.getInstance();
    String templateName = myValuesResourceFile ? AndroidFileTemplateProvider.VALUE_RESOURCE_FILE_TEMPLATE : AndroidFileTemplateProvider.RESOURCE_FILE_TEMPLATE;
    FileTemplate template = manager.getJ2eeTemplate(templateName);
    Properties properties = new Properties();
    if (!myValuesResourceFile) {
      properties.setProperty(ROOT_TAG_PROPERTY, myDefaultRootTag);
    }
    PsiElement createdElement = FileTemplateUtil.createFromTemplate(template, newName, properties, directory);
    assert createdElement instanceof XmlFile;
    final XmlFile file = (XmlFile)createdElement;
    PsiNavigateUtil.navigate(file);
    if (myChooseTagName) {
      XmlDocument document = file.getDocument();
      if (document != null) {
        XmlTag rootTag = document.getRootTag();
        if (rootTag != null) {
          final Project project = file.getProject();
          final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
          if (editor != null) {
            CaretModel caretModel = editor.getCaretModel();
            caretModel.moveToOffset(rootTag.getTextOffset() + 1);
            XmlTagInplaceRenamer.rename(editor, rootTag);
            ApplicationManager.getApplication().invokeLater(new Runnable() {
              public void run() {
                invokeCompletion(project, editor, file);
              }
            });
          }
        }
      }
    }
    return new PsiElement[]{createdElement};
  }

  private void invokeCompletion(Project project, final Editor editor, XmlFile file) {
    new CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(project, editor, file, 1);
    CompletionProgressIndicator indicator = CompletionServiceImpl.getCompletionService().getCurrentCompletion();
    if (indicator != null) {
      final LookupImpl lookup = indicator.getLookup();
      lookup.setAdditionalPrefix(myDefaultRootTag);
      lookup.addLookupListener(new LookupAdapter() {
        boolean prefixCanceled;

        @Override
        public void itemSelected(LookupEvent event) {
          TemplateManagerImpl.getTemplateState(editor).gotoEnd();
        }

        @Override
        public void currentItemChanged(LookupEvent event) {
          if (!prefixCanceled) {
            prefixCanceled = true;
            lookup.setAdditionalPrefix("");
          }
        }

        @Override
        public void lookupCanceled(LookupEvent event) {
        }
      });
    }
  }

  @Override
  protected boolean isAvailable(DataContext context) {
    if (!super.isAvailable(context)) return false;
    PsiElement element = (PsiElement)context.getData(DataKeys.PSI_ELEMENT.getName());
    if (element instanceof PsiDirectory) {
      return ResourceManager.isResourceSubdirectory((PsiDirectory)element, myResourceType);
    }
    return false;
  }

  @Override
  protected String getErrorTitle() {
    return CommonBundle.getErrorTitle();
  }

  @Override
  protected String getCommandName() {
    return AndroidBundle.message("new.typed.resource.command.name", myResourceType);
  }

  @Nullable
  @Override
  protected String getActionName(PsiDirectory directory, String newName) {
    return null;
  }

  @Override
  public String toString() {
    return myResourcePresentableName;
  }
}
