package com.intellij.spring.model.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.model.xml.beans.ConstructorArg;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.spring.model.xml.beans.SpringValueHolderDefinition;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.Icons;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.DomElementsNavigationManager;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class SpringInjectionTreeElement implements StructureViewTreeElement, ItemPresentation {

  private final SpringValueHolderDefinition myInjection;
  @NonNls private static final String CONSTRUCTOR_ARG = "constructor-arg";
  @NonNls private static final String PROPERTY_TAG = "property";

  public SpringInjectionTreeElement(final @NotNull SpringValueHolderDefinition injection) {
    myInjection = injection;
  }

  public Object getValue() {
    return myInjection.getXmlElement();
  }

  public ItemPresentation getPresentation() {
    return this;
  }

  public String getPresentableText() {
    if (isConstructorArg()) {
      return CONSTRUCTOR_ARG;
    }
    else {
      final SpringProperty springProperty = (SpringProperty)myInjection;
      String propertyName = springProperty.getName().getStringValue();
      if (propertyName == null) propertyName = PROPERTY_TAG;
      final PsiType type = getPropertyType(springProperty);
      if (type != null) {
          propertyName += ": " + type.getCanonicalText();
      }
      return propertyName;
    }
  }

  @Nullable
  private static PsiType getPropertyType(SpringProperty property) {
    final List<BeanProperty> value = property.getName().getValue();
    if (value != null && !value.isEmpty()) {
      return value.get(0).getPropertyType();
    }
    else {
      PsiType[] psiTypes = property.getTypesByValue();
      return psiTypes == null || psiTypes.length == 0 ? null : psiTypes[0];
    }
 }

  public String getLocationString() {
    String value = getValueText();
    if (value != null) return "value=\"" + value + "\"";
    value = getRefText();
    if (value != null) return "ref=\"" + value + "\"";

    return null;
  }

  public Icon getIcon(boolean open) {
    return isConstructorArg() ? Icons.METHOD_ICON : SpringIcons.SPRING_BEAN_PROPERTY_ICON;
  }

  public TextAttributesKey getTextAttributesKey() {
    return null;
  }

  public TreeElement[] getChildren() {
    return new TreeElement[0];
  }

  public void navigate(final boolean requestFocus) {
    final DomElementNavigationProvider navigationProvider = DomElementsNavigationManager.getManager(myInjection.getManager().getProject())
      .getDomElementsNavigateProvider(DomElementsNavigationManager.DEFAULT_PROVIDER_NAME);

    navigationProvider.navigate(myInjection, requestFocus);
  }

  public boolean canNavigate() {
    return myInjection.isValid();
  }

  public boolean canNavigateToSource() {
    return  myInjection.isValid();
  }

  public boolean isConstructorArg() {
    return myInjection instanceof ConstructorArg;
  }

  @Nullable
  private String getValueText() {
    final GenericDomValue<?> element = myInjection.getValueElement();
    return element == null ? null : element.getStringValue();
  }
  @Nullable
  private String getRefText() {
    final GenericDomValue<SpringBeanPointer> refElement = myInjection.getRefElement();
    return refElement != null ? refElement.getStringValue() : null;
  }
}
