// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.AssemblyDescriptor;
import com.intellij.javaee.model.xml.ejb.EnterpriseBeans;
import com.intellij.javaee.model.xml.ejb.Interceptors;
import com.intellij.javaee.model.xml.ejb.Relationships;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:ejb-jarType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:ejb-jarType documentation</h3>
 * The ejb-jarType defines the root element of the EJB
 * 	deployment descriptor. It contains
 * 	    - an optional description of the ejb-jar file
 * 	    - an optional display name
 * 	    - an optional icon that contains a small and a large
 * 	      icon file name
 *             - an optional module name. Only applicable to
 *               stand-alone ejb-jars or ejb-jars packaged in an .ear.
 * 	    - structural information about all included
 * 	      enterprise beans that is not specified through
 *               annotations
 *             - structural information about interceptor classes
 * 	    - a descriptor for container managed relationships,
 * 	      if any.
 * 	    - an optional application-assembly descriptor
 * 	    - an optional name of an ejb-client-jar file for the
 * 	      ejb-jar.
 * </pre>
 */
public interface EjbJar extends CommonDomModelElement, DescriptionGroup {

	/**
	 * Returns the value of the version child.
	 * <pre>
	 * <h3>Attribute null:version documentation</h3>
	 * The version specifies the version of the
	 * 	  EJB specification that the instance document must
	 * 	  comply with. This information enables deployment tools
	 * 	  to validate a particular EJB Deployment
	 * 	  Descriptor with respect to a specific version of the EJB
	 * 	  schema.
	 * </pre>
	 * @return the value of the version child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getVersion();


	/**
	 * Returns the value of the metadata-complete child.
	 * <pre>
	 * <h3>Attribute null:metadata-complete documentation</h3>
	 * The metadata-complete attribute defines whether this
	 * 	  deployment descriptor and other related deployment
	 * 	  descriptors for this module (e.g., web service
	 * 	  descriptors) are complete, or whether the class
	 * 	  files available to this module and packaged with
	 * 	  this application should be examined for annotations
	 * 	  that specify deployment information.
	 * 	  If metadata-complete is set to "true", the deployment
	 * 	  tool must ignore any annotations that specify deployment
	 * 	  information, which might be present in the class files
	 * 	  of the application.
	 * 	  If metadata-complete is not specified or is set to
	 * 	  "false", the deployment tool must examine the class
	 * 	  files of the application for annotations, as
	 * 	  specified by the specifications.
	 * </pre>
	 * @return the value of the metadata-complete child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getMetadataComplete();


	/**
	 * Returns the value of the module-name child.
	 * @return the value of the module-name child.
	 */
	@NotNull
	GenericDomValue<String> getModuleName();


	/**
	 * Returns the value of the enterprise-beans child.
	 * @return the value of the enterprise-beans child.
	 */
	@NotNull
	EnterpriseBeans getEnterpriseBeans();


	/**
	 * Returns the value of the interceptors child.
	 * @return the value of the interceptors child.
	 */
	@NotNull
	Interceptors getInterceptors();


	/**
	 * Returns the value of the relationships child.
	 * @return the value of the relationships child.
	 */
	@NotNull
	Relationships getRelationships();


	/**
	 * Returns the value of the assembly-descriptor child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:assembly-descriptor documentation</h3>
	 * Providing an assembly-descriptor in the deployment
	 * 	    descriptor is optional for the ejb-jar file
	 * 	    producer.
	 * </pre>
	 * @return the value of the assembly-descriptor child.
	 */
	@NotNull
	AssemblyDescriptor getAssemblyDescriptor();


	/**
	 * Returns the value of the ejb-client-jar child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:ejb-client-jar documentation</h3>
	 * 	      The optional ejb-client-jar element specifies a JAR
	 * 	      file that contains the class files necessary for a
	 * 	      client program to access the
	 * 	      enterprise beans in the ejb-jar file.
	 * 	      Example:
	 * 		  <ejb-client-jar>employee_service_client.jar
	 * 		  </ejb-client-jar>
	 * 	      
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:pathType documentation</h3>
	 * The elements that use this type designate either a relative
	 * 	path or an absolute path starting with a "/".
	 * 	In elements that specify a pathname to a file within the
	 * 	same Deployment File, relative filenames (i.e., those not
	 * 	starting with "/") are considered relative to the root of
	 * 	the Deployment File's namespace.  Absolute filenames (i.e.,
	 * 	those starting with "/") also specify names in the root of
	 * 	the Deployment File's namespace.  In general, relative names
	 * 	are preferred.  The exception is .war files where absolute
	 * 	names are preferred for consistency with the Servlet API.
	 * </pre>
	 * @return the value of the ejb-client-jar child.
	 */
	@NotNull
	GenericDomValue<String> getEjbClientJar();


	/**
	 * Returns the list of description children.
	 * @return the list of description children.
	 */
	@NotNull
	List<Description> getDescriptions();
	/**
	 * Adds new child to the list of description children.
	 * @return created child
	 */
	Description addDescription();


	/**
	 * Returns the list of display-name children.
	 * @return the list of display-name children.
	 */
	@NotNull
	List<DisplayName> getDisplayNames();
	/**
	 * Adds new child to the list of display-name children.
	 * @return created child
	 */
	DisplayName addDisplayName();


	/**
	 * Returns the list of icon children.
	 * @return the list of icon children.
	 */
	@NotNull
	List<Icon> getIcons();
	/**
	 * Adds new child to the list of icon children.
	 * @return created child
	 */
	Icon addIcon();


}
