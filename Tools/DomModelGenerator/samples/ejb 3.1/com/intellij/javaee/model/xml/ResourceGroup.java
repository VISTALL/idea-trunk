// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:resourceGroup model group interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:resourceGroup documentation</h3>
 * This group collects elements that are common to all the
 * 	JNDI resource elements.
 * </pre>
 */
public interface ResourceGroup {

	@NotNull
	GenericDomValue<String> getMappedName();


	@NotNull
	List<InjectionTarget> getInjectionTargets();
	InjectionTarget addInjectionTarget();


}
