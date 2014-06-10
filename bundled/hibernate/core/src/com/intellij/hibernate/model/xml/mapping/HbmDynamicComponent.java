// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.enums.AccessType;
import com.intellij.hibernate.model.converters.AttributeMemberConverter;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.util.xml.*;
import com.intellij.psi.PsiMember;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-mapping-3.0.dtd:dynamic-component interface.
 * Type dynamic-component documentation
 * <pre>
 *  A dynamic-component maps columns of the database entity to a java.util.Map 
 * at the Java level 
 * </pre>
 */
public interface HbmDynamicComponent extends CommonDomModelElement, HbmAttributeBase {

	/**
	 * Returns the value of the access child.
	 * Attribute access
	 * @return the value of the access child.
	 */
	@NotNull
	GenericAttributeValue<AccessType> getAccess();


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
	 * Returns the value of the node child.
	 * Attribute node
	 * @return the value of the node child.
	 */
	@NotNull
	GenericAttributeValue<String> getNode();


	/**
	 * Returns the value of the insert child.
	 * Attribute insert
	 * @return the value of the insert child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getInsert();


	/**
	 * Returns the value of the unique child.
	 * Attribute unique
	 * @return the value of the unique child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getUnique();


	/**
	 * Returns the value of the optimistic-lock child.
	 * Attribute optimistic-lock
	 * @return the value of the optimistic-lock child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getOptimisticLock();


	/**
	 * Returns the value of the update child.
	 * Attribute update
	 * @return the value of the update child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getUpdate();


	/**
	 * Returns the list of property children.
	 * Type property documentation
	 * <pre>
	 *  Property of an entity class or component, component-element, composite-id, etc. 
	 * JavaBeans style properties are mapped to table columns. 
	 * </pre>
	 * @return the list of property children.
	 */
	@NotNull
	List<HbmProperty> getProperties();
	/**
	 * Adds new child to the list of property children.
	 * @return created child
	 */
	HbmProperty addProperty();


	/**
	 * Returns the list of many-to-one children.
	 * Type many-to-one documentation
	 * <pre>
	 *  Declares an association between two entities (Or from a component, component element,
	 * etc. to an entity). 
	 * </pre>
	 * @return the list of many-to-one children.
	 */
	@NotNull
	List<HbmManyToOne> getManyToOnes();
	/**
	 * Adds new child to the list of many-to-one children.
	 * @return created child
	 */
	HbmManyToOne addManyToOne();


	/**
	 * Returns the list of one-to-one children.
	 * Type one-to-one documentation
	 * <pre>
	 *  Declares a one-to-one association between two entities (Or from a component, 
	 * component element, etc. to an entity). 
	 * </pre>
	 * @return the list of one-to-one children.
	 */
	@NotNull
	List<HbmOneToOne> getOneToOnes();
	/**
	 * Adds new child to the list of one-to-one children.
	 * @return created child
	 */
	HbmOneToOne addOneToOne();


	/**
	 * Returns the list of component children.
	 * Type component documentation
	 * <pre>
	 *  A component is a user-defined class, persisted along with its containing entity
	 * to the table of the entity class. JavaBeans style properties of the component are
	 * mapped to columns of the table of the containing entity. A null component reference
	 * is mapped to null values in all columns and vice versa. Components do not support
	 * shared reference semantics. 
	 * </pre>
	 * @return the list of component children.
	 */
	@NotNull
	List<HbmComponent> getComponents();
	/**
	 * Adds new child to the list of component children.
	 * @return created child
	 */
	HbmComponent addComponent();


	/**
	 * Returns the list of dynamic-component children.
	 * Type dynamic-component documentation
	 * <pre>
	 *  A dynamic-component maps columns of the database entity to a java.util.Map 
	 * at the Java level 
	 * </pre>
	 * @return the list of dynamic-component children.
	 */
	@NotNull
	List<HbmDynamicComponent> getDynamicComponents();
	/**
	 * Adds new child to the list of dynamic-component children.
	 * @return created child
	 */
	HbmDynamicComponent addDynamicComponent();


	/**
	 * Returns the list of any children.
	 * Type any documentation
	 * <pre>
	 *  An "any" association is a polymorphic association to any table with
	 * the given identifier type. The first listed column is a VARCHAR column 
	 * holding the name of the class (for that row). 
	 * </pre>
	 * @return the list of any children.
	 */
	@NotNull
	List<HbmAny> getAnies();
	/**
	 * Adds new child to the list of any children.
	 * @return created child
	 */
	HbmAny addAny();


	/**
	 * Returns the list of map children.
	 * Type map documentation
	 * <pre>
	 *  Collection declarations nested inside a class declaration indicate a foreign key 
	 * relationship from the collection table to the enclosing class. 
	 * </pre>
	 * @return the list of map children.
	 */
	@NotNull
	List<HbmMap> getMaps();
	/**
	 * Adds new child to the list of map children.
	 * @return created child
	 */
	HbmMap addMap();


	/**
	 * Returns the list of set children.
	 * @return the list of set children.
	 */
	@NotNull
	List<HbmSet> getSets();
	/**
	 * Adds new child to the list of set children.
	 * @return created child
	 */
	HbmSet addSet();


	/**
	 * Returns the list of list children.
	 * @return the list of list children.
	 */
	@NotNull
	List<HbmList> getLists();
	/**
	 * Adds new child to the list of list children.
	 * @return created child
	 */
	HbmList addList();


	/**
	 * Returns the list of bag children.
	 * @return the list of bag children.
	 */
	@NotNull
	List<HbmBag> getBags();
	/**
	 * Adds new child to the list of bag children.
	 * @return created child
	 */
	HbmBag addBag();


	/**
	 * Returns the list of array children.
	 * @return the list of array children.
	 */
	@NotNull
	List<HbmArray> getArrays();
	/**
	 * Adds new child to the list of array children.
	 * @return created child
	 */
	HbmArray addArray();


	/**
	 * Returns the list of primitive-array children.
	 * @return the list of primitive-array children.
	 */
	@NotNull
	List<HbmPrimitiveArray> getPrimitiveArrays();
	/**
	 * Adds new child to the list of primitive-array children.
	 * @return created child
	 */
	HbmPrimitiveArray addPrimitiveArray();


}
