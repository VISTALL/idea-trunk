// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:listenerType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:listenerType documentation</h3>
 * The listenerType indicates the deployment properties for a web
 * 	application listener bean.
 * </pre>
 */
public interface Listener extends CommonDomModelElement, DescriptionGroup {

	/**
	 * Returns the value of the listener-class child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:listener-class documentation</h3>
	 * The listener-class element declares a class in the
	 * 	    application must be registered as a web
	 * 	    application listener bean. The value is the fully
	 * 	    qualified classname of the listener class.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:fully-qualified-classType documentation</h3>
	 * The elements that use this type designate the name of a
	 * 	Java class or interface.  The name is in the form of a
	 * 	"binary name", as defined in the JLS.  This is the form
	 * 	of name used in Class.forName().  Tools that need the
	 * 	canonical name (the name used in source code) will need
	 * 	to convert this binary name to the canonical name.
	 * </pre>
	 * @return the value of the listener-class child.
	 */
	@NotNull
	@Required
	GenericDomValue<PsiClass> getListenerClass();


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
