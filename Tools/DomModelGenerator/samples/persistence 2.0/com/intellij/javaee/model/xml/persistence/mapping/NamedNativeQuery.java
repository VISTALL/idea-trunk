// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence/orm:named-native-query interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:named-native-query documentation</h3>
 * @Target({TYPE}) @Retention(RUNTIME)
 *         public @interface NamedNativeQuery {
 *           String name();
 *           String query();
 *           QueryHint[] hints() default {};
 *           Class resultClass() default void.class;
 *           String resultSetMapping() default ""; //named SqlResultSetMapping
 *         }
 * </pre>
 */
public interface NamedNativeQuery extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the result-class child.
	 * @return the value of the result-class child.
	 */
	@NotNull
	GenericAttributeValue<String> getResultClass();


	/**
	 * Returns the value of the result-set-mapping child.
	 * @return the value of the result-set-mapping child.
	 */
	@NotNull
	GenericAttributeValue<String> getResultSetMapping();


	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


	/**
	 * Returns the value of the query child.
	 * @return the value of the query child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getQuery();


	/**
	 * Returns the list of hint children.
	 * @return the list of hint children.
	 */
	@NotNull
	List<QueryHint> getHints();
	/**
	 * Adds new child to the list of hint children.
	 * @return created child
	 */
	QueryHint addHint();


}
