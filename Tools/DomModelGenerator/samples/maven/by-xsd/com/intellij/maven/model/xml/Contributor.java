// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Contributor interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Contributor documentation</h3>
 * 3.0.0+
 * </pre>
 */
public interface Contributor extends DomElement {

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
	 * Returns the value of the email child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:email documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the email child.
	 */
	@NotNull
	GenericDomValue<String> getEmail();


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
	 * Returns the value of the organization child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:organization documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the organization child.
	 */
	@NotNull
	GenericDomValue<String> getOrganization();


	/**
	 * Returns the value of the organizationUrl child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:organizationUrl documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the organizationUrl child.
	 */
	@NotNull
	GenericDomValue<String> getOrganizationUrl();


	/**
	 * Returns the value of the roles child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:roles documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the roles child.
	 */
	@NotNull
	Roles getRoles();


	/**
	 * Returns the value of the timezone child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:timezone documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the timezone child.
	 */
	@NotNull
	GenericDomValue<String> getTimezone();


	/**
	 * Returns the value of the properties child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:properties documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the properties child.
	 */
	@NotNull
	Properties getProperties();


}
