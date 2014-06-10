// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:basic interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:basic documentation</h3>
 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
 *         public @interface Basic {
 *           FetchType fetch() default EAGER;
 *           boolean optional() default true;
 *         }
 * </pre>
 */
public interface Basic extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the fetch child.
	 * @return the value of the fetch child.
	 */
	@NotNull
	GenericAttributeValue<FetchType> getFetch();


	/**
	 * Returns the value of the optional child.
	 * @return the value of the optional child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getOptional();


	/**
	 * Returns the value of the access child.
	 * @return the value of the access child.
	 */
	@NotNull
	GenericAttributeValue<AccessType> getAccess();


	/**
	 * Returns the value of the column child.
	 * @return the value of the column child.
	 */
	@NotNull
	Column getColumn();


	/**
	 * Returns the value of the lob child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:lob documentation</h3>
	 * @Target({METHOD, FIELD}) @Retention(RUNTIME)
	 *         public @interface Lob {}
	 * </pre>
	 * @return the value of the lob child.
	 */
	@NotNull
	GenericDomValue<String> getLob();


	/**
	 * Returns the value of the temporal child.
	 * @return the value of the temporal child.
	 */
	@NotNull
	GenericDomValue<Temporal> getTemporal();


	/**
	 * Returns the value of the enumerated child.
	 * @return the value of the enumerated child.
	 */
	@NotNull
	GenericDomValue<Enumerated> getEnumerated();


}
