// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.EjbRelationshipRole;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:ejb-relationType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:ejb-relationType documentation</h3>
 * The ejb-relationType describes a relationship between two
 * 	entity beans with container-managed persistence.  It is used
 * 	by ejb-relation elements. It contains a description; an
 * 	optional ejb-relation-name element; and exactly two
 * 	relationship role declarations, defined by the
 * 	ejb-relationship-role elements. The name of the
 * 	relationship, if specified, is unique within the ejb-jar
 * 	file.
 * </pre>
 */
public interface EjbRelation extends CommonDomModelElement {

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
	 * Returns the value of the ejb-relation-name child.
	 * <pre>
	 * <h3>Element http://java.sun.com/xml/ns/javaee:ejb-relation-name documentation</h3>
	 * The ejb-relation-name element provides a unique name
	 * 	    within the ejb-jar file for a relationship.
	 * </pre>
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:string documentation</h3>
	 * This is a special string datatype that is defined by Java EE as
	 * 	a base type for defining collapsed strings. When schemas
	 * 	require trailing/leading space elimination as well as
	 * 	collapsing the existing whitespace, this base type may be
	 * 	used.
	 * </pre>
	 * @return the value of the ejb-relation-name child.
	 */
	@NotNull
	GenericDomValue<String> getEjbRelationName();


	/**
	 * Returns the value of the ejb-relationship-role child.
	 * @return the value of the ejb-relationship-role child.
	 */
	@NotNull
	@Required
	EjbRelationshipRole getEjbRelationshipRole();


}
