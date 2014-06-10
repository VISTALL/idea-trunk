// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:DependencyManagement interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:DependencyManagement documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface DependencyManagement extends DomElement {

	/**
	 * Returns the value of the dependencies child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:dependencies documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the dependencies child.
	 */
	@NotNull
	Dependencies getDependencies();


}
