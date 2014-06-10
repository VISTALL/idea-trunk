// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:mapped-superclass interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:mapped-superclass documentation</h3>
 * Defines the settings and mappings for a mapped superclass. Is 
 *         allowed to be sparsely populated and used in conjunction with 
 *         the annotations. Alternatively, the metadata-complete attribute 
 *         can be used to indicate that no annotations are to be processed 
 *         If this is the case then the defaulting rules will be recursively 
 *         applied.
 *         @Target(TYPE) @Retention(RUNTIME)
 *         public @interface MappedSuperclass{}
 * </pre>
 */
public interface MappedSuperclass extends JavaeeDomModelElement {

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
	 * Returns the value of the id-class child.
	 * @return the value of the id-class child.
	 */
	@NotNull
	IdClass getIdClass();


	/**
	 * Returns the value of the exclude-default-listeners child.
	 * @return the value of the exclude-default-listeners child.
	 */
	@NotNull
	@SubTag (value = "exclude-default-listeners", indicator = true)
	GenericDomValue<Boolean> getExcludeDefaultListeners();


	/**
	 * Returns the value of the exclude-superclass-listeners child.
	 * @return the value of the exclude-superclass-listeners child.
	 */
	@NotNull
	@SubTag (value = "exclude-superclass-listeners", indicator = true)
	GenericDomValue<Boolean> getExcludeSuperclassListeners();


	/**
	 * Returns the value of the entity-listeners child.
	 * @return the value of the entity-listeners child.
	 */
	@NotNull
	EntityListeners getEntityListeners();


	/**
	 * Returns the value of the pre-persist child.
	 * @return the value of the pre-persist child.
	 */
	@NotNull
	PrePersist getPrePersist();


	/**
	 * Returns the value of the post-persist child.
	 * @return the value of the post-persist child.
	 */
	@NotNull
	PostPersist getPostPersist();


	/**
	 * Returns the value of the pre-remove child.
	 * @return the value of the pre-remove child.
	 */
	@NotNull
	PreRemove getPreRemove();


	/**
	 * Returns the value of the post-remove child.
	 * @return the value of the post-remove child.
	 */
	@NotNull
	PostRemove getPostRemove();


	/**
	 * Returns the value of the pre-update child.
	 * @return the value of the pre-update child.
	 */
	@NotNull
	PreUpdate getPreUpdate();


	/**
	 * Returns the value of the post-update child.
	 * @return the value of the post-update child.
	 */
	@NotNull
	PostUpdate getPostUpdate();


	/**
	 * Returns the value of the post-load child.
	 * @return the value of the post-load child.
	 */
	@NotNull
	PostLoad getPostLoad();


	/**
	 * Returns the value of the attributes child.
	 * @return the value of the attributes child.
	 */
	@NotNull
	Attributes getAttributes();


}
