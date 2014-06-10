// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-mapping-3.0.dtd:database-object interface.
 * Type database-object documentation
 * <pre>
 *     Element for defining "auxiliary" database objects.  Must be one of two forms:
 *     #1 :
 *         <database-object>
 *             <definition class="CustomClassExtendingAuxiliaryObject"/>
 *         </database-object>
 *     #2 :
 *         <database-object>
 *             <create>CREATE OR REPLACE ....</create>
 *             <drop>DROP ....</drop>
 *         </database-object>
 * </pre>
 */
public interface HbmDatabaseObject extends CommonDomModelElement {

	/**
	 * Returns the list of dialect-scope children.
	 * Type dialect-scope documentation
	 * <pre>
	 *     dialect-scope element allows scoping auxiliary-objects to a particular
	 *     Hibernate dialect implementation.
	 * </pre>
	 * @return the list of dialect-scope children.
	 */
	@NotNull
	List<HbmDialectScope> getDialectScopes();
	/**
	 * Adds new child to the list of dialect-scope children.
	 * @return created child
	 */
	HbmDialectScope addDialectScope();


	/**
	 * Returns the value of the definition child.
	 * @return the value of the definition child.
	 */
	@NotNull
	HbmDefinition getDefinition();


	/**
	 * Returns the value of the create child.
	 * @return the value of the create child.
	 */
	@NotNull
	GenericDomValue<String> getCreate();


	/**
	 * Returns the value of the drop child.
	 * @return the value of the drop child.
	 */
	@NotNull
	GenericDomValue<String> getDrop();


}
