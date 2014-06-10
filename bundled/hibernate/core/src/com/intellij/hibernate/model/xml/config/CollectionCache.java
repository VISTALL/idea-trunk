// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-configuration-3.0.dtd

package com.intellij.hibernate.model.xml.config;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.hibernate.model.enums.CacheUsageType;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import org.jetbrains.annotations.NotNull;

/**
 * hibernate-configuration-3.0.dtd:collection-cache interface.
 */
public interface CollectionCache extends CommonDomModelElement {

	/**
	 * Returns the value of the usage child.
	 * Attribute usage
	 * @return the value of the usage child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<CacheUsageType> getUsage();


	/**
	 * Returns the value of the region child.
	 * Attribute region
	 * @return the value of the region child.
	 */
	@NotNull
	GenericAttributeValue<String> getRegion();


	/**
	 * Returns the value of the collection child.
	 * Attribute collection
	 * @return the value of the collection child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getCollection();


}
