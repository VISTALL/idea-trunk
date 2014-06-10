// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Dependency interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Dependency documentation</h3>
 * 3.0.0+
 * </pre>
 */
public interface Dependency extends DomElement {

	/**
	 * Returns the value of the groupId child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:groupId documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the groupId child.
	 */
	@NotNull
	GenericDomValue<String> getGroupId();


	/**
	 * Returns the value of the artifactId child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:artifactId documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the artifactId child.
	 */
	@NotNull
	GenericDomValue<String> getArtifactId();


	/**
	 * Returns the value of the version child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:version documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the version child.
	 */
	@NotNull
	GenericDomValue<String> getVersion();


	/**
	 * Returns the value of the type child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:type documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the type child.
	 */
	@NotNull
	GenericDomValue<String> getType();


	/**
	 * Returns the value of the classifier child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:classifier documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the classifier child.
	 */
	@NotNull
	GenericDomValue<String> getClassifier();


	/**
	 * Returns the value of the scope child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:scope documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the scope child.
	 */
	@NotNull
	GenericDomValue<String> getScope();


	/**
	 * Returns the value of the systemPath child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:systemPath documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the systemPath child.
	 */
	@NotNull
	GenericDomValue<String> getSystemPath();


	/**
	 * Returns the value of the exclusions child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:exclusions documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the exclusions child.
	 */
	@NotNull
	Exclusions getExclusions();


	/**
	 * Returns the value of the optional child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:optional documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the optional child.
	 */
	@NotNull
	GenericDomValue<Boolean> getOptional();


}
