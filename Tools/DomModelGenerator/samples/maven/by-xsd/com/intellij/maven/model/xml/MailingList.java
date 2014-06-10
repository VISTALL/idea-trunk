// Generated on Mon Mar 17 15:54:26 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package com.intellij.maven.model.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://maven.apache.org/POM/4.0.0:MailingList interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:MailingList documentation</h3>
 * 3.0.0+
 * </pre>
 */
public interface MailingList extends DomElement {

	/**
	 * Returns the value of the name child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:name documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the name child.
	 */
	@NotNull
	GenericDomValue<String> getName();


	/**
	 * Returns the value of the subscribe child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:subscribe documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the subscribe child.
	 */
	@NotNull
	GenericDomValue<String> getSubscribe();


	/**
	 * Returns the value of the unsubscribe child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:unsubscribe documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the unsubscribe child.
	 */
	@NotNull
	GenericDomValue<String> getUnsubscribe();


	/**
	 * Returns the value of the post child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:post documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the post child.
	 */
	@NotNull
	GenericDomValue<String> getPost();


	/**
	 * Returns the value of the archive child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:archive documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the archive child.
	 */
	@NotNull
	GenericDomValue<String> getArchive();


	/**
	 * Returns the value of the otherArchives child.
	 * <pre>
	 * <h3>Element http://maven.apache.org/POM/4.0.0:otherArchives documentation</h3>
	 * 3.0.0+
	 * </pre>
	 * @return the value of the otherArchives child.
	 */
	@NotNull
	OtherArchives getOtherArchives();


}
