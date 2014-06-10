// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Reporting interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Reporting documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface Reporting extends DomElement {

	/**
	 * Returns the value of the excludeDefaults child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:excludeDefaults documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the excludeDefaults child.
	 */
	@NotNull
	GenericDomValue<Boolean> getExcludeDefaults();


	/**
	 * Returns the value of the outputDirectory child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:outputDirectory documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the outputDirectory child.
	 */
	@NotNull
	GenericDomValue<String> getOutputDirectory();


	/**
	 * Returns the value of the plugins child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:plugins documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the plugins child.
	 */
	@NotNull
	Plugins getPlugins();


}
