package com.intellij.seam;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.patterns.DomPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.refactoring.rename.RenameInputValidator;
import com.intellij.refactoring.rename.RenameInputValidatorRegistry;
import com.intellij.seam.model.xml.pageflow.*;
import com.intellij.seam.resources.messages.PageflowBundle;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.TypeNameManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class PageflowApplicationComponent implements ApplicationComponent {

  @NonNls
  @NotNull
  public String getComponentName() {
    return getClass().getName();
  }

  public void initComponent() {
    registerPresentations();

    registerRenameValidators();
  }

  private static void registerPresentations() {
    TypeNameManager.registerTypeName(Page.class, PageflowBundle.message("pageflow.page"));
    TypeNameManager.registerTypeName(Decision.class, PageflowBundle.message("pageflow.decision"));
    TypeNameManager.registerTypeName(StartState.class, PageflowBundle.message("pageflow.start.state"));
    TypeNameManager.registerTypeName(EndState.class, PageflowBundle.message("pageflow.end.state"));
    TypeNameManager.registerTypeName(ProcessState.class, PageflowBundle.message("pageflow.process.state"));
  }

  private static void registerRenameValidators() {
    RenameInputValidatorRegistry.getInstance()
      .registerInputValidator(DomPatterns.domTargetElement(DomPatterns.domElement(PageflowNamedElement.class)), new RenameInputValidator() {
        public boolean isInputValid(final String newName, final PsiElement element, final ProcessingContext context) {
          final DomElement domElement = DomManager.getDomManager(element.getProject()).getDomElement((XmlTag)element);

          return (domElement instanceof PageflowNamedElement) && !(newName.contains("\""));
        }
      });
  }

  public void disposeComponent() {
  }

}