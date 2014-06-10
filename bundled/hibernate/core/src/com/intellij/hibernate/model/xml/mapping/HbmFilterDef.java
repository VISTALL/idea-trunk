// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.NameValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-mapping-3.0.dtd:filter-def interface.
 * Type filter-def documentation
 * <pre>
 * 	FILTER-DEF element; top-level filter definition.
 * </pre>
 */
public interface HbmFilterDef extends CommonDomModelElement {

	/**
	 * Returns the value of the simple content.
	 * @return the value of the simple content.
	 */
	@NotNull
	String getValue();
	/**
	 * Sets the value of the simple content.
	 * @param value the new value to set
	 */
	void setValue(String value);


	/**
	 * Returns the value of the name child.
	 * Attribute name
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
        @NameValue
        GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the condition child.
	 * Attribute condition
	 * @return the value of the condition child.
	 */
	@NotNull
	GenericAttributeValue<String> getCondition();


	/**
	 * Returns the list of filter-param children.
	 * Type filter-param documentation
	 * <pre>
	 * 	FILTER-PARAM element; qualifies parameters found within a FILTER-DEF
	 * 	condition.
	 * </pre>
	 * @return the list of filter-param children.
	 */
	@NotNull
        List<HbmFilterParam> getFilterParams();
	/**
	 * Adds new child to the list of filter-param children.
	 * @return created child
	 */
	HbmFilterParam addFilterParam();
}
