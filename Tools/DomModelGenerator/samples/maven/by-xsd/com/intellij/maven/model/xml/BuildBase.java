// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:BuildBase interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:BuildBase documentation</h3>
 * 3.0.0+
 * </pre>
 */
public interface BuildBase extends DomElement {

	/**
	 * Returns the value of the defaultGoal child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:defaultGoal documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the defaultGoal child.
	 */
	@NotNull
	GenericDomValue<String> getDefaultGoal();


	/**
	 * Returns the value of the resources child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:resources documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the resources child.
	 */
	@NotNull
	Resources getResources();


	/**
	 * Returns the value of the testResources child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:testResources documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the testResources child.
	 */
	@NotNull
	TestResources getTestResources();


	/**
	 * Returns the value of the directory child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:directory documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the directory child.
	 */
	@NotNull
	GenericDomValue<String> getDirectory();


	/**
	 * Returns the value of the finalName child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:finalName documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the finalName child.
	 */
	@NotNull
	GenericDomValue<String> getFinalName();


	/**
	 * Returns the value of the filters child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:filters documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the filters child.
	 */
	@NotNull
	Filters getFilters();


	/**
	 * Returns the value of the pluginManagement child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:pluginManagement documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the pluginManagement child.
	 */
	@NotNull
	PluginManagement getPluginManagement();


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
