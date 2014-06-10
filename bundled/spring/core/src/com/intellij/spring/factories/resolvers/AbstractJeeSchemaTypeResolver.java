package com.intellij.spring.factories.resolvers;

import com.intellij.spring.factories.ObjectTypeResolver;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.jee.SpringJeeElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public abstract class AbstractJeeSchemaTypeResolver implements ObjectTypeResolver {
  private FactoryPropertiesDependentTypeResolver myPropertyDependentResolver;

  @NotNull
  public Set<String> getObjectType(@NotNull CommonSpringBean context) {
    if (context instanceof SpringJeeElement) {
      return getJeeObjectType(context);
    }
    else {
      return getPropertyDependentResolver().getObjectType(context);
    }
  }


  public boolean accept(@NotNull String factoryClassName) {
    return getFactoryClasses().contains(factoryClassName);
  }

  public FactoryPropertiesDependentTypeResolver getPropertyDependentResolver() {
    if (myPropertyDependentResolver == null) {
      myPropertyDependentResolver = new FactoryPropertiesDependentTypeResolver(getProperties());
    }
    return myPropertyDependentResolver;
  }

  protected abstract Set<String> getJeeObjectType(final CommonSpringBean context);

  protected abstract List<String> getProperties();

  protected abstract List<String> getFactoryClasses();

}
