// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:CiManagement interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:CiManagement documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface CiManagement extends DomElement {

	/**
	 * Returns the value of the system child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:system documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the system child.
	 */
	@NotNull
	GenericDomValue<String> getSystem();


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


	/**
	 * Returns the value of the notifiers child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:notifiers documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the notifiers child.
	 */
	@NotNull
	Notifiers getNotifiers();


}
