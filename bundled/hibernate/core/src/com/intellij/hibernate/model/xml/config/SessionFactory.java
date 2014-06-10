// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-configuration-3.0.dtd

package com.intellij.hibernate.model.xml.config;

import com.intellij.hibernate.model.converters.ParamValueConverter;
import com.intellij.hibernate.model.converters.SessionFactoryNameConverter;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.NameValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * hibernate-configuration-3.0.dtd:session-factory interface.
 */
public interface SessionFactory extends CommonDomModelElement, PersistencePackage {

	/**
	 * Returns the value of the name child.
	 * Attribute name
	 * @return the value of the name child.
	 */
	@NotNull
        @NameValue
        @Convert(SessionFactoryNameConverter.class)  
        GenericAttributeValue<String> getName();


	/**
	 * Returns the list of property children.
	 * @return the list of property children.
	 */
	@NotNull
        @Convert(ParamValueConverter.class)
        List<Property<Object>> getProperties();
	/**
	 * Adds new child to the list of property children.
	 * @return created child
	 */
	Property<Object> addProperty();


	/**
	 * Returns the list of mapping children.
	 * @return the list of mapping children.
	 */
	@NotNull
	List<Mapping> getMappings();
	/**
	 * Adds new child to the list of mapping children.
	 * @return created child
	 */
	Mapping addMapping();


	/**
	 * Returns the list of event children.
	 * @return the list of event children.
	 */
	@NotNull
	List<Event> getEvents();
	/**
	 * Adds new child to the list of event children.
	 * @return created child
	 */
	Event addEvent();


	/**
	 * Returns the list of listener children.
	 * @return the list of listener children.
	 */
	@NotNull
	List<Listener> getListeners();
	/**
	 * Adds new child to the list of listener children.
	 * @return created child
	 */
	Listener addListener();


	/**
	 * Returns the list of class-cache children.
	 * @return the list of class-cache children.
	 */
	@NotNull
	List<ClassCache> getClassCaches();
	/**
	 * Adds new child to the list of class-cache children.
	 * @return created child
	 */
	ClassCache addClassCache();


	/**
	 * Returns the list of collection-cache children.
	 * @return the list of collection-cache children.
	 */
	@NotNull
	List<CollectionCache> getCollectionCaches();
	/**
	 * Adds new child to the list of collection-cache children.
	 * @return created child
	 */
	CollectionCache addCollectionCache();


}
