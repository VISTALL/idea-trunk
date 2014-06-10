// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.EjbRelation;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:relationshipsType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:relationshipsType documentation</h3>
 * The relationshipsType describes the relationships in
 * 	which entity beans with container-managed persistence
 * 	participate. The relationshipsType contains an optional
 * 	description; and a list of ejb-relation elements, which
 * 	specify the container managed relationships.
 * </pre>
 */
public interface Relationships extends CommonDomModelElement {

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
	 * Returns the list of ejb-relation children.
	 * @return the list of ejb-relation children.
	 */
	@NotNull
	@Required
	List<EjbRelation> getEjbRelations();
	/**
	 * Adds new child to the list of ejb-relation children.
	 * @return created child
	 */
	EjbRelation addEjbRelation();


}
