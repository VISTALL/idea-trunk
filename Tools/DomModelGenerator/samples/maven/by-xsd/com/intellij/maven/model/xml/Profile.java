// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Profile interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Profile documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface Profile extends DomElement {

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
	 * Returns the value of the activation child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:activation documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the activation child.
	 */
	@NotNull
	Activation getActivation();


	/**
	 * Returns the value of the build child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:build documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the build child.
	 */
	@NotNull
	BuildBase getBuild();


	/**
	 * Returns the value of the modules child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:modules documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the modules child.
	 */
	@NotNull
	Modules getModules();


	/**
	 * Returns the value of the repositories child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:repositories documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the repositories child.
	 */
	@NotNull
	Repositories getRepositories();


	/**
	 * Returns the value of the pluginRepositories child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:pluginRepositories documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the pluginRepositories child.
	 */
	@NotNull
	PluginRepositories getPluginRepositories();


	/**
	 * Returns the value of the dependencies child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:dependencies documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the dependencies child.
	 */
	@NotNull
	Dependencies getDependencies();


	/**
	 * Returns the value of the reports child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:reports documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the reports child.
	 */
	@NotNull
	Reports getReports();


	/**
	 * Returns the value of the reporting child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:reporting documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the reporting child.
	 */
	@NotNull
	Reporting getReporting();


	/**
	 * Returns the value of the dependencyManagement child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:dependencyManagement documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the dependencyManagement child.
	 */
	@NotNull
	DependencyManagement getDependencyManagement();


	/**
	 * Returns the value of the distributionManagement child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:distributionManagement documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the distributionManagement child.
	 */
	@NotNull
	DistributionManagement getDistributionManagement();


	/**
	 * Returns the value of the properties child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:properties documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the properties child.
	 */
	@NotNull
	Properties getProperties();


}
