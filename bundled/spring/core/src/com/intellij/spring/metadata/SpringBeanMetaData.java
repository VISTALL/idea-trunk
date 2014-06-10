package com.intellij.spring.metadata;

import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.SpringBundle;
import com.intellij.util.xml.DomMetaData;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * User: Sergey.Vasiliev
*/
public class SpringBeanMetaData extends DomMetaData<DomSpringBean> {

  @Nullable
  protected GenericDomValue getNameElement(final DomSpringBean element) {
    final GenericAttributeValue<String> id = element.getId();
    if (DomUtil.hasXml(id)) {
      return id;
    }
    if (element instanceof SpringBean) {
      final GenericAttributeValue<List<String>> name = ((SpringBean)element).getName();
      if (DomUtil.hasXml(name)) {
        return name;
      }
    }
    return null;
  }

  public void setName(final String name) throws IncorrectOperationException {
    getElement().setName(name);
  }

  public String getTypeName() {
    return SpringBundle.message("spring.bean");
  }
}
