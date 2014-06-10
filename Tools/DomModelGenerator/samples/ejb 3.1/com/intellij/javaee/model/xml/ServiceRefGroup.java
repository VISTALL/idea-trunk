// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:service-refGroup model group interface.
 */
public interface ServiceRefGroup {

	@NotNull
	List<ServiceRef> getServiceRefs();
	ServiceRef addServiceRef();


}
