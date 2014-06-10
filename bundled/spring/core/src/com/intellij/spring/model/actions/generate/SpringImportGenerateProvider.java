package com.intellij.spring.model.actions.generate;

import com.intellij.openapi.editor.Editor;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.xml.beans.SpringImport;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.Nullable;

public class SpringImportGenerateProvider extends BasicSpringDomGenerateProvider<SpringImport> {
  public SpringImportGenerateProvider() {
    super(SpringBundle.message("spring.import"), SpringImport.class);
  }

  public SpringImport generate(@Nullable final DomElement parent, final Editor editor) {
    final SpringImport springImport = super.generate(parent, editor);

    if (springImport != null) {
      springImport.getResource().ensureXmlElementExists();
    }

    return springImport;
  }

  protected DomElement getElementToNavigate(final SpringImport springImport) {
    return springImport.getResource();
  }
}
