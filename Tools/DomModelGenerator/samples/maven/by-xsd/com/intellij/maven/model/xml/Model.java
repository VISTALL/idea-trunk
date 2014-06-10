// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Model interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Model documentation</h3>
 * 3.0.0+
 * </pre>
 */
public interface Model extends DomElement {

	/**
	 * Returns the value of the parent child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:parent documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the parent child.
	 */
	@NotNull
	Parent getParent();


	/**
	 * Returns the value of the modelVersion child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:modelVersion documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the modelVersion child.
	 */
	@NotNull
	GenericDomValue<String> getModelVersion();


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
	 * Returns the value of the packaging child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:packaging documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the packaging child.
	 */
	@NotNull
	GenericDomValue<String> getPackaging();


	/**
	 * Returns the value of the name child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:name documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the name child.
	 */
	@NotNull
	GenericDomValue<String> getName();


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
	 * Returns the value of the description child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:description documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the description child.
	 */
	@NotNull
	GenericDomValue<String> getDescription();


	/**
	 * Returns the value of the url child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:url documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the url child.
	 */
	@NotNull
	GenericDomValue<String> getUrl();


	/**
	 * Returns the value of the prerequisites child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:prerequisites documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the prerequisites child.
	 */
	@NotNull
	Prerequisites getPrerequisites();


	/**
	 * Returns the value of the issueManagement child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:issueManagement documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the issueManagement child.
	 */
	@NotNull
	IssueManagement getIssueManagement();


	/**
	 * Returns the value of the ciManagement child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:ciManagement documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the ciManagement child.
	 */
	@NotNull
	CiManagement getCiManagement();


	/**
	 * Returns the value of the inceptionYear child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:inceptionYear documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the inceptionYear child.
	 */
	@NotNull
	GenericDomValue<String> getInceptionYear();


	/**
	 * Returns the value of the mailingLists child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:mailingLists documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the mailingLists child.
	 */
	@NotNull
	MailingLists getMailingLists();


	/**
	 * Returns the value of the developers child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:developers documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the developers child.
	 */
	@NotNull
	Developers getDevelopers();


	/**
	 * Returns the value of the contributors child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:contributors documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the contributors child.
	 */
	@NotNull
	Contributors getContributors();


	/**
	 * Returns the value of the licenses child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:licenses documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the licenses child.
	 */
	@NotNull
	Licenses getLicenses();


	/**
	 * Returns the value of the scm child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:scm documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the scm child.
	 */
	@NotNull
	Scm getScm();


	/**
	 * Returns the value of the organization child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:organization documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the organization child.
	 */
	@NotNull
	Organization getOrganization();


	/**
	 * Returns the value of the build child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:build documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the build child.
	 */
	@NotNull
	Build getBuild();


	/**
	 * Returns the value of the profiles child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:profiles documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the profiles child.
	 */
	@NotNull
	Profiles getProfiles();


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
