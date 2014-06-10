// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://maven.apache.org/POM/4.0.0:contributorsElemType interface.
 */
public interface Contributors extends DomElement {

	/**
	 * Returns the list of contributor children.
	 * @return the list of contributor children.
	 */
	@NotNull
	List<Contributor> getContributors();
	/**
	 * Adds new child to the list of contributor children.
	 * @return created child
	 */
	Contributor addContributor();


}
