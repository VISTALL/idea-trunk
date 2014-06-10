// Generated on Wed Oct 17 15:28:10 MSD 2007
// DTD/Schema  :    http://www.springframework.org/schema/context

package com.intellij.spring.model.xml.context;

import com.intellij.spring.model.values.converters.ResourceValueConverter;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/context:property-placeholderElemType interface.
 */
public interface PropertyPlaceholder extends DomSpringBean, SpringContextElement {


    @NotNull
    @Required
    @Referencing(value = ResourceValueConverter.class)  
    GenericAttributeValue<String> getLocation();
}
