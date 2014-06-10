// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.NamedQueryNameConverter;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.persistence.model.PersistenceQuery;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-mapping-3.0.dtd:query interface.
 * Type query documentation
 * <pre>
 *  The query element declares a named Hibernate query string 
 * </pre>
 */
public interface HbmQuery extends CommonDomModelElement, GenericDomValue<String>, PersistenceQuery {

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
        @PrimaryKey
        @NameValue
	@Required
        @Convert(NamedQueryNameConverter.class)    
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the cacheable child.
	 * Attribute cacheable
	 * @return the value of the cacheable child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getCacheable();


	/**
	 * Returns the value of the flush-mode child.
	 * Attribute flush-mode
	 * @return the value of the flush-mode child.
	 */
	@NotNull
	GenericAttributeValue<String> getFlushMode();


	/**
	 * Returns the value of the comment child.
	 * Attribute comment
	 * @return the value of the comment child.
	 */
	@NotNull
	GenericAttributeValue<String> getComment();


	/**
	 * Returns the value of the fetch-size child.
	 * Attribute fetch-size
	 * @return the value of the fetch-size child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getFetchSize();


	/**
	 * Returns the value of the read-only child.
	 * Attribute read-only
	 * @return the value of the read-only child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getReadOnly();


	/**
	 * Returns the value of the cache-mode child.
	 * Attribute cache-mode
	 * @return the value of the cache-mode child.
	 */
	@NotNull
	GenericAttributeValue<String> getCacheMode();


	/**
	 * Returns the value of the timeout child.
	 * Attribute timeout
	 * @return the value of the timeout child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getTimeout();


	/**
	 * Returns the value of the cache-region child.
	 * Attribute cache-region
	 * @return the value of the cache-region child.
	 */
	@NotNull
	GenericAttributeValue<String> getCacheRegion();


	/**
	 * Returns the list of query-param children.
	 * Type query-param documentation
	 * <pre>
	 *  The query-param element is used only by tools that generate
	 * finder methods for named queries
	 * </pre>
	 * @return the list of query-param children.
	 */
	@NotNull
        List<HbmQueryParam> getQueryParams();
	/**
	 * Adds new child to the list of query-param children.
	 * @return created child
	 */
	HbmQueryParam addQueryParam();
}
