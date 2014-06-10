// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.AttributeMemberConverter;
import com.intellij.hibernate.model.enums.AccessType;
import com.intellij.hibernate.model.enums.GeneratedType;
import com.intellij.hibernate.model.enums.SourceType;
import com.intellij.hibernate.model.enums.TimestampUnsavedValueType;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.psi.PsiMember;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-mapping-3.0.dtd:timestamp interface.
 */
public interface HbmTimestamp extends CommonDomModelElement, HbmAttributeBase, HbmColumnsHolderBase {

	/**
	 * Returns the value of the access child.
	 * Attribute access
	 * @return the value of the access child.
	 */
	@NotNull
	GenericAttributeValue<AccessType> getAccess();


	/**
	 * Returns the value of the generated child.
	 * Attribute generated
	 * @return the value of the generated child.
	 */
	@NotNull
	GenericAttributeValue<GeneratedType> getGenerated();


	/**
	 * Returns the value of the unsaved-value child.
	 * Attribute unsaved-value
	 * @return the value of the unsaved-value child.
	 */
	@NotNull
	GenericAttributeValue<TimestampUnsavedValueType> getUnsavedValue();


	/**
	 * Returns the value of the name child.
	 * Attribute name
	 * @return the value of the name child.
	 */
        @Required
        @Attribute("name")
        @Convert(AttributeMemberConverter.class)
        GenericAttributeValue<PsiMember> getTargetMember();


	/**
	 * Returns the value of the column child.
	 * Attribute column
	 * @return the value of the column child.
	 */
	@NotNull
        GenericAttributeValue<String> getColumn();


	/**
	 * Returns the value of the node child.
	 * Attribute node
	 * @return the value of the node child.
	 */
	@NotNull
	GenericAttributeValue<String> getNode();


	/**
	 * Returns the value of the source child.
	 * Attribute source
	 * @return the value of the source child.
	 */
	@NotNull
	GenericAttributeValue<SourceType> getSource();


	/**
	 * Returns the list of meta children.
	 * Type meta documentation
	 * <pre>
	 * 	META element definition; used to assign meta-level attributes to a class
	 * 	or property.  Is currently used by codegenerator as a placeholder for
	 * 	values that is not directly related to OR mappings.
	 * </pre>
	 * @return the list of meta children.
	 */
	@NotNull
	List<HbmMeta> getMetas();
	/**
	 * Adds new child to the list of meta children.
	 * @return created child
	 */
	HbmMeta addMeta();


}
