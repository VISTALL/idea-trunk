// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://maven.apache.org/POM/4.0.0:executionsElemType interface.
 */
public interface Executions extends DomElement {

	/**
	 * Returns the list of execution children.
	 * @return the list of execution children.
	 */
	@NotNull
	List<PluginExecution> getExecutions();
	/**
	 * Adds new child to the list of execution children.
	 * @return created child
	 */
	PluginExecution addExecution();


}
