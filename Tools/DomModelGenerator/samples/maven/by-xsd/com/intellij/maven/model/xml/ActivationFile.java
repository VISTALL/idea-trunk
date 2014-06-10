// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:ActivationFile interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:ActivationFile documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface ActivationFile extends DomElement {

	/**
	 * Returns the value of the missing child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:missing documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the missing child.
	 */
	@NotNull
	GenericDomValue<String> getMissing();


	/**
	 * Returns the value of the exists child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:exists documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the exists child.
	 */
	@NotNull
	GenericDomValue<String> getExists();


}
