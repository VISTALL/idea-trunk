// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://maven.apache.org/POM/4.0.0:exclusionsElemType interface.
 */
public interface Exclusions extends DomElement {

	/**
	 * Returns the list of exclusion children.
	 * @return the list of exclusion children.
	 */
	@NotNull
	List<Exclusion> getExclusions();
	/**
	 * Adds new child to the list of exclusion children.
	 * @return created child
	 */
	Exclusion addExclusion();


}
