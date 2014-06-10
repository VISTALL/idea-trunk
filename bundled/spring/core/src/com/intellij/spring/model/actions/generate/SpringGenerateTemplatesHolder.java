package com.intellij.spring.model.actions.generate;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Factory;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class SpringGenerateTemplatesHolder extends ArrayList<Pair<PsiElement, Factory<Template>>> {
  private final Project myProject;

  public SpringGenerateTemplatesHolder(final Project project) {
    myProject = project;
  }

  public void addTemplateFactory(PsiElement element, Factory<Template> template) {
    add(new Pair<PsiElement, Factory<Template>>(element, template));
  }

  public void runTemplates() {
    final TemplateManager manager = TemplateManager.getInstance(myProject);


    runTemplates(manager, 0);

  }

  private void runTemplates(final TemplateManager manager, final int index) {
    if(index >= size()) return;
    new WriteCommandAction(myProject) {
      protected void run(final Result result) throws Throwable {
        Pair<PsiElement, Factory<Template>> pair = get(index);
        Editor editor = getEditor(pair.getFirst());
        if (editor != null) {
          PsiDocumentManager.getInstance(myProject).doPostponedOperationsAndUnblockDocument(editor.getDocument());
          final Factory<Template> factory = pair.getSecond();
          if (factory != null) {
            final Template template = factory.create();
            if (template != null) {
              manager.startTemplate(editor, template, new TemplateEditingAdapter() {
                public void templateFinished(Template template) {
                  if (index + 1 < size()) {
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                      public void run() {
                        runTemplates(manager, index + 1);
                      }
                    });
                  }
                }
              });
            }
          }
        }
      }
    }.execute();
  }

  @Nullable
  private Editor getEditor(final PsiElement element) {
    final PsiFile psiFile = element.getContainingFile();
    if (psiFile != null) {
      final VirtualFile virtualFile = psiFile.getVirtualFile();
      if (virtualFile != null) {
        TextRange range = element.getTextRange();
        int textOffset = range.getStartOffset();

        OpenFileDescriptor descriptor = new OpenFileDescriptor(myProject, virtualFile, textOffset);
        return FileEditorManager.getInstance(myProject).openTextEditor(descriptor, true);
      }
    }
    return null;
  }
}

