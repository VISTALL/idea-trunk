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
 * http://java.sun.com/xml/ns/persistence/orm:sql-result-set-mapping interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:sql-result-set-mapping documentation</h3>
 * @Target({TYPE}) @Retention(RUNTIME)
 *         public @interface SqlResultSetMapping {
 *           String name();
 *           EntityResult[] entities() default {};
 *           ColumnResult[] columns() default {};
 *         }
 * </pre>
 */
public interface SqlResultSetMapping extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


	/**
	 * Returns the list of entity-result children.
	 * @return the list of entity-result children.
	 */
	@NotNull
	List<EntityResult> getEntityResults();
	/**
	 * Adds new child to the list of entity-result children.
	 * @return created child
	 */
	EntityResult addEntityResult();


	/**
	 * Returns the list of column-result children.
	 * @return the list of column-result children.
	 */
	@NotNull
	List<ColumnResult> getColumnResults();
	/**
	 * Adds new child to the list of column-result children.
	 * @return created child
	 */
	ColumnResult addColumnResult();


}
