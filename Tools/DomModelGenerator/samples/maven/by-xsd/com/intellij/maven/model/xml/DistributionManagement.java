// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:DistributionManagement interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:DistributionManagement documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface DistributionManagement extends DomElement {

	/**
	 * Returns the value of the repository child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:repository documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the repository child.
	 */
	@NotNull
	DeploymentRepository getRepository();


	/**
	 * Returns the value of the snapshotRepository child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:snapshotRepository documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the snapshotRepository child.
	 */
	@NotNull
	DeploymentRepository getSnapshotRepository();


	/**
	 * Returns the value of the site child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:site documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the site child.
	 */
	@NotNull
	Site getSite();


	/**
	 * Returns the value of the downloadUrl child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:downloadUrl documentation</h3>
	 * 4.0.0+
	 * </pre>
	 * @return the value of the downloadUrl child.
	 */
	@NotNull
	GenericDomValue<String> getDownloadUrl();


	/**
	 * Returns the value of the relocation child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:relocation documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the relocation child.
	 */
	@NotNull
	Relocation getRelocation();


	/**
	 * Returns the value of the status child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:status documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the status child.
	 */
	@NotNull
	GenericDomValue<String> getStatus();


}
