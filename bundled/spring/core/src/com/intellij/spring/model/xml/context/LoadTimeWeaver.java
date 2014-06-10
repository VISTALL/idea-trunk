// Generated on Wed Oct 17 15:28:10 MSD 2007
// DTD/Schema  :    http://www.springframework.org/schema/context

package com.intellij.spring.model.xml.context;

import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/context:load-time-weaverElemType interface.
 */
public interface LoadTimeWeaver extends DomSpringBean, SpringContextElement {

  /**
   * Returns the value of the weaver-class child.
   * <pre>
   * <h3>Attribute null:weaver-class documentation</h3>
   * 	The fully-qualified classname of the LoadTimeWeaver that is to be activated.
   * <p/>
   * </pre>
   *
   * @return the value of the weaver-class child.
   */
  @NotNull
  GenericAttributeValue<String> getWeaverClass();


  /**
   * Returns the value of the aspectj-weaving child.
   *
   * @return the value of the aspectj-weaving child.
   */
  @NotNull
  GenericAttributeValue<AspectjWeaving> getAspectjWeaving();


}
