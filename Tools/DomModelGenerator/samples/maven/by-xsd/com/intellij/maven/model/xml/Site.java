// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Site interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Site documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface Site extends DomElement {

	/**
	 * Returns the value of the id child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:id documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the id child.
	 */
	@NotNull
	GenericDomValue<String> getId();


	/**
	 * Returns the value of the name child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:name documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the name child.
	 */
	@NotNull
	GenericDomValue<String> getName();


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
