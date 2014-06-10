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
 * http://java.sun.com/xml/ns/persistence/orm:association-override interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:association-override documentation</h3>
 * @Target({TYPE, METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface AssociationOverride {
 *           String name();
 *           JoinColumn[] joinColumns() default{};
 * JoinTable joinTable() default @JoinTable;
 *         }
 * </pre>
 */
public interface AssociationOverride extends JavaeeDomModelElement {

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
	 * Returns the list of join-column children.
	 * @return the list of join-column children.
	 */
	@NotNull
	List<JoinColumn> getJoinColumns();
	/**
	 * Adds new child to the list of join-column children.
	 * @return created child
	 */
	JoinColumn addJoinColumn();


	/**
	 * Returns the value of the join-table child.
	 * @return the value of the join-table child.
	 */
	@NotNull
	JoinTable getJoinTable();


}
