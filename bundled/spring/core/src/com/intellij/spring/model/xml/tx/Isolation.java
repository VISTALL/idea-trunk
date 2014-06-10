// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/tx

package com.intellij.spring.model.xml.tx;

/**
 * http://www.springframework.org/schema/tx:isolationAttrType enumeration.
 * <pre>
 * <h3>Enumeration http://www.springframework.org/schema/tx:isolationAttrType documentation</h3>
 * 	The transaction isolation level.
 * 								
 * </pre>
 */
public enum Isolation {
	DEFAULT,
	READ_COMMITTED,
	READ_UNCOMMITTED,
	REPEATABLE_READ,
	SERIALIZABLE
}
