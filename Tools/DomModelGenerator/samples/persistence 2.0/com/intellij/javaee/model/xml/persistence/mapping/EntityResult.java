// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence/orm:entity-result interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:entity-result documentation</h3>
 * @Target({}) @Retention(RUNTIME)
 *         public @interface EntityResult {
 *           Class entityClass();
 *           FieldResult[] fields() default {};
 *           String discriminatorColumn() default "";
 *         }
 * </pre>
 */
public interface EntityResult extends JavaeeDomModelElement {

	/**
	 * Returns the value of the entity-class child.
	 * @return the value of the entity-class child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getEntityClass();


	/**
	 * Returns the value of the discriminator-column child.
	 * @return the value of the discriminator-column child.
	 */
	@NotNull
	GenericAttributeValue<String> getDiscriminatorColumn();


	/**
	 * Returns the list of field-result children.
	 * @return the list of field-result children.
	 */
	@NotNull
	List<FieldResult> getFieldResults();
	/**
	 * Adds new child to the list of field-result children.
	 * @return created child
	 */
	FieldResult addFieldResult();


}
