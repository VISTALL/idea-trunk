// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:entity-listener interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:entity-listener documentation</h3>
 * Defines an entity listener to be invoked at lifecycle events
 *         for the entities that list this listener.
 * </pre>
 */
public interface EntityListener extends JavaeeDomModelElement {

	/**
	 * Returns the value of the class child.
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("class")
	@Required
	GenericAttributeValue<String> getClazz();


	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


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


}
