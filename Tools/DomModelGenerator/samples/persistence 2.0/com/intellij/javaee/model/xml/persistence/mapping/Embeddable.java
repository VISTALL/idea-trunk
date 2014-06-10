// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:embeddable interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:embeddable documentation</h3>
 * Defines the settings and mappings for embeddable objects. Is 
 *         allowed to be sparsely populated and used in conjunction with 
 *         the annotations. Alternatively, the metadata-complete attribute 
 *         can be used to indicate that no annotations are to be processed 
 *         in the class. If this is the case then the defaulting rules will 
 *         be recursively applied.
 *         @Target({TYPE}) @Retention(RUNTIME)
 *         public @interface Embeddable {}
 * </pre>
 */
public interface Embeddable extends JavaeeDomModelElement {

	/**
	 * Returns the value of the class child.
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("class")
	@Required
	GenericAttributeValue<String> getClazz();


	/**
	 * Returns the value of the access child.
	 * @return the value of the access child.
	 */
	@NotNull
	GenericAttributeValue<AccessType> getAccess();


	/**
	 * Returns the value of the metadata-complete child.
	 * @return the value of the metadata-complete child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getMetadataComplete();


	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


	/**
	 * Returns the value of the attributes child.
	 * @return the value of the attributes child.
	 */
	@NotNull
	EmbeddableAttributes getAttributes();


}
