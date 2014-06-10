package com.intellij.spring.model.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.xml.XmlTagTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.DomElementsNavigationManager;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SpringModelTreeElement implements StructureViewTreeElement, ItemPresentation {

  private XmlFile myXmlFile;
  private DomElementNavigationProvider myNavigationProvider;
  private boolean myShowBeanStructure;

  public SpringModelTreeElement(XmlFile xmlFile, final boolean showBeanStructure) {
    this(xmlFile, DomElementsNavigationManager.getManager(xmlFile.getProject()).getDomElementsNavigateProvider(
      DomElementsNavigationManager.DEFAULT_PROVIDER_NAME), showBeanStructure);
  }

  public SpringModelTreeElement(XmlFile xmlFile, final DomElementNavigationProvider navigationProvider, final boolean showBeanStructure) {
    myXmlFile = xmlFile;
    myNavigationProvider = navigationProvider;
    myShowBeanStructure = showBeanStructure;
  }

  public Object getValue() {
    return myXmlFile;
  }

  public ItemPresentation getPresentation() {
    return this;
  }

  public TreeElement[] getChildren() {
    List<StructureViewTreeElement> treeElements = new ArrayList<StructureViewTreeElement>();
    SpringModel springModel = getSpringModel();
    if (springModel != null) {
      for (SpringBaseBeanPointer pointer : springModel.getAllDomBeans()) {
        final CommonSpringBean springBean = pointer.getSpringBean();
        if (pointer.isValid() && springBean.isValid() && springBean instanceof DomSpringBean) {
          treeElements
            .add(new SpringBeanTreeElement((DomSpringBean)springBean, myNavigationProvider, myShowBeanStructure));
        }
      }
    }
    final DomManager manager = DomManager.getDomManager(myXmlFile.getProject());
    final XmlDocument document = myXmlFile.getDocument();
    if (document != null) {
      final XmlTag rootTag = document.getRootTag();
      if (rootTag != null) {
        for (final XmlTag tag : rootTag.getSubTags()) {
          final DomElement element = manager.getDomElement(tag);
          if (element instanceof CustomBeanWrapper && ((CustomBeanWrapper)element).isDummy()) {
            treeElements.add(new XmlTagTreeElement(tag));
          }
        }
      }
    }

    return treeElements.toArray(new TreeElement[treeElements.size()]);
  }

  @Nullable
  private SpringModel getSpringModel() {
    return SpringManager.getInstance(myXmlFile.getProject()).getLocalSpringModel(myXmlFile);
  }

  public void navigate(final boolean requestFocus) {
  }

  public boolean canNavigate() {
    return false;
  }

  public boolean canNavigateToSource() {
    return false;
  }

  public String getPresentableText() {
    return SpringBundle.message("spring.beans");
  }

  public String getLocationString() {
    return null;
  }

  public Icon getIcon(boolean open) {
    return SpringIcons.SPRING_BEANS_ICON;
  }

  public TextAttributesKey getTextAttributesKey() {
    return null;
  }
}
