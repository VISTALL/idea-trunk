// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.javaee.model.xml.ejb.MessageDrivenBean;
import com.intellij.javaee.model.xml.ejb.SessionBean;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:enterprise-beansType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:enterprise-beansType documentation</h3>
 * The enterprise-beansType declares one or more enterprise
 * 	beans. Each bean can be a session, entity or message-driven
 * 	bean.
 * </pre>
 */
public interface EnterpriseBeans extends CommonDomModelElement {

	/**
	 * Returns the list of session children.
	 * @return the list of session children.
	 */
	@NotNull
	List<SessionBean> getSessions();
	/**
	 * Adds new child to the list of session children.
	 * @return created child
	 */
	SessionBean addSession();


	/**
	 * Returns the list of entity children.
	 * @return the list of entity children.
	 */
	@NotNull
	List<EntityBean> getEntities();
	/**
	 * Adds new child to the list of entity children.
	 * @return created child
	 */
	EntityBean addEntity();


	/**
	 * Returns the list of message-driven children.
	 * @return the list of message-driven children.
	 */
	@NotNull
	List<MessageDrivenBean> getMessageDrivens();
	/**
	 * Adds new child to the list of message-driven children.
	 * @return created child
	 */
	MessageDrivenBean addMessageDriven();


}
