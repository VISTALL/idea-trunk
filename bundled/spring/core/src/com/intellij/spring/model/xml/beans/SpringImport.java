// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.psi.PsiFile;
import com.intellij.spring.model.converters.SpringImportResourceConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/beans:importElemType interface.
 */
public interface SpringImport extends DomElement {

  /**
   * Returns the value of the resource child.
   * <pre>
   * <h3>Attribute null:resource documentation</h3>
   * 	The relative resource location of the XML (bean definition) file to import,
   * 	for example "myImport.xml" or "includes/myImport.xml" or "../myImport.xml".
   * <p/>
   * </pre>
   *
   * @return the value of the resource child.
   */
  @NotNull
  @Required
  @Convert(SpringImportResourceConverter.class)
  GenericAttributeValue<PsiFile> getResource();
}
