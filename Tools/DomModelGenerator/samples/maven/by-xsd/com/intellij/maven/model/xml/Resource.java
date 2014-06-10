// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Resource interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Resource documentation</h3>
 * 3.0.0+
 * </pre>
 */
public interface Resource extends DomElement {

	/**
	 * Returns the value of the targetPath child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:targetPath documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the targetPath child.
	 */
	@NotNull
	GenericDomValue<String> getTargetPath();


	/**
	 * Returns the value of the filtering child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:filtering documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the filtering child.
	 */
	@NotNull
	GenericDomValue<Boolean> getFiltering();


	/**
	 * Returns the value of the directory child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:directory documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the directory child.
	 */
	@NotNull
	GenericDomValue<String> getDirectory();


	/**
	 * Returns the value of the includes child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:includes documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the includes child.
	 */
	@NotNull
	Includes getIncludes();


	/**
	 * Returns the value of the excludes child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:excludes documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the excludes child.
	 */
	@NotNull
	Excludes getExcludes();


}
