// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:ReportPlugin interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:ReportPlugin documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface ReportPlugin extends DomElement {

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
	 * 4.0.0
	 * </pre>
	 * @return the value of the configuration child.
	 */
	@NotNull
	Configuration getConfiguration();


	/**
	 * Returns the value of the reportSets child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:reportSets documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the reportSets child.
	 */
	@NotNull
	ReportSets getReportSets();


}
