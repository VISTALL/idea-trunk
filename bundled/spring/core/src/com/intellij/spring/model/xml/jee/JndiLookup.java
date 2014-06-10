// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/jee

package com.intellij.spring.model.xml.jee;

import com.intellij.psi.PsiClass;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/jee:jndi-lookupElemType interface.
 */
public interface JndiLookup extends DomSpringBean, JndiLocated {

	/**
	 * Returns the value of the cache child.
	 * <pre>
	 * <h3>Attribute null:cache documentation</h3>
	 * 	Controls whether the object returned from the JNDI lookup is cached
	 * 	after the first lookup.
	 * 							
	 * </pre>
	 * @return the value of the cache child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getCache();


	/**
	 * Returns the value of the expected-type child.
	 * <pre>
	 * <h3>Attribute null:expected-type documentation</h3>
	 * 	The type that the located JNDI object is supposed to be assignable
	 * 	to, if indeed any.
	 * 							
	 * </pre>
	 * @return the value of the expected-type child.
	 */
	@NotNull
	GenericAttributeValue<PsiClass> getExpectedType();


	/**
	 * Returns the value of the lookup-on-startup child.
	 * <pre>
	 * <h3>Attribute null:lookup-on-startup documentation</h3>
	 * 	Controls whether the JNDI lookup is performed immediately on startup
	 * 	(if true, the default), or on first access (if false).
	 * 							
	 * </pre>
	 * @return the value of the lookup-on-startup child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getLookupOnStartup();


	/**
	 * Returns the value of the proxy-interface child.
	 * <pre>
	 * <h3>Attribute null:proxy-interface documentation</h3>
	 * 	The proxy interface to use for the JNDI object.
	 * 	
	 * 	Needs to be specified because the actual JNDI object type is not
	 * 	known in advance in case of a lazy lookup.
	 * 	
	 * 	Typically used in conjunction with "lookupOnStartup"=false and/or
	 * 	"cache"=false.
	 * 							
	 * </pre>
	 * @return the value of the proxy-interface child.
	 */
	@NotNull
	GenericAttributeValue<PsiClass> getProxyInterface();


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
