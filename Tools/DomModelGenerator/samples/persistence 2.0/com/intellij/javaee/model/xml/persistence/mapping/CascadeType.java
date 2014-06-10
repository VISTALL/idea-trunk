// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

import com.intellij.javaee.model.JavaeeDomModelElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/persistence/orm:cascade-type interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/persistence/orm:cascade-type documentation</h3>
 * public enum CascadeType { ALL, PERSIST, MERGE, REMOVE, REFRESH};
 * </pre>
 */
public interface CascadeType extends JavaeeDomModelElement {

	/**
	 * Returns the value of the cascade-all child.
	 * @return the value of the cascade-all child.
	 */
	@NotNull
	@SubTag (value = "cascade-all", indicator = true)
	GenericDomValue<Boolean> getCascadeAll();


	/**
	 * Returns the value of the cascade-persist child.
	 * @return the value of the cascade-persist child.
	 */
	@NotNull
	@SubTag (value = "cascade-persist", indicator = true)
	GenericDomValue<Boolean> getCascadePersist();


	/**
	 * Returns the value of the cascade-merge child.
	 * @return the value of the cascade-merge child.
	 */
	@NotNull
	@SubTag (value = "cascade-merge", indicator = true)
	GenericDomValue<Boolean> getCascadeMerge();


	/**
	 * Returns the value of the cascade-remove child.
	 * @return the value of the cascade-remove child.
	 */
	@NotNull
	@SubTag (value = "cascade-remove", indicator = true)
	GenericDomValue<Boolean> getCascadeRemove();


	/**
	 * Returns the value of the cascade-refresh child.
	 * @return the value of the cascade-refresh child.
	 */
	@NotNull
	@SubTag (value = "cascade-refresh", indicator = true)
	GenericDomValue<Boolean> getCascadeRefresh();


}
