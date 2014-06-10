// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/persistence/orm:entity-listeners interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:entity-listeners documentation</h3>
 * @Target({TYPE}) @Retention(RUNTIME)
 *         public @interface EntityListeners {
 *           Class[] value();
 *         }
 * </pre>
 */
public interface EntityListeners extends JavaeeDomModelElement {

	/**
	 * Returns the list of entity-listener children.
	 * @return the list of entity-listener children.
	 */
	@NotNull
	List<EntityListener> getEntityListeners();
	/**
	 * Adds new child to the list of entity-listener children.
	 * @return created child
	 */
	EntityListener addEntityListener();


}
