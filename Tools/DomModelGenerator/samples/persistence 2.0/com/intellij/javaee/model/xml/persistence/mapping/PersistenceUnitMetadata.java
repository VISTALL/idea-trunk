// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:persistence-unit-metadata interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:persistence-unit-metadata documentation</h3>
 * Metadata that applies to the persistence unit and not just to 
 *         the mapping file in which it is contained. 
 *         If the xml-mapping-metadata-complete element is specified,
 *         the complete set of mapping metadata for the persistence unit 
 *         is contained in the XML mapping files for the persistence unit.
 * </pre>
 */
public interface PersistenceUnitMetadata extends JavaeeDomModelElement {

	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


	/**
	 * Returns the value of the xml-mapping-metadata-complete child.
	 * @return the value of the xml-mapping-metadata-complete child.
	 */
	@NotNull
	@SubTag (value = "xml-mapping-metadata-complete", indicator = true)
	GenericDomValue<Boolean> getXmlMappingMetadataComplete();


	/**
	 * Returns the value of the persistence-unit-defaults child.
	 * @return the value of the persistence-unit-defaults child.
	 */
	@NotNull
	PersistenceUnitDefaults getPersistenceUnitDefaults();


}
