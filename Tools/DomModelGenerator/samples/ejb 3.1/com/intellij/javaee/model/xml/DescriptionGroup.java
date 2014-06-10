// Generated on Wed Apr 29 15:54:25 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:descriptionGroup model group interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:descriptionGroup documentation</h3>
 * This group keeps the usage of the contained description related
 * 	elements consistent across Java EE deployment descriptors.
 * 	All elements may occur multiple times with different languages,
 * 	to support localization of the content.
 * </pre>
 */
public interface DescriptionGroup {

	@NotNull
	List<Description> getDescriptions();
	Description addDescription();


	@NotNull
	List<DisplayName> getDisplayNames();
	DisplayName addDisplayName();


	@NotNull
	List<Icon> getIcons();
	Icon addIcon();


}
