// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Parent interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Parent documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface Parent extends DomElement {

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
	 * Returns the value of the relativePath child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:relativePath documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the relativePath child.
	 */
	@NotNull
	GenericDomValue<String> getRelativePath();


}
