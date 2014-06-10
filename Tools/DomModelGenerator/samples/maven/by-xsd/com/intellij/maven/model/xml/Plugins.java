// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://maven.apache.org/POM/4.0.0:pluginsElemType interface.
 */
public interface Plugins extends DomElement {

	/**
	 * Returns the list of plugin children.
	 * @return the list of plugin children.
	 */
	@NotNull
	List<Plugin> getPlugins();
	/**
	 * Adds new child to the list of plugin children.
	 * @return created child
	 */
	Plugin addPlugin();


}
