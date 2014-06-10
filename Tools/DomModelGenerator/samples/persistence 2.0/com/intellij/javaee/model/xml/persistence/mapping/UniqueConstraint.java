// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence/orm:unique-constraint interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:unique-constraint documentation</h3>
 * @Target({}) @Retention(RUNTIME)
 *         public @interface UniqueConstraint {
 * String name() default "";
 *           String[] columnNames();
 *         }
 * </pre>
 */
public interface UniqueConstraint extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	GenericDomValue<String> getName();


	/**
	 * Returns the list of column-name children.
	 * @return the list of column-name children.
	 */
	@NotNull
	@Required
	List<GenericDomValue<String>> getColumnNames();
	/**
	 * Adds new child to the list of column-name children.
	 * @return created child
	 */
	GenericDomValue<String> addColumnName();


}
