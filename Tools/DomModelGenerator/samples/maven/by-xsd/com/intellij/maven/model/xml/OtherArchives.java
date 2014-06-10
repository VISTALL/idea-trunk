// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://maven.apache.org/POM/4.0.0:otherArchivesElemType interface.
 */
public interface OtherArchives extends DomElement {

	/**
	 * Returns the list of otherArchive children.
	 * @return the list of otherArchive children.
	 */
	@NotNull
	List<GenericDomValue<String>> getOtherArchives();
	/**
	 * Adds new child to the list of otherArchive children.
	 * @return created child
	 */
	GenericDomValue<String> addOtherArchive();


}
