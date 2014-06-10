// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Scm interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Scm documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface Scm extends DomElement {

	/**
	 * Returns the value of the connection child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:connection documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the connection child.
	 */
	@NotNull
	GenericDomValue<String> getConnection();


	/**
	 * Returns the value of the developerConnection child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:developerConnection documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the developerConnection child.
	 */
	@NotNull
	GenericDomValue<String> getDeveloperConnection();


	/**
	 * Returns the value of the tag child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:tag documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the tag child.
	 */
	@NotNull
	GenericDomValue<String> getTag();


	/**
	 * Returns the value of the url child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:url documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the url child.
	 */
	@NotNull
	GenericDomValue<String> getUrl();


}
