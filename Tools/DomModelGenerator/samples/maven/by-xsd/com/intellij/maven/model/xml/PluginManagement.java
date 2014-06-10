// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:PluginManagement interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:PluginManagement documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface PluginManagement extends DomElement {

	/**
	 * Returns the value of the plugins child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:plugins documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the plugins child.
	 */
	@NotNull
	Plugins getPlugins();


}
