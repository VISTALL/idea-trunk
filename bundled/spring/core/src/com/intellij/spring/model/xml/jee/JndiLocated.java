// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/jee

package com.intellij.spring.model.xml.jee;

import com.intellij.spring.model.xml.beans.Identified;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/jee:jndiLocatedType interface.
 */
public interface JndiLocated extends SpringJeeElement, Identified {

	/**
	 * Returns the value of the jndi-name child.
	 * <pre>
	 * <h3>Attribute null:jndi-name documentation</h3>
	 * 	The JNDI name to look up.
	 * 							
	 * </pre>
	 * @return the value of the jndi-name child.
	 */
	@NotNull
	@Required
	GenericAttributeValue<String> getJndiName();


	/**
	 * Returns the value of the resource-ref child.
	 * <pre>
	 * <h3>Attribute null:resource-ref documentation</h3>
	 * 	Controls whether the lookup occurs in a J2EE container, i.e. if the
	 * 	prefix "java:comp/env/" needs to be added if the JNDI name doesn't
	 * 	already contain it.
	 * 							
	 * </pre>
	 * @return the value of the resource-ref child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getResourceRef();


	/**
	 * Returns the value of the environment child.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/jee:environment documentation</h3>
	 * 	The newline-separated, key-value pairs for the JNDI environment
	 * 	(in standard Properties format, namely 'key=value' pairs)
	 * 						 
	 * </pre>
	 * @return the value of the environment child.
	 */
	@NotNull
	GenericDomValue<String> getEnvironment();


}
