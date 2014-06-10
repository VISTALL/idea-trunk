// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/tx

package com.intellij.spring.model.xml.tx;

import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/tx:attributesType interface.
 */
public interface Attributes extends SpringTxElement {

	/**
	 * Returns the list of method children.
	 * @return the list of method children.
	 */
	@NotNull
	@Required
	List<SpringMethod> getMethods();
	/**
	 * Adds new child to the list of method children.
	 * @return created child
	 */
	SpringMethod addMethod();


}
