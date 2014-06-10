// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:RepositoryPolicy interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:RepositoryPolicy documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface RepositoryPolicy extends DomElement {

	/**
	 * Returns the value of the enabled child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:enabled documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the enabled child.
	 */
	@NotNull
	GenericDomValue<Boolean> getEnabled();


	/**
	 * Returns the value of the updatePolicy child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:updatePolicy documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the updatePolicy child.
	 */
	@NotNull
	GenericDomValue<String> getUpdatePolicy();


	/**
	 * Returns the value of the checksumPolicy child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:checksumPolicy documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the checksumPolicy child.
	 */
	@NotNull
	GenericDomValue<String> getChecksumPolicy();


}
