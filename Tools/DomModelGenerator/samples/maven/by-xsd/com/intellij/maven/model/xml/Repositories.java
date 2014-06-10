// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://maven.apache.org/POM/4.0.0:repositoriesElemType interface.
 */
public interface Repositories extends DomElement {

	/**
	 * Returns the list of repository children.
	 * @return the list of repository children.
	 */
	@NotNull
	List<Repository> getRepositories();
	/**
	 * Adds new child to the list of repository children.
	 * @return created child
	 */
	Repository addRepository();


}
