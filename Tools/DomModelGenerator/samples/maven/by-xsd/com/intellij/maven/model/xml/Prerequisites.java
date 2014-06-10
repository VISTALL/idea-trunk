// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:Prerequisites interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Prerequisites documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface Prerequisites extends DomElement {

	/**
	 * Returns the value of the maven child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:maven documentation</h3>
	 * 4.0.0
	 * </pre>
	 * @return the value of the maven child.
	 */
	@NotNull
	GenericDomValue<String> getMaven();


}
