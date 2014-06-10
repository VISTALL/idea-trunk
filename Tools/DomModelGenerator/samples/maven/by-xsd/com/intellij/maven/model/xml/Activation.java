// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Activation interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Activation documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface Activation extends DomElement {

	/**
	 * Returns the value of the activeByDefault child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:activeByDefault documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the activeByDefault child.
	 */
	@NotNull
	GenericDomValue<Boolean> getActiveByDefault();


	/**
	 * Returns the value of the jdk child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:jdk documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the jdk child.
	 */
	@NotNull
	GenericDomValue<String> getJdk();


	/**
	 * Returns the value of the os child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:os documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the os child.
	 */
	@NotNull
	ActivationOS getOs();


	/**
	 * Returns the value of the property child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:property documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the property child.
	 */
	@NotNull
	ActivationProperty getProperty();


	/**
	 * Returns the value of the file child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:file documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the file child.
	 */
	@NotNull
	ActivationFile getFile();


}
