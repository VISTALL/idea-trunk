// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-mapping-3.0.dtd:resultset interface.
 * Type resultset documentation
 * <pre>
 *  The resultset element declares a named resultset mapping definition for SQL queries 
 * </pre>
 */
public interface HbmResultset extends CommonDomModelElement {

	/**
	 * Returns the value of the name child.
	 * Attribute name
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the list of return-scalar children.
	 * @return the list of return-scalar children.
	 */
	@NotNull
	List<HbmReturnScalar> getReturnScalars();
	/**
	 * Adds new child to the list of return-scalar children.
	 * @return created child
	 */
	HbmReturnScalar addReturnScalar();


	/**
	 * Returns the list of return children.
	 * Type return documentation
	 * <pre>
	 * 	Defines a return component for a sql-query.  Alias refers to the alias
	 * 	used in the actual sql query; lock-mode specifies the locking to be applied
	 * 	when the query is executed.  The class, collection, and role attributes are mutually exclusive;
	 * 	class refers to the class name of a "root entity" in the object result; collection refers
	 * 	to a collection of a given class and is used to define custom sql to load that owned collection
	 * 	and takes the form "ClassName.propertyName"; role refers to the property path for an eager fetch
	 * 	and takes the form "owningAlias.propertyName"
	 * </pre>
	 * @return the list of return children.
	 */
	@NotNull
	List<HbmReturn> getReturns();
	/**
	 * Adds new child to the list of return children.
	 * @return created child
	 */
	HbmReturn addReturn();


	/**
	 * Returns the list of return-join children.
	 * @return the list of return-join children.
	 */
	@NotNull
	List<HbmReturnJoin> getReturnJoins();
	/**
	 * Adds new child to the list of return-join children.
	 * @return created child
	 */
	HbmReturnJoin addReturnJoin();


	/**
	 * Returns the list of load-collection children.
	 * @return the list of load-collection children.
	 */
	@NotNull
	List<HbmLoadCollection> getLoadCollections();
	/**
	 * Adds new child to the list of load-collection children.
	 * @return created child
	 */
	HbmLoadCollection addLoadCollection();


}
