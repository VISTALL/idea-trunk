// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://maven.apache.org/POM/4.0.0:filtersElemType interface.
 */
public interface Filters extends DomElement {

	/**
	 * Returns the list of filter children.
	 * @return the list of filter children.
	 */
	@NotNull
	List<GenericDomValue<String>> getFilters();
	/**
	 * Adds new child to the list of filter children.
	 * @return created child
	 */
	GenericDomValue<String> addFilter();


}
