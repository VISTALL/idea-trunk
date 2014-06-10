// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:sequence-generator interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:sequence-generator documentation</h3>
 * @Target({TYPE, METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface SequenceGenerator {
 *           String name();
 *           String sequenceName() default "";
 * String catalog() default "";
 * String schema() default "";
 *           int initialValue() default 1;
 *           int allocationSize() default 50;
 *         }
 * </pre>
 */
public interface SequenceGenerator extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the sequence-name child.
	 * @return the value of the sequence-name child.
	 */
	@NotNull
	GenericAttributeValue<String> getSequenceName();


	/**
	 * Returns the value of the catalog child.
	 * @return the value of the catalog child.
	 */
	@NotNull
	GenericAttributeValue<String> getCatalog();


	/**
	 * Returns the value of the schema child.
	 * @return the value of the schema child.
	 */
	@NotNull
	GenericAttributeValue<String> getSchema();


	/**
	 * Returns the value of the initial-value child.
	 * @return the value of the initial-value child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getInitialValue();


	/**
	 * Returns the value of the allocation-size child.
	 * @return the value of the allocation-size child.
	 */
	@NotNull
	GenericAttributeValue<Integer> getAllocationSize();


	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


}
