// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:PluginExecution interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:PluginExecution documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface PluginExecution extends DomElement {

	/**
	 * Returns the value of the id child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:id documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the id child.
	 */
	@NotNull
	GenericDomValue<String> getId();


	/**
	 * Returns the value of the phase child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:phase documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the phase child.
	 */
	@NotNull
	GenericDomValue<String> getPhase();


	/**
	 * Returns the value of the goals child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:goals documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the goals child.
	 */
	@NotNull
	Goals getGoals();


	/**
	 * Returns the value of the inherited child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:inherited documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the inherited child.
	 */
	@NotNull
	GenericDomValue<String> getInherited();


	/**
	 * Returns the value of the configuration child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:configuration documentation</h3>
	 * 0.0.0+
	 * </pre>
	 * @return the value of the configuration child.
	 */
	@NotNull
	Configuration getConfiguration();


}
