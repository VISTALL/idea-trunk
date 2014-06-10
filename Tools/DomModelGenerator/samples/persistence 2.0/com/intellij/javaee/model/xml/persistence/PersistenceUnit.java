// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence

package com.intellij.javaee.model.xml.persistence;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence:persistence-unitElemType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence:persistence-unitElemType documentation</h3>
 * Configuration of a persistence unit.
 * </pre>
 */
public interface PersistenceUnit extends JavaeeDomModelElement {

	/**
	 * Returns the value of the name child.
	 * <pre>
	 * <h3>Attribute null:name documentation</h3>
	 * Name used in code to reference this persistence unit.
	 * </pre>
	 * @return the value of the name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the transaction-type child.
	 * <pre>
	 * <h3>Attribute null:transaction-type documentation</h3>
	 * Type of transactions used by EntityManagers from this 
	 *                   persistence unit.
	 * </pre>
	 * @return the value of the transaction-type child.
	 */
	@NotNull
	GenericAttributeValue<PersistenceUnitTransactionType> getTransactionType();


	/**
	 * Returns the value of the description child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:description documentation</h3>
	 * Description of this persistence unit.
	 * </pre>
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


	/**
	 * Returns the value of the provider child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:provider documentation</h3>
	 * Provider class that supplies EntityManagers for this 
	 *                     persistence unit.
	 * </pre>
	 * @return the value of the provider child.
	 */
	@NotNull
	GenericDomValue<String> getProvider();


	/**
	 * Returns the value of the jta-data-source child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:jta-data-source documentation</h3>
	 * The container-specific name of the JTA datasource to use.
	 * </pre>
	 * @return the value of the jta-data-source child.
	 */
	@NotNull
	GenericDomValue<String> getJtaDataSource();


	/**
	 * Returns the value of the non-jta-data-source child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:non-jta-data-source documentation</h3>
	 * The container-specific name of a non-JTA datasource to use.
	 * </pre>
	 * @return the value of the non-jta-data-source child.
	 */
	@NotNull
	GenericDomValue<String> getNonJtaDataSource();


	/**
	 * Returns the list of mapping-file children.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:mapping-file documentation</h3>
	 * File containing mapping information. Loaded as a resource 
	 *                     by the persistence provider.
	 * </pre>
	 * @return the list of mapping-file children.
	 */
	@NotNull
	List<GenericDomValue<String>> getMappingFiles();
	/**
	 * Adds new child to the list of mapping-file children.
	 * @return created child
	 */
	GenericDomValue<String> addMappingFile();


	/**
	 * Returns the list of jar-file children.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:jar-file documentation</h3>
	 * Jar file that should be scanned for entities. 
	 *                     Not applicable to Java SE persistence units.
	 * </pre>
	 * @return the list of jar-file children.
	 */
	@NotNull
	List<GenericDomValue<String>> getJarFiles();
	/**
	 * Adds new child to the list of jar-file children.
	 * @return created child
	 */
	GenericDomValue<String> addJarFile();


	/**
	 * Returns the list of class children.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:class documentation</h3>
	 * Class to scan for annotations.  It should be annotated 
	 *                     with either @Entity, @Embeddable or @MappedSuperclass.
	 * </pre>
	 * @return the list of class children.
	 */
	@NotNull
	List<GenericDomValue<String>> getClasses();
	/**
	 * Adds new child to the list of class children.
	 * @return created child
	 */
	GenericDomValue<String> addClass();


	/**
	 * Returns the value of the exclude-unlisted-classes child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:exclude-unlisted-classes documentation</h3>
	 * When set to true then only listed classes and jars will 
	 *                     be scanned for persistent classes, otherwise the enclosing 
	 *                     jar or directory will also be scanned. Not applicable to 
	 *                     Java SE persistence units.
	 * </pre>
	 * @return the value of the exclude-unlisted-classes child.
	 */
	@NotNull
	GenericDomValue<Boolean> getExcludeUnlistedClasses();


	/**
	 * Returns the value of the caching child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:caching documentation</h3>
	 * Defines whether caching is enabled for the 
	 * persistence unit if caching is supported by the
	 * persistence provider. When set to ALL, all entities 
	 * will be cached. When set to NONE, no entities will
	 * be cached. When set to ENABLE_SELECTIVE, only entities
	 * specified as cacheable will be cached. When set to
	 * DISABLE_SELECTIVE, entities specified as not cacheable
	 * will not be cached.
	 * </pre>
	 * @return the value of the caching child.
	 */
	@NotNull
	GenericDomValue<PersistenceUnitCachingType> getCaching();


	/**
	 * Returns the value of the validation-mode child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:validation-mode documentation</h3>
	 * Specifies the validation mode to be used for the 
	 * persistence unit.
	 * </pre>
	 * @return the value of the validation-mode child.
	 */
	@NotNull
	GenericDomValue<PersistenceUnitValidationModeType> getValidationMode();


	/**
	 * Returns the value of the properties child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/persistence:properties documentation</h3>
	 * A list of vendor-specific properties.
	 * </pre>
	 * @return the value of the properties child.
	 */
	@NotNull
	Properties getProperties();


}
