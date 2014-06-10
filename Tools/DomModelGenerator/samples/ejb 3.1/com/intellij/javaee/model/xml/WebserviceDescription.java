// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:webservice-descriptionType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:webservice-descriptionType documentation</h3>
 * The webservice-description element defines a WSDL document file
 * 	and the set of Port components associated with the WSDL ports
 * 	defined in the WSDL document.  There may be multiple
 * 	webservice-descriptions defined within a module.
 * 	All WSDL file ports must have a corresponding port-component element
 * 	defined.
 * 	Used in: webservices
 * </pre>
 */
public interface WebserviceDescription extends CommonDomModelElement {

	/**
	 * Returns the value of the description child.
	 * @return the value of the description child.
	 */
	@NotNull
	Description getDescription();


	/**
	 * Returns the value of the display-name child.
	 * @return the value of the display-name child.
	 */
	@NotNull
	DisplayName getDisplayName();


	/**
	 * Returns the value of the icon child.
	 * @return the value of the icon child.
	 */
	@NotNull
	Icon getIcon();


	/**
	 * Returns the value of the webservice-description-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:webservice-description-name documentation</h3>
	 * The webservice-description-name identifies the collection of
	 * 	    port-components associated with a WSDL file and JAX-RPC
	 * 	    mapping. The name must be unique within the deployment descriptor.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:string documentation</h3>
	 * This is a special string datatype that is defined by Java EE as
	 * 	a base type for defining collapsed strings. When schemas
	 * 	require trailing/leading space elimination as well as
	 * 	collapsing the existing whitespace, this base type may be
	 * 	used.
	 * </pre>
	 * @return the value of the webservice-description-name child.
	 */
	@NotNull
	@Required
	GenericDomValue<String> getWebserviceDescriptionName();


	/**
	 * Returns the value of the wsdl-file child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:wsdl-file documentation</h3>
	 * The wsdl-file element contains the name of a WSDL file in the
	 * 	    module.  The file name is a relative path within the module.
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
	 * @return the value of the wsdl-file child.
	 */
	@NotNull
	GenericDomValue<String> getWsdlFile();


	/**
	 * Returns the value of the jaxrpc-mapping-file child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:jaxrpc-mapping-file documentation</h3>
	 * The jaxrpc-mapping-file element contains the name of a file that
	 * 	    describes the JAX-RPC mapping between the Java interaces used by
	 * 	    the application and the WSDL description in the wsdl-file.  The
	 * 	    file name is a relative path within the module.
	 * 	    This is not required when JAX-WS based runtime is used.
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
	 * @return the value of the jaxrpc-mapping-file child.
	 */
	@NotNull
	GenericDomValue<String> getJaxrpcMappingFile();


	/**
	 * Returns the list of port-component children.
	 * @return the list of port-component children.
	 */
	@NotNull
	@Required
	List<PortComponent> getPortComponents();
	/**
	 * Adds new child to the list of port-component children.
	 * @return created child
	 */
	PortComponent addPortComponent();


}
