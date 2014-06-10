// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.Method;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:exclude-listType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:exclude-listType documentation</h3>
 * The exclude-listType specifies one or more methods which
 * 	the Assembler marks to be uncallable.
 * 	If the method permission relation contains methods that are
 * 	in the exclude list, the Deployer should consider those
 * 	methods to be uncallable.
 * </pre>
 */
public interface ExcludeList extends CommonDomModelElement {

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
	 * Returns the list of method children.
	 * @return the list of method children.
	 */
	@NotNull
	@Required
	List<Method> getMethods();
	/**
	 * Adds new child to the list of method children.
	 * @return created child
	 */
	Method addMethod();


}
