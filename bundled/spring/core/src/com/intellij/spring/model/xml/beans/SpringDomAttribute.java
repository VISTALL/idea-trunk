package com.intellij.spring.model.xml.beans;

import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.xml.QualifierAttribute;
import com.intellij.spring.model.xml.SpringModelElement;

/**
 * @author Dmitry Avdeev
 */
@Namespace(SpringConstants.BEANS_NAMESPACE_KEY)
public interface SpringDomAttribute extends QualifierAttribute, SpringModelElement {

  GenericDomValue<String> getKey();

  GenericDomValue<String> getValue();
}
