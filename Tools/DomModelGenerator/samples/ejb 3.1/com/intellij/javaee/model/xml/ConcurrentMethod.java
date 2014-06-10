// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.enums.ConcurrentLockType;
import com.intellij.javaee.model.xml.ejb.AccessTimeout;
import com.intellij.javaee.model.xml.ejb.NamedMethod;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://java.sun.com/xml/ns/javaee:concurrent-methodType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:concurrent-methodType documentation</h3>
 * The concurrent-methodType specifies information about a method
 * 	of a bean with container managed concurrency.
 * 	The optional lock element specifies the kind of concurrency
 * 	lock asssociated with the method.
 * 	The optional access-timeout element specifies the amount of
 * 	time (in a given time unit) the container should wait for a
 * 	concurrency lock before throwing an exception to the client.
 * </pre>
 */
public interface ConcurrentMethod extends CommonDomModelElement {

	/**
	 * Returns the value of the method child.
	 * @return the value of the method child.
	 */
	@NotNull
	@Required
	NamedMethod getMethod();


	/**
	 * Returns the value of the lock child.
	 * @return the value of the lock child.
	 */
	@NotNull
	GenericDomValue<ConcurrentLockType> getLock();


	/**
	 * Returns the value of the access-timeout child.
	 * @return the value of the access-timeout child.
	 */
	@NotNull
	AccessTimeout getAccessTimeout();


}
