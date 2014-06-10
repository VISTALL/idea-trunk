package com.intellij.spring.model.actions.generate;

import com.intellij.openapi.editor.Editor;
import com.intellij.spring.model.xml.beans.Alias;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementNavigationProvider;
import org.jetbrains.annotations.Nullable;

public class SpringAliasGenerateProvider extends BasicSpringDomGenerateProvider<Alias> {
  public SpringAliasGenerateProvider() {
    super(getDescription(Alias.class), Alias.class);
  }

  public Alias generate(@Nullable final DomElement parent, final Editor editor) {
    final Alias alias = super.generate(parent, editor);

    if (alias != null) {
      alias.getAlias().ensureXmlElementExists();
      alias.getAliasedBean().ensureXmlElementExists();
    }

    return alias;
  }

  protected DomElement getElementToNavigate(final Alias alias) {
    return alias.getAliasedBean();
  }

  protected void doNavigate(final DomElementNavigationProvider navigateProvider, final DomElement element) {
    navigateProvider.navigate(((Alias)element).getAliasedBean(), true);
  }
}
