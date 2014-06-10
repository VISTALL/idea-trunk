// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Plugin interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Plugin documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface Plugin extends DomElement {

	/**
	 * Returns the value of the groupId child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:groupId documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the groupId child.
	 */
	@NotNull
	GenericDomValue<String> getGroupId();


	/**
	 * Returns the value of the artifactId child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:artifactId documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the artifactId child.
	 */
	@NotNull
	GenericDomValue<String> getArtifactId();


	/**
	 * Returns the value of the version child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:version documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the version child.
	 */
	@NotNull
	GenericDomValue<String> getVersion();


	/**
	 * Returns the value of the extensions child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:extensions documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the extensions child.
	 */
	@NotNull
	GenericDomValue<Boolean> getExtensions();


	/**
	 * Returns the value of the executions child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:executions documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the executions child.
	 */
	@NotNull
	Executions getExecutions();


	/**
	 * Returns the value of the dependencies child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:dependencies documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the dependencies child.
	 */
	@NotNull
	Dependencies getDependencies();


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
