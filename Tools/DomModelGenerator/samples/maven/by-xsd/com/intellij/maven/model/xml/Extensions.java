// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://maven.apache.org/POM/4.0.0:extensionsElemType interface.
 */
public interface Extensions extends DomElement {

	/**
	 * Returns the list of extension children.
	 * @return the list of extension children.
	 */
	@NotNull
	List<Extension> getExtensions();
	/**
	 * Adds new child to the list of extension children.
	 * @return created child
	 */
	Extension addExtension();


}
